package com.wrteam.quiz.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.wrteam.quiz.AdUtils;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.UnifiedNativeAdViewHolder;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Category;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationList extends AppCompatActivity {
    public RecyclerView recyclerView;
    public ProgressBar progressBar;
    public AdView mAdView;
    public TextView empty_msg;
    public RelativeLayout layout;
    public static ArrayList<Category> notificationList;
    public SwipeRefreshLayout swipeRefreshLayout;
    public Snackbar snackbar;
    public Toolbar toolbar;
    private ShimmerFrameLayout mShimmerViewContainer;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        activity = NotificationList.this;
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.notification));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAdView = findViewById(R.id.banner_AdView);
        mAdView.loadAd(new AdRequest.Builder().build());
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        empty_msg = findViewById(R.id.txtblanklist);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        getData();
        Session.setNCount(0, getApplicationContext());

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void getData() {
        mShimmerViewContainer.startShimmerAnimation();
        if (Utils.isNetworkAvailable(NotificationList.this)) {
            GetNotificationList();
        } else {
            setSnackBar();
            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);
        }

    }

    public void setSnackBar() {
        snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData();
                    }
                });

        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    /*
     * Get Quiz Category from Json
     */
    public void GetNotificationList() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_NOTIFICATIONS, "1");
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {

                        notificationList = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean(Constant.ERROR);

                        if (!error) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Category category = new Category();
                                JSONObject object = jsonArray.getJSONObject(i);
                                category.setName(object.getString("title"));
                                category.setMessage(object.getString("message"));
                                category.setImage(object.getString(Constant.IMAGE));
                                if (i != 0 && i % 5 == 0) {
                                    notificationList.add(new Category(true));
                                }
                                notificationList.add(category);

                            }
                            NotificationAdapter adapter = new NotificationAdapter(NotificationList.this, notificationList);
                            recyclerView.setAdapter(adapter);

                        } else {
                            empty_msg.setVisibility(View.VISIBLE);
                            empty_msg.setText(R.string.no_notification);
                            mShimmerViewContainer.stopShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.GONE);
                        }
                        mShimmerViewContainer.stopShimmerAnimation();
                        mShimmerViewContainer.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        private ArrayList<Category> dataList;
        private Context mContext;
        private final int MENU_ITEM_VIEW_TYPE = 0;
        private final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;


        public NotificationAdapter(Context context, ArrayList<Category> dataList) {
            this.dataList = dataList;
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case UNIFIED_NATIVE_AD_VIEW_TYPE:
                    View unifiedNativeLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_unified_rec, parent, false);
                    return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
                case MENU_ITEM_VIEW_TYPE:
                default:
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_lyt, parent, false);
                    return new ItemRowHolder(v);
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder1, int position) {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case UNIFIED_NATIVE_AD_VIEW_TYPE:
                    AdUtils.loadNativeAd(activity, holder1);
                    break;
                case MENU_ITEM_VIEW_TYPE:
                    final ItemRowHolder holder = (ItemRowHolder) holder1;
                    final Category notification = dataList.get(position);
                    empty_msg.setVisibility(View.GONE);

                    if (position % 6 == 0) {
                        holder.Lytnotify.setBackgroundResource(R.drawable.blue_white_bg);
                    } else if (position % 6 == 1) {
                        holder.Lytnotify.setBackgroundResource(R.drawable.purple_white_bg);
                    } else if (position % 6 == 2) {
                        holder.Lytnotify.setBackgroundResource(R.drawable.pink_white_bg);
                    } else if (position % 6 == 3) {
                        holder.Lytnotify.setBackgroundResource(R.drawable.green_white_bg);
                    } else if (position % 6 == 4) {
                        holder.Lytnotify.setBackgroundResource(R.drawable.orange_white_bg);
                    } else if (position % 6 == 5) {
                        holder.Lytnotify.setBackgroundResource(R.drawable.sky_white_bg);
                    }

                    if (notification.getName().length() > 0)
                        holder.tvPre.setText(notification.getName().substring(0, 2));
                    holder.tvTitle.setText(notification.getName());
                    holder.tvDes.setText(Html.fromHtml(notification.getMessage()));
                    if (!notification.getImage().isEmpty()) {
                        holder.img.setImageUrl(notification.getImage(), imageLoader);
                        holder.img.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return (null != dataList ? dataList.size() : 0);
        }


        @Override
        public int getItemViewType(int position) {
            if (notificationList.get(position).isAdsShow()) {
                return UNIFIED_NATIVE_AD_VIEW_TYPE;
            }
            return MENU_ITEM_VIEW_TYPE;
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {

            public TextView tvTitle, tvPre, tvDes;
            public NetworkImageView img;
            LinearLayout Lytnotify;

            public ItemRowHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPre = itemView.findViewById(R.id.tvPre);
                tvDes = itemView.findViewById(R.id.tvDes);
                img = itemView.findViewById(R.id.img);
                Lytnotify = itemView.findViewById(R.id.Lytnotify);

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
