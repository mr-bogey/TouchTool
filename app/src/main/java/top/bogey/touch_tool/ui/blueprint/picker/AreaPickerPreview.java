package top.bogey.touch_tool.ui.blueprint.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.Editable;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;

import java.util.List;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.save.model.LiteRTModel;
import top.bogey.touch_tool.bean.save.model.ModelResult;
import top.bogey.touch_tool.bean.save.model.ModelSaver;
import top.bogey.touch_tool.databinding.FloatPickerAreaPreviewBinding;
import top.bogey.touch_tool.service.MainAccessibilityService;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.callback.ResultCallback;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class AreaPickerPreview extends BasePicker<Rect> {
    private final FloatPickerAreaPreviewBinding binding;
    private boolean test = false;
    private int ocrAppIndex = 0;

    public AreaPickerPreview(@NonNull Context context, ResultCallback<Rect> callback, Rect rect) {
        super(context, callback);
        editable = true;

        Rect area = new Rect(rect);
        binding = FloatPickerAreaPreviewBinding.inflate(LayoutInflater.from(context), this, true);

        binding.leftEdit.setText(String.valueOf(area.left));
        binding.topEdit.setText(String.valueOf(area.top));
        binding.rightEdit.setText(String.valueOf(area.right));
        binding.bottomEdit.setText(String.valueOf(area.bottom));

        binding.leftEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.left = toInt(s);
            }
        });
        binding.topEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.top = toInt(s);
            }
        });
        binding.rightEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.right = toInt(s);
            }
        });
        binding.bottomEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.bottom = toInt(s);
            }
        });

        binding.switchButton.setVisibility(VISIBLE);
        binding.switchButton.setOnClickListener(v -> {
            test = !test;
            binding.title.setText(test ? R.string.picker_test_title : R.string.picker_area_title);
            binding.contentBox.setVisibility(test ? GONE : VISIBLE);
            binding.testBox.setVisibility(test ? VISIBLE : GONE);
        });


        binding.timeSlider.setLabelFormatter(value -> getContext().getString(R.string.picker_area_offset, (int) value));

        List<LiteRTModel> models = ModelSaver.getInstance().getModelList(LiteRTModel.ModelType.OCR);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.widget_textview_item);
        for (LiteRTModel model : models) {
            adapter.add(model.getName());
        }
        binding.spinner.setOnClickListener(v -> {
            ListPopupWindow popup = new ListPopupWindow(getContext());
            popup.setAdapter(adapter);
            popup.setAnchorView(binding.spinner);
            popup.setOnItemClickListener((parent, view, position, id) -> {
                binding.spinner.setText(adapter.getItem(position));
                ocrAppIndex = position;
                popup.dismiss();
            });
            popup.show();
        });
        if (!adapter.isEmpty()) binding.spinner.setText(adapter.getItem(ocrAppIndex));

        binding.testButton.setOnClickListener(v -> {
            if (ocrAppIndex < models.size()) {
                MainAccessibilityService service = MainApplication.getInstance().getService();
                Bitmap bitmap = service.tryGetScreenShot();
                if (bitmap != null) {
                    Bitmap clipBitmap = DisplayUtil.safeClipBitmap(bitmap, area.left, area.top, area.width(), area.height());
                    if (models.size() > ocrAppIndex) {
                        LiteRTModel model = models.get(ocrAppIndex);
                        List<ModelResult> results = model.execute(service, clipBitmap, binding.timeSlider.getValue() / 100);
                        StringBuilder builder = new StringBuilder();
                        for (ModelResult result : results) {
                            builder.append(result.getText()).append("\n");
                        }
                        post(() -> Toast.makeText(context, builder.toString().trim(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });

        binding.backButton.setOnClickListener(v -> dismiss());

        binding.saveButton.setOnClickListener(v -> {
            if (callback != null) callback.onResult(area);
            dismiss();
        });

        binding.pickerButton.setOnClickListener(v -> new AreaPicker(context, result -> {
            if (result == null) return;
            binding.leftEdit.setText(String.valueOf(result.left));
            binding.topEdit.setText(String.valueOf(result.top));
            binding.rightEdit.setText(String.valueOf(result.right));
            binding.bottomEdit.setText(String.valueOf(result.bottom));
        }, area).show());
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
