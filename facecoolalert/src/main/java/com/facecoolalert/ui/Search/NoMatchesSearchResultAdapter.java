package com.facecoolalert.ui.Search;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecool.cameramanager.utils.BitmapUtils;
import com.facecoolalert.R;
import com.facecoolalert.common.ImagePreviewDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoMatchesSearchResultAdapter extends RecyclerView.Adapter<NoMatchesSearchResultAdapter.SearchResultViewHolder> {
//    private HashMap<String, String> items;
//    private List<String> listItems;

    List<ImageSearchResult> results;



//    public NoMatchesSearchResultAdapter(HashMap<String, String> items) {
//        setList(items);
//    }
//
//    public void setList(HashMap<String, String> items) {
//        this.items = items;
//        listItems = new ArrayList<>(items.keySet());
//        notifyDataSetChanged();
//    }

    public NoMatchesSearchResultAdapter()
    {
        results=new ArrayList<>();
    }

    public void add(ImageSearchResult result)
    {
        results.add(result);
//        notifyItemInserted(results.size()-1);
        new Handler(Looper.getMainLooper()).post(()->{
//            notifyItemInserted(results.size()-1);
            notifyDataSetChanged();
        });
    }

    public int size()
    {
        return getItemCount();
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {

        ImageSearchResult result=results.get(position);
        holder.text.setText(result.getText());
//        Bitmap picture=BitmapFactory.decodeFile(filePath);
//        holder.picture.setImageBitmap(picture);
//
//        holder.picture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ImagePreviewDialog imagePreviewDialog=new ImagePreviewDialog(v.getContext());
//                imagePreviewDialog.show(picture);
//            }
//        });
        Bitmap picture=result.getFaceData().getBestImage();
        try {
            holder.picture.setImageBitmap(picture);
        }catch (Exception ed)
        {

        }

        if(picture!=null) {
            Bitmap finalPicture = picture;
            holder.picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalPicture != null) {
                        ImagePreviewDialog imagePreviewDialog = new ImagePreviewDialog(v.getContext());
                        imagePreviewDialog.show(finalPicture);
                    }
                }
            });
        }
        else{
            holder.picture.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class SearchResultViewHolder extends RecyclerView.ViewHolder {
        public ImageView picture;
        public TextView text;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.photo);
            text = itemView.findViewById(R.id.text);
        }
    }
}
