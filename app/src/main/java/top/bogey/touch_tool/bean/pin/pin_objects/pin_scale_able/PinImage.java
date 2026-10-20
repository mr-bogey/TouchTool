package top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.bean.pin.pin_objects.PinType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.EAnchor;
import top.bogey.touch_tool.utils.GsonUtil;

public class PinImage extends PinScaleAble<String> {
    private transient Bitmap image;
    private transient boolean serialized = true;

    public PinImage() {
        super(PinType.IMAGE);
    }

    public PinImage(PinSubType subType) {
        super(PinType.IMAGE, subType);
    }

    public PinImage(String value) {
        this();
        this.value = value;
    }

    public PinImage(JsonObject jsonObject) {
        super(jsonObject);
        value = GsonUtil.getAsString(jsonObject, "value", null);
    }

    @Override
    public PinBase copy() {
        PinImage pinImage = new PinImage(getSubType());
        pinImage.setScreen(screen);
        pinImage.setAnchor(anchor);
        pinImage.image = image;
        pinImage.serialized = serialized;
        pinImage.value = value;
        return pinImage;
    }

    public Bitmap getImage() {
        if (image == null || image.isRecycled()) {
            if (value == null || value.isEmpty()) return null;
            try {
                byte[] bytes = Base64.decode(value, Base64.NO_WRAP);
                image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                float scale = getScale();
                if (scale != 1) {
                    image = Bitmap.createScaledBitmap(image, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), true);
                }
            } catch (Exception | Error e) {
                AppLog.e(e);
            }
        }
        return image;
    }

    public void setImage(Bitmap image) {
        setScale();
        this.image = image;
        if (image == null) {
            value = null;
            serialized = true;
            return;
        }
        serialized = false;
    }

    @Override
    public void reset() {
        super.reset();
        value = null;
        image = null;
    }

    @Override
    public void sync(PinBase value) {
        super.sync(value);

        if (value instanceof PinImage pinImage) {
            image = pinImage.image;
            serialized = pinImage.serialized;
        }
    }

    @NonNull
    @Override
    public String toString() {
        Bitmap image = getImage();
        if (image == null) return super.toString();
        else return super.toString() + "[" + image.getWidth() + "x" + image.getHeight() + "]";
    }

    @Override
    public String getValue() {
        if (!serialized && image != null && !image.isRecycled()) {
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                image.compress(Bitmap.CompressFormat.WEBP, 100, stream);
                byte[] bytes = stream.toByteArray();
                value = Base64.encodeToString(bytes, Base64.NO_WRAP);
            } catch (IOException ignored) {
            }
        }
        return super.getValue();
    }

    @Override
    public String getValue(EAnchor anchor) {
        return getValue();
    }

    @Override
    public void setValue(EAnchor anchor, String value) {
        setValue(value);
    }

    public static class PinImageSerializer implements JsonSerializer<PinImage> {
        @Override
        public JsonElement serialize(PinImage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", src.getType().name());
            jsonObject.addProperty("subType", src.getSubType().name());
            jsonObject.addProperty("screen", src.getScreen());
            jsonObject.addProperty("anchor", src.getAnchor().name());
            jsonObject.addProperty("value", src.getValue());
            return jsonObject;
        }
    }
}
