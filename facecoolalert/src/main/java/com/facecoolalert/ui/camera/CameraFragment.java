package com.facecoolalert.ui.camera;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import androidx.core.content.ContextCompat;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.facecool.attendance.Constants;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.Subject;
import com.facecool.attendance.facedetector.FaceData;
import com.facecool.cameramanager.CameraManagerHelper;
import com.facecool.cameramanager.camera.CameraViewFragment;
import com.facecoolalert.R;
import com.facecoolalert.databinding.DlgCameraBinding;
import com.facecoolalert.databinding.FragmentCameraBinding;
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.ui.base.BaseFragment;
import com.facecoolalert.ui.subject.enrollments.EnrollSubject;
import com.facecoolalert.utils.PrefManager;
import com.facecoolalert.utils.RecognitionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class CameraFragment extends BaseFragment implements FaceDetectorProcessor.ResultListener {
    static final int[] layoutIds = new int[]{R.layout.layout_camera_1,R.layout.layout_camera_2,R.layout.layout_camera_3,R.layout.layout_camera_4};
    public static int SUBJECTCARD_REQUEST_CODE = 405;

    public static int lastSubjectsChange=0;

    FragmentCameraBinding mBinding;
    DlgCameraBinding dlgCameraBinding;
    RecentRecognitionsAdapter mDetectItemAdapter;
//    ArrayList<DetectItem> mDetectionItems = new ArrayList<>();

    LinearLayout[] layoutCameras = new LinearLayout[4];
    int[] resIdsForCameras = new int[] {
            R.id.layout_camera1,
            R.id.layout_camera2,
            R.id.layout_camera3,
            R.id.layout_camera4
    };

    private CardView currentCard;
    private ImageView imageCameraDetection;
    private TextView tvUserName;

    private CardView referenceCard;
    private ImageView refImageCameraDetection;
//    private TextView refTvUserName;

    private ImageView settings_action;

    private ImageView toggleCameraIcon;

    boolean bCameraSelectionChanged = false;
    CameraViewFragment[] mCameraViewFragments = new CameraViewFragment[4];
    int nCamVFragmentCount = 0;
    boolean[] mSelectedCameras;

    public void showCameraViews() {
        bCameraSelectionChanged = true;
        mBinding.tvCameraSelect.setText("Camera " + (getCameraIndex(0) + 1));
        int count = getApp().getSelectedCameraCount(mSelectedCameras);
        View v = getLayoutInflater().inflate(layoutIds[count-1], null);

        mBinding.layoutFrameCameras.removeAllViews();
        mBinding.layoutFrameCameras.addView(v,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    static boolean USE_POPUP = true;

    public void onClosePopup(View v) {
        mBinding.popupWindow.setVisibility(View.GONE);
        mBinding.tvCameraSelect.setText("Camera " + (getCameraIndex(0) + 1));
        if ( bCameraSelectionChanged ) {
//            replaceCameraFragments();
            configureViewsChange(false);
        }
    }



    public void onClickSelectCamera(View v) {
        /*
        dlgCameraBinding = DlgCameraBinding.inflate(getLayoutInflater());
        int[] location = new int[2];
        mBinding.layoutSelectCamera.getLocationOnScreen(location);
        if ( USE_POPUP ) {
            PopupWindow menu = new PopupWindow(dlgCameraBinding.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            menu.showAtLocation((View) mBinding.layoutSelectCamera.getParent(), Gravity.START, location[0], location[1]);
            menu.setOnDismissListener(()->{
                mBinding.tvCameraSelect.setText("Camera " + (getCameraIndex(0) + 1));
                if ( bCameraSelectionChanged )
                    replaceCameraFragments();
            });
        } else {
            Dialog dlg = new Dialog(getActivity());
            dlg.setContentView(dlgCameraBinding.getRoot());

            Window window = dlg.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.TOP | Gravity.START);
            window.setAttributes((WindowManager.LayoutParams) dlg.getWindow().getAttributes());
            window.getAttributes().x = location[0];
            window.getAttributes().y = location[1];//+ mBinding.layoutSelectCamera.getHeight() + getResources().getDimensionPixelSize(R.dimen.camera_spinner_margin);
            dlg.show();
            dlg.setOnDismissListener(dialog -> {
                mBinding.tvCameraSelect.setText("Camera " + (getCameraIndex(0) + 1));
                if ( bCameraSelectionChanged )
                    replaceCameraFragments();
            });
        }
        layoutCameras = new LinearLayout[]{
                dlgCameraBinding.layoutCamera1,
                dlgCameraBinding.layoutCamera2,
                dlgCameraBinding.layoutCamera3,
                dlgCameraBinding.layoutCamera4,
        };
        setCameraSelection();

        dlgCameraBinding.layoutAll.setOnClickListener(view -> {
            mSelectedCameras = new boolean[]{true, true, true, true};
            setCameraSelection();
        });
        for (int i = 0; i < 4; i++) {
            final int index = i;
            layoutCameras[i].setSelected(mSelectedCameras[i]);
            layoutCameras[i].setOnClickListener(view -> {
                if (mSelectedCameras[index] && getApp().getSelectedCameraCount(mSelectedCameras) == 1) {
                    return;
                }
                mSelectedCameras[index] = !mSelectedCameras[index];
                setCameraSelection();
            });
        }

        bCameraSelectionChanged = false;

        mBinding.tvCameraSelect.setText("Select Camera");

         */

        mBinding.popupWindow.setVisibility(View.VISIBLE);
        setCameraSelection(false);
        bCameraSelectionChanged = false;
        mBinding.tvCameraSelect.setText("Select Camera");

    }

    public void setCameraSelection(boolean renderCameras) {

        if (getApp().getSelectedCameraCount(mSelectedCameras) == 4) {
            dlgCameraBinding.layoutAll.setSelected(true);
        } else {
            dlgCameraBinding.layoutAll.setSelected(false);
        }
        for (int i = 0; i < 4; i++) {
            layoutCameras[i].setSelected(mSelectedCameras[i]);
        }
        getApp().setSelectedCameras(mSelectedCameras);
        if ( renderCameras ) {
            showCameraViews();
        }


    }

    public int getCameraIndex(int position) {
        for (int i = 0; i < 4; i++) {
            if (mSelectedCameras[i])
                position--;
            if (position < 0) {
                return i;
            }
        }
        return 0;
    }

    public void replaceCameraFragments() {
//        configureViewsChange();
        int index = 0;

        for ( int i = 0 ; i < 4; i++ ) {
            if ( mSelectedCameras[i] ) {
                ViewParent v = mBinding.layoutFrameCameras.findViewById(resIdsForCameras[index]).getParent();
                if ( v instanceof  CardView ) {
                    CardView cv = (CardView)v;
                    cv.setCardBackgroundColor(Color.DKGRAY);
                }
                createCameraView(i,index);
                index++;
            }

        }
    }

    private void updateCameraViews(int... updateIndex) {

        int index = 0;
        configureViewsChange(false);

        for ( int i = 0 ; i < 4; i++ ) {
            if(mCameraViewFragments[i]!=null)
                mCameraViewFragments[i].onDestroy();
            if ( mSelectedCameras[i]|| Arrays.asList(updateIndex).contains(i) ) {
                ViewParent v = mBinding.layoutFrameCameras.findViewById(resIdsForCameras[index]).getParent();
                if (v instanceof CardView) {
                    CardView cv = (CardView) v;
                    cv.setCardBackgroundColor(Color.DKGRAY);
                }
                createCameraView(i,index);
                index++;
            }

        }
    }

    private void createCameraView(int i, int index) {
        FaceDetectorProcessor processor = new FaceDetectorProcessor(getContext(), this);
        processor.setMycamera(getApp().getCameraInfos()[i]);
        mCameraViewFragments[index] =
                (CameraViewFragment) CameraManagerHelper.setupCameraView(getChildFragmentManager(), resIdsForCameras[index],
                        getApp().getCameraInfos()[i], processor, index > 0, selectedMode,index==0?toggleCameraIcon:null);

        mCameraViewFragments[index].onSave=()->{
            CameraInfo[] cameraInfos= getApp().getCameraInfos();
            PrefManager.saveCameraSettings(getContext(),cameraInfos);
            createCameraView(i,index);
        };
    }

    public ViewDataBinding getBinding(LayoutInflater inflater) {
        mBinding = FragmentCameraBinding.inflate(inflater);
        return mBinding;
    }

    public void onPostCreateView() {
        mSelectedCameras = getApp().getSelectedCameras();
        mBinding.layoutSelectCamera.setOnClickListener(CameraFragment.this::onClickSelectCamera);
        showCameraViews();

        configureViewsChange(false);
        toggleCameraIcon = mBinding.getRoot().findViewById(R.id.toogle_camera);


        replaceCameraFragments();

        mToolBarLeftIcon.setOnClickListener(view -> {
            openMenu();
        });

        loadDetectionItems();

        mDetectItemAdapter = new RecentRecognitionsAdapter(this);
        mBinding.rvDetectedFacesList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mBinding.rvDetectedFacesList.setAdapter(mDetectItemAdapter);

        mBinding.popupWindow.setOnClickListener(this::onClosePopup);

        dlgCameraBinding = DlgCameraBinding.inflate(getLayoutInflater());
        mBinding.popupContainer.addView(dlgCameraBinding.getRoot());

        layoutCameras = new LinearLayout[]{
                dlgCameraBinding.layoutCamera1,
                dlgCameraBinding.layoutCamera2,
                dlgCameraBinding.layoutCamera3,
                dlgCameraBinding.layoutCamera4,
        };

        dlgCameraBinding.layoutAll.setOnClickListener(view -> {
            if ( getApp().getSelectedCameraCount(mSelectedCameras) == 4 ) {
                return;
            }
            mSelectedCameras = new boolean[]{true, true, true, true};
            setCameraSelection(true);
            updateCameraViews(0,1,2,3);
        });
        for (int i = 0; i < 4; i++) {
            final int index = i;
            layoutCameras[i].setSelected(mSelectedCameras[i]);
            layoutCameras[i].setOnClickListener(view -> {
                if (mSelectedCameras[index] && getApp().getSelectedCameraCount(mSelectedCameras) == 1) {
                    return;
                }
                mSelectedCameras[index] = !mSelectedCameras[index];
                setCameraSelection(true);
                if(mSelectedCameras[index])
                    updateCameraViews(index);
                else
                    updateCameraViews(-1);
            });
        }

        new Thread(()->{//create a default wtchlist if it doesnt exist
            try{
                WatchlistDao watchlistDao= MyDatabase.getInstance(getContext()).watchlistDao();
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

        currentCard = mBinding.getRoot().findViewById(R.id.currentCard);
        imageCameraDetection = mBinding.getRoot().findViewById(R.id.image_camera_detection);
        tvUserName = mBinding.getRoot().findViewById(R.id.tv_user_name);

        referenceCard = mBinding.getRoot().findViewById(R.id.referenceCard);
        refImageCameraDetection = mBinding.getRoot().findViewById(R.id.refimage_camera_detection);
//        refTvUserName = mBinding.getRoot().findViewById(R.id.reftv_user_name);

        referenceCard.setVisibility(View.GONE);

        settings_action=mBinding.getRoot().findViewById(R.id.settings_action);

        settings_action.setOnClickListener((v)->{
//            ((MainActivity)getActivity()).addFragment(new SettingsFragment(true));
            if(mCameraViewFragments[0]!=null)
                mCameraViewFragments[0].openSettings();
            else
                Log.d("Launch Settings","is null "+mCameraViewFragments[0]);
//                ((MainActivity)getActivity()).addFragment(new SettingsFragment(true));
        });


       configureViewsChange(false);
       Fragment currentFragment=CameraFragment.this;
       updateColorAroundSCreenRunnable =()-> {
           if (currentFragment != null && currentFragment.isAdded()) {
               if (RecognitionUtils.similarityThreshold >= 80)
                   mBinding.layoutFrameCameras.setBackground(ContextCompat.getDrawable(currentFragment.getContext(), R.drawable.camera_border_green));
               else if (RecognitionUtils.similarityThreshold >= 60)
                   mBinding.layoutFrameCameras.setBackground(ContextCompat.getDrawable(currentFragment.getContext(), R.drawable.camera_border_orange));
               else
                   mBinding.layoutFrameCameras.setBackground(ContextCompat.getDrawable(currentFragment.getContext(), R.drawable.camera_border_red));

               Log.d("ColorAroundScreen","We Updated");
           }
       };

        updateColorAroundScreen();

    }


    private static int selectedMode=1;
    private HashMap<Integer,Fragment> cameraFragments=new HashMap<>();

    public static Runnable updateColorAroundSCreenRunnable;

    public static void updateColorAroundScreen()
    {
        if(updateColorAroundSCreenRunnable!=null)
        {
            try{
                updateColorAroundSCreenRunnable.run();
            }catch (Exception es)
            {
                es.printStackTrace();
            }
        }
    }

    private void configureViewsChange(Boolean willReplaceFragements) {

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            System.out.println("selected camera count "+getApp().getSelectedCameraCount(mSelectedCameras));
            handleViewModes();
            if(getApp().getSelectedCameraCount(mSelectedCameras)>1) {
                setMode(1,willReplaceFragements);
                mBinding.selectViewMode.setVisibility(View.GONE);
            }else {
                setMode(selectedMode,willReplaceFragements);
                mBinding.selectViewMode.setVisibility(View.VISIBLE);
            }
        }
    }


    private void handleViewModes() {

        mBinding.mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                v.setBackgroundColor(Color.parseColor("#132558"));
                setMode(1,true);
            }
        });

        mBinding.mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                v.setBackgroundColor(Color.parseColor("#132558"));
                setMode(2,true);
            }
        });
