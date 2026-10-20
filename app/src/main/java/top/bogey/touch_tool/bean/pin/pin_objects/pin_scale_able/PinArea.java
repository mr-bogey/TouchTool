package top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able;

import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.bean.pin.pin_objects.PinType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.EAnchor;
import top.bogey.touch_tool.utils.GsonUtil;

// 屏幕区域得实时获取才能兼容横竖屏
public class PinArea extends PinScaleAble<Rect> {

    public PinArea() {
        super(PinType.AREA);
        value = getScreenArea();
    }

    public PinArea(Rect area) {
        super(PinType.AREA);
        value = area;
    }

    public PinArea(JsonObject jsonObject) {
        super(jsonObject);
        value = GsonUtil.getAsObject(jsonObject, "value", Rect.class, getScreenArea());
    }

    @Override
    public void reset() {
        super.reset();
        value = getScreenArea();
    }

    @Override
    public boolean cast(String value) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+),(\\d+),(\\d+)\\)");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            try {
                setScale();
                Rect area = new Rect();
                area.left = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                area.top = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));
                area.right = Integer.parseInt(Objects.requireNonNull(matcher.group(3)));
                area.bottom = Integer.parseInt(Objects.requireNonNull(matcher.group(4)));
                this.value = area;
                return true;
            } catch (Exception e) {
                AppLog.e(e);
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        Rect area = getValue(EAnchor.TOP_LEFT);
        return super.toString() + "(" + area.left + "," + area.top + "," + area.right + "," + area.bottom + ")";
    }

    private static boolean isFullScreen(Rect area) {
        Rect screenArea = getScreenArea();
        Rect otherDirectionScreenArea = new Rect(0, 0, screenArea.height(), screenArea.width());
        return area.isEmpty() || screenArea.equals(area) || otherDirectionScreenArea.equals(area);
    }

    private static Rect getScreenArea() {
        return DisplayUtil.getScreenArea(MainApplication.getInstance());
    }

    @Override
    public Rect getValue() {
        Rect area = super.getValue();
        float scale = getScale();
        if (scale != 1) {
            area = new Rect((int) (area.left * scale), (int) (area.top * scale), (int) (area.right * scale), (int) (area.bottom * scale));
        }
        if (isFullScreen(area)) return getScreenArea();
        return new Rect(area);
    }

    @Override
    public Rect getValue(EAnchor anchor) {
        Rect area = getValue();
        if (anchor == this.anchor) return area;
        Point anchorPoint = this.anchor.getAnchorPoint();
        area.offset(anchorPoint.x, anchorPoint.y);
        anchorPoint = anchor.getAnchorPoint();
        area.offset(-anchorPoint.x, -anchorPoint.y);
        return area;
    }

    @Override
    public void setValue(Rect value) {
        super.setValue(value);
    }

    @Override
    public void setValue(EAnchor anchor, Rect value) {
        Point anchorPoint = anchor.getAnchorPoint();
        value.offset(-anchorPoint.x, -anchorPoint.y);
        setValue(value);
        this.anchor = anchor;
    }
}
