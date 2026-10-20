package top.bogey.touch_tool.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * 应用内日志：照常写 logcat，同时留一份在内存里，供设置页点击版本号查看
 */
public class AppLog {
    // 最多保留的条数，超出丢弃最早的
    public static final int MAX_COUNT = 200;
    // 单条内容的最大长度，超出截断，避免超长的 json 撑爆内存
    private static final int MAX_MESSAGE_LENGTH = 2000;

    // 固定分类：界面进出、定时与异常这类日志按业务归类，不按调用类归类
    public static final String CATEGORY_ACTIVITY = "活动类";
    public static final String CATEGORY_ALARM = "定时";
    public static final String CATEGORY_ERROR = "异常";

    private static final List<Entry> logs = new LinkedList<>();
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    public static void d(String tag, String message) {
        Log.d(tag, message);
        add(categoryOf(), tag, message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
        add(categoryOf(), tag, message);
    }

    // 带异常对象的日志一律归到「异常」分类，标签留给调用方区分是哪处出错
    public static void e(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
        add(CATEGORY_ERROR, tag, message + "\n" + Log.getStackTraceString(throwable));
    }

    // 便捷入口：catch 块里直接接住异常，标签取抛出异常的类名，堆栈一并记录
    public static void e(Throwable throwable) {
        if (throwable == null) return;
        e(categoryOf(), throwable.toString(), throwable);
    }

    // 按指定分类记一条日志，调用方自己决定分类
    public static void add(String category, String tag, String message) {
        if (message != null && message.length() > MAX_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_MESSAGE_LENGTH) + "…";
        }
        Entry entry = new Entry(timeFormat.format(new Date()), category, tag, message);
        synchronized (logs) {
            logs.add(0, entry);
            while (logs.size() > MAX_COUNT) {
                logs.remove(logs.size() - 1);
            }
        }
    }

    public static List<Entry> getLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    public static void clear() {
        synchronized (logs) {
            logs.clear();
        }
    }

    // 取调用方类名当分类，跳过 AppLog 与 Thread 自身的栈帧
    private static String categoryOf() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (className.equals(AppLog.class.getName())) continue;
            if (className.equals(Thread.class.getName())) continue;
            return className.substring(className.lastIndexOf('.') + 1);
        }
        return "应用";
    }

    public record Entry(String time, String category, String tag, String message) {
    }
}
