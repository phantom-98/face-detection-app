package com.facecoolalert.ui.subject.multiEnrollment.FolderPicker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AddFolderViewModel extends ViewModel {
    private AddFolderViewModel viewModel;
    private String folderPath;

    // TODO: Implement the ViewModel


    public void setViewModel(AddFolderViewModel viewModel) {
        this.viewModel = viewModel;
        
    }

    public void setFolderPath(String folderPath) {
        this.folderPath=folderPath;
    }


    public void showFolderImages() {

    }


    public LiveData<List<File>> getItemListLiveData() {
        List<File> photos=new LinkedList<>();
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        try{

            File folder=new File(folderPath);
            photos.addAll(Arrays.asList(Objects.requireNonNull(folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String fileName = pathname.getName().toLowerCase();
//                    if(!fileName.contains("_"))
//                        return false;
                    for (String extension : imageExtensions) {
                        if (fileName.endsWith(extension)) {
                            return true; // File is a valid image
                        }
                    }
                    return false;
                }
            }))));

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return new MutableLiveData<>(photos);

    }
}