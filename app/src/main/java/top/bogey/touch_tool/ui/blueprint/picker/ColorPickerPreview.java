package top.bogey.touch_tool.ui.blueprint.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import java.util.List;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able.PinColor;
import top.bogey.touch_tool.databinding.FloatPickerColorPreviewBinding;
import top.bogey.touch_tool.service.MainAccessibilityService;
import top.bogey.touch_tool.ui.custom.float_view.KeepAliveFloatView;
import top.bogey.touch_tool.ui.custom.float_view.TouchPathFloatView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.callback.ResultCallback;
import top.bogey.touch_tool.utils.float_window_manager.FloatWindow;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class ColorPickerPreview extends BasePicker<PinColor.ColorInfo> {
    private final FloatPickerColorPreviewBinding binding;
    private boolean test;

    public static void showPicker(ResultCallback<PinColor.ColorInfo> callback, int value) {
        KeepAliveFloatView keepView = (KeepAliveFloatView) FloatWindow.getView(KeepAliveFloatView.class.getName());
        if (keepView == null) return;
        new Handler(Looper.getMainLooper()).post(() -> {
            ColorPickerPreview colorPickerPreview = new ColorPickerPreview(keepView.getThemeContext(), callback, new PinColor.ColorInfo(value, 0, Integer.MAX_VALUE));
            colorPickerPreview.show();
        });
    }

    public ColorPickerPreview(@NonNull Context context, ResultCallback<PinColor.ColorInfo> callback, PinColor.ColorInfo color) {
        super(context, callback);
        editable = true;

        binding = FloatPickerColorPreviewBinding.inflate(LayoutInflater.from(context), this, true);

        PinColor.ColorInfo colorInfo = new PinColor.ColorInfo(color.getColor(), color.getMinArea(), color.getMaxArea());

        int colorValue = colorInfo.getColor();
        binding.redEdit.setText(String.valueOf(Color.red(colorValue)));
        binding.greenEdit.setText(String.valueOf(Color.green(colorValue)));
        binding.blueEdit.setText(String.valueOf(Color.blue(colorValue)));

        refreshColorInfo(colorInfo);

        binding.redEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                colorInfo.setRed(toColorInt(s));
                refreshColorInfo(colorInfo);
            }
        });
        binding.greenEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                colorInfo.setGreen(toColorInt(s));
                refreshColorInfo(colorInfo);
            }
        });
        binding.blueEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                colorInfo.setBlue(toColorInt(s));
                refreshColorInfo(colorInfo);
            }
        });

        binding.copyRgbButton.setOnClickListener(v -> AppUtil.copyToClipboard(context, binding.colorRgbText.getText().toString()));
        binding.copyHexButton.setOnClickListener(v -> AppUtil.copyToClipboard(context, binding.colorHexText.getText().toString()));

        binding.switchButton.setVisibility(VISIBLE);
        binding.switchButton.setOnClickListener(v -> {
            test = !test;
            binding.title.setText(test ? R.string.picker_test_title : R.string.picker_color_title);
            binding.contentBox.setVisibility(test ? GONE : VISIBLE);
            binding.testBox.setVisibility(test ? VISIBLE : GONE);
            if (test) {
                FloatWindow.hide(tag);
                postDelayed(() -> {
                    matchImage(colorInfo);
                    FloatWindow.show(tag);
                }, 100);
            }
        });

        binding.backButton.setOnClickListener(v -> dismiss());

        binding.saveButton.setOnClickListener(v -> {
            if (callback != null) callback.onResult(colorInfo);
            dismiss();
        });

        binding.pickerButton.setOnClickListener(v -> new ColorPicker(context, result -> {
            colorInfo.setColor(result.getColor());
            colorInfo.setMinArea(result.getMinArea());
            colorInfo.setMaxArea(result.getMaxArea());
            int resultColor = result.getColor();

            binding.redEdit.setText(String.valueOf(Color.red(resultColor)));
            binding.greenEdit.setText(String.valueOf(Color.green(resultColor)));
            binding.blueEdit.setText(String.valueOf(Color.blue(resultColor)));

            refreshColorInfo(colorInfo);
        }, colorInfo).show());

        binding.timeSlider.setLabelFormatter(value -> getContext().getString(R.string.picker_color_offset, (int) value));
        binding.matchButton.setOnClickListener(v -> {
            FloatWindow.hide(tag);
            postDelayed(() -> {
                matchImage(colorInfo);
                FloatWindow.show(tag);
            }, 100);
        });

        binding.touchButton.setOnClickListener(v -> {
            MainAccessibilityService service = MainApplication.getInstance().getService();
            if (service != null && service.isEnabled()) {
                FloatWindow.hide(tag);
                postDelayed(() -> {
                    Bitmap bitmap = service.tryGetScreenShot();
                    FloatWindow.show(tag);
                    if (bitmap != null) {
                        int offset = (int) binding.timeSlider.getValue();
                        List<Rect> rectList = DisplayUtil.matchColor(bitmap, colorInfo.getColor(), null, offset);
                        if (rectList == null || rectList.isEmpty()) return;
                        Rect rect = rectList.get(0);
                        int x = rect.left + rect.width() / 2;
                        int y = rect.top + rect.height() / 2;
                        service.runGesture(x, y, 50, null);
                        TouchPathFloatView.showGesture(x, y);
                    }
                }, 100);
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void refreshColorInfo(PinColor.ColorInfo colorInfo) {
        binding.color.setBackgroundColor(colorInfo.getColor());
        binding.colorRgbText.setText(String.format("%d,%d,%d", Color.red(colorInfo.getColor()), Color.green(colorInfo.getColor()), Color.blue(colorInfo.getColor())));
        binding.colorHexText.setText(String.format("#%06X", 0xFFFFFF & colorInfo.getColor()));
    }

    private int toColorInt(Editable s) {
        if (s == null || s.length() == 0) return 0;
        try {
            int i = Integer.parseInt(s.toString());
            return Math.clamp(i, 0, 255);
        } catch (NumberFormatException e) {
            AppLog.e(e);
            return 0;
        }
    }

    private void matchImage(PinColor.ColorInfo colorInfo) {
        MainAccessibilityService service = MainApplication.getInstance().getService();
        if (service == null || !service.isEnabled()) return;

        Bitmap bitmap = service.tryGetScreenShot();
        if (bitmap != null) {
            int offset = (int) binding.timeSlider.getValue();
            List<Rect> rectList = DisplayUtil.matchColor(bitmap, colorInfo.getColor(), null, offset);
            if (rectList == null || rectList.isEmpty()) binding.matchedImage.setImageDrawable(null);
            else {
                Rect rect = rectList.get(0);
                for (Rect r : rectList) {
                    int areaSize = r.width() * r.height();
                    if (areaSize >= colorInfo.getMinArea() && areaSize <= colorInfo.getMaxArea()) {
                        rect = r;
                        break;
                    }
                }

                int px = (int) DisplayUtil.dp2px(getContext(), 16);
                Rect area = DisplayUtil.safeClipBitmapArea(bitmap, rect.left - px, rect.top - px, rect.width() + px * 2, rect.height() + px * 2);
                if (area == null) return;
                Bitmap clipBitmap = DisplayUtil.safeClipBitmap(bitmap, area.left, area.top, area.width(), area.height());
                if (clipBitmap == null) return;
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStrokeWidth(2);
                paint.setStyle(Paint.Style.STROKE);
                Canvas canvas = new Canvas(clipBitmap);
                canvas.translate(rect.left - area.left, rect.top - area.top);
                canvas.drawRect(new Rect(0, 0, rect.width(), rect.height()), paint);
                binding.matchedImage.setImageBitmap(clipBitmap);
            }
        }
    }
}
