package top.bogey.touch_tool.bean.action.map;

import com.google.gson.JsonObject;

import java.util.Map;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinAny;
import top.bogey.touch_tool.bean.pin.pin_objects.PinMap;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_execute.PinExecute;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_list.PinList;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.service.TaskRunnable;

// 取字段/取下标：读取字典字段或列表下标，结果装箱为任意值；失败写错误信息并走失败分支
public class GetValueItemAction extends ExecuteAction {
    private final transient Pin targetPin = new Pin(new PinObject(PinSubType.DYNAMIC), R.string.pin_object);
    private final transient Pin keyPin = new Pin(new PinString(), R.string.pin_item_key);
    private final transient Pin resultPin = new Pin(new PinAny(), R.string.pin_any, true);
    private final transient Pin errorPin = new Pin(new PinString(), R.string.action_error, true);
    private final transient Pin failPin = new Pin(new PinExecute(), R.string.action_fail, true);

    public GetValueItemAction() {
        super(ActionType.GET_VALUE_ITEM);
        addPins(targetPin, keyPin, resultPin, errorPin, failPin);
    }

    public GetValueItemAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(targetPin, keyPin, resultPin, errorPin, failPin);
    }

    // 失败统一出口：写错误信息并走失败分支
    private void fail(TaskRunnable runnable, String message) {
        errorPin.getValue(PinString.class).setValue(message);
        executeNext(runnable, failPin);
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        PinObject target = getPinValue(runnable, targetPin);
        // 任意值输入先解包再取字段，否则装箱后的字典/列表会被误判为非容器
        if (target instanceof PinAny any) {
            target = any.getPayload() instanceof PinObject po ? po : null;
        }
        if (target == null) {
            fail(runnable, "任意值内容不是有效对象");
            return;
        }
        String key = getPinValue(runnable, keyPin).toString();
        if (target instanceof PinMap map) {
            // 字典键为 PinObject，按字符串形式匹配
            for (Map.Entry<PinObject, PinObject> entry : map.entrySet()) {
                if (entry.getKey().toString().equals(key)) {
                    resultPin.setValue(new PinAny(entry.getValue()));
                    executeNext(runnable, outPin);
                    return;
                }
            }
            fail(runnable, "键不存在：" + key);
        } else if (target instanceof PinList list) {
            try {
                int index = Integer.parseInt(key.trim());
                if (index < 0 || index >= list.size()) {
                    fail(runnable, "下标越界：" + index);
                    return;
                }
                resultPin.setValue(new PinAny(list.get(index)));
                executeNext(runnable, outPin);
            } catch (NumberFormatException e) {
                fail(runnable, "下标非数字：" + key);
            }
        } else {
            fail(runnable, "目标不是字典或列表");
        }
    }
}
