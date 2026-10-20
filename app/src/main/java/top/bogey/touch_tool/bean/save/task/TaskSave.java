package top.bogey.touch_tool.bean.save.task;

import com.tencent.mmkv.MMKV;

import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class TaskSave {
    private final MMKV mmkv;
    private final String key;

    private Task task;
    private long time = System.currentTimeMillis();

    public TaskSave(MMKV mmkv, String key) {
        this.mmkv = mmkv;
        this.key = key;
    }

    public TaskSave(MMKV mmkv, Task task) {
        this.mmkv = mmkv;
        this.key = task.getId();
        setTask(task);
    }

    public Task getTask() {
        if (task == null) {
            try {
                task = GsonUtil.getAsObject(mmkv.decodeString(key), Task.class, null);
            } catch (Exception e) {
                AppLog.e(e);
            }
        }
        time = System.currentTimeMillis();
        return task;
    }

    public Task getOriginTask() {
        try {
            return GsonUtil.getAsObject(mmkv.decodeString(key), Task.class, null);
        } catch (Exception e) {
            AppLog.e(e);
        }
        return null;
    }

    public void setTask(Task task) {
        this.task = task;
        mmkv.encode(key, GsonUtil.toJson(task));
    }

    // 大于10分钟没使用回收内存
    public void recycle() {
        long current = System.currentTimeMillis();
        if (current - time > 10 * 60 * 1000) {
            task = null;
        }
    }

    public void remove() {
        mmkv.remove(key);
    }
}
