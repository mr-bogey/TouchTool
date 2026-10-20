package top.bogey.touch_tool.ui.setting.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.ShapeAppearanceModel;

import top.bogey.touch_tool.R;

/**
 * 分组行圆角：分组首行顶部、末行底部用大圆角，中间行与整行只留小圆角
 */
public class SettingRowCorner {
    // 分组首末行的大圆角半径，与整页主按钮保持一致
    private static final float LARGE_SIZE = 20f;
    // 分组中间行、以及相邻行之间拼合处的小圆角半径
    private static final float SMALL_SIZE = 8f;

    private SettingRowCorner() {
    }

    /**
     * 按 app:rowCorner 的标注给行卡片设置圆角，未标注时按中间行处理
     */
    public static void apply(Context context, AttributeSet attrs, MaterialCardView card) {
        int corner = 1;
        if (attrs != null) {
            try (TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{R.attr.rowCorner})) {
                corner = typedArray.getInt(0, 1);
            }
        }

        float density = context.getResources().getDisplayMetrics().density;
        float large = LARGE_SIZE * density;
        float small = SMALL_SIZE * density;

        ShapeAppearanceModel.Builder builder = card.getShapeAppearanceModel().toBuilder();
        switch (corner) {
            case 0: // 分组首行：顶部大圆角
                builder.setTopLeftCornerSize(large);
                builder.setTopRightCornerSize(large);
                builder.setBottomLeftCornerSize(small);
                builder.setBottomRightCornerSize(small);
                break;
            case 2: // 分组末行：底部大圆角
                builder.setTopLeftCornerSize(small);
                builder.setTopRightCornerSize(small);
                builder.setBottomLeftCornerSize(large);
                builder.setBottomRightCornerSize(large);
                break;
            case 3: // 分组只有一行
                builder.setAllCornerSizes(large);
                break;
            default: // 分组中间行
                builder.setAllCornerSizes(small);
                break;
        }
        card.setShapeAppearanceModel(builder.build());
    }
}
