package com.facecoolalert.ui.Alerts;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.databinding.ViewDataBinding;

import com.facecoolalert.R;
import com.facecoolalert.databinding.FragmentAlertsBinding;
import com.facecoolalert.ui.Alerts.Alert.AlertView;
import com.facecoolalert.ui.Alerts.AlertsLog.AlertLogView;
import com.facecoolalert.ui.Alerts.Distribution.DistributionView;
import com.facecoolalert.ui.Alerts.Subscribers.SubscriberView;
import com.facecoolalert.ui.Alerts.emailSettings.EmailSettingsFragment;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.base.BaseFragment;

public class AlertFragment extends BaseFragment {

    private Button alertsLogButton;
    private Button alertsButton;
    private Button distributionButton;
    private Button subscribersButton;
    private Button emailSettingsButton;
    public RelativeLayout playground;


    @Override
    public ViewDataBinding getBinding(LayoutInflater inflater) {
        this.mViewDataBinding= FragmentAlertsBinding.inflate(inflater);
        return mViewDataBinding;
    }

    @Override
    public void onPostCreateView() {
        mToolBarLeftIcon.setOnClickListener(v -> {
            openMenu();
        });

        initializeButtons();
        setButtonNavigations();
        mainActivity=getMainActivity();

    }

    private void setButtonNavigations() {
        alertsLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonStyles(); // Reset styles for all buttons
                setButtonStyle(alertsLogButton, Color.WHITE, "#132558", true);
                new AlertLogView(AlertFragment.this);
            }
        });

        alertsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonStyles();
                setButtonStyle(alertsButton, Color.WHITE, "#132558", true);
                new AlertView(AlertFragment.this);


            }
        });

        distributionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonStyles();
                setButtonStyle(distributionButton, Color.WHITE, "#132558", true);
                new DistributionView(AlertFragment.this);
            }
        });

        subscribersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonStyles();
                setButtonStyle(subscribersButton, Color.WHITE, "#132558", true);
               new SubscriberView(AlertFragment.this);
            }
        });

        emailSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonStyles();
                setButtonStyle(emailSettingsButton, Color.WHITE, "#132558", true);
                playground.removeAllViews();
                getMainActivity().addFragment(new EmailSettingsFragment());

            }
        });



        alertsLogButton.performClick();
    }

    private void resetButtonStyles() {
        // Reset styles for all buttons
        setButtonStyle(alertsLogButton, Color.TRANSPARENT, "#64748B", false);
        setButtonStyle(alertsButton, Color.TRANSPARENT, "#64748B", false);
        setButtonStyle(distributionButton, Color.TRANSPARENT, "#64748B", false);
        setButtonStyle(subscribersButton, Color.TRANSPARENT, "#64748B", false);
        setButtonStyle(emailSettingsButton, Color.TRANSPARENT, "#64748B", false);
    }

    private void setButtonStyle(Button button, int backgroundColor, String textColor, boolean isBold) {
        // Set button style based on parameters
        button.setBackgroundColor(backgroundColor);
        button.setTextColor(Color.parseColor(textColor));
        button.setTypeface(null, isBold ? Typeface.BOLD : Typeface.NORMAL);
    }



    private void initializeButtons() {

        View rootView=mViewDataBinding.getRoot();
        alertsLogButton = rootView.findViewById(R.id.alertsLogButton);
        alertsButton = rootView.findViewById(R.id.alertsButton);
        distributionButton = rootView.findViewById(R.id.distributionButton);
        subscribersButton = rootView.findViewById(R.id.subscribersButton);
        emailSettingsButton = rootView.findViewById(R.id.emailSettingsButton);
        this.playground=rootView.findViewById(R.id.play_ground);

    }

    public static MainActivity mainActivity;

    public MainActivity getMainActivity()
    {
        return (MainActivity) getActivity();
    }
}
