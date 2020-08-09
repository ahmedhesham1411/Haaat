package com.uriallab.haat.haat.UI.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uriallab.haat.haat.DataModels.NotificationsModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.Adapters.NotificationsAdapter;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.databinding.FragmentNotificationBinding;
import com.uriallab.haat.haat.viewModels.NotificationsViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    public List<NotificationsModel.ResultBean.NotficationsBean> notificationsList = new ArrayList<>();
    private NotificationsAdapter notificationsAdapter;

    private NotificationsViewModel viewModel;

    public int nextPage = 1;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false);

        initRecycler();

        viewModel = new NotificationsViewModel(this);

        binding.setNotificationVM(viewModel);

        return binding.getRoot();
    }

    private void initRecycler() {
        notificationsAdapter = new NotificationsAdapter(getActivity(), notificationsList);
        binding.notificationRecycler.setAdapter(notificationsAdapter);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext());
        binding.notificationRecycler.setLayoutManager(mLayoutManager);

        binding.notificationRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                nextPage++;
                if (nextPage <= viewModel.lastPage && !viewModel.isLoading) {
                    viewModel.getNotificationsRequest(nextPage);
                }
            }
        });
        Utilities.runAnimation(binding.notificationRecycler, 1);
    }

    public void updateRecycler(List<NotificationsModel.ResultBean.NotficationsBean> notificationsModel) {
        if (viewModel.lastPage == 1)
            notificationsList.clear();
        notificationsList.addAll(notificationsModel);
        notificationsAdapter.notifyDataSetChanged();
    }
}