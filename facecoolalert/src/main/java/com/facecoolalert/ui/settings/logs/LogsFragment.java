package com.facecoolalert.ui.settings.logs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.facecoolalert.R;
import com.facecoolalert.ui.MainActivity;
import com.google.android.material.tabs.TabLayout;


public class LogsFragment extends Fragment {

    private View view;
    private View backButton;


    private TabLayout tabLayout;

    private ViewPager viewPager;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_logs,container,false);

        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener((v)->{
            ((MainActivity)getActivity()).removeFragment(LogsFragment.class);
        });

        tabLayout=view.findViewById(R.id.tabsHeader);

        viewPager=view.findViewById(R.id.viewPager);


        configureTabs();






        return view;
    }

    private void configureTabs() {
        viewPager.setAdapter(new LogsTabsPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    private static class LogsTabsPagerAdapter extends FragmentPagerAdapter {

        public LogsTabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CrashLogsFragment();
                case 1:
                    return new OtherLogsFragment();
                default:
                    return new CrashLogsFragment(); // Default
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Crash Logs";
                case 1:
                    return "Other Logs";
                default:
                    return "";
            }
        }
    }
}
