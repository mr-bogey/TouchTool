package top.bogey.touch_tool.ui.setting.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.slider.Slider;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.databinding.WidgetSettingSliderBinding;

public class SettingWidgetSlider extends FrameLayout {
    private final WidgetSettingSliderBinding binding;
    private final ColorStateList descriptionColor;
    // 是否默认收起滑条，只在展开时才占一行高度
    private final boolean collapsible;

    private String description;
    // 收起态显示当前值时的格式化串，如 "%d%%"；为空时只显示数值
    private String valueFormat;
    private boolean expanded = false;
    // 是否已完成首次折叠，折叠时机见 onAttachedToWindow
    private boolean collapsedOnce = false;

    public SettingWidgetSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = WidgetSettingSliderBinding.inflate(LayoutInflater.from(context), this, true);
        descriptionColor = binding.description.getTextColors();

        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingWidgetSlider)) {
            int icon = typedArray.getResourceId(R.styleable.SettingWidgetSlider_icon, 0);
            binding.icon.setImageResource(icon);

            String title = typedArray.getString(R.styleable.SettingWidgetSlider_title);
            binding.title.setText(title);

            description = typedArray.getString(R.styleable.SettingWidgetSlider_description);
            binding.description.setText(description);
            binding.description.setVisibility(description == null || description.isEmpty() ? GONE : VISIBLE);

            int min = typedArray.getInt(R.styleable.SettingWidgetSlider_min, 0);
            int max = typedArray.getInt(R.styleable.SettingWidgetSlider_max, 100);
            int value = typedArray.getInt(R.styleable.SettingWidgetSlider_value, 50);
            int step = typedArray.getInt(R.styleable.SettingWidgetSlider_step, 1);
            binding.slider.setStepSize(step);
            binding.slider.setValueTo(max);
            binding.slider.setValueFrom(min);
            binding.slider.setValue(value);

            collapsible = typedArray.getBoolean(R.styleable.SettingWidgetSlider_collapsible, false);
        }

        // 可折叠时整行点击切换展开，右侧箭头提示方向
        if (collapsible) {
            binding.arrow.setVisibility(VISIBLE);
            binding.getRoot().setOnClickListener(v -> setExpanded(!expanded));
            // 收起态要实时显示当前值
            binding.slider.addOnChangeListener((slider, value, fromUser) -> refreshDescription());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 构造期就隐藏滑条的话，之后再展开可能画不出来；等首次测量布局完成、正式绘制前再折叠
        if (collapsible && !collapsedOnce) {
            collapsedOnce = true;
            getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    setExpanded(false);
                    return true;
                }
            });
        }
    }

    public void setSliderOnChangeListener(Slider.OnChangeListener listener) {
        binding.slider.addOnChangeListener(listener);
    }

    public void setValue(float value) {
        value = Math.clamp(value, binding.slider.getValueFrom(), binding.slider.getValueTo());
        binding.slider.setValue(value);
    }

    public void setValueFormat(String format) {
        valueFormat = format;
        binding.slider.setLabelFormatter(value -> String.format(format, (int) value));
        refreshDescription();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        binding.getRoot().setOnClickListener(listener);
    }

    public void setDescription(String description) {
        this.description = description;
        refreshDescription();
    }

    // 展开态显示说明，收起态显示当前值
    private void setExpanded(boolean value) {
        if (!collapsible) return;
        expanded = value;
        binding.arrow.setImageResource(value ? R.drawable.icon_keyboard_arrow_up : R.drawable.icon_keyboard_arrow_down);
        binding.sliderBox.setVisibility(value ? VISIBLE : GONE);
        refreshDescription();
    }

    private void refreshDescription() {
        if (!collapsible) {
            binding.description.setText(description);
            return;
        }
        if (expanded) {
            binding.description.setText(description);
            binding.description.setTextColor(descriptionColor);
            binding.description.setVisibility(description == null || description.isEmpty() ? GONE : VISIBLE);
            return;
        }

        int value = (int) binding.slider.getValue();
        String current = getContext().getString(R.string.setting_value_current_text,
                valueFormat == null ? String.valueOf(value) : String.format(valueFormat, value));
        binding.description.setText(current);
        binding.description.setTextColor(MaterialColors.getColor(binding.description, androidx.appcompat.R.attr.colorPrimary));
        binding.description.setVisibility(VISIBLE);
    }
}