//        setMode(1);
    }

    private void setMode(int viewMode,Boolean willReplaceFragments) {



//        mBinding.layoutFrameCameras.requestLayout();
//        if(viewMode<10)
//            return;


        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
        {

            switch (viewMode){
                case 2:{
                    mBinding.mode2.setBackgroundColor(Color.parseColor("#132558"));
                    mBinding.mode1.setBackgroundColor(Color.WHITE);

//                    mBinding.layoutFrameCameras.marginBottom=80;
                    // Adjust marginBottom using LayoutParams
//                    mBinding.layoutFrameCameras.requestLayout();
//                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mBinding.layoutFrameCameras.getLayoutParams();
                    ///PADDING DEFORMS INTERNAL VIEWS
                    /*float density = getResources().getDisplayMetrics().density;
                    int pixels = (int) (80 * density);


                    int shiftHeight = pixels;//mBinding.imageCameraDetection.getHeight();
//                    params.bottomMargin=shiftHeight;
//                    params.height = mBinding.layoutFrameCameras.getHeight()-shiftHeight;

//                    mBinding.layoutFrameCameras.setLayoutParams(params);
//                    mBinding.layoutFrameCameras.requestLayout();
//                    mBinding.layoutFrameCameras.requestLayout();

                    mBinding.layoutFrameCameras.setPadding(
                            mBinding.layoutFrameCameras.getPaddingLeft(),
                            mBinding.layoutFrameCameras.getPaddingTop(),
                            mBinding.layoutFrameCameras.getPaddingRight(),
                             shiftHeight
                    );*/
                    ConstraintLayout cl = mBinding.camerasConstraint;
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(cl);
                    constraintSet.connect(mBinding.layoutFrameCameras.getId(), ConstraintSet.BOTTOM, mBinding.latestDetectedContainer.getId(), ConstraintSet.TOP, 0);
                    constraintSet.applyTo(cl);

                    break;
                }
                default:
                {
                    mBinding.mode1.setBackgroundColor(Color.parseColor("#132558"));
                    mBinding.mode2.setBackgroundColor(Color.WHITE);

                    /*int shiftHeight=0;
//                    mBinding.layoutFrameCameras.marginBottom=0;
                    // Reset marginBottom to 0 using LayoutParams
//                    mBinding.layoutFrameCameras.requestLayout();
//                    ViewGroup.MarginLayoutParams defaultParams = (ViewGroup.MarginLayoutParams) mBinding.layoutFrameCameras.getLayoutParams();
//                    defaultParams.bottomMargin = 0;
//                    mBinding.layoutFrameCameras.setLayoutParams(defaultParams);
//                    mBinding.layoutFrameCameras.requestLayout();
                    mBinding.layoutFrameCameras.setPadding(
                            mBinding.layoutFrameCameras.getPaddingLeft(),
                            mBinding.layoutFrameCameras.getPaddingTop(),
                            mBinding.layoutFrameCameras.getPaddingRight(),
                            shiftHeight
                    );*/
                    ConstraintLayout cl = mBinding.camerasConstraint;
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(cl);
                    constraintSet.connect(mBinding.layoutFrameCameras.getId(), ConstraintSet.BOTTOM, mBinding.camerasConstraint.getId(), ConstraintSet.BOTTOM, 0);
                    constraintSet.applyTo(cl);
                    break;
                }
            }

        }

        if(getApp().getSelectedCameraCount(mSelectedCameras)==1) {
            selectedMode = viewMode;
            if(willReplaceFragments)
                replaceCameraFragments();

        }
    }


    private void loadDetectionItems() {
//        mDetectionItems.add(createNewFakeDetection());
////        mDetectionItems.add(createNewFakeDetection());
//
//        RecognitionUtils.mDectionItems=mDetectionItems;
//        for (int i =0 ; i < 8; i++ ) {
//            mDetectionItems.add(i < 3 ? createNewFakeDetection() : createNewFakeNoDetection());
//        }
    }




    public void initToolbar() {
        super.initToolbar();
    }



    @Override
    public void onSuccess(FaceData faceData, boolean bOnlyReg) {
        if(EnrollSubject.next!=null)
        {
            if(EnrollSubject.next.getFace().getTrackingId()==faceData.getFace().getTrackingId())
            {
                if(FaceDetectorProcessor.filterFaces&&faceData.getImageQualityScore()<=EnrollSubject.next.getImageQualityScore())
                    return;//eliminate updating current tracking id if new image is not better quality than the already existing one
            }
        }

        EnrollSubject.next = faceData;
        imageCameraDetection.setImageBitmap(faceData.getBestImage());
        if(faceData.getScoreMatch()>=RecognitionUtils.similarityThreshold)
        {
            tvUserName.setText(faceData.getName());
            referenceCard.setVisibility(View.VISIBLE);
            try {
                Subject subject= (Subject) faceData.subject;
                refImageCameraDetection.setImageBitmap(subject.loadImage(getContext()));
            }catch (Exception es)
            {
            }
//            currentCard.setOnClickListener(null);
            if(isAdded()) {
                currentCard.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), com.design.R.color.cameraGreenBackgroundColor)));
                referenceCard.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), com.design.R.color.cameraGreenBackgroundColor)));
            }
            tvUserName.setTextColor(Color.parseColor("#013220"));
        }
        else {
            tvUserName.setText(Constants.Name_NOTIDENTIFIED);
            referenceCard.setVisibility(View.GONE);
            if(!currentCard.hasOnClickListeners()) {
                currentCard.setOnClickListener(v -> {
                    if (EnrollSubject.next.getFeatures() != null&&EnrollSubject.next.getScoreMatch()<RecognitionUtils.similarityThreshold) {
                        Intent intent = new Intent(getContext(), EnrollSubject.class);
                        intent.putExtra("RecognitionResult", "next");
                        getActivity().startActivityForResult(intent, SUBJECTCARD_REQUEST_CODE);
                    }
                });
            }
//            currentCard.setCardBackgroundColor(Color.RED);
            if(isAdded())
                currentCard.setBackgroundTintList( ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.red)));
            tvUserName.setTextColor(Color.RED);
        }
        mDetectItemAdapter.lastDataChange = new Date().getTime();
    }

    @Override
    public void onSuccessNull() {

    }

    private int reportError=0;
    @Override
    public void onError(Exception exp) {

        if(reportError<5)
        {
            reportError++;
            try {
                Toast.makeText(getContext(), "Ai Model encountered an error : " + exp + " Restart App", Toast.LENGTH_SHORT).show();
            }catch (Exception ed)
            {
            }
        }


    }

    public void resume() {
        //replaceCameraFragments();
//        showCameraViews();
//        setCameraSelection(true);
//        bCameraSelectionChanged=true;
    }

    public void pause() {
    }



}
