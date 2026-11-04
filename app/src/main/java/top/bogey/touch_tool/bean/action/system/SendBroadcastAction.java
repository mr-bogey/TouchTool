package top.bogey.touch_tool.bean.action.system;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.gson.JsonObject;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.ActionType;
import top.bogey.touch_tool.bean.action.app.OpenAppAction;
import top.bogey.touch_tool.bean.action.parent.ExecuteAction;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.pin_objects.PinMap;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_application.PinApplication;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_list.PinList;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_list.PinMultiSelect;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.service.TaskRunnable;

public class SendBroadcastAction extends ExecuteAction {
    private final transient Pin appPin = new Pin(new PinApplication(PinSubType.SINGLE_APP_WITH_ACTIVITY), R.string.pin_app);
    private final transient Pin actionPin = new Pin(new PinString(), R.string.send_broadcast_action_name);
    private final transient Pin categoryPin = new Pin(new PinMultiSelect(new PinString()), R.string.send_broadcast_action_category, false, false, true);
    private final transient Pin dataPin = new Pin(new PinString(), R.string.send_broadcast_action_data, false, false, true);
    private final transient Pin extrasPin = new Pin(new PinMap(new PinString(), new PinString()), R.string.send_broadcast_action_extra);

    public SendBroadcastAction() {
        super(ActionType.SEND_BROADCAST);
        addPins(appPin, actionPin, categoryPin, dataPin, extrasPin);
        initCategorySelection();
    }

    public SendBroadcastAction(JsonObject jsonObject) {
        super(jsonObject);
        reAddPins(appPin, actionPin, categoryPin, dataPin, extrasPin);
        initCategorySelection();
    }

    @Override
    public void execute(TaskRunnable runnable, Pin pin) {
        PinString action = getPinValue(runnable, actionPin);
        String actionName = action.getValue();

        if (actionName == null || actionName.isEmpty()) {
            executeNext(runnable, outPin);
            return;
        }

        Intent intent = new Intent(action.getValue());

        // 广播只按包限定投递：app 针脚给出的是 activity，指向组件的显式广播动态注册的接收器收不到
        PinApplication app = getPinValue(runnable, appPin);
        String packageName = app == null ? null : app.getPackageName();
        if (packageName != null && !packageName.isEmpty()) {
            intent.setPackage(packageName);
        }

        PinList category = getPinValue(runnable, categoryPin);
        for (PinObject object : category) {
            PinString pinString = (PinString) object;
            intent.addCategory(pinString.getValue());
        }

        PinString data = getPinValue(runnable, dataPin);
        String dataValue = data.getValue();
        if (dataValue != null && !dataValue.isEmpty()) {
            intent.setData(Uri.parse(dataValue));
        }

        Bundle bundle = new Bundle();
        PinMap extras = getPinValue(runnable, extrasPin);
        extras.forEach((key, value) -> OpenAppAction.putBundle(bundle, key.toString(), value.toString()));
        intent.putExtras(bundle);

        // 发送广播不依赖无障碍服务存活，服务断连时退回 Application 作为 context
        MainApplication application = MainApplication.getInstance();
        Context context = application.getService();
        if (context == null) context = application;
        context.sendBroadcast(intent);
        executeNext(runnable, outPin);
    }

    private void initCategorySelection() {
        PinMultiSelect multiSelect = categoryPin.getValue(PinMultiSelect.class);
        multiSelect.resetSelectObjects();
        Context context = MainApplication.getInstance();

        String[] names = context.getResources().getStringArray(R.array.intent_category);
        String[] desc = context.getResources().getStringArray(R.array.intent_category_desc);
        for (int i = 0, categoriesSize = OpenAppAction.CATEGORIES.size(); i < categoriesSize; i++) {
            String value = OpenAppAction.CATEGORIES.get(i);
            PinMultiSelect.MultiSelectObject object = new PinMultiSelect.MultiSelectObject(names[i], desc[i], value);
            multiSelect.addSelectObject(object);
        }
    }
}
