package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.shimmer.ShimmerFrameLayout;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wrteam.quiz.AdUtils;
import com.wrteam.quiz.UnifiedNativeAdViewHolder;
import com.wrteam.quiz.battle.WaitingRoomActivity;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.Session;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;


import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.ImageView;
import android.widget.ProgressBar;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.toolbox.ImageLoader;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.helper.Utils;

import com.wrteam.quiz.model.Category;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;
import java.util.Objects;


public class CategoryActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    public ProgressBar progressBar;
    public AdView mAdView;
    public TextView empty_msg;
    public RelativeLayout layout;
    public ArrayList<Category> categoryList;
    public SwipeRefreshLayout swipeRefreshLayout;
    public Snackbar snackbar;
    public Toolbar toolbar;
    public AlertDialog alertDialog;
    CategoryAdapter adapter;
    private ShimmerFrameLayout mShimmerViewContainer;

    String quizType;
    Activity activity;
    ProgressDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_category);
        activity = CategoryActivity.this;
        quizType = getIntent().getStringExtra(Constant.QUIZ_TYPE);
        layout = findViewById(R.id.layout);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.select_category));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAdView = findViewById(R.id.banner_AdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        // I am using Retrofit Client for fetching data from an API.
        // API Call Using Retrofit Client
        empty_msg = findViewById(R.id.txtblanklist);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        recyclerView = findViewById(R.id.recyclerView);

        getData();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    private void getData() {
        //mShimmerViewContainer.startShimmerAnimation();
        //progressBar.setVisibility(View.VISIBLE);
        if (Utils.isNetworkAvailable(activity)) {
            getMainCategoryFromJson();
            invalidateOptionsMenu();

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
    public void getMainCategoryFromJson() {
        // progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext())) {
            params.put(Constant.GET_CATE_BY_LANG, "1");
            params.put(Constant.LANGUAGE_ID, Session.getCurrentLanguage(getApplicationContext()));
        } else
            params.put(Constant.getCategories, "1");
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                System.out.println("========search result " + response);
                if (result) {
                    try {
                        categoryList = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response);
                        String error = jsonObject.getString(Constant.ERROR);
                        System.out.println("=====cate res " + response);
                        if (error.equalsIgnoreCase("false")) {
                            empty_msg.setVisibility(View.GONE);
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Category category = new Category();
                                JSONObject object = jsonArray.getJSONObject(i);
                                category.setId(object.getString(Constant.ID));
                                category.setName(object.getString(Constant.CATEGORY_NAME));
                                category.setImage(object.getString(Constant.IMAGE));
                                category.setMaxLevel(object.getString(Constant.MAX_LEVEL));
                                category.setTtlQues(object.getString(Constant.NO_OF_QUES));
                                category.setNoOfCate(object.getString(Constant.NO_OF_CATE));
                                if (i != 0 && i % 5 == 0) {
                                    categoryList.add(new Category(true));
                                }
                                categoryList.add(category);
                            }
                            GridLayoutManager mLayoutManager = new GridLayoutManager(activity, 2);
                            mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                @Override
                                public int getSpanSize(int position) {
                                    if (position % 3 == 0) {
                                        return 2;
                                    } else {
                                        return 1;
                                    }
                                }
                            });
                            recyclerView.setLayoutManager(mLayoutManager);
                            adapter = new CategoryAdapter(activity, categoryList);
                            recyclerView.setAdapter(adapter);
                            mShimmerViewContainer.stopShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);

                        } else {

                            empty_msg.setText(getString(R.string.no_category));
                            progressBar.setVisibility(View.GONE);
                            empty_msg.setVisibility(View.VISIBLE);
                            mShimmerViewContainer.stopShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.GONE);

                            if (adapter != null) {
                                adapter = new CategoryAdapter(activity, categoryList);
                                recyclerView.setAdapter(adapter);
                            }
                        }
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
        mShimmerViewContainer.startShimmerAnimation();
        Utils.CheckBgMusic(activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppController.StopSound();
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        Context context = activity;
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);

        animation.setDuration(position * 50 + 500);
        viewToAnimate.startAnimation(animation);

    }


    public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        private ArrayList<Category> dataList;
        public Context mContext;
        private final int MENU_ITEM_VIEW_TYPE = 0;
        private final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

        public CategoryAdapter(Context context, ArrayList<Category> dataList) {
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
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_category, parent, false);
                    return new ItemRowHolder(v);
            }

        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder1, final int position) {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case UNIFIED_NATIVE_AD_VIEW_TYPE:
                    AdUtils.loadNativeAd(activity, holder1);
                    break;
                case MENU_ITEM_VIEW_TYPE:
                    final ItemRowHolder holder = (ItemRowHolder) holder1;
                    final Category category = dataList.get(position);
                    holder.nameLyt.setBackgroundResource(Constant.colorBg[position % 6]);
                    holder.imgCircles.setColorFilter(ContextCompat.getColor(activity, Constant.colors[position % 6]));
                    holder.text.setText(category.getName());
                    holder.noofque.setText(getString(R.string.que) + category.getTtlQues());
                    holder.image.setDefaultImageResId(R.drawable.ic_launcher);
                    holder.image.setImageUrl(category.getImage(), imageLoader);
                    setAnimation(holder.text, position);
                    holder.relativeLayout.setOnClickListener(v -> {
                        Constant.CATE_ID = category.getId();
                        Constant.cate_name = category.getName();
                        if (!category.getTtlQues().equals("0")) {
                            if (quizType.equals(Constant.REGULAR)) {
                                if (!category.getNoOfCate().equals("0")) {
                                    Intent intent = null;
                                    intent = new Intent(activity, SubcategoryActivity.class);
                                    startActivity(intent);

                                } else {

                                    if (category.getMaxLevel() == null) {
                                        Constant.TotalLevel = 0;
                                    } else if (category.getMaxLevel().equals("null")) {
                                        Constant.TotalLevel = 0;
                                    } else {
                                        Constant.TotalLevel = Integer.parseInt(category.getMaxLevel());
                                    }
                                    Intent intent = new Intent(activity, LevelActivity.class);
                                    intent.putExtra("fromQue", "cate");
                                    startActivity(intent);
                                }
                            } else {
                                Intent intent = null;
                                if (quizType.equals(Constant.PRACTICE)) {
                                    intent = new Intent(activity, PracticeQuiz.class);
                                    startActivity(intent);
                                } else if (quizType.equals(Constant.MULTIPLAYER_ROOM)) {
                                    createGameRoom(activity);

                                } else if (quizType.equals(Constant.BATTLE)) {
                                    intent = new Intent(activity, SearchPlayerActivity.class);
                                    startActivity(intent);

                                }
                            }
                        } else {
                            Toast.makeText(activity, getString(R.string.question_not_available), Toast.LENGTH_SHORT).show();
                        }

                    });
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (categoryList.get(position).isAdsShow()) {
                return UNIFIED_NATIVE_AD_VIEW_TYPE;
            }
            return MENU_ITEM_VIEW_TYPE;
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            public NetworkImageView image;
            public TextView text, noofque;
            RelativeLayout relativeLayout, nameLyt;
            ImageView imgCircles;

            public ItemRowHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.cateImg);
                imgCircles = itemView.findViewById(R.id.imgCircles);
                text = itemView.findViewById(R.id.item_title);
                relativeLayout = itemView.findViewById(R.id.cat_layout);
                nameLyt = itemView.findViewById(R.id.nameLyt);
                noofque = itemView.findViewById(R.id.noofque);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cate_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.setting) {
            Utils.CheckVibrateOrSound(activity);
            Intent playQuiz = new Intent(activity, SettingActivity.class);
            startActivity(playQuiz);
            overridePendingTransition(R.anim.open_next, R.anim.close_next);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    public void createGameRoom(Activity activity) {

        String gameCode = Constant.randomAlphaNumeric(5);
        showProgressDialog();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String authId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference dbRef = firebaseDatabase.getReference(Constant.MULTIPLAYER_ROOM).child(gameCode);
        HashMap<String, String> map = new HashMap<>();
        map.put(Constant.isRoomActive, Constant.TRUE);
        map.put(Constant.authId, authId);
        map.put(Constant.isStarted, Constant.FALSE);

        dbRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Do what you need to do
                Toast.makeText(activity, getString(R.string.room_created_msg), Toast.LENGTH_SHORT).show();
                //room userDetail
                HashMap<String, String> map = new HashMap<>();
                map.put(Constant.IMAGE, Session.getUserData(Session.PROFILE, activity));
                map.put(Constant.NAME, Session.getUserData(Session.NAME, activity));
                map.put(Constant.USER_ID, Session.getUserData(Session.USER_ID, activity));
                dbRef.child(Constant.roomUser).setValue(map);
                //join user detail
                HashMap<String, String> joinMap = new HashMap<>();
                joinMap.put(Constant.UID, authId);
                joinMap.put(Constant.IMAGE, Session.getUserData(Session.PROFILE, activity));
                joinMap.put(Constant.NAME, Session.getUserData(Session.NAME, activity));
                joinMap.put(Constant.IS_JOINED, "true");
                joinMap.put(Constant.USER_ID, Session.getUserData(Session.USER_ID, activity));
                dbRef.child(Constant.joinUser).child(authId).setValue(joinMap);
                sendRoomDataOnServer(gameCode);
            }
        });


    }

    public void sendRoomDataOnServer(final String roomId) {
        Map<String, String> params = new HashMap<>();
        params.put("create_room", "1");
        params.put(Constant.userId, Session.getUserData(Session.USER_ID, activity));
        params.put("room_id", roomId);
        params.put("room_type", "private");
        if (Constant.isGroupCateEnable)
            params.put("category", Constant.CATE_ID);
        params.put("no_of_que", "10");
        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext())) {
            params.put("language_id", Session.getUserData(Session.LANGUAGE, activity));
        }
        System.out.println("====== create room " + params.toString());
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {

            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        hideProgressDialog();

                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean(Constant.ERROR);
                        if (!error) {
                            Intent intent = new Intent(activity, WaitingRoomActivity.class);
                            intent.putExtra("from", "private");
                            intent.putExtra("roomKey", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            intent.putExtra("roomId", roomId);
                            intent.putExtra("type", "regular");
                            startActivity(intent);
                        } else
                            Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}