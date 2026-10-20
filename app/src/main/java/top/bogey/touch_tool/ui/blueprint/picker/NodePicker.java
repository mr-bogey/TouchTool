package top.bogey.touch_tool.ui.blueprint.picker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.other.NodeInfo;
import top.bogey.touch_tool.bean.other.SavedScreenNodeInfo;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_string.PinNodePathString;
import top.bogey.touch_tool.bean.save.setting.SettingSaver;
import top.bogey.touch_tool.databinding.FloatPickerNodeBinding;
import top.bogey.touch_tool.service.TaskInfoSummary;
import top.bogey.touch_tool.ui.MainActivity;
import top.bogey.touch_tool.ui.custom.float_view.KeepAliveFloatView;
import top.bogey.touch_tool.ui.custom.float_view.NodeInfoFloatView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.DisplayUtil;
import top.bogey.touch_tool.utils.GsonUtil;
import top.bogey.touch_tool.utils.callback.ResultCallback;
import top.bogey.touch_tool.utils.float_window_manager.FloatWindow;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class NodePicker extends FullScreenPicker<NodeInfo> implements NodePickerTreeAdapter.SelectNode {
    private final FloatPickerNodeBinding binding;
    private final NodePickerTreeAdapter adapter;

    private List<NodeInfo> roots = NodeInfo.getWindows();

    private final Paint gridPaint;
    private final Paint markPaint;
    private final Paint bitmapPaint;

    private final PinNodePathString nodePath;
    private NodeInfo nodeInfo = null;

    private List<NodeInfo> cachedFindNodes = new ArrayList<>();

    public static void showPicker(ResultCallback<NodeInfo> callback) {
        KeepAliveFloatView keepView = (KeepAliveFloatView) FloatWindow.getView(KeepAliveFloatView.class.getName());
        if (keepView == null) return;
        new Handler(Looper.getMainLooper()).post(() -> {
            NodePicker nodePicker = new NodePicker(keepView.getThemeContext(), callback, "");
            nodePicker.setFloatCallback(new NodePickerCallback(nodePicker));
            nodePicker.show();
        });
    }

    public NodePicker(@NonNull Context context, ResultCallback<NodeInfo> callback, String path) {
        super(context, callback);

        editable = true;
        nodePath = new PinNodePathString(path);
        binding = FloatPickerNodeBinding.inflate(LayoutInflater.from(context), this, true);

        adapter = new NodePickerTreeAdapter(this, roots);
        binding.widgetRecyclerView.setAdapter(adapter);
        // 不知道为啥，webview需要第二次才能正常显示节点
        postDelayed(() -> {
            roots = NodeInfo.getWindows();
            adapter.setRoots(roots);
        }, 50);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStrokeCap(Paint.Cap.ROUND);
        gridPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPaint.setStyle(Paint.Style.STROKE);

        markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);

        binding.saveButton.setOnClickListener(v -> {
            if (callback != null) callback.onResult(nodeInfo);
            dismiss();
        });

        BottomSheetBehavior<?> sheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        binding.detailButton.setOnClickListener(v -> sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        binding.backButton.setOnClickListener(v -> {
            if (callback != null) callback.onResult(null);
            dismiss();
        });

        binding.upButton.setOnClickListener(v -> {
            if (nodeInfo != null) {
                NodeInfo node = nodeInfo.getParent();
                if (node != null) selectNode(node);
            }
        });

        binding.downButton.setOnClickListener(v -> {
            if (nodeInfo != null) {
                NodeInfo node = nodeInfo.getChild(0);
                if (node != null) selectNode(node);
            }
        });

        binding.leftButton.setOnClickListener(v -> {
            if (nodeInfo != null) {
                NodeInfo parent = nodeInfo.getParent();
                if (parent == null) return;
                int childCount = parent.getChildCount();
                int nextIndex = nodeInfo.index - 2;
                if (nextIndex < 0) nextIndex = childCount - 1;
                NodeInfo node = parent.getChild(nextIndex);
                if (node != null) selectNode(node);
            }
        });

        binding.rightButton.setOnClickListener(v -> {
            if (nodeInfo != null) {
                NodeInfo parent = nodeInfo.getParent();
                if (parent == null) return;
                int childCount = parent.getChildCount();
                int nextIndex = nodeInfo.index;
                if (nextIndex >= childCount) nextIndex = 0;
                NodeInfo node = parent.getChild(nextIndex);
                if (node != null) selectNode(node);
            }
        });

        binding.infoButton.setOnClickListener(v -> {
            if (nodeInfo == null) return;
            NodeInfoFloatView.showInfo(nodeInfo, this::selectNode);
        });

        binding.searchEdit.addTextChangedListener(new TextChangedListener() {
            @Override
            public void afterTextChanged(Editable s) {
                adapter.searchNodes(s.toString());
            }
        });

        String[] strings = context.getResources().getStringArray(R.array.picker_node_type);
        binding.typeButton.setOnClickListener(v -> {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.widget_textview_item, strings);
            ListPopupWindow popup = new ListPopupWindow(context);
            popup.setAdapter(arrayAdapter);
            popup.setAnchorView(binding.typeButton);
            popup.setModal(true);
            popup.setOnItemClickListener((parent, view, position, id) -> {
                SettingSaver.NODE_PICKER_PICK_TYPE.set(position);
                binding.typeButton.setText(strings[position]);
                invalidate();
                popup.dismiss();
            });
            popup.show();
        });
        binding.typeButton.setText(strings[SettingSaver.NODE_PICKER_PICK_TYPE.get()]);

        binding.exportButton.setOnClickListener(v -> {
            if (screenInfo == null) return;

            MainActivity activity = MainApplication.getInstance().getActivity();
            if (activity == null) return;

            ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            try {
                manager.moveTaskToFront(activity.getTaskId(), 0);
            } catch (Exception e) {
                AppLog.e(e);
            }

            FloatWindow.hide(tag);

            SavedScreenNodeInfo info = new SavedScreenNodeInfo(screenInfo.getScreenShot(), roots);
            String json = GsonUtil.toJson(info);

            TaskInfoSummary.PackageActivity packageActivity = TaskInfoSummary.getInstance().getPackageActivity();
            String appName = TaskInfoSummary.getInstance().getAppName(packageActivity.packageName());

            AppUtil.exportFile(activity, appName + ".ttl", json.getBytes(), result -> {
                FloatWindow.show(tag);
                activity.moveTaskToBack(true);
            });
        });

        binding.importButton.setOnClickListener(v -> {
            if (screenInfo == null) return;

            MainActivity activity = MainApplication.getInstance().getActivity();
            if (activity == null) return;

            ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            try {
                manager.moveTaskToFront(activity.getTaskId(), 0);
            } catch (Exception e) {
                AppLog.e(e);
            }

            FloatWindow.hide(tag);

            activity.launcherOpenDocument(((code, intent) -> {
                if (code == Activity.RESULT_OK && intent != null) {
                    byte[] bytes = AppUtil.readFile(activity, intent.getData());
                    SavedScreenNodeInfo info = GsonUtil.getAsObject(new String(bytes), SavedScreenNodeInfo.class, null);
                    if (info != null && info.isValid()) {
                        screenInfo.setScreenShot(info.getImage());
                        roots = info.getRoots();
                        adapter.setRoots(roots);
                    }
                }
                FloatWindow.show(tag);
                activity.moveTaskToBack(true);
            }), "*/*");
        });
    }

    @Override
    protected void realShow() {
        if (adapter == null) return;
        nodeInfo = nodePath.findNode(roots, true);
        selectNode(nodeInfo);
        adapter.setSelectedNode(nodeInfo);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            DisplayUtil.setViewHeight(binding.bottomSheet, (int) (getHeight() * 0.7f));
        }
    }

    public void selectNode(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;

        binding.markBox.setVisibility(GONE);
        binding.idTitle.setVisibility(GONE);

        if (nodeInfo != null) {
            binding.markBox.setVisibility(VISIBLE);
            binding.idTitle.setVisibility(VISIBLE);

            binding.idTitle.setText(nodeInfo.id);

            Rect area = new Rect(nodeInfo.area);
            area.offset(-location[0], -location[1]);

            // 限制区域在屏幕内
            area.left = Math.max(0, area.left);
            area.right = Math.min(getWidth(), area.right);
            area.top = Math.max(0, area.top);
            area.bottom = Math.min(getHeight(), area.bottom);

            DisplayUtil.setViewWidth(binding.markBox, area.width());
            DisplayUtil.setViewHeight(binding.markBox, area.height());
            binding.markBox.setX(area.left);
            binding.markBox.setY(area.top);

            float x = area.left + (area.width() - binding.buttonBox.getWidth()) / 2f;
            x = Math.clamp(x, 0, getWidth() - binding.buttonBox.getWidth());
            binding.buttonBox.setX(x);

            float offset = DisplayUtil.dp2px(getContext(), 8);
            if (area.height() * 2 > getHeight()) {
                // 内部
                binding.buttonBox.setY(area.bottom - binding.buttonBox.getHeight() - offset);
            } else if (getHeight() < area.bottom + binding.buttonBox.getHeight() + offset) {
                // 外部 顶上
                binding.buttonBox.setY(area.top - binding.buttonBox.getHeight() - offset);
            } else {
                // 外部 底下
                binding.buttonBox.setY(area.bottom + offset);
            }
        } else {
            binding.buttonBox.setX((getWidth() - binding.buttonBox.getWidth()) / 2f);
            binding.buttonBox.setY(getHeight() - DisplayUtil.dp2px(getContext(), 64) - binding.buttonBox.getHeight());
        }
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        if (screenInfo != null) {
            Bitmap screenShot = screenInfo.getScreenShot();
            if (screenShot != null) canvas.drawBitmap(screenShot, 0, 0, bitmapPaint);
        }

        canvas.saveLayer(getLeft(), getTop(), getRight(), getBottom(), bitmapPaint);
        super.dispatchDraw(canvas);
        if (nodeInfo != null) {
            Rect area = new Rect(nodeInfo.area);
            area.offset(-location[0], -location[1]);
            canvas.drawRect(area, markPaint);

            drawChild(canvas, binding.markBox, getDrawingTime());
            drawChild(canvas, binding.idTitle, getDrawingTime());
        }
        canvas.restore();

        for (int i = roots.size() - 1; i >= 0; i--) {
            NodeInfo root = roots.get(i);
            Rect area = new Rect(root.area);
            area.offset(-location[0], -location[1]);
            if (area.width() != getWidth() || area.height() != getHeight()) {
                canvas.drawRect(area, gridPaint);
            }
            drawNode(canvas, root);
        }

        drawChild(canvas, binding.buttonBox, getDrawingTime());
        drawChild(canvas, binding.bottomSheet, getDrawingTime());
    }

    private void drawNode(Canvas canvas, NodeInfo node) {
        if (node == null) return;
        if (!node.visible) return;

        Rect area = new Rect(node.area);
        area.offset(-location[0], -location[1]);

        int type = SettingSaver.NODE_PICKER_PICK_TYPE.get();
        if (type != 0 && !node.usable) {
            gridPaint.setColor(DisplayUtil.getAttrColor(getContext(), com.google.android.material.R.attr.colorSecondary));
            gridPaint.setStrokeWidth(1);
            canvas.drawRect(area, gridPaint);
        }

        for (NodeInfo child : node.getCacheChildren()) {
            drawNode(canvas, child);
        }

        if (node.usable) {
            gridPaint.setColor(DisplayUtil.getAttrColor(getContext(), R.attr.colorPrimaryLight));
            gridPaint.setStrokeWidth(3);
            area.offset(2, 2);
            area.right -= 4;
            area.bottom -= 4;
            canvas.drawRect(area, gridPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();

        if (event.getAction() == MotionEvent.ACTION_UP) {
            int type = SettingSaver.NODE_PICKER_PICK_TYPE.get();
            NodeInfo node = null;
            switch (type) {
                case 0 -> node = findNodeByTop(Math.round(x), Math.round(y), true);
                case 1 -> node = findNodeByTop(Math.round(x), Math.round(y), false);
                case 2 -> node = findNodeByDepth(Math.round(x), Math.round(y), false);
            }
            if (node != null) {
                selectNode(node);
                adapter.setSelectedNode(node);
            }
        }
        return true;
    }

    private NodeInfo findNodeByTop(int x, int y, boolean justUsable) {
        for (NodeInfo root : roots) {
            List<NodeInfo> children = root.findChildren(new Rect(x, y, x, y), justUsable);
            if (children.isEmpty()) continue;
            if (cachedFindNodes.equals(children)) {
                int index = children.indexOf(nodeInfo);
                if (index <= 0) return children.get(children.size() - 1);
                return children.get(index - 1);
            }
            cachedFindNodes = children;
            return children.get(children.size() - 1);
        }
        return null;
    }

    private NodeInfo findNodeByDepth(int x, int y, boolean justUsable) {
        Map<NodeInfo, Integer> map = new HashMap<>();
        for (int i = 0; i < roots.size(); i++) {
            NodeInfo root = roots.get(i);
            root.mapChildrenDepth(map, new Rect(x, y, x, y), (roots.size() - i) * Byte.MAX_VALUE);
        }

        List<NodeInfo> children = new ArrayList<>();
        for (Map.Entry<NodeInfo, Integer> entry : map.entrySet()) {
            if (!entry.getKey().visible) continue;
            if (!justUsable || entry.getKey().usable) {
                children.add(entry.getKey());
            }
        }

        if (children.isEmpty()) return null;

        children.sort(Comparator.comparingInt(map::get));

        if (cachedFindNodes.equals(children)) {
            int index = children.indexOf(nodeInfo);
            if (index <= 0) return children.get(children.size() - 1);
            return children.get(index - 1);
        }
        cachedFindNodes = children;
        return children.get(children.size() - 1);
    }

    private static class NodePickerCallback extends FullScreenPickerCallback {

        public NodePickerCallback(FullScreenPicker<?> picker) {
            super(picker);
        }

        @Override
        public void onDismiss() {

        }
    }
}
