package top.bogey.touch_tool.bean.pin.pin_objects.pin_number;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.text.DecimalFormat;

import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class PinDouble extends PinNumber<Double> {

    public PinDouble() {
        super(PinSubType.DOUBLE, 0.0);
    }

    public PinDouble(double value) {
        super(PinSubType.DOUBLE, value);
    }

    public PinDouble(JsonObject jsonObject) {
        super(jsonObject);
        value = GsonUtil.getAsDouble(jsonObject, "value", 0);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        DecimalFormat format = new DecimalFormat("#.####");
        return format.format(value);
    }

    @Override
    public void reset() {
        super.reset();
        value = 0.0;
    }

    @Override
    public boolean cast(String value) {
        try {
            this.value = Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            AppLog.e(e);
            return false;
        }
    }
}
