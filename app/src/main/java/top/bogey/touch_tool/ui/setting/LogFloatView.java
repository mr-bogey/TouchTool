package top.bogey.touch_tool.ui.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import top.bogey.touch_tool.MainAccessibilityService;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.databinding.FloatLogBinding;
import top.bogey.touch_tool.utils.DisplayUtils;
import top.bogey.touch_tool.utils.easy_float.EasyFloat;
import top.bogey.touch_tool.utils.easy_float.FloatGravity;
import top.bogey.touch_tool.utils.easy_float.FloatViewHelper;
import top.bogey.touch_tool.utils.easy_float.FloatViewInterface;

public class LogFloatView extends FrameLayout implements FloatViewInterface {
    private static final String LOG_LEVEL = "log_level";

    private final FloatLogBinding binding;

    private float lastY = 0f;
    private boolean isToBottom = false;
    private boolean isToTop = true;

    private boolean isExpand = true;

    private boolean isZoom = false;

    private int level = (1 << LogLevel.values().length) - 1;

    @SuppressLint("ClickableViewAccessibility")
    public LogFloatView(@NonNull Context context) {
        super(context);
        binding = FloatLogBinding.inflate(LayoutInflater.from(context), this, true);

        SharedPreferences preferences = context.getSharedPreferences(MainAccessibilityService.SAVE_PATH, Context.MODE_PRIVATE);
        level = preferences.getInt(LOG_LEVEL, level);

        LogRecyclerViewAdapter adapter = new LogRecyclerViewAdapter(level);
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setOnTouchListener((v, event) -> {
            ViewParent parent = getParent();
            if (parent != null){
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastY = event.getY();
                        parent.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        checkPosition(event.getY());
                        if (isToBottom || isToTop){
                            parent.requestDisallowInterceptTouchEvent(false);
                            return false;
                        } else {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        lastY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        parent.requestDisallowInterceptTouchEvent(false);
                        break;
                }
            }
            return false;
        });

        binding.closeButton.setOnClickListener(v -> dismiss());

        binding.expandButton.setOnClickListener(v -> {
            isExpand = !isExpand;
            refreshUI();
        });

        binding.zoomButton.setOnClickListener(v -> {
            isZoom = !isZoom;
            refreshUI();
        });

        MaterialButton[] buttons = {binding.lowButton, binding.middleButton, binding.highButton};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setAlpha((level & (1 << i)) > 0 ? 1f : 0.25f);
            buttons[i].setBackgroundTintList(ColorStateList.valueOf(LogLevel.values()[i].getLevelColor(context)));
        }

        binding.highButton.setOnClickListener(v -> {
            SharedPreferences.Editor edit = preferences.edit();
            int value = 1 << LogLevel.HIGH.ordinal();
            level = level ^ value;
            binding.highButton.setAlpha((level & value) > 0 ? 1f : 0.25f);
            edit.putInt(LOG_LEVEL, level);
            edit.apply();
            adapter.setLevel(level);
        });

        binding.middleButton.setOnClickListener(v -> {
            SharedPreferences.Editor edit = preferences.edit();
            int value = 1 << LogLevel.MIDDLE.ordinal();
            level = level ^ value;
            binding.middleButton.setAlpha((level & value) > 0 ? 1f : 0.25f);
            edit.putInt(LOG_LEVEL, level);
            edit.apply();
            adapter.setLevel(level);
        });

        binding.lowButton.setOnClickListener(v -> {
            SharedPreferences.Editor edit = preferences.edit();
            int value = 1 << LogLevel.LOW.ordinal();
            level = level ^ value;
            binding.lowButton.setAlpha((level & value) > 0 ? 1f : 0.25f);
            edit.putInt(LOG_LEVEL, level);
            edit.apply();
            adapter.setLevel(level);
        });
    }

    @Override
    public void show() {
        EasyFloat.with(getContext())
                .setLayout(this)
                .setTag(LogFloatView.class.getCanonicalName())
                .setDragEnable(true)
                .setGravity(FloatGravity.CENTER, 0, 0)
                .hasEditText(true)
                .setAnimator(null)
                .setAlwaysShow(true)
                .show();
    }

    @Override
    public void dismiss() {
        EasyFloat.dismiss(LogFloatView.class.getCanonicalName());
    }

    private void checkPosition(float nowY){
        LinearLayoutManager layoutManager = (LinearLayoutManager)binding.recyclerView.getLayoutManager();
        if (layoutManager != null){
            if (layoutManager.getItemCount() > 3){
                isToTop = false;
                isToBottom =false;
                int first = layoutManager.findFirstCompletelyVisibleItemPosition();
                int last = layoutManager.findLastCompletelyVisibleItemPosition();

                if (layoutManager.getChildCount() > 0){
                    if (last == layoutManager.getItemCount() - 1){
                        if (canScrollVertically(-1) && nowY < lastY){
                            isToBottom = true;
                        }
                    } else if (first == 0){
                        if (canScrollVertically(1) && nowY > lastY){
                            isToTop = true;
                        }
                    }
                }
            } else {
                isToTop = true;
                isToBottom = true;
            }
        }
    }

    private void refreshUI(){
        FloatViewHelper helper = EasyFloat.getHelper(LogFloatView.class.getCanonicalName());

        Point size = DisplayUtils.getScreenSize(getContext());
        int height = DisplayUtils.getStatusBarHeight(getContext());

        int bgWidth = DisplayUtils.dp2px(getContext(), isExpand ? (isZoom ? 320 : 240) : 32);
        int bgHeight = DisplayUtils.dp2px(getContext(), isExpand ? (isZoom ? 640 : 240) : 30);
        bgHeight = Math.min(bgHeight, size.y - height);

        MaterialCardView root = binding.getRoot();
        ViewGroup.LayoutParams rootLayoutParams = root.getLayoutParams();

        if (isExpand){
            binding.markBox.setVisibility(VISIBLE);
            binding.drag.setVisibility(VISIBLE);
            binding.zoomButton.setVisibility(VISIBLE);
            binding.closeButton.setVisibility(VISIBLE);
            binding.highButton.setVisibility(VISIBLE);
            binding.middleButton.setVisibility(VISIBLE);
            binding.lowButton.setVisibility(VISIBLE);
            binding.expandButton.setIconResource(R.drawable.icon_remove);
        } else {
            binding.markBox.setVisibility(GONE);
            binding.drag.setVisibility(GONE);
            binding.zoomButton.setVisibility(GONE);
            binding.closeButton.setVisibility(GONE);
            binding.highButton.setVisibility(GONE);
            binding.middleButton.setVisibility(GONE);
            binding.lowButton.setVisibility(GONE);
            binding.expandButton.setIconResource(R.drawable.icon_add);
        }

        binding.zoomButton.setIconResource(isZoom ? R.drawable.icon_zoom_in : R.drawable.icon_zoom_out);

        rootLayoutParams.width = bgWidth;
        rootLayoutParams.height = bgHeight;
        root.setLayoutParams(rootLayoutParams);
        helper.params.width = bgWidth;
        helper.params.height = bgHeight;
        helper.manager.updateViewLayout(helper.floatViewParent, helper.params);
        postDelayed(helper::initGravity, 50);
    }
}
