package top.bogey.touch_tool.bean.action.string;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinMap;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_execute.PinExecute;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinFileContentString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.service.TaskRunnable;

// JSON解析(安全)：与旧JSON解析同链路，但解析失败写错误信息并走失败分支，不静默吞异常
public class ParseJsonSafeAction extends ExecuteAction {
    private final transient Pin jsonPin = new Pin(new PinFileContentString(), R.string.pin_string);
    // 结果声明为字典（与旧版一致，兼容直连旧字典节点）；数组根时实际值为列表，请接取字段/拆箱等动态节点，勿直连旧字典节点
    private final transient Pin resultPin = new Pin(new PinMap(), R.string.pin_boolean_result, true);
    private final transient Pin errorPin = new Pin(new PinString(), R.string.action_error, true);
    private final transient Pin failPin = new Pin(new PinExecute(), R.string.action_fail, true);

    public ParseJsonSafeAction() {
        super(ActionType.PARSE_JSON_SAFE);
        addPins(jsonPin, resultPin, errorPin, failPin);
    }

    public ParseJsonSafeAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(jsonPin, resultPin, errorPin, failPin);
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        try {
            // 容忍前导空白/换行，避免合法 JSON 被误判根类型
            String json = getPinValue(runnable, jsonPin).toString().trim();
            Gson gson = new Gson();
            if (json.startsWith("{")) {
                // 对象根：递归装箱为字典，嵌套与混合类型不丢失
                Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
                }.getType());
                resultPin.setValue(PinBase.parseValue(map));
            } else if (json.startsWith("[")) {
                // 数组根：递归装箱为列表
                List<Object> list = gson.fromJson(json, new TypeToken<List<Object>>() {
                }.getType());
                resultPin.setValue(PinBase.parseValue(list));
            } else {
                throw new IllegalArgumentException("JSON根节点不是对象或数组");
            }
            executeNext(runnable, outPin);
        } catch (Exception e) {
            errorPin.getValue(PinString.class).setValue(String.valueOf(e.getMessage()));
            executeNext(runnable, failPin);
        }
    }
}
