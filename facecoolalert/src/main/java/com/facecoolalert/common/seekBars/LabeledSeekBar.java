package com.facecoolalert.common.seekBars;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facecoolalert.databinding.LabeledSeekbarLayoutBinding;

public class LabeledSeekBar extends LinearLayout {

    private TextView label,value,valueUnits;

    private SeekBar seekBar;

    private int previousValue=50;

    private Runnable onchangeAction=null;


    public LabeledSeekBar(Context context) {
        super(context);
        init();
    }



    public LabeledSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LabeledSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LabeledSeekbarLayoutBinding binding = LabeledSeekbarLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        label=binding.label;
        value=binding.value;
        valueUnits=binding.valueUnits;
        seekBar=binding.seekBar;
        setDefault(previousValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                handleChange();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handleChange();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handleChange();
            }
        });
    }

    private void handleChange() {
        int currentValue=getValue();
        if(currentValue!=previousValue)
        {
            previousValue=currentValue;
            value.setText(Integer.toString(currentValue));
            if(onchangeAction!=null)
                onchangeAction.run();
        }
    }

    public void setDefault(int defaultValue)
    {
        value.setText(Integer.toString(defaultValue));
        seekBar.setProgress(defaultValue,true);
    }

    public void setLabel(String labelText)
    {
        label.setText(labelText);
    }

    public void setUnits(String unitsSymbol)
    {
        valueUnits.setText(unitsSymbol);
    }

    public int getValue()
    {
        return seekBar.getProgress();
    }

    public void setOnChangeAction(Runnable runnable)
    {
        onchangeAction=runnable;
    }
}
