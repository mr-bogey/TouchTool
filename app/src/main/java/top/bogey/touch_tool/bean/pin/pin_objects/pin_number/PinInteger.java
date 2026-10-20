package top.bogey.touch_tool.bean.pin.pin_objects.pin_number;

import com.google.gson.JsonObject;

import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class PinInteger extends PinNumber<Integer> {

    public PinInteger() {
        super(PinSubType.INTEGER, 0);
    }

    public PinInteger(int value) {
        super(PinSubType.INTEGER, value);
    }

    public PinInteger(JsonObject jsonObject) {
        super(jsonObject);
        value = GsonUtil.getAsInt(jsonObject, "value", 0);
    }

    @Override
    public void reset() {
        super.reset();
        value = 0;
    }

    @Override
    public boolean cast(String value) {
        try {
            this.value = Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            AppLog.e(e);
            return false;
        }
    }
}
