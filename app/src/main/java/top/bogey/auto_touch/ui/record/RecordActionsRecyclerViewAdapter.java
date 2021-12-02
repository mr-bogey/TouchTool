package top.bogey.auto_touch.ui.record;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import top.bogey.auto_touch.R;
import top.bogey.auto_touch.databinding.FloatFragmentRecordItemBinding;
import top.bogey.auto_touch.room.bean.Action;
import top.bogey.auto_touch.room.bean.Node;
import top.bogey.auto_touch.room.bean.Task;
import top.bogey.auto_touch.ui.action.ActionEditDialog;

public class RecordActionsRecyclerViewAdapter extends RecyclerView.Adapter<RecordActionsRecyclerViewAdapter.ViewHolder> {
    private final TaskRecordDialog parent;
    public final List<Action> actions = new ArrayList<>();

    public RecordActionsRecyclerViewAdapter(TaskRecordDialog parent){
        this.parent = parent;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FloatFragmentRecordItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Action action = actions.get(position);
        switch (action.target.type){
            case WORD:
                holder.delete.setIconResource(R.drawable.text);
                break;
            case IMAGE:
                holder.delete.setIconResource(R.drawable.image);
                break;
            case POS:
                holder.delete.setIconResource(R.drawable.pos);
                break;
            case TASK:
                switch (action.actionMode) {
                    case KEY:
                        holder.delete.setIconResource(R.drawable.keyboard);
                        break;
                    case TASK:
                        holder.delete.setIconResource(R.drawable.task);
                        break;
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    public void addAction(@NonNull Action action){
        if (action.stop == null){
            Node stop = new Node();
            stop.setNull();
            action.stop = stop;
        }
        actions.add(action);
        notifyItemInserted(actions.size() - 1);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public final MaterialButton delete;

        public ViewHolder(@NonNull FloatFragmentRecordItemBinding binding) {
            super(binding.getRoot());
            delete = binding.deleteButton;

            delete.setOnClickListener(v -> {
                int index = getAdapterPosition();
                Action action = actions.get(index);
                Task task = parent.task;
                new ActionEditDialog(parent.getContext(), task, action, () -> notifyItemChanged(index)).show();
            });

            delete.setOnLongClickListener(v -> {
                int index = getAdapterPosition();
                actions.remove(index);
                notifyItemRemoved(index);
                return true;
            });
        }
    }
}