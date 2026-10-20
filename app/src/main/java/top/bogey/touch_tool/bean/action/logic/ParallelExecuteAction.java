package top.bogey.touch_tool.bean.action.logic;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.Action;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.DynamicPinsAction;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.action.start.InnerStartAction;
import top.bogey.touch_tool.bean.other.log.ActionLog;
import top.bogey.touch_tool.bean.other.log.LogInfo;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinAdd;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBoolean;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_execute.PinExecute;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinInteger;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinNumber;
import top.bogey.touch_tool.bean.pin.special_pin.AlwaysShowPin;
import top.bogey.touch_tool.bean.save.log.LogSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.service.ITaskListener;
import top.bogey.touch_tool.service.MainAccessibilityService;
import top.bogey.touch_tool.service.TaskRunnable;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.tree.ITreeNodeData;

public class ParallelExecuteAction extends ExecuteAction implements DynamicPinsAction {
    private final static Pin morePin = new Pin(new PinExecute(), R.string.pin_execute, true);

    private final transient Pin countPin = new Pin(new PinInteger(1), R.string.parallel_action_count);
    private final transient Pin timeoutPin = new Pin(new PinInteger(5000), R.string.parallel_action_timeout);

    private final transient Pin addPin = new AlwaysShowPin(new PinAdd(morePin), R.string.pin_add_execute, true);
    private final transient Pin resultPin = new Pin(new PinBoolean(), R.string.pin_boolean_result, true);
    private final transient Pin completePin = new Pin(new PinExecute(), R.string.random_action_complete, true);


    public ParallelExecuteAction() {
        super(ActionType.PARALLEL_LOGIC);
        Pin secondPin = new Pin(new PinExecute(), R.string.pin_execute, true, true);
        addPins(countPin, timeoutPin, secondPin, addPin, resultPin, completePin);
    }

    public ParallelExecuteAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(countPin, timeoutPin);
        reAddPins(morePin);
        reAddPins(addPin, resultPin, completePin);
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        PinNumber<?> count = getPinValue(runnable, countPin);
        PinNumber<?> timeout = getPinValue(runnable, timeoutPin);

        MainAccessibilityService service = MainApplication.getInstance().getService();

        List<Pin> validPins = new ArrayList<>();
        for (Pin dynamicPin : getDynamicPins()) {
            if (!dynamicPin.isLinked()) continue;
            validPins.add(dynamicPin);
        }

        CountDownLatch latch = new CountDownLatch(validPins.size());
        int countValue = count.intValue();
        List<TaskRunnable> runnableList = new ArrayList<>();

        Task currTask = runnable.getTask();
        if (runnable.isDebug()) currTask.addFlag(Task.FLAG_DEBUG);

        for (Pin dynamicPin : validPins) {
            TaskRunnable taskRunnable = service.runTask(currTask, new InnerStartAction(dynamicPin), new ITaskListener() {
                @Override
                public void onExecute(TaskRunnable run, Action action, int progress) {
                    if (runnable.isCurrentInterrupt()) run.stop();
                }

                @Override
                public void onFinish(TaskRunnable run) {
                    List<LogInfo> logList = run.getCacheLogList();
                    List<String> logs = saveLogs(runnable, logList);
                    LogInfo logInfo = new LogInfo(new ActionLog(runnable.getProgress() + 1, currTask, ParallelExecuteAction.this, true));
                    logInfo.setChildren(logs);
                    runnable.addLog(logInfo, 0);

                    latch.countDown();
                    if (countValue <= latch.getCount()) {
                        runnableList.forEach(TaskRunnable::stop);
                        resultPin.getValue(PinBoolean.class).setValue(true);
                    }
                }
            });
            runnableList.add(taskRunnable);
        }

        ITaskListener listener = new ITaskListener() {
            @Override
            public void onPauseChanged(TaskRunnable runnable, boolean paused) {
                for (TaskRunnable taskRunnable : runnableList) {
                    if (paused) taskRunnable.forcePause();
                    else taskRunnable.forceResume();
                }
            }
        };
        runnable.addListener(listener);

        try {
            if (timeout.intValue() <= 0) {
                latch.await();
            } else {
                latch.await(timeout.intValue(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            AppLog.e(e);
            resultPin.getValue(PinBoolean.class).setValue(false);
        }
        runnable.removeListener(listener);

        executeNext(runnable, completePin);
    }

    private List<String> saveLogs(TaskRunnable runnable, List<LogInfo> logList) {
        List<String> logs = new ArrayList<>();
        for (LogInfo logInfo : logList) {
            logs.add(logInfo.getUid());
            LogSaver.getInstance().addLog(runnable.getStartTask().getId(), logInfo, false);
            List<LogInfo> children = new ArrayList<>();
            for (ITreeNodeData child : logInfo.getChildrenData()) {
                LogInfo childLogInfo = (LogInfo) child;
                children.add(childLogInfo);
            }
            saveLogs(runnable, children);
        }
        return logs;
    }

    @Override
    public List<Pin> getDynamicPins() {
        List<Pin> pins = new ArrayList<>();
        pins.add(outPin);
        boolean start = false;
        for (Pin pin : getPins()) {
            if (pin == addPin) start = false;
            if (start) pins.add(pin);
            if (pin == timeoutPin) start = true;
        }
        return pins;
    }
}
