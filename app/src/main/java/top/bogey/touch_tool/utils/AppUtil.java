package top.bogey.touch_tool.utils;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.content.FileProvider;

import com.github.promeg.pinyinhelper.Pinyin;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.databinding.DialogInputTextBinding;
import top.bogey.touch_tool.service.TaskInfoSummary;
import top.bogey.touch_tool.ui.BaseActivity;
import top.bogey.touch_tool.utils.callback.BooleanResultCallback;
import top.bogey.touch_tool.utils.callback.StringResultCallback;

public class AppUtil {
    public final static String LOG_DIR_NAME = "log";
    public final static String TASK_DIR_NAME = "task";
    public final static String DOCUMENT_DIR_NAME = "document";
    /// 外部存储应用专属目录（/Android/data/<包名>/files/data），供用户自行投放文件
    public final static String EXTERNAL_DIR_NAME = "data";

    // 判断当前环境是否为发布环境
    public static boolean isRelease(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0;
    }

    public static void crashTest() {
        throw new RuntimeException("Crash test");
    }

    public static void runOnUiThread(Runnable runnable) {
        if (runnable != null) {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                runnable.run();
            } else {
                new Handler(Looper.getMainLooper()).post(runnable);
            }
        }
    }

    public static void showDialog(Context context, @StringRes int msg, BooleanResultCallback callback) {
        new MaterialAlertDialogBuilder(context).setTitle(R.string.dialog_title).setMessage(msg).setPositiveButton(R.string.enter, (dialog, which) -> callback.onResult(true)).setNegativeButton(R.string.cancel, (dialog, which) -> callback.onResult(false)).show();
    }

    public static void showDialog(Context context, String msg, BooleanResultCallback callback) {
        new MaterialAlertDialogBuilder(context).setTitle(R.string.dialog_title).setMessage(msg).setPositiveButton(R.string.enter, (dialog, which) -> callback.onResult(true)).setNegativeButton(R.string.cancel, (dialog, which) -> callback.onResult(false)).show();
    }

    public static void showEditDialog(Context context, @StringRes int title, CharSequence defaultValue, StringResultCallback callback) {
        DialogInputTextBinding binding = DialogInputTextBinding.inflate(LayoutInflater.from(context));
        binding.titleEdit.setText(defaultValue);

        new MaterialAlertDialogBuilder(context).setTitle(title).setView(binding.getRoot()).setPositiveButton(R.string.enter, (dialog, which) -> {
            if (binding.titleEdit.getText() == null) {
                callback.onResult(null);
            } else {
                callback.onResult(binding.titleEdit.getText().toString());
            }
        }).setNegativeButton(R.string.cancel, null).show();

        binding.getRoot().postDelayed(() -> {
            binding.titleEdit.requestFocus();
            binding.titleEdit.selectAll();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.titleEdit, 0);
        }, 100);
    }

    public static void gotoAppDetailView(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    public static void gotoIgnoreBattery(Context context) {
        try {
            @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    public static boolean isIgnoredBattery(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public static TaskInfoSummary.PhoneState getPhoneState(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screenOn = powerManager.isInteractive();
        if (screenOn) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            boolean locked = keyguardManager.isKeyguardLocked();
            if (locked) return TaskInfoSummary.PhoneState.LOCKED;
            else return TaskInfoSummary.PhoneState.ON;
        }
        return TaskInfoSummary.PhoneState.OFF;
    }

    public static TaskInfoSummary.WifiInfo getWifiInfo(Context context) {
        String wifiName, gatewayIp = "", ip = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return null;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) return null;

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo == null) return null;

        wifiName = wifiInfo.getSSID();

        int gateway = dhcpInfo.gateway;
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(gateway).array();

        try {
            InetAddress address = InetAddress.getByAddress(bytes);
            gatewayIp = address.getHostAddress();
        } catch (UnknownHostException e) {
            AppLog.e(e);
        }

        int ipAddress = wifiInfo.getIpAddress();
        bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipAddress).array();

        try {
            InetAddress address = InetAddress.getByAddress(bytes);
            ip = address.getHostAddress();
        } catch (UnknownHostException e) {
            AppLog.e(e);
        }
        return new TaskInfoSummary.WifiInfo(wifiName, gatewayIp, ip);
    }

    public static void wakePhone(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, context.getPackageName());
        wakeLock.acquire(1000);
        wakeLock.release();
    }

    public static void gotoScheme(Context context, String scheme) {
        try {
            Intent intent = Intent.parseUri(scheme, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(context, intent, null);
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    public static void startActivity(Context context, Intent intent, Bundle options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            ComponentName component = intent.getComponent();
            if (component != null && !component.getClassName().isEmpty()) {
                ActivityInfo activityInfo = TaskInfoSummary.getInstance().getActivityInfo(component.getPackageName(), component.getClassName());
                if (activityInfo != null && !activityInfo.exported) {

                    String currAssistant = Settings.Secure.getString(context.getContentResolver(), "assistant");
                    String assistant = component.flattenToString();

                    try {
                        Settings.Secure.putString(context.getContentResolver(), "assistant", assistant);
                        SearchManager manager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
                        if (manager != null) {
                            HiddenApiBypass.invoke(SearchManager.class, manager, "launchAssist", intent.getExtras());
                        }
                        Thread.sleep(100);
                        Settings.Secure.putString(context.getContentResolver(), "assistant", currAssistant);
                    } catch (Exception ignored) {
                        Settings.Secure.putString(context.getContentResolver(), "assistant", currAssistant);
                    }
                    return;
                }
            }
        }
        try {
            context.startActivity(intent, options);
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    public static void gotoUrl(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    public static void copyToClipboard(Context context, String text) {
        AppUtil.copyToClipboard(context, text, true);
    }

    public static void copyToClipboard(Context context, String text, boolean showToast) {
        if (text == null) return;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(text, text);
        clipboard.setPrimaryClip(clip);
        if (showToast) Toast.makeText(context, R.string.copy_tips, Toast.LENGTH_SHORT).show();
    }

    public static void copyToClipboard(Context context, Bitmap image, boolean showToast) {
        if (image == null) return;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        Uri uri = writeCacheFile(context, DOCUMENT_DIR_NAME, "Picture_" + formatDateTime(context, System.currentTimeMillis(), false, true) + ".jpg", outputStream.toByteArray());
        if (uri == null) return;

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newUri(context.getContentResolver(), "Image", uri);
        clipboard.setPrimaryClip(clip);
        if (showToast) Toast.makeText(context, R.string.copy_tips, Toast.LENGTH_SHORT).show();
    }

    public static Object readFromClipboard(Context context) {
        Object result = null;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                ClipDescription description = clip.getDescription();
                ClipData.Item item = clip.getItemAt(0);
                if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                    CharSequence text = item.getText();
                    if (text != null) result = text.toString();
                } else {
                    Uri uri = item.getUri();
                    if (uri != null) {
                        ContentResolver resolver = context.getContentResolver();
                        String type = resolver.getType(uri);
                        if (type != null && type.startsWith("image")) {
                            byte[] bytes = readFile(context, uri);
                            result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (info.getId().contains(context.getPackageName() + "/")) {
                return true;
            }
        }
        return false;
    }

    public static <T> void chineseSort(List<T> list, Function<T, String> function) {
        Collator collator = Collator.getInstance(Locale.CHINA);
        list.sort((o1, o2) -> collator.compare(function.apply(o1), function.apply(o2)));
    }

    public static Pattern getPattern(String pattern) {
        try {
            return Pattern.compile(pattern);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatRegex(String value) {
        if (value == null) return null;
        final String[] metaCharacters = {"\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "/"};

        // 逐个转义特殊字符
        for (String metaCharacter : metaCharacters) {
            if (value.contains(metaCharacter)) {
                value = value.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return value;
    }

    public static boolean isStringContains(String str, String value) {
        Pattern pattern = getPattern(value.toLowerCase());
        if (pattern == null) {
            return str.toLowerCase().contains(value);
        } else {
            Matcher matcher = pattern.matcher(str.toLowerCase());
            return matcher.find();
        }
    }

    public static boolean isStringContainsWithPinyin(String str, String value) {
        if (isStringContains(str, value)) return true;
        return isPinyinContains(str, value);
    }

    public static boolean isPinyinContains(String str, String value) {
        String pinyin = Pinyin.toPinyin(str, ",");
        if (pinyin == null || pinyin.isEmpty()) return false;
        pinyin = pinyin.toLowerCase();
        value = value.toLowerCase();
        if (isStringContains(pinyin, value)) return true;
        StringBuilder builder = new StringBuilder();
        for (String s : pinyin.split(",")) {
            if (s == null || s.isEmpty()) continue;
            builder.append(s.charAt(0));
        }
        return isStringContains(builder.toString(), value);
    }

    public static String formatDate(Context context, long time, boolean ignoreYear) {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        StringBuilder builder = new StringBuilder();
        if (current.get(Calendar.YEAR) != calendar.get(Calendar.YEAR) || !ignoreYear) builder.append(context.getString(R.string.year, current.get(Calendar.YEAR)));
        builder.append(context.getString(R.string.month, current.get(Calendar.MONTH) + 1));
        builder.append(context.getString(R.string.day, current.get(Calendar.DAY_OF_MONTH)));
        return builder.toString();
    }

    public static String formatTime(Context context, long time, boolean ignoreMillisecond) {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(time);
        StringBuilder builder = new StringBuilder();
        builder.append(context.getString(R.string.hour, current.get(Calendar.HOUR_OF_DAY)));
        builder.append(context.getString(R.string.minute, current.get(Calendar.MINUTE)));
        builder.append(context.getString(R.string.second, current.get(Calendar.SECOND)));
        if (!ignoreMillisecond) builder.append(context.getString(R.string.millisecond, current.get(Calendar.MILLISECOND)));
        return builder.toString();
    }

    public static String formatDateTime(Context context, long time, boolean ignoreYear, boolean ignoreMillisecond) {
        return formatDate(context, time, ignoreYear) + " " + formatTime(context, time, ignoreMillisecond);
    }

    public static String formatDuration(Context context, long duration) {
        long hour = duration / 3600000;
        long minute = (duration % 3600000) / 60000;

        StringBuilder builder = new StringBuilder();
        if (hour > 0) builder.append(context.getString(R.string.hours, hour));
        if (minute > 0) builder.append(context.getString(R.string.minutes, minute));
        return builder.toString();
    }

    public static long mergeDateTime(long date, long time) {
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTimeInMillis(time);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTimeInMillis(date);
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateCalendar.get(Calendar.YEAR), dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DATE), timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE), 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getFileSize(File file) {
        if (file == null || !file.exists()) return 0;
        if (file.isFile()) return file.length();
        long size = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                size += getFileSize(f);
            }
        }
        return size;
    }

    @SuppressLint("DefaultLocale")
    public static String getFileSizeString(File file) {
        long size = getFileSize(file);
        double kb = size / 1024.0;
        if (kb < 1024) return String.format("%.1fKB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1fMB", mb);
        return String.format("%.1fGB", mb / 1024.0);
    }

    @SuppressLint("DefaultLocale")
    public static String getCacheDirsSizeString(Context context) {
        String[] dirs = new String[]{LOG_DIR_NAME, TASK_DIR_NAME, DOCUMENT_DIR_NAME};
        long size = 0;
        for (String dir : dirs) {
            size += getFileSize(new File(context.getCacheDir(), dir));
        }
        double kb = size / 1024.0;
        if (kb < 1024) return String.format("%.1fKB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1fMB", mb);
        return String.format("%.1fGB", mb / 1024.0);
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) return false;
        if (file.isFile()) return file.delete();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!deleteFile(f)) return false;
            }
        }
        return file.delete();
    }

    public static byte[] readFile(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return new byte[0];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            byte[] byteArray = outputStream.toByteArray();
            outputStream.close();
            return byteArray;
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static File writeFile(String path, InputStream inputStream) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) return null;
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] content = new byte[4096];
            int length;
            while ((length = inputStream.read(content)) != -1) {
                outputStream.write(content, 0, length);
            }
            outputStream.flush();
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    public static Uri writeCacheFile(Context context, String parent, String fileName, byte[] content) {
        File fileParent = context.getCacheDir();
        if (parent != null) fileParent = new File(fileParent, parent);

        File file = new File(fileParent, fileName);
        file = writeFile(file.getAbsolutePath(), new ByteArrayInputStream(content));

        if (file != null) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".file_provider", file);
        }
        return null;
    }

    public static Uri writeFilesFile(Context context, String parent, String fileName, byte[] content) {
        File fileParent = context.getFilesDir();
        if (parent != null) fileParent = new File(fileParent, parent);

        File file = new File(fileParent, fileName);
        file = writeFile(file.getAbsolutePath(), new ByteArrayInputStream(content));

        if (file != null) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".file_provider", file);
        }
        return null;
    }

    /// 获取外部存储的应用专属目录，不存在则创建
    /// 返回 /storage/emulated/0/Android/data/<包名>/files/data，应用启动时调用
    public static void initExternalDir(Context context) {
        File dir = context.getExternalFilesDir(EXTERNAL_DIR_NAME);
        // 外部存储未挂载或不可用时退回内部存储，保证返回的路径一定可读写
        if (dir == null) dir = new File(context.getFilesDir(), EXTERNAL_DIR_NAME);
        if (!dir.exists()) dir.mkdirs();
    }

    public static Uri writeDownloadFile(Context context, String parent, String fileName, byte[] content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "*/*");
            if (parent != null) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + parent);
            } else {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            }

            try {
                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri == null) return null;

                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                    if (outputStream == null) return null;
                    outputStream.write(content);
                    outputStream.flush();
                }
                return uri;
            } catch (IOException ignored) {
            }
        } else {
            File fileParent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (parent != null) fileParent = new File(fileParent, parent);

            File file = new File(fileParent, fileName);
            file = writeFile(file.getAbsolutePath(), new ByteArrayInputStream(content));

            if (file != null) {
                return FileProvider.getUriForFile(context, context.getPackageName() + ".file_provider", file);
            }
        }
        return null;
    }

    public static void exportFile(BaseActivity activity, String fileName, byte[] content) {
        exportFile(activity, fileName, content, null);
    }

    public static void exportFile(BaseActivity activity, String fileName, byte[] content, BooleanResultCallback callback) {
        activity.launcherCreateDocument(fileName, (code, intent) -> {
            if (code == Activity.RESULT_OK) {
                Uri uri = intent.getData();
                if (uri == null) return;
                try (OutputStream outputStream = activity.getContentResolver().openOutputStream(uri)) {
                    if (outputStream == null) return;
                    outputStream.write(content);
                    outputStream.flush();
                } catch (IOException ignored) {
                }
                if (callback != null) callback.onResult(true);
            } else {
                if (callback != null) callback.onResult(false);
            }
        });
    }

    public static Uri writePictureImage(Context context, Bitmap image) {
        String fileName = "Picture_" + formatDateTime(context, System.currentTimeMillis(), false, true);
        return writePictureImage(context, MainApplication.appName, fileName, image);
    }

    public static Uri writePictureImage(Context context, String parent, String fileName, Bitmap image) {
        if (image == null) return null;

        int index = fileName.lastIndexOf(".");
        if (index > 0) fileName = fileName.substring(0, index);
        fileName = fileName + ".jpg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (parent != null) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + parent);
            } else {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            }

            try {
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (uri == null) return null;

                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                    if (outputStream == null) return null;
                    image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
                return uri;
            } catch (IOException ignored) {
            }
        } else {
            File fileParent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (parent != null) fileParent = new File(fileParent, parent);
            if (!fileParent.exists() && !fileParent.mkdirs()) return null;

            File file = new File(fileParent, fileName);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"image/jpeg"}, null);
            } catch (IOException ignored) {
            }
            return FileProvider.getUriForFile(context, context.getPackageName() + ".file_provider", file);
        }
        return null;
    }

    public static void shareImage(Context context, Bitmap image) {
        if (image == null) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("image/*");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        Uri uri = writeCacheFile(context, DOCUMENT_DIR_NAME, "Picture_" + formatDateTime(context, System.currentTimeMillis(), false, true) + ".jpg", outputStream.toByteArray());

        if (uri != null) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(intent);
        }
    }

    public static void shareText(Context context, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("text/*");

        Uri uri = writeCacheFile(context, DOCUMENT_DIR_NAME, "Text_" + formatDateTime(context, System.currentTimeMillis(), false, true) + ".txt", text.getBytes());
        if (uri != null) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(intent);
        }
    }

    public static List<AccessibilityNodeInfo> getWindows(AccessibilityService service) {
        List<AccessibilityNodeInfo> windows = new ArrayList<>();
        List<AccessibilityWindowInfo> list = service.getWindows();
        for (AccessibilityWindowInfo window : list) {
            if (window == null) continue;
            if (window.getType() == AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) continue;
            AccessibilityNodeInfo root = window.getRoot();
            if (root == null) continue;
            if (root.getChildCount() == 0) continue;
            windows.add(root);
        }
        return windows;
    }
}
