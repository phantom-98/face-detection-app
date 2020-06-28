package com.facecoolalert.ui.watchlist;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.databinding.FragmentWatchlistBinding;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.base.BaseFragment;
import com.facecoolalert.ui.watchlist.addWatchList.AddWatchList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WatchListFragment extends BaseFragment {
    private View view;
    private WatchlistDao watchlistDao;
    private RecyclerView recycleView;


    @Override
    public ViewDataBinding getBinding(LayoutInflater inflater) {
        this.mViewDataBinding= FragmentWatchlistBinding.inflate(inflater);
        return mViewDataBinding;
    }

    @Override
    public void onPostCreateView() {


        mToolBarLeftIcon.setOnClickListener(v -> {
            openMenu();
        });

        this.view=mViewDataBinding.getRoot();

        view.findViewById(R.id.add_new_watchlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                MainActivity act=(MainActivity)getActivity();
                act.addFragment(new AddWatchList(null));
                v.setEnabled(true);
            }
        });

        feedWatchlists();
    }

    private void feedWatchlists() {
        this.watchlistDao= MyDatabase.getInstance(getContext()).watchlistDao();
        
        this.recycleView=view.findViewById(R.id.subjects_list);

        recycleView.setLayoutManager(new LinearLayoutManager(getContext()));


        CompletableFuture.supplyAsync(()->{
            try {
                return watchlistDao.getAll();
            }catch (Exception es)
            {
                es.printStackTrace();
                return Collections.emptyList();
            }
        }).thenAcceptAsync(watchLists -> {
            Log.d("Watchlists", String.valueOf(watchLists.size()));
            new Handler(Looper.getMainLooper()).post(()->{
                recycleView.setAdapter(new WatchlistAdapter(getContext(), (List<WatchList>) watchLists, (MainActivity) getActivity()));
            });
        }, AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public WatchListFragment() {

    }
}
