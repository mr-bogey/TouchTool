package top.bogey.touch_tool.bean.action.string;

import com.google.gson.JsonObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.CalculateAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinSingleSelect;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.bean.pin.special_pin.NotLinkAblePin;
import top.bogey.touch_tool.service.TaskRunnable;
import top.bogey.touch_tool.utils.AppLog;

public class StringEncryptAction extends CalculateAction {
    private final transient Pin typePin = new NotLinkAblePin(new PinSingleSelect(R.array.string_encrypt_type), R.string.string_encrypt_action_type);
    private final transient Pin textPin = new Pin(new PinString(), R.string.pin_string);
    private final transient Pin resultPin = new Pin(new PinString(), R.string.pin_boolean_result, true);

    public StringEncryptAction() {
        super(ActionType.STRING_ENCRYPT);
        addPins(typePin, textPin, resultPin);
    }

    public StringEncryptAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(typePin, textPin, resultPin);
    }

    @Override
    public void calculate(TaskRunnable runnable, Pin pin) {
        PinSingleSelect type = typePin.getValue();
        PinString text = getPinValue(runnable, textPin);
        String value = text.getValue();
        if (value == null || value.isEmpty()) return;

        try {
            MessageDigest digest = MessageDigest.getInstance(type.getValue());
            String result = bytesToHex(digest.digest(value.getBytes()));
            resultPin.getValue(PinString.class).setValue(result);
        } catch (NoSuchAlgorithmException e) {
            AppLog.e(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
