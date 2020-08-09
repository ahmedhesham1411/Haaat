package com.uriallab.haat.haat.UI.Adapters;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.DataModels.JourneyModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.Activities.DriverNewOrderActivity;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.databinding.ItemJourneyBinding;

import java.util.List;

/**
 * Created by Mahmoud on 4/19/2020.
 */

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    private Activity activity;
    private List<JourneyModel.ResultBean.OrdersBean> incomingList;

    public JourneyAdapter(Activity activity, List<JourneyModel.ResultBean.OrdersBean> incomingList) {
        this.activity = activity;
        this.incomingList = incomingList;
    }

    @Override
    public JourneyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemJourneyBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_journey, parent, false);
        return new JourneyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final JourneyViewHolder holder, final int position) {

        if (!incomingList.get(position).isOrd_Is_Show()) {
            holder.binding.mainLin.setBackgroundResource(R.drawable.shape_rounded_moov);

            holder.binding.storeNameTxt.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.orderDetails.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.orderNumber.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.orderNumberTxt.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.meTxt.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.recievingLoc.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.deliveringLoc.setTextColor(activity.getResources().getColor(R.color.colorWhite));
        }

        try {
            Picasso.get().load(incomingList.get(position).getOrd_Shop_ImgUrl())
                    .placeholder(R.drawable.store_blue).into(holder.binding.orderImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.binding.orderNumber.setText(String.valueOf(incomingList.get(position).getOrder_ID()));
        holder.binding.storeNameTxt.setText(incomingList.get(position).getOrd_Shop_Nm());
        holder.binding.orderDetails.setText(incomingList.get(position).getOrd_Dtls());

        holder.binding.deliverLocImg.setImageResource(R.drawable.delivering);
        holder.binding.recieveLocImg.setImageResource(R.drawable.delivering);
        holder.binding.meLocImg.setImageResource(R.drawable.location);

        holder.binding.deliverLocImg.setColorFilter(activity.getResources().getColor(R.color.colorMoov), PorterDuff.Mode.SRC_ATOP);
        holder.binding.recieveLocImg.setColorFilter(activity.getResources().getColor(R.color.colorMoov), PorterDuff.Mode.SRC_ATOP);
        holder.binding.meLocImg.setColorFilter(activity.getResources().getColor(R.color.colorMoov), PorterDuff.Mode.SRC_ATOP);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
//                bundle.putDouble("shopLat", Double.parseDouble(incomingList.get(position).getShop_Lat()));
//                bundle.putDouble("shopLng", Double.parseDouble(incomingList.get(position).getShop_Lng()));
//                bundle.putDouble("clientLat", Double.parseDouble(incomingList.get(position).getClient_Lat()));
//                bundle.putDouble("clientLng", Double.parseDouble(incomingList.get(position).getClient_Lng()));
//                bundle.putString("orderId", String.valueOf(incomingList.get(position).getOrder_ID()));
//                bundle.putString("clientId", String.valueOf(incomingList.get(position).getClient_ID()));
//                bundle.putString("deliveryId", String.valueOf(incomingList.get(position).getDriver_ID()));
//
//                bundle.putString("userName", String.valueOf(incomingList.get(position).getClient_Full_Name()));
//                bundle.putString("userImg", String.valueOf(incomingList.get(position).getClient_Full_Name()));
//                bundle.putString("userRate", String.valueOf(incomingList.get(position).getClient_Rate()));
//                bundle.putString("userTRate", String.valueOf(incomingList.get(position).getClient_Count_Rate()));

                bundle.putString("orderId", String.valueOf(incomingList.get(position).getOrder_ID()));
                IntentClass.goToActivity(activity, DriverNewOrderActivity.class, bundle);
            }
        });

        holder.binding.orderLocLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    IntentClass.goToLocationOnMap(activity,
                            Double.parseDouble(incomingList.get(position).getShop_Lat()),
                            Double.parseDouble(incomingList.get(position).getShop_Lng()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.binding.deliverLocLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    IntentClass.goToLocationOnMap(activity,
                            Double.parseDouble(incomingList.get(position).getClient_Lat()),
                            Double.parseDouble(incomingList.get(position).getClient_Lng()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return incomingList.size();
    }

    public class JourneyViewHolder extends RecyclerView.ViewHolder {

        private ItemJourneyBinding binding;

        private JourneyViewHolder(ItemJourneyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}