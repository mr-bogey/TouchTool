package top.bogey.touch_tool.ui.blueprint.pin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.action.Action;
import top.bogey.touch_tool.bean.pin.Pin;
import top.bogey.touch_tool.bean.pin.PinInfo;
import top.bogey.touch_tool.bean.pin.PinListener;
import top.bogey.touch_tool.bean.pin.pin_objects.PinBase;
import top.bogey.touch_tool.bean.pin.pin_objects.PinObject;
import top.bogey.touch_tool.bean.pin.special_pin.AlwaysShowPin;
import top.bogey.touch_tool.bean.pin.special_pin.NotShowPin;
import top.bogey.touch_tool.bean.task.Task;
import top.bogey.touch_tool.ui.blueprint.BlueprintView;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.card.IDynamicPinCard;
import top.bogey.touch_tool.ui.blueprint.pin_slot.PinSlotView;
import top.bogey.touch_tool.ui.blueprint.pin_widget.PinWidget;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.DisplayUtil;

public abstract class PinView extends FrameLayout implements PinListener {
    private static PinObject copyValue;

    protected final ActionCard card;
    protected final Pin pin;
    protected final boolean custom;

    protected PinSlotView slotView;

    public PinView(@NonNull Context context, ActionCard card, Pin pin, boolean custom) {
        super(context);
        this.card = card;
        this.pin = pin;
        this.custom = custom;

        pin.addListener(this);
    }

