package top.bogey.touch_tool.bean.pin.pin_objects.pin_execute;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.GsonUtil;

public class PinIconExecute extends PinStringExecute {
    private String image;
    private transient Bitmap bitmap;

    public PinIconExecute() {
        super(PinSubType.WITH_ICON);
    }

    public PinIconExecute(JsonObject jsonObject) {
        super(jsonObject);
        image = GsonUtil.getAsString(jsonObject, "image", null);
    }

    public Bitmap getImage() {
        if (bitmap == null || bitmap.isRecycled()) {
            if (image == null || image.isEmpty()) return null;
            try {
                byte[] bytes = Base64.decode(image, Base64.NO_WRAP);
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception | Error e) {
                AppLog.e(e);
            }
        }
        return bitmap;
    }

    public void setImage(Bitmap image) {
        if (image == null) {
            this.image = null;
            return;
        }

        float px = DisplayUtil.dp2px(MainApplication.getInstance(), 28);
        image = DisplayUtil.createScaledBitmap(image, (int) px, (int) px);
        this.bitmap = image;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            image.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            byte[] bytes = stream.toByteArray();
            this.image = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            AppLog.e(e);
        }
    }

    @Override
    public void reset() {
        super.reset();
        image = null;
        bitmap = null;
    }

    @Override
    public void sync(PinBase value) {
        super.sync(value);
        if (value instanceof PinIconExecute pinIcon) {
            this.image = pinIcon.image;
        }
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PinIconExecute that)) return false;
        if (!super.equals(object)) return false;

        return Objects.equals(image, that.image) && Objects.equals(getImage(), that.getImage());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(image);
        result = 31 * result + Objects.hashCode(getImage());
        return result;
    }
}
