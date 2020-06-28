package com.facecoolalert.ui.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.facecoolalert.App;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.R;

public abstract class BaseFragment extends Fragment {

    protected ViewDataBinding mViewDataBinding;
    protected View mToolBar;
    protected ImageView mToolBarLeftIcon;
    protected ImageView mToolBarRightIcon;
    protected ImageView mToolBarLogo;

    private PowerManager.WakeLock wakeLock;

    public App getApp() {
        return (App) getActivity().getApplication();
    }

    public void initToolbar() {
        mToolBar = mViewDataBinding.getRoot().findViewById(R.id.toolbar_container);
        mToolBarLeftIcon = mToolBar.findViewById(R.id.iv_left);
        mToolBarRightIcon = mToolBar.findViewById(R.id.iv_right);
        mToolBarLogo = mToolBar.findViewById(R.id.iv_top_logo);
    }
    public abstract ViewDataBinding getBinding(LayoutInflater inflater) ;
    public abstract void onPostCreateView();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewDataBinding = getBinding(inflater);
        initToolbar();
        onPostCreateView();
        return mViewDataBinding.getRoot();
    }

    public void openMenu() {
        Activity activity = getActivity();
        if ( activity instanceof MainActivity) {
            ((MainActivity)activity).openMenu();
        }
    }


    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onResume() {
        super.onResume();
        // Acquire a wake lock to prevent the device from sleeping
        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "YourWakeLockTag");
        wakeLock.acquire();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Release the wake lock when the fragment is no longer visible
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
