package top.bogey.touch_tool.ui.blueprint.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.text.Editable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.databinding.FloatPickerPointPreviewBinding;
import top.bogey.touch_tool.service.MainAccessibilityService;
import top.bogey.touch_tool.ui.custom.float_view.TouchPathFloatView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.callback.ResultCallback;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class PointPickerPreview extends BasePicker<Point> {
    private final FloatPickerPointPreviewBinding binding;

    public PointPickerPreview(@NonNull Context context, ResultCallback<Point> callback, Point p) {
        super(context, callback);
        editable = true;
        binding = FloatPickerPointPreviewBinding.inflate(LayoutInflater.from(context), this, true);
        Point point = new Point(p);

        binding.xEdit.setText(String.valueOf(point.x));
        binding.yEdit.setText(String.valueOf(point.y));
        binding.xEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                point.x = toInt(s);
            }
        });

        binding.yEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                point.y = toInt(s);
            }
        });

        binding.playButton.setOnClickListener(v -> {
            MainAccessibilityService service = MainApplication.getInstance().getService();
            service.runGesture(point.x, point.y, 50, null);
            TouchPathFloatView.showGesture(point.x, point.y);
        });

        binding.backButton.setOnClickListener(v -> dismiss());

        binding.saveButton.setOnClickListener(v -> {
            if (callback != null) callback.onResult(point);
            dismiss();
        });

        binding.pickerButton.setOnClickListener(v -> new PointPicker(context, result -> {
            point.set(result.x, result.y);
            binding.xEdit.setText(String.valueOf(result.x));
            binding.yEdit.setText(String.valueOf(result.y));
        }, point).show());
    }

    private int toInt(Editable s) {
        if (s == null || s.length() == 0) return 0;
        try {
            return Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
            AppLog.e(e);
            return 0;
        }
    }
}
