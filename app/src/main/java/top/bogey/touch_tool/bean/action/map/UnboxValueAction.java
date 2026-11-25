package top.bogey.touch_tool.bean.action.map;

import com.google.gson.JsonObject;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.action.parent.SyncAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinAny;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBoolean;
import top.bogey.touch_tool.bean.pin.pin_objects.PinMap;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_execute.PinExecute;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_list.PinList;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinDouble;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able.PinImage;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinSingleSelect;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.service.TaskRunnable;

// 拆箱：把任意值按目标类型拆出；类型不符写错误信息并走失败分支，不抛异常
public class UnboxValueAction extends ExecuteAction implements SyncAction {
    private final transient Pin anyPin = new Pin(new PinAny(), R.string.pin_any);
    private final transient Pin typePin = new Pin(new PinSingleSelect(R.array.unbox_target_type), R.string.pin_target_type);
    private final transient Pin resultPin = new Pin(new PinString(), R.string.pin_object, true);
    private final transient Pin errorPin = new Pin(new PinString(), R.string.action_error, true);
    private final transient Pin failPin = new Pin(new PinExecute(), R.string.action_fail, true);

    public UnboxValueAction() {
        super(ActionType.UNBOX_VALUE);
        addPins(anyPin, typePin, resultPin, errorPin, failPin);
    }

    public UnboxValueAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(anyPin, typePin, resultPin, errorPin, failPin);
    }

    // 按目标类型构造空值，供编辑期切换结果针脚类型
    private PinObject newTargetValue() {
        int index = typePin.getValue(PinSingleSelect.class).getIndex();
        return switch (index) {
            case 1 -> new PinDouble();
            case 2 -> new PinBoolean();
            case 3 -> new PinList();
            case 4 -> new PinMap();
            case 5 -> new PinImage();
            case 6 -> new PinObject();
            default -> new PinString();
        };
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        PinObject v = getPinValue(runnable, anyPin);
        // 旧类型直连时按自动装箱语义包装，避免泛型取值处的类型转换异常
        PinAny any = v instanceof PinAny a ? a : new PinAny(v);
        PinBase payload = any.getPayload();
        PinObject target = newTargetValue();
        Class<? extends PinObject> clazz = target.getClass();
        if (clazz.isInstance(payload)) {
            // 类型相合直接拆出
            resultPin.setValue(payload);
            executeNext(runnable, outPin);
        } else if (target instanceof PinString) {
            // 目标为字符串时允许安全转换
            resultPin.setValue(new PinString(payload.toString()));
            executeNext(runnable, outPin);
        } else {
            errorPin.getValue(PinString.class).setValue("类型不符：实际 " + payload.getType() + "，目标 " + target.getType());
            executeNext(runnable, failPin);
        }
    }

    @Override
    public void sync(Task context) {
        // 编辑期按目标类型切换结果针脚，保证连出线的类型正确
        resultPin.setValue(context, newTargetValue());
    }

    @Override
    public void onValueUpdated(Task task, Pin origin, PinBase value) {
        super.onValueUpdated(task, origin, value);
        sync(task);
    }
}
