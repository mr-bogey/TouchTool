package top.bogey.touch_tool.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import top.bogey.touch_tool.utils.thread.TaskQueue;
import top.bogey.touch_tool.utils.thread.TaskThreadPoolExecutor;

public class ThreadUtil {
    private static final ExecutorService executorService = new TaskThreadPoolExecutor(2, 10, 60, TimeUnit.SECONDS, new TaskQueue<>(10));
    private static final ExecutorService taskService = new TaskThreadPoolExecutor(5, 30, 60, TimeUnit.SECONDS, new TaskQueue<>(20));

    // 发出即忘的辅助线程，调用方（删标签、自动备份、导入模型、整理布局等）不关心结果，
    // 池满时这里兜住拒绝异常，只记日志，不让它把 UI 线程炸掉
    public static void execute(Runnable runnable) {
        try {
            executorService.execute(runnable);
        } catch (RejectedExecutionException e) {
            AppLog.e("线程", "execute: 线程池已满，任务未能提交", e);
        }
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static Future<?> submitTask(Runnable runnable) {
        return taskService.submit(runnable);
    }
}