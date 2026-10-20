package top.bogey.touch_tool.bean.save.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class YoloModel extends LiteRTModel {
    private List<String> labels = new ArrayList<>();
    private YoloModelConfig modelConfig;

    private transient LiteRTModelExecutor executor;

    public YoloModel() {
        super(ModelType.YOLO);
    }

    public YoloModel(JsonObject jsonObject) {
        super(jsonObject);
        labels = GsonUtil.getAsObject(jsonObject, "labels", TypeToken.getParameterized(List.class, String.class).getType(), new ArrayList<>());
        modelConfig = GsonUtil.getAsObject(jsonObject, "modelConfig", YoloModelConfig.class, null);
    }

    @Override
    public boolean importModel(Context context, Uri uri) {
        String modelDirPath = getModelDirPath(context);
        File modelDir = new File(modelDirPath);
        if (!modelDir.exists()) if (!modelDir.mkdirs()) return false;

        try (ZipInputStream zipInputStream = new ZipInputStream(context.getContentResolver().openInputStream(uri))) {
            ZipEntry zipEntry;
            boolean hasModel = false;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String name = zipEntry.getName();
                if (name.endsWith(MODEL_SUFFIX) && name.startsWith(modelConfig.getModelName())) {
                    File file = new File(modelDirPath, name);

                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                    } catch (Exception e) {
                        AppLog.e(e);
                    }

                    hasModel = true;
                    break;
                }
            }
            return hasModel;
        } catch (Exception e) {
            AppLog.e(e);
            return false;
        }
    }

    @Override
    public void removeModel(Context context) {
        super.removeModel(context);
        if (executor != null) executor.release();
        executor = null;
    }

    public List<String> getLabels() {
        return labels;
    }

    private boolean initModel(Context context) {
        if (executor == null || !executor.isValid()) {
            String modelPath = getModelDirPath(context) + File.separator + modelConfig.getModelName() + MODEL_SUFFIX;
            executor = new LiteRTModelExecutor(modelPath);
            if (!executor.isValid()) {
                executor.release();
                executor = null;
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public List<ModelResult> execute(Context context, Bitmap bitmap, float confThreshold) {
        if (!initModel(context)) return new ArrayList<>();

        int[] inputShape = executor.getInputShape();
        int inputWidth = inputShape[1];
        int inputHeight = inputShape[2];
        LetterBox letterBox = new LetterBox(bitmap, inputWidth, inputHeight);

        float[] input = new float[inputWidth * inputHeight * 3];
        int[] pixels = new int[inputWidth * inputHeight];
        letterBox.getBitmap().getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            input[i * 3] = (float) Color.red(pixel) / 255.0f;
            input[i * 3 + 1] = (float) Color.green(pixel) / 255.0f;
            input[i * 3 + 2] = (float) Color.blue(pixel) / 255.0f;
        }

        float[] output = executor.execute(input);
        if (output == null) return new ArrayList<>();

        List<ModelResult> resultList;
        if (modelConfig.isNeedNMS()) {
            resultList = parseOutputsWithNMS(output, confThreshold);
        } else {
            resultList = parseOutputs(output, confThreshold);
        }

        // 这时的区域是百分比的区域，需要转换成实际的区域
        AppLog.d("模型", "execute yolo: " + resultList);

        resultList.forEach(result -> {
            RectF area = result.getArea();
            float left = (area.left * inputWidth - letterBox.getOffsetX()) / letterBox.getScale();
            float top = (area.top * inputHeight - letterBox.getOffsetY()) / letterBox.getScale();
            float right = (area.right * inputWidth - letterBox.getOffsetX()) / letterBox.getScale();
            float bottom = (area.bottom * inputHeight - letterBox.getOffsetY()) / letterBox.getScale();
            area.set(left, top, right, bottom);
        });
        return resultList;
    }

    private List<ModelResult> parseOutputs(float[] outputs, float confThreshold) {
        int[] outputShape = executor.getOutputShape();
        int channelNum = outputShape[2];
        int boxCount = outputShape[1];

        List<ModelResult> resultList = new ArrayList<>();
        for (int i = 0; i < boxCount; i++) {
            int index = i * channelNum;
            float confidence = outputs[index + 4];
            if (confidence < confThreshold) continue;

            float left = outputs[index];
            float top = outputs[index + 1];
            float right = outputs[index + 2];
            float bottom = outputs[index + 3];

            int classIndex = (int) outputs[index + 5];
            String label = labels.get(classIndex);

            ModelResult result = new ModelResult(label, confidence, new RectF(left, top, right, bottom));
            resultList.add(result);
        }
        return resultList;
    }

    private List<ModelResult> parseOutputsWithNMS(float[] outputs, float confThreshold) {
        int classNum = labels.size();
        int[] outputShape = executor.getOutputShape();
        int boxCount = outputShape[2];

        List<ModelResult> resultList = new ArrayList<>();
        for (int i = 0; i < boxCount; i++) {
            float maxConfidence = -1;
            int classIndex = -1;

            for (int j = 0; j < classNum; j++) {
                float confidence = outputs[(4 + j) * boxCount + i];
                if (confidence > maxConfidence) {
                    maxConfidence = confidence;
                    classIndex = j;
                }
            }

            if (maxConfidence < confThreshold) continue;

            String label = labels.get(classIndex);

            float cx = outputs[i];
            float cy = outputs[boxCount + i];
            float w = outputs[2 * boxCount + i];
            float h = outputs[3 * boxCount + i];

            float left = cx - w / 2;
            float top = cy - h / 2;
            float right = cx + w / 2;
            float bottom = cy + h / 2;

            ModelResult result = new ModelResult(label, maxConfidence, new RectF(left, top, right, bottom));
            resultList.add(result);
        }
        return nonMaxSuppression(resultList);
    }

    private static List<ModelResult> nonMaxSuppression(List<ModelResult> list) {
        List<ModelResult> resultList = new ArrayList<>();
        list.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        while (!list.isEmpty()) {
            ModelResult a = list.remove(0);
            resultList.add(a);

            Iterator<ModelResult> iterator = list.iterator();
            while (iterator.hasNext()) {
                ModelResult b = iterator.next();
                if (a.getText().equals(b.getText())) {
                    float iou = intersectionOverUnion(a.getArea(), b.getArea());
                    if (iou > 0.5f) {
                        iterator.remove();
                    }
                }
            }
        }

        return resultList;
    }

    private static float intersectionOverUnion(RectF aRect, RectF bRect) {
        float left = Math.max(aRect.left, bRect.left);
        float top = Math.max(aRect.top, bRect.top);
        float right = Math.min(aRect.right, bRect.right);
        float bottom = Math.min(aRect.bottom, bRect.bottom);

        float width = Math.max(right - left, 0);
        float height = Math.max(bottom - top, 0);
        float intersectionArea = width * height;

        float aArea = aRect.width() * aRect.height();
        float bArea = bRect.width() * bRect.height();
        float unionArea = aArea + bArea - intersectionArea;
        if (unionArea > 0) return intersectionArea / unionArea;
        return 0;
    }
}
