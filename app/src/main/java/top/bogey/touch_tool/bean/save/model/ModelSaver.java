package top.bogey.touch_tool.bean.save.model;

import android.content.Context;
import android.net.Uri;

import com.tencent.mmkv.MMKV;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class ModelSaver {
    private static ModelSaver instance;

    public static ModelSaver getInstance() {
        synchronized (ModelSaver.class) {
            if (instance == null) {
                instance = new ModelSaver();
            }
        }
        return instance;
    }

    private static final String MODEL_DB = "MODEL_DB";
    private static final String METADATA = "metadata.json";

    private final MMKV mmkv = MMKV.mmkvWithID(MODEL_DB, MMKV.SINGLE_PROCESS_MODE);
    private final Map<String, LiteRTModel> modelMap = new LinkedHashMap<>();

    private ModelSaver() {
        String[] keys = mmkv.allKeys();
        if (keys != null) {
            for (String key : keys) {
                LiteRTModel liteRTModel = GsonUtil.getAsObject(mmkv.decodeString(key), LiteRTModel.class, null);
                if (liteRTModel != null) modelMap.put(liteRTModel.getId(), liteRTModel);
            }
        }
    }

    public boolean importModel(Context context, Uri uri) {
        LiteRTModel liteRTModel = null;
        try (ZipInputStream zipInputStream = new ZipInputStream(context.getContentResolver().openInputStream(uri))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String name = zipEntry.getName();
                if (METADATA.equals(name)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));

                    StringBuilder builder = new StringBuilder();
                    char[] buffer = new char[1024];
                    int length;
                    while ((length = reader.read(buffer)) != -1) {
                        builder.append(new String(buffer, 0, length));
                    }
                    String json = builder.toString();
                    liteRTModel = GsonUtil.getAsObject(json, LiteRTModel.class, null);
                    break;
                }
            }
        } catch (Exception e) {
            AppLog.e(e);
        }

        if (liteRTModel == null) return false;
        boolean imported = liteRTModel.importModel(context, uri);
        if (imported) {
            modelMap.put(liteRTModel.getId(), liteRTModel);
            mmkv.encode(liteRTModel.getId(), GsonUtil.toJson(liteRTModel));
        }
        return imported;
    }

    public void removeModel(Context context, String id) {
        LiteRTModel liteRTModel = modelMap.remove(id);
        if (liteRTModel != null) {
            liteRTModel.removeModel(context);
        }
        mmkv.removeValueForKey(id);
    }

    public List<LiteRTModel> getModelList(LiteRTModel.ModelType type) {
        List<LiteRTModel> list = new ArrayList<>();
        modelMap.values().forEach(modelInfo -> {
            if (modelInfo.getType() == type) {
                list.add(modelInfo);
            }
        });
        list.sort(Comparator.comparingLong(LiteRTModel::getTime));
        return list;
    }

    public List<LiteRTModel> getModelList() {
        List<LiteRTModel> list = new ArrayList<>(modelMap.values());
        list.sort(Comparator.comparingLong(LiteRTModel::getTime));
        return list;
    }

    public LiteRTModel getModel(String id) {
        return modelMap.get(id);
    }
}
