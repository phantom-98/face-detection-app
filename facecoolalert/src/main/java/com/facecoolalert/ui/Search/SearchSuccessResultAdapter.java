package com.facecoolalert.ui.Search;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.common.ImagePreviewDialog;
import com.facecoolalert.database.entities.Subject;

import java.util.ArrayList;
import java.util.List;

public class SearchSuccessResultAdapter extends RecyclerView.Adapter<SearchSuccessResultAdapter.SearchResultViewHolder> {

    private List<ImageSearchResult> results;

//    public SearchSuccessResultAdapter(HashMap<String, String> items, HashMap<String, Long> matchedRefImages) {
//        setList(items, matchedRefImages);
//    }

    SearchSuccessResultAdapter()
    {
        results=new ArrayList<>();
    }

//    public void setList(HashMap<String, String> items, HashMap<String, Long> matchedRefImages) {
//        this.items = items;
//        this.refImages=matchedRefImages;
//        listItems = new ArrayList<>(items.keySet());
//        notifyDataSetChanged();
//    }

    public void add(ImageSearchResult result)
    {
        results.add(result);
        new Handler(Looper.getMainLooper()).post(()->{
            notifyItemInserted(results.size()-1);
//            notifyDataSetChanged();
        });
    }

    public int size()
    {
        return getItemCount();
    }


    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_success_result, parent, false);
        return new SearchResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        ImageSearchResult result=results.get(position);
        holder.text.setText(result.getText());
//        Bitmap picture=BitmapFactory.decodeFile(filePath);
        Bitmap picture=result.getFaceData().getBestImage();
        holder.picture.setImageBitmap(picture);
        Subject tmp= (Subject) result.getFaceData().subject;
        Bitmap refPicture=tmp.loadImage(holder.itemView.getContext());
        holder.refPicture.setImageBitmap(refPicture);

//        holder.picture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ImagePreviewDialog imagePreviewDialog=new ImagePreviewDialog(v.getContext());
//                imagePreviewDialog.show(picture);
//            }
//        });

        holder.refPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePreviewDialog imagePreviewDialog=new ImagePreviewDialog(v.getContext());
                imagePreviewDialog.show(refPicture);
            }
        });


        Bitmap finalPicture = picture;
        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalPicture !=null) {
                    ImagePreviewDialog imagePreviewDialog = new ImagePreviewDialog(v.getContext());
                    imagePreviewDialog.show(finalPicture);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class SearchResultViewHolder extends RecyclerView.ViewHolder {
        public ImageView picture;
        public TextView text;
        public ImageView refPicture;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.photo);
            text = itemView.findViewById(R.id.text);
            refPicture=itemView.findViewById(R.id.refphoto);
        }
    }
}
