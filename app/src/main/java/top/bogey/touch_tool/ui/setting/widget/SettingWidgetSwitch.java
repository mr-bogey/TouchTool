package top.bogey.touch_tool.ui.setting.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.databinding.WidgetSettingSwitchBinding;

public class SettingWidgetSwitch extends FrameLayout {
    private final WidgetSettingSwitchBinding binding;

    public SettingWidgetSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = WidgetSettingSwitchBinding.inflate(LayoutInflater.from(context), this, true);
        // 分组首末行的大圆角由布局里的 app:rowCorner 标注
        SettingRowCorner.apply(context, attrs, binding.getRoot());

        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingWidgetSwitch)) {
            int icon = typedArray.getResourceId(R.styleable.SettingWidgetSwitch_icon, 0);
            binding.icon.setImageResource(icon);

            String title = typedArray.getString(R.styleable.SettingWidgetSwitch_title);
            binding.title.setText(title);

            String description = typedArray.getString(R.styleable.SettingWidgetSwitch_description);
            binding.description.setText(description);
            binding.description.setVisibility(description == null || description.isEmpty() ? GONE : VISIBLE);

            binding.switchButton.setSaveEnabled(false);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        binding.divider.setVisibility(listener == null ? GONE : VISIBLE);
        binding.getRoot().setOnClickListener(listener);
    }

    public void setOnSwitchClickListener(OnClickListener listener) {
        binding.switchButton.setOnClickListener(listener);
        binding.getRoot().setOnClickListener(v -> binding.switchButton.performClick());
    }

    public void setChecked(boolean checked) {
        binding.switchButton.setChecked(checked);
    }

    public boolean isChecked() {
        return binding.switchButton.isChecked();
    }

    public void setDescription(String description) {
        binding.description.setText(description);
    }
}
