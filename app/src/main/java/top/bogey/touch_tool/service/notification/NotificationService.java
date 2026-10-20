package top.bogey.touch_tool.service.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.service.TaskInfoSummary;
import top.bogey.touch_tool.utils.AppLog;

public class NotificationService extends NotificationListenerService {
    private static boolean isEnabled = false;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (SettingSaver.PERMISSION_NOTIFICATION.get() != 1) return;
        String packageName = sbn.getPackageName();
        Map<String, String> content = new HashMap<>();
        Bundle extras = sbn.getNotification().extras;
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            content.put(key, String.valueOf(value));
        }
        TaskInfoSummary.getInstance().setNotification(TaskInfoSummary.NotificationType.NOTIFICATION, packageName, content);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        AppLog.d("通知", "NotificationService onListenerConnected");
        isEnabled = true;
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        isEnabled = false;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void requestConnect(Context context) {
        if (!isEnabled) {
            context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            Toast.makeText(context, R.string.permission_setting_notification_type_tips, Toast.LENGTH_SHORT).show();
        }
    }
}
