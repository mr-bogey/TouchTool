package top.bogey.touch_tool.bean.save.setting.special;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

import top.bogey.touch_tool.bean.save.setting.SettingSave;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.ui.custom.float_view.KeepAliveFloatView;
import top.bogey.touch_tool.utils.callback.ActivityLifecycleCallback;
import top.bogey.touch_tool.utils.float_window_manager.FloatWindow;

public class DynamicColor extends SettingSave<Boolean> {

    public DynamicColor(String key, Boolean defaultValue) {
        super(key, defaultValue);
    }

    public void set(Activity activity, Boolean value) {
        super.set(value);
        activity.recreate();
    }

    public void handle(Application  application) {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallback() {
            @Override
            public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                super.onActivityPreCreated(activity, savedInstanceState);
                // 系统调色板颜色是写死在主题资源里的，material 的可用性判定却带厂商白名单，非白名单 ROM 上会直接跳过，这里按资源再套一次
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && get()) {
                    activity.getTheme().applyStyle(com.google.android.material.R.style.ThemeOverlay_Material3Expressive_DynamicColors_DayNight, true);
                }
                DynamicColors.applyToActivityIfAvailable(activity, getDynamicColorOptions());
            }
        });
    }

    private DynamicColorsOptions getDynamicColorOptions() {
        DynamicColorsOptions.Builder builder = new DynamicColorsOptions.Builder();
        builder.setPrecondition((activity, theme) -> get());
        builder.setOnAppliedCallback(activity -> {
            boolean darkMode = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            KeepAliveFloatView keepView = (KeepAliveFloatView) FloatWindow.getView(KeepAliveFloatView.class.getName());
            if (keepView != null) {
                if (keepView.isDarkMode() == darkMode) return;
            }
            FloatWindow.dismiss(KeepAliveFloatView.class.getName());
        });
        int colorValue = SettingSaver.DYNAMIC_COLOR_OPTIONS.get();
        if (colorValue != Color.BLACK) builder.setContentBasedSource(colorValue);
        return builder.build();
    }
}
