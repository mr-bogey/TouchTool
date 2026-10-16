package top.bogey.touch_tool.utils.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

// 不响应滚动的列表管理器
// 针脚列表按内容撑开，本来就没有可滚动的余量，留着滚动只会把针脚里文本框的手势抢走
public class NoScrollLinearLayoutManager extends LinearLayoutManager {
    public NoScrollLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public NoScrollLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }
}
