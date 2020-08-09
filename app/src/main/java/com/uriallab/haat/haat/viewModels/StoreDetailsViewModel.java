package com.uriallab.haat.haat.viewModels;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.DataModels.GoogleStoresModel;
import com.uriallab.haat.haat.DataModels.ProductsModel;
import com.uriallab.haat.haat.DataModels.StoreDetailsModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.StoreUsersReviewsActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.MakeOrderFirstStepActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.OtherBranchesActivity;
import com.uriallab.haat.haat.UI.Activities.makeOrder.StoreDetailsActivity;
import com.uriallab.haat.haat.Utilities.Dialogs;
import com.uriallab.haat.haat.Utilities.GPSTracker;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.LoadingDialog;
import com.uriallab.haat.haat.Utilities.Utilities;

import java.lang.reflect.Type;
import java.text.NumberFormat;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mahmoud on 4/23/2020.
 */

public class StoreDetailsViewModel {

    public ObservableInt rotation = new ObservableInt(0);

    public ObservableBoolean isFromServerObservable = new ObservableBoolean(false);

    private StoreDetailsActivity activity;

    private StoreDetailsModel storeDetailsModel;

    private GoogleStoresModel otherBranchesList;

    private String placeId;

    public double lat, lng;

    private boolean isOpen = false;

    private String photoUrl;

    public StoreDetailsViewModel(StoreDetailsActivity activity, String placeId, boolean isFromServer) {
        this.activity = activity;
        this.placeId = placeId;

        isFromServerObservable.set(isFromServer);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("en"))
            rotation.set(180);

        if (isFromServer) {
            getServerStoreDetails();

            getProducts();
        } else {
            getStoreDetails();
        }
    }

    private void getStoreDetails() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        String url = "details/json?place_id=" + placeId +
                "&fields=geometry,icon,formatted_address,photo,place_id,url,name,opening_hours,rating,review" +
                "&key=" + activity.getString(R.string.api_key) +
                "&language=" + ConfigurationFile.getCurrentLanguage(activity);
        APIModel.getMethodForGoogle(activity, url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getStoreDetails();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("response", responseString);
                Type dataType = new TypeToken<StoreDetailsModel>() {
                }.getType();
                storeDetailsModel = new Gson().fromJson(responseString, dataType);

                activity.binding.storeBranchLin.setVisibility(View.VISIBLE);
                getOtherBranches(storeDetailsModel.getResult().getName());

                activity.binding.storeLocation.setText(storeDetailsModel.getResult().getFormatted_address());

                activity.binding.storeName.setText(storeDetailsModel.getResult().getName());

                lat = storeDetailsModel.getResult().getGeometry().getLocation().getLat();
                lng = storeDetailsModel.getResult().getGeometry().getLocation().getLng();

                NumberFormat mNumberFormat = NumberFormat.getInstance();
                mNumberFormat.setMinimumFractionDigits(3);
                mNumberFormat.setMaximumFractionDigits(3);

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    GPSTracker gpsTracker = new GPSTracker(activity);

                    String distance = mNumberFormat.format(Math.round(Utilities.getKilometers(
                            gpsTracker.getLocation().getLatitude(),
                            gpsTracker.getLocation().getLongitude(),
                            storeDetailsModel.getResult().getGeometry().getLocation().getLat(),
                            storeDetailsModel.getResult().getGeometry().getLocation().getLng())));

                    Log.e("distance", distance + " ");

                    activity.binding.distanceFromYou.setText(activity.getString(R.string.distance_from_you) + "  " + distance +
                            activity.getString(R.string.km));
                }

                try {
                    photoUrl = "https://maps.googleapis.com/maps/api/place/photo?photoreference=" +
                            storeDetailsModel.getResult().getPhotos().get(0).getPhoto_reference()
                            + "&maxheight=400&maxwidth=400&key=AIzaSyAmD_A7N-SI2JbkhGh4xY_OFip7GtQRZfg";
                } catch (Exception e) {
                    photoUrl = storeDetailsModel.getResult().getIcon();
                    e.printStackTrace();
                }

                Picasso.get().load(photoUrl).placeholder(R.drawable.logo).into(activity.binding.storeImg);

                try {
                    if (storeDetailsModel.getResult().getOpening_hours().isOpen_now()) {
                        isOpen = true;
                        activity.binding.openClosed.setText(activity.getString(R.string.open));
                    } else
                        activity.binding.openClosed.setText(activity.getString(R.string.closed));

                    activity.binding.openFromTo.setText(storeDetailsModel.getResult().getOpening_hours().getPeriods().get(0).getOpen().getTime() + " - " +
                            storeDetailsModel.getResult().getOpening_hours().getPeriods().get(0).getOpen().getTime());
                } catch (Exception e) {
                    activity.binding.openClosed.setText(activity.getString(R.string.closed));
                    e.printStackTrace();
                }

                try {
                    activity.binding.starBar.setRating((int) storeDetailsModel.getResult().getRating());
                    activity.binding.userRatesNumber.setText(storeDetailsModel.getResult().getReviews().size() + " " + activity.getString(R.string.comment));
                } catch (Exception e) {
                    activity.binding.userRatesNumber.setText(0 + " " + activity.getString(R.string.comment));
                    e.printStackTrace();
                }

                activity.binding.recyclerMenu.setVisibility(View.GONE);
                activity.binding.menuTxt.setVisibility(View.GONE);

                // TODO: 7/13/2020
