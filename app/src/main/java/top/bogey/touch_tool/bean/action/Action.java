package top.bogey.touch_tool.bean.action;

import android.graphics.Point;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.base.Identity;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.PinListener;
import top.bogey.touch_tool.bean.pin.pin_objects.PinAdd;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinMap;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_list.PinList;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.service.TaskRunnable;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public abstract class Action extends Identity implements PinListener {
    // 任务中只能存在一个
    public final static int SINGLE_IN_TASK = 1;
    // 任务中这个动作互相同步数据
    public final static int SYNC_IN_TASK = 2;


    private final ActionType type;
    private final List<Pin> pins = new ArrayList<>();

    private ExpandType expandType;
    private boolean locked = false;
    private Point pos = new Point();

    protected transient List<Pin> tmpPins = new ArrayList<>();
    protected final transient Set<ActionListener> listeners = new HashSet<>();

    private transient int flag = 0;

    protected Action(ActionType type) {
        this.type = type;
        expandType = ExpandType.values()[SettingSaver.BLUEPRINT_CARD_EXPAND_STATE.get()];
    }

    protected Action(JsonObject jsonObject) {
        super(jsonObject);
        type = GsonUtil.getAsObject(jsonObject, "type", ActionType.class, null);
        assert type != null;
        expandType = GsonUtil.getAsObject(jsonObject, "expandType", ExpandType.class, ExpandType.HALF);
        locked = GsonUtil.getAsBoolean(jsonObject, "locked", false);
        pos = GsonUtil.getAsObject(jsonObject, "pos", Point.class, new Point());
        tmpPins = GsonUtil.getAsObject(jsonObject, "pins", TypeToken.getParameterized(ArrayList.class, Pin.class).getType(), new ArrayList<>());
    }

    public void addPin(Pin pin) {
        addPin(pins.size(), pin);
    }

    public void addPin(Pin flag, Pin pin) {
        int index = pins.indexOf(flag);
        if (index == -1) return;
        addPin(index, pin);
    }

    public void addPin(int index, Pin pin) {
        if (pin == null) return;
        if (getPinById(pin.getId()) != null) return;
        pins.add(index, pin);
        pin.setOwnerId(getId());
        pin.addListener(this);
        listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onPinAdded(pin, index));
    }

    public void addPins(Pin... pins) {
        for (Pin pin : pins) addPin(pin);
    }

    // 从临时列表中获取一个类似的针脚加入到正式列表中
    public void reAddPin(Pin def) {
        reAddPin(def, false);
    }

    public void reAddPin(Pin def, boolean backSync) {
        if (!tmpPins.isEmpty()) {
            Pin tmpPin = tmpPins.get(0);
            if (def.isSameClass(tmpPin)) {
                tmpPins.remove(0);
                def.sync(tmpPin);
            } else {
                if (backSync) {
                    tmpPins.remove(0);
                    def.setValue(tmpPin.getValue());
                    def.sync(tmpPin);
                }
            }
        }
        addPin(def);
    }

    public void reAddPins(Pin... pins) {
        List<Pin> list = new ArrayList<>(Arrays.asList(pins));
        for (Pin e : list) reAddPin(e);
    }

    // 从临时列表获取一系列类似针脚加入到正式列表中，直到出现添加针脚为止
    public void reAddPins(Pin def) {
        if (tmpPins.isEmpty()) {
            return;
        }

        Pin tmpPin = tmpPins.get(0);
        while (!tmpPin.isSameClass(PinAdd.class)) {
            if (def.isSameClass(tmpPin)) {
                Pin copy = def.newCopy();
                if (copy.getTitleId() == 0) {
                    copy.setTitle(tmpPin.getTitle());
                }
                reAddPin(copy);
            } else {
                tmpPins.remove(0);
            }
            if (tmpPins.isEmpty()) break;
            tmpPin = tmpPins.get(0);
        }
    }

    // 从临时列表获取一系列针脚加入到正式列表中，直到出现添加针脚或到结束
    public void reAddPins(Pin def, boolean toEnd) {
        if (tmpPins.isEmpty()) {
            return;
        }

        Pin tmpPin = tmpPins.get(0);
        while (toEnd || !tmpPin.isSameClass(PinAdd.class)) {
            if (def != null) tmpPin.setTitleId(def.getTitleId());
            addPin(tmpPin);
            tmpPins.remove(0);

            if (tmpPins.isEmpty()) break;
            tmpPin = tmpPins.get(0);
        }
    }

    public void removePin(Pin pin) {
        if (pin == null) return;
        if (pins.remove(pin)) {
            pin.removeListener(this);
            listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onPinRemoved(pin));
        }
    }

    public void removePin(Task context, Pin pin) {
        if (pin == null) return;
        pin.clearLinks(context);
        removePin(pin);
    }

    public Pin getPinById(String id) {
        return pins.stream().filter(pin -> pin.getId().equals(id)).findFirst().orElse(null);
    }

    public Pin getPinByUid(String uid) {
        return pins.stream().filter(pin -> pin.getUid().equals(uid)).findFirst().orElse(null);
    }

    public Pin getPinByTitle(String title) {
        return pins.stream().filter(p -> p.isOut() && p.getTitle().equals(title)).findFirst().orElse(null);
    }

    public Pin findConnectToAblePin(Pin pin) {
        return pins.stream().filter(p -> p.linkAble() && p.linkAble(pin)).findFirst().orElse(null);
    }

    public void addListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ActionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Action copy() {
        return GsonUtil.copy(this, Action.class);
    }

    @Override
    public Action newCopy() {
        Action copy = copy();
        copy.setId(UUID.randomUUID().toString());
        copy.setLocked(false);
        copy.getPins().forEach(pin -> {
            pin.setId(UUID.randomUUID().toString());
            pin.setOwnerId(copy.getId());
            pin.getLinks().clear();
        });

        copy.pos.offset(1, 1);
        return copy;
    }

    @Override
    public String getTitle() {
        ActionInfo info = ActionInfo.getActionInfo(getType());
        if (info == null) return "";
        return info.getTitle();
    }

    public abstract void execute(TaskRunnable runnable, Pin pin);

    public void executeNext(TaskRunnable runnable, Pin pin) {
        beforeExecuteNext(runnable, pin);

        if (runnable.isCurrentInterrupt()) return;
        if (pin == null) return;
        if (!pin.isOut()) return;

        Pin linkedPin = pin.getLinkedPin(runnable.getTask());
        if (linkedPin == null) return;
        Action action = runnable.getTask().getAction(linkedPin.getOwnerId());
        if (action == null) return;

        runnable.addExecuteProgress(action);
        runnable.addDebugLog(action, 1);
        action.resetReturnValue(runnable, linkedPin);
        action.execute(runnable, linkedPin);
    }

    public void beforeExecuteNext(TaskRunnable runnable, Pin pin) {
        runnable.addDebugLog(this, -1);
    }

    public abstract void calculate(TaskRunnable runnable, Pin pin);

    public void resetReturnValue(TaskRunnable runnable, Pin pin) {

    }

    public <T extends PinObject> T getPinValue(TaskRunnable runnable, Pin pin) {
        if (pin.isOut()) {
            resetReturnValue(runnable, pin);
            calculate(runnable, pin);
            runnable.addDebugLog(this, 0);
            runnable.addCalculateProgress(this);
            return returnValue(pin.getValue());
        }

        if (pin.isLinked()) {
            Pin linkedPin = pin.getLinkedPin(runnable.getTask());
            if (linkedPin != null) {
                Action action = runnable.getTask().getAction(linkedPin.getOwnerId());
                if (action != null) {
                    T pinValue = action.getPinValue(runnable, linkedPin);
                    pin.setValue(pinValue);
                    return pinValue;
                }
            }
        }
        return returnValue(pin.getValue());
    }

    // 控件、列表和集合类型返回引用，其他类型返回拷贝
    protected <T extends PinObject> T returnValue(T value) {
        if (value instanceof PinList || value instanceof PinMap) return value;
        return (T) value.copy();
    }

    public void check(ActionCheckResult result, Task task) {
        // 检查所有输入针脚是否真的能连上
        getPins().forEach(pin -> {
            if (!pin.isOut() && !pin.isVertical()) {
                Pin linkedPin = pin.getLinkedPin(task);
                if (linkedPin != null && !pin.linkAble(linkedPin)) {
                    result.addResult(ActionCheckResult.ResultType.ERROR, R.string.check_pin_linkable_error);
                }
            }
        });
    }

    @Override
    public void onLinkedTo(Task task, Pin origin, Pin to) {
        listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onPinChanged(origin));
    }

    @Override
    public void onUnLinkedFrom(Task task, Pin origin, Pin from) {
        listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onPinChanged(origin));
    }

    @Override
    public void onValueReplaced(Task task, Pin origin, PinBase value) {
        listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onPinChanged(origin));
    }

    @Override
    public void onValueUpdated(Task task, Pin origin, PinBase value) {
        listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onPinChanged(origin));
    }

    @Override
    public void onTitleChanged(Pin origin, String title) {
        listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onPinChanged(origin));
    }

    public ActionType getType() {
        return type;
    }

    public List<Pin> getPins() {
        return pins;
    }

    public ExpandType getExpandType() {
        return expandType;
    }

    public void setExpandType(ExpandType expandType) {
        this.expandType = expandType;
    }

    public boolean canExpand() {
        for (Pin pin : getPins()) {
            if (pin.isHide()) return true;
        }
        return false;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public void setPos(int x, int y) {
        pos.set(x, y);
    }


    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void addFlag(int flag) {
        this.flag |= flag;
    }

    public void toggleFlag(int flag) {
        this.flag ^= flag;
    }

    public boolean hasFlag(int flag) {
        return (this.flag & flag) != 0;
    }

    public enum ExpandType {
        NONE, HALF, FULL
    }

    public static class ActionDeserializer implements JsonDeserializer<Action> {
        @Override
        public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            ActionType type = GsonUtil.getAsObject(jsonObject, "type", ActionType.class, null);
            if (type == null) return null;
            ActionInfo info = ActionInfo.getActionInfo(type);
            if (info == null) return null;
            try {
                Constructor<? extends Action> constructor = info.getClazz().getConstructor(JsonObject.class);
                return constructor.newInstance(jsonObject);
            } catch (Exception e) {
                AppLog.d("动作", "deserialize action: " + info);
                AppLog.d("动作", "deserialize action json: " + json);
                AppLog.e(e);
                return null;
            }
        }
    }
}
