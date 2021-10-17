package com.wrteam.quiz;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;


public class AdUtils {

    public static void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }

    /*   public static void watchVideo(final Activity activity) {
           TextView tvWatchVideo = activity.findViewById(R.id.tvWatchNow);

           tvWatchVideo.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   DrawerActivity.showRewardedVideo(activity);
               }
           });

       }

       public static void watchVideo(final Activity activity, View view) {
           TextView tvWatchVideo = view.findViewById(R.id.tvWatchNow);
           RelativeLayout videoLyt = view.findViewById(R.id.videoLyt);
           videoLyt.setVisibility(View.VISIBLE);
           tvWatchVideo.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   DrawerActivity.showRewardedVideo(activity);
               }
           });

       }
   */
    public static void loadNativeAd(Activity activity, final RecyclerView.ViewHolder holder) {
        AdLoader.Builder builder = new AdLoader.Builder(activity, activity.getString(R.string.native_unit_id))
                .forNativeAd(nativeAd -> {

                    AdUtils.populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
                });

        // Load the Native ads.

        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Handle the failure by logging, altering the UI, and so on.
            }
            @Override
            public void onAdClicked() {
                // Log the click event or other custom behavior.
            }
        }).build().loadAd(new AdRequest.Builder().build());
    }

/*    public static void loadAdView(RelativeLayout fbBannerAd,final Activity activity) {
        AdView fbAdView = null;

        fbAdView = new com.facebook.ads.AdView(activity, activity.getResources().getString(R.string.fb_placement_id), AdSize.BANNER_HEIGHT_50);
        fbBannerAd.addView(fbAdView);
        fbAdView.loadAd(fbAdView.buildLoadAdConfig().withAdListener(new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
            }

            @Override
            public void onAdLoaded(Ad ad) {
            }

            @Override
            public void onAdClicked(Ad ad) {
                Toast.makeText(activity, "Ad Clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        }).build());
    }*/

}