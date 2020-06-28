package com.facecoolalert.utils.datetimepicker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePickerUtils {


    private EditText dateEditText;
    private Calendar calendar;

    public Runnable onchange;

    public Boolean hasChanged;



    public DatePickerUtils(EditText dateEditText) {

        calendar = Calendar.getInstance();
        this.dateEditText=dateEditText;
        dateEditText.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
        dateEditText.setFocusable(false);
        dateEditText.setClickable(true);
        hasChanged=false;


        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();

            }
        });
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                dateEditText.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        updateView();
                        hasChanged=true;
                        if(onchange!=null)
                        {
                            onchange.run();
                        }

                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    public void setDate(Calendar calendar)
    {
        this.calendar=calendar;
        updateView();
    }

    public void setToday()
    {
        this.calendar=Calendar.getInstance();
        updateView();
    }

    private void updateView() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        String selectedDate = dateFormat.format(calendar.getTime());
        dateEditText.setText(selectedDate);
        dateEditText.setTextSize(12);
    }

    public Date getMinDate() {
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Set hour to 00
        calendar.set(Calendar.MINUTE, 0);      // Set minute to 00
        calendar.set(Calendar.SECOND, 0);      // Set second to 00
        calendar.set(Calendar.MILLISECOND, 0); // Set millisecond to 0

        int oldYear= calendar.get(Calendar.YEAR);
        if(!hasChanged)
            calendar.set(Calendar.YEAR,1);

        Date date=calendar.getTime();

        if(!hasChanged)
            calendar.set(Calendar.YEAR,oldYear);

        return date;
    }

    public Date getMaxDate() {
        calendar.set(Calendar.HOUR_OF_DAY, 23); // Set hour to 23
        calendar.set(Calendar.MINUTE, 59);      // Set minute to 59
        calendar.set(Calendar.SECOND, 59);      // Set second to 59
        calendar.set(Calendar.MILLISECOND, 999); // Set millisecond to 999 (maximum value)


        int oldYear= calendar.get(Calendar.YEAR);
        if(!hasChanged)
            calendar.set(Calendar.YEAR,2099);

        Date date=calendar.getTime();

        if(!hasChanged)
            calendar.set(Calendar.YEAR,oldYear);


        return date;
    }


    public Date getCombinedTime(Calendar timeCalendar)
    {
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND,timeCalendar.get(Calendar.MILLISECOND));


        Date date=calendar.getTime();

        return date;

    }

}
