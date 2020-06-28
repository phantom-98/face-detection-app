package com.facecoolalert.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import com.facecool.attendance.Constants;
import com.facecool.cameramanager.CameraManagerHelper;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecoolalert.App;
import com.facecoolalert.R;
import com.facecoolalert.common.imageSavePreview.SaveImagePreviewDialog;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.databinding.ActivityMainBinding;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facecoolalert.sesssions.SessionManager;
import com.facecoolalert.ui.Account.LoginActivity;
import com.facecoolalert.ui.Account.UserDevice;
import com.facecoolalert.ui.Alerts.AlertFragment;
import com.facecoolalert.ui.Alerts.emailSettings.EmailSettingsFragment;
import com.facecoolalert.ui.Search.SearchFragment;
import com.facecoolalert.ui.base.BaseActivity;
import com.facecoolalert.ui.camera.CameraFragment;

import com.facecoolalert.ui.help.HelpFragment;

import com.facecoolalert.ui.settings.AdvancedSettingsFragment;

import com.facecoolalert.ui.settings.SettingsFragment;
import com.facecoolalert.ui.settings.logs.LogsFragment;
import com.facecoolalert.ui.settings.logs.OtherLogsFragment;
import com.facecoolalert.ui.subject.SubjectFragment;
import com.facecoolalert.ui.visits.GenVisitAdapter;
import com.facecoolalert.ui.visits.VisitsFragment;
import com.facecoolalert.ui.watchlist.WatchListFragment;

import com.facecoolalert.utils.RecognitionUtils;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class MainActivity extends BaseActivity{
    public SaveImagePreviewDialog imageSaveDialog;
    ActivityMainBinding mBinding;

    private FirebaseAuth firebaseAuth;

    @Override
    public ViewDataBinding createBinding() {

        return mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    public void setCameraFragment() {


        RecognitionUtils.refreshSubjects();
        currentCameraFragment=new CameraFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_main, currentCameraFragment)
                .commit();
    }

    public CameraFragment currentCameraFragment;
    public void openMenu() {
        try {
            mBinding.myDrawerLayout.openDrawer(GravityCompat.START);
        }catch (Exception ess)
        {
            ess.printStackTrace();
        }
//        throw new RuntimeException("Test Crash"); // Force a crash
    }

    @Override
    public void onBackPressed() {
        if ( getSupportFragmentManager().getFragments().isEmpty() ) {
            finish();
            getSupportFragmentManager().popBackStack();//R.id.fragment_container, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public Handler handlerActivity = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if ( msg.what == 1 ) {
                getSupportFragmentManager().popBackStackImmediate();
            } else if ( msg.what == 2 ) {
                getApp().setCameraInfos((CameraInfo[]) msg.obj);
                getSupportFragmentManager().popBackStackImmediate();
                setCameraFragment();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLoginState();
        ensureDefaultWatchlistExists();
        RecognitionUtils.context=getApplicationContext();//getApplicationContext();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        try {
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "FaceCoolAlert:WakeLocktag");
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        retrieveApnInfo();


        FragmentManager fragmentManager = getSupportFragmentManager();

        if(fragmentManager.getFragments().size()==0)
            setCameraFragment();


//            clearExistingFlagments();
//            setCameraFragment();
//            setVisitsFragment();
//            setSearchFragment();
//            setWatchListFragment();
//            setAlertFragment();

        mBinding.navigationView.setNavigationItemSelectedListener((menuItem) -> {

            if (menuItem.getItemId() == R.id.nav_visits) {
                setVisitsFragment();
            } else if (menuItem.getItemId() == R.id.nav_subjects) {
                setSubjectsFragment();

            } else if (menuItem.getItemId() == R.id.live_camera) {
                setCameraFragment();
            } else if (menuItem.getItemId() == R.id.nav_settings) {
                clearExistingFlagments();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragment_main, new SettingsFragment())
                        .commit();

            } else if (menuItem.getItemId() == R.id.nav_search) {
                setSearchFragment();
            } else if (menuItem.getItemId() == R.id.nav_watchlist) {
                setWatchListFragment();
            } else if (menuItem.getItemId() == R.id.nav_reports) {

                setAlertFragment();
            } else if (menuItem.getItemId() == R.id.nav_help) {
                setHelpFragment();
            } else if (menuItem.getItemId() == R.id.nav_logout) {
                logOut();
            }


            mBinding.myDrawerLayout.closeDrawers();
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager())
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }

        int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    1
            );
        }

        int smsPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
        if (smsPermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.SEND_SMS},
                    2
            );
        }

        int phoneStatePermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    3
            );
        }

        int storagePermission = checkSelfPermission(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{android.Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                    4
            );
        }

        int readstoragePermission = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readstoragePermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    5
            );
        }

        int writestoragePermission = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writestoragePermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    6
            );
        }


//        addFragment(new OtherLogsFragment());

        //below are for easy testing navigation
//        addFragment(new HistoryFragment());
//        addFragment(new LogsFragment());
//        addFragment(new SearchFragment());
//        addFragment(AppDatabaseFragment.getInstance());
//        addFragment(new VisitsFragment());
//        addFragment(new AlertFragment());

