package top.bogey.touch_tool.ui.blueprint.pin_widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.text.Editable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import top.bogey.touch_tool.bean.pin.pin_objects.pin_scale_able.PinPoint;
import top.bogey.touch_tool.databinding.PinWidgetPointBinding;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.picker.PointPickerPreview;
import top.bogey.touch_tool.ui.blueprint.pin.PinView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class PinWidgetPoint extends PinWidget<PinPoint> {
    private final PinWidgetPointBinding binding;

    public PinWidgetPoint(@NonNull Context context, ActionCard card, PinView pinView, PinPoint pinBase, boolean custom) {
        super(context, card, pinView, pinBase, custom);
        binding = PinWidgetPointBinding.inflate(LayoutInflater.from(context), this, true);
        init();
    }

    @Override
    protected void initBase() {
        Point point = pinBase.getValue();
        pinBase.setValue(point);
        binding.xEdit.setText(String.valueOf(point.x));
        binding.yEdit.setText(String.valueOf(point.y));

        binding.xEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                point.x = toInt(s);
                pinView.getPin().notifyValueUpdated(card.getTask());
            }
        });

        binding.yEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                point.y = toInt(s);
                pinView.getPin().notifyValueUpdated(card.getTask());
            }
        });

        binding.pickButton.setOnClickListener(v -> new PointPickerPreview(getContext(), result -> {
            point.set(result.x, result.y);
            pinView.getPin().notifyValueUpdated(card.getTask());
            binding.xEdit.setText(String.valueOf(point.x));
            binding.yEdit.setText(String.valueOf(point.y));
        }, point).show());
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
