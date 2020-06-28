package com.facecoolalert.ui.subject.multiEnrollment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ipsec.ike.exceptions.IkeNetworkLostException;
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
import com.fasterxml.jackson.databind.node.POJONode;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FolderPhotosAdapter extends RecyclerView.Adapter<FolderPhotosAdapter.ViewHolder> {
    private List<File> photos;
    public HashMap<Integer,String> holderstatus;

    public HashMap<Integer,Bitmap> fetchedBitmaps;

    @NonNull
    @Override
    public FolderPhotosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrollment_frame, parent, false); // Specify the item layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderPhotosAdapter.ViewHolder holder, int position) {
        File myphoto=photos.get(position);
        try{
            holder.text.setText(myphoto.getName());
            holder.text.setTextColor(Color.BLACK);
            holder.image.setImageBitmap(null);
            Bitmap imageBitmap;
            if(!fetchedBitmaps.containsKey(position)) {
                imageBitmap = BitmapUtils.getPictureBitmap(myphoto.getAbsolutePath());
            }else
                imageBitmap=fetchedBitmaps.get(position);
            holder.image.setImageBitmap(imageBitmap);
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
