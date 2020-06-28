package com.facecoolalert.ui.settings.logs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.utils.EditTextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class OtherLogsFragment extends Fragment {

    private View view;
    private View backButton;

    private EditText search;

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_other_logs,container,false);

        recyclerView=view.findViewById(R.id.logs_list);
        search=view.findViewById(R.id.search_logs);

        feedInfo();

        return view;
    }


    private OtherLogsAdapter otherLogsAdapter;
    private void feedInfo() {
        otherLogsAdapter=new OtherLogsAdapter(getContext());

        EditTextUtils.addTextChangeListener(search,newText -> {
            if(newText!=null)
                otherLogsAdapter.query(newText);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setAdapter(otherLogsAdapter);
//        new Thread(this::readErrorLogsLast10Minutes).start();
        ExecutorService threadPool=Executors.newSingleThreadExecutor();
        threadPool.submit(this::readErrorLogsLast10Minutes);
    }

    private void readErrorLogsLast10Minutes() {
        long currentTime = System.currentTimeMillis();
        long tenMinutesAgo = currentTime - (10 * 60 * 1000);


        Process process=null;
        String command="logcat *age:20m *:E";
        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            String prevSig="";
            String atSeparetor="at ";
            StringBuilder stringBuilder=new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                int contentStart=findIndexAfter(":",line,20)+1;
                String preContent=line.substring(0,contentStart);
//                Log.d("Reading Logs",preContent);
                String[] preContents=preContent.split("\\s+");
                if(preContents.length>3)
                    preContent = preContents[2]+"-"+preContents[3];
                if(stringBuilder.length()>0) {
                    if (line.contains(atSeparetor)&&false) {
                        stringBuilder.append("\n" + line.substring(line.indexOf(atSeparetor)));
                    } else if(prevSig.equals(preContent))
                    {
                        stringBuilder.append("\n" + line.substring(contentStart));
                    } else
                    {
                        otherLogsAdapter.add(stringBuilder.toString());
                        stringBuilder.setLength(0);
                        stringBuilder.append(line);
                    }
                }
                else
                    stringBuilder.append(line);

                prevSig=preContent;
            }
            if(stringBuilder.length()>0)
                otherLogsAdapter.add(stringBuilder.toString());

            otherLogsAdapter.notifyAllChanged();

        } catch (Exception e) {
            Log.e("Reading Logs", "Error reading logs: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Read Bytes : "+e.getStackTrace().toString());
        } finally {
            if(process != null)
                process.destroy();

        }
    }

    private int findIndexAfter(String findStr,String findIn,int afterIndex)
    {
        String space=findIn.substring(afterIndex);
        int findIndex=space.indexOf(findStr);
        if(findIndex!=-1)
            findIndex+=afterIndex;
        return findIndex;
    }


}
