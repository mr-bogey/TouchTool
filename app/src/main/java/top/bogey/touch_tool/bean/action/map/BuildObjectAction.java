package top.bogey.touch_tool.bean.action.map;

import com.google.gson.JsonObject;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinAny;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinMap;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_execute.PinExecute;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_list.PinList;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.service.TaskRunnable;

// 组对象：向字典写入键值或向列表追加值，结果装箱为任意值；可串联多个本节点逐字段组对象
public class BuildObjectAction extends ExecuteAction {
    private final transient Pin basePin = new Pin(new PinObject(PinSubType.DYNAMIC), R.string.pin_base_object);
    private final transient Pin keyPin = new Pin(new PinString(), R.string.pin_item_key);
    private final transient Pin valuePin = new Pin(new PinObject(PinSubType.DYNAMIC), R.string.pin_object);
    private final transient Pin resultPin = new Pin(new PinAny(), R.string.pin_any, true);
    private final transient Pin errorPin = new Pin(new PinString(), R.string.action_error, true);
    private final transient Pin failPin = new Pin(new PinExecute(), R.string.action_fail, true);

    public BuildObjectAction() {
        super(ActionType.BUILD_OBJECT);
        addPins(basePin, keyPin, valuePin, resultPin, errorPin, failPin);
    }

    public BuildObjectAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(basePin, keyPin, valuePin, resultPin, errorPin, failPin);
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        try {
            PinObject base = getPinValue(runnable, basePin);
            // 任意值输入先解包：串联组对象时基对象是上一级的装箱结果，解包才能保留已有内容
            if (base instanceof PinAny a && a.getPayload() instanceof PinObject po) base = po;
            String key = getPinValue(runnable, keyPin).toString();
            PinObject value = getPinValue(runnable, valuePin);
            // 值为任意值时解一层，存入实际内容而非值包本身
            if (value instanceof PinAny a && a.getPayload() instanceof PinObject po) value = po;
            // 基础对象为空对象时按有无键新建字典或列表；否则在副本上写入，不污染来源值
            PinBase result;
            if (base instanceof PinMap map) {
                PinMap copy = map.copy();
                copy.put(new PinString(key), value);
                result = copy;
            } else if (base instanceof PinList list) {
                PinList copy = list.copy();
                copy.add(value);
                result = copy;
            } else if (key.isEmpty()) {
                PinList list = new PinList();
                list.add(value);
                result = list;
            } else {
                PinMap map = new PinMap();
                map.put(new PinString(key), value);
                result = map;
            }
            resultPin.setValue(new PinAny(result));
            executeNext(runnable, outPin);
        } catch (Exception e) {
            errorPin.getValue(PinString.class).setValue(String.valueOf(e.getMessage()));
            executeNext(runnable, failPin);
        }
    }
}
