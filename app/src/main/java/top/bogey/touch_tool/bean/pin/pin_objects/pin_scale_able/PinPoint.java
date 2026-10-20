package top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able;

import android.graphics.Point;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.bogey.touch_tool.bean.pin.pin_objects.PinType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.EAnchor;
import top.bogey.touch_tool.utils.GsonUtil;

public class PinPoint extends PinScaleAble<Point> {

    public PinPoint() {
        super(PinType.POINT);
        value = new Point();
    }

    public PinPoint(int x, int y) {
        this();
        value = new Point(x, y);
    }

    public PinPoint(JsonObject jsonObject) {
        super(jsonObject);
        value = GsonUtil.getAsObject(jsonObject, "value", Point.class, new Point());
    }

    @Override
    public void reset() {
        super.reset();
        value = new Point();
    }

    @Override
    public boolean cast(String value) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            try {
                setScale();
                int x = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
                int y = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));
                this.value = new Point(x, y);
                return true;
            } catch (NumberFormatException e) {
                AppLog.e(e);
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        Point point = getValue(EAnchor.TOP_LEFT);
        return super.toString() + "(" + point.x + "," + point.y + ")";
    }

    @Override
    public Point getValue() {
        Point point = super.getValue();
        float scale = getScale();
        if (scale != 1) {
            point = new Point((int) (point.x * scale), (int) (point.y * scale));
        }
        return new Point(point.x, point.y);
    }

    @Override
    public Point getValue(EAnchor anchor) {
        Point point = getValue();
        if (anchor == this.anchor) return point;
        Point anchorPoint = this.anchor.getAnchorPoint();
        point.offset(anchorPoint.x, anchorPoint.y);
        anchorPoint = anchor.getAnchorPoint();
        point.offset(-anchorPoint.x, -anchorPoint.y);
        return point;
    }

    public void setValue(int x, int y) {
        setValue(new Point(x, y));
    }

    @Override
    public void setValue(Point value) {
        super.setValue(value);
    }

    @Override
    public void setValue(EAnchor anchor, Point value) {
        Point anchorPoint = anchor.getAnchorPoint();
        value.offset(-anchorPoint.x, -anchorPoint.y);
        setValue(value);
        this.anchor = anchor;
    }
}
