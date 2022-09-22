package top.bogey.touch_tool.ui.actions;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import top.bogey.touch_tool.MainAccessibilityService;
import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.MainViewModel;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.databinding.FloatActionItemBinding;
import top.bogey.touch_tool.room.bean.Action;
import top.bogey.touch_tool.room.bean.Task;
import top.bogey.touch_tool.room.bean.TaskStatus;
import top.bogey.touch_tool.room.bean.node.ColorNode;
import top.bogey.touch_tool.room.bean.node.DelayNode;
import top.bogey.touch_tool.room.bean.node.ImageNode;
import top.bogey.touch_tool.room.bean.node.KeyNode;
import top.bogey.touch_tool.room.bean.node.Node;
import top.bogey.touch_tool.room.bean.node.NodeType;
import top.bogey.touch_tool.room.bean.node.TaskNode;
import top.bogey.touch_tool.room.bean.node.TextNode;
import top.bogey.touch_tool.room.bean.node.TimeArea;
import top.bogey.touch_tool.room.bean.node.TouchNode;
import top.bogey.touch_tool.ui.picker.ColorPickerFloatView;
import top.bogey.touch_tool.ui.picker.ImagePickerFloatView;
import top.bogey.touch_tool.ui.picker.TouchPickerFloatView;
import top.bogey.touch_tool.ui.picker.WordPickerFloatView;
import top.bogey.touch_tool.utils.DisplayUtils;
import top.bogey.touch_tool.utils.easy_float.EasyFloat;

public class ActionsRecyclerViewAdapter extends RecyclerView.Adapter<ActionsRecyclerViewAdapter.ViewHolder> {
    private final Task task;
    private final List<Node> nodes = new ArrayList<>();

    private int maxCount = 1;

