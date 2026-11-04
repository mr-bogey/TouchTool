package top.bogey.touch_tool.bean.action.number;

import com.google.gson.JsonObject;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.CalculateAction;
import top.bogey.touch_tool.bean.action.parent.DynamicPinsAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinDouble;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinNumber;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinAutoPinString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.service.TaskRunnable;
import top.bogey.touch_tool.utils.AppLog;

/**
 * 数学公式动作，把公式文本交给动态针脚里的变量求值
 */
public class MathExpressionAction extends CalculateAction implements DynamicPinsAction {
    // 公式里的单字母变量，每个变量对应一个动态针脚
    private final static Pattern VARIABLE_PATTERN = Pattern.compile("\\b([a-zA-Z])\\b");

    /**
     * 公式针脚，连接后公式由来源针脚提供
     */
    private final transient Pin expressionPin = new Pin(new PinAutoPinString(), R.string.math_expression_action_express);
    /**
     * 计算结果针脚，不可计算时保持 0
     */
    private final transient Pin resultPin = new Pin(new PinDouble(), R.string.math_expression_action_result, true);

    public MathExpressionAction() {
        super(ActionType.MATH_EXPRESSION);
        addPins(expressionPin, resultPin);
    }

    public MathExpressionAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(expressionPin, resultPin);
        reAddPins(new Pin(new PinDouble()));
        // 公式已连接时变量由来源针脚决定，存档里的动态针脚全部丢弃
        if (expressionPin.isLinked()) getDynamicPins().forEach(this::removePin);
    }

    /**
     * 计算并写入结果
     *
     * @param runnable 任务运行器
     * @param pin      被计算的输出针脚
     */
    @Override
    public void calculate(TaskRunnable runnable, Pin pin) {
        PinString expression = getPinValue(runnable, expressionPin);
        String expressionStr = expression.getValue();
        if (expressionStr == null || expressionStr.isEmpty()) return;

        ExpressionBuilder builder = new ExpressionBuilder(expressionStr);
        List<Pin> dynamicPins = getDynamicPins();
        dynamicPins.forEach(dynamicPin -> builder.variable(dynamicPin.getTitle()));
        try {
            Expression exp = builder.build();
            dynamicPins.forEach(dynamicPin -> {
                PinNumber<?> value = getPinValue(runnable, dynamicPin);
                exp.setVariable(dynamicPin.getTitle(), value.doubleValue());
            });
            double result = exp.evaluate();
            resultPin.getValue(PinDouble.class).setValue(result);
        } catch (Exception e) {
            AppLog.e(e);
        }
    }

    /**
     * 公式针脚被连接时，公式内容不再由自身决定，移除全部动态针脚
     *
     * @param task   针脚所属任务
     * @param origin 发生变化的针脚
     * @param to     连接到的针脚
     */
    @Override
    public void onLinkedTo(Task task, Pin origin, Pin to) {
        super.onLinkedTo(task, origin, to);
        if (origin != expressionPin) return;
        new ArrayList<>(getDynamicPins()).forEach(pin -> removePin(task, pin));
    }

    /**
     * 公式针脚断开连接后，按公式文本重新解析动态针脚
     *
     * @param task   针脚所属任务
     * @param origin 发生变化的针脚
     * @param from   断开连接的针脚
     */
    @Override
    public void onUnLinkedFrom(Task task, Pin origin, Pin from) {
        super.onUnLinkedFrom(task, origin, from);
        if (origin != expressionPin) return;
        resetDynamicPins(task);
    }

    /**
     * 按公式文本重新解析动态针脚，公式里的单字母变量对应一个数值针脚
     *
     * @param task 针脚所属任务，用于断开被移除针脚的连接
     */
    public void resetDynamicPins(Task task) {
        List<String> keys = new ArrayList<>();
        if (!expressionPin.isLinked()) {
            PinString expression = expressionPin.getValue();
            String expressionStr = expression == null ? null : expression.getValue();
            if (expressionStr != null) {
                Matcher matcher = VARIABLE_PATTERN.matcher(expressionStr);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    if (keys.contains(key)) continue;
                    keys.add(key);
                }
            }
        }

        List<Pin> removePins = new ArrayList<>();
        for (Pin dynamicPin : getDynamicPins()) {
            if (keys.isEmpty() || !keys.remove(dynamicPin.getTitle())) removePins.add(dynamicPin);
        }
        removePins.forEach(pin -> removePin(task, pin));

        for (String key : keys) {
            Pin pin = new Pin(new PinDouble(), 0, false, true, false);
            pin.setTitle(key);
            addPin(pin);
        }
    }

    /**
     * 获取结果针脚之后的全部动态针脚，即公式里的变量针脚
     *
     * @return 动态针脚列表
     */
    @Override
    public List<Pin> getDynamicPins() {
        List<Pin> pins = new ArrayList<>();
        boolean start = false;
        for (Pin pin : getPins()) {
            if (start) pins.add(pin);
            if (pin == resultPin) start = true;
        }
        return pins;
    }
}
