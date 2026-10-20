package top.bogey.touch_tool.ui.blueprint.pin_widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able.PinArea;
import top.bogey.touch_tool.databinding.PinWidgetAreaBinding;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.picker.AreaPickerPreview;
import top.bogey.touch_tool.ui.blueprint.pin.PinView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class PinWidgetArea extends PinWidget<PinArea> {
    private final PinWidgetAreaBinding binding;

    public PinWidgetArea(@NonNull Context context, ActionCard card, PinView pinView, PinArea pinBase, boolean custom) {
        super(context, card, pinView, pinBase, custom);
        binding = PinWidgetAreaBinding.inflate(LayoutInflater.from(context), this, true);
        init();
    }

    @Override
    protected void initBase() {
        Rect area = pinBase.getValue();
        pinBase.setValue(area);
        binding.leftEdit.setText(String.valueOf(area.left));
        binding.topEdit.setText(String.valueOf(area.top));
        binding.rightEdit.setText(String.valueOf(area.right));
        binding.bottomEdit.setText(String.valueOf(area.bottom));

        binding.leftEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.left = toInt(s);
                pinView.getPin().notifyValueUpdated(card.getTask());
            }
        });
        binding.topEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.top = toInt(s);
                pinView.getPin().notifyValueUpdated(card.getTask());
            }
        });
        binding.rightEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.right = toInt(s);
                pinView.getPin().notifyValueUpdated(card.getTask());
            }
        });
        binding.bottomEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                area.bottom = toInt(s);
                pinView.getPin().notifyValueUpdated(card.getTask());
            }
        });

        binding.pickButton.setOnClickListener(v -> new AreaPickerPreview(getContext(), result -> {
            area.set(result);
            binding.leftEdit.setText(String.valueOf(area.left));
            binding.topEdit.setText(String.valueOf(area.top));
            binding.rightEdit.setText(String.valueOf(area.right));
            binding.bottomEdit.setText(String.valueOf(area.bottom));
        }, area).show());
    }

    @Override
    protected void initCustom() {

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
