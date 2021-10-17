package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import com.google.android.gms.ads.AdListener;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.ApiConfig;

import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.AudienceProgress;
import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.UserSessionManager;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CompleteActivityTournaments extends AppCompatActivity {

    public Toolbar toolbar;
    RelativeLayout intertial;

    public TextView txt_result_title,  tvLevelScore, tvCorrect, tvInCorrect, tvLevelCoins,  tvPlayNext;
    public ScrollView scrollView;
    public AudienceProgress result_prog;
    int levelNo = 1;
    boolean isLevelCompleted;
    ProgressDialog mProgressDialog;
    public RelativeLayout mainLayout;
    public String fromQue;
    UserSessionManager session;
    public String contestid;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    public Context context;
    ProgressBar progressbar;
    NetworkImageView banner;
    public String url;
    RelativeLayout lyt_rank3, lyt_rank2, lyt_rank1;
    LinearLayout lyttop;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tournament_complete);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mainLayout = findViewById(R.id.mainLayout);

        fromQue = getIntent().getStringExtra("fromQue");
        context = CompleteActivityTournaments.this;
        session = new UserSessionManager(CompleteActivityTournaments.this);
        Utils.loadAd(CompleteActivityTournaments.this);
        contestid = getIntent().getStringExtra("qid");
        result_prog = findViewById(R.id.result_progress);
        result_prog.SetAttributes1();
        scrollView = findViewById(R.id.scrollView);
        txt_result_title = findViewById(R.id.txt_result_title);
        tvCorrect = findViewById(R.id.right);
        tvInCorrect = findViewById(R.id.wrong);

        lyttop = findViewById(R.id.lyttop);
        lyt_rank1 = findViewById(R.id.lyt_rank1);
        lyt_rank2 = findViewById(R.id.lyt_rank2);
        lyt_rank3 = findViewById(R.id.lyt_rank3);
        tvLevelScore = findViewById(R.id.tvScore);
        tvLevelScore.setText(String.valueOf(Utils.TournmentScore));
        tvLevelCoins = findViewById(R.id.tvCoin);
        banner = findViewById(R.id.banner_AdView);
        tvCorrect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        tvInCorrect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_close_24, 0, 0, 0);
        tvLevelScore.setCompoundDrawablesWithIntrinsicBounds(R.drawable.score_white, 0, 0, 0);
        tvLevelCoins.setCompoundDrawablesWithIntrinsicBounds(R.drawable.coins_wjite, 0, 0, 0);
        tvPlayNext = findViewById(R.id.tvPlayNext);
        progressbar = findViewById(R.id.progressbar);
        isLevelCompleted = Session.isLevelCompleted(CompleteActivityTournaments.this);

        txt_result_title.setText(getString(R.string.contest_complete));
        tvPlayNext.setText(getString(R.string.contest_complete));

        result_prog.setCurrentProgress((double) getPercentageCorrect(Utils.TotalTournment, Utils.CoreectQuetion));
        tvCorrect.setText(String.valueOf(Utils.CoreectQuetion));
        tvInCorrect.setText(String.valueOf(Utils.WrongQuation));

        if (Session.isLogin(CompleteActivityTournaments.this)) {
            GetUserData();
            prepareData(contestid);
        }
    }


    private void prepareData(String contestid) {

        showProgressDialog();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GETLEADERBOARD, Constant.GETDATAKEY);
        params.put(Constant.CONTEST_ID, contestid);
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        if (jsonObject1.getString("error").equalsIgnoreCase("false")) {

                            JSONArray jsonArray = jsonObject1.getJSONArray("data");


                            lyttop.setVisibility(View.VISIBLE);

                            for (int i = 1; i <= 3; i++) {
                                RelativeLayout lyt = (RelativeLayout) findViewById(getResources().getIdentifier("lyt_rank" + i, "id", getPackageName()));
                                lyt.setVisibility(View.INVISIBLE);
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                hideProgressDialog();

                                int rankno = i + 1;
                                if (rankno <= 3) {
                                    RelativeLayout lyt = (RelativeLayout) findViewById(getResources().getIdentifier("lyt_rank" + rankno, "id", getPackageName()));
                                    lyt.setVisibility(View.VISIBLE);
                                    TextView txtname = (TextView) findViewById(getResources().getIdentifier("txtname" + rankno, "id", getPackageName()));
                                    TextView txtscore = (TextView) findViewById(getResources().getIdentifier("txtscore" + rankno, "id", getPackageName()));
                                    CircleImageView imgprofile = (CircleImageView) findViewById(getResources().getIdentifier("img" + rankno, "id", getPackageName()));

                                    txtname.setText(jsonObject.getString(Constant.name));
                                    txtscore.setText(jsonObject.getString(Constant.SCORE));
                                    imgprofile.setImageUrl(jsonObject.getString(Constant.PROFILE), imageLoader);
                                }
                            }
                        } else {
                            hideProgressDialog();
                            lyttop.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);

    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(CompleteActivityTournaments.this);
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


    public static float getPercentageCorrect(int questions, int correct) {
        float proportionCorrect = ((float) correct) / ((float) questions);
        return proportionCorrect * 100;
    }

    public void PlayAgain(View view) {
        if (isLevelCompleted) {
            if (Constant.TotalLevel == Utils.RequestlevelNo) {
                Utils.RequestlevelNo = 1;
            } else {
                Utils.RequestlevelNo = Utils.RequestlevelNo + 1;
            }
        }
        Intent intent = new Intent(CompleteActivityTournaments.this, PlayActivity.class);
        intent.putExtra("fromQue", fromQue);
        startActivity(intent);
        ((CompleteActivity) context).finish();
    }

    public void ReviewAnswers(View view) {
        Intent intentReview = new Intent(CompleteActivityTournaments.this, ReviewActivity.class);
        startActivity(intentReview);
    }

    public void ShareScore(View view) {
        String shareMsg = "I have finished Level No : " + Utils.RequestlevelNo + " with " + Utils.level_score + " Score in " + getString(R.string.app_name);
        Utils.ShareInfo(scrollView, CompleteActivityTournaments.this, shareMsg);
    }

    public void RateApp(View view) {
        Utils.displayInterstitial(CompleteActivityTournaments.this);
        rateClicked();
    }

    public void Home(View view) {
        Intent intent1 = new Intent(CompleteActivityTournaments.this, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("type", "default");
        startActivity(intent1);
    }

    private void rateClicked() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.APP_LINK)));
        }
    }

    public void GetUserData() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_BY_ID, "1");
        params.put(Constant.ID, Session.getUserData(Session.USER_ID, getApplicationContext()));
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                System.out.println("========search result " + response);
                if (result) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean error = obj.getBoolean("error");
                        if (!error) {
                            JSONObject jsonobj = obj.getJSONObject("data");
                              /*  Constant.TOTAL_COINS = Integer.parseInt(jsonobj.getString(Constant.COINS));
                                tvTotalCoins.setText("" + Constant.TOTAL_COINS);*/
                            tvLevelCoins.setText(jsonobj.getString(Constant.COINS));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.loadAd(CompleteActivityTournaments.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}