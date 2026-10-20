package top.bogey.touch_tool.service;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.Action;
import top.bogey.touch_tool.bean.action.start.ApplicationQuitStartAction;
import top.bogey.touch_tool.bean.action.start.ApplicationStartAction;
import top.bogey.touch_tool.bean.action.start.BatteryStartAction;
import top.bogey.touch_tool.bean.action.start.BluetoothStartAction;
import top.bogey.touch_tool.bean.action.start.BroadcastStartAction;
import top.bogey.touch_tool.bean.action.start.ManualStartAction;
import top.bogey.touch_tool.bean.action.start.NetworkStartAction;
import top.bogey.touch_tool.bean.action.start.NotificationStartAction;
import top.bogey.touch_tool.bean.action.start.ReceivedShareStartAction;
import top.bogey.touch_tool.bean.action.start.ScreenStartAction;
import top.bogey.touch_tool.bean.action.start.StartAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.bean.save.task.TaskSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.ui.MainActivity;
import top.bogey.touch_tool.ui.custom.float_view.ChoiceExecuteFloatView;
import top.bogey.touch_tool.ui.play.PlayFloatView;
import top.bogey.touch_tool.ui.play.SinglePlayView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.callback.ResultCallback;

public class TaskInfoSummary {
    private static final String XMLNS_ANDROID = "http://schemas.android.com/apk/res/android";

    private static TaskInfoSummary instance;

    public static TaskInfoSummary getInstance() {
        synchronized (TaskInfoSummary.class) {
            if (instance == null) {
                instance = new TaskInfoSummary();
            }
        }
        return instance;
    }

    private final Map<String, PackageInfo> apps = new ConcurrentHashMap<>();
    private final List<String> launcherApps = new ArrayList<>();
    // 桌面应用的主界面类名缓存
    private final Map<String, String> launcherActivities = new HashMap<>();
    private PackageActivity packageActivity = new PackageActivity("", "");
    private PackageActivity lastPackageActivity = new PackageActivity("", "");
    private final List<ResultCallback<PackageActivity>> packageActivityListeners = new ArrayList<>();

    private final Map<String, Notification> notifications = new ConcurrentHashMap<>();
    private BatteryInfo batteryInfo;
    private final List<BluetoothInfo> bluetoothInfoList = new ArrayList<>();
    private BluetoothInfo lastBluetoothInfo;
    private WifiInfo wifiInfo;
    private BroadcastInfo broadcastInfo;
    private String lastClipboard;
    private List<NotworkState> networkState;

