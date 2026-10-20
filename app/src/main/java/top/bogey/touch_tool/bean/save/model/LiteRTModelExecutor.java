package top.bogey.touch_tool.bean.save.model;

import com.google.ai.edge.litert.Accelerator;
import com.google.ai.edge.litert.CompiledModel;
import com.google.ai.edge.litert.Environment;
import com.google.ai.edge.litert.LiteRtException;
import com.google.ai.edge.litert.TensorBuffer;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import top.bogey.touch_tool.utils.AppLog;


public class LiteRTModelExecutor {
    private static final List<Accelerator> accelerators = Arrays.asList(Accelerator.GPU, Accelerator.CPU);
    private static final Environment ENVIRONMENT;

    static {
        try {
            ENVIRONMENT = Environment.create(new HashMap<>());
        } catch (LiteRtException e) {
            throw new RuntimeException(e);
        }
    }

    private CompiledModel model;
    private List<TensorBuffer> inputs;
    private List<TensorBuffer> outputs;

    private int[] inputShape;
    private int[] outputShape;

    public LiteRTModelExecutor(String modelPath) {
        for (Accelerator accelerator : accelerators) {
            AppLog.d("模型", "LiteRTModelExecutor: " + accelerator);
            try {
                model = CompiledModel.create(modelPath, new CompiledModel.Options(accelerator), ENVIRONMENT);
                inputs = model.createInputBuffers();
                outputs = model.createOutputBuffers();
                AppLog.d("模型", "LiteRTModelExecutor: model = " + modelPath);
            } catch (LiteRtException ignored) {
                AppLog.d("模型", "LiteRTModelExecutor: model = " + modelPath + ", accelerator = " + accelerator + " not support");
            }
        }

        try (Interpreter interpreter = new Interpreter(new File(modelPath))) {
            inputShape = interpreter.getInputTensor(0).shape();
            outputShape = interpreter.getOutputTensor(0).shape();
            AppLog.d("模型", "LiteRTModelExecutor: inputShape = " + Arrays.toString(inputShape));
            AppLog.d("模型", "LiteRTModelExecutor: outputShape = " + Arrays.toString(outputShape));
        } catch (Exception ignored) {
            release();
        }
    }

    public float[] execute(float[] input) {
        try {
            inputs.get(0).writeFloat(input);
            model.run(inputs, outputs);
            return outputs.get(0).readFloat();
        } catch (Exception e) {
            AppLog.e(e);
            release();
        }
        return null;
    }

    public boolean isValid() {
        return model != null;
    }

    public void release() {
        if (model != null) {
            inputs.forEach(TensorBuffer::close);
            outputs.forEach(TensorBuffer::close);
            model.close();
        }
    }

    public int[] getInputShape() {
        return inputShape;
    }

    public int[] getOutputShape() {
        return outputShape;
    }
}