//                try {
//                    if (storeDetailsModel.getResult().getPhotos().isEmpty()) {
//                        activity.binding.recyclerMenu.setVisibility(View.GONE);
//                        activity.binding.menuTxt.setVisibility(View.GONE);
//                    } else
//                        activity.initMenuRecycler(storeDetailsModel.getResult().getPhotos());
//                } catch (Exception e) {
//                    activity.binding.recyclerMenu.setVisibility(View.GONE);
//                    activity.binding.menuTxt.setVisibility(View.GONE);
//                    e.printStackTrace();
//                }
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

    private void getServerStoreDetails() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Data/GetStorDetails?place_id=" + placeId, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getServerStoreDetails();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("response", responseString);
                Type dataType = new TypeToken<StoreDetailsModel>() {
                }.getType();
                storeDetailsModel = new Gson().fromJson(responseString, dataType);

                activity.binding.storeLocation.setText(storeDetailsModel.getResult().getFormatted_address());

                activity.binding.storeName.setText(storeDetailsModel.getResult().getName());

                lat = storeDetailsModel.getResult().getGeometry().getLocation().getLat();
                lng = storeDetailsModel.getResult().getGeometry().getLocation().getLng();

                NumberFormat mNumberFormat = NumberFormat.getInstance();
                mNumberFormat.setMinimumFractionDigits(3);
                mNumberFormat.setMaximumFractionDigits(3);

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    GPSTracker gpsTracker = new GPSTracker(activity);

                    String distance = mNumberFormat.format(Math.round(Utilities.getKilometers(
                            gpsTracker.getLocation().getLatitude(),
                            gpsTracker.getLocation().getLongitude(),
                            storeDetailsModel.getResult().getGeometry().getLocation().getLat(),
                            storeDetailsModel.getResult().getGeometry().getLocation().getLng())));

                    Log.e("distance", distance + " ");

                    activity.binding.distanceFromYou.setText(activity.getString(R.string.distance_from_you) + "  " + distance +
                            activity.getString(R.string.km));
                }


                try {
                    photoUrl = storeDetailsModel.getResult().getPhotos().get(0).getPhoto_reference();
                } catch (Exception e) {
                    photoUrl = storeDetailsModel.getResult().getIcon();
                    e.printStackTrace();
                }

                Picasso.get().load(photoUrl).into(activity.binding.storeImg);

                try {
                    activity.binding.openFromTo.setText(storeDetailsModel.getResult().getOpening_hours().getPeriods().get(0).getOpen().getTime() + " - " +
                            storeDetailsModel.getResult().getOpening_hours().getPeriods().get(0).getOpen().getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (storeDetailsModel.getResult().getOpening_hours().isOpen_now()) {
                        isOpen = true;
                        activity.binding.openClosed.setText(activity.getString(R.string.open));
                    } else
                        activity.binding.openClosed.setText(activity.getString(R.string.closed));
                } catch (Exception e) {
                    activity.binding.openClosed.setText(activity.getString(R.string.closed));
                    e.printStackTrace();
                }

                try {
                    activity.binding.starBar.setRating((int) storeDetailsModel.getResult().getRating());
                    activity.binding.userRatesNumber.setText(storeDetailsModel.getResult().getReviews().size() + " " + activity.getString(R.string.comment));
                } catch (Exception e) {
                    activity.binding.userRatesNumber.setText(0 + " " + activity.getString(R.string.comment));
                    e.printStackTrace();
                }

//                try {
//                    if (storeDetailsModel.getResult().getPhotos().isEmpty()) {
//                        activity.binding.recyclerMenu.setVisibility(View.GONE);
//                        activity.binding.menuTxt.setVisibility(View.GONE);
//                    } else
//                        activity.initMenuRecycler(storeDetailsModel.getResult().getPhotos());
//                } catch (Exception e) {
//                    activity.binding.recyclerMenu.setVisibility(View.GONE);
//                    activity.binding.menuTxt.setVisibility(View.GONE);
//                    e.printStackTrace();
//                }
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

    private void getProducts() {
        final LoadingDialog loadingDialog = new LoadingDialog();
        APIModel.getMethod(activity, "Data/GetStorProducs?place_id=" + placeId, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getProducts();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("response", responseString);
                Type dataType = new TypeToken<ProductsModel>() {
                }.getType();
                ProductsModel data = new Gson().fromJson(responseString, dataType);

                if (data.getResult().getProducts().size() > 0)
                    activity.initProductsRecycler(data.getResult().getProducts());
                else
                    isFromServerObservable.set(false);
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

    public void workingHours() {
        try {
            if (storeDetailsModel.getResult().getOpening_hours().getWeekday_text().size() > 0) {
                final Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_alert_working_hours);
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView text_value = dialog.findViewById(R.id.text_value);

                StringBuilder weekdayHours = new StringBuilder();
                for (int i = 0; i < storeDetailsModel.getResult().getOpening_hours().getWeekday_text().size(); i++)
                    weekdayHours.append(storeDetailsModel.getResult().getOpening_hours().getWeekday_text().get(i)).append("\n");
                text_value.setText(weekdayHours.toString());

                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getReviews() {
        try {
            if (storeDetailsModel.getResult().getReviews().size() > 0) {
                Intent intent = new Intent(activity, StoreUsersReviewsActivity.class);
                Gson gson = new Gson();
                String myJson = gson.toJson(storeDetailsModel);
                intent.putExtra("myjson", myJson);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            Utilities.toastyError(activity, activity.getString(R.string.no_reviews));
            e.printStackTrace();
        }
    }

    public void otherBranches() {
        try {
            if (otherBranchesList.getResults().size() > 0) {
                Intent intent = new Intent(activity, OtherBranchesActivity.class);
                Gson gson = new Gson();
                String myJson = gson.toJson(otherBranchesList);
                intent.putExtra("myjson", myJson);
                activity.startActivityForResult(intent, 369);
            }
        } catch (Exception e) {
            Utilities.toastyError(activity, activity.getString(R.string.no_reviews));
            e.printStackTrace();
        }
    }

    public void next() {
        if (isOpen) {
            Bundle bundle = new Bundle();
            bundle.putString("storeName", storeDetailsModel.getResult().getName());
            bundle.putString("shopImg", photoUrl);
            bundle.putDouble("lat", lat);
            bundle.putDouble("lng", lng);
            IntentClass.goToActivity(activity, MakeOrderFirstStepActivity.class, bundle);
        } else
            Utilities.toastyError(activity, activity.getString(R.string.closed_now));
    }

    public void back() {
        activity.finish();
    }

    public void shareStore() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_TEXT, "http://haat.com/" + placeId + "$");
        intent.setType("text/plain");
        activity.startActivity(Intent.createChooser(intent, activity.getResources().getString(R.string.app_name)));
    }

    private void getOtherBranches(final String placeName) {
        final LoadingDialog loadingDialog = new LoadingDialog();
        String url = "textsearch/json?query=" + placeName + "&key=" + activity.getResources().getString(R.string.api_key) +
                "&language=" + ConfigurationFile.getCurrentLanguage(activity);
        APIModel.getMethodForGoogle(activity, url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("response", responseString + "Error");
                switch (statusCode) {
                    default:
                        APIModel.handleFailure(activity, statusCode, responseString, new APIModel.RefreshTokenListener() {
                            @Override
                            public void onRefresh() {
                                getOtherBranches(placeName);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("response", responseString);
                Type dataType = new TypeToken<GoogleStoresModel>() {
                }.getType();
                otherBranchesList = new Gson().fromJson(responseString, dataType);

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