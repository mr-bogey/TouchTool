package top.bogey.touch_tool.ui.setting;

import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import top.bogey.touch_tool.databinding.LogItemBinding;
import top.bogey.touch_tool.utils.AppLog;

// 日志弹窗的列表，一行一条，展示时间、分类、标签与内容；内容超过三行的可点击展开收起
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    // 收起时显示的行数
    private static final int COLLAPSED_LINES = 3;

    // 全部日志，筛选前保留一份
    private final List<AppLog.Entry> entries = new ArrayList<>();
    // 当前展示的日志，按标签筛过
    private final List<AppLog.Entry> logs = new ArrayList<>();
    // 当前筛选的分类，null 表示全部
    private String categoryFilter;
    // 已展开的条目下标
    private final Set<Integer> expanded = new HashSet<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LogItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.refresh(logs.get(position), expanded.contains(position), () -> {
            int index = holder.getBindingAdapterPosition();
            if (index == RecyclerView.NO_POSITION) return;
            if (!expanded.remove(index)) expanded.add(index);
            notifyItemChanged(index);
        });
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    // 一次性填充列表，默认展示全部
    public void setLogs(List<AppLog.Entry> all) {
        entries.clear();
        entries.addAll(all);
        categoryFilter = null;
        filterLogs();
    }

    // 按分类筛选，null 表示全部
    public void setCategoryFilter(String category) {
        categoryFilter = category;
        filterLogs();
    }

    // 按当前分类重算展示的条目
    private void filterLogs() {
        logs.clear();
        for (AppLog.Entry entry : entries) {
            if (categoryFilter == null || categoryFilter.equals(entry.category())) logs.add(entry);
        }
        expanded.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LogItemBinding binding;

        public ViewHolder(@NonNull LogItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void refresh(AppLog.Entry entry, boolean expanded, Runnable onToggle) {
            binding.time.setText(entry.time());
            binding.category.setText(entry.category());
            binding.tag.setText(entry.tag());

            TextView message = binding.message;
            message.setText(entry.message());
            message.setMaxLines(expanded ? Integer.MAX_VALUE : COLLAPSED_LINES);
            message.setEllipsize(expanded ? null : TextUtils.TruncateAt.END);

            // 排版完才知道内容有没有被省略号截断，只有截断的条目才可点击
            message.post(() -> {
                if (!entry.message().equals(message.getText().toString())) return;
                boolean expandable = expanded || hasEllipsis(message);
                binding.getRoot().setClickable(expandable);
                binding.getRoot().setFocusable(expandable);
                binding.getRoot().setOnClickListener(expandable ? v -> onToggle.run() : null);
            });
        }

        // 有任意一行被省略号截断
        private static boolean hasEllipsis(TextView view) {
            Layout layout = view.getLayout();
            if (layout == null) return false;
            for (int i = 0; i < layout.getLineCount(); i++) {
                if (layout.getEllipsisCount(i) > 0) return true;
            }
            return false;
        }
    }
}
