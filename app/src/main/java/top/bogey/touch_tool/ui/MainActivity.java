package top.bogey.touch_tool.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.bean.save.task.TaskSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.databinding.ActivityMainBinding;
import top.bogey.touch_tool.ui.blueprint.BlueprintView;
import top.bogey.touch_tool.ui.task.TaskViewDirections;
import top.bogey.touch_tool.ui.tool.task_manager.ImportTaskDialog;
import top.bogey.touch_tool.utils.AppUtil;

public class MainActivity extends FloatViewActivity {
    public static final String INTENT_KEY_OPEN_TASK = "INTENT_KEY_OPEN_TASK";


    private ActivityMainBinding binding;

    public static Fragment getCurrentFragment() {
        MainActivity activity = MainApplication.getInstance().getActivity();
        if (activity == null) return null;
        Fragment navFragment = activity.getSupportFragmentManager().getPrimaryNavigationFragment();
        if (navFragment == null || !navFragment.isAdded()) return null;
        return navFragment.getChildFragmentManager().getPrimaryNavigationFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.getInstance().setActivity(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        SettingSaver.getInstance().init(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        NavController controller = Navigation.findNavController(this, R.id.conView);

        // 底部三个 Tab 左右滑动切换：按 Tab 顺序前进从右滑入、后退从左滑入
        binding.menuView.setOnItemSelectedListener(item -> {
            NavDestination current = controller.getCurrentDestination();
            int currentId = current == null ? 0 : current.getId();
            if (currentId == item.getItemId()) return true;

            boolean forward = tabIndex(currentId) < 0 || tabIndex(item.getItemId()) > tabIndex(currentId);

            // 目标 Tab 还在返回栈里时只能弹回去：navigate 会被判为 launchSingleTop，
            // 它重建的 Frame 事务不带自定义动画（点回「任务」没有转场就是这个原因），
            // 弹栈则由当初那条事务记录的 pop 动画提供，方向正好是后退
            if (isOnBackStack(controller, item.getItemId())) {
                controller.popBackStack(item.getItemId(), false);
                return true;
            }

            NavOptions.Builder builder = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setEnterAnim(forward ? R.anim.slide_in_right : R.anim.slide_in_left)
                    .setExitAnim(forward ? R.anim.slide_out_left : R.anim.slide_out_right)
                    // 被弹掉的页面一律从右往左收回，与后退方向一致
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right);
            // 后退且目标不在栈上（如从「设置」直接点「工具」）：先收回起始页之上的页面，栈保持有序不重复
            if (!forward) builder.setPopUpTo(NavGraph.Companion.findStartDestination(controller.getGraph()).getId(), false);
            controller.navigate(item.getItemId(), null, builder.build());
            return true;
        });

        controller.addOnDestinationChangedListener((navController, navDestination, bundle) -> {
            int id = navDestination.getId();
            if (id == R.id.task || id == R.id.tool || id == R.id.setting) {
                showBottomNavigation();
            } else {
                hideBottomNavigation();
            }
            // 目的地由代码切换（返回、深链）时同步底部选中项
            MenuItem item = binding.menuView.getMenu().findItem(id);
            if (item != null && !item.isChecked()) binding.menuView.setSelectedItemId(id);
        });

        String runningError = SettingSaver.APP_RUNNING_ERROR.get();
        if (runningError != null && !runningError.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title)
                    .setMessage(R.string.running_error_tips)
                    .setPositiveButton(R.string.copy_and_join, (dialog, which) -> {
                        AppUtil.copyToClipboard(this, runningError, true);
                        AppUtil.gotoUrl(this, getString(R.string.setting_qq_group_url));
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.copy, (dialog, which) -> {
                        AppUtil.copyToClipboard(this, runningError, true);
                        dialog.dismiss();
                    })
                    .setNeutralButton(R.string.cancel, null)
                    .show();
            SettingSaver.APP_RUNNING_ERROR.set("");
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    // 底部导航栏显隐动画时长，与 nav 转场动画保持一致
    private static final long NAV_BAR_ANIM_DURATION = 300L;

    // 底部 Tab 从左到右的顺序，用于判断滑动转场的方向
    private static final int[] TAB_IDS = {R.id.task, R.id.tool, R.id.setting};

    // 目标页面是否还留在返回栈里；在栈上就只能 pop，navigate 会走 launchSingleTop 丢掉转场动画
    private static boolean isOnBackStack(NavController controller, int destinationId) {
        List<NavBackStackEntry> entries = controller.getCurrentBackStack().getValue();
        if (entries == null) return false;
        for (NavBackStackEntry entry : entries) {
            if (entry.getDestination().getId() == destinationId) return true;
        }
        return false;
    }

    private static int tabIndex(int id) {
        for (int i = 0; i < TAB_IDS.length; i++) {
            if (TAB_IDS[i] == id) return i;
        }
        return -1;
    }

    // 底部导航栏随页面切换淡入，避免瞬间出现
    public void showBottomNavigation() {
        View view = binding.menuView;
        view.animate().cancel();
        if (view.getVisibility() == View.VISIBLE && view.getAlpha() == 1f && view.getTranslationY() == 0f) return;
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.animate().translationY(0f).alpha(1f)
                .setDuration(NAV_BAR_ANIM_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    // 底部导航栏随页面切换淡出，避免瞬间消失造成闪屏
    public void hideBottomNavigation() {
        View view = binding.menuView;
        view.animate().cancel();
        if (view.getVisibility() != View.VISIBLE) return;
        view.setTranslationY(0f);
        view.setAlpha(1f);
        view.animate().translationY(view.getHeight()).alpha(0f)
                .setDuration(NAV_BAR_ANIM_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    view.setTranslationY(0f);
                    view.setAlpha(1f);
                })
                .start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController controller = Navigation.findNavController(this, R.id.conView);
        return controller.navigateUp() || super.onSupportNavigateUp();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        setIntent(null);

        Uri uri = null;
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            uri = intent.getData();
        }
        if (uri != null) {
            ImportTaskDialog.showDialog(this, uri);
        }

        if (INTENT_KEY_OPEN_TASK.equals(intent.getAction())) {
            String taskId = intent.getStringExtra(InstantActivity.TASK_ID);
            Task task = TaskSaver.getInstance().getTask(taskId);
            if (!BlueprintView.tryPushStack(task)) {
                NavController controller = Navigation.findNavController(this, R.id.conView);
                controller.navigate(R.id.task);
                controller.navigate(TaskViewDirections.actionTaskToBlueprint(taskId));
            }
        }
    }
}