package top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.Objects;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.bean.pin.pin_objects.PinType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.EAnchor;
import top.bogey.touch_tool.utils.GsonUtil;

public abstract class PinScaleAble<T> extends PinObject {
    private final static int SCREEN = DisplayUtil.getScreenWidth(MainApplication.getInstance());

    protected int screen = SCREEN;
    protected EAnchor anchor = EAnchor.TOP_LEFT;
    protected T value;

    protected PinScaleAble(PinType type) {
        super(type);
    }

    protected PinScaleAble(PinType type, PinSubType subType) {
        super(type, subType);
    }

    protected PinScaleAble(JsonObject jsonObject) {
        super(jsonObject);
        screen = GsonUtil.getAsInt(jsonObject, "screen", SCREEN);
        anchor = GsonUtil.getAsObject(jsonObject, "anchor", EAnchor.class, EAnchor.TOP_LEFT);
    }

    protected float getScale() {
        if (screen == 0) return 1f;
        return DisplayUtil.getScreenWidth(MainApplication.getInstance()) * 1f / screen;
    }

    protected void setScale() {
        screen = DisplayUtil.getScreenWidth(MainApplication.getInstance());
    }

    @Override
    public void reset() {
        screen = SCREEN;
        anchor = EAnchor.TOP_LEFT;
    }

    @Override
    public void sync(PinBase value) {
        if (value instanceof PinScaleAble<?> pinScale) {
            screen = pinScale.screen;
            anchor = pinScale.anchor;
            this.value = (T) pinScale.value;
        }
    }

    @Override
    public boolean cast(String value) {
        try {
            screen = Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            AppLog.e(e);
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(screen);
    }

    public int getScreen() {
        return screen;
    }

    public void setScreen(int screen) {
        this.screen = screen;
    }

    public EAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(EAnchor anchor) {
        this.anchor = anchor;
    }

    public T getValue() {
        return value;
    }

    public abstract T getValue(EAnchor anchor);

    public void setValue(T value) {
        setScale();
        this.value = value;
    }

    public abstract void setValue(EAnchor anchor, T value);

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PinScaleAble<?> that)) return false;
        if (!super.equals(object)) return false;

        return getScreen() == that.getScreen() && getAnchor() == that.getAnchor() && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getScreen();
        result = 31 * result + getAnchor().hashCode();
        result = 31 * result + Objects.hashCode(getValue());
        return result;
    }
}