//        addFragment(new SettingsFragment());
//        setWatchListFragment();

    }

    private void ensureDefaultWatchlistExists() {
        //add default watchlist if it does not exist
        new Thread(()->{
            try{
                WatchlistDao watchlistDao= MyDatabase.getInstance(this).watchlistDao();
                if(!watchlistDao.listAll().contains("Default"))
                {
                    WatchList watchList=new WatchList();
                    watchList.setName("Default");
                    watchList.setCreatedOn(new Date().getTime());
                    watchList.setType("Default");
                    watchList.setNote("Default");
                    watchlistDao.insertWatchlist(watchList);
                }
            }catch (Exception ed)
            {
                ed.printStackTrace();
            }
        }).start();
    }


    private void checkLoginState() {
//        if(!SessionManager.sessionValid(this)) {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }
        //using firebase

//        FirebaseAuth
        if(firebaseAuth ==null)
            firebaseAuth = FirebaseAuth.getInstance();

        if(!SessionManager.sessionValid(this)){// Check Login is within last 7 days
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser==null)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        App.currentUser=currentUser;

        //check if this Device has been logged out
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users_devices").document(currentUser.getUid())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            UserDevice userDevice = document.toObject(UserDevice.class);
                            if (!SessionManager.getDeviceIMEI(getApplicationContext()).equals(userDevice.getDeviceId()))
                            {
                                Toast.makeText(this, userDevice.getDeviceName()+" has Logged you Out", Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                Intent intent = new Intent(this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                    } else
                        task.getException().printStackTrace();
                });
    }

    private void logOut() {
        if(firebaseAuth ==null)
            firebaseAuth = FirebaseAuth.getInstance();

        new AlertDialog.Builder(this).setMessage("Confirm Logout\n \nAre you sure you want to Logout from your account ?")
                .setNegativeButton("No", (dialog, i) -> {

                })
                .setPositiveButton("Yes Log Out", ((dialogInterface, i) -> {
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    firestore.collection("users_devices").document(App.currentUser.getUid()).delete();
                    firebaseAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })).create().show();

    }


    private void acquireWakeLock() {
        try {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
    }

    private void releaseWakeLock() {

        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private PowerManager.WakeLock wakeLock;

    public void setVisitsFragment() {
        clearExistingFlagments();
        getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fragment_main, new VisitsFragment())
                            .commit();
    }

    public void setSearchFragment() {
        clearExistingFlagments();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_main, new SearchFragment())
                .commit();
    }

    public void setWatchListFragment() {
        clearExistingFlagments();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_main, new WatchListFragment())
                .commit();
    }

    public void setHelpFragment() {
        clearExistingFlagments();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_main, new HelpFragment())
                .commit();
    }

    public void setSettingsFragment() {
        clearExistingFlagments();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_main, new SettingsFragment())
                .commit();
    }

    public void setAdvancedSettingsFragment() {
        clearExistingFlagments();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_main, new AdvancedSettingsFragment())
                .commit();
    }

    public void clearExistingFlagments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        for (Fragment fragment : fragmentManager.getFragments()) {
            if (!(fragment instanceof CameraFragment)) {
                fragmentTransaction.remove(fragment);
                System.out.println("Started Removing");
            }
        }

        fragmentTransaction.commit();
    }



    private void restoreNavigation() {
        if(prevNavigation==R.id.live_camera)
            setCameraFragment();
        else if (prevNavigation==R.id.nav_subjects) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_main, new SubjectFragment())
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginState();
        RecognitionUtils.refreshSubjects();
        if(currentCameraFragment!=null)
        {
            currentCameraFragment.resume();
        }
        acquireWakeLock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the camera preview when the activity is paused
//        if (camera != null) {
//            camera.stopPreview();
//            camera.release();
//            camera = null;

        if(currentCameraFragment!=null)
        {
            currentCameraFragment.pause();
        }
        releaseWakeLock();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Constants.LogDebug("Permission granted! ");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == 100 && resultCode == RESULT_OK ) {
            CameraInfo[] cameraInfos = CameraManagerHelper.getCameraInfos(data);
            getApp().setCameraInfos(cameraInfos);
            setCameraFragment();
        } else if(requestCode==SubjectFragment.SUBJECTCARD_REQUEST_CODE)
        {
            if(resultCode== RESULT_OK)
                setSubjectsFragment();

        } else if(requestCode==CameraFragment.SUBJECTCARD_REQUEST_CODE&&resultCode== RESULT_OK)
        {
//            System.out.println("Success on Camera");
//            setCameraFragment();
        } else if (requestCode==SearchFragment.PICK_IMAGE_REQUEST) {
//            System.out.println("Success on Search");

        } else if (requestCode == CameraFragment.SUBJECTCARD_REQUEST_CODE) {
//            setCameraFragment();
        } else if (requestCode == GenVisitAdapter.SUBJECTCARD_REQUEST_CODE) {
           // setVisitsFragment();
        } else if(requestCode == SaveImagePreviewDialog.SAVE_IMAGE_REQUEST_CODE&&resultCode==RESULT_OK)
        {
            if(imageSaveDialog!=null)
                imageSaveDialog.saveImageFile(data.getData());
        }

//        } else
//            setCameraFragment();

        System.out.println("Success Overall "+requestCode);
    }

    public void setSubjectsFragment()
    {

        clearExistingFlagments();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_main, new SubjectFragment())
                .commit();
    }

    public void reloadCameraInfos() {

    }


    public void setAlertFragment()
    {
        clearExistingFlagments();
        addFragment(new AlertFragment());
    }


    public void removeFragment(Class<?> fragmentToRemove) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragmentToRemove.isAssignableFrom(fragment.getClass())) {
                fragmentTransaction.remove(fragment);
            }
        }
        fragmentTransaction.commit();
    }

    public void addFragment(Fragment fragment)
    {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.popup,R.anim.popout)
                .add(R.id.fragment_main, fragment)
                .commit();
    }

    private com.klinker.android.send_message.Settings apnSettings;

    private void retrieveApnInfo() {

//        ApnUtils.initDefaultApns(this, new ApnUtils.OnApnFinishedListener() {
//            @Override
//            public void onFinished() {
//                apnSettings = com.klinker.android.send_message.Settings.get(MainActivity.this, true);
//            }
//        });


    }



}