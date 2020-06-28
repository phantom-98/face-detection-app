package com.facecoolalert.ui.subject.reports.viewReport;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.facecool.cameramanager.utils.BitmapUtils;
import com.facecoolalert.R;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportPhotosAdapter extends RecyclerView.Adapter<ReportPhotosAdapter.ViewHolder> {
    private List<File> photos=new ArrayList<>();
    public HashMap<Integer,String> holderstatus=new HashMap<>();

    public HashMap<Integer,Bitmap> fetchedBitmaps;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrollment_frame, parent, false); // Specify the item layout
        return new ViewHolder(view);
    }

    private ExecutorService threadPool= Executors.newCachedThreadPool();
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File myphoto=photos.get(position);
        try{
            holder.text.setText(myphoto.getName());
            holder.text.setTextColor(Color.BLACK);
            holder.image.setImageBitmap(null);
            if(!holderstatus.containsKey(position)) {
                holder.progressBar.setVisibility(View.GONE);
                holder.card.setBackgroundColor(Color.WHITE);
            }
            else if(holderstatus.get(position).equals("processing")){
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.card.setBackgroundColor(Color.WHITE);
            } else if (holderstatus.get(position).startsWith("success")) {
                holder.progressBar.setVisibility(View.GONE);
                holder.card.setBackgroundColor(Color.GREEN);
                holder.text.setText(holderstatus.get(position).replaceAll("success", myphoto.getName()));
            }
            else{
                holder.progressBar.setVisibility(View.GONE);
                holder.card.setBackgroundColor(Color.RED);
                holder.text.setText(holderstatus.get(position));
            }

            ImageView tmpimage=holder.image;
            threadPool.submit(()->{
                try {
                    Bitmap imageBitmap;
                    if (!fetchedBitmaps.containsKey(position)) {
                        imageBitmap = BitmapUtils.getPictureBitmap(myphoto.getAbsolutePath());
                    } else
                        imageBitmap = fetchedBitmaps.get(position);
                    new Handler(Looper.getMainLooper()).post(()->{
                        tmpimage.setImageBitmap(imageBitmap);
                    });
                }catch (Exception er)
                {
                    er.printStackTrace();
                }
            });
        }catch (Exception er)
        {
            er.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setItems(List<File> photos) {
        this.photos=photos;
        if(holderstatus==null)
            holderstatus=new HashMap<>();
        fetchedBitmaps=new HashMap<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image;
        public TextView text;
        public ProgressBar progressBar;

        public View itemView;

        public CardView card;

        public Bitmap bitmap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.iv_user_image);
            text=itemView.findViewById(R.id.tv_name);
            progressBar=itemView.findViewById(R.id.progress);
            this.itemView=itemView;
            this.card=itemView.findViewById(R.id.card_root);
        }
    }
}
