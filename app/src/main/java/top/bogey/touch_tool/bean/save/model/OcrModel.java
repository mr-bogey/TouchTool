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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.GsonUtil;

public class OcrModel extends LiteRTModel {
    private List<String> labels = new ArrayList<>();
    private OcrModelConfig detConfig = null;
    private List<OcrModelConfig> recConfigs = new ArrayList<>();
    private transient int[] widths = {};


    private transient LiteRTModelExecutor detExecutor;
    private transient List<LiteRTModelExecutor> recExecutors;

    public OcrModel() {
        super(ModelType.OCR);
    }

    public OcrModel(JsonObject jsonObject) {
        super(jsonObject);
        labels = GsonUtil.getAsObject(jsonObject, "labels", TypeToken.getParameterized(List.class, String.class).getType(), new ArrayList<>());
        detConfig = GsonUtil.getAsObject(jsonObject, "detConfig", OcrModelConfig.class, null);
        recConfigs = GsonUtil.getAsObject(jsonObject, "recConfigs", TypeToken.getParameterized(List.class, OcrModelConfig.class).getType(), new ArrayList<>());
    }

    @Override
    public boolean importModel(Context context, Uri uri) {
        String modelDirPath = getModelDirPath(context);
        File modelDir = new File(modelDirPath);
        if (!modelDir.exists()) if (!modelDir.mkdirs()) return false;

        try (ZipInputStream zipInputStream = new ZipInputStream(context.getContentResolver().openInputStream(uri))) {
            ZipEntry zipEntry;
            int modelFileCount = 0;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String name = zipEntry.getName();
                if (name.endsWith(MODEL_SUFFIX) && isModelFile(name)) {
                    File file = new File(modelDirPath, name);
                    if (file.exists()) {
                        modelFileCount++;
                        continue;
                    }

                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                    } catch (Exception e) {
                        AppLog.e(e);
                    }

                    modelFileCount++;
                }
            }
            return modelFileCount == 1 + recConfigs.size();
        } catch (Exception e) {
            AppLog.e(e);
            return false;
        }
    }

    @Override
    public void removeModel(Context context) {
        super.removeModel(context);
        if (detExecutor != null) detExecutor.release();
        detExecutor = null;

        if (recExecutors != null) {
            for (LiteRTModelExecutor recExecutor : recExecutors) {
                recExecutor.release();
            }
        }
        recExecutors = null;
    }

    private boolean isModelFile(String name) {
        if (detConfig != null && name.startsWith(detConfig.getModelName())) return true;
        for (OcrModelConfig recConfig : recConfigs) {
            if (name.startsWith(recConfig.getModelName())) return true;
        }
        return false;
    }


    private boolean initModel(Context context) {
        if (detExecutor == null || !detExecutor.isValid() || recExecutors == null || recExecutors.size() != recConfigs.size()) {
            boolean result = true;

            String detModelPath = getModelDirPath(context) + File.separator + detConfig.getModelName() + MODEL_SUFFIX;
            detExecutor = new LiteRTModelExecutor(detModelPath);
            result &= detExecutor.isValid();

            recExecutors = new ArrayList<>();
            for (int i = 0; i < recConfigs.size(); i++) {
                OcrModelConfig recConfig = recConfigs.get(i);
                String recModelPath = getModelDirPath(context) + File.separator + recConfig.getModelName() + MODEL_SUFFIX;
                LiteRTModelExecutor recExecutor = new LiteRTModelExecutor(recModelPath);
                result &= recExecutor.isValid();
                if (result) {
                    recExecutors.add(recExecutor);
                }
            }

            widths = new int[recExecutors.size()];
            recExecutors.sort(Comparator.comparingInt(o -> o.getInputShape()[2]));
            for (int i = 0; i < recExecutors.size(); i++) {
                LiteRTModelExecutor recExecutor = recExecutors.get(i);
                widths[i] = recExecutor.getInputShape()[2];
                result &= widths[i] > 0;
            }

            if (!result) {
                detExecutor.release();
                detExecutor = null;

                for (LiteRTModelExecutor recExecutor : recExecutors) {
                    recExecutor.release();
                }
                recExecutors = null;
                return false;
            }

            return true;
        }
        return true;
    }

    @Override
    public List<ModelResult> execute(Context context, Bitmap bitmap, float confThreshold) {
        if (!initModel(context)) return new ArrayList<>();

        int[] inputShape = detExecutor.getInputShape();
        int inputWidth = inputShape[1];
        int inputHeight = inputShape[2];
        LetterBox letterBox = new LetterBox(bitmap, inputWidth, inputHeight);
        float[] letterBoxInfo = new float[]{letterBox.getScale(), letterBox.getOffsetX(), letterBox.getOffsetY()};

        float[] input = new float[inputWidth * inputHeight * 3];
        int[] pixels = new int[inputWidth * inputHeight];
        letterBox.getBitmap().getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            input[i * 3] = (float) Color.red(pixel) / 255.0f;
            input[i * 3 + 1] = (float) Color.green(pixel) / 255.0f;
            input[i * 3 + 2] = (float) Color.blue(pixel) / 255.0f;
        }

        float[] output = detExecutor.execute(input);
        if (output == null) return new ArrayList<>();
        if (output.length != inputWidth * inputHeight) return new ArrayList<>();

        OcrRecInput[] inputs = detPostProcess(bitmap, output, inputWidth, inputHeight, widths, letterBoxInfo);
        List<OcrRecInput> list = Arrays.asList(inputs);
        list.sort((o1, o2) -> {
            if (Math.abs(o1.y - o2.y) < 20) {
                return Float.compare(o1.x, o2.x);
            }
            return Float.compare(o1.y, o2.y);
        });

        List<ModelResult> results = new ArrayList<>();
        for (OcrRecInput ocrRecInput : list) {
            LiteRTModelExecutor executor = recExecutors.get(ocrRecInput.index);
            float[] recOutput = executor.execute(ocrRecInput.input);
            if (recOutput == null) continue;

            OcrRecOutput ocrRecOutput = ctcDecode(recOutput, executor.getInputShape(), executor.getOutputShape());
            if (ocrRecOutput.getValue() < confThreshold) continue;

            StringBuilder builder = new StringBuilder();
            for (int index : ocrRecOutput.getIndex()) {
                if (index >= 0 && index < labels.size()) {
                    builder.append(labels.get(index));
                } else {
                    builder.append(" ");
                }
            }
            ModelResult result = new ModelResult(builder.toString(), ocrRecOutput.getValue(), new RectF(ocrRecInput.area));
            results.add(result);
        }

        return results;
    }

    private OcrRecOutput ctcDecode(float[] output, int[] inputShape, int[] outputShape) {

        int numClasses, timeStep;
        if (outputShape.length >= 3) {
            numClasses = outputShape[2];
            timeStep = outputShape[1];
        } else {
            numClasses = outputShape[1];
            timeStep = outputShape[0];
        }
        if (timeStep <= 0) timeStep = inputShape[2] / 8;

        return recPostProcess(output, numClasses, timeStep);
    }

    private static native OcrRecInput[] detPostProcess(Bitmap bitmap, float[] probMap, int width, int height, int[] widths, float[] letterBox);

    private static native OcrRecOutput recPostProcess(float[] output, int numClasses, int timeStep);
}
