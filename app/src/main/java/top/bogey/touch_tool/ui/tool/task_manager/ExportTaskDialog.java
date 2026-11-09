package top.bogey.touch_tool.ui.tool.task_manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.save.task.TaskSaver;
import top.bogey.touch_tool.bean.save.variable.VariableSaver;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.bean.task.Variable;
import top.bogey.touch_tool.databinding.DialogTaskManagerBinding;
import top.bogey.touch_tool.ui.MainActivity;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.GsonUtil;
import top.bogey.touch_tool.utils.ThreadUtil;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class ExportTaskDialog extends FrameLayout {
    private static void showDialog(Context context, ExportTaskDialog view) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.task_export)
                .setView(view)
                .setPositiveButton(R.string.export_task, (dialog, which) -> view.export())
                .setNegativeButton(R.string.save, (dialog, which) -> view.save())
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    public static void showDialog(Context context) {
        List<Task> tasks = TaskSaver.getInstance().getTasks();
        ExportTaskDialog view = new ExportTaskDialog(context, tasks, false);
        showDialog(context, view);
    }

    public static void showDialog(Context context, List<Task> tasks) {
        ExportTaskDialog view = new ExportTaskDialog(context, tasks, true);
        showDialog(context, view);
    }

    public static void autoBackup(Context context) {
        ThreadUtil.execute(() -> {
            Set<Task> tasks = new HashSet<>(TaskSaver.getInstance().getTasks());
            Set<Variable> vars = new HashSet<>(VariableSaver.getInstance().getVars());
            TaskRecord record = new TaskRecord(tasks, vars);
            String json = GsonUtil.toJson(record);
            String fileName = record.getDefaultName(context) + ".tt";
            AppUtil.writeDownloadFile(context, MainApplication.appName, fileName, json.getBytes());
        });
    }

    private final ExportTaskDialogAdapter adapter;

    public ExportTaskDialog(@NonNull Context context, List<Task> tasks, boolean selectAll) {
        super(context);
        DialogTaskManagerBinding binding = DialogTaskManagerBinding.inflate(LayoutInflater.from(context), this, true);

        binding.importTag.setVisibility(View.GONE);
        binding.importCopy.setVisibility(View.GONE);

        adapter = new ExportTaskDialogAdapter();
        binding.selectionBox.setAdapter(adapter);

        binding.searchEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                String searchString = s.toString();
                if (searchString.isEmpty()) {
                    adapter.refreshTasks(tasks);
                } else {
                    List<Task> searchTasks = new ArrayList<>();
                    for (Task task : tasks) {
                        if (AppUtil.isStringContains(task.getFullDescription(), searchString)) searchTasks.add(task);
                    }
                    adapter.refreshTasks(searchTasks);
                }
            }
        });

        binding.selectAllButton.setOnClickListener(v -> {
            if (binding.selectAllButton.isChecked()) {
                adapter.selectAll();
            } else {
                adapter.unselectAll();
            }
        });
        binding.selectAllButton.setChecked(selectAll);

        adapter.refreshTasks(tasks);
        if (selectAll) adapter.selectAll();
    }

    public void export() {
        Context context = getContext();
        TaskRecord taskRecord = adapter.getTaskRecord();
        AppUtil.showEditDialog(context, R.string.file_name, taskRecord.getDefaultName(context), filename -> {
            String json = GsonUtil.toJson(taskRecord);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("application/octet-stream");

            Uri uri = AppUtil.writeCacheFile(context, AppUtil.TASK_DIR_NAME, filename + ".tt", json.getBytes());
            if (uri != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                // 走 createChooser 才会拉起系统分享面板；FLAG_ACTIVITY_NEW_TASK 要加在 chooser 上，里层 intent 的 flag 不会带过去
                Intent chooser = Intent.createChooser(intent, null);
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chooser);
            }
        });
    }

    public void save() {
        TaskRecord taskRecord = adapter.getTaskRecord();
        String json = GsonUtil.toJson(taskRecord);

        MainActivity activity = MainApplication.getInstance().getActivity();
        AppUtil.exportFile(activity, taskRecord.getDefaultName(getContext()) + ".tt", json.getBytes());
    }
}
