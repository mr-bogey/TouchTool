package top.bogey.touch_tool.bean.action.map;

import com.google.gson.JsonObject;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinAny;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.service.TaskRunnable;

// 装箱：把任意类型的值包装为任意值(值包)
public class BoxValueAction extends ExecuteAction {
    private final transient Pin valuePin = new Pin(new PinObject(PinSubType.DYNAMIC), R.string.pin_object);
    private final transient Pin resultPin = new Pin(new PinAny(), R.string.pin_any, true);

    public BoxValueAction() {
        super(ActionType.BOX_VALUE);
        addPins(valuePin, resultPin);
    }

    public BoxValueAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(valuePin, resultPin);
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        // 取值后装箱，原值类型保留在值包内
        PinObject value = getPinValue(runnable, valuePin);
        resultPin.setValue(new PinAny(value));
        executeNext(runnable, outPin);
    }
}
