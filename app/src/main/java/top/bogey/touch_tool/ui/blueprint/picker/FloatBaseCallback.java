package top.bogey.touch_tool.ui.blueprint.picker;

import android.app.ActivityManager;
import android.content.Context;
import android.view.MotionEvent;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.ui.MainActivity;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.float_window_manager.FloatCallback;
import top.bogey.touch_tool.utils.float_window_manager.FloatWindow;

public class FloatBaseCallback implements FloatCallback {
    private static final String TAG = FloatBaseCallback.class.getName();
    public static boolean Block = false;


    @Override
    public void onCreate() {
        AppLog.d(TAG, "onCreate: ");
    }

    @Override
    public void onShow(String tag) {
        AppLog.d(TAG, "onShow: " + tag);
        FloatWindow.hideAll(tag);

        MainActivity activity = MainApplication.getInstance().getActivity();
        if (activity != null) {
            if (Block || (SettingSaver.FREE_FORM_OPTIMIZE.get() && DisplayUtil.isInFreeFormMode(activity))) return;
            activity.moveTaskToBack(true);
        }
    }

    @Override
    public void onHide() {
        AppLog.d(TAG, "onHide: ");
    }

    @Override
    public void onDismiss() {
        AppLog.d(TAG, "onDismiss: ");
        if (!FloatWindow.showLast()) {
            MainActivity activity = MainApplication.getInstance().getActivity();
            if (activity != null) {
                if (Block || (SettingSaver.FREE_FORM_OPTIMIZE.get() && DisplayUtil.isInFreeFormMode(activity))) return;
                ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                try {
                    manager.moveTaskToFront(activity.getTaskId(), 0);
                } catch (Exception e) {
                    AppLog.e(e);
                }
            }
        }
    }

    @Override
    public void onDrag() {
        AppLog.d(TAG, "onDrag: ");
    }

    @Override
    public void onDragEnd() {
        AppLog.d(TAG, "onDragEnd: ");
    }

    @Override
    public void onRotate() {
        AppLog.d(TAG, "onRotate: ");
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        return false;
    }
}
