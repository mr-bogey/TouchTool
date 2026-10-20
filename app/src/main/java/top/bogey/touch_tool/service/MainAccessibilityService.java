package top.bogey.touch_tool.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.hardware.HardwareBuffer;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.Action;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.start.BroadcastStartAction;
import top.bogey.touch_tool.bean.action.start.StartAction;
import top.bogey.touch_tool.bean.action.start.TimeStartAction;
import top.bogey.touch_tool.bean.other.log.LogInfo;
import top.bogey.touch_tool.bean.other.log.NormalLog;
import top.bogey.touch_tool.bean.save.log.LogSaver;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.bean.save.task.TaskSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.service.capture.CaptureService;
import top.bogey.touch_tool.service.receiver.BootCompletedReceiver;
import top.bogey.touch_tool.service.receiver.SystemEventReceiver;
import top.bogey.touch_tool.service.super_user.SuperUser;
import top.bogey.touch_tool.ui.FloatViewActivity;
import top.bogey.touch_tool.ui.InstantActivity;
import top.bogey.touch_tool.ui.MainActivity;
import top.bogey.touch_tool.ui.PermissionActivity;
import top.bogey.touch_tool.ui.custom.float_view.KeepAliveFloatView;
import top.bogey.touch_tool.ui.play.PlayFloatView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.ThreadUtil;
import top.bogey.touch_tool.utils.callback.BooleanResultCallback;
import top.bogey.touch_tool.utils.float_window_manager.FloatWindow;

@SuppressLint("AccessibilityPolicy")
public class MainAccessibilityService extends AccessibilityService {
    static {
        System.loadLibrary("native");
    }

    public static final MutableLiveData<Boolean> enabled = new MutableLiveData<>(false);
    public static final MutableLiveData<Boolean> connected = new MutableLiveData<>(false);

    public static final String INTENT_KEY_DELETE_URI = "INTENT_KEY_DELETE_URI";
    public static final String URI = "URI";
    public static final String INTENT_KEY_AUTO_BACKUP = "INTENT_KEY_AUTO_BACKUP";

