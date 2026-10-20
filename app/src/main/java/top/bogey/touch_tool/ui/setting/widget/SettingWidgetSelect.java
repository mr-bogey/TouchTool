package top.bogey.touch_tool.ui.setting.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.MaterialColors;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.databinding.WidgetSettingSelectBinding;
import top.bogey.touch_tool.databinding.WidgetSettingSelectButtonHorizontalBinding;

public class SettingWidgetSelect extends FrameLayout {
    private final WidgetSettingSelectBinding binding;
    private final ColorStateList descriptionColor;
    private final GradientDrawable cornerBackground = new GradientDrawable();
    // 展开时按钮组自上而下划出的动画
    private final SettingRevealAnimator reveal;
    // 是否默认收起按钮组，只在展开时才占一行高度
    private final boolean collapsible;

    private final String description;
    private boolean expanded = false;
    private int selectedIndex = 0;
    // 是否已完成首次折叠，折叠时机见 onAttachedToWindow
    private boolean collapsedOnce = false;

    public SettingWidgetSelect(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = WidgetSettingSelectBinding.inflate(LayoutInflater.from(context), this, true);
        // 分组首末行的大圆角由布局里的 app:rowCorner 标注
        SettingRowCorner.apply(context, attrs, binding.getRoot());
        reveal = new SettingRevealAnimator(binding.group);
        descriptionColor = binding.description.getTextColors();
        cornerBackground.setShape(GradientDrawable.RECTANGLE);
        cornerBackground.setCornerRadius(dimen(R.attr.smallIconButtonSize) / 2f);

        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingWidgetSelect)) {
            int icon = typedArray.getResourceId(R.styleable.SettingWidgetSelect_icon, 0);
            binding.icon.setImageResource(icon);

            String title = typedArray.getString(R.styleable.SettingWidgetSelect_title);
            binding.title.setText(title);

            description = typedArray.getString(R.styleable.SettingWidgetSelect_description);
            binding.description.setText(description);
            binding.description.setVisibility(description == null || description.isEmpty() ? GONE : VISIBLE);

            int options = typedArray.getResourceId(R.styleable.SettingWidgetSelect_options, 0);
            for (String option : getResources().getStringArray(options)) {
                WidgetSettingSelectButtonHorizontalBinding buttonBinding = WidgetSettingSelectButtonHorizontalBinding.inflate(LayoutInflater.from(context), binding.group, true);
                buttonBinding.getRoot().setText(option);
                buttonBinding.getRoot().setId(generateViewId());
            }
            binding.group.check(binding.group.getChildAt(0).getId());

            collapsible = typedArray.getBoolean(R.styleable.SettingWidgetSelect_collapsible, false);

            String corner = typedArray.getString(R.styleable.SettingWidgetSelect_cornerText);
            setCornerText(corner);
        }

        // 可折叠时整行点击切换展开，右侧箭头提示方向
        if (collapsible) {
            binding.arrow.setVisibility(VISIBLE);
            binding.getRoot().setOnClickListener(v -> setExpanded(!expanded, true));
            // 收起态要显示当前档位文案
            binding.group.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                selectedIndex = group.indexOfChild(group.findViewById(checkedId));
                refreshDescription();
            });
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 构造期就隐藏按钮组的话，之后再展开只会画出背景、画不出文字；等首次测量布局完成、正式绘制前再折叠
        if (collapsible && !collapsedOnce) {
            collapsedOnce = true;
            getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    setExpanded(false, false);
                    return true;
                }
            });
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        binding.getRoot().setOnClickListener(listener);
    }

    // 行尾的小按钮，用来接走原本挂在整行上的动作
    public void setCornerText(String text) {
        binding.corner.setVisibility(text == null || text.isEmpty() ? GONE : VISIBLE);
        binding.corner.setText(text);
        if (text == null || text.isEmpty()) return;
        // 底淡字主色：只需少数人用的动作，只弱化底色，文字仍用前景色保对比
        int accent = MaterialColors.getColor(binding.corner, androidx.appcompat.R.attr.colorPrimary);
        cornerBackground.setColor(ColorUtils.setAlphaComponent(accent, Math.round(Color.alpha(accent) * 0.16f)));
        binding.corner.setBackground(cornerBackground);
        binding.corner.setTextColor(accent);
    }

    public void setOnCornerClickListener(OnClickListener listener) {
        binding.corner.setOnClickListener(listener);
    }

    public void setOnButtonCheckedListener(MaterialButtonToggleGroup.OnButtonCheckedListener listener) {
        binding.group.addOnButtonCheckedListener(listener);
    }

    public void check(int id) {
        binding.group.check(id);
    }

    public void checkIndex(int index) {
        View child = binding.group.getChildAt(index);
        if (child == null) return;
        check(child.getId());
    }

    // 展开态显示说明，收起态显示当前档位；展开时按钮组自上而下划出
    private void setExpanded(boolean value, boolean animate) {
        if (!collapsible) return;
        expanded = value;
        binding.arrow.setImageResource(value ? R.drawable.icon_keyboard_arrow_up : R.drawable.icon_keyboard_arrow_down);
        if (value) {
            // 说明文字先换成展开态，再让按钮组划出
            refreshDescription();
            reveal.apply(true, animate);
        } else {
            // 收起动画结束后再换回当前档位，文字不跟着缩
            reveal.apply(false, animate, this::refreshDescription);
        }
    }

    // 取主题里定义的尺寸，避免在代码里写死 dp
    private int dimen(int attr) {
        TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(attr, value, true)) return 0;
        return TypedValue.complexToDimensionPixelSize(value.data, getResources().getDisplayMetrics());
    }

    private void refreshDescription() {
        if (!collapsible) return;
        if (expanded) {
            binding.description.setText(description);
            binding.description.setTextColor(descriptionColor);
            binding.description.setVisibility(description == null || description.isEmpty() ? GONE : VISIBLE);
            return;
        }

        CharSequence current = description;
        View child = binding.group.getChildAt(selectedIndex);
        if (child instanceof MaterialButton) {
            current = ((MaterialButton) child).getText();
        }
        binding.description.setText(current);
        binding.description.setTextColor(MaterialColors.getColor(binding.description, androidx.appcompat.R.attr.colorPrimary));
        binding.description.setVisibility(current == null || current.length() == 0 ? GONE : VISIBLE);
    }
}
