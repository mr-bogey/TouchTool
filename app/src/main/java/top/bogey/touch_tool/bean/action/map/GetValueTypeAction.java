package top.bogey.touch_tool.bean.action.map;

import com.google.gson.JsonObject;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinAny;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.service.TaskRunnable;

// 值类型判断：输出任意值当前的类型名，可接 SwitchAction 做分派
public class GetValueTypeAction extends ExecuteAction {
    private final transient Pin anyPin = new Pin(new PinAny(), R.string.pin_any);
    private final transient Pin resultPin = new Pin(new PinString(), R.string.pin_target_type, true);

    public GetValueTypeAction() {
        super(ActionType.GET_VALUE_TYPE);
        addPins(anyPin, resultPin);
    }

    public GetValueTypeAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(anyPin, resultPin);
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        // 类型标签由值包 payload 派生；旧类型直连时按自动装箱语义包装
        PinObject v = getPinValue(runnable, anyPin);
        PinAny any = v instanceof PinAny a ? a : new PinAny(v);
        String type = any.getTypeTag().name();
        resultPin.getValue(PinString.class).setValue(type);
        executeNext(runnable, outPin);
    }
}
