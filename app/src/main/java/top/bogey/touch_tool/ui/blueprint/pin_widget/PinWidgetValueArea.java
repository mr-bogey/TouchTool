package top.bogey.touch_tool.ui.blueprint.pin_widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.pin.pin_objects.PinValueArea;
import top.bogey.touch_tool.databinding.PinWidgetValueAreaBinding;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.pin.PinView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class PinWidgetValueArea extends PinWidget<PinValueArea> {
    private final PinWidgetValueAreaBinding binding;
    private boolean locked;

    public PinWidgetValueArea(@NonNull Context context, ActionCard card, PinView pinView, PinValueArea pinBase, boolean custom) {
        super(context, card, pinView, pinBase, custom);
        binding = PinWidgetValueAreaBinding.inflate(LayoutInflater.from(context), this, true);
        init();
    }

    @Override
    protected void initBase() {
        binding.lowEdit.setText(String.valueOf(pinBase.getMin()));
        binding.highEdit.setText(String.valueOf(pinBase.getMax()));
        locked = pinBase.getMin() == pinBase.getMax();
        binding.lockButton.setChecked(locked);
        binding.lockButton.setIconResource(locked ? R.drawable.icon_lock : R.drawable.icon_lock_open);
        binding.highEdit.setEnabled(!locked);

        binding.lowEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                pinBase.setMin(toInt(s));
                if (binding.lockButton.isChecked()) {
                    binding.highEdit.setText(s);
                } else {
                    pinView.getPin().notifyValueUpdated(card.getTask());
                }
            }
        });

        binding.highEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                pinBase.setMax(toInt(s));
                pinView.getPin().notifyValueUpdated(card.getTask());
            }
        });

        binding.lockButton.setOnClickListener(v -> {
            locked = !locked;
            binding.highEdit.setEnabled(!locked);
            binding.highEdit.setText(binding.lowEdit.getText());
            binding.lockButton.setChecked(locked);
            binding.lockButton.setIconResource(locked ? R.drawable.icon_lock : R.drawable.icon_lock_open);
        });
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
