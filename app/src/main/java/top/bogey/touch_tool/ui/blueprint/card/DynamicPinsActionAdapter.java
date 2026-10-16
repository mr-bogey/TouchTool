package top.bogey.touch_tool.ui.blueprint.card;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.ui.blueprint.pin.PinBottomView;
import top.bogey.touch_tool.ui.blueprint.pin.PinLeftView;
import top.bogey.touch_tool.ui.blueprint.pin.PinRightView;
import top.bogey.touch_tool.ui.blueprint.pin.PinTopView;
import top.bogey.touch_tool.ui.blueprint.pin.PinView;
import top.bogey.touch_tool.utils.ui.DragViewHolderHelper;
import top.bogey.touch_tool.utils.ui.IDragAbleRecycleViewAdapter;
import top.bogey.touch_tool.utils.ui.NoScrollLinearLayoutManager;

// 一个方位上的全部针脚，界面顺序与动作的针脚列表保持一致
// 动态添加的针脚长按可以调换顺序，默认针脚没有拖动柄、位置固定
// 一次添加多个针脚时（如点加时长）同组的针脚并作一行，拖动时整行一起移动
public class DynamicPinsActionAdapter extends RecyclerView.Adapter<DynamicPinsActionAdapter.ViewHolder> implements IDragAbleRecycleViewAdapter {
    private final List<List<PinView>> rows = new ArrayList<>();
    private final ActionCard card;
    private final boolean vertical;
    private final boolean out;
    private final int groupSize;
    private final ItemTouchHelper touchHelper;

    // vertical 为针脚自身的朝向，out 为是否输出针脚，两者决定这个列表装哪些针脚
    // groupSize 为一次添加的针脚数量，同组的针脚放在同一行
    public DynamicPinsActionAdapter(ActionCard card, boolean vertical, boolean out, int groupSize) {
        this.card = card;
        this.vertical = vertical;
        this.out = out;
        this.groupSize = Math.max(1, groupSize);
        // 针脚有横竖两种排布方向，拖动也只允许沿这个方向
        // 长按整行拖动，拖动柄只作为可拖动位置的视觉提示
        int dirs = vertical ? DragViewHolderHelper.HORIZONTAL : DragViewHolderHelper.VERTICAL;
        touchHelper = new ItemTouchHelper(new DragViewHolderHelper(dirs, this) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // 默认针脚所在的行不参与排序，长按和拖动柄都不生效
                int position = viewHolder.getBindingAdapterPosition();
                if (position < 0 || position >= rows.size() || !isDragRow(rows.get(position))) {
                    return makeMovementFlags(0, 0);
                }
                return super.getMovementFlags(recyclerView, viewHolder);
            }
        });
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        // 竖直针脚横向排布，普通针脚纵向排布
        int orientation = vertical ? RecyclerView.HORIZONTAL : RecyclerView.VERTICAL;
        // 列表按内容撑开，本来就没有可滚动的余量，留着滚动只会把针脚里文本框的手势抢走
        recyclerView.setLayoutManager(new NoScrollLinearLayoutManager(recyclerView.getContext(), orientation, false));
        recyclerView.setAdapter(this);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    // 按动作的针脚列表重建行，保证界面顺序与动作数据始终一致
    public void refreshRows() {
        rows.clear();
        List<PinView> group = null;
        int dynamicCount = 0;
        for (Pin pin : card.getAction().getPins()) {
            if (!belongs(pin)) continue;
            PinView pinView = card.getPinView(pin.getId());
            if (pinView == null) continue;

            // 成组添加的针脚并入同一行
            if (pin.isDynamic() && groupSize > 1) {
                if (group == null || dynamicCount % groupSize == 0) {
                    group = new ArrayList<>();
                    rows.add(group);
                }
                group.add(pinView);
                dynamicCount++;
            } else {
                group = null;
                List<PinView> row = new ArrayList<>();
                row.add(pinView);
                rows.add(row);
            }
        }
        notifyDataSetChanged();
    }

    // 这个针脚是否归当前列表管
    private boolean belongs(Pin pin) {
        return pin.isVertical() == vertical && pin.isOut() == out;
    }

    // 按针脚样式创建视图，拖动由卡片的长按拖动统一处理
    public PinView addPin(Pin pin) {
        if (pin.isOut()) {
            if (pin.isVertical()) {
                return new PinBottomView(card.getContext(), card, pin);
            } else {
                return new PinRightView(card.getContext(), card, pin);
            }
        } else {
            if (pin.isVertical()) {
                return new PinTopView(card.getContext(), card, pin);
            } else {
                return new PinLeftView(card.getContext(), card, pin);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frame = new FrameLayout(parent.getContext());
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(vertical ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        // 行在交叉轴方向占满，子项才能在交叉轴上做顶部/底部、左侧/右侧对齐
        if (vertical) {
            frame.addView(row, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            frame.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            frame.addView(row, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            frame.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return new ViewHolder(frame, row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.refresh(rows.get(position), vertical, out);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @Override
    public void swap(int from, int to) {
        // 拖动过程中位置可能失效，越界直接忽略
        if (from == to || from < 0 || to < 0 || from >= rows.size() || to >= rows.size()) return;
        // 只有整行都是动态针脚时才允许排序，且拖动区间里不能夹着默认针脚
        for (int i = Math.min(from, to); i <= Math.max(from, to); i++) {
            if (!isDragRow(rows.get(i))) return;
        }

        // 参与排序的针脚可能在针脚列表里并不连续，先把它们占用的位置记下来
        List<Pin> oldPins = collectDragPins();
        List<Pin> pins = card.getAction().getPins();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < pins.size(); i++) {
            if (containsSame(oldPins, pins.get(i))) slots.add(i);
        }
        if (slots.size() != oldPins.size()) return;

        rows.add(to, rows.remove(from));

        // 只把这些针脚按新的顺序写回它们原本占用的位置，默认针脚不动
        List<Pin> newPins = collectDragPins();
        for (int i = 0; i < slots.size(); i++) {
            pins.set(slots.get(i), newPins.get(i));
        }

        notifyItemMoved(from, to);
    }

    // 按当前行顺序收集参与排序的针脚
    private List<Pin> collectDragPins() {
        List<Pin> pins = new ArrayList<>();
        for (List<PinView> row : rows) {
            if (!isDragRow(row)) continue;
            for (PinView pinView : row) pins.add(pinView.getPin());
        }
        return pins;
    }

    private boolean containsSame(List<Pin> pins, Pin pin) {
        for (Pin item : pins) {
            if (item == pin) return true;
        }
        return false;
    }

    private boolean isDragRow(List<PinView> row) {
        for (PinView pinView : row) {
            if (!pinView.getPin().isDynamic()) return false;
        }
        return true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout row;

        public ViewHolder(@NonNull FrameLayout itemView, LinearLayout row) {
            super(itemView);
            this.row = row;
        }

        public void refresh(List<PinView> pinViews, boolean vertical, boolean out) {
            row.removeAllViews();
            for (PinView pinView : pinViews) {
                ViewGroup parent = (ViewGroup) pinView.getParent();
                if (parent != null) parent.removeView(pinView);
                row.addView(pinView);
            }

            // 上方针脚靠顶、下方针脚靠底，输入针脚靠左、输出针脚靠右
            int gravity = vertical
                    ? (out ? Gravity.BOTTOM : Gravity.TOP)
                    : (out ? Gravity.END : Gravity.START);
            row.setGravity(gravity);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) row.getLayoutParams();
            params.gravity = gravity;
            row.setLayoutParams(params);
        }
    }
}
