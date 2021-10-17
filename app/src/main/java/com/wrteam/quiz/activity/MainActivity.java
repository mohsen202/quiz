package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.Button;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wrteam.quiz.R;
import com.wrteam.quiz.battle.WaitingRoomActivity;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.facebook.login.LoginManager;


import com.google.firebase.auth.FirebaseAuth;
import com.wrteam.quiz.model.Category;
import com.wrteam.quiz.model.Language;
import com.wrteam.quiz.model.Question;
import com.wrteam.quiz.model.Item;
import com.wrteam.quiz.model.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends DrawerActivity implements View.OnClickListener {

    SharedPreferences settings;
    public String type;
    public View divider;
    public RelativeLayout lytBattle;
    public RelativeLayout lytPlay, lytCategory;
    ImageView backmain;
    public LinearLayout bottomLyt;
    public LinearLayout lytMidScreen;
    public String status = "0";
    public TextView tvAlert, tvBattle, tvPlay, tvSelfChallenge, tvDailyquiz, tvViewall;
    public Toolbar toolbar;
    RelativeLayout rlHome, languagelyt, lytContest, leadboardlyt, storelyt;
    AlertDialog alertDialog;
    ImageView imgLogout, imgQuiz;
    public RecyclerView recyclerView, PlayMode;
    public static TextView tvName, tvscore, coinstxt, txtrank;
    CategoryAdapter adapter;
    public ArrayList<Category> categoryList;
    ArrayList<Item> arrayList;
    String[] iconsName;
    Activity activity;
    ProgressDialog mProgressDialog;
    String authId;

    @SuppressLint({"NewApi", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getLayoutInflater().inflate(R.layout.activity_main, frameLayout);
        activity = MainActivity.this;
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        authId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        divider = findViewById(R.id.divider);
        rlHome = findViewById(R.id.rlHome);
        drawerToggle = new ActionBarDrawerToggle
                (
                        this,
                        drawerLayout, toolbar,
                        R.string.drawer_open,
                        R.string.drawer_close
                ) {
        };


        tvViewall = findViewById(R.id.viewallTxt);
        tvViewall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryPage(Constant.REGULAR);
            }
        });
        tvName = findViewById(R.id.tvName);
        tvName.setText(Session.getUserData(Session.NAME, getApplicationContext()));
        imgProfile = findViewById(R.id.imgProfile);
        imgProfile.setImageUrl(Session.getUserData(Session.PROFILE, getApplicationContext()), imageLoader);
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);

            }
        });
        leadboardlyt = findViewById(R.id.leadboardlyt);

        leadboardlyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent leaderBoard = new Intent(MainActivity.this, LeaderboardTabActivity.class);
                startActivity(leaderBoard);
            }
        });
        lytCategory = findViewById(R.id.lytCategory);
        tvscore = findViewById(R.id.tvscore);
        coinstxt = findViewById(R.id.coinstxt);
        txtrank = findViewById(R.id.txtrank);
        backmain = findViewById(R.id.backmain);
        languagelyt = findViewById(R.id.languagelyt);
        storelyt = findViewById(R.id.storelyt);


        try{
            if (Session.getBoolean(Session.STORE, getApplicationContext())) {
                storelyt.setVisibility(View.VISIBLE);
            } else {
                storelyt.setVisibility(View.GONE);
            }
        }catch (Exception e){

        }

        storelyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent instruction = new Intent(getApplicationContext(), CoinStoreActivity.class);
                startActivity(instruction);
            }
        });

        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext())) {
            LanguageDialog(activity);

            if (!Session.getBoolean(Session.IS_FIRST_TIME, getApplicationContext())) {
                if (alertDialog != null)
                    alertDialog.show();
            } else {
                getMainCategoryFromJson();
            }
            languagelyt.setVisibility(View.VISIBLE);
        } else {
            getMainCategoryFromJson();
            languagelyt.setVisibility(View.GONE);
        }

        languagelyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null)
                    alertDialog.show();
            }
        });
        backmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        lytPlay = findViewById(R.id.lytPlay);
        lytContest = findViewById(R.id.lytContest);
        lytMidScreen = findViewById(R.id.midScreen);
        bottomLyt = findViewById(R.id.bottomLayout);
        tvAlert = findViewById(R.id.tvAlert);
        imgLogout = findViewById(R.id.imgLogout);
        imgQuiz = findViewById(R.id.imgQuiz);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        PlayMode = findViewById(R.id.PlayMode);
        PlayMode.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2, RecyclerView.HORIZONTAL, false));


        tvPlay = findViewById(R.id.tvPlay);

        settings = getSharedPreferences(Session.SETTING_Quiz_PREF, 0);
        type = getIntent().getStringExtra("type");

        assert type != null;
        if (!type.equals("null")) {
            if (type.equals("category")) {
                Constant.TotalLevel = Integer.parseInt(getIntent().getStringExtra("maxLevel"));
                Constant.CATE_ID = getIntent().getStringExtra("cateId");
                if (getIntent().getStringExtra("no_of").equals("0")) {
                    Intent intent = new Intent(activity, LevelActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(activity, SubcategoryActivity.class);
                    startActivity(intent);
                }
            }
        }

        navigationView.getMenu().getItem(3).setActionView(R.layout.cart_count_layout);
        NavigationCartCount();

        if (Utils.isNetworkAvailable(activity)) {

            if (Session.isLogin(getApplicationContext())) {
                imgLogout.setImageResource(R.drawable.logout);
                GetUserStatus();
                GetMark();
                GetUpadate(activity);
            } else {
                imgLogout.setImageResource(R.drawable.login);
            }
        }

        DeleteOldGameRoom();
    }

    public void setDefaultQuiz() {
        iconsName = new String[]{getString(R.string.daily_quiz), getString(R.string.random_quiz), getString(R.string.true_false), getString(R.string.self_challenge), getString(R.string.practice)};
        arrayList = new ArrayList<>();
        for (String s : iconsName) {
            Item itemModel = new Item();
            itemModel.setName(s);
            if (s.equals(getString(R.string.daily_quiz))) {
                if (Session.getBoolean(Session.GETDAILY, activity)) {
                    arrayList.add(itemModel);
                }
            } else {
                arrayList.add(itemModel);
            }


        }
        CustomAdapter adapter = new CustomAdapter(getApplicationContext(), arrayList);
        PlayMode.setAdapter(adapter);
    }

    public void getMainCategoryFromJson() {
        // progressBar.setVisibility(View.VISIBLE);

        lytCategory.setVisibility(View.GONE);
        Map<String, String> params = new HashMap<>();
        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext())) {
            params.put(Constant.GET_CATE_BY_LANG, "1");
            params.put(Constant.LANGUAGE_ID, Session.getCurrentLanguage(getApplicationContext()));
        } else
            params.put(Constant.getCategories, "1");
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        categoryList = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response);
                        String error = jsonObject.getString(Constant.ERROR);
                        //   System.out.println("=====cate res " + response);
                        if (error.equalsIgnoreCase("false")) {

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
                                categoryList.add(category);
                            }
                            adapter = new CategoryAdapter(activity, categoryList);
                            recyclerView.setAdapter(adapter);
                            lytCategory.setVisibility(View.VISIBLE);


                        } else {
                            lytCategory.setVisibility(View.GONE);
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


    public void ContestQuiz(View view) {
        Intent intent = new Intent(activity, ContestActivity.class);
        startActivity(intent);
    }

    public void RandomBattle(View view) {
        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext())) {
            if (Session.getCurrentLanguage(getApplicationContext()).equals(Constant.D_LANG_ID)) {
                if (alertDialog != null)
                    alertDialog.show();
            } else {

                searchPlayerCall();
            }
        } else {
            searchPlayerCall();
        }

    }

    public void searchPlayerCall() {
        if (Constant.isCateEnable)
            openCategoryPage(Constant.BATTLE);
        else
            startActivity(new Intent(activity, SearchPlayerActivity.class));
    }

    public void GroupBattle(View view) {
        JoinCreateDialog();
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

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.viewHolder> {

        Context context;
        ArrayList<Item> arrayList;

        public CustomAdapter(Context context, ArrayList<Item> arrayList) {
            this.context = context;
            this.arrayList = arrayList;
        }

        @Override
        public viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_playquiz, viewGroup, false);
            return new viewHolder(view);
        }

        @Override
        public void onBindViewHolder(viewHolder viewHolder, int position) {
            Item item = arrayList.get(position);
            viewHolder.iconName.setText(item.getName());
            if (position % 6 == 0) {
                viewHolder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_orange);
            } else if (position % 6 == 1) {
                viewHolder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_green);
            } else if (position % 6 == 2) {
                viewHolder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_sky);
            } else if (position % 6 == 3) {
                viewHolder.cardTitle.setBackgroundResource(R.drawable.gradient_category);
            } else if (position % 6 == 4) {
                viewHolder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_blue);
            } else if (position % 6 == 5) {
                viewHolder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_pink);
            }

            viewHolder.cardTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Session.getBoolean(Session.LANG_MODE, getApplicationContext())) {
                        if (Session.getCurrentLanguage(getApplicationContext()).equals(Constant.D_LANG_ID)) {
                            if (alertDialog != null)
                                alertDialog.show();
                        } else {
                            setQuiz(item);
                        }
                    } else {
                        setQuiz(item);
                    }
                }
            });
        }

        public void setQuiz(Item item) {
            if (item.getName().equalsIgnoreCase(getString(R.string.daily_quiz))) {
                DailyRandomQuiz("daily");
            } else if (item.getName().equalsIgnoreCase(getString(R.string.random_quiz))) {
                DailyRandomQuiz("random");
            } else if (item.getName().equalsIgnoreCase(getString(R.string.self_challenge))) {
                startActivity(new Intent(activity, NewSelfChallengeActivity.class));
            } else if (item.getName().equalsIgnoreCase(getString(R.string.true_false))) {
                DailyRandomQuiz("true_false");
            } else if (item.getName().equalsIgnoreCase(getString(R.string.practice))) {
                openCategoryPage(Constant.PRACTICE);
            }
        }


        public void DailyRandomQuiz(String quizType) {
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            intent.putExtra("fromQue", quizType);
            startActivity(intent);

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class viewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView iconName;
            RelativeLayout cardTitle;

            public viewHolder(View itemView) {
                super(itemView);

                iconName = itemView.findViewById(R.id.item_title);
                cardTitle = itemView.findViewById(R.id.cardTitle);

            }
        }

    }

    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ItemRowHolder> {
        private final ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        private final ArrayList<Category> dataList;
        public Context mContext;

        public CategoryAdapter(Context context, ArrayList<Category> dataList) {
            this.dataList = dataList;
            this.mContext = context;
        }

        @Override
        public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_maincat, parent, false);
            return new ItemRowHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemRowHolder holder, final int position) {

            final Category category = dataList.get(position);


            holder.text.setText(category.getName());

            if (position % 6 == 0) {
                holder.cardTitle.setBackgroundResource(R.drawable.gradient_category);
            } else if (position % 6 == 1) {
                holder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_sky);
            } else if (position % 6 == 2) {
                holder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_orange);
            } else if (position % 6 == 3) {
                holder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_blue);
            } else if (position % 6 == 4) {
                holder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_pink);
            } else if (position % 6 == 5) {
                holder.cardTitle.setBackgroundResource(R.drawable.gradient_cat_green);
            }

            holder.noofque.setText(  getString(R.string.que)+ category.getTtlQues());
