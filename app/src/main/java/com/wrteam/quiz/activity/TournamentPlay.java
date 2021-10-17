package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;

import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.AudienceProgress;
import com.wrteam.quiz.helper.CircleTimer;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.TouchImageView;
import com.wrteam.quiz.helper.UserSessionManager;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class TournamentPlay extends AppCompatActivity implements OnClickListener {

    public static AdRequest adRequest;

    public Question question;
    public int questionIndex = 0,
            btnPosition = 0,

    count_question_completed = 0,
            score = 0,
            level_coin = 6,
            correctQuestion = 0,
            inCorrectQuestion = 0,
            rightAns;
    public TextView tvScore, coin_count;
    public static CircleTimer progressTimer;
    public static TextView btnOpt1, btnOpt2, btnOpt3, btnOpt4, btnOpt5, txtQuestion, tvAlert, tvIndex;
    public CompleteActivity completeActivity;
    public static Activity activity;
    public ImageView fifty_fifty, skip_question, resetTimer, audience_poll;
    public RelativeLayout playLayout;

    public RelativeLayout layout_A, layout_B, layout_C, layout_D, layout_E;
    private Animation animation;
    private final Handler mHandler = new Handler();
    String qid, qtitle, rightanswer, entrypoint, type, from, rightString;
    public Animation RightSwipe_A, RightSwipe_B, RightSwipe_C, RightSwipe_D, RightSwipe_E, Fade_in, fifty_fifty_anim, slid_up;
    private AudienceProgress progressBarTwo_A, progressBarTwo_B, progressBarTwo_C, progressBarTwo_D, progressBarTwo_E;
    public static Timer timer;
    UserSessionManager session;
    public static InterstitialAd interstitial;
    public static ArrayList<String> options;
    public static long leftTime = 0;
    public boolean isDialogOpen = false;
    public static ArrayList<Question> questionList;
    int queattend = 0, correctans = 0, count = 0, queindex = 0, btnposition = 0, wrongans = 0, totalque = 0, point;

    TouchImageView imgQuestion;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    ProgressBar progressBar;
    ImageView imgZoom;
    int click = 0;
    int textSize;
    RelativeLayout mainLayout;
    public String fromQue;
    public ScrollView mainScroll, queScroll;
    public RewardedAd rewardedVideoAd;

    public Toolbar toolbar;
    AdView mAdView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_play);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mainLayout = findViewById(R.id.play_layout);
        qid = getIntent().getStringExtra("id");
        qtitle = getIntent().getStringExtra("title");
        activity = TournamentPlay.this;
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(qtitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        from = getIntent().getStringExtra("from");
        completeActivity = new CompleteActivity();
        session = new UserSessionManager(activity);
        final int[] CLICKABLE = new int[]{R.id.a_layout, R.id.b_layout, R.id.c_layout, R.id.d_layout, R.id.e_layout};

        for (int i : CLICKABLE) {
            findViewById(i).setOnClickListener(this);
        }

        type = from;

        if (from != null) {
            if (from.equals("contest")) {
                entrypoint = getIntent().getStringExtra("entrypoint");
                type = "Contest Quiz";
            } else if (from.equals("spin")) {
                type = "Spin Quiz";
            }
        }
        mAdView = findViewById(R.id.banner_AdView);
        adRequest = new AdRequest.Builder().build();
        textSize = Integer.parseInt(Session.getSavedTextSize(activity));
        Session.removeSharedPreferencesData(activity);
        RightSwipe_A = AnimationUtils.loadAnimation(activity, R.anim.anim_right_a);
        RightSwipe_B = AnimationUtils.loadAnimation(activity, R.anim.anim_right_b);
        RightSwipe_C = AnimationUtils.loadAnimation(activity, R.anim.anim_right_c);
        RightSwipe_D = AnimationUtils.loadAnimation(activity, R.anim.anim_right_d);
        RightSwipe_E = AnimationUtils.loadAnimation(activity, R.anim.anim_right_e);
        Fade_in = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
        fifty_fifty_anim = AnimationUtils.loadAnimation(activity, R.anim.fifty_fifty);

        resetAllValue();
        mainScroll.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                v.findViewById(R.id.queScroll).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        queScroll.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        progressTimer.setMaxProgress(Constant.CIRCULAR_MAX_PROGRESS);
        progressTimer.setCurrentProgress(Constant.CIRCULAR_MAX_PROGRESS);
        // rewardedVideoAd.setRewardedVideoAdListener(rewardedVideoAdListener);
        loadInterstitialAds();
        RewardsadsLoad();
    }

    public void loadInterstitialAds() {
        try {
            adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(this, getString(R.string.admob_interstitial_id), adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    TournamentPlay.this.interstitial = interstitialAd;

                    interstitialAd.setFullScreenContentCallback(
                            new FullScreenContentCallback() {
                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when fullscreen content is shown.
                                    //when ads show , we have to stop timer
                                    stopTimer();
                                }
                            });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    // Handle the error

                    interstitial = null;
                    if (!tvAlert.getText().equals(getString(R.string.no_enough_questiondaily)))
                        starTimer();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RewardsadsLoad() {
        adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.admob_Rewarded_Video_Ads),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        // Log.d(TAG, loadAdError.getMessage());
                        rewardedVideoAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        rewardedVideoAd = rewardedAd;
                        // Log.d(TAG, "onAdFailedToLoad");
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public static void ChangeTextSize(int size) {

        if (btnOpt1 != null)
            btnOpt1.setTextSize(size);
        if (btnOpt2 != null)
            btnOpt2.setTextSize(size);
        if (btnOpt3 != null)
            btnOpt3.setTextSize(size);
        if (btnOpt4 != null)
            btnOpt4.setTextSize(size);
        if (btnOpt5 != null)
            btnOpt5.setTextSize(size);
        if (txtQuestion != null)
            txtQuestion.setTextSize(size);

    }

    @SuppressLint("SetTextI18n")
    public void resetAllValue() {
        progressTimer = findViewById(R.id.circleTimer);

        playLayout = findViewById(R.id.innerLayout);
        tvIndex = findViewById(R.id.tvIndex);

        mainScroll = findViewById(R.id.mainScroll);
        queScroll = findViewById(R.id.queScroll);
        progressBar = findViewById(R.id.progressBar);
        coin_count = findViewById(R.id.coin_count);
        imgQuestion = findViewById(R.id.imgQuestion);

        tvAlert = findViewById(R.id.tvAlert);

        btnOpt1 = findViewById(R.id.btnOpt1);
        btnOpt2 = findViewById(R.id.btnOpt2);
        btnOpt3 = findViewById(R.id.btnOpt3);
        btnOpt4 = findViewById(R.id.btnOpt4);
        btnOpt5 = findViewById(R.id.btnOpt5);

        imgZoom = findViewById(R.id.imgZoom);
        fifty_fifty = findViewById(R.id.fifty_fifty);
        skip_question = findViewById(R.id.skip_quation);
        resetTimer = findViewById(R.id.reset_timer);
        audience_poll = findViewById(R.id.audience_poll);
        tvAlert.setText(getString(R.string.msg_no_internet));
        txtQuestion = findViewById(R.id.txtQuestion);
        layout_A = findViewById(R.id.a_layout);
        layout_B = findViewById(R.id.b_layout);
        layout_C = findViewById(R.id.c_layout);
        layout_D = findViewById(R.id.d_layout);
        layout_E = findViewById(R.id.e_layout);
        ChangeTextSize(textSize);
        progressBarTwo_A = findViewById(R.id.progress_A);
        progressBarTwo_B = findViewById(R.id.progress_B);
        progressBarTwo_C = findViewById(R.id.progress_C);
        progressBarTwo_D = findViewById(R.id.progress_D);
        progressBarTwo_E = findViewById(R.id.progress_E);
        progressBarTwo_A.SetAttributes();
        progressBarTwo_B.SetAttributes();
        progressBarTwo_C.SetAttributes();
        progressBarTwo_D.SetAttributes();
        progressBarTwo_E.SetAttributes();

        animation = AnimationUtils.loadAnimation(activity, R.anim.right_ans_anim); // Change alpha from fully visible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
        count_question_completed = Session.getCountQuestionCompleted(getApplicationContext());
        tvScore = findViewById(R.id.txtScore);
        tvScore.setText(String.valueOf(score));
        coin_count.setText(String.valueOf(Constant.TOTAL_COINS));


        if (Utils.isNetworkAvailable(activity)) {
            getQuestionsFromJson();

        } else {
            tvAlert.setText(getString(R.string.msg_no_internet));
            playLayout.setVisibility(View.GONE);


        }

    }


    private void nextQuizQuestion() {
        queScroll.scrollTo(0,0);
        queattend = queattend + 1;
        count = 0;
        stopTimer();
        starTimer();

        Constant.LeftTime = 0;
        leftTime = 0;
        setAgain();
        if (questionIndex >= questionList.size()) {
            levelCompleted();
        }
        invalidateOptionsMenu();

        layout_A.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        layout_B.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        layout_C.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        layout_D.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        layout_E.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        layout_A.clearAnimation();
        layout_B.clearAnimation();
        layout_C.clearAnimation();
        layout_D.clearAnimation();
        layout_E.clearAnimation();
        layout_A.setClickable(true);
        layout_B.setClickable(true);
        layout_C.setClickable(true);
        layout_D.setClickable(true);
        layout_E.setClickable(true);
        btnOpt1.startAnimation(RightSwipe_A);
        btnOpt2.startAnimation(RightSwipe_B);
        btnOpt3.startAnimation(RightSwipe_C);
        btnOpt4.startAnimation(RightSwipe_D);
        btnOpt5.startAnimation(RightSwipe_E);
        txtQuestion.startAnimation(Fade_in);

        if (questionIndex < questionList.size()) {
            mAdView.loadAd(adRequest);
            question = questionList.get(questionIndex);
            int temp = questionIndex;
            imgQuestion.resetZoom();
            tvIndex.setText(++temp + "/" + questionList.size());

            if (!question.getImage().isEmpty()) {
                imgZoom.setVisibility(View.VISIBLE);
                imgQuestion.setImageUrl(question.getImage(), imageLoader);
                imgQuestion.setVisibility(View.VISIBLE);
                imgZoom.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        click++;
                        if (click == 1)
                            imgQuestion.setZoom(1.25f);
                        else if (click == 2)
                            imgQuestion.setZoom(1.50f);
                        else if (click == 3)
                            imgQuestion.setZoom(1.75f);
                        else if (click == 4) {
                            imgQuestion.setZoom(2.00f);
                            click = 0;
                        }
                    }
                });
            } else {
                imgZoom.setVisibility(View.GONE);
                imgQuestion.setVisibility(View.GONE);
            }

            txtQuestion.setText(question.getQuestion());
            options = new ArrayList<String>();
            options.addAll(question.getOptions());

            if (question.getQueType().equals(Constant.TRUE_FALSE)) {
                layout_C.setVisibility(View.GONE);
                layout_D.setVisibility(View.GONE);
                btnOpt1.setPadding(0, 100, 0, 100);
                btnOpt2.setPadding(0, 100, 0, 100);
                btnOpt1.setGravity(Gravity.CENTER);
                btnOpt2.setGravity(Gravity.CENTER);

            } else {
                Collections.shuffle(options);
                layout_C.setVisibility(View.VISIBLE);
                layout_D.setVisibility(View.VISIBLE);
                btnOpt1.setPadding(0, 0, 0, 0);
                btnOpt2.setPadding(0, 0, 0, 0);
                btnOpt1.setGravity(Gravity.NO_GRAVITY);
                btnOpt2.setGravity(Gravity.NO_GRAVITY);
            }

            if (Session.getBoolean(Session.E_MODE, getApplicationContext())) {
                if (options.size() == 4)
                    layout_E.setVisibility(View.GONE);
                else
                    layout_E.setVisibility(View.VISIBLE);

            }
            btnOpt1.setText(Html.fromHtml(options.get(0).trim()));
            btnOpt2.setText(Html.fromHtml(options.get(1).trim()));
            btnOpt3.setText(Html.fromHtml(options.get(2).trim()));
            btnOpt4.setText(Html.fromHtml(options.get(3).trim()));
            if (Session.getBoolean(Session.E_MODE, getApplicationContext())) {
                if (options.size() == 5)
                    btnOpt5.setText(Html.fromHtml(options.get(4).trim()));
            }

        }
    }

    public void levelCompleted() {
        Utils.TotalQuestion = questionList.size();
        Utils.CoreectQuetion = correctQuestion;
        Utils.WrongQuation = inCorrectQuestion;

        stopTimer();
        Utils.TotalTournment = questionList.size();
        int total = questionList.size();
        int percent = (correctQuestion * 100) / total;

        if (percent >= 30 && percent < 40) {
            Constant.TOTAL_COINS = Constant.TOTAL_COINS + Constant.giveOneCoin;
            level_coin = Constant.giveOneCoin;

        } else if (percent >= 40 && percent < 60) {
            Constant.TOTAL_COINS = Constant.TOTAL_COINS + Constant.giveTwoCoins;
            level_coin = Constant.giveTwoCoins;

        } else if (percent >= 60 && percent < 80) {
            Constant.TOTAL_COINS = Constant.TOTAL_COINS + Constant.giveThreeCoins;
            level_coin = Constant.giveThreeCoins;

        } else if (percent >= 80) {
            Constant.TOTAL_COINS = Constant.TOTAL_COINS + Constant.giveFourCoins;
            level_coin = Constant.giveFourCoins;

        } else
            level_coin = 0;


        Utils.level_coin = level_coin;
        Utils.level_score = score;
        Utils.TournmentScore = score;

        coin_count.setText(String.valueOf(Constant.TOTAL_COINS));
        if (Session.isLogin(getApplicationContext())) {

            UpdateScore(String.valueOf(Utils.TournmentScore), String.valueOf(Utils.TotalTournment));

            AddPoint(String.valueOf(point), type, false);


        }

        if (percent >= Constant.PASSING_PER)
            Session.setLevelComplete(getApplicationContext(), true);
        else
            Session.setLevelComplete(getApplicationContext(), false);


        Intent intent = new Intent(activity, CompleteActivityTournaments.class);
        intent.putExtra("qid", qid);
        intent.putExtra("qtitle", qtitle);
        intent.putExtra("rans", correctQuestion);
        intent.putExtra("wans", inCorrectQuestion);
        intent.putExtra("score", score);
        intent.putExtra("totalque", Utils.TotalTournment);
        startActivity(intent);
        finish();
        blankAllValue();

    }


    public void UpdateScore(final String score, final String queattend) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.CONTEST_UPDATE_SCORE, Constant.GETDATAKEY);
        params.put(Constant.CONTEST_ID, qid);
        params.put(Constant.userId, Session.getUserData(Session.USER_ID, activity));
        params.put(Constant.QUESTION_ATTEND, queattend);
        params.put(Constant.CORRECT_ANSWERS, "" + Utils.CoreectQuetion);
        params.put(Constant.SCORE, "" + score);
        System.out.println("Value of PARAms::=" + params);
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {

                }
            }
        }, params);

    }


    private void killActivity() {
        finish();
    }


    public void AddReview(Question question, TextView tvBtnOpt, RelativeLayout layout) {
        layout_A.setClickable(false);
        layout_B.setClickable(false);
        layout_C.setClickable(false);
        layout_D.setClickable(false);
        layout_E.setClickable(false);

        if (tvBtnOpt.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            rightSound();

            layout.setBackgroundResource(R.drawable.right_gradient);
            layout.startAnimation(animation);
            correctQuestion = correctQuestion + 1;
            if (count >= 20) {
                point = point + Constant.score1;
                score = score + Constant.score1;
                System.out.println("20 plus::=" + score);
                System.out.println("20 plus::=" + point);
            } else if (count < 20 && count >= 10) {
                point = point + Constant.score2;
                score = score + Constant.score2;
                System.out.println("20 down::=" + score);
                System.out.println("20 down::=" + point);
            } else {

                point = point + Constant.score3;
                score = score + Constant.score3;
                System.out.println("10 down::=" + score);
                System.out.println("10 down::=" + point);
            }
            tvScore.setText(String.valueOf(score));
            System.out.println("Values of Score" + score);
        } else {

            playWrongSound();
            layout.setBackgroundResource(R.drawable.wrong_gradient);
            score = score - Constant.PENALTY;
            inCorrectQuestion = inCorrectQuestion + 1;
            System.out.println("Values of INcorrecation" + inCorrectQuestion);
            tvScore.setText(String.valueOf(score));

        }

        question.setSelectedAns(tvBtnOpt.getText().toString());
        if (Constant.QUICK_ANSWER_ENABLE.equals("1"))
            RightAnswerBackgroundSet();
        question.setAttended(true);
        stopTimer();

        questionIndex++;
        mHandler.postDelayed(mUpdateUITimerTask, 1000);

    }

    public void RightAnswerBackgroundSet() {
        if (btnOpt1.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_A.setBackgroundResource(R.drawable.right_gradient);
            layout_A.startAnimation(animation);

        } else if (btnOpt2.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_B.setBackgroundResource(R.drawable.right_gradient);
            layout_B.startAnimation(animation);

        } else if (btnOpt3.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_C.setBackgroundResource(R.drawable.right_gradient);
            layout_C.startAnimation(animation);

        } else if (btnOpt4.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_D.setBackgroundResource(R.drawable.right_gradient);
            layout_D.startAnimation(animation);

        } else if (btnOpt5.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_E.setBackgroundResource(R.drawable.right_gradient);
            layout_E.startAnimation(animation);
        }
    }

    @Override
    public void onClick(View v) {
        if (questionIndex < questionList.size()) {
            question = questionList.get(questionIndex);
            layout_A.setClickable(false);
            layout_B.setClickable(false);
            layout_C.setClickable(false);
            layout_D.setClickable(false);
            layout_E.setClickable(false);
            if (progressBarTwo_A.getVisibility() == (View.VISIBLE)) {
                progressBarTwo_A.setVisibility(View.GONE);
                progressBarTwo_B.setVisibility(View.GONE);
                progressBarTwo_C.setVisibility(View.GONE);
                progressBarTwo_D.setVisibility(View.GONE);
                progressBarTwo_E.setVisibility(View.GONE);
            }
            switch (v.getId()) {
                case R.id.a_layout:
                    AddReview(question, btnOpt1, layout_A);
                    break;

                case R.id.b_layout:
                    AddReview(question, btnOpt2, layout_B);

                    break;
                case R.id.c_layout:
                    AddReview(question, btnOpt3, layout_C);

                    break;
                case R.id.d_layout:
                    AddReview(question, btnOpt4, layout_D);

                    break;
                case R.id.e_layout:
                    AddReview(question, btnOpt5, layout_E);

                    break;
            }

        }
    }


    private final Runnable mUpdateUITimerTask = new Runnable() {
        public void run() {
            if (getApplicationContext() != null) {

                nextQuizQuestion();

            }
        }
    };


    public void PlayAreaLeaveDialog() {


        if (!tvAlert.getText().equals(getResources().getString(R.string.no_enough_question))) {
            stopTimer();
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            // Setting Dialog Message
            alertDialog.setMessage(getString(R.string.exit_msg));
            alertDialog.setCancelable(false);
            final AlertDialog alertDialog1 = alertDialog.create();
            // Setting OK Button
            alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to execute after dialog closed
                    stopTimer();
                    leftTime = 0;
                    Constant.LeftTime = 0;
                    UpdateScore(String.valueOf(score), String.valueOf(queattend));
                    AddPoint(String.valueOf(point), type, false);
                    killActivity();

                }
            });

            alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog1.dismiss();
                    Constant.LeftTime = leftTime;
                    if (Constant.LeftTime != 0) {

                        timer = new Timer(leftTime, 1000);
                        timer.start();


                    }
                }
            });
            // Showing Alert Message
            alertDialog.show();
        } else {
            killActivity();
        }
    }


    //play sound when answer is correct
    public void rightSound() {
        if (Session.getSoundEnableDisable(activity))
            Utils.setrightAnssound(activity);

        if (Session.getVibration(activity))
            Utils.vibrate(activity, Utils.VIBRATION_DURATION);

    }

    //play sound when answer is incorrect
    private void playWrongSound() {
        if (Session.getSoundEnableDisable(activity))
            Utils.setwronAnssound(activity);

        if (Session.getVibration(activity))
            Utils.vibrate(activity, Utils.VIBRATION_DURATION);

    }

    //set progress again after next question
    private void setAgain() {
        if (progressBarTwo_A.getVisibility() == (View.VISIBLE)) {
            progressBarTwo_A.setVisibility(View.GONE);
            progressBarTwo_B.setVisibility(View.GONE);
            progressBarTwo_C.setVisibility(View.GONE);
            progressBarTwo_D.setVisibility(View.GONE);
            progressBarTwo_E.setVisibility(View.GONE);
        }
    }

    public void FiftyFifty(View view) {
        Utils.btnClick(view, activity);
        // CheckSound();
        if (!Session.isFiftyFiftyUsed(activity)) {
            if (Constant.TOTAL_COINS >= Constant.FIFTY_AUD_COINS) {
                btnPosition = 0;
               // Constant.TOTAL_COINS = Constant.TOTAL_COINS - 4;
                if (Session.isLogin(getApplicationContext()))
                    Utils.UpdateCoin(getApplicationContext(), "-" +Constant.FIFTY_AUD_COINS);
                coin_count.setText(String.valueOf(Constant.TOTAL_COINS));
                if (btnOpt1.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
                    btnPosition = 1;
                if (btnOpt2.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
                    btnPosition = 2;
                if (btnOpt3.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
                    btnPosition = 3;
                if (btnOpt4.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
                    btnPosition = 4;

                if (Session.getBoolean(Session.E_MODE, getApplicationContext()))
                    FiftyFiftyWith_E();
                else
                    FiftyFiftyWithout_E();

                Session.setFifty_Fifty(activity);
            } else
                ShowRewarded(activity,Constant.FIFTY_AUD_COINS);

        } else
            AlreadyUsed();
    }

    public void FiftyFiftyWithout_E() {

        if (btnPosition == 1) {
            layout_B.startAnimation(fifty_fifty_anim);
            layout_C.startAnimation(fifty_fifty_anim);
            layout_B.setClickable(false);
            layout_C.setClickable(false);

        } else if (btnPosition == 2) {
            layout_C.startAnimation(fifty_fifty_anim);
            layout_D.startAnimation(fifty_fifty_anim);
            layout_C.setClickable(false);
            layout_D.setClickable(false);

        } else if (btnPosition == 3) {
            layout_D.startAnimation(fifty_fifty_anim);
            layout_A.startAnimation(fifty_fifty_anim);
            layout_D.setClickable(false);
            layout_A.setClickable(false);

        } else if (btnPosition == 4) {
            layout_A.startAnimation(fifty_fifty_anim);
            layout_B.startAnimation(fifty_fifty_anim);
            layout_A.setClickable(false);
            layout_B.setClickable(false);
        }
    }

    public void FiftyFiftyWith_E() {
        if (btnOpt5.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim())) {
            btnPosition = 5;
        }

        if (btnPosition == 1) {
            layout_B.startAnimation(fifty_fifty_anim);
            layout_C.startAnimation(fifty_fifty_anim);
            layout_B.setClickable(false);
            layout_C.setClickable(false);

        } else if (btnPosition == 2) {
            layout_C.startAnimation(fifty_fifty_anim);
            layout_D.startAnimation(fifty_fifty_anim);
            layout_C.setClickable(false);
            layout_D.setClickable(false);

        } else if (btnPosition == 3) {
            layout_D.startAnimation(fifty_fifty_anim);
            layout_E.startAnimation(fifty_fifty_anim);
            layout_D.setClickable(false);
            layout_E.setClickable(false);

        } else if (btnPosition == 4) {

            layout_E.startAnimation(fifty_fifty_anim);
            layout_A.startAnimation(fifty_fifty_anim);
            layout_E.setClickable(false);
            layout_A.setClickable(false);

        } else if (btnPosition == 5) {
            layout_A.startAnimation(fifty_fifty_anim);
            layout_B.startAnimation(fifty_fifty_anim);
            layout_A.setClickable(false);
            layout_B.setClickable(false);
        }
    }

    public void SkipQuestion(View view) {
        Utils.btnClick(view, activity);
        // CheckSound();

        if (!Session.isSkipUsed(activity)) {
            if (Constant.TOTAL_COINS >=Constant.RESET_SKIP_COINS) {
                stopTimer();
                leftTime = 0;
                Constant.LeftTime = 0;

              //  Constant.TOTAL_COINS = Constant.TOTAL_COINS - 2;
                if (Session.isLogin(getApplicationContext()))
                    Utils.UpdateCoin(getApplicationContext(), "-" + Constant.RESET_SKIP_COINS);
                coin_count.setText(String.valueOf(Constant.TOTAL_COINS));
                questionIndex++;
                nextQuizQuestion();
                Session.setSkip(activity);
            } else
                ShowRewarded(activity,Constant.RESET_SKIP_COINS);
        } else
            AlreadyUsed();
    }

    public void AudiencePoll(View view) {
        Utils.btnClick(view, activity);
        //CheckSound();
        if (!Session.isAudiencePollUsed(activity)) {
            if (Constant.TOTAL_COINS >= Constant.FIFTY_AUD_COINS) {
                btnPosition = 0;
              //  Constant.TOTAL_COINS = Constant.TOTAL_COINS - 4;
                if (Session.isLogin(getApplicationContext()))
                    Utils.UpdateCoin(getApplicationContext(), "-" +  Constant.FIFTY_AUD_COINS);
                coin_count.setText(String.valueOf(Constant.TOTAL_COINS));
                if (Session.getBoolean(Session.E_MODE, getApplicationContext()))
                    AudienceWith_E();
                else
                    AudienceWithout_E();
                Session.setAudiencePoll(activity);
            } else
                ShowRewarded(activity, Constant.FIFTY_AUD_COINS);
        } else
            AlreadyUsed();
    }

    public void AudienceWithout_E() {
        int min = 45;
        int max = 70;
        Random r = new Random();
        int A = r.nextInt(max - min + 1) + min;
        int remain1 = 100 - A;
        int B = r.nextInt(((remain1 - 10)) + 1);
        int remain2 = remain1 - B;
        int C = r.nextInt(((remain2 - 5)) + 1);
        int D = remain2 - C;
        progressBarTwo_A.setVisibility(View.VISIBLE);
        progressBarTwo_B.setVisibility(View.VISIBLE);
        progressBarTwo_C.setVisibility(View.VISIBLE);
        progressBarTwo_D.setVisibility(View.VISIBLE);

        if (btnOpt1.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 1;
        if (btnOpt2.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 2;
        if (btnOpt3.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 3;
        if (btnOpt4.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 4;

        if (btnPosition == 1) {
            progressBarTwo_A.setCurrentProgress(A);
            progressBarTwo_B.setCurrentProgress(B);
            progressBarTwo_C.setCurrentProgress(C);
            progressBarTwo_D.setCurrentProgress(D);

        } else if (btnPosition == 2) {
            progressBarTwo_B.setCurrentProgress(A);
            progressBarTwo_C.setCurrentProgress(C);
            progressBarTwo_D.setCurrentProgress(D);
            progressBarTwo_A.setCurrentProgress(B);

        } else if (btnPosition == 3) {
            progressBarTwo_C.setCurrentProgress(A);
            progressBarTwo_B.setCurrentProgress(C);
            progressBarTwo_D.setCurrentProgress(D);
            progressBarTwo_A.setCurrentProgress(B);

        } else if (btnPosition == 4) {
            progressBarTwo_D.setCurrentProgress(A);
            progressBarTwo_B.setCurrentProgress(C);
            progressBarTwo_C.setCurrentProgress(D);
            progressBarTwo_A.setCurrentProgress(B);

        }
    }

    public void AudienceWith_E() {
        int min = 45;
        int max = 70;
        Random r = new Random();
        int A = r.nextInt(max - min + 1) + min;
        int remain1 = 100 - A;
        int B = r.nextInt(((remain1 - 8)) + 1);
        int remain2 = remain1 - B;
        int C = r.nextInt(((remain2 - 4)) + 1);
        int remain3 = remain2 - C;
        int D = r.nextInt(((remain3 - 2)) + 1);
        int E = remain3 - D;
        progressBarTwo_A.setVisibility(View.VISIBLE);
        progressBarTwo_B.setVisibility(View.VISIBLE);
        progressBarTwo_C.setVisibility(View.VISIBLE);
        progressBarTwo_D.setVisibility(View.VISIBLE);
        progressBarTwo_E.setVisibility(View.VISIBLE);

        if (btnOpt1.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 1;
        if (btnOpt2.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 2;
        if (btnOpt3.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 3;
        if (btnOpt4.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 4;
        if (btnOpt5.getText().toString().trim().equalsIgnoreCase(questionList.get(questionIndex).getTrueAns().trim()))
            btnPosition = 5;

        if (btnPosition == 1) {
            progressBarTwo_A.setCurrentProgress(A);
            progressBarTwo_B.setCurrentProgress(B);
            progressBarTwo_C.setCurrentProgress(C);
            progressBarTwo_D.setCurrentProgress(D);
            progressBarTwo_E.setCurrentProgress(E);

        } else if (btnPosition == 2) {

            progressBarTwo_B.setCurrentProgress(A);
            progressBarTwo_C.setCurrentProgress(B);
            progressBarTwo_D.setCurrentProgress(C);
            progressBarTwo_E.setCurrentProgress(D);
            progressBarTwo_A.setCurrentProgress(E);

        } else if (btnPosition == 3) {

            progressBarTwo_C.setCurrentProgress(A);
            progressBarTwo_D.setCurrentProgress(B);
            progressBarTwo_E.setCurrentProgress(C);
            progressBarTwo_A.setCurrentProgress(D);
            progressBarTwo_B.setCurrentProgress(E);

        } else if (btnPosition == 4) {

            progressBarTwo_D.setCurrentProgress(A);
            progressBarTwo_E.setCurrentProgress(B);
            progressBarTwo_A.setCurrentProgress(C);
            progressBarTwo_B.setCurrentProgress(D);
            progressBarTwo_C.setCurrentProgress(E);

        } else if (btnPosition == 5) {
            progressBarTwo_E.setCurrentProgress(A);
            progressBarTwo_A.setCurrentProgress(B);
            progressBarTwo_B.setCurrentProgress(C);
            progressBarTwo_C.setCurrentProgress(D);
            progressBarTwo_D.setCurrentProgress(E);
        }
    }

    public void ResetTimer(View view) {
        Utils.btnClick(view, activity);
        // CheckSound();
        if (!Session.isResetUsed(activity)) {
            if (Constant.TOTAL_COINS >= Constant.RESET_SKIP_COINS) {
                //Constant.TOTAL_COINS = Constant.TOTAL_COINS - 2;
                if (Session.isLogin(getApplicationContext()))
                    Utils.UpdateCoin(getApplicationContext(), "-" + Constant.RESET_SKIP_COINS);
                coin_count.setText(String.valueOf(Constant.TOTAL_COINS));
                Constant.LeftTime = 0;
                leftTime = 0;
                stopTimer();
                starTimer();
                Session.setReset(activity);
            } else
                ShowRewarded(activity,Constant.RESET_SKIP_COINS);
        } else
            AlreadyUsed();
    }

    //Show alert dialog when lifeline already used in current level
    public void AlreadyUsed() {
        stopTimer();
        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.lifeline_dialog, null);
        dialog.setView(dialogView);
        TextView ok = (TextView) dialogView.findViewById(R.id.ok);
        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if (leftTime != 0) {
                    timer = new Timer(leftTime, 1000);
                    timer.start();
                }
            }
        });
    }

    public void BackButtonMethod() {

        CheckSound();
        PlayAreaLeaveDialog();

    }

    public void CheckSound() {

        if (Session.getSoundEnableDisable(activity))
            Utils.backSoundonclick(activity);

        if (Session.getVibration(activity))
            Utils.vibrate(activity, Utils.VIBRATION_DURATION);
    }

    public void SettingButtonMethod() {
        CheckSound();
        stopTimer();

        Intent intent = new Intent(activity, SettingActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.open_next, R.anim.close_next);
    }

    public void getQuestionsFromJson() {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_QUESTION_BY_CONTEST, Constant.GETDATAKEY);
        params.put(Constant.CONTEST_ID, qid);
        System.out.println("PARAms::=" + params);
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        queindex = 0;
                        totalque = 0;
                        session.setData(UserSessionManager.KEY_QUEATTEND, "0");
                        session.setData(UserSessionManager.KEY_CORRECTANS, "0");
                        session.setData(UserSessionManager.KEY_WRONGANS, "0");
                        progressBar.setVisibility(View.VISIBLE);
                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean(Constant.ERROR);
                        if (!error) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);

                            questionList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                Question question = new Question();
                                question.setId(Integer.parseInt(object.getString(Constant.ID)));
                                question.setQuestion(object.getString(Constant.QUESTION));
                                question.setImage(object.getString(Constant.IMAGE));
                                question.setQueType(object.getString(Constant.QUE_TYPE));
                                question.addOption(object.getString(Constant.OPTION_A).trim());
                                question.addOption(object.getString(Constant.OPTION_B).trim());
                                question.addOption(object.getString(Constant.OPTION_C).trim());
                                question.addOption(object.getString(Constant.OPTION_D).trim());
                                if (Session.getBoolean(Session.E_MODE, activity)) {
                                    if (!object.getString(Constant.OPTION_E).isEmpty() || !object.getString(Constant.OPTION_E).equals(""))
                                        question.addOption(object.getString(Constant.OPTION_E).trim());
                                }
                                question.setSelectedOpt("none");
                                String rightAns = object.getString("answer");
                                question.setAnsOption(rightAns);
                                if (rightAns.equalsIgnoreCase("A")) {
                                    question.setTrueAns(object.getString(Constant.OPTION_A).trim());
                                } else if (rightAns.equalsIgnoreCase("B")) {
                                    question.setTrueAns(object.getString(Constant.OPTION_B).trim());
                                } else if (rightAns.equalsIgnoreCase("C")) {
                                    question.setTrueAns(object.getString(Constant.OPTION_C).trim());
                                } else if (rightAns.equalsIgnoreCase("D")) {
                                    question.setTrueAns(object.getString(Constant.OPTION_D).trim());
                                } else if (rightAns.equalsIgnoreCase("E")) {
                                    question.setTrueAns(object.getString(Constant.OPTION_E).trim());
                                }

                                question.setNote(object.getString(Constant.NOTE));

                                questionList.add(question);
                            }

                            if (from.equals("contest"))
                                AddPoint("-" + entrypoint, "Contest Entrypoint", true);

                            nextQuizQuestion();
                            playLayout.setVisibility(View.VISIBLE);


                        } else {
                            NotEnoughQuestion();
                        }
                        progressBar.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);

    }

    public void NotEnoughQuestion() {
        invalidateOptionsMenu();
        tvAlert.setText(getString(R.string.no_enough_question));
        progressBar.setVisibility(View.GONE);
        playLayout.setVisibility(View.GONE);

    }

    public void blankAllValue() {
        questionIndex = 0;
        count_question_completed = 0;
        score = 0;
        correctQuestion = 0;
        inCorrectQuestion = 0;
    }


    //Show dialog for rewarded ad
    //if user has not enough coin to use lifeline
    public void ShowRewarded(final Activity activity, int coin) {
        stopTimer();
        if (!Utils.isNetworkAvailable(activity)) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setTitle("Internet Connection Error!");
            dialog.setMessage("Internet Connection Error! Please connect to working Internet connection");
            dialog.setOnCancelListener(dialogInterface -> {
                if (leftTime != 0) {
                    timer = new Timer(leftTime, 1000);
                    timer.start();
                }

            });
            dialog.setPositiveButton("OK", (dialogInterface, i) -> {
                if (leftTime != 0) {
                    timer = new Timer(leftTime, 1000);
                    timer.start();
                }

            });
            dialog.show();


        } else {

            final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.dialog_alert_coin, null);
            dialog.setView(dialogView);
            TextView tvCoinMsg = dialogView.findViewById(R.id.tvCoinMSg);
            TextView skip = dialogView.findViewById(R.id.skip);
            TextView watchNow = dialogView.findViewById(R.id.watch_now);
            tvCoinMsg.setText(activity.getResources().getString(R.string.coin_message1) + coin + activity.getResources().getString(R.string.coin_message2));
            final AlertDialog alertDialog = dialog.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();
            alertDialog.setCancelable(false);
            skip.setOnClickListener(view -> {
                alertDialog.dismiss();
                isDialogOpen = false;
                if (leftTime != 0) {
                    timer = new Timer(leftTime, 1000);
                    timer.start();
                }
            });
            watchNow.setOnClickListener(view -> {
                showRewardedVideo();
                alertDialog.dismiss();
                isDialogOpen = false;
            });
        }
    }

    public void showRewardedVideo() {

        if (rewardedVideoAd != null) {

            Activity activityContext = activity;
            rewardedVideoAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    rewardedVideoAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.

                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    RewardsadsLoad();
                    // Called when ad is dismissed.
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.

                }
            });
            rewardedVideoAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull com.google.android.gms.ads.rewarded.RewardItem rewardItem) {
                    Constant.TOTAL_COINS = (Constant.TOTAL_COINS + Constant.REWARD_COIN_VALUE);
                    if (Session.isLogin(getApplicationContext()))
                        Utils.UpdateCoin(getApplicationContext(), String.valueOf(Constant.REWARD_COIN_VALUE));
                    RewardsadsLoad();
                }
            });
        } else {
            reWardsNotLoad();
        }
    }


    public void reWardsNotLoad() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.lifeline_dialog, null);
        dialog.setView(dialogView);
        TextView ok = (TextView) dialogView.findViewById(R.id.ok);
        TextView title = (TextView) dialogView.findViewById(R.id.title);
        TextView message = (TextView) dialogView.findViewById(R.id.message);
        message.setText(getString(R.string.rewards_message));
        title.setText(getString(R.string.rewads_adstitle));
        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                RewardsadsLoad();
                alertDialog.dismiss();

            }

        });
    }


    public class Timer extends CountDownTimer {
        private Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            leftTime = millisUntilFinished;
            //   System.out.println("===  timer  " + millisUntilFinished);
            count = (int) (millisUntilFinished / 1000);
            if (progressTimer == null)
                progressTimer = findViewById(R.id.circleTimer);
            else
                progressTimer.setCurrentProgress(count);

            //when left last 5 second we show progress color red
            if (millisUntilFinished <= 6000)
                progressTimer.SetTimerAttributes(Color.RED, Color.RED);
            else
                progressTimer.SetTimerAttributes(Color.parseColor(Constant.PROGRESS_COLOR), Color.parseColor(Constant.PROGRESS_COLOR));

        }

        @Override
        public void onFinish() {
            if (questionIndex >= questionList.size()) {
                levelCompleted();
            } else {
                playWrongSound();
                score = score - Constant.PENALTY;
                inCorrectQuestion = inCorrectQuestion + 1;
                tvScore.setText(String.valueOf(score));
                mHandler.postDelayed(mUpdateUITimerTask, 100);
                questionIndex++;
            }

        }
    }

    private void AddPoint(final String point, final String type, final Boolean isminus) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.ADDPOINT, Constant.GETDATAKEY);
        params.put(Constant.USER_ID, Session.getUserData(Session.USER_ID, activity));
        params.put(Constant.POINTS, point);
        params.put(Constant.TYPE, type);
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("error").equals("false")) {

                            double point1;
                            if (isminus)
                                point1 = Double.parseDouble(session.getData(UserSessionManager.KEY_POINT)) - Double.parseDouble(point);
                            else
                                point1 = Double.parseDouble(session.getData(UserSessionManager.KEY_POINT)) + Double.parseDouble(point);

                            session.setData(UserSessionManager.KEY_POINT, String.valueOf(point1));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);
    }


    public void starTimer() {
        timer = new Timer(Constant.TIME_PER_QUESTION, Constant.COUNT_DOWN_TIMER);
        timer.start();
    }

    public void stopTimer() {
        if (timer != null)
            timer.cancel();
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        Utils.CheckBgMusic(activity);
        if (Constant.LeftTime != 0) {
            timer = new Timer(leftTime, 1000);
            timer.start();
        }
        coin_count.setText(String.valueOf(Constant.TOTAL_COINS));
        super.onResume();
    }

    @Override
    public void onPause() {
        AppController.StopSound();
        Constant.LeftTime = leftTime;
        stopTimer();
        super.onPause();
    }


    @Override
    public void onDestroy() {
        leftTime = 0;
        stopTimer();
        AddPoint(String.valueOf(point), type, false);
        if (Session.getSoundEnableDisable(activity)) {
            int resourceId = R.raw.wrong;
            MediaPlayer mediaplayer = MediaPlayer.create(activity, resourceId);
            mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.reset();
                    mp.release();
                }
            });
            int resourceIds = R.raw.right;
            MediaPlayer mediaplayers = MediaPlayer.create(activity, resourceIds);
            mediaplayers.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.reset();
                    mp.release();
                }
            });
        }
        blankAllValue();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.report).setVisible(false);
        menu.findItem(R.id.bookmark).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.setting:
                SettingButtonMethod();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed() {

        PlayAreaLeaveDialog();

    }
}