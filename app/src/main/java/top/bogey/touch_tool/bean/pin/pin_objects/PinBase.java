package top.bogey.touch_tool.bean.pin.pin_objects;

import android.graphics.Bitmap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import top.bogey.touch_tool.bean.base.Copyable;
import top.bogey.touch_tool.bean.pin.PinInfo;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_list.PinList;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinDouble;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able.PinImage;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public abstract class PinBase implements Copyable {
    protected final PinType type;
    protected final PinSubType subType;

    protected PinBase() {
        this(PinType.NONE);
    }

    protected PinBase(PinType type) {
        this(type, PinSubType.NORMAL);
    }

    protected PinBase(PinType type, PinSubType subType) {
        this.type = type;
        this.subType = subType;
    }

    protected PinBase(JsonObject jsonObject) {
        type = GsonUtil.getAsObject(jsonObject, "type", PinType.class, PinType.NONE);
        subType = GsonUtil.getAsObject(jsonObject, "subType", PinSubType.class, PinSubType.NORMAL);
    }

    public abstract void reset();

    public abstract void sync(PinBase value);

    // 是否为动态值
    public abstract boolean isDynamic();

    @Override
    public PinBase copy() {
        return GsonUtil.copy(this, PinBase.class);
    }

    @Override
    public PinBase newCopy() {
        PinBase copy = copy();
        copy.reset();
        return copy;
    }

    public boolean linkFromAble(PinBase pin) {
        return getClass().isInstance(pin);
    }

    public boolean linkToAble(PinBase pin) {
        return pin.getClass().isInstance(this);
    }

    public PinType getType() {
        return type;
    }

    public PinSubType getSubType() {
        return subType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PinBase pinBase)) return false;

        return getType() == pinBase.getType() && getSubType() == pinBase.getSubType();
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getSubType().hashCode();
        return result;
    }

    public static class PinBaseDeserializer implements JsonDeserializer<PinBase> {
        @Override
        public PinBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            PinType type = GsonUtil.getAsObject(jsonObject, "type", PinType.class, PinType.NONE);
            PinSubType subType = GsonUtil.getAsObject(jsonObject, "subType", PinSubType.class, PinSubType.NORMAL);
            PinInfo pinInfo = PinInfo.getPinInfo(type, subType);
            if (pinInfo == null)
                return null;
            try {
                Constructor<? extends PinBase> constructor = pinInfo.getClazz().getConstructor(JsonObject.class);
                return constructor.newInstance(jsonObject);
            } catch (Exception e) {
                AppLog.d("针脚", "deserialize pin: " + pinInfo);
                AppLog.d("针脚", "deserialize pin json: " + json);
                throw new JsonParseException(e);
            }
        }
    }

    public static PinBase parseValue(Object value) {
        if (value == null) return new PinString();
        switch (value) {
            case Map<?, ?> map -> {
                PinMap pinMap = new PinMap();
                map.forEach((key, v) -> {
                    PinBase pinKey = parseValue(key);
                    PinBase pinValue = parseValue(v);
                    if (pinMap.isDynamic()) {
                        pinMap.setKeyType(getTypeValue((PinObject) pinKey));
                        pinMap.setValueType(getTypeValue((PinObject) pinValue));
                    }
                    pinMap.put(new PinString(String.valueOf(key)), (PinObject) parseValue(v));
                });
                return pinMap;
            }
            case List<?> list -> {
                PinList pinList = new PinList();
                for (Object v : list) {
                    PinBase pinValue = parseValue(v);
                    if (pinList.isDynamic()) {
                        pinList.setValueType(getTypeValue((PinObject) pinValue));
                    }
                    pinList.add((PinObject) pinValue);
                }
                return pinList;
            }
            case String str -> {
                return new PinString(str);
            }
            case Number num -> {
                return new PinDouble(num.doubleValue());
            }
            case Boolean bool -> {
                return new PinBoolean(bool);
            }
            case Bitmap bitmap -> {
                PinImage image = new PinImage();
                image.setImage(bitmap);
                return image;
            }
            default -> {
                return new PinString(value.toString());
            }
        }
    }

    private static PinObject getTypeValue(PinObject pinObject) {
        if (pinObject instanceof PinMap pinMap) {
            for (Map.Entry<PinObject, PinObject> entry : pinMap.entrySet()) {
                PinObject key = entry.getKey();
                PinObject value = entry.getValue();
                PinObject keyType = getTypeValue(key);
                PinObject valueType = getTypeValue(value);
                return new PinMap(keyType, valueType);
            }
            return new PinMap();
        } else if (pinObject instanceof PinList pinList) {
            for (PinObject value : pinList) {
                PinObject valueType = getTypeValue(value);
                return new PinList(valueType);
            }
            return new PinList();
        } else {
            PinInfo pinInfo = PinInfo.getPinInfo(pinObject);
            return (PinObject) pinInfo.newInstance();
        }
    }
}
