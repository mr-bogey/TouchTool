package top.bogey.touch_tool.ui.setting.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.color.MaterialColors;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.databinding.WidgetSettingHeroBinding;

/**
 * 整宽的大号主操作行，用于承载主要动作（功能启用）
 * 右上角可叠一个纯图标按钮，图标染成本行的前景色
 */
public class SettingWidgetHero extends FrameLayout {
    private final WidgetSettingHeroBinding binding;

    private boolean checked = false;

    public SettingWidgetHero(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = WidgetSettingHeroBinding.inflate(LayoutInflater.from(context), this, true);

        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingWidgetHero)) {
            int icon = typedArray.getResourceId(R.styleable.SettingWidgetHero_icon, 0);
            binding.icon.setImageResource(icon);

            String title = typedArray.getString(R.styleable.SettingWidgetHero_title);
            binding.title.setText(title);

            String description = typedArray.getString(R.styleable.SettingWidgetHero_description);
            binding.description.setText(description);
            binding.description.setVisibility(description == null || description.isEmpty() ? GONE : VISIBLE);
        }

        refresh();

        // 角标宽度要等布局完成才知道，届时再让出位置
        binding.getRoot().addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (binding.corner.getVisibility() != VISIBLE) return;
            int padding = dimen(R.attr.halfIconButtonSize);
            int endPadding = padding + binding.corner.getWidth() + dimen(R.attr.defaultPadding);
            if (binding.content.getPaddingEnd() != endPadding) binding.content.setPaddingRelative(padding, padding, endPadding, padding);
        });
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        binding.getRoot().setOnClickListener(listener);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        refresh();
    }

    // 角标只显示图标，文字改成无障碍描述，避免在主按钮上再压一条胶囊
    public void setCornerIcon(int iconRes, CharSequence description) {
        binding.corner.setIconResource(iconRes);
        binding.corner.setContentDescription(description);
        binding.corner.setVisibility(iconRes == 0 ? GONE : VISIBLE);
        refresh();
    }

    public void setOnCornerClickListener(OnClickListener listener) {
        binding.corner.setOnClickListener(listener);
    }

    // 启用态用主色、未启用态用第三色容器，两种状态换色相而非只调明暗
    private void refresh() {
        int containerColor;
        int contentColor;
        if (checked) {
            containerColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary);
            contentColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary);
        } else {
            containerColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorTertiaryContainer);
            contentColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnTertiaryContainer);
        }

        binding.getRoot().setCardBackgroundColor(containerColor);
        binding.icon.setImageTintList(ColorStateList.valueOf(contentColor));
        binding.title.setTextColor(contentColor);
        binding.state.setTextColor(withAlpha(contentColor, 0.78f));
        binding.state.setText(checked ? R.string.setting_hero_on : R.string.setting_hero_off);
        binding.description.setTextColor(withAlpha(contentColor, 0.78f));

        // 现成的图标按钮样式负责圆底与水波纹，这里只改底色和图标色
        binding.corner.setBackgroundTintList(ColorStateList.valueOf(withAlpha(contentColor, 0.18f)));
        binding.corner.setIconTint(ColorStateList.valueOf(contentColor));

        // 角标叠在右上角，显示时给内容让出位置
        int padding = dimen(R.attr.halfIconButtonSize);
        int endPadding = binding.corner.getVisibility() == VISIBLE && binding.corner.getWidth() > 0
                ? padding + binding.corner.getWidth() + dimen(R.attr.defaultPadding) : padding;
        binding.content.setPaddingRelative(padding, padding, endPadding, padding);
    }

    // 取主题里定义的尺寸，避免在代码里写死 dp
    private int dimen(int attr) {
        TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(attr, value, true)) return 0;
        return TypedValue.complexToDimensionPixelSize(value.data, getResources().getDisplayMetrics());
    }

    private static int withAlpha(int color, float alpha) {
        return ColorUtils.setAlphaComponent(color, Math.round(Color.alpha(color) * alpha));
    }
}
