package top.bogey.auto_touch.ui.easy_float;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import top.bogey.auto_touch.util.AppUtil;

class FloatTouchUtils {
    private final Context context;
    private final FloatConfig config;

    private Rect showRect = new Rect();

    private float lastX;
    private float lastY;

    FloatTouchUtils(Context context, FloatConfig config) {
        this.context = context;
        this.config = config;
    }

    void updateFloatPosition(View view, MotionEvent event, WindowManager manager, LayoutParams params){
        if (config.callback != null){
            config.callback.onTouch(event);
        }
        if (!config.dragEnable || config.isAnim){
            config.isDrag = false;
            return;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                config.isDrag = false;
                lastX = event.getRawX();
                lastY = event.getRawY();
                initValue();
                break;
            case MotionEvent.ACTION_MOVE:
                // 边界范围外的触摸被忽略
                if (event.getRawX() < config.leftBorder + showRect.left || event.getRawX() > showRect.right - config.rightBorder
                || event.getRawY() < config.topBorder + showRect.top || event.getRawY() > showRect.bottom - config.bottomBorder) return;

                float dx = event.getRawX() - lastX;
                float dy = event.getRawY() - lastY;
                if (!config.isDrag && dx * dx + dy * dy < 81) return;
                config.isDrag = true;

                // 限制界面x，y轴的位置
                int x = params.x + (int) dx;
                if (x < config.leftBorder) x = showRect.left + config.leftBorder;
                else if (x > showRect.right - config.rightBorder) x = showRect.right - config.rightBorder - view.getWidth();

                int y = params.y + (int) dy;
                if (y < config.topBorder + showRect.top) y = showRect.top + config.topBorder;
                else if (y > showRect.bottom - config.bottomBorder) y = showRect.bottom - config.bottomBorder - view.getHeight();

                params.x = x;
                params.y = y;
                manager.updateViewLayout(view, params);
                if (config.callback != null){
                    config.callback.onDrag(event);
                }
                lastX = event.getRawX();
                lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!config.isDrag) return;
                config.isDrag = false;
                if (config.callback != null){
                    config.callback.onDrag(event);
                }
                if (config.side == SidePattern.DEFAULT){
                    dragEnd();
                } else {
                    sideAnim(view, manager, params);
                }
        }
    }

    void updateFloatPosition(View view, WindowManager manager, LayoutParams params){
        initValue();
        sideAnim(view, manager, params);
    }

    private void initValue(){
        showRect = AppUtil.getShowArea(context);
    }

    private void sideAnim(View view, WindowManager manager, LayoutParams params){
        int leftDistance = params.x - config.leftBorder - showRect.left;
        int rightDistance = showRect.right - config.rightBorder - params.x - view.getWidth();
        int topDistance = params.y - config.topBorder - showRect.top;
        int bottomDistance = showRect.bottom - config.bottomBorder - params.y - view.getHeight();

        boolean isX = true;
        int end = 0;
        switch (config.side){
            case LEFT:
                end = config.leftBorder + showRect.left;
                break;
            case RIGHT:
                end = showRect.right - config.rightBorder - view.getWidth();
                break;
            case TOP:
                isX = false;
                end = config.topBorder + showRect.top;
                break;
            case BOTTOM:
                isX = false;
                end = showRect.bottom - config.bottomBorder - view.getHeight();
                break;
            case HORIZONTAL:
                end = leftDistance < rightDistance ? config.leftBorder + showRect.left : showRect.right - config.rightBorder - view.getWidth();
                break;
            case VERTICAL:
                isX = false;
                end = topDistance < bottomDistance ? config.topBorder + showRect.top : showRect.bottom - config.bottomBorder - view.getHeight();
                break;
            case SIDE:
                int minX = Math.min(leftDistance, rightDistance);
                int minY = Math.min(topDistance, bottomDistance);
                if (minX > minY){
                    isX = false;
                    end = topDistance < bottomDistance ? config.topBorder + showRect.top : showRect.bottom - config.bottomBorder - view.getHeight();
                } else {
                    end = leftDistance < rightDistance ? config.leftBorder + showRect.left : showRect.right - config.rightBorder - view.getWidth();
                }
                break;
        }
        ValueAnimator animator = ValueAnimator.ofInt(isX ? params.x : params.y, end);
        boolean finalIsX = isX;
        animator.addUpdateListener(animation -> {
            try{
                if (finalIsX) params.x = (int) animation.getAnimatedValue(); else params.y = (int) animation.getAnimatedValue();
                manager.updateViewLayout(view, params);
            }catch (Exception ignored){
                animation.cancel();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                config.isAnim = false;
                dragEnd();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                config.isAnim = false;
                dragEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                config.isAnim = true;
            }
        });
        animator.start();
    }

    private void dragEnd(){
        if (config.callback != null){
            config.callback.onDragEnd();
        }
    }
}
