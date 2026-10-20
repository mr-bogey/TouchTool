package top.bogey.touch_tool.utils.float_window_manager;

import android.accessibilityservice.AccessibilityService;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.view.ContextThemeWrapper;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.EAnchor;

public class FloatWindowHelper {
    private final Context context;
    final FloatConfig config;

    WindowManager manager;
    WindowManager.LayoutParams params;

    public FloatViewParent viewParent;
    private Boolean isPortrait = null;

    FloatWindowHelper(Context context, FloatConfig config) {
        this.context = context;
        this.config = config;
    }

    void createFloatWindow() {
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context instanceof AccessibilityService)
                params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
            else params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.START | Gravity.TOP;
        params.flags = FloatWindow.NOT_FOCUSABLE | config.flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        params.width = config.width;
        params.height = config.height;

        viewParent = new FloatViewParent(new ContextThemeWrapper(context, R.style.Theme_TouchTool), this);
        viewParent.setVisibility(View.INVISIBLE);
        View floatView = config.layoutView;
        if (floatView == null) {
            floatView = LayoutInflater.from(viewParent.getContext()).inflate(config.layoutId, viewParent, true);
            config.layoutView = floatView;
        }
        viewParent.addView(floatView);

        try {
            manager.addView(viewParent, params);
        } catch (android.view.WindowManager.BadTokenException e) {
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    manager.addView(viewParent, params);
                } catch (Exception ignored) {
                }
            }, 200);
        }
    }

    void onLayoutCreated() {
        setRelativePoint(config.anchor, config.gravity, config.location);
        isPortrait = DisplayUtil.isPortrait(context);

        if (config.callback != null) config.callback.onCreate();
        showFloatWindow();

        viewParent.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            boolean portrait = DisplayUtil.isPortrait(context);
            if (isPortrait == null || portrait == isPortrait) return;
            isPortrait = portrait;
            DisplayUtil.resetStatusBarHeight();
            setRelativePoint(config.anchor, config.gravity, config.location);
            if (config.callback != null) config.callback.onRotate();
        });

        if (config.hideByScreenshot) {
            viewParent.post(() -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Object viewRootImpl = HiddenApiBypass.invoke(View.class, viewParent, "getViewRootImpl");
                        if (viewRootImpl != null) {
                            Object surfaceControl = HiddenApiBypass.invoke(viewRootImpl.getClass(), viewRootImpl, "getSurfaceControl");
                            if (surfaceControl != null) {
                                List<Field> fields = HiddenApiBypass.getInstanceFields(viewRootImpl.getClass());
                                Optional<Field> first = fields.stream().filter(field -> field.getName().equals("mTransaction")).findFirst();
                                if (first.isPresent()) {
                                    Field transactionField = first.get();
                                    transactionField.setAccessible(true);
                                    Object transaction = transactionField.get(viewRootImpl);
                                    if (transaction != null) {
                                        HiddenApiBypass.invoke(transaction.getClass(), transaction, "setSkipScreenshot", surfaceControl, true);
                                        manager.updateViewLayout(viewParent, params);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    AppLog.e(e);
                }
            });
        }

        if (config.existEditText) initEditText(viewParent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Rect rect = new Rect(0, 0, viewParent.getWidth(), viewParent.getHeight());
            viewParent.setSystemGestureExclusionRects(List.of(rect));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    void initEditText(View view) {
        if (view instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                initEditText(viewGroup.getChildAt(i));
            }
        } else if (view instanceof EditText editText) {
            editText.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    params.flags = FloatWindow.FOCUSABLE | config.flag;
                    manager.updateViewLayout(viewParent, params);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) inputMethodManager.showSoftInput(editText, 0);
                    }, 100);
                }
                return false;
            });
        }
    }

    void showFloatWindow() {
        if (config.animator != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Animator enter = config.animator.enter(viewParent, config.side);
                if (enter != null) {
                    enter.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            config.animating = false;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            config.animating = false;
                            viewParent.toDockSide();
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            config.animating = true;
                            viewParent.setVisibility(View.VISIBLE);
                            if (config.callback != null) config.callback.onShow(config.tag);
                        }
                    });
                    enter.start();
                } else {
                    viewParent.setVisibility(View.VISIBLE);
                    viewParent.toDockSide();
                    if (config.callback != null) config.callback.onShow(config.tag);
                }
            }, 50);
        } else {
            viewParent.setVisibility(View.VISIBLE);
            viewParent.toDockSide();
            if (config.callback != null) config.callback.onShow(config.tag);
        }
    }

    void dismissFloatWindow() {
        if (config.animator != null) {
            Animator exit = config.animator.exit(viewParent, config.side);
            if (exit != null) {
                exit.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        config.animating = false;
                        removeViewFromWindow(viewParent);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        config.animating = false;
                        removeViewFromWindow(viewParent);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        config.animating = true;
                    }
                });
                exit.start();
            } else {
                removeViewFromWindow(viewParent);
            }
        } else {
            removeViewFromWindow(viewParent);
        }
    }

    private void removeViewFromWindow(View view) {
        try {
            manager.removeView(view);
        } catch (Exception ignored) {
        }
    }

    // 设置窗口位置
    void setRelativePoint(EAnchor anchor, EAnchor gravity, Point relativePoint) {
        Rect showArea = getShowArea();
        showArea.sort();
        config.anchor = anchor;
        config.gravity = gravity;
        config.location = relativePoint;
        Point gravityPoint = getGravityPoint();
        Point offset = getAnchorOffset();
        params.x = Math.clamp(gravityPoint.x + relativePoint.x + offset.x, showArea.left, showArea.right);
        params.y = Math.clamp(gravityPoint.y + relativePoint.y + offset.y, showArea.top, showArea.bottom);
        manager.updateViewLayout(viewParent, params);
    }

    // 获取相对位置
    public Point getRelativePoint() {
        Point point = new Point(params.x, params.y);
        Point garvityPoint = getGravityPoint();
        point.offset(-garvityPoint.x, -garvityPoint.y);
        Point offset = getAnchorOffset();
        point.offset(-offset.x, -offset.y);
        return point;
    }

    Point getAnchorOffset() {
        int width = viewParent.getWidth();
        int height = viewParent.getHeight();
        return config.anchor.getAnchorOffset(width, height);
    }

    Point getGravityPoint() {
        return config.gravity.getAnchorPoint();
    }

    Rect getShowArea() {
        Size size = DisplayUtil.getScreenSize(context);
        int statusBar = DisplayUtil.getStatusBarHeight(viewParent, params);

        Rect showArea = new Rect(0, 0, size.getWidth(), size.getHeight() - statusBar);
        showArea.left += config.border.left;
        showArea.top += config.border.top;
        showArea.right -= config.border.right;
        showArea.bottom -= config.border.bottom;

        showArea.right -= viewParent.getWidth();
        showArea.bottom -= viewParent.getHeight();

        return showArea;
    }

    public void setBorder(Rect border) {
        Point point = getRelativePoint();
        config.border = border;
        setRelativePoint(config.anchor, config.gravity, point);
    }

    public void setFocusable(boolean focusable, boolean withIM) {
        if (focusable) {
            params.flags = (withIM ? FloatWindow.FOCUSABLE : FloatWindow.FOCUSABLE_NOT_IM) | config.flag;
        } else {
            params.flags = FloatWindow.NOT_FOCUSABLE | config.flag;
        }
        manager.updateViewLayout(viewParent, params);
    }
}
