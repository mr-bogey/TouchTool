package top.bogey.touch_tool.ui.blueprint.pin_widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import top.bogey.touch_tool.R;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinDate;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinDouble;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinFloat;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinInteger;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinLong;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinNumber;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinPeriodic;
import top.bogey.touch_tool.bean.pin.pin_objects.pin_number.PinTime;
import top.bogey.touch_tool.databinding.PinWidgetInputBinding;
import top.bogey.touch_tool.ui.blueprint.card.ActionCard;
import top.bogey.touch_tool.ui.blueprint.pin.PinView;
import top.bogey.touch_tool.utils.AppLog;
import top.bogey.touch_tool.utils.AppUtil;
import top.bogey.touch_tool.utils.listener.TextChangedListener;

@SuppressLint("ViewConstructor")
public class PinWidgetNumber extends PinWidget<PinNumber<?>> {
    private final PinWidgetInputBinding binding;

    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinNumber<?> pinBase, boolean custom) {
        super(context, card, pinView, pinBase, custom);
        binding = PinWidgetInputBinding.inflate(LayoutInflater.from(context), this, true);
        binding.editText.setGravity(Gravity.CENTER);
        init();
    }

    @Keep
    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinInteger pinBase, boolean custom) {
        this(context, card, pinView, (PinNumber<Integer>) pinBase, custom);
    }

    @Keep
    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinFloat pinBase, boolean custom) {
        this(context, card, pinView, (PinNumber<Float>) pinBase, custom);
    }

    @Keep
    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinLong pinBase, boolean custom) {
        this(context, card, pinView, (PinNumber<Long>) pinBase, custom);
    }

    @Keep
    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinDouble pinBase, boolean custom) {
        this(context, card, pinView, (PinNumber<Double>) pinBase, custom);
    }

    @Keep
    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinDate pinBase, boolean custom) {
        this(context, card, pinView, (PinLong) pinBase, custom);
    }

    @Keep
    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinTime pinBase, boolean custom) {
        this(context, card, pinView, (PinLong) pinBase, custom);
    }

    @Keep
    public PinWidgetNumber(@NonNull Context context, ActionCard card, PinView pinView, PinPeriodic pinBase, boolean custom) {
        this(context, card, pinView, (PinLong) pinBase, custom);
    }

    @Override
    protected void initBase() {
        binding.editText.setSaveEnabled(false);
        binding.editText.setSaveFromParentEnabled(false);
        Number value = pinBase.getValue();
        if (value != null) binding.editText.setText(pinBase.toString());

        switch (pinBase.getSubType()) {
            case INTEGER -> {
                binding.editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        String string = s.toString();
                        try {
                            int i = Integer.parseInt(string);
                            ((PinInteger) pinBase).setValue(i);
                            pinView.getPin().notifyValueUpdated(card.getTask());
                        } catch (NumberFormatException e) {
                            AppLog.e(e);
                            pinBase.reset();
                        }
                    }
                });
            }
            case FLOAT -> {
                binding.editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        String string = s.toString();
                        try {
                            float i = Float.parseFloat(string);
                            ((PinFloat) pinBase).setValue(i);
                            pinView.getPin().notifyValueUpdated(card.getTask());
                        } catch (NumberFormatException e) {
                            AppLog.e(e);
                            pinBase.reset();
                        }
                    }
                });
            }
            case DOUBLE -> {
                binding.editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        String string = s.toString();
                        try {
                            double i = Double.parseDouble(string);
                            ((PinDouble) pinBase).setValue(i);
                            pinView.getPin().notifyValueUpdated(card.getTask());
                        } catch (NumberFormatException e) {
                            AppLog.e(e);
                            pinBase.reset();
                        }
                    }
                });
            }
            case LONG -> {
                binding.editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
                binding.editText.addTextChangedListener(new TextChangedListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        String string = s.toString();
                        try {
                            long i = Long.parseLong(string);
                            ((PinLong) pinBase).setValue(i);
                            pinView.getPin().notifyValueUpdated(card.getTask());
                        } catch (NumberFormatException e) {
                            AppLog.e(e);
                            pinBase.reset();
                        }
                    }
                });
            }

            case DATE -> {
                PinDate date = (PinDate) pinBase;
                binding.editText.setText(AppUtil.formatDate(getContext(), date.getValue(), true));
                binding.editText.setEnabled(false);
                binding.pickButton.setIconResource(R.drawable.icon_calendar_month);
                binding.pickButton.setVisibility(VISIBLE);
                binding.pickButton.setOnClickListener(v -> {
                    CalendarConstraints calendarConstraints = new CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.from(System.currentTimeMillis() - 48 * 60 * 60 * 1000))
                            .build();

                    MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                            .datePicker()
                            .setSelection(date.getValue())
                            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                            .setCalendarConstraints(calendarConstraints)
                            .build();

                    picker.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), null);

                    picker.addOnPositiveButtonClickListener(selection -> {
                        date.setValue(selection);
                        pinView.getPin().notifyValueUpdated(card.getTask());
                        binding.editText.setText(AppUtil.formatDate(getContext(), date.getValue(), true));
                    });
                });
            }

            case TIME -> {
                PinTime time = (PinTime) pinBase;
                binding.editText.setText(AppUtil.formatTime(getContext(), time.getValue(), true));
                binding.editText.setEnabled(false);
                binding.pickButton.setIconResource(R.drawable.icon_schedule);
                binding.pickButton.setVisibility(VISIBLE);
                binding.pickButton.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time.getValue());

                    MaterialTimePicker picker = new MaterialTimePicker.Builder()
                            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                            .setMinute(calendar.get(Calendar.MINUTE))
                            .build();

                    picker.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), null);

                    picker.addOnPositiveButtonClickListener(view -> {
                        calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
                        calendar.set(Calendar.MINUTE, picker.getMinute());
                        calendar.set(Calendar.SECOND, 0);
                        time.setValue(calendar.getTimeInMillis());
                        pinView.getPin().notifyValueUpdated(card.getTask());
                        binding.editText.setText(AppUtil.formatTime(getContext(), time.getValue(), true));
                    });
                });
            }

            case PERIODIC -> {
                PinPeriodic periodic = (PinPeriodic) pinBase;
                binding.editText.setText(AppUtil.formatDuration(getContext(), periodic.getValue()));
                binding.editText.setEnabled(false);
                binding.pickButton.setIconResource(R.drawable.icon_timer);
                binding.pickButton.setVisibility(VISIBLE);
                binding.pickButton.setOnClickListener(v -> {
                    MaterialTimePicker picker = new MaterialTimePicker.Builder()
                            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour((int) TimeUnit.MILLISECONDS.toHours(periodic.getValue()) % 24)
                            .setMinute((int) TimeUnit.MILLISECONDS.toMinutes(periodic.getValue()))
                            .setTitleText(R.string.time_start_action_tips)
                            .build();

                    picker.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), null);

                    picker.addOnPositiveButtonClickListener(view -> {
                        long lastValue = periodic.getValue();
                        periodic.setValue(TimeUnit.HOURS.toMillis(picker.getHour()) + TimeUnit.MINUTES.toMillis(picker.getMinute()));
                        if (picker.getHour() == 0 && picker.getMinute() == 0) {
                            if (lastValue == 0) periodic.setValue(TimeUnit.HOURS.toMillis(24));
                            else periodic.setValue(0L);
                            pinView.getPin().notifyValueUpdated(card.getTask());
                        }
                        binding.editText.setText(AppUtil.formatDuration(getContext(), periodic.getValue()));
                    });
                });
            }
        }
    }

    @Override
    protected void initCustom() {

    }
}