/*            holder.image.setDefaultImageResId(R.drawable.ic_launcher);
            holder.image.setImageUrl(category.getImage(), imageLoader);*/
            holder.noofcat.setText(  getString(R.string.category)+ category.getNoOfCate());
            holder.relativeLayout
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Constant.CATE_ID = category.getId();
                            Constant.cate_name = category.getName();
                            if (!category.getTtlQues().equals("0")) {
                                if (!category.getNoOfCate().equals("0")) {
                                    Intent intent = new Intent(activity, SubcategoryActivity.class);
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
                                Toast.makeText(activity, getString(R.string.question_not_available), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            public NetworkImageView image;
            public TextView text, noofque, noofcat;
            RelativeLayout relativeLayout, cardTitle;

            public ItemRowHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.cateImg);
                text = itemView.findViewById(R.id.item_title);
                relativeLayout = itemView.findViewById(R.id.cat_layout);
                noofque = itemView.findViewById(R.id.noofque);
                noofcat = itemView.findViewById(R.id.noofcate);
                cardTitle = itemView.findViewById(R.id.cardTitle);
            }
        }
    }


    public void GetMark() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GetBookmark, "1");
        params.put(Constant.userId, Session.getUserData(Session.USER_ID, activity));
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                System.out.println("========search result " + response);
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean(Constant.ERROR);
                        if (!error) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Question question = new Question();
                                JSONObject object = jsonArray.getJSONObject(i);
                                question.setId(Integer.parseInt(object.getString(Constant.ID)));
                                Session.setMark(getApplicationContext(), "question_" + question.getId(), true);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);
    }

    public void GetUpadate(final Activity activity) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SYSTEM_CONFIG, "1");
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean error = obj.getBoolean("error");
                        if (!error) {
                            JSONObject jsonobj = obj.getJSONObject("data");
                            Constant.APP_LINK = jsonobj.getString(Constant.KEY_APP_LINK);
                            Constant.MORE_APP_URL = jsonobj.getString(Constant.KEY_MORE_APP);
                            Constant.VERSION_CODE = jsonobj.getString(Constant.KEY_APP_VERSION);
                            Constant.REQUIRED_VERSION = jsonobj.getString(Constant.KEY_APP_VERSION);
                            Constant.DAILYQUIZON = jsonobj.getString(Constant.DailyQuizText);
                            Constant.CONTESTON = jsonobj.getString(Constant.ContestText);
                            Constant.FORCEUPDATE = jsonobj.getString(Constant.ForceUpdateText);
                            if (jsonobj.has(Constant.RANDOM_BATTLE_CATE_MODE))
                                Constant.isCateEnable = jsonobj.getString(Constant.RANDOM_BATTLE_CATE_MODE).equals("1");
                            if (jsonobj.has(Constant.GROUP_BATTLE_CATE_MODE))
                                Constant.isGroupCateEnable = jsonobj.getString(Constant.GROUP_BATTLE_CATE_MODE).equals("1");
                            Session.setBoolean(Session.GETDAILY, Constant.DAILYQUIZON.equals("1"), activity);
                            Session.setBoolean(Session.GETCONTEST, Constant.CONTESTON.equals("1"), activity);
                            lytContest.setVisibility(Session.getBoolean(Session.GETCONTEST, activity) ? View.VISIBLE : View.GONE);

                            setDefaultQuiz();
                            String versionName = "";
                            try {
                                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                                versionName = packageInfo.versionName;
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                            if (Constant.FORCEUPDATE.equals("1")) {
                                if (compareVersion(versionName, Constant.VERSION_CODE) < 0) {
                                    OpenBottomDialog(activity);
                                } else if (compareVersion(versionName, Constant.REQUIRED_VERSION) < 0) {
                                    OpenBottomDialog(activity);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);
    }

    public static void OpenBottomDialog(final Activity activity) {
        View sheetView = activity.getLayoutInflater().inflate(R.layout.lyt_terms_privacy, null);
        ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
        if (parentViewGroup != null) {
            parentViewGroup.removeAllViews();
        }

        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity);
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
        // FrameLayout bottomSheet = (FrameLayout) mBottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);

        ImageView imgclose = sheetView.findViewById(R.id.imgclose);
        TextView txttitle = sheetView.findViewById(R.id.tvTitle);
        Button btnNotNow = sheetView.findViewById(R.id.btnNotNow);
        Button btnUpadateNow = sheetView.findViewById(R.id.btnUpdateNow);

        mBottomSheetDialog.setCancelable(false);


        imgclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheetDialog.isShowing())
                    mBottomSheetDialog.dismiss();
            }
        });
        btnNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheetDialog.isShowing())
                    mBottomSheetDialog.dismiss();
            }
        });

        btnUpadateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.APP_LINK)));
                System.out.println("Packge Name::=" + Constant.APP_LINK + activity.getPackageName());

            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public static int compareVersion(String version1, String version2) {
        String[] arr1 = version1.split("\\.");
        String[] arr2 = version2.split("\\.");

        int i = 0;
        while (i < arr1.length || i < arr2.length) {
            if (i < arr1.length && i < arr2.length) {
                if (Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i])) {
                    return -1;
                } else if (Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i])) {
                    return 1;
                }
            } else if (i < arr1.length) {
                if (Integer.parseInt(arr1[i]) != 0) {
                    return 1;
                }
            } else {
                if (Integer.parseInt(arr2[i]) != 0) {
                    return -1;
                }
            }

            i++;
        }

        return 0;
    }


    public void LanguageDialog(Activity activity) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater1 = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater1.inflate(R.layout.language_dialog, null);
        dialog.setView(dialogView);
        RecyclerView languageView = dialogView.findViewById(R.id.recyclerView);
        languageView.setLayoutManager(new LinearLayoutManager(activity));
        alertDialog = dialog.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        GetLanguage(languageView, activity, alertDialog);
    }

    public void GetUserStatus() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_BY_ID, "1");
        params.put(Constant.ID, Session.getUserData(Session.USER_ID, getApplicationContext()));
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {

                        JSONObject obj = new JSONObject(response);
                        boolean error = obj.getBoolean("error");
                        if (!error) {
                            JSONObject jsonobj = obj.getJSONObject("data");
                            if (jsonobj.getString(Constant.status).equals(Constant.DE_ACTIVE)) {
                                Session.clearUserSession(getApplicationContext());
                                FirebaseAuth.getInstance().signOut();
                                LoginManager.getInstance().logOut();
                                Intent intentLogin = new Intent(activity, LoginTabActivity.class);
                                startActivity(intentLogin);
                                finish();
                            } else {
                                Constant.TOTAL_COINS = Integer.parseInt(jsonobj.getString(Constant.COINS));
                                String numberString = "";
                                if (Math.abs(Integer.parseInt(String.valueOf(Constant.TOTAL_COINS )) / 1000000) > 1) {
                                    numberString = (Integer.parseInt(String.valueOf(Constant.TOTAL_COINS )) / 1000000) + "M";

                                } else if (Math.abs(Integer.parseInt(String.valueOf(Constant.TOTAL_COINS )) / 1000) > 1) {
                                    numberString = (Integer.parseInt(String.valueOf(Constant.TOTAL_COINS )) / 1000) + "K";

                                } else {
                                    numberString =String.valueOf(Constant.TOTAL_COINS );

                                }
                                coinstxt.setText(numberString);
                                txtrank.setText("" + jsonobj.getString(Constant.GLOBAL_RANK));
                                tvscore.setText(jsonobj.getString(Constant.GLOBAL_SCORE));
                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<String> task) {
                                        String token = task.getResult();
                                        if (!token.equals(Session.getUserData(Session.FCM, getApplicationContext()))) {
                                            Utils.postTokenToServer(getApplicationContext(), token);
                                        }
                                    }
                                });
                                // Utils.RemoveGameRoomId(FirebaseAuth.getInstance().getUid());
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
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.lytPlay:

                Utils.btnClick(view, activity);
                startGame();

        }
    }


    public void UpdateProfile() {
        startActivity(new Intent(activity, ProfileActivity.class));
    }


    //send registration token to server


    public void LeaderBoard(View view) {
        Utils.btnClick(view, activity);

        startActivity(new Intent(activity, LeaderboardTabActivity.class));

    }

    public void Logout(View view) {
        Utils.btnClick(view, activity);

        Utils.SignOutWarningDialog(activity);

    }

    public void StartQuiz(View view) {
        imgQuiz.setVisibility(View.GONE);


        startGame();

    }

    private void startGame() {

        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext())) {
            if (Session.getCurrentLanguage(getApplicationContext()).equals(Constant.D_LANG_ID)) {
                if (alertDialog != null)
                    alertDialog.show();
            } else {
                startActivity(new Intent(activity, CategoryActivity.class));
            }
        } else {
            startActivity(new Intent(activity, CategoryActivity.class));
        }

    }

    public void UserProfile(View view) {
        Utils.btnClick(view, activity);
        UpdateProfile();
    }

    public void Settings(View view) {
        Utils.btnClick(view, activity);
        Intent playQuiz = new Intent(activity, SettingActivity.class);
        startActivity(playQuiz);
        overridePendingTransition(R.anim.open_next, R.anim.close_next);
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
        params.put(Constant.ROOM_ID, roomId);
        params.put(Constant.roomType, "private");
        params.put(Constant.NO_OF_QUES, "10");
        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext()))
            params.put(Constant.LANGUAGE_ID, Session.getUserData(Session.LANGUAGE, activity));
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

    public void NavigationCartCount() {

        View viewCount = navigationView.getMenu().getItem(3).setActionView(R.layout.cart_count_layout).getActionView();
        TextView tvCount = viewCount.findViewById(R.id.tvCount);
        if (Session.getNCount(getApplicationContext()) == 0) {
            tvCount.setVisibility(View.GONE);
        } else {
            tvCount.setVisibility(View.VISIBLE);
        }
        tvCount.setText(String.valueOf(Session.getNCount(getApplicationContext())));
    }

    public void JoinCreateDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_join_create_battle, null);
        dialog.setView(dialogView);

        TextView tvCreate = dialogView.findViewById(R.id.tvCreate);
        TextView tvJoin = dialogView.findViewById(R.id.tvJoin);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.show();

        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if (Constant.isGroupCateEnable) {
                    openCategoryPage(Constant.MULTIPLAYER_ROOM);
                } else
                    createGameRoom(activity);
            }
        });
        tvJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                JoinRoom();
            }
        });

    }

    public void GetLanguage(final RecyclerView languageView, final Context context, final AlertDialog alertDialog) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_LANGUAGES, "1");
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean(Constant.ERROR);
                        if (!error) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            ArrayList<Language> languageList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Language language = new Language();
                                JSONObject object = jsonArray.getJSONObject(i);
                                language.setId(object.getString(Constant.ID));
                                language.setLanguage(object.getString(Constant.LANGUAGE));
                                languageList.add(language);
                            }
                            if (languageList.size() == 1) {
                                Session.setCurrentLanguage(languageList.get(0).getId(), context);
                                Session.setBoolean(Session.IS_FIRST_TIME, true, context);
                            }

                            LanguageAdapter languageAdapter = new LanguageAdapter(context, languageList, alertDialog);
                            languageView.setAdapter(languageAdapter);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);


    }

    public class LanguageAdapter extends RecyclerView.Adapter<MainActivity.LanguageAdapter.ItemRowHolder> {
        private final ArrayList<Language> dataList;
        private final Context mContext;
        AlertDialog alertDialog;

        public LanguageAdapter(Context context, ArrayList<Language> dataList, AlertDialog alertDialog) {
            this.dataList = dataList;
            this.mContext = context;
            this.alertDialog = alertDialog;
        }

        @Override
        public MainActivity.LanguageAdapter.ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_layout, parent, false);
            return new MainActivity.LanguageAdapter.ItemRowHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MainActivity.LanguageAdapter.ItemRowHolder holder, final int position) {

            final Language language = dataList.get(position);
            final MainActivity.LanguageAdapter.ItemRowHolder itemRowHolder = holder;
            itemRowHolder.tvLanguage.setText(language.getLanguage());
            if (Session.getCurrentLanguage(mContext).equals(language.getId())) {
                itemRowHolder.radio.setImageResource(R.drawable.ic_radio_check);
            } else {
                itemRowHolder.radio.setImageResource(R.drawable.ic_radio_unchecked);
            }
            itemRowHolder.radio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemRowHolder.radio.setImageResource(R.drawable.ic_radio_check);
                    Session.setCurrentLanguage(language.getId(), mContext);
                    Session.setBoolean(Session.IS_FIRST_TIME, true, mContext);
                    notifyDataSetChanged();
                    getMainCategoryFromJson();

                    alertDialog.dismiss();

                }
            });

        }

        @Override
        public int getItemCount() {
            return (dataList.size());
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            public ImageView radio;
            public TextView tvLanguage;


            public ItemRowHolder(View itemView) {
                super(itemView);
                radio = itemView.findViewById(R.id.radio);
                tvLanguage = itemView.findViewById(R.id.tvLanguage);
            }

        }

    }


    public void JoinRoom() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
        View bottomView = getLayoutInflater().inflate(R.layout.join_room_dialog, null);
        bottomSheetDialog.setContentView(bottomView);
        ImageView imgClose = bottomView.findViewById(R.id.imgClose);
        TextView tvJoinRoom = bottomView.findViewById(R.id.tvJoinRoom);
        TextView tvAlert = bottomView.findViewById(R.id.tvAlert);
        EditText edtGameCode = bottomView.findViewById(R.id.edtGameCode);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        tvJoinRoom.setOnClickListener(v -> {
            String code = edtGameCode.getText().toString().trim();
            if (code.isEmpty()) {
                tvAlert.setText(getString(R.string.enter_code));
            } else if (code.length() != 5) {
                Toast.makeText(activity, getString(R.string.game_code_alert), Toast.LENGTH_SHORT).show();
            } else {
                DatabaseReference dbRef;
                dbRef = rootRef.child(Constant.MULTIPLAYER_ROOM).child(code);
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // run some code
                            Room room = snapshot.getValue(Room.class);
                            assert room != null;
                            if (room.getIsRoomActive().equals(Constant.TRUE) && room.getIsStarted().equals(Constant.FALSE)) {
                                dbRef.child(Constant.joinUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // get total available quest

                                        int size = (int) dataSnapshot.getChildrenCount();
                                        if (size < Constant.JoinMember) {
                                            Intent intent = new Intent(activity, WaitingRoomActivity.class);
                                            intent.putExtra("from", "private");
                                            intent.putExtra("roomKey", room.getAuthId());
                                            intent.putExtra("roomId", code);
                                            intent.putExtra("type", "invite");
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(activity, getString(R.string.gameroom_full), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                Toast.makeText(activity, getString(R.string.game_deactive_alert), Toast.LENGTH_SHORT).show();
                            }
                            bottomSheetDialog.cancel();

                        } else {
                            Toast.makeText(activity, getString(R.string.gameroom_code_alert), Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        imgClose.setOnClickListener(view1 -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        assert bottomSheet != null;
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void DeleteOldGameRoom() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbRef;
        dbRef = rootRef.child(Constant.MULTIPLAYER_ROOM);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get total available quest
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        if (ds.getKey() != null)
                            if (dataSnapshot.child(ds.getKey()).child(Constant.authId).exists()) {
                                String ownerRoomIds = dataSnapshot.child(ds.getKey()).child(Constant.authId).getValue().toString();
                                if (ownerRoomIds.equalsIgnoreCase(authId)) {
                                    dbRef.child(ds.getKey()).removeValue();
                                }
                            }/*else{
                                dbRef.child(ds.getKey()).removeValue();
                            }*/
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void openCategoryPage(String type) {
        startActivity(new Intent(activity, CategoryActivity.class)
                .putExtra(Constant.QUIZ_TYPE, type));
    }

    @Override
    protected void onPause() {
        AppController.StopSound();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppController.playSound();
        DeleteOldGameRoom();
        if (Utils.isNetworkAvailable(activity)) {
            Utils.GetSystemConfig(getApplicationContext());
            GetUpadate(activity);
            GetUserStatus();
            invalidateOptionsMenu();
            if (Session.isLogin(activity)) {
                NavigationCartCount();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}