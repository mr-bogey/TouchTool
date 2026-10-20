package top.bogey.touch_tool.bean.save.variable;

import com.tencent.mmkv.MMKV;

import top.bogey.touch_tool.bean.task.Variable;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class VariableSave {
    private final MMKV mmkv;
    private final String key;

    private Variable var;
    private long time = System.currentTimeMillis();

    public VariableSave(MMKV mmkv, String key) {
        this.mmkv = mmkv;
        this.key = key;
    }

    public VariableSave(MMKV mmkv, Variable var) {
        this.mmkv = mmkv;
        this.key = var.getId();
        setVar(var);
    }

    public Variable getVar() {
        if (var == null) {
            try {
                var = GsonUtil.getAsObject(mmkv.decodeString(key), Variable.class, null);
            } catch (Exception e) {
                AppLog.e(e);
            }
        }
        time = System.currentTimeMillis();
        return var;
    }

    public Variable getOriginVar() {
        try {
            return GsonUtil.getAsObject(mmkv.decodeString(key), Variable.class, null);
        } catch (Exception e) {
            AppLog.e(e);
        }
        return null;
    }

    public void setVar(Variable var) {
        this.var = var;
        mmkv.encode(key, GsonUtil.toJson(var));
    }

    // 大于10分钟没使用回收内存
    public void recycle() {
        long current = System.currentTimeMillis();
        if (current - time > 10 * 60 * 1000) {
            var = null;
        }
    }

    public void remove() {
        mmkv.remove(key);
    }
}
