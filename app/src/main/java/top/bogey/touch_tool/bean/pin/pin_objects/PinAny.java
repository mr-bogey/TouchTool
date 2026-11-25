package top.bogey.touch_tool.bean.pin.pin_objects;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import top.bogey.touch_tool.utils.GsonUtil;

/**
 * 任意值针脚：以 payload 装箱任意 PinBase，typeTag 由 payload 派生，meta 存长度等元数据。
 * 声明为非 DYNAMIC：旧针脚侧因此走严格类匹配而拒绝直连，实现"必须经拆箱节点适配旧节点"。
 */
public class PinAny extends PinObject {
    private PinBase payload = new PinObject();
    private final Map<String, String> meta = new LinkedHashMap<>();

    public PinAny() {
        super(PinType.ANY);
    }

    public PinAny(PinBase value) {
        this();
        setPayload(value);
    }

    public PinAny(JsonObject jsonObject) {
        super(jsonObject);
        payload = GsonUtil.getAsObject(jsonObject, "payload", PinBase.class, new PinObject());
        // meta 为派生数据，反序列化后直接重算，避免拷贝链路丢失
        meta.put("length", String.valueOf(payload.toString().length()));
    }

    // 当前装箱的值
    public PinBase getPayload() {
        return payload;
    }

    // 装箱新值，null 时重置为空对象
    public void setPayload(PinBase value) {
        payload = value == null ? new PinObject() : value;
        meta.put("length", String.valueOf(payload.toString().length()));
    }

    // 值类型标签，由 payload 派生，不单独存状态
    public PinType getTypeTag() {
        return payload.getType();
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    // 输入方向放行任意值类型（含跨组的列表/字典）：旧类型可直接连入新节点，视为自动装箱；执行针脚等非值类型仍拒绝
    @Override
    public boolean linkFromAble(PinBase pin) {
        return pin instanceof PinObject;
    }

    // 输出方向仅放行到 PinAny 输入；旧针脚因严格类匹配无法直连，须经拆箱节点
    @Override
    public boolean linkToAble(PinBase pin) {
        return pin instanceof PinAny;
    }

    @Override
    public void reset() {
        payload.reset();
        meta.clear();
    }

    @Override
    public void sync(PinBase value) {
        if (value instanceof PinAny any) {
            payload = any.payload.copy();
            meta.clear();
            meta.putAll(any.meta);
        } else if (value != null) {
            setPayload(value.copy());
        }
    }

    @NonNull
    @Override
    public String toString() {
        return payload.toString();
    }
}
