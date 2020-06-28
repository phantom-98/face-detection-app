package com.facecoolalert.ui.help;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.facecoolalert.R;
import com.facecoolalert.databinding.FragmentHelpBinding;
import com.facecoolalert.ui.base.BaseFragment;

public class HelpFragment extends BaseFragment {

    @Override
    public ViewDataBinding getBinding(LayoutInflater inflater) {
        this.mViewDataBinding= FragmentHelpBinding.inflate(inflater);

        mViewDataBinding.getRoot().findViewById(R.id.btn_send_help_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeEmail(new String[]{getString(R.string.help_support_email)}, getString(R.string.help_support_msg));
            }
        });

        return this.mViewDataBinding;
    }

    private void composeEmail(String[] addresses, String subject) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));  // Specifies the "mailto" scheme
        emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses);  // Email address of the recipient
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);  // Subject of the email
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");  // Body of the email

        requireActivity().startActivity(Intent.createChooser(emailIntent, getString(R.string.help_choose_email_provider)));
    }


    @Override
    public void onPostCreateView() {

        mToolBarLeftIcon.setOnClickListener(v -> {
            openMenu();
        });

    }
}
