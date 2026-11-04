package top.bogey.touch_tool.ui.blueprint.pin_widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.Action;
import top.bogey.touch_tool.bean.action.number.MathExpressionAction;
import top.bogey.touch_tool.bean.action.task.ExecuteTaskAction;
import top.bogey.touch_tool.bean.pin.pin_objects.PinSubType;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinAutoPinString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinFileContentString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinNodePathString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinNodePathTextString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinRingtoneString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinShortcutString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinSingleLineString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinTaskString;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinUrlString;
import top.bogey.touch_tool.bean.save.task.TaskSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.databinding.PinWidgetInputBinding;
import top.bogey.touch_tool.ui.InstantActivity;
import top.bogey.touch_tool.ui.MainActivity;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.picker.NodePickerPreview;
import top.bogey.touch_tool.ui.blueprint.pin.PinView;
import top.bogey.touch_tool.ui.blueprint.selecter.select_action.SelectActionByAllActionDialog;
import top.bogey.touch_tool.ui.blueprint.selecter.select_action.SelectActionByCustomActionDialog;
import top.bogey.touch_tool.ui.blueprint.selecter.select_edit_text.SelectEditTextDialog;
import top.bogey.touch_tool.ui.blueprint.selecter.select_icon.SelectIconDialog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.callback.ResultCallback;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class PinWidgetString extends PinWidget<PinString> {
    private final PinWidgetInputBinding binding;

    private final ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_edit_text, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.fullScreen) {
                mode.finish();
                new SelectEditTextDialog(getContext(), binding.editText, binding.editText::setText).show();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinString pinBase, boolean custom) {
        super(context, card, pinView, pinBase, custom);
        binding = PinWidgetInputBinding.inflate(LayoutInflater.from(context), this, true);
        init();
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinUrlString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinShortcutString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinRingtoneString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinAutoPinString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinSingleLineString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinNodePathString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinNodePathTextString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinTaskString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    public PinWidgetString(@NonNull Context context, ActionCard card, PinView pinView, PinFileContentString pinBase, boolean custom) {
        this(context, card, pinView, (PinString) pinBase, custom);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initBase() {
        binding.editText.setSaveEnabled(false);
        binding.editText.setSaveFromParentEnabled(false);

        binding.editText.setEnabled(false);
        binding.pickButton.setVisibility(VISIBLE);

        binding.editText.setCustomSelectionActionModeCallback(callback);
        binding.editText.setCustomInsertionActionModeCallback(callback);

        switch (pinBase.getSubType()) {
            case URL -> {
                String url = "tt://do_action?" + InstantActivity.TASK_ID + "=" + card.getTask().getId() + "&" + InstantActivity.ACTION_ID + "=" + card.getAction().getId();
                binding.editText.setText(url);
                binding.pickButton.setIconResource(R.drawable.icon_content_copy);
                binding.pickButton.setOnClickListener(v -> AppUtil.copyToClipboard(getContext(), url));
            }
            case SHORTCUT -> {
                binding.editBox.setVisibility(GONE);
                binding.pickButton.setIconResource(R.drawable.icon_share);
                binding.pickButton.setOnClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ShortcutManager manager = (ShortcutManager) getContext().getSystemService(Context.SHORTCUT_SERVICE);
                        if (manager.isRequestPinShortcutSupported()) {
                            new SelectIconDialog(getContext(), icon -> {
                                Intent intent = new Intent(getContext(), InstantActivity.class);
                                intent.setAction(InstantActivity.INTENT_KEY_DO_ACTION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(InstantActivity.TASK_ID, card.getTask().getId());
                                intent.putExtra(InstantActivity.ACTION_ID, card.getAction().getId());
                                String shortcutName = card.getAction().getDescription();
                                if (shortcutName == null || shortcutName.isEmpty()) shortcutName = card.getTask().getTitle();
                                ShortcutInfo info = new ShortcutInfo.Builder(getContext(), card.getAction().getId())
                                        .setShortLabel(shortcutName)
                                        .setIcon(Icon.createWithBitmap(icon))
                                        .setIntent(intent)
                                        .build();
                                manager.requestPinShortcut(info, null);
                            }).show();
                            Toast.makeText(getContext(), R.string.select_icon_tips, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Toast.makeText(getContext(), R.string.device_not_support_shortcut, Toast.LENGTH_SHORT).show();
                });
            }
            case RINGTONE -> {
                String value = pinBase.getValue();
                binding.editText.setText(getRingtoneName(value));
                binding.pickButton.setIconResource(R.drawable.icon_notifications);
                binding.pickButton.setOnClickListener(v -> {
                    MainActivity activity = MainApplication.getInstance().getActivity();
                    activity.launcherRingtone(value, (code, intent) -> {
                        if (code == Activity.RESULT_OK) {
                            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                            if (uri == null) {
                                binding.editText.setText("");
                                pinBase.setValue(null);
                                pinView.getPin().notifyValueUpdated(card.getTask());
                            } else {
                                binding.editText.setText(getRingtoneName(uri.toString()));
                                pinBase.setValue(uri.toString());
                                pinView.getPin().notifyValueUpdated(card.getTask());
                            }
                        }
                    });
                });
            }
            case AUTO_PIN -> {
                binding.editText.setEnabled(true);
                binding.pickButton.setVisibility(GONE);
                binding.editText.setText(pinBase.getValue());
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (Objects.equals(s.toString(), pinBase.getValue())) return;
                        pinBase.setValue(s.toString());
                        pinView.getPin().notifyValueUpdated(card.getTask());
                        resetDynamicPin();
                    }
                });
            }
            case SINGLE_LINE -> {
                binding.pickButton.setVisibility(GONE);
                binding.editText.setEnabled(true);
                binding.editText.setText(pinBase.getValue());
                binding.editText.setSingleLine(true);
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        pinBase.setValue(s.toString());
                        pinView.getPin().notifyValueUpdated(card.getTask());
                    }
                });
            }
            case NODE_PATH -> {
                PinNodePathString nodePath = (PinNodePathString) pinBase;
                binding.editText.setText(nodePath.getSimpleValue());
                binding.pickButton.setIconResource(R.drawable.icon_widgets);
                binding.pickButton.setOnClickListener(v -> new NodePickerPreview(getContext(), result -> {
                    nodePath.setValue(result);
                    pinView.getPin().notifyValueUpdated(card.getTask());
                    binding.editText.setText(nodePath.getSimpleValue());
                }, nodePath.getValue()).show());
            }
            case NODE_PATH_TEXT -> {
                PinNodePathTextString nodePath = (PinNodePathTextString) pinBase;
                binding.editText.setEnabled(true);
                binding.editText.setSingleLine(false);
                binding.editText.setMaxLines(10);
                binding.editText.setText(nodePath.getValue());
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        nodePath.setValue(s.toString());
                        pinView.getPin().notifyValueUpdated(card.getTask());
                    }
                });
                binding.pickButton.setIconResource(R.drawable.icon_text_fields);
                binding.pickButton.setOnClickListener(v -> new NodePickerPreview(getContext(), result -> {
                    nodePath.setValue(result);
                    String regex = AppUtil.formatRegex(nodePath.getValue());
                    nodePath.setValue(regex);
                    pinView.getPin().notifyValueUpdated(card.getTask());
                    binding.editText.setText(regex);
                }, null).show());
            }
            case TASK_ID, ALL_TASK_ID -> {
                Task task = TaskSaver.getInstance().upFindTask(card.getTask(), pinBase.getValue());
                if (task != null) binding.editText.setText(task.getTitle());
                binding.pickButton.setIconResource(R.drawable.icon_assignment);
                ResultCallback<Action> callback = action -> {
                    ExecuteTaskAction executeTaskAction = (ExecuteTaskAction) action;
                    Task executeTask = executeTaskAction.getTask(card.getTask());
                    pinBase.setValue(executeTask.getId());
                    pinView.getPin().notifyValueUpdated(card.getTask());
                    binding.editText.setText(executeTask.getTitle());
                    if (card.getAction() instanceof ExecuteTaskAction executeAction) {
                        executeAction.sync(card.getTask(), executeTask);
                    }
                };
                binding.pickButton.setOnClickListener(v -> {
                    if (pinBase.getSubType() == PinSubType.TASK_ID) {
                        new SelectActionByCustomActionDialog(getContext(), card.getTask(), callback).show();
                    } else {
                        new SelectActionByAllActionDialog(getContext(), card.getTask(), callback).show();
                    }
                });
            }
            case FILE_CONTENT -> {
                binding.editText.setEnabled(true);
                binding.editText.setText(pinBase.getValue());
                binding.editText.setSingleLine(false);
                binding.editText.setMaxLines(10);
                binding.editText.setInputType(binding.editText.getInputType() | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        pinBase.setValue(s.toString());
                        pinView.getPin().notifyValueUpdated(card.getTask());
                    }
                });
                binding.pickButton.setIconResource(R.drawable.icon_folder);
                binding.pickButton.setOnClickListener(v -> {
                    MainActivity activity = MainApplication.getInstance().getActivity();
                    activity.launcherOpenDocument((code, intent) -> {
                        if (code == Activity.RESULT_OK) {
                            Uri uri = intent.getData();
                            if (uri == null) {
                                binding.editText.setText("");
                            } else {
                                String content = new String(AppUtil.readFile(getContext(), uri));
                                binding.editText.setText(content);
                                pinBase.setValue(content);
                                pinView.getPin().notifyValueUpdated(card.getTask());
                            }
                        }
                    }, "*/*");
                });
            }
            default -> {
                binding.editText.setEnabled(true);
                binding.editText.setText(pinBase.getValue());
                binding.editText.setSingleLine(false);
                binding.editText.setMaxLines(10);
                binding.editText.setInputType(binding.editText.getInputType() | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        pinBase.setValue(s.toString());
                        pinView.getPin().notifyValueUpdated(card.getTask());
                    }
                });
                binding.pickButton.setVisibility(GONE);
            }
        }
    }

    @Override
    protected void initCustom() {

    }

    private String getRingtoneName(String path) {
        if (path == null) return null;
        Uri uri = Uri.parse(path);
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
        if (ringtone == null) return path;
        return ringtone.getTitle(getContext());
    }

    /**
     * 公式文本变化后，让动作按文本重新解析动态针脚
     */
    private void resetDynamicPin() {
        if (card.getAction() instanceof MathExpressionAction action) action.resetDynamicPins(card.getTask());
    }
}