    protected void init() {
        ViewGroup slotBox = getSlotBox();
        if (slotBox != null) {
            slotBox.removeAllViews();
            PinInfo info = PinInfo.getPinInfo(pin.getValue());
            try {
                Constructor<? extends PinSlotView> constructor = info.getSlot().getConstructor(Context.class, Pin.class);
                slotView = constructor.newInstance(getContext(), pin);
                slotBox.addView(slotView);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                AppLog.e(e);
            }
        }

        Button removeButton = getRemoveButton();
        if (removeButton != null) {
            removeButton.setVisibility(pin.isDynamic() ? VISIBLE : GONE);
            removeButton.setOnClickListener(v -> card.removePin(pin));
        }

        // 拖动柄只出现在可拖动排序的卡片里，且只给动态添加的针脚做提示
        Button dragButton = getDragButton();
        if (dragButton != null) {
            boolean dragAble = card instanceof IDynamicPinCard && pin.isDynamic();
            dragButton.setVisibility(dragAble ? VISIBLE : GONE);
        }

        Button copyButton = getCopyAndPasteButton();
        if (copyButton != null) {
            copyButton.setOnClickListener(v -> {
                if (copyValue == null || !pin.isSameClass(copyValue)) {
                    copyValue = (PinObject) pin.getValue().copy();
                    BlueprintView.tryRefreshPinView();
                } else {
                    pin.getValue().sync(copyValue);
                    refreshPin();
                    copyValue = null;
                    BlueprintView.tryRefreshPinView();
                }
            });
        }

        refreshPin();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 抑制一下recycleView的滚动，让针脚连线能够生效
        if (card instanceof IDynamicPinCard dynamicPinCard) {
            float x = event.getX();
            float y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                PointF pointF = DisplayUtil.getLocationRelativeToView(this, card);
                if (card.getLinkAblePinView((x + pointF.x) * card.getScaleX(), (y + pointF.y) * card.getScaleY()) == this) {
                    dynamicPinCard.suppressLayout();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public abstract Button getRemoveButton();

    // 可拖动排序的针脚布局提供拖动柄，其余返回 null
    public Button getDragButton() {
        return null;
    }

    public abstract ViewGroup getSlotBox();

    public abstract TextView getTitleView();

    public abstract ViewGroup getWidgetBox();

    public abstract Button getCopyAndPasteButton();

    public void refreshPin() {
        if (slotView != null) slotView.setLinked(pin.isLinked());

        TextView textView = getTitleView();
        if (textView != null) {
            textView.setVisibility(VISIBLE);
            textView.setText(pin.getTitle());
            if (pin.getTitle() == null || pin.getTitle().isEmpty()) {
                if (pin.isVertical()) {
                    textView.setText(R.string.pin_execute);
                } else {
                    textView.setVisibility(GONE);
                }
            }
        }

        refreshCopyButton();

        ViewGroup widgetBox = getWidgetBox();
        if (widgetBox != null) {
            widgetBox.removeAllViews();

            PinInfo info = PinInfo.getPinInfo(pin.getValue());
            if (info != null) {
                Class<? extends PinWidget<? extends PinBase>> widgetClass = info.getWidget();
                if (widgetClass != null) {
                    try {
                        PinWidget<? extends PinBase> widget = widgetClass.getConstructor(Context.class, ActionCard.class, PinView.class, info.getClazz(), boolean.class).newInstance(getContext(), card, this, pin.getValue(), custom);
                        widgetBox.addView(widget);
                    } catch (Exception e) {
                        AppLog.d("针脚", "refreshPin: " + info);
                        AppLog.e(e);
                    }
                }
            }
        }
    }

    public void refreshCopyButton() {
        MaterialButton button = (MaterialButton) getCopyAndPasteButton();
        if (button == null) return;

        boolean visible = (pin.getValue() instanceof PinObject) && (!pin.isLinked());
        button.setVisibility(visible ? View.VISIBLE : View.GONE);

        if (copyValue == null || !pin.isSameClass(copyValue)) {
            button.setIconResource(R.drawable.icon_copy);
        } else {
            button.setIconResource(R.drawable.icon_content_paste);
        }
    }

    public void expand(Action.ExpandType expandType) {
        if (pin instanceof AlwaysShowPin) return;
        if (pin instanceof NotShowPin) {
            setVisibility(GONE);
            return;
        }

        if (pin.showAble(card.getTask())) {
            // 全显示
            if (expandType == Action.ExpandType.FULL) {
                setVisibility(VISIBLE);
            } else {
                // 连接不为空，必须显示
                if (pin.getLinks().isEmpty()) {
                    // 半开的显示非隐藏针脚
                    if (expandType == Action.ExpandType.HALF) {
                        if (pin.isHide()) setVisibility(GONE);
                        else setVisibility(VISIBLE);
                    } else setVisibility(GONE);
                } else {
                    setVisibility(VISIBLE);
                }
            }
        } else {
            setVisibility(GONE);
        }
    }

    public PointF getSlotPosInLayout(float scale) {
        View view = getSlotBox();
        PointF pos = DisplayUtil.getLocationRelativeToView(view, card);
        pos.x *= scale;
        pos.y *= scale;
        if (pin.isVertical()) {
            pos.x += view.getWidth() * scale / 2;
            if (pin.isOut()) {
                pos.y += view.getHeight() * scale;
            }
        } else {
            pos.y += view.getHeight() * scale / 2;
            if (pin.isOut()) {
                pos.x += view.getWidth() * scale;
            }
        }
        pos.offset(card.getX(), card.getY());
        return pos;
    }

    public ActionCard getCard() {
        return card;
    }

    public Pin getPin() {
        return pin;
    }

    @ColorInt
    public int getPinColor() {
        if (slotView != null) return slotView.getPinColor();
        return Color.GRAY;
    }

    @Override
    public void onLinkedTo(Task task, Pin origin, Pin to) {
        post(this::refreshPin);
    }

    @Override
    public void onUnLinkedFrom(Task task, Pin origin, Pin from) {
        post(this::refreshPin);
    }

    @Override
    public void onValueReplaced(Task task, Pin origin, PinBase value) {
        post(this::init);
    }

    @Override
    public void onValueUpdated(Task task, Pin origin, PinBase value) {
    }

    @Override
    public void onTitleChanged(Pin origin, String title) {
        post(this::refreshPin);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 针脚视图会被列表回收后重新挂回来，重新挂载时要把监听补上
        pin.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        pin.removeListener(this);
        super.onDetachedFromWindow();
    }
}
