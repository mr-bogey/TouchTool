package top.bogey.touch_tool.bean.pin.pin_objects.pin_number;

import com.google.gson.JsonObject;

import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class PinFloat extends PinNumber<Float> {

    public PinFloat() {
        super(PinSubType.FLOAT, 0f);
    }

    public PinFloat(float value) {
        super(PinSubType.FLOAT, value);
    }

    public PinFloat(JsonObject jsonObject) {
        super(jsonObject);
        value = GsonUtil.getAsFloat(jsonObject, "value", 0);
    }

    @Override
    public void reset() {
        super.reset();
        value = 0f;
    }

    @Override
    public boolean cast(String value) {
        try {
            this.value = Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            AppLog.e(e);
            return false;
        }
    }
}
