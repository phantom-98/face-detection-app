package com.facecoolalert.utils.datetimepicker;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimePickerUtils {

    private EditText timeEditText;
    private Calendar calendar;

    public Runnable onchange;

    public Boolean hasChanged;

    public TimePickerUtils(EditText timeEditText) {
        calendar = Calendar.getInstance();

        this.timeEditText = timeEditText;
        timeEditText.setClickable(true);
        timeEditText.setFocusable(false);
        timeEditText.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);


        hasChanged = false;

        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }

    private void showTimePickerDialog() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                timeEditText.getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        updateView();
                        hasChanged = true;

                        if (onchange != null) {
                            onchange.run();
                        }
                    }
                },
                hour, minute, true // true for 24-hour format, false for 12-hour format
        );

        timePickerDialog.show();
    }

    private void updateView() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        String selectedTime = timeFormat.format(calendar.getTime());

        timeEditText.setText(selectedTime);
        timeEditText.setTextSize(12);
    }

    public void setDate(Calendar calendar)
    {
        this.calendar=calendar;
        updateView();
    }

    public void setNow()
    {
        this.calendar=Calendar.getInstance();
        updateView();
    }

    public void setMorning()
    {
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Set hour to 00
        calendar.set(Calendar.MINUTE, 0);      // Set minute to 00
        calendar.set(Calendar.SECOND, 0);      // Set second to 00
        calendar.set(Calendar.MILLISECOND, 1); // Set millisecond to 0
        updateView();
    }

    public void setEvening()
    {
        calendar.set(Calendar.HOUR_OF_DAY, 23); // Set hour to 23
        calendar.set(Calendar.MINUTE, 59);      // Set minute to 59
        calendar.set(Calendar.SECOND, 59);      // Set second to 59
        calendar.set(Calendar.MILLISECOND, 999); // Set millisecond to 999 (maximum value)
        updateView();
    }



    public Date getSelectedTime() {
        return calendar.getTime();
    }

    public Calendar getCalendar()
    {
        return calendar;
    }
}
