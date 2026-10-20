package top.bogey.touch_tool.ui.setting.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

// 可折叠行的展开动画：容器高度与上外边距按动画值写入，超出的内容被外层裁掉，视觉上自上而下划出
public class SettingRevealAnimator {
    private static final long DURATION = 200L;

    private final View view;
    private final int marginTop;
    private ValueAnimator animator;

    public SettingRevealAnimator(View view) {
        this.view = view;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        marginTop = params instanceof ViewGroup.MarginLayoutParams ? ((ViewGroup.MarginLayoutParams) params).topMargin : 0;
    }

    public void apply(boolean expand, boolean animate) {
        apply(expand, animate, null);
    }

    // 展开：0 → 实测高度；收起：实测高度 → 0；animate 为 false 时直接落到终态（首次折叠）
    public void apply(boolean expand, boolean animate, Runnable endAction) {
        cancel();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) return;

        if (!animate) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) params).topMargin = marginTop;
            }
            view.setVisibility(expand ? View.VISIBLE : View.GONE);
            view.requestLayout();
            if (endAction != null) endAction.run();
            return;
        }

        // 目标高度必须在把高度改成定值之前测出来
        int target = measureHeight();
        if (target <= 0) {
            // 还没完成布局，测不出高度就直接切换，别留下半截状态
            apply(expand, false, endAction);
            return;
        }

        // 从当前高度接着动，连点时不会跳回起点
        float start = 0f;
        if (view.getVisibility() == View.VISIBLE) {
            start = params.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 1f : Math.min(1f, (float) params.height / target);
        }
        // 先摆到起始高度再变可见，避免露出完整高度的一帧
        writeHeight(Math.round(target * start), Math.round(marginTop * start));
        view.setVisibility(View.VISIBLE);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, expand ? 1f : 0f);
        valueAnimator.setDuration(DURATION);
        valueAnimator.setInterpolator(expand ? new DecelerateInterpolator() : new AccelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            writeHeight(Math.round(target * fraction), Math.round(marginTop * fraction));
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean canceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                canceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (canceled) return;
                ViewGroup.LayoutParams finalParams = view.getLayoutParams();
                finalParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (finalParams instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) finalParams).topMargin = marginTop;
                }
                view.setVisibility(expand ? View.VISIBLE : View.GONE);
                view.requestLayout();
                if (endAction != null) endAction.run();
            }
        });
        animator = valueAnimator;
        valueAnimator.start();
    }

    private void cancel() {
        if (animator == null) return;
        animator.cancel();
        animator = null;
    }

    private void writeHeight(int height, int topMargin) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) params).topMargin = topMargin;
        }
        view.requestLayout();
    }

    // 用未指定高度测一次自然高度；宽度取容器宽度，避免按钮组按错误宽度换行
    private int measureHeight() {
        int width = view.getWidth();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (width <= 0 && view.getParent() instanceof View) {
            width = ((View) view.getParent()).getWidth();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                width -= marginParams.leftMargin + marginParams.rightMargin;
            }
        }
        if (width <= 0) return 0;
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return view.getMeasuredHeight();
    }
}
