package com.facecoolalert.ui.Search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecool.attendance.facedetector.FaceData;
import com.facecool.cameramanager.utils.BitmapUtils;
import com.facecoolalert.R;
import com.facecoolalert.databinding.FragmentSearchBinding;
import com.facecoolalert.ui.base.BaseFragment;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.utils.GenUtils;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.mlkit.vision.face.Face;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchFragment extends BaseFragment {

    public static int PICK_IMAGE_REQUEST = 146;
    private LinearLayout searchResults;
    private RelativeLayout searchContainer;
    private EditText searchField;
    private ProgressBar progressBar;
    private TextView progressText;
    private CardView initSelectCardView;
    private Button matchesButton;
    private RecyclerView matchesRecyclerView;
    private CardView noMatchesCardView;
    private Button noMatchesButton;
    private RecyclerView noMatchesRecyclerView;
    private CardView rejectedCardView;
    private Button rejectedButton;
    private RecyclerView rejectedRecyclerView;
    private CardView matchesCardView;
    @Override
    public ViewDataBinding getBinding(LayoutInflater inflater) {
        this.mViewDataBinding= FragmentSearchBinding.inflate(inflater);
        return mViewDataBinding;
    }

    @Override
    public void onPostCreateView() {

        declareFields();
        View.OnClickListener selGalerry=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSelectCardView.setVisibility(View.GONE);
//                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

                galleryIntent.setType("image/*");
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                searchResults.setVisibility(View.VISIBLE);

            }
        };

        mToolBarLeftIcon.setOnClickListener(v -> {
            openMenu();
        });

        searchContainer.setOnClickListener(selGalerry);
        initSelectCardView.setOnClickListener(selGalerry);
        searchField.setOnClickListener(selGalerry);


    }

    private void declareFields() {
        View rootView=mViewDataBinding.getRoot();
        searchResults = rootView.findViewById(R.id.searchResults);
        searchContainer = rootView.findViewById(R.id.search_container);
        searchField = rootView.findViewById(R.id.search_field);
        progressBar = rootView.findViewById(R.id.progressBar);
        progressText=rootView.findViewById(R.id.progress_text);
        initSelectCardView = rootView.findViewById(R.id.initSelect);
        matchesButton = rootView.findViewById(R.id.matchesButton);
        matchesRecyclerView = rootView.findViewById(R.id.matchesRecycleView);
        noMatchesCardView = rootView.findViewById(R.id.nomatches);
        noMatchesButton = rootView.findViewById(R.id.nomatchesButton);
        noMatchesRecyclerView = rootView.findViewById(R.id.nomatchesRecycleView);
        rejectedCardView = rootView.findViewById(R.id.rejected);
        rejectedButton = rootView.findViewById(R.id.rejectedButton);
        rejectedRecyclerView = rootView.findViewById(R.id.rejectedRecycleView);
        matchesCardView = rootView.findViewById(R.id.matches);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(PICK_IMAGE_REQUEST, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // The user has picked one or more images.
            if (data.getClipData() != null) {
                // Multiple images are selected.
                int count = data.getClipData().getItemCount();
                ArrayList<Uri> imagePaths = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
//                    String imagePath = getImagePathFromUri(imageUri);
                    if (imageUri != null) {
                        imagePaths.add(imageUri);
                    }
                }

                // Now we have the paths of selected images in the "imagePaths" ArrayList.
                System.out.println("Gotten "+imagePaths);
                processImages(imagePaths);
            } else {
                // Single image is selected.
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    // Now we have the path of the selected image in "imagePath".
                    ArrayList<Uri> imagePaths = new ArrayList<>();
                    imagePaths.add(imageUri);
                    processImages(imagePaths);
                }
            }
        }
    }






    private void processImages(ArrayList<Uri> imagePaths) {

        matchesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        noMatchesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        rejectedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SearchSuccessResultAdapter searchSuccessResultAdapter=new SearchSuccessResultAdapter();
        NoMatchesSearchResultAdapter nonMatchedAdapter=new NoMatchesSearchResultAdapter();
        RejectedSearchAdapter rejectedSearchAdapter=new RejectedSearchAdapter();

        matchesRecyclerView.setAdapter(searchSuccessResultAdapter);
        noMatchesRecyclerView.setAdapter(nonMatchedAdapter);
        rejectedRecyclerView.setAdapter(rejectedSearchAdapter);

        SearchSuccessResultAdapter matched = searchSuccessResultAdapter;
        NoMatchesSearchResultAdapter nonMatched = nonMatchedAdapter;
        RejectedSearchAdapter rejected = rejectedSearchAdapter;


        matchesButton.setText("Matches Found "+matched.size());
        noMatchesButton.setText("No Match Found "+nonMatched.size());
        rejectedButton.setText("Rejected Images "+rejected.size());

        Runnable updateStats=new Runnable() {
            @Override
            public void run() {
                System.out.println("updating Ui");

                System.out.println("Results "+matched.size()+","+nonMatched.size()+","+rejected.size());
                matchesButton.setText("Matches Found "+matched.size());
                noMatchesButton.setText("No Match Found "+nonMatched.size());
                rejectedButton.setText("Rejected Images "+rejected.size());
            }
        };



       ExecutorService threadPool= Executors.newCachedThreadPool();

        Handler uiHandler = new Handler(Looper.getMainLooper());

        final int[] i = {0};
       for(Uri image:imagePaths) {

           i[0]++;
           int finalI = i[0];
           uiHandler.post(()->{
               progressBar.setProgress((finalI / imagePaths.size())*100);
               System.out.println("Processing "+ finalI);
               progressText.setText(finalI +" Files Uploaded");
           });

           threadPool.submit(() -> {



               final Boolean[] completed = {false};

               FaceDetectorProcessor detectorProcessor = new FaceDetectorProcessor(getContext(), new FaceDetectorProcessor.ResultListener() {

                   @Override
                   public void onSuccess(FaceData faceData, boolean bOnlyReg) {

                   }

                   @Override
                   public void onSuccessNull() {

                   }

                   @Override
                   public void onError(Exception exp) {

                   }
               }){
                   @Override
                   protected void onSuccessReg(@NonNull Bitmap bmpFrame, @NonNull List<Face> faces) {
                        new Thread(()-> {
                            if (faces.size() == 0) {
                                rejected.add(new ImageSearchResult(new FaceData(null, 0f, bmpFrame), "No Face Found"));
                                completed[0] = true;
//                           updateStats.run();
                                uiHandler.post(updateStats);
                            }

                            for (Face face : faces) {
                                FaceData faceData = null;
                                try {
                                    faceData = RecognitionUtils.extractFeatures(bmpFrame, RecognitionUtils.createFaceData(bmpFrame, face));
                                    RecognitionUtils.recognizeFace(faceData);

                                    if (faceData.getScoreMatch() >= RecognitionUtils.similarityThreshold) {
                                        matched.add(new ImageSearchResult(faceData, faceData.getName() + " (" + GenUtils.roundScore(faceData.getScoreMatch()) + "%)"));
                                    } else {
                                        nonMatched.add(new ImageSearchResult(faceData, "No Matches (" + GenUtils.roundScore(faceData.getScoreMatch()) + " %)"));
                                    }


                                } catch (Exception es) {
                                    es.printStackTrace();
                                    if (faceData != null)
                                        nonMatched.add(new ImageSearchResult(faceData, "Unable to get Face features"));
                                    else
                                        nonMatched.add(new ImageSearchResult(new FaceData(face, 0f, bmpFrame), "Unable to get Face features"));

                                }
//                           updateStats.run();
                                uiHandler.post(updateStats);
                            }
                            completed[0] = true;
                        }).start();
                   }

               };

               detectorProcessor.setReportSuccessNull(true);

               try {
//                   detectorProcessor.detectFace(BitmapFactory.decodeFile(image));
                   Bitmap bitmap= BitmapUtils.getBitmapFromContentUri(getContext().getContentResolver(),image);
                   detectorProcessor.detectFace(bitmap);
               }catch (Exception e)
               {
                   e.printStackTrace();
               }

               Boolean willWait=true;
               if(willWait)
               {
                   //code to wait
                   Long delayed = 0L;
                   try {
                       while (!completed[0] || delayed > 3000) {
                           Thread.sleep(200);
                           delayed += 200;
                       }

                   } catch (Exception ew) {
                       ew.printStackTrace();
                   }
               }

           });

       }





    }


}
