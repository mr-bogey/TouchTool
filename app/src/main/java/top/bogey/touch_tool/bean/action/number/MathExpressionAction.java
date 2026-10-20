package top.bogey.touch_tool.bean.action.number;

import com.google.gson.JsonObject;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.parent.CalculateAction;
import top.bogey.touch_tool.bean.action.parent.DynamicPinsAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinDouble;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinNumber;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinAutoPinString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.bean.pin.special_pin.NotLinkAblePin;
import top.bogey.touch_tool.service.TaskRunnable;
import top.bogey.touch_tool.utils.AppLog;

public class MathExpressionAction extends CalculateAction implements DynamicPinsAction {
    private final transient Pin expressionPin = new NotLinkAblePin(new PinAutoPinString(), R.string.math_expression_action_express);
    private final transient Pin resultPin = new Pin(new PinDouble(), R.string.math_expression_action_result, true);

    public MathExpressionAction() {
        super(ActionType.MATH_EXPRESSION);
        addPins(expressionPin, resultPin);
    }

    public MathExpressionAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(expressionPin, resultPin);
        reAddPins(new Pin(new PinDouble()));
    }

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
