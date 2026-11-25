package top.bogey.touch_tool.bean.action;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.lang.reflect.Constructor;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.app.CheckInAppAction;
import top.bogey.touch_tool.bean.action.app.GetCurrentAppAction;
import top.bogey.touch_tool.bean.action.app.OpenAppAction;
import top.bogey.touch_tool.bean.action.app.OpenShortcutAction;
import top.bogey.touch_tool.bean.action.app.OpenUriSchemeAction;
import top.bogey.touch_tool.bean.action.app.StringToAppAction;
import top.bogey.touch_tool.bean.action.app.WaitInAppAction;
import top.bogey.touch_tool.bean.action.area.AreaFromIntegerAction;
import top.bogey.touch_tool.bean.action.area.AreaOffsetAction;
import top.bogey.touch_tool.bean.action.area.AreaToIntegerAction;
import top.bogey.touch_tool.bean.action.area.CheckAreaContainPosAction;
import top.bogey.touch_tool.bean.action.area.CheckAreaRelationAction;
import top.bogey.touch_tool.bean.action.area.GetAreaCenterAction;
import top.bogey.touch_tool.bean.action.area.GetAreaIntersectionAction;
import top.bogey.touch_tool.bean.action.area.GetAreaRandomAction;
import top.bogey.touch_tool.bean.action.area.PickAreaAction;
import top.bogey.touch_tool.bean.action.bool.BooleanAndAction;
import top.bogey.touch_tool.bean.action.bool.BooleanAndShortCircuitAction;
import top.bogey.touch_tool.bean.action.bool.BooleanNotAction;
import top.bogey.touch_tool.bean.action.bool.BooleanOrAction;
import top.bogey.touch_tool.bean.action.bool.BooleanOrShortCircuitAction;
import top.bogey.touch_tool.bean.action.image.ColorEqualAction;
import top.bogey.touch_tool.bean.action.image.CreateQRCodeAction;
import top.bogey.touch_tool.bean.action.image.CropImageAction;
import top.bogey.touch_tool.bean.action.image.FindColorsAction;
import top.bogey.touch_tool.bean.action.image.FindImageAction;
import top.bogey.touch_tool.bean.action.image.FindImagesAction;
import top.bogey.touch_tool.bean.action.image.GetColorAction;
import top.bogey.touch_tool.bean.action.image.GetImageAction;
import top.bogey.touch_tool.bean.action.image.IsColorExistAction;
import top.bogey.touch_tool.bean.action.image.IsImageExistAction;
import top.bogey.touch_tool.bean.action.image.LoadImageAction;
import top.bogey.touch_tool.bean.action.image.ParseQRCodeAction;
import top.bogey.touch_tool.bean.action.image.ResizeImageAction;
import top.bogey.touch_tool.bean.action.image.SaveImageAction;
import top.bogey.touch_tool.bean.action.image.TouchColorAction;
import top.bogey.touch_tool.bean.action.image.TouchImageAction;
import top.bogey.touch_tool.bean.action.image.YoloDetectAction;
import top.bogey.touch_tool.bean.action.list.ListAddAction;
import top.bogey.touch_tool.bean.action.list.ListAppendAction;
import top.bogey.touch_tool.bean.action.list.ListChoiceAction;
import top.bogey.touch_tool.bean.action.list.ListClearAction;
import top.bogey.touch_tool.bean.action.list.ListContainAction;
import top.bogey.touch_tool.bean.action.list.ListForeachAction;
import top.bogey.touch_tool.bean.action.list.ListGetAction;
import top.bogey.touch_tool.bean.action.list.ListIndexOfAction;
import top.bogey.touch_tool.bean.action.list.ListIsEmptyAction;
import top.bogey.touch_tool.bean.action.list.ListRemoveAction;
import top.bogey.touch_tool.bean.action.list.ListRemoveObjectAction;
import top.bogey.touch_tool.bean.action.list.ListReverseAction;
import top.bogey.touch_tool.bean.action.list.ListSetAction;
import top.bogey.touch_tool.bean.action.list.ListSizeAction;
import top.bogey.touch_tool.bean.action.list.ListSortAction;
import top.bogey.touch_tool.bean.action.list.ListSubListAction;
import top.bogey.touch_tool.bean.action.list.MakeListAction;
import top.bogey.touch_tool.bean.action.logic.ChoiceExecuteAction;
import top.bogey.touch_tool.bean.action.logic.ForLoopAction;
import top.bogey.touch_tool.bean.action.logic.IfConditionAction;
import top.bogey.touch_tool.bean.action.logic.ParallelExecuteAction;
import top.bogey.touch_tool.bean.action.logic.RandomExecuteAction;
import top.bogey.touch_tool.bean.action.logic.SequenceExecuteAction;
import top.bogey.touch_tool.bean.action.logic.SwitchAction;
import top.bogey.touch_tool.bean.action.logic.WaitConditionAction;
import top.bogey.touch_tool.bean.action.logic.WhileLoopAction;
import top.bogey.touch_tool.bean.action.map.BoxValueAction;
import top.bogey.touch_tool.bean.action.map.BuildObjectAction;
import top.bogey.touch_tool.bean.action.map.GetValueItemAction;
import top.bogey.touch_tool.bean.action.map.GetValueTypeAction;
import top.bogey.touch_tool.bean.action.map.MakeMapAction;
import top.bogey.touch_tool.bean.action.map.UnboxValueAction;
import top.bogey.touch_tool.bean.action.map.MapAppendAction;
import top.bogey.touch_tool.bean.action.map.MapClearAction;
import top.bogey.touch_tool.bean.action.map.MapContainKeyAction;
import top.bogey.touch_tool.bean.action.map.MapContainValueAction;
import top.bogey.touch_tool.bean.action.map.MapForeachAction;
import top.bogey.touch_tool.bean.action.map.MapGetAction;
import top.bogey.touch_tool.bean.action.map.MapGetKeysAction;
import top.bogey.touch_tool.bean.action.map.MapGetValuesAction;
import top.bogey.touch_tool.bean.action.map.MapIsEmptyAction;
import top.bogey.touch_tool.bean.action.map.MapRemoveAction;
import top.bogey.touch_tool.bean.action.map.MapSetAction;
import top.bogey.touch_tool.bean.action.map.MapSizeAction;
import top.bogey.touch_tool.bean.action.node.CheckNodeValidAction;
import top.bogey.touch_tool.bean.action.node.EditTextInputAction;
import top.bogey.touch_tool.bean.action.node.EditTextPasteAction;
import top.bogey.touch_tool.bean.action.node.FindNodeAction;
import top.bogey.touch_tool.bean.action.node.GetNodeChildrenAction;
import top.bogey.touch_tool.bean.action.node.GetNodeInfoAction;
import top.bogey.touch_tool.bean.action.node.GetNodeParentAction;
import top.bogey.touch_tool.bean.action.node.GetNodesInAreaAction;
import top.bogey.touch_tool.bean.action.node.GetWindowsAction;
import top.bogey.touch_tool.bean.action.node.IsNodeExistAction;
import top.bogey.touch_tool.bean.action.node.NodeTouchAction;
import top.bogey.touch_tool.bean.action.node.PickNodeAction;
import top.bogey.touch_tool.bean.action.normal.DelayAction;
import top.bogey.touch_tool.bean.action.normal.InputConfigAction;
import top.bogey.touch_tool.bean.action.normal.LoggerAction;
import top.bogey.touch_tool.bean.action.area.MarkAreaAction;
import top.bogey.touch_tool.bean.action.normal.ShowTextAction;
import top.bogey.touch_tool.bean.action.normal.StickCloseAction;
import top.bogey.touch_tool.bean.action.normal.StickCloseAllAction;
import top.bogey.touch_tool.bean.action.normal.StickScreenAction;
import top.bogey.touch_tool.bean.action.number.CheckNumberInValueArea;
import top.bogey.touch_tool.bean.action.number.MathExpressionAction;
import top.bogey.touch_tool.bean.action.number.NumberAbsAction;
import top.bogey.touch_tool.bean.action.number.NumberAddAction;
import top.bogey.touch_tool.bean.action.number.NumberDivAction;
import top.bogey.touch_tool.bean.action.number.NumberEqualAction;
import top.bogey.touch_tool.bean.action.number.NumberGreaterAction;
import top.bogey.touch_tool.bean.action.number.NumberLessAction;
import top.bogey.touch_tool.bean.action.number.NumberModAction;
import top.bogey.touch_tool.bean.action.number.NumberMulAction;
import top.bogey.touch_tool.bean.action.number.NumberRandomAction;
import top.bogey.touch_tool.bean.action.number.NumberSubAction;
import top.bogey.touch_tool.bean.action.number.NumberToIntegerAction;
import top.bogey.touch_tool.bean.action.number.NumberToValueArea;
import top.bogey.touch_tool.bean.action.point.PointFromIntegerAction;
import top.bogey.touch_tool.bean.action.point.PointOffsetAction;
import top.bogey.touch_tool.bean.action.point.PointToIntegerAction;
import top.bogey.touch_tool.bean.action.point.PointToTouchAction;
import top.bogey.touch_tool.bean.action.point.PointsToTouchAction;
import top.bogey.touch_tool.bean.action.point.TouchAction;
import top.bogey.touch_tool.bean.action.point.TouchPointAction;
import top.bogey.touch_tool.bean.action.start.ApplicationQuitStartAction;
import top.bogey.touch_tool.bean.action.start.ApplicationStartAction;
import top.bogey.touch_tool.bean.action.start.BatteryStartAction;
import top.bogey.touch_tool.bean.action.start.BluetoothStartAction;
import top.bogey.touch_tool.bean.action.start.BroadcastStartAction;
import top.bogey.touch_tool.bean.action.start.ManualStartAction;
import top.bogey.touch_tool.bean.action.start.NetworkStartAction;
import top.bogey.touch_tool.bean.action.start.NotificationStartAction;
import top.bogey.touch_tool.bean.action.start.OutCallStartAction;
import top.bogey.touch_tool.bean.action.start.ReceivedShareStartAction;
import top.bogey.touch_tool.bean.action.start.ScreenStartAction;
import top.bogey.touch_tool.bean.action.start.TimeStartAction;
import top.bogey.touch_tool.bean.action.string.FindOcrTextAction;
import top.bogey.touch_tool.bean.action.string.GetOcrTextAction;
import top.bogey.touch_tool.bean.action.string.IsOcrTextExistAction;
import top.bogey.touch_tool.bean.action.string.ParseJsonAction;
import top.bogey.touch_tool.bean.action.string.ParseJsonSafeAction;
import top.bogey.touch_tool.bean.action.string.StringAppendAction;
import top.bogey.touch_tool.bean.action.string.StringDecodeAction;
import top.bogey.touch_tool.bean.action.string.StringEncodeAction;
import top.bogey.touch_tool.bean.action.string.StringEncryptAction;
import top.bogey.touch_tool.bean.action.string.StringEqualAction;
import top.bogey.touch_tool.bean.action.string.StringFromObjectAction;
import top.bogey.touch_tool.bean.action.string.StringMatchAction;
import top.bogey.touch_tool.bean.action.string.StringReplaceAction;
import top.bogey.touch_tool.bean.action.string.StringSplitAction;
import top.bogey.touch_tool.bean.action.string.StringSubStringAction;
import top.bogey.touch_tool.bean.action.string.StringToNumberAction;
import top.bogey.touch_tool.bean.action.string.StringToSingleSelectAction;
import top.bogey.touch_tool.bean.action.system.CheckCaptureReadyAction;
import top.bogey.touch_tool.bean.action.system.ExecuteShellAction;
import top.bogey.touch_tool.bean.action.system.GetBatteryStatusAction;
import top.bogey.touch_tool.bean.action.system.GetBluetoothDevicesAction;
import top.bogey.touch_tool.bean.action.system.GetDateAction;
import top.bogey.touch_tool.bean.action.system.GetNetworkStatusAction;
import top.bogey.touch_tool.bean.action.system.GetScreenStatusAction;
import top.bogey.touch_tool.bean.action.system.GetTimeAction;
import top.bogey.touch_tool.bean.action.system.GetVolumeAction;
import top.bogey.touch_tool.bean.action.system.GetWifiInfoAction;
import top.bogey.touch_tool.bean.action.system.PlayRingtoneAction;
import top.bogey.touch_tool.bean.action.system.ReadFromClipboardAction;
import top.bogey.touch_tool.bean.action.system.SendNotificationAction;
import top.bogey.touch_tool.bean.action.system.SendBroadcastAction;
import top.bogey.touch_tool.bean.action.system.SendToastAction;
import top.bogey.touch_tool.bean.action.system.SetVolumeAction;
import top.bogey.touch_tool.bean.action.system.ShareToAction;
import top.bogey.touch_tool.bean.action.system.StopRingtoneAction;
import top.bogey.touch_tool.bean.action.system.SwitchCaptureAction;
import top.bogey.touch_tool.bean.action.system.SwitchScreenAction;
import top.bogey.touch_tool.bean.action.system.SystemKeyAction;
import top.bogey.touch_tool.bean.action.system.TextToSpeechAction;
import top.bogey.touch_tool.bean.action.system.VibrateAction;
import top.bogey.touch_tool.bean.action.system.WriteToClipboardAction;
import top.bogey.touch_tool.bean.action.task.CustomEndAction;
import top.bogey.touch_tool.bean.action.task.CustomStartAction;
import top.bogey.touch_tool.bean.action.task.ExecuteTaskAction;
import top.bogey.touch_tool.bean.action.task.GetAllRunningTaskAction;
import top.bogey.touch_tool.bean.action.task.IsTaskRunningAction;
import top.bogey.touch_tool.bean.action.task.StopTaskAction;
import top.bogey.touch_tool.bean.action.task.SwitchTaskAction;
import top.bogey.touch_tool.bean.action.variable.GetOrSetVariableAction;
import top.bogey.touch_tool.bean.action.variable.GetVariableAction;
import top.bogey.touch_tool.bean.action.variable.SetVariableAction;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.card.CreateListActionCard;
import top.bogey.touch_tool.ui.blueprint.card.CustomActionCard;
import top.bogey.touch_tool.ui.blueprint.card.DynamicParamsActionCard;
import top.bogey.touch_tool.ui.blueprint.card.DynamicPinsActionCard;
import top.bogey.touch_tool.ui.blueprint.card.ExecuteCustomActionCard;
import top.bogey.touch_tool.ui.blueprint.card.InputConfigActionCard;
import top.bogey.touch_tool.ui.blueprint.card.NormalActionCard;
import top.bogey.touch_tool.ui.blueprint.card.ShowTextActionCard;