    private SystemEventReceiver systemEventReceiver;
    private final TaskInfoSummary taskInfoSummary = TaskInfoSummary.getInstance();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        AppLog.d("事件", "onAccessibilityEvent: " + event);
        // 窗口变化事件不带包名与类名，这里按需取值
        String packageName = event.getPackageName() == null ? null : event.getPackageName().toString();
        String className = event.getClassName() == null ? null : event.getClassName().toString();

        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName == null || className == null) return;
            if (packageName.isEmpty() || className.isEmpty()) return;
            // 采集窗口事件，供设置页的日志查看，不去重
            AppLog.add(AppLog.CATEGORY_ACTIVITY, packageName, className);
            // 桌面应用需要检查实际界面与事件是否匹配
            if (taskInfoSummary.isLauncherApp(packageName)) {
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root != null && root.getPackageName() != null) {
                    if (!Objects.equals(packageName, root.getPackageName().toString())) {
                        // 界面不匹配直接退出
                        return;
                    }
                }
            }
            taskInfoSummary.enterActivity(packageName, className);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            // 部分桌面切回桌面不发窗口状态变化事件，按激活窗口兜底
            String windowPackage = null;
            for (AccessibilityWindowInfo window : getWindows()) {
                if (window.getType() != AccessibilityWindowInfo.TYPE_APPLICATION) continue;
                if (!window.isActive()) continue;
                AccessibilityNodeInfo root = window.getRoot();
                if (root == null || root.getPackageName() == null) continue;
                windowPackage = root.getPackageName().toString();
                break;
            }
            if (windowPackage != null && taskInfoSummary.isLauncherApp(windowPackage)) {
                taskInfoSummary.enterLauncher(windowPackage);
            }
        } else if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            if (packageName == null || className == null) return;
            Map<String, String> content = new HashMap<>();
            List<CharSequence> textList = event.getText();

            if (className.contains(Notification.class.getSimpleName())) {
                if (SettingSaver.PERMISSION_NOTIFICATION.get() != 0) return;

                Notification notification = (Notification) event.getParcelableData();
                if (notification != null && notification.extras != null) {
                    Bundle extras = notification.extras;
                    for (String key : extras.keySet()) {
                        String value = extras.getString(key);
                        content.put(key, value);
                    }
                } else if (!textList.isEmpty()) {
                    String s = textList.get(0).toString();
                    Pattern pattern = AppUtil.getPattern("^(.+?): (.+)$");
                    if (pattern != null) {
                        Matcher matcher = pattern.matcher(s);
                        if (matcher.find()) {
                            content.put(Notification.EXTRA_TITLE, matcher.group(1));
                            content.put(Notification.EXTRA_TEXT, matcher.group(2));
                        }
                    }
                }

                taskInfoSummary.setNotification(TaskInfoSummary.NotificationType.NOTIFICATION, packageName, content);
            } else if (className.contains(Toast.class.getSimpleName())) {
                if (textList.isEmpty()) return;
                String text = textList.get(0).toString();
                content.put(Notification.EXTRA_TEXT, text);
                taskInfoSummary.setNotification(TaskInfoSummary.NotificationType.TOAST, packageName, content);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        connected.setValue(true);
        setEnabled(SettingSaver.APP_SERVICE.get());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        connected.setValue(false);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MainApplication.getInstance().setService(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connected.setValue(false);
        setEnabled(false);
        MainApplication.getInstance().setService(null);
        FloatWindow.reset();
    }

    public boolean isConnected() {
        return Boolean.TRUE.equals(connected.getValue());
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled.getValue()) && isConnected();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void setEnabled(boolean bool) {
        enabled.setValue(bool);

        if (isEnabled()) {
            systemEventReceiver = new SystemEventReceiver(this);
            systemEventReceiver.register();

            resetAllAlarm();
            resetAllBroadcast();
            initTTS();
            SuperUser.getInstance().init(result -> {
            });
            tryStartMainActivity();
        } else {
            if (systemEventReceiver != null) systemEventReceiver.unregister();
            systemEventReceiver = null;

            if (tts != null) tts.shutdown();
            tts = null;

            stopAllTask();
            stopCapture();
            stopSound(null);
            cancelAllAlarm();
            cancelAllBroadcast();
            destroyTTS();
            SuperUser.getInstance().exit();
            FloatWindow.dismiss(KeepAliveFloatView.class.getName());
        }
    }

    public void tryStartMainActivity() {
        ThreadUtil.execute(() -> {
            while (!BootCompletedReceiver.isBootCompleted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    return;
                }
            }
            MainActivity activity = MainApplication.getInstance().getActivity();
            if (activity == null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent intent = new Intent(this, FloatViewActivity.class);
                    intent.setAction(FloatViewActivity.INTENT_KEY_AUTO_START);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
            }
        });
    }

    // 任务 ----------------------------------------------------------------------------- start
    private final Set<TaskRunnable> tasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<ITaskListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addListener(ITaskListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ITaskListener listener) {
        listeners.remove(listener);
    }

    public void runTask(Task task, StartAction startAction) {
        runTask(task, startAction, null);
    }

    public TaskRunnable runTask(Task task, StartAction startAction, ITaskListener listener) {
        cleanInterruptTask();

        if (task == null || startAction == null) return null;
        if (!isEnabled()) return null;

        if (startAction.getRestartType() == StartAction.RestartType.CANCEL) {
            if (isTaskRunning(task, startAction)) return null;
        }

        TaskRunnable runnable = new TaskRunnable(task, startAction);
        if (listener != null) runnable.addListener(listener);

        runnable.addListener(new ITaskListener() {

            @Override
            public void onStart(TaskRunnable runnable) {
                StartAction action = runnable.getStartAction();
                if (!action.isShow()) return;

                ActionType actionType = action.getType();
                if (actionType == ActionType.MANUAL_START) return;
                if (actionType == ActionType.INNER_START) return;
                PlayFloatView.addRunningAction(runnable);
            }

            @Override
            public void onFinish(TaskRunnable runnable) {
                tasks.remove(runnable);
            }
        });

        // 直接加入到任务列表。如果在开始里加入，需要等待线程启动，当同时有多条开始进来时，重入策略会失效
        if (startAction.getRestartType() == StartAction.RestartType.RESTART) {
            TaskRunnable taskRunnable = getTaskRunnable(task.getId(), startAction.getId());
            if (taskRunnable != null) {
                taskRunnable.stop();
            }
        }
        tasks.add(runnable);

        listeners.stream().filter(Objects::nonNull).forEach(runnable::addListener);
        Future<?> future;
        try {
            future = ThreadUtil.submitTask(runnable);
        } catch (RejectedExecutionException e) {
            // 线程池容量已满，任务没有真正提交，也就永远不会触发 onFinish，
            // 必须立刻从运行列表里移除，否则会被当成运行中的任务
            tasks.remove(runnable);
            AppLog.e("线程", "runTask: 线程池已满，任务未能提交", e);
            return null;
        }
        runnable.setFuture(future);
        return runnable;
    }

    public boolean isTaskRunning(Task task) {
        if (task == null) return false;
        TaskRunnable runnable = getTaskRunnable(task.getId(), null);
        return runnable != null && !runnable.isInterrupt();
    }

    public boolean isTaskRunning(Task task, Action action) {
        if (task == null || action == null) return false;
        TaskRunnable runnable = getTaskRunnable(task.getId(), action.getId());
        return runnable != null && !runnable.isInterrupt();
    }

    public TaskRunnable getTaskRunnable(String taskId, String actionId) {
        for (TaskRunnable runnable : tasks) {
            if (runnable.isInterrupt()) continue;

            Task startTask = runnable.getStartTask();
            if (taskId.equals(startTask.getId())) {
                if (actionId != null) {
                    Action action = runnable.getStartAction();
                    if (actionId.equals(action.getId())) {
                        return runnable;
                    }
                } else {
                    return runnable;
                }
            }
        }
        return null;
    }

    public List<TaskRunnable> getRunningTask() {
        // 任务中断后要等线程跑完 onFinish 才会从列表移除，
        // 这段窗口里已停止的任务不能算运行中，过滤掉才与 isTaskRunning 的判断一致
        List<TaskRunnable> runningTasks = new ArrayList<>(tasks);
        runningTasks.removeIf(TaskRunnable::isInterrupt);
        return runningTasks;
    }

    private void cleanInterruptTask() {
        tasks.removeIf(TaskRunnable::isInterrupt);
    }

    public void stopTask(Task task) {
        if (task == null) return;
        for (TaskRunnable runnable : tasks) {
            if (runnable.isInterrupt()) continue;

            Task startTask = runnable.getStartTask();
            if (task.equals(startTask)) {
                runnable.stop();
            }
        }
    }

    public void stopAllTask() {
        for (TaskRunnable runnable : tasks) {
            if (runnable.isInterrupt()) continue;
            runnable.stop();
        }
    }

    public void stopAllTask(Task excludeTask, StartAction excludeAction) {
        for (TaskRunnable runnable : tasks) {
            if (runnable.isInterrupt()) continue;
            if (runnable.getStartTask().equals(excludeTask) && runnable.getStartAction().equals(excludeAction)) continue;
            runnable.stop();
        }
    }
    // 任务 ----------------------------------------------------------------------------- end

    // 定时 ----------------------------------------------------------------------------- start
    private PendingIntent getAlarmPendingIntent(String taskId, String actionId) {
        Intent intent = new Intent(InstantActivity.INTENT_KEY_DO_ACTION);
        intent.putExtra(InstantActivity.TASK_ID, taskId);
        intent.putExtra(InstantActivity.ACTION_ID, actionId);

        return PendingIntent.getBroadcast(this, Objects.hashCode(taskId + actionId), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getAlarmPendingIntent(Uri uri) {
        Intent intent = new Intent(INTENT_KEY_DELETE_URI);
        intent.putExtra(URI, uri);
        return PendingIntent.getBroadcast(this, Objects.hashCode(uri.toString()), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getAlarmPendingIntent() {
        Intent intent = new Intent(INTENT_KEY_AUTO_BACKUP);
        return PendingIntent.getBroadcast(this, Objects.hashCode(INTENT_KEY_AUTO_BACKUP), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void resetAllAlarm() {
        List<Task> tasks = TaskSaver.getInstance().getTasks(TimeStartAction.class);
        for (Task task : tasks) {
            for (Action action : task.getActions(TimeStartAction.class)) {
                TimeStartAction timeStartAction = (TimeStartAction) action;
                cancelAlarm(task, timeStartAction);
                if (timeStartAction.isEnable()) {
                    addAlarm(task, timeStartAction);
                }
            }
        }
        addAlarm();
    }

    public void cancelAllAlarm() {
        List<Task> tasks = TaskSaver.getInstance().getTasks(TimeStartAction.class);
        for (Task task : tasks) {
            for (Action action : task.getActions(TimeStartAction.class)) {
                TimeStartAction timeStartAction = (TimeStartAction) action;
                cancelAlarm(task, timeStartAction);
            }
        }
        cancelAlarm();
    }

    public void cancelAlarm(Task task, TimeStartAction timeStartAction) {
        if (task == null || timeStartAction == null) return;
        PendingIntent intent = getAlarmPendingIntent(task.getId(), timeStartAction.getId());
        if (intent == null) return;
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(intent);
    }

    public void cancelAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = getAlarmPendingIntent();
        if (pendingIntent != null) manager.cancel(pendingIntent);
    }

    public void addAlarm(Uri uri, long periodic) {
        if (uri == null) return;
        if (periodic <= 0) return;

        PendingIntent pendingIntent = getAlarmPendingIntent(uri);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long nextStartTime = System.currentTimeMillis() + periodic;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (manager.canScheduleExactAlarms()) {
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(nextStartTime, null);
                manager.setAlarmClock(clockInfo, pendingIntent);
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextStartTime, pendingIntent);
            }
        } else {
            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(nextStartTime, null);
            manager.setAlarmClock(clockInfo, pendingIntent);
        }
    }

    public void addAlarm() {
        if (SettingSaver.TASK_AUTO_BACKUP.get() == 0) {
            cancelAlarm();
            return;
        }

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = getAlarmPendingIntent();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        long nextStartTime = calendar.getTimeInMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (manager.canScheduleExactAlarms()) {
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(nextStartTime, null);
                manager.setAlarmClock(clockInfo, pendingIntent);
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextStartTime, pendingIntent);
            }
        } else {
            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(nextStartTime, null);
            manager.setAlarmClock(clockInfo, pendingIntent);
        }
    }

    public void addAlarm(Task task, TimeStartAction timeStartAction) {
        if (task == null || timeStartAction == null) return;
        if (!timeStartAction.isEnable()) return;
        if (!isEnabled()) return;
        if (!SettingSaver.PERMISSION_EXACT_ALARM.get()) return;

        PendingIntent pendingIntent = getAlarmPendingIntent(task.getId(), timeStartAction.getId());
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long timeMillis = System.currentTimeMillis();
        long startTime = timeStartAction.getStartTime();
        long periodic = timeStartAction.getPeriodic();

        long nextStartTime = startTime;
        if (periodic > 0) {
            long l = timeMillis - startTime;
            // 当前时间没达到定时时间，下次执行时间就是开始时间
            if (l > 0) {
                // 当前时间大于开始时间，需要计算下次开始的时间，防止定时任务刚设定就执行了
                int loop = (int) Math.ceil(l * 1f / periodic);
                nextStartTime = startTime + loop * periodic;
                // 如果算出的下个开始时间没有大于现在时间30s，应该是算错了，下个开始时间再加个间隔
                if (nextStartTime - timeMillis < 30 * 1000) {
                    nextStartTime += periodic;
                }
            }
        }
        if (nextStartTime < timeMillis) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (manager.canScheduleExactAlarms()) {
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(nextStartTime, null);
                manager.setAlarmClock(clockInfo, pendingIntent);
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextStartTime, pendingIntent);
            }
        } else {
            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(nextStartTime, null);
            manager.setAlarmClock(clockInfo, pendingIntent);
        }
        if (timeStartAction.isShowTimeLog()) {
            String timeTips = getString(R.string.time_start_action_set_tips, AppUtil.formatDateTime(this, nextStartTime, false, true));
            LogSaver.getInstance().addLog(task.getId(), new LogInfo(new NormalLog(timeTips)), true);
            AppLog.add(AppLog.CATEGORY_ALARM, task.getTitle(), timeTips);
        }
    }

    public void replaceAlarm(Task task) {
        if (task == null) return;
        Task originTask = TaskSaver.getInstance().getOriginTask(task.getId());
        List<Action> actions = task.getActions(TimeStartAction.class);

        if (originTask != null) {
            originTask.getActions(TimeStartAction.class).forEach(startAction -> {
                TimeStartAction timeStartAction = (TimeStartAction) startAction;
                if (!timeStartAction.isEnable()) return;

                boolean isExist = false;
                for (Action action : actions) {
                    if (action.getId().equals(timeStartAction.getId())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    cancelAlarm(task, timeStartAction);
                }
            });
        }

        actions.forEach(startAction -> {
            TimeStartAction timeStartAction = (TimeStartAction) startAction;
            if (timeStartAction.isEnable()) {
                addAlarm(task, timeStartAction);
            } else {
                cancelAlarm(task, timeStartAction);
            }
        });
    }

    // 定时 ----------------------------------------------------------------------------- end

    // 广播 ----------------------------------------------------------------------------- start

    private final Map<String, BroadcastReceiver> broadcastReceivers = new HashMap<>();

    private void resetAllBroadcast() {
        cancelAllBroadcast();
        for (Task task : TaskSaver.getInstance().getTasks(BroadcastStartAction.class)) {
            replaceBroadcast(task);
        }
    }

    private void cancelAllBroadcast() {
        for (BroadcastReceiver receiver : broadcastReceivers.values()) {
            unregisterReceiver(receiver);
        }
        broadcastReceivers.clear();
    }

    public void cancelBroadcast(Task task, BroadcastStartAction broadcastStartAction) {
        if (task == null || broadcastStartAction == null) return;
        String key = task.getId() + broadcastStartAction.getId();
        BroadcastReceiver receiver = broadcastReceivers.get(key);
        if (receiver == null) return;
        unregisterReceiver(receiver);
        broadcastReceivers.remove(key);
    }

    public void addBroadcast(Task task, BroadcastStartAction broadcastStartAction) {
        if (task == null || broadcastStartAction == null) return;
        if (!broadcastStartAction.isEnable()) return;
        if (broadcastStartAction.getAction() == null || broadcastStartAction.getAction().isEmpty()) return;
        if (!isEnabled()) return;

        String key = task.getId() + broadcastStartAction.getId();
        BroadcastReceiver receiver = broadcastReceivers.get(key);
        if (receiver != null) return;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;
                Uri uri = intent.getData();
                String data = "";
                if (uri != null) data = uri.toString();
                Map<String, String> extras = new HashMap<>();
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String name : bundle.keySet()) {
                        extras.put(name, bundle.getString(name));
                    }
                }
                TaskInfoSummary.getInstance().setBroadcastInfo(action, data, extras);
            }
        };
        broadcastReceivers.put(key, receiver);
        IntentFilter filter = new IntentFilter(broadcastStartAction.getAction());
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    public void replaceBroadcast(Task task) {
        if (task == null) return;
        Task originTask = TaskSaver.getInstance().getOriginTask(task.getId());
        List<Action> actions = task.getActions(BroadcastStartAction.class);

        if (originTask != null) {
            originTask.getActions(BroadcastStartAction.class).forEach(startAction -> {
                BroadcastStartAction broadcastStartAction = (BroadcastStartAction) startAction;
                if (!broadcastStartAction.isEnable()) return;

                boolean isExist = false;
                for (Action action : actions) {
                    if (action.getId().equals(broadcastStartAction.getId())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    cancelBroadcast(task, broadcastStartAction);
                }
            });
        }

        actions.forEach(startAction -> {
            BroadcastStartAction broadcastStartAction = (BroadcastStartAction) startAction;
            if (broadcastStartAction.isEnable()) {
                addBroadcast(task, broadcastStartAction);
            } else {
                cancelBroadcast(task, broadcastStartAction);
            }
        });
    }

    // 广播 ----------------------------------------------------------------------------- end

    // 录屏 ----------------------------------------------------------------------------- start
    private BooleanResultCallback captureCallback;
    private ServiceConnection captureConnection = null;
    private CaptureService.CaptureBinder captureBinder = null;

    public boolean isCaptureEnabled() {
        if (isEnabled()) {
            return captureBinder != null;
        }
        return false;
    }

    public boolean startCapture(BooleanResultCallback callback) {
        if (captureBinder == null) {
            captureCallback = callback;
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.putExtra(PermissionActivity.CAPTURE_PERMISSION, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } else {
            if (callback != null) callback.onResult(true);
            return false;
        }
    }

    public void stopCapture() {
        if (captureConnection != null) {
            try {
                unbindService(captureConnection);
                captureConnection = null;
                stopService(new Intent(this, CaptureService.class));
            } catch (Exception ignored) {
            }
        }
        captureBinder = null;
    }

    public void bindCapture(boolean result, Intent data) {
        if (result) {
            captureConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    captureBinder = (CaptureService.CaptureBinder) service;
                    callCaptureCallback(true);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    stopCapture();
                }
            };

            Intent intent = new Intent(this, CaptureService.class);
            intent.putExtra(CaptureService.DATA, data);
            if (!bindService(intent, captureConnection, Context.BIND_AUTO_CREATE)) callCaptureCallback(false);
        } else {
            callCaptureCallback(false);
        }
    }

    private void callCaptureCallback(boolean result) {
        if (captureCallback != null) {
            captureCallback.onResult(result);
            captureCallback = null;
        }
    }

    // 录屏 ----------------------------------------------------------------------------- end

    // 截图 ----------------------------------------------------------------------------- start

    private SoftReference<Bitmap> screenShot = new SoftReference<>(null);

    public synchronized Bitmap getScreenShotByCapture() {
        Bitmap cachedBitmap = screenShot.get();
        if (captureBinder == null) return cachedBitmap;

        Bitmap bitmap = captureBinder.getScreenShot();
        if (bitmap == null) {
            return cachedBitmap;
        } else {
            if (cachedBitmap != null && !cachedBitmap.isRecycled()) cachedBitmap.recycle();
            screenShot = new SoftReference<>(bitmap);
            return bitmap;
        }
    }

    // 不能递归调用，会锁死
    public Bitmap getScreenShotByAccessibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            CompletableFuture<Bitmap> future = new CompletableFuture<>();
            TakeScreenshotCallback callback = new TakeScreenshotCallback() {
                @Override
                public void onSuccess(@NonNull ScreenshotResult result) {
                    try (HardwareBuffer hardwareBuffer = result.getHardwareBuffer()) {
                        Bitmap bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, result.getColorSpace());
                        if (bitmap != null) {
                            Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                            Bitmap cachedBitmap = screenShot.get();
                            if (cachedBitmap != null && !cachedBitmap.isRecycled()) cachedBitmap.recycle();

                            screenShot = new SoftReference<>(copy);
                            bitmap.recycle();
                            future.complete(copy);
                        }
                    } catch (Exception e) {
                        future.complete(screenShot.get());
                    }
                }

                @Override
                public void onFailure(int errorCode) {
                    future.complete(screenShot.get());
                }
            };

            try {
                takeScreenshot(0, ThreadUtil.getExecutorService(), callback);
            } catch (RejectedExecutionException e) {
                // 线程池已满，截图请求没有提交，直接退回缓存的截图
                AppLog.e("截图", "getScreenShotByAccessibility: 线程池已满，截图未能提交", e);
                return screenShot.get();
            }
            return future.join();
        } else {
            return getScreenShotByCapture();
        }
    }

    public Bitmap tryGetScreenShot() {
        if (isCaptureEnabled()) {
            return getScreenShotByCapture();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return getScreenShotByAccessibility();
        }

        return null;
    }

    // 截图 ----------------------------------------------------------------------------- end

    // 按键 ----------------------------------------------------------------------------- start
    private Handler handler;

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (!SettingSaver.TASK_VOLUME_KEY_STOP.get()) return super.onKeyEvent(event);
        if (handler == null) handler = new Handler(Looper.getMainLooper());

        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                handler.postDelayed(() -> {
                    Toast.makeText(this, isEnabled() ? R.string.app_setting_disable : R.string.app_setting_enabled, Toast.LENGTH_SHORT).show();
                    setEnabled(!isEnabled());
                }, 2000);
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                handler.removeCallbacksAndMessages(null);
                if (isEnabled()) stopAllTask();
            }
        }
        return super.onKeyEvent(event);
    }
    // 按键 ----------------------------------------------------------------------------- end

    // 手势 ----------------------------------------------------------------------------- start
    public void runGesture(int x, int y, int time, BooleanResultCallback callback) {
        if (x >= 0 && y >= 0 && time > 0) {
            Path path = new Path();
            path.moveTo(x, y);
            runGesture(Collections.singleton(new GestureDescription.StrokeDescription(path, 0, time)), callback);
            return;
        }
        if (callback != null) callback.onResult(false);
    }

    public void runGesture(Set<GestureDescription.StrokeDescription> strokeDescriptions, BooleanResultCallback callback) {
        if (strokeDescriptions == null || strokeDescriptions.isEmpty()) {
            if (callback != null) callback.onResult(false);
            return;
        }

        GestureDescription.Builder builder = new GestureDescription.Builder();
        for (GestureDescription.StrokeDescription strokeDescription : strokeDescriptions) {
            builder.addStroke(strokeDescription);
        }
        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if (callback != null) callback.onResult(true);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                if (callback != null) callback.onResult(false);
            }
        }, null);
    }

    public void runGesture(List<Set<GestureDescription.StrokeDescription>> strokeDescriptions, BooleanResultCallback callback) {
        if (strokeDescriptions == null || strokeDescriptions.isEmpty()) {
            if (callback != null) callback.onResult(false);
            return;
        }

        GestureHandler gestureHandler = new GestureHandler(strokeDescriptions, callback);
        gestureHandler.dispatchGesture();
    }

    private class GestureHandler {
        private final List<Set<GestureDescription.StrokeDescription>> strokeList;
        private final BooleanResultCallback callback;

        public GestureHandler(List<Set<GestureDescription.StrokeDescription>> strokeList, BooleanResultCallback callback) {
            this.strokeList = strokeList;
            this.callback = callback;
        }

        private void dispatchGesture() {
            if (strokeList.isEmpty()) {
                if (callback != null) callback.onResult(true);
                return;
            }
            runGesture(strokeList.remove(0), result -> {
                if (result) {
                    dispatchGesture();
                } else {
                    if (callback != null) callback.onResult(false);
                }
            });
        }
    }
    // 手势 ----------------------------------------------------------------------------- end

    // 播放声音 ----------------------------------------------------------------------------- start
    private final Map<String, MediaPlayer> playerMap = new HashMap<>();

    public void playSound(String path, int index) {
        stopSound(path);
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, Uri.parse(path));
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(index == 0 ? AudioAttributes.USAGE_MEDIA : AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(index == 0 ? AudioAttributes.CONTENT_TYPE_MUSIC : AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            mediaPlayer.setOnPreparedListener(mp -> {
                playerMap.put(path, mp);
                mp.start();
                int duration = mp.getDuration();
                new Handler(Looper.getMainLooper()).postDelayed(() -> stopSound(path), duration);
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    public void stopSound(String path) {
        if (path == null || path.isEmpty()) {
            playerMap.forEach((s, mediaPlayer) -> {
                mediaPlayer.stop();
                mediaPlayer.release();
            });
            playerMap.clear();
            return;
        }

        MediaPlayer mediaPlayer = playerMap.remove(path);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private TextToSpeech tts;
    private final Map<String, BooleanResultCallback> speakCallbacks = new HashMap<>();

    private void initTTS() {
        if (tts != null) return;
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.CHINA);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onDone(String utteranceId) {
                        BooleanResultCallback callback = speakCallbacks.remove(utteranceId);
                        if (callback != null) callback.onResult(true);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        BooleanResultCallback callback = speakCallbacks.remove(utteranceId);
                        if (callback != null) callback.onResult(false);
                    }

                    @Override
                    public void onStart(String utteranceId) {

                    }
                });
            }
        });
    }

    private void destroyTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    public void speak(String text, BooleanResultCallback callback) {
        if (tts == null) return;
        String id = UUID.randomUUID().toString();
        speakCallbacks.put(id, callback);

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id);
    }

    // 播放声音 ----------------------------------------------------------------------------- end

    // 震动 ----------------------------------------------------------------------------- start
    public void vibrate(long duration) {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = manager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }
    }
    // 震动 ----------------------------------------------------------------------------- end
}