    public void resetApps() {
        apps.clear();
        PackageManager packageManager = MainApplication.getInstance().getPackageManager();
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES | PackageManager.MATCH_DISABLED_COMPONENTS | PackageManager.MATCH_ALL);
        for (ApplicationInfo application : applications) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(application.packageName, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) apps.put(packageInfo.packageName, packageInfo);
            } catch (Exception ignored) {
            }
        }

        launcherApps.clear();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo resolveInfo : resolveInfos) {
            launcherApps.add(resolveInfo.activityInfo.packageName);
        }
        launcherApps.sort(String::compareTo);

        AppLog.d("前台", "resetApps: " + apps.size());
    }

    public PackageInfo getAppInfo(String packageName) {
        return apps.get(packageName);
    }

    public ActivityInfo getActivityInfo(String packageName, String activityName) {
        PackageInfo packageInfo = getAppInfo(packageName);
        if (packageInfo == null || packageInfo.activities == null) return null;
        for (ActivityInfo activityInfo : packageInfo.activities) {
            if (activityInfo.name.equals(activityName)) return activityInfo;
        }
        return null;
    }

    public String getAppName(String packageName) {
        PackageInfo packageInfo = getAppInfo(packageName);
        if (packageInfo == null || packageInfo.applicationInfo == null) return null;
        return packageInfo.applicationInfo.loadLabel(MainApplication.getInstance().getPackageManager()).toString();
    }

    public List<PackageInfo> findApps(String keyword, boolean system) {
        List<PackageInfo> packages = new ArrayList<>();

        for (PackageInfo info : apps.values()) {
            if (info.packageName.equals(MainApplication.getInstance().getPackageName())) continue;
            if (info.applicationInfo == null) continue;
            if (system || (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                if (keyword == null || keyword.isEmpty()) {
                    packages.add(info);
                } else {
                    if (AppUtil.isStringContains(info.packageName.toLowerCase(), keyword.toLowerCase())) packages.add(info);
                    else {
                        String appName = info.applicationInfo.loadLabel(MainApplication.getInstance().getPackageManager()).toString();
                        if (AppUtil.isStringContainsWithPinyin(appName.toLowerCase(), keyword.toLowerCase())) packages.add(info);
                    }
                }
            }
        }
        return packages;
    }

    public List<PackageInfo> findSendApps(String keyword, boolean system) {
        List<PackageInfo> packages = new ArrayList<>();

        PackageManager packageManager = MainApplication.getInstance().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        List<ResolveInfo> resolves = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo resolveInfo : resolves) {
            if (resolveInfo.activityInfo.packageName.equals(MainApplication.getInstance().getPackageName())) continue;
            PackageInfo info = apps.get(resolveInfo.activityInfo.packageName);
            if (info == null || info.applicationInfo == null) continue;
            if (system || (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                if (keyword == null || keyword.isEmpty()) {
                    packages.add(info);
                } else {
                    if (AppUtil.isStringContains(info.packageName.toLowerCase(), keyword.toLowerCase())) packages.add(info);
                    else {
                        String appName = info.applicationInfo.loadLabel(MainApplication.getInstance().getPackageManager()).toString();
                        if (AppUtil.isStringContainsWithPinyin(appName.toLowerCase(), keyword.toLowerCase())) packages.add(info);
                    }
                }
            }
        }
        return packages;
    }

    private List<ShortcutInfo> findShortcutInfo() {
        List<ShortcutInfo> shortcuts = new ArrayList<>();

        PackageManager packageManager = MainApplication.getInstance().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_UNINSTALLED_PACKAGES | PackageManager.MATCH_ALL | PackageManager.GET_META_DATA);
        for (ResolveInfo resolveInfo : resolveInfos) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            try (XmlResourceParser parser = activityInfo.loadXmlMetaData(packageManager, "android.app.shortcuts")) {
                if (parser == null) continue;
                int eventType = parser.getEventType();
                Intent shortcutIntent = null;
                ShortcutInfo shortcutInfo = new ShortcutInfo(activityInfo.packageName);

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.END_TAG && "shortcut".equals(parser.getName())) {
                        if (shortcutInfo.isValid()) {
                            shortcuts.add(shortcutInfo);
                        }
                        shortcutInfo = new ShortcutInfo(activityInfo.packageName);
                    } else if (eventType == XmlPullParser.END_TAG && "intent".equals(parser.getName())) {
                        if (shortcutIntent != null) {
                            shortcutInfo.intent = shortcutIntent.toUri(Intent.URI_INTENT_SCHEME);
                            shortcutIntent = null;
                        }
                    } else if (eventType == XmlPullParser.START_TAG) {
                        if ("shortcut".equals(parser.getName()) && parser.getAttributeBooleanValue(XMLNS_ANDROID, "enabled", true)) {
                            shortcutInfo.id = parser.getAttributeValue(XMLNS_ANDROID, "shortcutId");
                            if (shortcutInfo.id == null) shortcutInfo.id = UUID.randomUUID().toString();

                            int titleId = parser.getAttributeResourceValue(XMLNS_ANDROID, "shortcutShortLabel", 0);
                            if (titleId == 0) titleId = parser.getAttributeResourceValue(XMLNS_ANDROID, "shortcutLongLabel", 0);
                            if (titleId != 0) {
                                try {
                                    shortcutInfo.title = packageManager.getResourcesForApplication(activityInfo.packageName).getString(titleId);
                                } catch (PackageManager.NameNotFoundException ignored) {
                                    shortcutInfo.title = shortcutInfo.id;
                                }
                            }
                            shortcutInfo.icon = parser.getAttributeResourceValue(XMLNS_ANDROID, "icon", 0);
                        } else if ("intent".equals(parser.getName()) && shortcutInfo.id != null) {
                            shortcutIntent = new Intent();
                            shortcutIntent.setAction(parser.getAttributeValue(XMLNS_ANDROID, "action"));

                            String pkg = parser.getAttributeValue(XMLNS_ANDROID, "targetPackage");
                            String cls = parser.getAttributeValue(XMLNS_ANDROID, "targetClass");
                            if (cls == null) {
                                cls = parser.getAttributeValue(XMLNS_ANDROID, "targetActivity");
                                if (cls != null) {
                                    cls = cls.replace("$", "_");
                                }
                            }
                            shortcutInfo.activityClass = cls;
                            if (pkg != null && cls != null) {
                                shortcutIntent.setClassName(pkg, cls);
                            }
                            if (shortcutInfo.title == null && cls != null) {
                                shortcutInfo.title = cls;
                            }
                            String data = parser.getAttributeValue(XMLNS_ANDROID, "data");
                            if (data != null) {
                                shortcutIntent.setData(Uri.parse(data));
                            }
                        } else if ("extra".equals(parser.getName()) && shortcutIntent != null) {
                            String name = parser.getAttributeValue(XMLNS_ANDROID, "name");
                            String value = parser.getAttributeValue(XMLNS_ANDROID, "value");
                            if (name != null && value != null) shortcutIntent.putExtra(name, value);
                        } else if ("categories".equals(parser.getName()) && shortcutIntent != null) {
                            String name = parser.getAttributeValue(XMLNS_ANDROID, "name");
                            if (name != null) shortcutIntent.addCategory(name);
                        }
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                AppLog.e(e);
            }
        }
        return shortcuts;
    }

    public List<ShortcutInfo> findShortcutInfo(String packageName, String activityClass) {
        List<ShortcutInfo> shortcuts = new ArrayList<>();
        if (packageName == null) return shortcuts;
        for (ShortcutInfo shortcutApp : findShortcutInfo()) {
            if (shortcutApp.packageName.equals(packageName) && (activityClass == null || activityClass.equals(shortcutApp.activityClass))) {
                shortcuts.add(shortcutApp);
            }
        }
        return shortcuts;
    }

    public List<PackageInfo> findShortcutApps(String keyword, boolean system) {
        List<PackageInfo> shortcutApps = new ArrayList<>();
        for (ShortcutInfo shortcutApp : findShortcutInfo()) {
            PackageInfo info = getAppInfo(shortcutApp.packageName);
            if (info == null || info.applicationInfo == null) continue;
            if (shortcutApps.contains(info)) continue;
            if (system || (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                if (keyword == null || keyword.isEmpty()) {
                    shortcutApps.add(info);
                } else {
                    if (AppUtil.isStringContains(info.packageName.toLowerCase(), keyword.toLowerCase())) shortcutApps.add(info);
                    else {
                        String appName = info.applicationInfo.loadLabel(MainApplication.getInstance().getPackageManager()).toString();
                        if (AppUtil.isStringContainsWithPinyin(appName.toLowerCase(), keyword.toLowerCase())) shortcutApps.add(info);
                    }
                }
            }
        }
        return shortcutApps;
    }

    public boolean isLauncherApp(String packageName) {
        return launcherApps.contains(packageName);
    }

    // 部分桌面切回桌面不发窗口状态变化事件，按激活窗口补一次桌面切换
    public void enterLauncher(String packageName) {
        if (packageName == null || packageName.isEmpty()) return;
        if (!isLauncherApp(packageName)) return;
        if (packageName.equals(packageActivity.packageName)) return;
        String activityName = getLauncherActivity(packageName);
        if (activityName == null) return;
        AppLog.add(AppLog.CATEGORY_ACTIVITY, packageName, "Launcher");
        enterActivity(packageName, activityName);
    }

    // 取桌面应用的主界面类名，取到后缓存
    private String getLauncherActivity(String packageName) {
        String activityName = launcherActivities.get(packageName);
        if (activityName != null) return activityName;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setPackage(packageName);
        ResolveInfo resolveInfo = MainApplication.getInstance().getPackageManager().resolveActivity(intent, PackageManager.MATCH_ALL);
        if (resolveInfo == null || resolveInfo.activityInfo == null) return null;
        activityName = resolveInfo.activityInfo.name;
        launcherActivities.put(packageName, activityName);
        return activityName;
    }

    public void tryStartActions(Class<? extends StartAction> clazz) {
        MainAccessibilityService service = MainApplication.getInstance().getService();
        if (service == null || !service.isEnabled()) return;

        for (Task task : TaskSaver.getInstance().getTasks(clazz)) {
            for (Action action : task.getActions(clazz)) {
                StartAction startAction = (StartAction) action;
                if (startAction.isEnable() && startAction.ready()) service.runTask(task, startAction);
            }
        }
    }

    public void tryStartNotificationActions(Notification notification) {
        MainAccessibilityService service = MainApplication.getInstance().getService();
        if (service == null || !service.isEnabled()) return;

        for (Task task : TaskSaver.getInstance().getTasks(NotificationStartAction.class)) {
            for (Action action : task.getActions(NotificationStartAction.class)) {
                NotificationStartAction startAction = (NotificationStartAction) action;
                if (startAction.isEnable() && startAction.ready(notification)) {
                    notifications.put(startAction.getId(), notification);
                    service.runTask(task, startAction);
                }
            }
        }
    }

    public void tryStartShareTask(PinObject pinObject) {
        MainAccessibilityService service = MainApplication.getInstance().getService();
        if (service == null || !service.isEnabled()) return;

        Map<Action, Task> tasks = new HashMap<>();
        for (Task task : TaskSaver.getInstance().getTasks(ReceivedShareStartAction.class)) {
            for (Action action : task.getActions(ReceivedShareStartAction.class)) {
                ReceivedShareStartAction startAction = (ReceivedShareStartAction) action;
                if (startAction.isEnable() && startAction.ready()) {
                    Pin connectToAblePin = action.getPins().stream().filter(p -> p.isSameClass(pinObject.getClass()) && p.isOut()).findFirst().orElse(null);
                    if (connectToAblePin != null) {
                        Task copy = task.copy();
                        Action actionCopy = copy.getAction(action.getId());
                        Pin pinById = actionCopy.getPinById(connectToAblePin.getId());
                        pinById.setValue(pinObject);
                        tasks.put(actionCopy, copy);
                    }
                }
            }
        }
        if (tasks.isEmpty()) return;
        if (tasks.size() == 1) {
            Action action = tasks.keySet().iterator().next();
            Task task = tasks.get(action);
            service.runTask(task, (StartAction) action);
            return;
        }
        List<ChoiceExecuteFloatView.Choice> choices = new ArrayList<>();
        tasks.forEach((action, task) -> choices.add(new ChoiceExecuteFloatView.Choice(action.getId(), task.getTitle(), null)));
        ChoiceExecuteFloatView.showChoice(service.getString(R.string.execute_task_action), choices, 0, result -> {
            Action action = tasks.keySet().stream().filter(a -> a.getId().equals(result)).findFirst().orElse(null);
            if (action == null) return;
            Task task = tasks.get(action);
            service.runTask(task, (StartAction) action);
        });
    }

    public void tryShowManualPlayView(boolean show) {
        MainAccessibilityService service = MainApplication.getInstance().getService();
        if (service == null || !service.isEnabled()) return;

        List<ManualExecuteInfo> normalList = new ArrayList<>();
        List<ManualExecuteInfo> singleShowList = new ArrayList<>();

        if (show) {
            for (Task task : TaskSaver.getInstance().getOrderTasks(ManualStartAction.class)) {
                for (Action action : task.getActions(ManualStartAction.class)) {
                    ManualStartAction startAction = (ManualStartAction) action;
                    if (startAction.isEnable() && startAction.ready()) {
                        if (startAction.isSingleShow()) {
                            singleShowList.add(new ManualExecuteInfo(task, startAction));
                        } else {
                            normalList.add(new ManualExecuteInfo(task, startAction));
                        }
                    }
                }
            }
        }

        // 手动悬浮窗显示限制
        int playType = SettingSaver.MANUAL_PLAY_VIEW_SHOW_TYPE.get();
        if (playType == 0 || (playType == 1 && getPhoneState() != PhoneState.ON)) {
            normalList.clear();
            singleShowList.clear();
        }

        PlayFloatView.showActions(normalList);
        SinglePlayView.showActions(singleShowList);
    }

    public boolean isActivityClass(String packageName, String activityName) {
        if (packageName == null || activityName == null) return false;
        if (packageName.isEmpty() || activityName.isEmpty()) return false;
        PackageInfo packageInfo = getAppInfo(packageName);
        if (packageInfo == null || packageInfo.activities == null) return false;
        for (ActivityInfo activityInfo : packageInfo.activities) {
            if (activityInfo.name.equals(activityName)) return true;
        }
        return false;
    }

    public void enterActivity(String packageName, String activityName) {
        // 桌面只按包名判定，避免窗口状态与窗口变化两条事件对同一次桌面切换各处理一遍
        if (isLauncherApp(packageName) && packageName.equals(packageActivity.packageName)) return;
        if (isActivityClass(packageName, activityName)) {
            if (packageName.equals(MainApplication.getInstance().getPackageName()) && activityName.equals(MainActivity.class.getName())) {
                if (lastPackageActivity != null && !packageActivity.packageName.equals(lastPackageActivity.packageName)) {
                    tryStartActions(ApplicationQuitStartAction.class);
                }
                if (setPackageActivity(packageName, activityName)) {
                    tryShowManualPlayView(false);
                }
            } else {
                if (setPackageActivity(packageName, activityName)) {
                    if (lastPackageActivity != null && !packageActivity.packageName.equals(lastPackageActivity.packageName)) {
                        tryStartActions(ApplicationQuitStartAction.class);
                    }
                    tryStartActions(ApplicationStartAction.class);
                    tryShowManualPlayView(true);
                }
            }
        }
    }

    public PackageActivity getPackageActivity() {
        return packageActivity;
    }

    public PackageActivity getLastPackageActivity() {
        return lastPackageActivity;
    }

    public boolean setPackageActivity(String packageName, String activityName) {
        if (packageName == null || activityName == null) return false;
        if (packageName.isEmpty() || activityName.isEmpty()) return false;
        if (packageName.equals(packageActivity.packageName) && activityName.equals(packageActivity.activityName)) return false;
        lastPackageActivity = packageActivity;
        packageActivity = new PackageActivity(packageName, activityName);
        packageActivityListeners.stream().filter(Objects::nonNull).forEach(listener -> listener.onResult(packageActivity));
        return true;
    }

    public void addPackageActivityListener(ResultCallback<PackageActivity> listener) {
        packageActivityListeners.add(listener);
    }

    public void removePackageActivityListener(ResultCallback<PackageActivity> listener) {
        packageActivityListeners.remove(listener);
    }

    public Notification getNotification(String id) {
        return notifications.get(id);
    }

    public void setNotification(NotificationType type, String packageName, Map<String, String> content) {
        Notification notification = new Notification(type, packageName, content);
        tryStartNotificationActions(notification);
    }

    public BatteryInfo getBatteryInfo() {
        return batteryInfo;
    }

    public void setBatteryInfo(int percent, BatteryState status) {
        batteryInfo = new BatteryInfo(percent, status);
        tryStartActions(BatteryStartAction.class);
    }

    public List<BluetoothInfo> getBluetoothInfoList() {
        return bluetoothInfoList;
    }

    public BluetoothInfo getBluetoothInfo() {
        return lastBluetoothInfo;
    }

    public void addBluetoothInfo(String bluetoothAddress, String bluetoothName, boolean connected) {
        lastBluetoothInfo = new BluetoothInfo(bluetoothAddress, bluetoothName, connected);
        if (connected) {
            bluetoothInfoList.add(lastBluetoothInfo);
        } else {
            bluetoothInfoList.removeIf(info -> info.bluetoothAddress().equals(bluetoothAddress));
        }
        tryStartActions(BluetoothStartAction.class);
    }

    public WifiInfo getWifiInfo() {
        return wifiInfo;
    }

    public void setWifiInfo(WifiInfo wifiInfo) {
        this.wifiInfo = wifiInfo;
    }

    public BroadcastInfo getBroadcastInfo() {
        return broadcastInfo;
    }

    public void setBroadcastInfo(String action, String data, Map<String, String> extras) {
        broadcastInfo = new BroadcastInfo(action, data, extras);
        tryStartActions(BroadcastStartAction.class);
    }

    public PhoneState getPhoneState() {
        return AppUtil.getPhoneState(MainApplication.getInstance());
    }

    public void onPhoneStateChanged() {
        tryStartActions(ScreenStartAction.class);
        tryShowManualPlayView(!MainActivity.class.getName().equals(packageActivity.activityName));
    }

    public List<NotworkState> getNetworkState() {
        return networkState;
    }

    public void setNetworkState(List<NotworkState> networkState) {
        this.networkState = networkState;
        tryStartActions(NetworkStartAction.class);
    }

    public String getLastClipboard() {
        return lastClipboard;
    }

    public void setLastClipboard(String lastClipboard) {
        this.lastClipboard = lastClipboard;
    }

    public static class ShortcutInfo {
        public final String packageName;
        public String activityClass;
        public String id;
        public String title;
        public int icon;
        public String intent;

        public ShortcutInfo(String packageName) {
            this.packageName = packageName;
        }

        public boolean isValid() {
            return activityClass != null && id != null && title != null && intent != null;
        }
    }

    public record PackageActivity(String packageName, String activityName) {
    }

    public record Notification(NotificationType type, String packageName, Map<String, String> content) {
    }

    public record BatteryInfo(int percent, BatteryState status) {
    }

    public record BluetoothInfo(String bluetoothAddress, String bluetoothName, boolean active) {
    }

    public record WifiInfo(String wifiName, String gateway, String ip) {
    }

    public record BroadcastInfo(String action, String data, Map<String, String> extras) {
    }

    public record ManualExecuteInfo(Task task, ManualStartAction action) {
    }

    public enum PhoneState {OFF, LOCKED, ON}

    public enum NotworkState {NONE, WIFI, MOBILE, VPN}

    public enum BatteryState {UNKNOWN, CHARGING, DISCHARGING, NOT_CHARGING, FULL}

    public enum NotificationType {NOTIFICATION, TOAST}
}
