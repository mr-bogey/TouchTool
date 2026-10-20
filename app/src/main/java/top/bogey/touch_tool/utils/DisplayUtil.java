package top.bogey.touch_tool.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.ListPopupWindow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DisplayUtil {
    private static int statusHeight = -1;

    @ColorInt
    public static int getAttrColor(Context context, int id) {
        int[] attrs = {id};
        try (TypedArray typedArray = context.obtainStyledAttributes(attrs)) {
            int resourceId = typedArray.getResourceId(0, 0);
            return context.getColor(resourceId);
        } catch (Exception e) {
            return 0;
        }
    }

    @ColorInt
    public static int blendColor(int color1, int color2, float ratio) {
        ratio = Math.clamp(ratio, 0f, 1f);
        final float inverseRatio = 1f - ratio;
        int a = (int) (Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio);
        int r = (int) (Color.red(color1) * inverseRatio + Color.red(color2) * ratio);
        int g = (int) (Color.green(color1) * inverseRatio + Color.green(color2) * ratio);
        int b = (int) (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio);
        return Color.argb(a, r, g, b);
    }

    @ColorInt
    public static int getTextColor(@ColorInt int backgroundColor) {
        return Color.luminance(backgroundColor) > 0.5 ? Color.BLACK : Color.WHITE;
    }

    public static float dp2px(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float sp2px(Context context, float sp) {
        return sp * context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static boolean isPortrait(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isInFreeFormMode(Activity activity) {
        if (activity.isInMultiWindowMode()) return true;
        Size screenSize = getScreenSize(activity);

        View decorView = activity.getWindow().getDecorView();
        Rect rect = new Rect();
        decorView.getDrawingRect(rect);
        return screenSize.getWidth() != rect.width() || screenSize.getHeight() != rect.height();
    }

    public static Rect getScreenArea(Context context) {
        Size size = getScreenSize(context);
        return new Rect(0, 0, size.getWidth(), size.getHeight());
    }

    public static Size getScreenSize(Context context) {
        Point point = new Point();
        // 获取屏幕宽高
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        return new Size(point.x, point.y);
    }

    public static int getScreenWidth(Context context) {
        Size size = getScreenSize(context);
        return Math.min(size.getWidth(), size.getHeight());
    }

    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        return statusBarHeight;
    }

    public static int getStatusBarHeight(View view, WindowManager.LayoutParams params) {
        if (statusHeight >= 0) return statusHeight;
        if (view == null) return 0;
        if (params == null) return getStatusBarHeight(view.getContext());

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        // 绝对坐标与相对坐标一致，代表状态栏高度为0
        statusHeight = location[0] + location[1] - params.x - params.y;
        return statusHeight;
    }

    public static void resetStatusBarHeight() {
        statusHeight = -1;
    }

    // 不用getLocationOnScreen，因为这种方法没有考虑缩放
    public static PointF getLocationRelativeToView(View view, View relativeView) {
        PointF pointF = new PointF(view.getX(), view.getY());
        ViewParent viewParent = view.getParent();
        while (viewParent != null && viewParent != relativeView) {
            View parentView = (View) viewParent;
            pointF.offset(parentView.getX(), parentView.getY());
            viewParent = viewParent.getParent();
        }
        return pointF;
    }

    public static void setViewWidth(View view, int width) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) params = new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        else params.width = width;
        view.setLayoutParams(params);
    }

    public static void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height);
        else params.height = height;
        view.setLayoutParams(params);
    }

    public static void setViewMargin(View view, int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (params == null) return;
        params.setMargins(left, top, right, bottom);
        view.setLayoutParams(params);
    }

    public static void setListPopupWindowAutoHeight(ListPopupWindow popup) {
        if (popup == null) return;

        ListView listView = popup.getListView();
        View anchorView = popup.getAnchorView();
        if (listView == null || anchorView == null) return;

        Context context = listView.getContext();
        int padding = 0;
        Drawable background = popup.getBackground();
        if (background != null) {
            Rect rect = new Rect();
            background.getPadding(rect);
            padding = rect.top + rect.bottom;
        }
        int height = listView.getHeight() + listView.getPaddingTop() + listView.getPaddingBottom() + padding;

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        Size screenSize = DisplayUtil.getScreenSize(context);
        int maxHeight = screenSize.getHeight() - location[1] - anchorView.getHeight();
        int minHeight = (int) DisplayUtil.dp2px(context, 32);
        height = Math.clamp(height, minHeight, maxHeight);
        popup.setHeight(height);
        popup.show();
    }

    public static Rect getPointsArea(List<Point> points) {
        Rect rect = new Rect();
        boolean init = false;
        for (Point point : points) {
            if (init) {
                rect.left = Math.min(rect.left, point.x);
                rect.top = Math.min(rect.top, point.y);
                rect.right = Math.max(rect.right, point.x);
                rect.bottom = Math.max(rect.bottom, point.y);
            } else {
                rect.set(point.x, point.y, point.x, point.y);
                init = true;
            }
        }
        return rect;
    }

    public static RectF getPointFsArea(List<PointF> points) {
        RectF rect = new RectF();
        boolean init = false;
        for (PointF point : points) {
            if (init) {
                rect.left = Math.min(rect.left, point.x);
                rect.top = Math.min(rect.top, point.y);
                rect.right = Math.max(rect.right, point.x);
                rect.bottom = Math.max(rect.bottom, point.y);
            } else {
                rect.set(point.x, point.y, point.x, point.y);
                init = true;
            }
        }
        return rect;
    }

    public static Bitmap createScaledBitmap(Bitmap bitmap, int width, int height) {
        final int srcWidth = bitmap.getWidth();
        final int srcHeight = bitmap.getHeight();
        if (srcWidth == width && srcHeight == height) return bitmap;

        final float scaleX = width / (float) srcWidth;
        final float scaleY = height / (float) srcHeight;
        final float scale = Math.min(scaleX, scaleY);
        final Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, srcWidth, srcHeight, matrix, true);
    }

    public static Bitmap safeScaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null || bitmap.isRecycled()) return null;
        final int srcWidth = bitmap.getWidth();
        final int srcHeight = bitmap.getHeight();
        if (srcWidth <= maxWidth && srcHeight <= maxHeight) return bitmap;
        return createScaledBitmap(bitmap, maxWidth, maxHeight);
    }

    public static Bitmap safeClipBitmap(Bitmap bitmap, int x, int y, int width, int height) {
        Rect area = safeClipBitmapArea(bitmap, x, y, width, height);
        if (area == null) return null;
        return Bitmap.createBitmap(bitmap, area.left, area.top, area.width(), area.height());
    }

    public static Rect safeClipBitmapArea(Bitmap bitmap, int x, int y, int width, int height) {
        if (bitmap == null || bitmap.isRecycled()) return null;
        if (x < 0) {
            width += x;
            x = 0;
        }
        if (y < 0) {
            height += y;
            y = 0;
        }
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        if (x > bitmapWidth) return null;
        if (y > bitmapHeight) return null;
        if (x + width > bitmapWidth) width = bitmapWidth - x;
        if (y + height > bitmapHeight) height = bitmapHeight - y;
        // 裁剪后宽高可能为零或负数，此时没有可截取的区域
        if (width <= 0 || height <= 0) return null;
        return new Rect(x, y, x + width, y + height);
    }


    public static Bitmap createTextBitmap(Context context, String text, int textColor, int textSizeSp, int maxWidth, int lineSpacing, int padding) {
        // 1. 初始化Paint
        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setTextSize(sp2px(context, textSizeSp));
        paint.setAntiAlias(true);

        // 2. 分割文本为多行并记录每行实际宽度
        String[] paragraphs = text.split("\n", -1); // -1保留空行

        List<String> lines = new ArrayList<>();
        List<Float> lineWidths = new ArrayList<>();
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add(" ");
                lineWidths.add(0f);
                continue;
            }

            int start = 0;
            while (start < paragraph.length()) {
                // 测量能显示多少个字符
                int count = paint.breakText(paragraph, start, paragraph.length(), true, maxWidth - 2 * padding, null);
                String line = paragraph.substring(start, start + count);
                lines.add(line);

                // 记录每行实际宽度
                float lineWidth = paint.measureText(line);
                lineWidths.add(lineWidth);

                start += count;
            }
        }

        // 3. 计算实际需要的宽度（取最长行宽度）
        float maxLineWidth = 0;
        for (float width : lineWidths) {
            if (width > maxLineWidth) {
                maxLineWidth = width;
            }
        }
        int totalWidth = (int) (maxLineWidth + 2 * padding);

        // 4. 测量总高度
        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        int totalHeight = (int) (lines.size() * lineHeight + (lines.size() - 1) * lineSpacing + 2 * padding);

        // 5. 创建Bitmap（使用实际需要的宽度而非最大宽度）
        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 6. 逐行绘制文本
        float y = padding - fm.ascent; // 初始y位置
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            canvas.drawText(line, padding, y, paint);
            y += lineHeight + lineSpacing;
        }

        return bitmap;
    }

    public static int measureArrayAdapterContentWidth(Context context, ArrayAdapter<?> adapter) {
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < adapter.getCount(); i++) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, new FrameLayout(context));
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            maxWidth = Math.max(maxWidth, itemView.getMeasuredWidth());
        }

        return maxWidth;
    }

    public static native MatchResult nativeMatchTemplate(Bitmap bitmap, Bitmap template, int speed, boolean canny);

    public static Rect matchTemplate(Bitmap bitmap, Bitmap template, Rect area, int similarity) {
        return matchTemplate(bitmap, template, area, similarity, 0, false);
    }

    public static Rect matchTemplate(Bitmap bitmap, Bitmap template, Rect area, int similarity, int speed, boolean canny) {
        MatchResult matchResult = matchTemplateResult(bitmap, template, area, similarity, speed, canny);
        if (matchResult == null) return null;
        return matchResult.area;
    }

    public static MatchResult matchTemplateResult(Bitmap bitmap, Bitmap template, Rect area, int similarity) {
        return matchTemplateResult(bitmap, template, area, similarity, 0, false);
    }

    public static MatchResult matchTemplateResult(Bitmap bitmap, Bitmap template, Rect area, int similarity, int speed, boolean canny) {
        if (bitmap == null) return null;
        if (template == null) return null;
        // 如果图片尺寸小于模板尺寸，则不匹配
        if (bitmap.getWidth() < template.getWidth() || bitmap.getHeight() < template.getHeight()) return null;

        if (area == null) area = new Rect();

        Bitmap tmp = null;
        if (!area.isEmpty()) {
            bitmap = safeClipBitmap(bitmap, area.left, area.top, area.width(), area.height());
            tmp = bitmap;
            if (bitmap == null) return null;
        }

        MatchResult matchResult = nativeMatchTemplate(bitmap, template, (int) Math.pow(2, speed), canny);
        if (tmp != null) tmp.recycle();
        if (matchResult == null) return null;
        AppLog.d("图像", "matchTemplateResult: value = " + matchResult.value);
        if (matchResult.value * 100 < similarity) return null;
        matchResult.area.offset(area.left, area.top);
        return matchResult;
    }

    public static native List<MatchResult> nativeMatchAllTemplate(Bitmap bitmap, Bitmap template, int similarity, int speed, boolean canny);

    public static synchronized List<MatchResult> matchAllTemplate(Bitmap bitmap, Bitmap template, Rect area, int similarity, int speed, boolean canny) {
        if (bitmap == null) return null;
        if (template == null) return null;
        // 如果图片尺寸小于模板尺寸，则不匹配
        if (bitmap.getWidth() < template.getWidth() || bitmap.getHeight() < template.getHeight()) return null;

        if (area == null) area = new Rect();

        Bitmap tmp = null;
        if (!area.isEmpty()) {
            bitmap = safeClipBitmap(bitmap, area.left, area.top, area.width(), area.height());
            tmp = bitmap;
            if (bitmap == null) return null;
        }

        List<MatchResult> matchResults = nativeMatchAllTemplate(bitmap, template, similarity, (int) Math.pow(2, speed), canny);
        if (tmp != null) tmp.recycle();

        if (matchResults == null || matchResults.isEmpty()) return null;
        matchResults.sort(Comparator.comparingDouble(result -> result.value));
        List<MatchResult> resultList = new ArrayList<>();
        for (int i = matchResults.size() - 1; i >= 0; i--) {
            MatchResult result = matchResults.get(i);
            result.area.offset(area.left, area.top);
            resultList.add(result);
        }
        return resultList;
    }

    public static native List<MatchResult> nativeMatchColor(Bitmap bitmap, int[] color, int similarity);

    public static synchronized List<Rect> matchColor(Bitmap bitmap, int color, Rect area, int similarity) {
        if (bitmap == null) return null;
        if (area == null) area = new Rect();

        Bitmap tmp = null;
        if (!area.isEmpty()) {
            bitmap = safeClipBitmap(bitmap, area.left, area.top, area.width(), area.height());
            tmp = bitmap;
            if (bitmap == null) return null;
        }

        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};

        List<MatchResult> matchResults = nativeMatchColor(bitmap, rgb, similarity);
        if (tmp != null) tmp.recycle();

        if (matchResults == null || matchResults.isEmpty()) return null;
        matchResults.sort(Comparator.comparingDouble(result -> result.value));
        List<Rect> rectList = new ArrayList<>();
        for (int i = matchResults.size() - 1; i >= 0; i--) {
            MatchResult result = matchResults.get(i);
            result.area.offset(area.left, area.top);
            rectList.add(result.area);
        }
        return rectList;
    }
}
