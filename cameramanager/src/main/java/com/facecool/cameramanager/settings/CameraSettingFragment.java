package com.facecool.cameramanager.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.facecool.cameramanager.R;
import com.facecool.cameramanager.entity.CameraInfo;


public class CameraSettingFragment extends Fragment {

    boolean isChanged = false;

    RecyclerView listCameras;

    public CameraInfo[] mCameras = new CameraInfo[]{};
    CamerasAdapter mCameraAdapter = new CamerasAdapter();

    public Handler handlerActivity;

    public int containerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_camera_setting, null);

        listCameras = v.findViewById(R.id.list_cameras);
        listCameras.setAdapter(mCameraAdapter);
        listCameras.setLayoutManager(new LinearLayoutManager(getActivity()));


        v.findViewById(R.id.btn_back).setOnClickListener(this::onBackClick);
        v.findViewById(R.id.btn_save).setOnClickListener(this::onSaveClick);

        return v;
    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mCameras = CameraManagerHelper.getCameraInfos(getIntent());
//        setContentView(R.layout.activity_camera_setting);
//        listCameras = findViewById(R.id.list_cameras);
//        listCameras.setAdapter(mCameraAdapter);
//        listCameras.setLayoutManager(new LinearLayoutManager(this));
////        Toast.makeText(this, "You should select the video file", Toast.LENGTH_SHORT).show();
//
//    }


    public void finish() {
//        getActivity().onBackPressed();
        try {
            handlerActivity.sendEmptyMessage(1);
        }catch (Exception er)
        {
            er.printStackTrace();
        }

    }

    public void onBackClick(View v) {
        if ( isChanged ) {
            new AlertDialog.Builder(getActivity()).setMessage("Camera setting has been modified.\nWould you like to ignore changes?")
                    .setNegativeButton("No", (dialog, i)->{

                    })
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        finish();
                    })).create().show();
        } else {
            finish();
        }
    }


    public void onSaveClick(View v) {
        Toast.makeText(getActivity(), "Saving Info ...", Toast.LENGTH_SHORT).show();
        handlerActivity.sendMessage(Message.obtain(handlerActivity, 2, mCameras));
    }

    public void onRemoveClick(View v) {
        CameraInfo cameraInfo = (CameraInfo) v.getTag();

        cameraInfo.type = -1;
        cameraInfo.location = "";
        cameraInfo.username = "";
        cameraInfo.password = "";
        cameraInfo.videoPath = "";
        cameraInfo.url = "";
        cameraInfo.isFront = false;



        mCameraAdapter.notifyDataSetChanged();
    }

    public void onEditClick(View v) {
        CameraInfo cameraInfo = (CameraInfo) v.getTag();
        int count = 0;
        for (CameraInfo cInfo : mCameras ){
            if ( cInfo.type == CameraInfo.TYPE_ANDROID ){
                count++;
            }
        }

        CameraInfoFragment fragment = new CameraInfoFragment();

        fragment.mCameraInfo = cameraInfo;
        fragment.mCount = count;
        fragment.containerId = containerId;
        fragment.mCameraSettingFragment = this;

        getActivity().getSupportFragmentManager().beginTransaction()
                .add(containerId, fragment)
                .addToBackStack(null)
                .commit();

//        startActivityForResult(new Intent(getActivity(), CameraInfoFragment.class)
//                .putExtra(CameraInfoFragment.TAG_CAMERA, cameraInfo)
//                .putExtra(CameraInfoFragment.TAG_ANDROID, count), 1);
    }

    public void onChangeCamera(CameraInfo cameraInfo) {
        mCameras[cameraInfo.index] = cameraInfo;
        mCameraAdapter.notifyDataSetChanged();
        isChanged = true;
    }

    public class CameraViewHolder extends RecyclerView.ViewHolder {

        ImageView ivType;
        TextView tvName;
        TextView tvDesc;
        ImageView ivRemove;


        public CameraViewHolder(View view) {
            super(view);
            ivType = view.findViewById(R.id.iv_type);
            tvName = view.findViewById(R.id.tv_camera_name);
            tvDesc = view.findViewById(R.id.tv_camera_detail);
            ivRemove = view.findViewById(R.id.iv_remove);
            ivRemove.setOnClickListener(CameraSettingFragment.this::onRemoveClick);
            view.setOnClickListener(CameraSettingFragment.this::onEditClick);
        }

        public void bind(CameraInfo cameraInfo) {
            ivType.setImageResource(cameraInfo.type == -1 ? R.drawable.ic_mirror : R.drawable.ic_camera);
            ivRemove.setTag(cameraInfo);
            itemView.setTag(cameraInfo);
            ivRemove.setVisibility(cameraInfo.type == -1 ? View.INVISIBLE : View.VISIBLE);
            tvName.setText("Camera " + (cameraInfo.index + 1) + ": " + cameraInfo.location);
            if ( cameraInfo.type == -1 ) {
                tvDesc.setText(" --- ");
                return;
            }

            tvDesc.setText(getResources().getStringArray(R.array.type_array)[cameraInfo.type]);

        }
    }

    public class CamerasAdapter extends RecyclerView.Adapter<CameraViewHolder> {
        public CamerasAdapter() {
        }

        @Override
        public CameraViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_camera_info, null);
            return new CameraViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CameraViewHolder holder, int position) {
            holder.bind(mCameras[position]);
        }

        @Override
        public int getItemCount() {
            return mCameras.length;
        }
    }
}
