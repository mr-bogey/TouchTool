package top.bogey.touch_tool.bean.action.system;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_application.PinApplication;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able.PinImage;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinSingleSelect;
import top.bogey.touch_tool.service.TaskRunnable;
import top.bogey.touch_tool.utils.AppUtil;

public class ShareToAction extends ExecuteAction {
    private final transient Pin appPin = new Pin(new PinApplication(PinSubType.SINGLE_SEND_ACTIVITY), R.string.pin_app);
    private final transient Pin valuePin = new Pin(new PinObject(), R.string.pin_object);
    private final transient Pin typePin = new Pin(new PinSingleSelect(R.array.share_type), R.string.share_to_action_as);

    public ShareToAction() {
        super(ActionType.SHARE_TO);
        addPins(appPin, valuePin, typePin);
    }

    public ShareToAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(appPin, valuePin, typePin);
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        PinApplication app = getPinValue(runnable, appPin);
        PinObject value = getPinValue(runnable, valuePin);
        PinSingleSelect type = getPinValue(runnable, typePin);

        Context context = MainApplication.getInstance();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        switch (type.getIndex()) {
            case 0 -> {
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, value.toString());
            }
            case 1 -> {
                String fileName;
                byte[] bytes = new byte[0];
                if (value instanceof PinImage image) {
                    fileName = "Picture_" + AppUtil.formatDateTime(context, System.currentTimeMillis(), false, true) + ".jpg";
                    intent.setType("image/jpeg");
                    Bitmap bitmap = image.getImage();
                    if (bitmap != null) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        bytes = outputStream.toByteArray();
                    }
                } else {
                    fileName = "Text_" + AppUtil.formatDateTime(context, System.currentTimeMillis(), false, true) + ".txt";
                    intent.setType("text/plain");
                    bytes = value.toString().getBytes();
                }
                Uri uri = AppUtil.writeCacheFile(context, AppUtil.DOCUMENT_DIR_NAME, fileName, bytes);
                if (uri != null) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                }
            }
        }

        if (app.getPackageName() == null || app.getPackageName().isEmpty()) {
            Intent chooser = Intent.createChooser(intent, null);
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } else {
            intent.setPackage(app.getPackageName());
            if (app.getFirstActivity() != null) intent.setClassName(app.getPackageName(), app.getFirstActivity());
            context.startActivity(intent);
        }

        executeNext(runnable, outPin);
    }
}
