package top.bogey.touch_tool.service.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.service.MainAccessibilityService;
import top.bogey.touch_tool.service.TaskInfoSummary;
import top.bogey.touch_tool.ui.InstantActivity;
import top.bogey.touch_tool.ui.tool.task_manager.ExportTaskDialog;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;

public class SystemEventReceiver extends BroadcastReceiver {
    private final Context context;
    private ConnectivityManager.NetworkCallback networkCallback;

    public SystemEventReceiver(Context context) {
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case Intent.ACTION_BATTERY_CHANGED -> {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int state = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                TaskInfoSummary.getInstance().setBatteryInfo(level * 100 / scale, TaskInfoSummary.BatteryState.values()[state - 1]);
            }

            case Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF, Intent.ACTION_USER_PRESENT -> TaskInfoSummary.getInstance().onPhoneStateChanged();

            case BluetoothDevice.ACTION_ACL_CONNECTED, BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) return;
                TaskInfoSummary.getInstance().addBluetoothInfo(device.getAddress(), device.getName(), action.equals(BluetoothDevice.ACTION_ACL_CONNECTED));
            }

            case InstantActivity.INTENT_KEY_DO_ACTION -> {
                String taskId = intent.getStringExtra(InstantActivity.TASK_ID);
                String actionId = intent.getStringExtra(InstantActivity.ACTION_ID);
                String pinId = intent.getStringExtra(InstantActivity.PIN_ID);
                InstantActivity.doAction(taskId, actionId, pinId, null);
            }

            case MainAccessibilityService.INTENT_KEY_DELETE_URI -> {
                Uri uri = intent.getParcelableExtra(MainAccessibilityService.URI);
                AppLog.d("系统", "onReceive: " + uri);
                if (uri == null) return;
                context.getContentResolver().delete(uri, null, null);
            }

            case MainAccessibilityService.INTENT_KEY_AUTO_BACKUP -> {
                MainAccessibilityService service = MainApplication.getInstance().getService();
                if (service != null && service.isEnabled()) {
                    service.addAlarm();
                }

                int times = SettingSaver.TASK_AUTO_BACKUP_TIMES.get();
                times++;

                int index = SettingSaver.TASK_AUTO_BACKUP.get();
                int[] timesArray = new int[]{0, 1, 3, 7};
                if (timesArray[index] == 0) return;

                if (times < timesArray[index]) {
                    SettingSaver.TASK_AUTO_BACKUP_TIMES.set(times);
                    return;
                }
                SettingSaver.TASK_AUTO_BACKUP_TIMES.set(0);

                ExportTaskDialog.autoBackup(context);
            }
        }
    }

    private IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        // 电量变动
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        // 屏幕与锁屏状态变更
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 蓝牙连接或断开
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        // 动作执行
        filter.addAction(InstantActivity.INTENT_KEY_DO_ACTION);
        filter.addAction(MainAccessibilityService.INTENT_KEY_DELETE_URI);
        filter.addAction(MainAccessibilityService.INTENT_KEY_AUTO_BACKUP);

        return filter;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void register() {
        ContextCompat.registerReceiver(context, this, getFilter(), ContextCompat.RECEIVER_EXPORTED);
        registerNetworkReceiver();
    }

    public void unregister() {
        context.unregisterReceiver(this);
        unregisterNetworkReceiver();
    }

    private void registerNetworkReceiver() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null && networkCallback == null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    TaskInfoSummary.getInstance().setNetworkState(Collections.singletonList(TaskInfoSummary.NotworkState.NONE));
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    List<TaskInfoSummary.NotworkState> state = new ArrayList<>();
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        state.add(TaskInfoSummary.NotworkState.WIFI);
                        TaskInfoSummary.getInstance().setWifiInfo(AppUtil.getWifiInfo(context));
                    } else {
                        TaskInfoSummary.getInstance().setWifiInfo(null);
                    }
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        state.add(TaskInfoSummary.NotworkState.MOBILE);
                    }
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        state.add(TaskInfoSummary.NotworkState.VPN);
                    }
                    TaskInfoSummary.getInstance().setNetworkState(state);
                }
            };

            manager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private void unregisterNetworkReceiver() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null && networkCallback != null) {
            manager.unregisterNetworkCallback(networkCallback);
        }
    }
}