    public ActionsRecyclerViewAdapter(Task task, List<Node> nodes) {
        this.task = task;
        if (nodes != null) {
            for (Node node : nodes) {
                this.nodes.add(node.clone());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FloatActionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.refreshItem(nodes.get(position));
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public void addNode(NodeType type){
        if (nodes.size() < maxCount){
            Node node;
            switch (type) {
                case DELAY:
                    node = new DelayNode(new TimeArea(1000, 1000));
                    break;
                case IMAGE:
                    node = new ImageNode(new ImageNode.ImageInfo(95));
                    break;
                case TOUCH:
                    node = new TouchNode("");
                    break;
                case COLOR:
                    node = new ColorNode(new ColorNode.ColorInfo());
                    break;
                case KEY:
                    node = new KeyNode(AccessibilityService.GLOBAL_ACTION_BACK);
                    break;
                case TASK:
                    node = new TaskNode(null);
                    break;
                default:
                    node = new TextNode("");
                    break;
            }
            nodes.add(node);
            notifyItemInserted(nodes.size() - 1);
        }
    }

    public void setMaxCount(int maxCount){
        this.maxCount = maxCount;
        while (nodes.size() > maxCount){
            nodes.remove(nodes.size() - 1);
            notifyItemRemoved(nodes.size());
        }
    }

    public List<Node> getNodes(){
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            TimeArea timeArea = node.getTimeArea();
            if (timeArea.getRealMin() <= 0) {
                nodes.remove(i);
                continue;
            }

            if (!node.isValid()) nodes.remove(i);
        }
        return nodes;
    }

    protected static class TextChangedWatcher implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder{
        private final FloatActionItemBinding binding;
        private final ArrayAdapter<TaskNode.TaskInfo> adapter;

        public ViewHolder(FloatActionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            initEditText(itemView);

            binding.timeInclude.setTextWatcher((isMin, editable) -> {
                int index = getBindingAdapterPosition();
                Node node = nodes.get(index);
                if (isMin){
                    if (editable != null && editable.length() > 0){
                        node.getTimeArea().setMin(Integer.parseInt(String.valueOf(editable)));
                    } else {
                        node.getTimeArea().setMin(0);
                    }
                } else {
                    if (editable != null && editable.length() > 0){
                        node.getTimeArea().setMax(Integer.parseInt(String.valueOf(editable)));
                    } else {
                        node.getTimeArea().setMax(0);
                    }
                }
            });

            binding.deleteButton.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                nodes.remove(index);
                notifyItemRemoved(index);
            });

            binding.delayInclude.setTextWatcher((isMin, editable) -> {
                int index = getBindingAdapterPosition();
                DelayNode node = (DelayNode) nodes.get(index);
                if (isMin){
                    if (editable != null && editable.length() > 0){
                        node.getValue().setMin(Integer.parseInt(String.valueOf(editable)));
                    } else {
                        node.getValue().setMin(0);
                    }
                } else {
                    if (editable != null && editable.length() > 0){
                        node.getValue().setMax(Integer.parseInt(String.valueOf(editable)));
                    } else {
                        node.getValue().setMax(0);
                    }
                }
            });

            binding.textInclude.textBaseInclude.titleEdit.addTextChangedListener(new TextChangedWatcher(){
                @Override
                public void afterTextChanged(Editable s) {
                    int index = getBindingAdapterPosition();
                    Node node = nodes.get(index);
                    if (node.getType() == NodeType.TEXT) node.setValue(String.valueOf(s));
                }
            });

            binding.textInclude.playButton.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                Node node = nodes.get(index);
                TouchNode touchNode = (TouchNode) node;
                MainAccessibilityService service = MainApplication.getService();
                if (touchNode.isValid()){
                    if (service != null){
                        Task task = new Task();
                        Action action = new Action();
                        action.setTargets(Collections.singletonList(touchNode));
                        task.setActions(Collections.singletonList(action));

                        service.runTask(task, null);
                    } else {
                        Toast.makeText(itemView.getContext(), R.string.capture_service_on_tips_3, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            binding.textInclude.pickerButton.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                Node node = nodes.get(index);
                switch (node.getType()) {
                    case TEXT:
                        TextNode textNode = (TextNode) node;
                        new WordPickerFloatView(itemView.getContext(), picker -> {
                            WordPickerFloatView wordPicker = (WordPickerFloatView) picker;
                            String word = wordPicker.getWord();
                            textNode.setValue(word);
                            binding.textInclude.textBaseInclude.titleEdit.setText(word);
                        }, textNode).show();
                        break;
                    case TOUCH:
                        TouchNode touchNode = (TouchNode) node;
                        new TouchPickerFloatView(itemView.getContext(), picker -> {
                            TouchPickerFloatView touchPicker = (TouchPickerFloatView) picker;
                            List<Point> points = touchPicker.getPoints();
                            touchNode.setValue(itemView.getContext(), points);
                            binding.textInclude.textBaseInclude.titleEdit.setText(touchNode.getTitle());
                        }, touchNode.getPoints(itemView.getContext())).show();
                        break;
                }
            });

            binding.imageInclude.similarText.addTextChangedListener(new TextChangedWatcher(){
                @Override
                public void afterTextChanged(Editable s) {
                    int index = getBindingAdapterPosition();
                    ImageNode node = (ImageNode) nodes.get(index);
                    if (s != null && s.length() > 0){
                        node.getValue().setValue(Integer.parseInt(String.valueOf(s)));
                    } else {
                        node.getValue().setValue(100);
                    }
                }
            });

            binding.imageInclude.pickerButton.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                Node node = nodes.get(index);
                ImageNode imageNode = (ImageNode) node;
                new ImagePickerFloatView(itemView.getContext(), picker -> {
                    ImagePickerFloatView imagePicker = (ImagePickerFloatView) picker;
                    Bitmap bitmap = imagePicker.getBitmap();
                    imageNode.getValue().setBitmap(bitmap, DisplayUtils.getScreenWidth(itemView.getContext()));
                    binding.imageInclude.image.setImageBitmap(bitmap);
                }, imageNode).show();
            });

            binding.colorInclude.pickerButton.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                Node node = nodes.get(index);
                ColorNode colorNode = (ColorNode) node;
                new ColorPickerFloatView(itemView.getContext(), picker -> {
                    ColorPickerFloatView colorPicker = (ColorPickerFloatView) picker;
                    ColorNode.ColorInfo color = colorPicker.getColor();
                    colorNode.setValue(color);
                    binding.colorInclude.colorCard.setCardBackgroundColor(DisplayUtils.getColorFromHsv(colorNode.getValue().getColor()));
                    binding.colorInclude.similarText.setText(colorNode.getTitle());
                }, colorNode).show();
            });

            adapter = new ArrayAdapter<>(itemView.getContext(), R.layout.float_action_spinner_item);
            binding.spinnerInclude.spinner.setAdapter(adapter);
            binding.spinnerInclude.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int index = getBindingAdapterPosition();
                    Node node = nodes.get(index);
                    TaskNode.TaskInfo taskInfo = adapter.getItem(position);
                    if (node.getType() == NodeType.KEY) {
                        node.setValue(Integer.valueOf(taskInfo.getId()));
                    } else if (node.getType() == NodeType.TASK){
                        node.setValue(taskInfo);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            binding.upButton.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                int newIndex = Math.max(0, index - 1);
                nodes.add(newIndex, nodes.remove(index));
                notifyItemRangeChanged(newIndex, 2);
            });

            binding.downButton.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                int newIndex = Math.min(nodes.size() - 1, index + 1);
                nodes.add(newIndex, nodes.remove(index));
                notifyItemRangeChanged(index, 2);
            });
        }

        public void refreshItem(Node node){
            String[] strings = itemView.getContext().getResources().getStringArray(R.array.node_type);
            binding.titleText.setText(strings[node.getType().ordinal()]);

            binding.timeInclude.setValue(node.getTimeArea().getMin(), node.getTimeArea().getMax());

            switch (node.getType()) {
                case DELAY:
                    binding.delayInclude.setVisibility(View.VISIBLE);
                    binding.textInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.imageInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.spinnerInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.colorInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.timeInclude.setVisibility(View.GONE);
                    binding.delayInclude.setValue(((DelayNode) node).getValue().getMin(), ((DelayNode) node).getValue().getMax());
                    break;
                case COLOR:
                    binding.delayInclude.setVisibility(View.INVISIBLE);
                    binding.textInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.imageInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.spinnerInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.colorInclude.getRoot().setVisibility(View.VISIBLE);
                    binding.timeInclude.setVisibility(View.VISIBLE);
                    binding.colorInclude.colorCard.setCardBackgroundColor(DisplayUtils.getColorFromHsv(((ColorNode) node).getValue().getColor()));
                    binding.colorInclude.similarText.setText(((ColorNode) node).getTitle());
                    break;
                case TEXT:
                case TOUCH:
                    binding.delayInclude.setVisibility(View.INVISIBLE);
                    binding.textInclude.getRoot().setVisibility(View.VISIBLE);
                    binding.imageInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.spinnerInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.colorInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.timeInclude.setVisibility(View.VISIBLE);
                    switch (node.getType()) {
                        case TEXT:
                            binding.textInclude.playButton.setVisibility(View.GONE);
                            binding.textInclude.pickerButton.setIconResource(R.drawable.icon_text);
                            binding.textInclude.textBaseInclude.textInputLayout.setEnabled(true);
                            binding.textInclude.textBaseInclude.titleEdit.setText(((TextNode) node).getValue());
                            break;
                        case TOUCH:
                            binding.textInclude.playButton.setVisibility(View.VISIBLE);
                            binding.textInclude.pickerButton.setIconResource(R.drawable.icon_touch);
                            binding.textInclude.textBaseInclude.textInputLayout.setEnabled(false);
                            binding.textInclude.textBaseInclude.titleEdit.setText(((TouchNode) node).getTitle());
                            break;
                    }
                    break;
                case IMAGE:
                    binding.delayInclude.setVisibility(View.INVISIBLE);
                    binding.textInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.imageInclude.getRoot().setVisibility(View.VISIBLE);
                    binding.spinnerInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.colorInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.timeInclude.setVisibility(View.VISIBLE);
                    ImageNode.ImageInfo imageInfo = ((ImageNode) node).getValue();
                    binding.imageInclude.similarText.setText(String.valueOf(imageInfo.getValue()));
                    if (imageInfo.getBitmap() != null){
                        binding.imageInclude.image.setImageBitmap(imageInfo.getBitmap());
                    }
                    break;
                case KEY:
                case TASK:
                    binding.delayInclude.setVisibility(View.INVISIBLE);
                    binding.textInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.imageInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.spinnerInclude.getRoot().setVisibility(View.VISIBLE);
                    binding.colorInclude.getRoot().setVisibility(View.INVISIBLE);
                    binding.timeInclude.setVisibility(View.GONE);
                    if (node.getType() == NodeType.KEY){
                        String[] ids = itemView.getContext().getResources().getStringArray(R.array.key_ids);
                        String[] keys = itemView.getContext().getResources().getStringArray(R.array.keys);
                        adapter.clear();
                        for (int i = 0; i < ids.length; i++) {
                            adapter.add(new TaskNode.TaskInfo(String.valueOf(ids[i]), keys[i]));
                        }
                        selectSpinner(String.valueOf(((KeyNode) node).getValue()));
                    } else {
                        adapter.clear();
                        MainViewModel viewModel = new ViewModelProvider(MainApplication.getActivity()).get(MainViewModel.class);

                        for (Task taskItem : viewModel.getAllTasks()) {
                            if (taskItem.getStatus() == TaskStatus.CLOSED
                                    && !taskItem.getId().equals(task.getId())
                                    && (taskItem.getPkgName().equals(task.getPkgName()) || taskItem.getPkgName().equals(itemView.getContext().getString(R.string.common_package_name)))){
                                adapter.add(new TaskNode.TaskInfo(taskItem.getId(), taskItem.getTitle()));
                            }
                        }

                        TaskNode.TaskInfo taskInfo = ((TaskNode) node).getValue();
                        if (taskInfo == null){
                            selectSpinner("");
                        } else {
                            selectSpinner(taskInfo.getId());
                        }
                    }
                    break;
            }
        }

        private void initEditText(View view){
            if (view instanceof ViewGroup){
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    initEditText(viewGroup.getChildAt(i));
                }
            } else {
                if (view instanceof EditText){
                    EasyFloat.initInput((EditText) view, ActionFloatView.class.getCanonicalName());
                }
            }
        }

        private void selectSpinner(String id){
            Spinner spinner = binding.spinnerInclude.spinner;
            for (int i = 0; i < adapter.getCount(); i++) {
                TaskNode.TaskInfo item = adapter.getItem(i);
                if (item.getId().equals(id)) {
                    spinner.setSelection(i);
                    return;
                }
            }
            if (adapter.getCount() > 0){
                spinner.setSelection(0);
                AdapterView.OnItemSelectedListener listener = spinner.getOnItemSelectedListener();
                listener.onItemSelected(spinner, spinner.getSelectedView(), 0, adapter.getItemId(0));
            }
        }
    }
}