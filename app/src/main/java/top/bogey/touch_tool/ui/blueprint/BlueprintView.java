package top.bogey.touch_tool.ui.blueprint;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.Action;
import top.bogey.touch_tool.bean.action.start.StartAction;
import top.bogey.touch_tool.bean.action.task.CustomEndAction;
import top.bogey.touch_tool.bean.action.task.CustomStartAction;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.bean.save.task.TaskSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.databinding.ViewBlueprintBinding;
import top.bogey.touch_tool.ui.MainActivity;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.selecter.select_action.SelectActionDialog;
import top.bogey.touch_tool.ui.tool.log.LogFloatView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.ThreadUtil;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

public class BlueprintView extends Fragment {

    private final static List<Action> copyActions = new ArrayList<>();

    private final Stack<Task> taskStack = new Stack<>();

    private ViewBlueprintBinding binding;
    private boolean needDelete = false;

    public static boolean tryPushStack(Task task) {
        Fragment fragment = MainActivity.getCurrentFragment();
        if (fragment instanceof BlueprintView blueprintView) {
            blueprintView.pushStack(task);
            return true;
        }
        return false;
    }

    public static boolean tryFocusAction(Task task, Action action) {
        if (task == null || action == null) return false;

        Fragment fragment = MainActivity.getCurrentFragment();
        if (fragment instanceof BlueprintView blueprintView) {
            if (!task.equals(blueprintView.taskStack.peek())) {
                blueprintView.pushStack(task);
                blueprintView.binding.getRoot().postDelayed(() -> blueprintView.binding.cardLayout.focusCard(action.getId()), 100);
            } else {
                blueprintView.binding.cardLayout.focusCard(action.getId());
            }

            ActivityManager manager = (ActivityManager) blueprintView.requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
            try {
                manager.moveTaskToFront(blueprintView.requireActivity().getTaskId(), 0);
            } catch (Exception e) {
                AppLog.e(e);
            }

            return true;
        }
        return false;
    }

    public static void tryRefreshPinView() {
        Fragment fragment = MainActivity.getCurrentFragment();
        if (fragment instanceof BlueprintView blueprintView) {
            blueprintView.binding.cardLayout.refreshPinView();
        }
    }

    public static void tryShowFloatingToolBar(boolean show) {
        Fragment fragment = MainActivity.getCurrentFragment();
        if (fragment instanceof BlueprintView blueprintView) {
            blueprintView.binding.floatingToolBar.setVisibility(show ? View.VISIBLE : View.GONE);
            blueprintView.binding.baseToolBar.setVisibility(show ? View.GONE : View.VISIBLE);

            if (show) {
                List<Action> selectedActions = blueprintView.binding.cardLayout.getSelectedActions();
                boolean locked = false;
                for (Action selectedAction : selectedActions) {
                    if (selectedAction.isLocked()) {
                        locked = true;
                        break;
                    }
                }
                blueprintView.binding.lockButton.setIconResource(locked ? R.drawable.icon_lock : R.drawable.icon_lock_open);
                blueprintView.binding.lockButton.setChecked(locked);
            }
        }
    }

