package com.facecoolalert.ui.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.facecool.attendance.Constants;
import com.facecoolalert.R;
import com.facecoolalert.databinding.FragmentSettingsBinding;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.detect.FaceQualityTester;
import com.facecoolalert.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class AdvancedSettingsFragment extends Fragment {

    private List<Rule> rules;
    private LinearLayout layout;
    private Button saveButton;
    private LinearLayout backButton;
    private Button startButton;
    private Button stopButton;

    private CheckBox verboseModeCheckbox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        PrefManager.removeAllRules(getContext());
        rules = new ArrayList<>();
        rules.add(new Rule(Constants.RULE_1, 30, 150, 100, 60, 60));
        rules.add(new Rule(Constants.RULE_2, 20, 160, 40, 40, 40));
        rules.add(new Rule(Constants.RULE_3, 30, 0, 0, 30, 30));
        rules.add(new Rule(Constants.RULE_4, 10, 200, 0, 0, 0));
        rules.add(new Rule(Constants.RULE_5, 0, 0, 30, 0, 0));

        @NonNull FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(inflater, container, false);

        View view = inflater.inflate(R.layout.fragment_advanced_settings, container, false);
//        layout = view.findViewById(R.id.layout_rules);
//        saveButton = view.findViewById(R.id.submit_button);
//        saveButton.setOnClickListener(v -> saveRules());
        backButton = binding.backButton;
        backButton.setOnClickListener(v -> navigateBackToSettings(v));
        startButton = view.findViewById(R.id.start_button);
        stopButton = view.findViewById(R.id.stop_button);
        startButton.setOnClickListener(v -> startCSVWriting());
        stopButton.setOnClickListener(v -> stopCSVWriting());

//        loadSavedRules();
//        saveButton.setVisibility(View.INVISIBLE);

        //versbose MODE Button
        verboseModeCheckbox = view.findViewById(R.id.detectionPropertiesVerboseMode);
        verboseModeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PrefManager.setBooleanDetectionVerboseMode(isChecked,getContext());
        });
        verboseModeCheckbox.setChecked(PrefManager.getBooleanDetectionVerboseMode(getContext()));

        return view;
    }

    private void loadSavedRules() {
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = rules.get(i);
            Rule savedRule = PrefManager.getRule(getContext(), rule.getName());

            if (savedRule != null) {
                rules.set(i, savedRule);
            }
            layout.addView(createInputFields(rules.get(i)));
        }
    }

    private void saveRules() {
        List<Rule> rulesToSave = collectInputData();
        for (Rule rule : rulesToSave) {
            PrefManager.saveRule(getContext(), rule.getName(), rule);
        }
        Toast.makeText(getContext(), "Rules saved successfully", Toast.LENGTH_SHORT).show();
        navigateBackToSettings(getView());

    }

    private List<Rule> collectInputData() {
        List<Rule> updatedRules = new ArrayList<>();

        for (int i = 0; i < layout.getChildCount(); i++) {
            View ruleView = layout.getChildAt(i);
            if (ruleView instanceof LinearLayout) {
                LinearLayout ruleLayout = (LinearLayout) ruleView;
                String ruleName = ((TextView) ruleLayout.getChildAt(0)).getText().toString();

                Integer brLow = parseInput(getValueByTag(ruleLayout, ruleName, Constants.BR_LOW));
                Integer brHigh = parseInput(getValueByTag(ruleLayout, ruleName, Constants.BR_HIGH));
                Integer sharpness = parseInput(getValueByTag(ruleLayout, ruleName, Constants.SHARPNESS));
                Integer size = parseInput(getValueByTag(ruleLayout, ruleName, Constants.SIZE));
                Integer absYaw = parseInput(getValueByTag(ruleLayout, ruleName, Constants.ABS_YAW));

                Rule rule = new Rule(ruleName, brLow, brHigh, sharpness, size, absYaw);
                updatedRules.add(rule);
            }
        }
        return updatedRules;
    }

    private String getValueByTag(LinearLayout layout, String ruleName, String paramName) {
        String tag = ruleName + Constants.RULE_PARAM_TAG_CHAR + paramName;
        EditText input = layout.findViewWithTag(tag);
        if (input != null) {
            return input.getText().toString();
        }
        return "";
    }

    private Integer parseInput(String input) {
        try {
            return !input.trim().isEmpty() ? Integer.parseInt(input.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private View createInputFields(Rule rule) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setPadding(16, 16, 16, 16);

        TextView titleView = new TextView(getContext());
        titleView.setTextColor(Color.BLACK);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        titleView.setText(rule.getName());
        layout.addView(titleView);

        addInputField(layout, rule.getName(), Constants.BR_LOW, rule.getBrLow());
        addInputField(layout, rule.getName(), Constants.BR_HIGH, rule.getBrHigh());
        addInputField(layout, rule.getName(), Constants.SHARPNESS, rule.getSharpness());
        addInputField(layout, rule.getName(), Constants.SIZE, rule.getSize());
        addInputField(layout, rule.getName(), Constants.ABS_YAW, rule.getAbsYaw());

        return layout;
    }

    private void addInputField(LinearLayout layout, String ruleName, String paramName, Integer value) {
        TextView label = new TextView(getContext());
        label.setText(paramName);

        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setText(value != null ? String.valueOf(value) : "");

        String tag = ruleName + "_" + paramName;
        input.setTag(tag);

        layout.addView(label);
        layout.addView(input);
    }


    private void navigateBackToSettings(View view) {
        ((MainActivity) view.getContext()).removeFragment(this.getClass());
    }

    private void startCSVWriting() {
        FaceQualityTester.getInstance(getContext()).enableCSVWriting(true);
        Toast.makeText(getContext(), "CSV Writing Started", Toast.LENGTH_SHORT).show();
    }

    private void stopCSVWriting() {
        FaceQualityTester.getInstance(getContext()).enableCSVWriting(false);
        Toast.makeText(getContext(), "CSV Writing Stopped", Toast.LENGTH_SHORT).show();
    }

}
