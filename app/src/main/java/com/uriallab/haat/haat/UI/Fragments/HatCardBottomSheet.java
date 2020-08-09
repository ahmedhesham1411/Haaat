package com.uriallab.haat.haat.UI.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.loopj.android.http.TextHttpResponseHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.HatServiceModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.Activities.PaymentGateActivity;
import com.uriallab.haat.haat.UI.Activities.Updates.PhotoViewActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

public class HatCardBottomSheet extends BottomSheetDialogFragment {

    private Activity activity;

    private double tPrice = 0;

    private int cardQuantity = 1;

    private HatServiceModel.ResultEntity.HaatCardCategoryEntity.CardsEntity cardData;

    public HatCardBottomSheet() {
    }

    @SuppressLint("ValidFragment")
    public HatCardBottomSheet(Activity activity, HatServiceModel.ResultEntity.HaatCardCategoryEntity.CardsEntity cardData) {
        this.activity = activity;
        this.cardData = cardData;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(activity, R.layout.bottom_sheet_hat_card, null);
        dialog.setContentView(contentView);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        Button hatCard = dialog.findViewById(R.id.hat_card);
        ImageView close = dialog.findViewById(R.id.close);
        FrameLayout plus = dialog.findViewById(R.id.plus_frame);
        FrameLayout minus = dialog.findViewById(R.id.minus_frame);
        final TextView quantity = dialog.findViewById(R.id.quantity);
        final TextView price = dialog.findViewById(R.id.price);
        TextView cardName = dialog.findViewById(R.id.card_name);
        RoundedImageView cardImg = dialog.findViewById(R.id.card_img);

        Picasso.get().load(cardData.getUrl()).placeholder(R.drawable.logo).into(cardImg);

        cardImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("img", cardData.getUrl());
                IntentClass.goToActivity(activity, PhotoViewActivity.class, bundle);
            }
        });

        tPrice = cardData.getPrice();

        cardName.setText(activity.getString(R.string.card) + " " + cardData.getName() + " " +
                activity.getString(R.string.with) + " " +
                cardData.getPrice() + " " + activity.getString(R.string.currency));
        price.setText(cardData.getPrice() + " " + activity.getString(R.string.currency));

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityTxt = quantity.getText().toString();

                cardQuantity = (Integer.parseInt(quantityTxt) + 1);

                quantity.setText(cardQuantity + "");

                tPrice = (Integer.parseInt(quantity.getText().toString()) * cardData.getPrice());

                price.setText(tPrice + " " + activity.getString(R.string.currency));
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityTxt = quantity.getText().toString();

                if (Integer.parseInt(quantityTxt) > 1) {
                    cardQuantity = (Integer.parseInt(quantityTxt) - 1);

                    quantity.setText(cardQuantity + "");
                }

                tPrice = (Integer.parseInt(quantity.getText().toString()) * cardData.getPrice());

                price.setText(tPrice + " " + activity.getString(R.string.currency));
            }
        });

        hatCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hatCardRequest();
                Bundle bundle = new Bundle();
                bundle.putDouble("money", tPrice);
                bundle.putString("orderID", "0");
                bundle.putInt("type", 3);
                bundle.putInt("hatCardId", cardData.getCard_UID());
                bundle.putInt("hatCardQuantity", cardQuantity);
                IntentClass.goToActivity(activity, PaymentGateActivity.class, bundle);
                dismiss();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void hatCardRequest() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("card_id", cardData.getCard_UID());
            jsonParams.put("card_quantity", cardQuantity);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIModel.postMethod(activity, "api/HaatCard/Check", jsonParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    case 400:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.has("error"))
                                Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                            else
                                Utilities.toastyError(activity, responseString + "    ");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 500:
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.has("error"))
                                Utilities.toastyError(activity, jsonObject.getJSONObject("error").getString("Message"));
                            else
                                Utilities.toastyError(activity, responseString + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                hatCardRequest();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.e("response", responseString);

                Bundle bundle = new Bundle();
                bundle.putDouble("money", tPrice);
                bundle.putString("orderID", "0");
                bundle.putInt("type", 3);
                bundle.putInt("hatCardId", cardData.getCard_UID());
                bundle.putInt("hatCardQuantity", cardQuantity);
                IntentClass.goToActivity(activity, PaymentGateActivity.class, bundle);

                dismiss();
            }

            @Override
            public void onStart() {
                super.onStart();
                Dialogs.showLoading(activity, loadingDialog);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Dialogs.dismissLoading(loadingDialog);
            }
        });
    }

}