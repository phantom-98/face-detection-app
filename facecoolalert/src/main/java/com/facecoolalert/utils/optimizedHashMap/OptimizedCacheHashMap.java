package com.facecoolalert.utils.optimizedHashMap;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class OptimizedCacheHashMap<K, V> extends ConcurrentHashMap<K, V>{

    private HashMap<K,Long> lastKeyAccess;
    private OptimizingThread optimizingThread;

    private ArrayList<K> keysOrder;

    private int maxItems=10;

    private Long stayDuration= TimeUnit.SECONDS.toMillis(20);

    public OptimizedCacheHashMap() {
        super();
        lastKeyAccess=new HashMap<>();
        optimizingThread=new OptimizingThread();
        optimizingThread.start();
        keysOrder=new ArrayList<>();
    }


    @Nullable
    @Override
    public V get(@NonNull Object key) {
        setAccessed((K) key);
        return super.get(key);
    }




    @Nullable
    @Override
    public V put(@NonNull K key, @NonNull V value) {
        setAccessed(key);
        return super.put(key, value);
    }

    @Override
    public boolean containsKey(@NonNull Object key) {
        setAccessed((K) key);
        return super.containsKey(key);
    }

    private void setAccessed(K key) {
        Log.d("Optimized HashMap","accessed "+key);
        Long time=new Date().getTime();
        lastKeyAccess.put(key,time);
        try {
            if (keysOrder.contains(key))
                keysOrder.remove(key);
        }catch (Exception es)
        {

        }
        keysOrder.add(key);
    }


    private class OptimizingThread extends Thread{
        @Override
        public void run() {
            while(true)
            {
                try{
                    ArrayList<K> toremove=new ArrayList<>();
                    if(size()>maxItems) {
                        Long time=new Date().getTime();
                        for(int i=0;i<keysOrder.size()&&size()-toremove.size()>maxItems;i++)
                        {
                            K currKey=keysOrder.get(i);
                            if(time-lastKeyAccess.get(currKey)>stayDuration) {
                                toremove.add(currKey);
                            }else
                                break;
                        }
                    }
                    for(K i:toremove)
                    {
                        lastKeyAccess.remove(i);
                        keysOrder.remove(i);
                        remove(i);
                    }
                    Log.d("Optimized HashMap","removed "+toremove.size());
                    toremove.clear();
                    Thread.sleep(stayDuration/2);
                }catch (Exception es)
                {
                    es.printStackTrace();
                }
            }

        }
    }



}