public class ActionInfo {
    // 开始动作
    private final static ActionInfo MANUAL_START_INFO = new ActionInfo(ActionType.MANUAL_START, ManualStartAction.class, R.drawable.icon_waving_hand, R.string.manual_start_action, R.string.manual_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo APPLICATION_START_INFO = new ActionInfo(ActionType.APPLICATION_START, ApplicationStartAction.class, R.drawable.icon_widgets, R.string.application_start_action, R.string.application_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo APPLICATION_QUIT_START_INFO = new ActionInfo(ActionType.APPLICATION_QUIT_START, ApplicationQuitStartAction.class, R.drawable.icon_output, R.string.application_quit_start_action, R.string.application_quit_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo TIME_START_INFO = new ActionInfo(ActionType.TIME_START, TimeStartAction.class, R.drawable.icon_timer, R.string.time_start_action, R.string.time_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NOTIFICATION_START_INFO = new ActionInfo(ActionType.NOTIFICATION_START, NotificationStartAction.class, R.drawable.icon_notifications, R.string.notification_start_action, R.string.notification_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NETWORK_START_INFO = new ActionInfo(ActionType.NETWORK_START, NetworkStartAction.class, R.drawable.icon_globe, R.string.network_start_action, R.string.network_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo BATTERY_START_INFO = new ActionInfo(ActionType.BATTERY_START, BatteryStartAction.class, R.drawable.icon_battery_android_full, R.string.battery_start_action, R.string.battery_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SCREEN_START_INFO = new ActionInfo(ActionType.SCREEN_START, ScreenStartAction.class, R.drawable.icon_mobile, R.string.screen_start_action, R.string.screen_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo BLUETOOTH_START_INFO = new ActionInfo(ActionType.BLUETOOTH_START, BluetoothStartAction.class, R.drawable.icon_bluetooth, R.string.bluetooth_start_action, R.string.bluetooth_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo RECEIVED_SHARE_START_INFO = new ActionInfo(ActionType.RECEIVED_SHARE_START, ReceivedShareStartAction.class, R.drawable.icon_share, R.string.received_share_start_action, R.string.received_share_start_action_desc, 0, DynamicParamsActionCard.class);
    private final static ActionInfo BROADCAST_START_INFO = new ActionInfo(ActionType.BROADCAST_START, BroadcastStartAction.class, R.drawable.icon_share, R.string.broadcast_start_action, R.string.broadcast_start_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo OUT_CALL_START_INFO = new ActionInfo(ActionType.OUT_CALL_START, OutCallStartAction.class, R.drawable.icon_autoplay, R.string.out_call_start_action, R.string.out_call_start_action_desc, 0, DynamicParamsActionCard.class);

    // 自定义动作
    private final static ActionInfo CUSTOM_START_INFO = new ActionInfo(ActionType.CUSTOM_START, CustomStartAction.class, R.drawable.icon_output, R.string.custom_start_action, R.string.custom_start_action_desc, 0, CustomActionCard.class);
    private final static ActionInfo CUSTOM_END_INFO = new ActionInfo(ActionType.CUSTOM_END, CustomEndAction.class, R.drawable.icon_input, R.string.custom_end_action, R.string.custom_end_action_desc, 0, CustomActionCard.class);
    private final static ActionInfo EXECUTE_TASK_INFO = new ActionInfo(ActionType.EXECUTE_TASK, ExecuteTaskAction.class, R.drawable.icon_assignment, R.string.execute_task_action, 0, 0, ExecuteCustomActionCard.class);
    private final static ActionInfo STOP_TASK_INFO = new ActionInfo(ActionType.STOP_TASK, StopTaskAction.class, R.drawable.icon_stop, R.string.stop_task_action, R.string.stop_task_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SWITCH_TASK_INFO = new ActionInfo(ActionType.SWITCH_TASK, SwitchTaskAction.class, R.drawable.icon_visibility, R.string.switch_task_action, R.string.switch_task_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo IS_TASK_RUNNING_INFO = new ActionInfo(ActionType.IS_TASK_RUNNING, IsTaskRunningAction.class, R.drawable.icon_assignment, R.string.is_task_running_action, R.string.is_task_running_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_ALL_RUNNING_TASK_INFO = new ActionInfo(ActionType.GET_ALL_RUNNING_TASK, GetAllRunningTaskAction.class, R.drawable.icon_assignment, R.string.get_all_running_task_action, R.string.get_all_running_task_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_VARIABLE_INFO = new ActionInfo(ActionType.GET_VARIABLE, GetVariableAction.class, R.drawable.icon_upload, R.string.get_value_action, 0, 0, NormalActionCard.class);
    private final static ActionInfo SET_VARIABLE_INFO = new ActionInfo(ActionType.SET_VARIABLE, SetVariableAction.class, R.drawable.icon_download, R.string.set_value_action, 0, 0, NormalActionCard.class);
    private final static ActionInfo GET_OR_SET_VARIABLE_INFO = new ActionInfo(ActionType.GET_OR_SET_VARIABLE, GetOrSetVariableAction.class, R.drawable.icon_shuffle, R.string.get_or_set_value_action, 0, 0, NormalActionCard.class);


    // 逻辑动作
    private final static ActionInfo IF_LOGIC_INFO = new ActionInfo(ActionType.IF_LOGIC, IfConditionAction.class, R.drawable.icon_graph_1, R.string.if_action, R.string.if_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo WAIT_IF_LOGIC_INFO = new ActionInfo(ActionType.WAIT_IF_LOGIC, WaitConditionAction.class, R.drawable.icon_hourglass_bottom, R.string.wait_if_action, R.string.wait_if_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SWITCH_LOGIC_INFO = new ActionInfo(ActionType.SWITCH_LOGIC, SwitchAction.class, R.drawable.icon_live_help, R.string.switch_action, R.string.switch_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo CHOICE_LOGIC_INFO = new ActionInfo(ActionType.CHOICE_LOGIC, ChoiceExecuteAction.class, R.drawable.icon_person_check, R.string.choice_action, R.string.choice_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo LIST_CHOICE_LOGIC_INFO = new ActionInfo(ActionType.LIST_CHOICE, ListChoiceAction.class, R.drawable.icon_data_array, R.string.list_choice_action, R.string.list_choice_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo FOR_LOGIC_INFO = new ActionInfo(ActionType.FOR_LOGIC, ForLoopAction.class, R.drawable.icon_repeat_one, R.string.for_loop_action, R.string.for_loop_action_desc, R.string.for_loop_action_help, NormalActionCard.class);
    private final static ActionInfo WHILE_LOGIC_INFO = new ActionInfo(ActionType.WHILE_LOGIC, WhileLoopAction.class, R.drawable.icon_repeat, R.string.while_loop_action, R.string.while_loop_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SEQUENCE_LOGIC_INFO = new ActionInfo(ActionType.SEQUENCE_LOGIC, SequenceExecuteAction.class, R.drawable.icon_tactic, R.string.sequence_action, R.string.sequence_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo RANDOM_LOGIC_INFO = new ActionInfo(ActionType.RANDOM_LOGIC, RandomExecuteAction.class, R.drawable.icon_shuffle, R.string.random_action, R.string.random_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo PARALLEL_LOGIC_INFO = new ActionInfo(ActionType.PARALLEL_LOGIC, ParallelExecuteAction.class, R.drawable.icon_graph_2, R.string.parallel_action, R.string.parallel_action_desc, 0, DynamicPinsActionCard.class);


    // 通用动作
    private final static ActionInfo DELAY_INFO = new ActionInfo(ActionType.DELAY, DelayAction.class, R.drawable.icon_schedule, R.string.delay_action, R.string.delay_action_desc, R.string.delay_action_help, NormalActionCard.class);
    private final static ActionInfo LOG_INFO = new ActionInfo(ActionType.LOG, LoggerAction.class, R.drawable.icon_draw, R.string.log_action, R.string.log_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STICK_INFO = new ActionInfo(ActionType.STICK, StickScreenAction.class, R.drawable.icon_note_stack, R.string.stick_screen_action, R.string.stick_screen_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CLOSE_STICK_INFO = new ActionInfo(ActionType.CLOSE_STICK, StickCloseAction.class, R.drawable.icon_note_stack, R.string.stick_close_action, R.string.stick_close_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CLOSE_ALL_STICK_INFO = new ActionInfo(ActionType.CLOSE_ALL_STICK, StickCloseAllAction.class, R.drawable.icon_note_stack, R.string.stick_close_all_action, R.string.stick_close_all_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo INPUT_CONFIG_INFO = new ActionInfo(ActionType.INPUT_CONFIG, InputConfigAction.class, R.drawable.icon_edit, R.string.input_config_action, R.string.input_config_action_desc, 0, InputConfigActionCard.class);
    private final static ActionInfo SHOW_TEXT_INFO = new ActionInfo(ActionType.SHOW_TEXT, ShowTextAction.class, R.drawable.icon_edit, R.string.show_text_action, R.string.show_text_action_desc, 0, ShowTextActionCard.class);


    // 应用相关
    private final static ActionInfo OPEN_APP_INFO = new ActionInfo(ActionType.OPEN_APP, OpenAppAction.class, R.drawable.icon_apps, R.string.open_app_action, R.string.open_app_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo OPEN_URI_SCHEME_INFO = new ActionInfo(ActionType.OPEN_URI_SCHEME, OpenUriSchemeAction.class, R.drawable.icon_link, R.string.open_uri_scheme_action, R.string.open_uri_scheme_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo OPEN_SHORTCUT_INFO = new ActionInfo(ActionType.OPEN_SHORTCUT, OpenShortcutAction.class, R.drawable.icon_apps, R.string.open_shortcut_action, R.string.open_shortcut_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_CURRENT_APPLICATION_INFO = new ActionInfo(ActionType.GET_CURRENT_APPLICATION, GetCurrentAppAction.class, R.drawable.icon_apps, R.string.get_current_app_action, R.string.get_current_app_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CHECK_IN_APPLICATION_INFO = new ActionInfo(ActionType.CHECK_IN_APPLICATION, CheckInAppAction.class, R.drawable.icon_apps, R.string.check_in_app_action, R.string.check_in_app_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_TO_APP_INFO = new ActionInfo(ActionType.STRING_TO_APP, StringToAppAction.class, R.drawable.icon_apps, R.string.string_to_app_action, R.string.string_to_app_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo WAIT_IN_APPLICATION_INFO = new ActionInfo(ActionType.WAIT_IN_APPLICATION, WaitInAppAction.class, R.drawable.icon_hourglass_bottom, R.string.wait_in_app_action, R.string.wait_in_app_action_desc, 0, NormalActionCard.class);

    // 系统动作
    private final static ActionInfo SHELL_INFO = new ActionInfo(ActionType.SHELL, ExecuteShellAction.class, R.drawable.icon_terminal, R.string.execute_shell_action, R.string.execute_shell_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SHARE_TO_INFO = new ActionInfo(ActionType.SHARE_TO, ShareToAction.class, R.drawable.icon_share, R.string.share_to_action, R.string.share_to_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SYSTEM_KEY_INFO = new ActionInfo(ActionType.SYSTEM_KEY, SystemKeyAction.class, R.drawable.icon_keyboard, R.string.system_key_action, R.string.system_key_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo PLAY_RINGTONE_INFO = new ActionInfo(ActionType.PLAY_RINGTONE, PlayRingtoneAction.class, R.drawable.icon_notifications_active, R.string.play_ringtone_action, R.string.play_ringtone_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STOP_RINGTONE_INFO = new ActionInfo(ActionType.STOP_RINGTONE, StopRingtoneAction.class, R.drawable.icon_notifications_off, R.string.stop_ringtone_action, R.string.stop_ringtone_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo TEXT_TO_SPEECH_INFO = new ActionInfo(ActionType.TEXT_TO_SPEECH, TextToSpeechAction.class, R.drawable.icon_text_to_speech, R.string.text_to_speak_action, R.string.text_to_speak_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo VIBRATE_INFO = new ActionInfo(ActionType.VIBRATE, VibrateAction.class, R.drawable.icon_mobile_vibrate, R.string.vibrate_action, R.string.vibrate_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SEND_NOTIFICATION_INFO = new ActionInfo(ActionType.SEND_NOTIFICATION, SendNotificationAction.class, R.drawable.icon_notifications, R.string.send_notification_action, R.string.send_notification_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SEND_BROADCAST_INFO = new ActionInfo(ActionType.SEND_BROADCAST, SendBroadcastAction.class, R.drawable.icon_share, R.string.send_broadcast_action, R.string.send_broadcast_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SEND_TOAST_INFO = new ActionInfo(ActionType.SEND_TOAST, SendToastAction.class, R.drawable.icon_toast, R.string.send_toast_action, R.string.send_toast_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo WRITE_TO_CLIPBOARD_INFO = new ActionInfo(ActionType.WRITE_TO_CLIPBOARD, WriteToClipboardAction.class, R.drawable.icon_content_copy, R.string.write_to_clipboard_action, R.string.write_to_clipboard_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo READ_FROM_CLIPBOARD_INFO = new ActionInfo(ActionType.READ_FROM_CLIPBOARD, ReadFromClipboardAction.class, R.drawable.icon_content_paste, R.string.read_from_clipboard_action, R.string.read_from_clipboard_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo SWITCH_SCREEN_INFO = new ActionInfo(ActionType.SWITCH_SCREEN, SwitchScreenAction.class, R.drawable.icon_mobile, R.string.switch_screen_action, R.string.switch_screen_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_SCREEN_STATUS_INFO = new ActionInfo(ActionType.GET_SCREEN_STATUS, GetScreenStatusAction.class, R.drawable.icon_mobile, R.string.get_screen_status_action, R.string.get_screen_status_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo SWITCH_CAPTURE_INFO = new ActionInfo(ActionType.SWITCH_CAPTURE, SwitchCaptureAction.class, R.drawable.icon_screen_record, R.string.capture_switch_action, R.string.capture_switch_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CHECK_CAPTURE_READY_INFO = new ActionInfo(ActionType.CHECK_CAPTURE_READY, CheckCaptureReadyAction.class, R.drawable.icon_screen_record, R.string.check_capture_ready_action, R.string.check_capture_ready_action_desc, 0, NormalActionCard.class);


    private final static ActionInfo GET_BATTERY_STATUS_INFO = new ActionInfo(ActionType.GET_BATTERY_STATUS, GetBatteryStatusAction.class, R.drawable.icon_battery_android_full, R.string.get_battery_status_action, R.string.get_battery_status_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo GET_NETWORK_STATUS_INFO = new ActionInfo(ActionType.GET_NETWORK_STATUS, GetNetworkStatusAction.class, R.drawable.icon_globe, R.string.get_network_status_action, R.string.get_network_status_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo GET_WIFI_INFO_INFO = new ActionInfo(ActionType.GET_WIFI_INFO, GetWifiInfoAction.class, R.drawable.icon_globe, R.string.get_wifi_info_action, R.string.get_wifi_info_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo GET_BLUETOOTH_DEVICES_INFO = new ActionInfo(ActionType.GET_BLUETOOTH_DEVICES, GetBluetoothDevicesAction.class, R.drawable.icon_bluetooth, R.string.get_bluetooth_devices_action, R.string.get_bluetooth_devices_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo GET_CURRENT_DATE_INFO = new ActionInfo(ActionType.GET_CURRENT_DATE, GetDateAction.class, R.drawable.icon_calendar_month, R.string.get_date_action, R.string.get_date_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_CURRENT_TIME_INFO = new ActionInfo(ActionType.GET_CURRENT_TIME, GetTimeAction.class, R.drawable.icon_schedule, R.string.get_time_action, R.string.get_time_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_VOLUME_INFO = new ActionInfo(ActionType.GET_VOLUME, GetVolumeAction.class, R.drawable.icon_volume, R.string.get_volume_action, R.string.get_volume_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SET_VOLUME_INFO = new ActionInfo(ActionType.SET_VOLUME, SetVolumeAction.class, R.drawable.icon_volume, R.string.set_volume_action, R.string.set_volume_action_desc, 0, NormalActionCard.class);

    // 数值运算
    private final static ActionInfo NUMBER_ADD_INFO = new ActionInfo(ActionType.NUMBER_ADD, NumberAddAction.class, R.drawable.icon_add, R.string.number_add_action, R.string.number_add_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo NUMBER_SUB_INFO = new ActionInfo(ActionType.NUMBER_SUB, NumberSubAction.class, R.drawable.icon_remove, R.string.number_subtract_action, R.string.number_subtract_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_MUL_INFO = new ActionInfo(ActionType.NUMBER_MUL, NumberMulAction.class, R.drawable.icon_close, R.string.number_multiply_action, R.string.number_multiply_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo NUMBER_DIV_INFO = new ActionInfo(ActionType.NUMBER_DIV, NumberDivAction.class, R.drawable.icon_stream, R.string.number_divide_action, R.string.number_divide_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_MOD_INFO = new ActionInfo(ActionType.NUMBER_MOD, NumberModAction.class, R.drawable.icon_percent, R.string.number_mod_action, R.string.number_mod_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_ABS_INFO = new ActionInfo(ActionType.NUMBER_ABS, NumberAbsAction.class, R.drawable.icon_data_array, R.string.number_abs_action, R.string.number_abs_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_EQUAL_INFO = new ActionInfo(ActionType.NUMBER_EQUAL, NumberEqualAction.class, R.drawable.icon_equal, R.string.number_equal_action, R.string.number_equal_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_GREATER_INFO = new ActionInfo(ActionType.NUMBER_GREATER, NumberGreaterAction.class, R.drawable.icon_keyboard_arrow_right, R.string.number_greater_action, R.string.number_greater_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_LESS_INFO = new ActionInfo(ActionType.NUMBER_LESS, NumberLessAction.class, R.drawable.icon_keyboard_arrow_left, R.string.number_less_action, R.string.number_less_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_RANDOM_INFO = new ActionInfo(ActionType.NUMBER_RANDOM, NumberRandomAction.class, R.drawable.icon_shuffle, R.string.number_random_action, R.string.number_random_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_TO_INT_INFO = new ActionInfo(ActionType.NUMBER_TO_INT, NumberToIntegerAction.class, R.drawable.icon_decimal_decrease, R.string.number_to_integer_action, R.string.number_to_integer_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CHECK_NUMBER_IN_VALUE_AREA_INFO = new ActionInfo(ActionType.CHECK_NUMBER_IN_VALUE_AREA, CheckNumberInValueArea.class, R.drawable.icon_straighten, R.string.check_number_in_value_area_action, R.string.check_number_in_value_area_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo NUMBER_TO_VALUE_AREA_INFO = new ActionInfo(ActionType.NUMBER_TO_VALUE_AREA, NumberToValueArea.class, R.drawable.icon_straighten, R.string.number_to_value_area_action, R.string.number_to_value_area_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MATH_EXPRESSION_INFO = new ActionInfo(ActionType.MATH_EXPRESSION, MathExpressionAction.class, R.drawable.icon_function, R.string.math_expression_action, R.string.math_expression_action_desc, 0, NormalActionCard.class);


    // 文本处理
    private final static ActionInfo STRING_FROM_OBJECT_INFO = new ActionInfo(ActionType.STRING_FROM_OBJECT, StringFromObjectAction.class, R.drawable.icon_text_fields, R.string.string_from_object_action, R.string.string_from_object_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_TO_NUMBER_INFO = new ActionInfo(ActionType.STRING_TO_NUMBER, StringToNumberAction.class, R.drawable.icon_123, R.string.string_to_number_action, R.string.string_to_number_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_APPEND_INFO = new ActionInfo(ActionType.STRING_APPEND, StringAppendAction.class, R.drawable.icon_add, R.string.string_append_action, R.string.string_append_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo STRING_SUBSTRING_INFO = new ActionInfo(ActionType.STRING_SUBSTRING, StringSubStringAction.class, R.drawable.icon_split_scene, R.string.string_substring_action, R.string.string_substring_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_REGEX_INFO = new ActionInfo(ActionType.STRING_REGEX, StringMatchAction.class, R.drawable.icon_regular_expression, R.string.string_match_action, R.string.string_match_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_SPLIT_INFO = new ActionInfo(ActionType.STRING_SPLIT, StringSplitAction.class, R.drawable.icon_data_array, R.string.string_split_action, R.string.string_split_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_EQUAL_INFO = new ActionInfo(ActionType.STRING_EQUAL, StringEqualAction.class, R.drawable.icon_equal, R.string.string_equal_action, R.string.string_equal_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_REPLACE_INFO = new ActionInfo(ActionType.STRING_REPLACE, StringReplaceAction.class, R.drawable.icon_swap_horiz, R.string.string_replace_action, R.string.string_replace_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_ENCODE_INFO = new ActionInfo(ActionType.STRING_ENCODE, StringEncodeAction.class, R.drawable.icon_input, R.string.string_encode_action, R.string.string_encode_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_DECODE_INFO = new ActionInfo(ActionType.STRING_DECODE, StringDecodeAction.class, R.drawable.icon_output, R.string.string_decode_action, R.string.string_decode_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_ENCRYPT_INFO = new ActionInfo(ActionType.STRING_ENCRYPT, StringEncryptAction.class, R.drawable.icon_input, R.string.string_encrypt_action, R.string.string_encrypt_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_OCR_TEXT_INFO = new ActionInfo(ActionType.GET_OCR_TEXT, GetOcrTextAction.class, R.drawable.icon_document_scanner, R.string.get_ocr_text_action, R.string.get_ocr_text_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo IS_OCR_TEXT_EXIST_INFO = new ActionInfo(ActionType.IS_OCR_TEXT_EXIST, IsOcrTextExistAction.class, R.drawable.icon_document_scanner, R.string.is_ocr_text_exist_action, R.string.is_ocr_text_exist_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo FIND_OCR_TEXT_INFO = new ActionInfo(ActionType.FIND_OCR_TEXT, FindOcrTextAction.class, R.drawable.icon_document_scanner, R.string.find_ocr_text_action, R.string.find_ocr_text_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo PARSE_JSON_INFO = new ActionInfo(ActionType.PARSE_JSON, ParseJsonAction.class, R.drawable.icon_text_fields, R.string.parse_json_action, R.string.parse_json_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo PARSE_JSON_SAFE_INFO = new ActionInfo(ActionType.PARSE_JSON_SAFE, ParseJsonSafeAction.class, R.drawable.icon_text_fields, R.string.parse_json_safe_action, R.string.parse_json_safe_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo BOX_VALUE_INFO = new ActionInfo(ActionType.BOX_VALUE, BoxValueAction.class, R.drawable.icon_output, R.string.box_value_action, R.string.box_value_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo UNBOX_VALUE_INFO = new ActionInfo(ActionType.UNBOX_VALUE, UnboxValueAction.class, R.drawable.icon_input, R.string.unbox_value_action, R.string.unbox_value_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_VALUE_TYPE_INFO = new ActionInfo(ActionType.GET_VALUE_TYPE, GetValueTypeAction.class, R.drawable.icon_help, R.string.get_value_type_action, R.string.get_value_type_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_VALUE_ITEM_INFO = new ActionInfo(ActionType.GET_VALUE_ITEM, GetValueItemAction.class, R.drawable.icon_search, R.string.get_value_item_action, R.string.get_value_item_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo BUILD_OBJECT_INFO = new ActionInfo(ActionType.BUILD_OBJECT, BuildObjectAction.class, R.drawable.icon_add, R.string.build_object_action, R.string.build_object_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo STRING_TO_SINGLE_SELECT_INFO = new ActionInfo(ActionType.STRING_TO_SINGLE_SELECT, StringToSingleSelectAction.class, R.drawable.icon_text_fields, R.string.string_to_single_select_action, R.string.string_to_single_select_action_desc, 0, NormalActionCard.class);


    // 条件判断
    private final static ActionInfo BOOLEAN_OR_INFO = new ActionInfo(ActionType.BOOLEAN_OR, BooleanOrAction.class, R.drawable.icon_repeat_one, R.string.boolean_or_action, R.string.boolean_or_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo BOOLEAN_AND_INFO = new ActionInfo(ActionType.BOOLEAN_AND, BooleanAndAction.class, R.drawable.icon_repeat, R.string.boolean_and_action, R.string.boolean_and_action_desc, 0, DynamicPinsActionCard.class);
     private final static ActionInfo BOOLEAN_NOT_INFO = new ActionInfo(ActionType.BOOLEAN_NOT, BooleanNotAction.class, R.drawable.icon_swap_horiz, R.string.boolean_not_action, R.string.boolean_not_action_desc, 0, NormalActionCard.class);
     private final static ActionInfo BOOLEAN_AND_SHORT_INFO = new ActionInfo(ActionType.BOOLEAN_AND_SHORT, BooleanAndShortCircuitAction.class, R.drawable.icon_repeat, R.string.boolean_and_short_action, R.string.boolean_and_short_action_desc, 0, DynamicPinsActionCard.class);
     private final static ActionInfo BOOLEAN_OR_SHORT_INFO = new ActionInfo(ActionType.BOOLEAN_OR_SHORT, BooleanOrShortCircuitAction.class, R.drawable.icon_repeat_one, R.string.boolean_or_short_action, R.string.boolean_or_short_action_desc, 0, DynamicPinsActionCard.class);


    // 控件操作
    private final static ActionInfo FIND_NODE_BY_PATH_INFO = new ActionInfo(ActionType.FIND_NODE, FindNodeAction.class, R.drawable.icon_widgets, R.string.find_node_action, R.string.find_node_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo IS_NODE_EXIST_BY_PATH_INFO = new ActionInfo(ActionType.IS_NODE_EXIST, IsNodeExistAction.class, R.drawable.icon_check, R.string.is_node_exist_action, R.string.is_node_exist_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo FIND_NODES_IN_AREA_INFO = new ActionInfo(ActionType.GET_NODES_IN_AREA, GetNodesInAreaAction.class, R.drawable.icon_widgets, R.string.find_nodes_in_area_action, R.string.find_nodes_in_area_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_NODE_INFO_INFO = new ActionInfo(ActionType.GET_NODE_INFO, GetNodeInfoAction.class, R.drawable.icon_info, R.string.get_node_info_action, R.string.get_node_info_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_NODE_CHILDREN_INFO = new ActionInfo(ActionType.GET_NODE_CHILDREN, GetNodeChildrenAction.class, R.drawable.icon_widgets, R.string.get_node_children_action, R.string.get_node_children_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_NODE_PARENT_INFO = new ActionInfo(ActionType.GET_NODE_PARENT, GetNodeParentAction.class, R.drawable.icon_widgets, R.string.get_node_parent_action, R.string.get_node_parent_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_WINDOWS_INFO = new ActionInfo(ActionType.GET_WINDOWS, GetWindowsAction.class, R.drawable.icon_mobile, R.string.get_windows_action, R.string.get_windows_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CHECK_NODE_VALID_INFO = new ActionInfo(ActionType.CHECK_NODE_VALID, CheckNodeValidAction.class, R.drawable.icon_check, R.string.check_node_valid_action, R.string.check_node_valid_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo NODE_TOUCH_INFO = new ActionInfo(ActionType.NODE_TOUCH, NodeTouchAction.class, R.drawable.icon_touch_app, R.string.node_touch_action, R.string.node_touch_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo EDITTEXT_INPUT_INFO = new ActionInfo(ActionType.EDITTEXT_INPUT, EditTextInputAction.class, R.drawable.icon_edit, R.string.edit_text_input_action, R.string.edit_text_input_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo EDITTEXT_PASTE_INFO = new ActionInfo(ActionType.EDITTEXT_PASTE, EditTextPasteAction.class, R.drawable.icon_content_paste, R.string.edit_text_paste_action, R.string.edit_text_paste_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo PICK_NODE_INFO = new ActionInfo(ActionType.PICK_NODE, PickNodeAction.class, R.drawable.icon_widgets, R.string.pick_node_action, R.string.pick_node_action_desc, 0, NormalActionCard.class);


    // 图片与颜色操作
    private final static ActionInfo GET_IMAGE_INFO = new ActionInfo(ActionType.GET_IMAGE, GetImageAction.class, R.drawable.icon_image, R.string.get_image_action, R.string.get_image_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LOAD_IMAGE_INFO = new ActionInfo(ActionType.LOAD_IMAGE, LoadImageAction.class, R.drawable.icon_image, R.string.load_image_action, R.string.load_image_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CROP_IMAGE_INFO = new ActionInfo(ActionType.CROP_IMAGE, CropImageAction.class, R.drawable.icon_crop, R.string.crop_image_action, R.string.crop_image_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo RESIZE_IMAGE_INFO = new ActionInfo(ActionType.RESIZE_IMAGE, ResizeImageAction.class, R.drawable.icon_area, R.string.resize_image_action, R.string.resize_image_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo SAVE_IMAGE_INFO = new ActionInfo(ActionType.SAVE_IMAGE, SaveImageAction.class, R.drawable.icon_save, R.string.save_image_action, R.string.save_image_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo FIND_IMAGE_INFO = new ActionInfo(ActionType.FIND_IMAGE, FindImageAction.class, R.drawable.icon_visibility, R.string.find_image_action, R.string.find_image_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo FIND_IMAGES_INFO = new ActionInfo(ActionType.FIND_IMAGES, FindImagesAction.class, R.drawable.icon_visibility, R.string.find_images_action, R.string.find_images_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo IS_IMAGE_EXIST_INFO = new ActionInfo(ActionType.IS_IMAGE_EXIST, IsImageExistAction.class, R.drawable.icon_check, R.string.is_image_exist_action, R.string.is_image_exist_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo TOUCH_IMAGE_INFO = new ActionInfo(ActionType.TOUCH_IMAGE, TouchImageAction.class, R.drawable.icon_touch_app, R.string.touch_image_action, R.string.touch_image_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CREATE_QRCODE_INFO = new ActionInfo(ActionType.CREATE_QRCODE, CreateQRCodeAction.class, R.drawable.icon_qr_code, R.string.create_qrcode_action, R.string.create_qrcode_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo PARSE_QRCODE_INFO = new ActionInfo(ActionType.PARSE_QRCODE, ParseQRCodeAction.class, R.drawable.icon_qr_code_scanner, R.string.parse_qrcode_action, R.string.parse_qrcode_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo YOLO_DETECT_INFO = new ActionInfo(ActionType.YOLO_DETECT, YoloDetectAction.class, R.drawable.icon_detection_and_zone, R.string.yolo_detect_action, R.string.yolo_detect_action_desc, 0, NormalActionCard.class);

    private final static ActionInfo GET_COLOR_INFO = new ActionInfo(ActionType.GET_COLOR, GetColorAction.class, R.drawable.icon_palette, R.string.get_color_action, R.string.get_color_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo FIND_COLORS_INFO = new ActionInfo(ActionType.FIND_COLORS, FindColorsAction.class, R.drawable.icon_visibility, R.string.find_colors_action, R.string.find_colors_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo IS_COLOR_EXIST_INFO = new ActionInfo(ActionType.IS_COLOR_EXIST, IsColorExistAction.class, R.drawable.icon_check, R.string.is_color_exist_action, R.string.is_color_exist_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo COLOR_EQUAL_INFO = new ActionInfo(ActionType.COLOR_EQUAL, ColorEqualAction.class, R.drawable.icon_equal, R.string.color_equal_action, R.string.color_equal_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo TOUCH_COLOR_INFO = new ActionInfo(ActionType.TOUCH_COLOR, TouchColorAction.class, R.drawable.icon_touch_app, R.string.touch_color_action, R.string.touch_color_action_desc, 0, NormalActionCard.class);


    // 区域操作
    private final static ActionInfo AREA_TO_INT_INFO = new ActionInfo(ActionType.AREA_TO_INT, AreaToIntegerAction.class, R.drawable.icon_123, R.string.area_to_integer_action, R.string.area_to_integer_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo AREA_FROM_INT_INFO = new ActionInfo(ActionType.AREA_FROM_INT, AreaFromIntegerAction.class, R.drawable.icon_area, R.string.area_from_integer_action, R.string.area_from_integer_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo AREA_OFFSET_INFO = new ActionInfo(ActionType.AREA_OFFSET, AreaOffsetAction.class, R.drawable.icon_my_location, R.string.area_offset_action, R.string.area_offset_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CHECK_AREA_CONTAIN_POS_INFO = new ActionInfo(ActionType.CHECK_AREA_CONTAIN_POS, CheckAreaContainPosAction.class, R.drawable.icon_my_location, R.string.check_area_contain_pos_action, R.string.check_area_contain_pos_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo CHECK_AREA_RELATION_INFO = new ActionInfo(ActionType.CHECK_AREA_RELATION, CheckAreaRelationAction.class, R.drawable.icon_crop, R.string.check_area_relation_action, R.string.check_area_relation_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_AREA_INTERSECTION_INFO = new ActionInfo(ActionType.GET_AREA_INTERSECTION, GetAreaIntersectionAction.class, R.drawable.icon_join_inner, R.string.get_area_intersection_action, R.string.get_area_intersection_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_AREA_CENTER_INFO = new ActionInfo(ActionType.GET_AREA_CENTER, GetAreaCenterAction.class, R.drawable.icon_my_location, R.string.get_area_center_action, R.string.get_area_center_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo GET_AREA_RANDOM_INFO = new ActionInfo(ActionType.GET_AREA_RANDOM, GetAreaRandomAction.class, R.drawable.icon_area, R.string.get_area_random_action, R.string.get_area_random_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo PICK_AREA_INFO = new ActionInfo(ActionType.PICK_AREA, PickAreaAction.class, R.drawable.icon_area, R.string.pick_area_action, R.string.pick_area_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MARK_AREA_INFO = new ActionInfo(ActionType.MARK_AREA, MarkAreaAction.class, R.drawable.icon_area, R.string.mark_area_action, R.string.mark_area_action_desc, 0, NormalActionCard.class);


    // 位置操作
    private final static ActionInfo POINT_TO_INT_INFO = new ActionInfo(ActionType.POINT_TO_INT, PointToIntegerAction.class, R.drawable.icon_123, R.string.point_to_integer_action, R.string.point_to_integer_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo POINT_FROM_INT_INFO = new ActionInfo(ActionType.POINT_FROM_INT, PointFromIntegerAction.class, R.drawable.icon_my_location, R.string.point_from_integer_action, R.string.point_from_integer_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo POINT_OFFSET_INFO = new ActionInfo(ActionType.POINT_OFFSET, PointOffsetAction.class, R.drawable.icon_my_location, R.string.point_offset_action, R.string.point_offset_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo POINT_TO_TOUCH_INFO = new ActionInfo(ActionType.POINT_TO_TOUCH, PointToTouchAction.class, R.drawable.icon_gesture, R.string.point_to_touch_action, R.string.point_to_touch_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo POINTS_TO_TOUCH_INFO = new ActionInfo(ActionType.POINTS_TO_TOUCH, PointsToTouchAction.class, R.drawable.icon_gesture, R.string.points_to_touch_action, R.string.points_to_touch_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo TOUCH_INFO = new ActionInfo(ActionType.TOUCH, TouchAction.class, R.drawable.icon_gesture, R.string.touch_action, R.string.touch_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo TOUCH_POINT_INFO = new ActionInfo(ActionType.TOUCH_POINT, TouchPointAction.class, R.drawable.icon_touch_app, R.string.touch_point_action, R.string.touch_point_action_desc, 0, NormalActionCard.class);


    // List操作
    private final static ActionInfo LIST_MAKE_INFO = new ActionInfo(ActionType.LIST_MAKE, MakeListAction.class, R.drawable.icon_data_array, R.string.list_make_action, R.string.list_make_action_desc, 0, CreateListActionCard.class);
    private final static ActionInfo LIST_SIZE_INFO = new ActionInfo(ActionType.LIST_SIZE, ListSizeAction.class, R.drawable.icon_straighten, R.string.list_size_action, R.string.list_size_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_IS_EMPTY_INFO = new ActionInfo(ActionType.LIST_IS_EMPTY, ListIsEmptyAction.class, R.drawable.icon_data_array, R.string.list_is_empty_action, R.string.list_is_empty_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_CONTAIN_INFO = new ActionInfo(ActionType.LIST_CONTAIN, ListContainAction.class, R.drawable.icon_check, R.string.list_contain_action, R.string.list_contain_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_ADD_INFO = new ActionInfo(ActionType.LIST_ADD, ListAddAction.class, R.drawable.icon_add, R.string.list_add_action, R.string.list_add_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_REMOVE_INFO = new ActionInfo(ActionType.LIST_REMOVE, ListRemoveAction.class, R.drawable.icon_remove, R.string.list_remove_action, R.string.list_remove_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_REMOVE_OBJECT_INFO = new ActionInfo(ActionType.LIST_REMOVE_OBJECT, ListRemoveObjectAction.class, R.drawable.icon_remove, R.string.list_remove_object_action, R.string.list_remove_object_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_APPEND_INFO = new ActionInfo(ActionType.LIST_APPEND, ListAppendAction.class, R.drawable.icon_add, R.string.list_append_action, R.string.list_append_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo LIST_CLEAR_INFO = new ActionInfo(ActionType.LIST_CLEAR, ListClearAction.class, R.drawable.icon_delete, R.string.list_clear_action, R.string.list_clear_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_GET_INFO = new ActionInfo(ActionType.LIST_GET, ListGetAction.class, R.drawable.icon_upload, R.string.list_get_action, R.string.list_get_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_SET_INFO = new ActionInfo(ActionType.LIST_SET, ListSetAction.class, R.drawable.icon_download, R.string.list_set_action, R.string.list_set_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_INDEX_OF_INFO = new ActionInfo(ActionType.LIST_INDEX_OF, ListIndexOfAction.class, R.drawable.icon_123, R.string.list_index_of_action, R.string.list_index_of_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_SUBLIST_INFO = new ActionInfo(ActionType.LIST_SUBLIST, ListSubListAction.class, R.drawable.icon_split_scene, R.string.list_sublist_action, R.string.list_sublist_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_FOREACH_INFO = new ActionInfo(ActionType.LIST_FOREACH, ListForeachAction.class, R.drawable.icon_repeat, R.string.list_foreach_action, R.string.list_foreach_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_SORT_INFO = new ActionInfo(ActionType.LIST_SORT, ListSortAction.class, R.drawable.icon_swap_horiz, R.string.list_sort_action, R.string.list_sort_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo LIST_REVERSE_INFO = new ActionInfo(ActionType.LIST_REVERSE, ListReverseAction.class, R.drawable.icon_swap_horiz, R.string.list_reverse_action, R.string.list_reverse_action_desc, 0, NormalActionCard.class);


    // Map操作
    private final static ActionInfo MAP_MAKE_INFO = new ActionInfo(ActionType.MAP_MAKE, MakeMapAction.class, R.drawable.icon_map, R.string.map_make_action, R.string.map_make_action_desc, 0, CreateListActionCard.class);
    private final static ActionInfo MAP_SIZE_INFO = new ActionInfo(ActionType.MAP_SIZE, MapSizeAction.class, R.drawable.icon_straighten, R.string.map_size_action, R.string.map_size_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_IS_EMPTY_INFO = new ActionInfo(ActionType.MAP_IS_EMPTY, MapIsEmptyAction.class, R.drawable.icon_check, R.string.map_is_empty_action, R.string.map_is_empty_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_CONTAIN_KEY_INFO = new ActionInfo(ActionType.MAP_CONTAIN_KEY, MapContainKeyAction.class, R.drawable.icon_check, R.string.map_contain_key_action, R.string.map_contain_key_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_CONTAIN_VALUE_INFO = new ActionInfo(ActionType.MAP_CONTAIN_VALUE, MapContainValueAction.class, R.drawable.icon_check, R.string.map_contain_value_action, R.string.map_contain_value_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_SET_INFO = new ActionInfo(ActionType.MAP_SET, MapSetAction.class, R.drawable.icon_add, R.string.map_set_action, R.string.map_set_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_REMOVE_INFO = new ActionInfo(ActionType.MAP_REMOVE, MapRemoveAction.class, R.drawable.icon_remove, R.string.map_remove_action, R.string.map_remove_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_APPEND_INFO = new ActionInfo(ActionType.MAP_APPEND, MapAppendAction.class, R.drawable.icon_add, R.string.map_append_action, R.string.map_append_action_desc, 0, DynamicPinsActionCard.class);
    private final static ActionInfo MAP_CLEAR_INFO = new ActionInfo(ActionType.MAP_CLEAR, MapClearAction.class, R.drawable.icon_delete, R.string.map_clear_action, R.string.map_clear_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_GET_INFO = new ActionInfo(ActionType.MAP_GET, MapGetAction.class, R.drawable.icon_upload, R.string.map_get_action, R.string.map_get_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_KEYS_INFO = new ActionInfo(ActionType.MAP_KEYS, MapGetKeysAction.class, R.drawable.icon_data_array, R.string.map_keys_action, R.string.map_keys_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_VALUES_INFO = new ActionInfo(ActionType.MAP_VALUES, MapGetValuesAction.class, R.drawable.icon_data_array, R.string.map_values_action, R.string.map_values_action_desc, 0, NormalActionCard.class);
    private final static ActionInfo MAP_FOREACH_INFO = new ActionInfo(ActionType.MAP_FOREACH, MapForeachAction.class, R.drawable.icon_repeat, R.string.map_foreach_action, R.string.map_foreach_action_desc, 0, NormalActionCard.class);

    public static ActionInfo getActionInfo(ActionType type) {
        return switch (type) {
            case MANUAL_START -> MANUAL_START_INFO;
            case APPLICATION_START -> APPLICATION_START_INFO;
            case APPLICATION_QUIT_START -> APPLICATION_QUIT_START_INFO;
            case TIME_START -> TIME_START_INFO;
            case NOTIFICATION_START -> NOTIFICATION_START_INFO;
            case NETWORK_START -> NETWORK_START_INFO;
            case BATTERY_START -> BATTERY_START_INFO;
            case SCREEN_START -> SCREEN_START_INFO;
            case BLUETOOTH_START -> BLUETOOTH_START_INFO;
            case RECEIVED_SHARE_START -> RECEIVED_SHARE_START_INFO;
            case BROADCAST_START -> BROADCAST_START_INFO;
            case OUT_CALL_START -> OUT_CALL_START_INFO;

            case CUSTOM_START -> CUSTOM_START_INFO;
            case CUSTOM_END -> CUSTOM_END_INFO;
            case EXECUTE_TASK -> EXECUTE_TASK_INFO;
            case STOP_TASK -> STOP_TASK_INFO;
            case SWITCH_TASK -> SWITCH_TASK_INFO;
            case IS_TASK_RUNNING -> IS_TASK_RUNNING_INFO;
            case GET_ALL_RUNNING_TASK -> GET_ALL_RUNNING_TASK_INFO;
            case GET_VARIABLE -> GET_VARIABLE_INFO;
            case SET_VARIABLE -> SET_VARIABLE_INFO;
            case GET_OR_SET_VARIABLE -> GET_OR_SET_VARIABLE_INFO;

            case IF_LOGIC -> IF_LOGIC_INFO;
            case WAIT_IF_LOGIC -> WAIT_IF_LOGIC_INFO;
            case SWITCH_LOGIC -> SWITCH_LOGIC_INFO;
            case CHOICE_LOGIC -> CHOICE_LOGIC_INFO;
            case LIST_CHOICE -> LIST_CHOICE_LOGIC_INFO;
            case FOR_LOGIC -> FOR_LOGIC_INFO;
            case WHILE_LOGIC -> WHILE_LOGIC_INFO;
            case SEQUENCE_LOGIC -> SEQUENCE_LOGIC_INFO;
            case RANDOM_LOGIC -> RANDOM_LOGIC_INFO;
            case PARALLEL_LOGIC -> PARALLEL_LOGIC_INFO;


            case DELAY -> DELAY_INFO;
            case LOG -> LOG_INFO;
            case STICK -> STICK_INFO;
            case CLOSE_STICK -> CLOSE_STICK_INFO;
            case CLOSE_ALL_STICK -> CLOSE_ALL_STICK_INFO;
            case INPUT_CONFIG -> INPUT_CONFIG_INFO;
            case SHOW_TEXT -> SHOW_TEXT_INFO;


            case OPEN_APP -> OPEN_APP_INFO;
            case OPEN_URI_SCHEME -> OPEN_URI_SCHEME_INFO;
            case OPEN_SHORTCUT -> OPEN_SHORTCUT_INFO;
            case GET_CURRENT_APPLICATION -> GET_CURRENT_APPLICATION_INFO;
            case CHECK_IN_APPLICATION -> CHECK_IN_APPLICATION_INFO;
            case STRING_TO_APP -> STRING_TO_APP_INFO;
            case WAIT_IN_APPLICATION -> WAIT_IN_APPLICATION_INFO;


            case SHELL -> SHELL_INFO;
            case SHARE_TO -> SHARE_TO_INFO;
            case SYSTEM_KEY -> SYSTEM_KEY_INFO;

            case PLAY_RINGTONE -> PLAY_RINGTONE_INFO;
            case STOP_RINGTONE -> STOP_RINGTONE_INFO;
            case TEXT_TO_SPEECH -> TEXT_TO_SPEECH_INFO;
            case VIBRATE -> VIBRATE_INFO;
            case SEND_NOTIFICATION -> SEND_NOTIFICATION_INFO;
            case SEND_BROADCAST -> SEND_BROADCAST_INFO;
            case SEND_TOAST -> SEND_TOAST_INFO;

            case WRITE_TO_CLIPBOARD -> WRITE_TO_CLIPBOARD_INFO;
            case READ_FROM_CLIPBOARD -> READ_FROM_CLIPBOARD_INFO;

            case SWITCH_SCREEN -> SWITCH_SCREEN_INFO;
            case GET_SCREEN_STATUS -> GET_SCREEN_STATUS_INFO;

            case SWITCH_CAPTURE -> SWITCH_CAPTURE_INFO;
            case CHECK_CAPTURE_READY -> CHECK_CAPTURE_READY_INFO;


            case GET_BATTERY_STATUS -> GET_BATTERY_STATUS_INFO;

            case GET_NETWORK_STATUS -> GET_NETWORK_STATUS_INFO;
            case GET_WIFI_INFO -> GET_WIFI_INFO_INFO;

            case GET_BLUETOOTH_DEVICES -> GET_BLUETOOTH_DEVICES_INFO;

            case GET_CURRENT_DATE -> GET_CURRENT_DATE_INFO;
            case GET_CURRENT_TIME -> GET_CURRENT_TIME_INFO;

            case GET_VOLUME -> GET_VOLUME_INFO;
            case SET_VOLUME -> SET_VOLUME_INFO;


            case NUMBER_ADD -> NUMBER_ADD_INFO;
            case NUMBER_SUB -> NUMBER_SUB_INFO;
            case NUMBER_MUL -> NUMBER_MUL_INFO;
            case NUMBER_DIV -> NUMBER_DIV_INFO;
            case NUMBER_MOD -> NUMBER_MOD_INFO;
            case NUMBER_ABS -> NUMBER_ABS_INFO;
            case NUMBER_EQUAL -> NUMBER_EQUAL_INFO;
            case NUMBER_LESS -> NUMBER_LESS_INFO;
            case NUMBER_GREATER -> NUMBER_GREATER_INFO;
            case NUMBER_RANDOM -> NUMBER_RANDOM_INFO;
            case NUMBER_TO_INT -> NUMBER_TO_INT_INFO;

            case CHECK_NUMBER_IN_VALUE_AREA -> CHECK_NUMBER_IN_VALUE_AREA_INFO;
            case NUMBER_TO_VALUE_AREA -> NUMBER_TO_VALUE_AREA_INFO;

            case MATH_EXPRESSION -> MATH_EXPRESSION_INFO;


            case STRING_FROM_OBJECT -> STRING_FROM_OBJECT_INFO;
            case STRING_TO_NUMBER -> STRING_TO_NUMBER_INFO;
            case STRING_APPEND -> STRING_APPEND_INFO;
            case STRING_SUBSTRING -> STRING_SUBSTRING_INFO;
            case STRING_REGEX -> STRING_REGEX_INFO;
            case STRING_SPLIT -> STRING_SPLIT_INFO;
            case STRING_EQUAL -> STRING_EQUAL_INFO;
            case STRING_REPLACE -> STRING_REPLACE_INFO;
            case STRING_ENCODE -> STRING_ENCODE_INFO;
            case STRING_DECODE -> STRING_DECODE_INFO;
            case STRING_ENCRYPT -> STRING_ENCRYPT_INFO;
            case GET_OCR_TEXT -> GET_OCR_TEXT_INFO;
            case FIND_OCR_TEXT -> FIND_OCR_TEXT_INFO;
            case IS_OCR_TEXT_EXIST -> IS_OCR_TEXT_EXIST_INFO;
            case PARSE_JSON -> PARSE_JSON_INFO;
            case PARSE_JSON_SAFE -> PARSE_JSON_SAFE_INFO;
            case BOX_VALUE -> BOX_VALUE_INFO;
            case UNBOX_VALUE -> UNBOX_VALUE_INFO;
            case GET_VALUE_TYPE -> GET_VALUE_TYPE_INFO;
            case GET_VALUE_ITEM -> GET_VALUE_ITEM_INFO;
            case BUILD_OBJECT -> BUILD_OBJECT_INFO;
            case STRING_TO_SINGLE_SELECT -> STRING_TO_SINGLE_SELECT_INFO;


            case BOOLEAN_OR -> BOOLEAN_OR_INFO;
            case BOOLEAN_AND -> BOOLEAN_AND_INFO;
            case BOOLEAN_OR_SHORT -> BOOLEAN_OR_SHORT_INFO;
            case BOOLEAN_AND_SHORT -> BOOLEAN_AND_SHORT_INFO;
            case BOOLEAN_NOT -> BOOLEAN_NOT_INFO;


            case FIND_NODE -> FIND_NODE_BY_PATH_INFO;
            case IS_NODE_EXIST -> IS_NODE_EXIST_BY_PATH_INFO;
            case GET_NODES_IN_AREA -> FIND_NODES_IN_AREA_INFO;
            case GET_NODE_INFO -> GET_NODE_INFO_INFO;
            case GET_NODE_CHILDREN -> GET_NODE_CHILDREN_INFO;
            case GET_NODE_PARENT -> GET_NODE_PARENT_INFO;
            case GET_WINDOWS -> GET_WINDOWS_INFO;
            case CHECK_NODE_VALID -> CHECK_NODE_VALID_INFO;

            case NODE_TOUCH -> NODE_TOUCH_INFO;
            case EDITTEXT_INPUT -> EDITTEXT_INPUT_INFO;
            case EDITTEXT_PASTE -> EDITTEXT_PASTE_INFO;
            case PICK_NODE -> PICK_NODE_INFO;


            case GET_IMAGE -> GET_IMAGE_INFO;
            case LOAD_IMAGE -> LOAD_IMAGE_INFO;
            case CROP_IMAGE -> CROP_IMAGE_INFO;
            case RESIZE_IMAGE -> RESIZE_IMAGE_INFO;
            case SAVE_IMAGE -> SAVE_IMAGE_INFO;
            case FIND_IMAGE -> FIND_IMAGE_INFO;
            case FIND_IMAGES -> FIND_IMAGES_INFO;
            case IS_IMAGE_EXIST -> IS_IMAGE_EXIST_INFO;
            case TOUCH_IMAGE -> TOUCH_IMAGE_INFO;
            case CREATE_QRCODE -> CREATE_QRCODE_INFO;
            case PARSE_QRCODE -> PARSE_QRCODE_INFO;
            case YOLO_DETECT -> YOLO_DETECT_INFO;


            case GET_COLOR -> GET_COLOR_INFO;
            case FIND_COLORS -> FIND_COLORS_INFO;
            case IS_COLOR_EXIST -> IS_COLOR_EXIST_INFO;
            case COLOR_EQUAL -> COLOR_EQUAL_INFO;
            case TOUCH_COLOR -> TOUCH_COLOR_INFO;


            case AREA_TO_INT -> AREA_TO_INT_INFO;
            case AREA_FROM_INT -> AREA_FROM_INT_INFO;
            case AREA_OFFSET -> AREA_OFFSET_INFO;
            case CHECK_AREA_CONTAIN_POS -> CHECK_AREA_CONTAIN_POS_INFO;
            case CHECK_AREA_RELATION -> CHECK_AREA_RELATION_INFO;
            case GET_AREA_INTERSECTION -> GET_AREA_INTERSECTION_INFO;
            case GET_AREA_CENTER -> GET_AREA_CENTER_INFO;
            case GET_AREA_RANDOM -> GET_AREA_RANDOM_INFO;
            case PICK_AREA -> PICK_AREA_INFO;
            case MARK_AREA -> MARK_AREA_INFO;


            case POINT_FROM_INT -> POINT_FROM_INT_INFO;
            case POINT_TO_INT -> POINT_TO_INT_INFO;
            case POINT_OFFSET -> POINT_OFFSET_INFO;
            case POINT_TO_TOUCH -> POINT_TO_TOUCH_INFO;
            case POINTS_TO_TOUCH -> POINTS_TO_TOUCH_INFO;
            case TOUCH -> TOUCH_INFO;
            case TOUCH_POINT -> TOUCH_POINT_INFO;


            case LIST_MAKE -> LIST_MAKE_INFO;
            case LIST_SIZE -> LIST_SIZE_INFO;
            case LIST_IS_EMPTY -> LIST_IS_EMPTY_INFO;
            case LIST_CONTAIN -> LIST_CONTAIN_INFO;
            case LIST_ADD -> LIST_ADD_INFO;
            case LIST_REMOVE -> LIST_REMOVE_INFO;
            case LIST_REMOVE_OBJECT -> LIST_REMOVE_OBJECT_INFO;
            case LIST_APPEND -> LIST_APPEND_INFO;
            case LIST_CLEAR -> LIST_CLEAR_INFO;
            case LIST_GET -> LIST_GET_INFO;
            case LIST_SET -> LIST_SET_INFO;
            case LIST_INDEX_OF -> LIST_INDEX_OF_INFO;
            case LIST_SUBLIST -> LIST_SUBLIST_INFO;
            case LIST_FOREACH -> LIST_FOREACH_INFO;
            case LIST_SORT -> LIST_SORT_INFO;
            case LIST_REVERSE -> LIST_REVERSE_INFO;


            case MAP_MAKE -> MAP_MAKE_INFO;
            case MAP_SIZE -> MAP_SIZE_INFO;
            case MAP_IS_EMPTY -> MAP_IS_EMPTY_INFO;
            case MAP_CONTAIN_KEY -> MAP_CONTAIN_KEY_INFO;
            case MAP_CONTAIN_VALUE -> MAP_CONTAIN_VALUE_INFO;
            case MAP_REMOVE -> MAP_REMOVE_INFO;
            case MAP_APPEND -> MAP_APPEND_INFO;
            case MAP_CLEAR -> MAP_CLEAR_INFO;
            case MAP_GET -> MAP_GET_INFO;
            case MAP_SET -> MAP_SET_INFO;
            case MAP_KEYS -> MAP_KEYS_INFO;
            case MAP_VALUES -> MAP_VALUES_INFO;
            case MAP_FOREACH -> MAP_FOREACH_INFO;

            default -> null;
        };
    }


    //-------------------------------------------------------------------------------------------------

    private final ActionType type;

    private final Class<? extends Action> clazz;

    @StringRes
    private final int title;

    @StringRes
    private final int description;

    @DrawableRes
    private final int icon;

    @StringRes
    private final int help;

    private final Class<? extends ActionCard> cardClass;

    private final Action action;

    public ActionInfo(ActionType type, Class<? extends Action> clazz, int icon, int title, int description) {
        this(type, clazz, title, description, icon, 0);
    }

    public ActionInfo(ActionType type, Class<? extends Action> clazz, int icon, int title, int description, int help) {
        this(type, clazz, title, description, icon, help, NormalActionCard.class);
    }

    public ActionInfo(ActionType type, Class<? extends Action> clazz, @DrawableRes int icon, @StringRes int title, @StringRes int description, @StringRes int help, Class<? extends ActionCard> cardClass) {
        this.type = type;
        this.clazz = clazz;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.help = help;
        this.cardClass = cardClass;
        action = newInstance();
    }

    public ActionType getType() {
        return type;
    }

    public Class<? extends Action> getClazz() {
        return clazz;
    }

    @DrawableRes
    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        if (title == 0) return "";
        return MainApplication.getInstance().getString(title);
    }

    public String getDescription() {
        if (description == 0) return "";
        return MainApplication.getInstance().getString(description);
    }

    public String getHelp() {
        if (help == 0) return "";
        return MainApplication.getInstance().getString(help);
    }

    public Class<? extends ActionCard> getCardClass() {
        return cardClass;
    }

    public Action newInstance() {
        try {
            Constructor<? extends Action> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

    public Action getAction() {
        return action;
    }

    @NonNull
    @Override
    public String toString() {
        return "ActionInfo{" +
                "clazz=" + clazz +
                ", type=" + type +
                '}';
    }
}