    private final OnBackPressedCallback callback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            popStack();
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshUI();
    }

    private void refreshUI() {
        if (DisplayUtil.isPortrait(requireContext())) {
            DisplayUtil.setViewMargin(binding.floatingToolBar, 0, 0, 0, (int) DisplayUtil.dp2px(requireContext(), 48));
            DisplayUtil.setViewMargin(binding.baseToolBar, 0, 0, 0, (int) DisplayUtil.dp2px(requireContext(), 48));
            DisplayUtil.setViewMargin(binding.cachedPinBar, 0, 0, 0, (int) DisplayUtil.dp2px(requireContext(), 30));
        } else {
            DisplayUtil.setViewMargin(binding.floatingToolBar, 0, 0, 0, (int) DisplayUtil.dp2px(requireContext(), 24));
            DisplayUtil.setViewMargin(binding.baseToolBar, 0, 0, 0, (int) DisplayUtil.dp2px(requireContext(), 24));
            DisplayUtil.setViewMargin(binding.cachedPinBar, 0, 0, 0, (int) DisplayUtil.dp2px(requireContext(), 6));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHideActivityBackground(false);
        binding.getRoot().post(() -> binding.cardLayout.initCacheBoxArea(binding.baseToolBar, binding.cachedPinBox));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments == null) throw new IllegalArgumentException();
        BlueprintViewArgs args = BlueprintViewArgs.fromBundle(arguments);
        Task task = TaskSaver.getInstance().getTask(args.getTaskId());
        if (task == null) throw new IllegalArgumentException();

        Window window = requireActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        binding = ViewBlueprintBinding.inflate(inflater, container, false);

        float offset = DisplayUtil.dp2px(requireContext(), 8);
        float statusBarHeight = DisplayUtil.getStatusBarHeight(requireContext());
        DisplayUtil.setViewMargin(binding.backBox, (int) offset, (int) (offset + statusBarHeight), 0, 0);
        DisplayUtil.setViewMargin(binding.saveBox, 0, (int) (offset + statusBarHeight), (int) offset, 0);
        DisplayUtil.setViewMargin(binding.searchBox, 0, (int) (offset + statusBarHeight), 0, 0);
        DisplayUtil.setViewMargin(binding.searchOptionBox, 0, (int) (offset * 6 + statusBarHeight), 0, 0);

        binding.backButton.setOnClickListener(v -> {
            if (taskStack.size() > 1) {
                popStack();
            } else {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.saveButton.setOnClickListener(v -> {
            Task currTask = taskStack.peek();
            currTask.save();
        });

        binding.searchButton.addOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.searchBox.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.searchOptionBox.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.searchEdit.setText("");
        });

        List<Action> searchActions = new ArrayList<>();
        AtomicInteger index = new AtomicInteger();
        binding.searchEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                searchActions.clear();
                binding.resultCount.setText("");
            }
        });

        binding.nextButton.setOnClickListener(v -> {
            if (searchActions.isEmpty()) {
                searchActions.addAll(searchActions());
                binding.resultCount.setText(getString(R.string.search_card_count, 0, 0));
                if (searchActions.isEmpty()) return;

                index.set(0);
                Task currTask = taskStack.peek();
                tryFocusAction(currTask, searchActions.get(index.get()));
                binding.resultCount.setText(getString(R.string.search_card_count, index.get() + 1, searchActions.size()));
            } else {
                index.getAndIncrement();
                if (index.get() >= searchActions.size()) {
                    index.set(0);
                }
                tryFocusAction(taskStack.peek(), searchActions.get(index.get()));
                binding.resultCount.setText(getString(R.string.search_card_count, index.get() + 1, searchActions.size()));
            }
        });

        binding.preButton.setOnClickListener(v -> {
            if (searchActions.isEmpty()) {
                searchActions.addAll(searchActions());
                binding.resultCount.setText(getString(R.string.search_card_count, 0, 0));
                if (searchActions.isEmpty()) return;

                index.set(searchActions.size() - 1);
                Task currTask = taskStack.peek();
                tryFocusAction(currTask, searchActions.get(index.get()));
                binding.resultCount.setText(getString(R.string.search_card_count, index.get() + 1, searchActions.size()));
            } else {
                index.getAndDecrement();
                if (index.get() < 0) {
                    index.set(searchActions.size() - 1);
                }
                tryFocusAction(taskStack.peek(), searchActions.get(index.get()));
                binding.resultCount.setText(getString(R.string.search_card_count, index.get() + 1, searchActions.size()));
            }
        });

        binding.pinyinCheck.addOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingSaver.BLUEPRINT_SEARCH_WITH_PINYIN.set(isChecked);
            searchActions.clear();
            binding.resultCount.setText("");
        });
        binding.pinyinCheck.setChecked(SettingSaver.BLUEPRINT_SEARCH_WITH_PINYIN.get());

        binding.posCheck.addOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingSaver.BLUEPRINT_SEARCH_WITH_POSITION.set(isChecked);
            searchActions.clear();
            binding.resultCount.setText("");
        });
        binding.posCheck.setChecked(SettingSaver.BLUEPRINT_SEARCH_WITH_POSITION.get());

        binding.caseCheck.addOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingSaver.BLUEPRINT_SEARCH_WITH_CASE.set(isChecked);
            searchActions.clear();
            binding.resultCount.setText("");
        });
        binding.caseCheck.setChecked(SettingSaver.BLUEPRINT_SEARCH_WITH_CASE.get());

        binding.regCheck.addOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingSaver.BLUEPRINT_SEARCH_WITH_REGEX.set(isChecked);
            searchActions.clear();
            binding.resultCount.setText("");
        });
        binding.regCheck.setChecked(SettingSaver.BLUEPRINT_SEARCH_WITH_REGEX.get());

        binding.moreButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), binding.saveBox);
            popupMenu.getMenuInflater().inflate(R.menu.menu_blueprint, popupMenu.getMenu());

            MenuItem menuItem = popupMenu.getMenu().findItem(R.id.taskDetailLog);
            menuItem.setChecked(task.hasFlag(Task.FLAG_DEBUG));
            menuItem.setVisible(task.getParent() == null);

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.taskRunningLog) {
                    Task currTask = taskStack.peek();
                    while (currTask.getParent() != null) currTask = currTask.getParent();
                    new LogFloatView(requireContext(), currTask).show();
                    return true;
                } else if (itemId == R.id.taskDetailLog) {
                    Task currTask = taskStack.peek();
                    currTask.toggleFlag(Task.FLAG_DEBUG);
                    currTask.save();
                    item.setChecked(currTask.hasFlag(Task.FLAG_DEBUG));
                    return true;
                } else if (itemId == R.id.taskCapture) {
                    Bitmap bitmap = binding.cardLayout.takeTaskCapture();
                    ShapeableImageView imageView = new ShapeableImageView(requireContext());
                    imageView.setImageBitmap(bitmap);

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.task_capture)
                            .setView(imageView)
                            .setPositiveButton(R.string.save, (dialog, which) -> {
                                dialog.dismiss();
                                AppUtil.writePictureImage(requireContext(), bitmap);
                            })
                            .setNegativeButton(R.string.share_to_action, (dialog, which) -> {
                                dialog.dismiss();
                                AppUtil.shareImage(requireContext(), bitmap);
                            })
                            .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .show();

                    Size size = DisplayUtil.getScreenSize(requireContext());
                    DisplayUtil.setViewWidth(imageView, ViewGroup.LayoutParams.MATCH_PARENT);
                    DisplayUtil.setViewHeight(imageView, size.getHeight() / 2);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        binding.addButton.setOnClickListener(v -> new SelectActionDialog(requireContext(), taskStack.peek(), action -> {
            ActionCard card = binding.cardLayout.addCard(action);
            if (card == null) return;
            binding.cardLayout.initCardPos(card);
        }).show());

        binding.sortButton.setOnClickListener(v -> {
            if (!binding.cardLayout.isLoaded()) return;
            ThreadUtil.execute(() -> {
                Task currTask = taskStack.peek();
                List<Action> startActions = currTask.getActions(StartAction.class);
                List<Action> actions = currTask.getActions(CustomStartAction.class);
                startActions.addAll(actions);
                CardLayoutHelper.ActionArea actionArea = new CardLayoutHelper.ActionArea(binding.cardLayout, startActions);
                actionArea.arrange(new PointF(), 0);
                actionArea.compact();
                currTask.save();
                binding.cardLayout.post(() -> binding.cardLayout.updateCardsPos());
            });
        });

        binding.editButton.setOnClickListener(v -> {
            boolean editable = !binding.editButton.isChecked();
            SettingSaver.BLUEPRINT_EDITABLE.set(editable);
            binding.cardLayout.setEditable(editable);
        });
        binding.editButton.setChecked(!SettingSaver.BLUEPRINT_EDITABLE.get());
        binding.cardLayout.setEditable(SettingSaver.BLUEPRINT_EDITABLE.get());

        binding.pasteButton.setOnClickListener(v -> {
            binding.cardLayout.cleanSelectedCards();
            boolean first = true;
            int offsetX = 0, offsetY = 0;
            copyActions.sort(Comparator.comparingInt(o -> o.getPos().y));

            for (Action copyAction : copyActions) {
                ActionCard card = binding.cardLayout.addCard(copyAction);
                if (card == null) continue;
                if (first) {
                    first = false;
                    int x = copyAction.getPos().x;
                    int y = copyAction.getPos().y;
                    binding.cardLayout.initCardPos(card);
                    offsetX = card.getAction().getPos().x - x;
                    offsetY = card.getAction().getPos().y - y;
                } else {
                    Point pos = copyAction.getPos();
                    copyAction.setPos(pos.x + offsetX, pos.y + offsetY);
                    binding.cardLayout.updateCardPos(card);
                }
                binding.cardLayout.addSelectedCard(copyAction);
            }
            copyActions.clear();
            binding.pasteButton.setVisibility(View.GONE);
        });
        binding.pasteButton.setVisibility(copyActions.isEmpty() ? View.GONE : View.VISIBLE);

        binding.pasteButton.setOnLongClickListener(v -> {
            copyActions.clear();
            binding.pasteButton.setVisibility(View.GONE);
            return true;
        });

        binding.exchangeButton.setOnClickListener(v -> AppUtil.showEditDialog(getContext(), R.string.task_exchange_to_custom, "", result -> {
            if (result.isEmpty()) return;

            Task currTask = taskStack.peek();
            Task innerTask = new Task();
            List<Action> selectedActionsCopy = binding.cardLayout.getSelectedActionsCopy();
            int offsetX = Integer.MAX_VALUE, offsetY = Integer.MAX_VALUE;
            for (Action action : selectedActionsCopy) {
                Point pos = action.getPos();
                offsetX = Math.min(offsetX, pos.x);
                offsetY = Math.min(offsetY, pos.y);
            }
            for (Action action : selectedActionsCopy) {
                action.getPos().offset(-offsetX, -offsetY);
                // 避免与已有的action重叠
                action.getPos().offset(20, 0);
            }
            selectedActionsCopy.forEach(innerTask::addAction);

            innerTask.addAction(new CustomStartAction());
            innerTask.addAction(new CustomEndAction());
            innerTask.setTitle(result);
            currTask.addTask(innerTask);
            currTask.save();
            pushStack(innerTask);
        }));

        binding.lockButton.setOnClickListener(v -> {
            boolean locked = binding.lockButton.isChecked();
            binding.lockButton.setIconResource(locked ? R.drawable.icon_lock : R.drawable.icon_lock_open);
            binding.lockButton.setChecked(locked);
            List<Action> selectedActions = binding.cardLayout.getSelectedActions();
            selectedActions.forEach(action -> {
                action.setLocked(locked);
                ActionCard card = binding.cardLayout.getActionCard(action);
                if (card != null) card.refreshCardLockState();
            });
        });

        binding.copyButton.setOnClickListener(v -> {
            List<Action> selectedActions = binding.cardLayout.getSelectedActionsCopy();
            binding.cardLayout.cleanSelectedCards();
            selectedActions.forEach(action -> {
                binding.cardLayout.addCard(action);
                binding.cardLayout.addSelectedCard(action);
            });
        });

        binding.copyButton.setOnLongClickListener(v -> {
            copyActions.clear();
            copyActions.addAll(binding.cardLayout.getSelectedActionsCopy());
            binding.pasteButton.setVisibility(copyActions.isEmpty() ? View.GONE : View.VISIBLE);
            binding.cardLayout.cleanSelectedCards();
            return true;
        });

        binding.deleteButton.setOnClickListener(v -> {
            if (needDelete) {
                for (ActionCard card : new HashSet<>(binding.cardLayout.selectedCards)) {
                    binding.cardLayout.removeCard(card);
                }
                binding.cardLayout.selectedCards.clear();
                binding.floatingToolBar.setVisibility(View.GONE);
            } else {
                binding.deleteButton.setChecked(true);
                needDelete = true;
                binding.deleteButton.postDelayed(() -> {
                    binding.deleteButton.setChecked(false);
                    needDelete = false;
                }, 1500);
            }
        });

        binding.cachedPinBox.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                binding.cachedPinBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                binding.cachedPinBar.setVisibility(binding.cachedPinBox.getChildCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });

        refreshUI();

        pushStack(task);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setHideActivityBackground(SettingSaver.APP_HIDE_ACTIVITY_BACKGROUND.get());

        while (!taskStack.empty()) {
            taskStack.pop().save();
        }

        Window window = requireActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    public void pushStack(Task task) {
        if (task == null) return;

        if (!taskStack.empty()) taskStack.peek().save();

        taskStack.remove(task);
        taskStack.push(task);

        setTask(task);
    }

    public void popStack() {
        if (taskStack.empty()) return;
        Task task = taskStack.pop();
        task.save();

        if (!taskStack.empty()) {
            task = taskStack.peek();
            setTask(task);
        }
    }

    public void setTask(Task task) {
        binding.cardLayout.setTask(task);
        binding.title.setText(task.getTitle());
        callback.setEnabled(taskStack.size() > 1);
    }

    private List<Action> searchActions() {
        List<Action> searchActions = new ArrayList<>();
        Editable text = binding.searchEdit.getText();
        if (text == null || text.length() == 0) return searchActions;
        String searchString = text.toString();

        boolean pinyin = binding.pinyinCheck.isChecked();
        boolean matchPos = binding.posCheck.isChecked();
        boolean matchCase = binding.caseCheck.isChecked();
        boolean regex = binding.regCheck.isChecked();

        if (!matchCase) searchString = searchString.toLowerCase();
        Pattern pattern = null;
        if (regex) pattern = AppUtil.getPattern(searchString);

        Task currTask = taskStack.peek();
        for (Action action : currTask.getActions()) {
            boolean matched = false;
            if (matchPos) {
                Point pos = action.getPos();
                String posString = pos.x + "," + pos.y;
                if (regex) {
                    if (pattern != null && pattern.matcher(posString).find()) {
                        searchActions.add(action);
                        matched = true;
                    }
                } else {
                    if (posString.contains(searchString)) {
                        searchActions.add(action);
                        matched = true;
                    }
                }
            }
            if (!matched) {
                String actionString = action.getFullDescription();
                if (!matchCase) {
                    actionString = actionString.toLowerCase();
                }
                if (regex) {
                    if (pattern != null && pattern.matcher(actionString).find()) {
                        searchActions.add(action);
                        matched = true;
                    }
                } else {
                    if (actionString.contains(searchString)) {
                        searchActions.add(action);
                        matched = true;
                    }
                }
            }

            if (!matched && pinyin) {
                if (AppUtil.isPinyinContains(action.getFullDescription(), searchString)) {
                    searchActions.add(action);
                }
            }
        }
        return searchActions;
    }

    private void setHideActivityBackground(boolean hide) {
        int taskId = requireActivity().getTaskId();
        ActivityManager manager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.AppTask> taskList = manager.getAppTasks();
            if (taskList != null) {
                for (ActivityManager.AppTask task : taskList) {
                    ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();
                    if (taskInfo == null) continue;
                    if (taskInfo.id == taskId) task.setExcludeFromRecents(hide);
                }
            }
        }
    }
}
