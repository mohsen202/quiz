package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.toolbox.ImageLoader;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.helper.CircleTimer;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.TouchImageView;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class BattlePlayActivity extends AppCompatActivity implements View.OnClickListener {
    public static TextView btnOpt1, btnOpt2, btnOpt3, btnOpt4, btnOpt5, txtQuestion,
            p2ans_a, p2ans_b, p2ans_c, p2ans_d, p2ans_e, tvPlayer1Name, tvPlayer2Name, btnQuitGame, tvIndex;
    public static ArrayList<String> options;


    private final Handler mHandler = new Handler();
    public Question question;

    public RelativeLayout playLayout;
    public Animation RightSwipe_A, RightSwipe_B, RightSwipe_C, RightSwipe_D, RightSwipe_E, Fade_in;

    CircleImageView imgPlayer1, imgPlayer2;
    DatabaseReference myGameRef;
    ImageView imgZoom;
    RelativeLayout layout_A, layout_B, layout_C, layout_D, layout_E;

    TouchImageView imgQuestion;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    private Context mContext;
    private Animation animation;

    public long leftTime;
    public MyCountDownTimer timer;
    public AlertDialog quitAlertDialog;
    public TextSwitcher right_p1, right_p2, right_p01, right_p02;
    public Animation in, out;

    public ProgressBar progressBar;

    public ScrollView mainScroll, queScroll;
    public int questionIndex = 0, correctQuestion = 0, inCorrectQuestion = 0, click = 0, textSize, preScore = 0;
    public String userId1, userId2, Player1Name, Player2Name, winner, winnerMessage,
            matchingId = "", player1Key = "", player2Key = "", optionClicked = "false",
            tts = "tts", profilePlayer1, profilePlayer2, winDialogTitle, pauseCheck = "regular";
    public ValueEventListener player1Listener, player2Listener;
    public boolean isPlayerLeft;
    public ArrayList<Question> battleQuestionList;

    public String index = "index00";
    int attendedQue = 0;
    public static CircleTimer progressTimer;
    AdView mAdView;
    AdRequest adRequest;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);
        mContext = BattlePlayActivity.this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTelephoneListener();
        matchingId = getIntent().getStringExtra("gameid");

        player1Key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        player2Key = getIntent().getStringExtra("opponentId");
        userId1 = getIntent().getStringExtra("user_id1");
        userId2 = getIntent().getStringExtra("user_id2");
        Player2Name = getIntent().getStringExtra("player2Name");
        profilePlayer2 = getIntent().getStringExtra("player2Profile");

        mAdView = findViewById(R.id.banner_AdView);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        myGameRef = FirebaseDatabase.getInstance().getReference().child(Constant.DB_GAME_ROOM_NEW);

        int[] CLICKABLE = new int[]{R.id.a_layout, R.id.b_layout, R.id.c_layout, R.id.d_layout, R.id.e_layout};
        for (int i : CLICKABLE) {
            findViewById(i).setOnClickListener(this);
        }

        progressBar = findViewById(R.id.progressBar);
        right_p1 = findViewById(R.id.right_p1);
        right_p2 = findViewById(R.id.right_p2);
        right_p01 = findViewById(R.id.right_p01);
        right_p02 = findViewById(R.id.right_p02);

        p2ans_a = findViewById(R.id.p2ans_a);
        p2ans_b = findViewById(R.id.p2ans_b);
        p2ans_c = findViewById(R.id.p2ans_c);
        p2ans_d = findViewById(R.id.p2ans_d);
        p2ans_e = findViewById(R.id.p2ans_e);

        mainScroll = findViewById(R.id.mainScroll);
        queScroll = findViewById(R.id.queScroll);

        imgQuestion = findViewById(R.id.imgQuestion);

        btnOpt1 = findViewById(R.id.btnOpt1);
        btnOpt2 = findViewById(R.id.btnOpt2);
        btnOpt3 = findViewById(R.id.btnOpt3);
        btnOpt4 = findViewById(R.id.btnOpt4);
        btnOpt5 = findViewById(R.id.btnOpt5);


        imgZoom = findViewById(R.id.imgZoom);

        tvPlayer1Name = findViewById(R.id.tv_player1_name);
        tvPlayer2Name = findViewById(R.id.tv_player2_name);
        imgPlayer1 = findViewById(R.id.iv_player1_pic);
        imgPlayer2 = findViewById(R.id.iv_player2_pic);

        btnQuitGame = findViewById(R.id.btn_quit);
        imgPlayer1.setDefaultImageResId(R.drawable.ic_account);
        imgPlayer2.setDefaultImageResId(R.drawable.ic_android);
        textSize = Integer.parseInt(Session.getSavedTextSize(BattlePlayActivity.this));
        Session.removeSharedPreferencesData(BattlePlayActivity.this);

        RightSwipe_A = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_a);
        RightSwipe_B = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_b);
        RightSwipe_C = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_c);
        RightSwipe_D = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_d);
        RightSwipe_E = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_e);
        Fade_in = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);


        playLayout = findViewById(R.id.innerLayout);
        playLayout.setVisibility(View.GONE);

        txtQuestion = findViewById(R.id.txtQuestion);
        layout_A = findViewById(R.id.a_layout);
        layout_B = findViewById(R.id.b_layout);
        layout_C = findViewById(R.id.c_layout);
        layout_D = findViewById(R.id.d_layout);
        layout_E = findViewById(R.id.e_layout);
        tvIndex = findViewById(R.id.tvIndex);

        progressTimer = findViewById(R.id.circleTimer);
        progressTimer.setMaxProgress(Constant.CIRCULAR_MAX_PROGRESS);
        progressTimer.setCurrentProgress(Constant.CIRCULAR_MAX_PROGRESS);
        ChangeTextSize(textSize);

        animation = AnimationUtils.loadAnimation(BattlePlayActivity.this, R.anim.right_ans_anim); // Change alpha from fully visible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the


        if (Utils.isNetworkAvailable(BattlePlayActivity.this)) {
            init();
            getQuestionsFromJson();
        } else {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar
                    .make(parentLayout, getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
            snackbar.show();
        }

        btnQuitGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        in = AnimationUtils.loadAnimation(this, R.anim.slide_up1);
        out = AnimationUtils.loadAnimation(this, R.anim.slide_up);


        right_p1.setFactory(mFactory);
        right_p01.setFactory(mFactory);
        right_p2.setFactory(mFactory);
        right_p02.setFactory(mFactory);

        right_p1.setCurrentText(String.valueOf(correctQuestion));
        right_p01.setCurrentText(String.valueOf(inCorrectQuestion));
        right_p2.setCurrentText(String.valueOf(correctQuestion));
        right_p02.setCurrentText(String.valueOf(inCorrectQuestion));

        right_p1.setInAnimation(in);
        right_p1.setOutAnimation(out);

        right_p01.setOutAnimation(out);
        right_p2.setInAnimation(in);
        right_p2.setOutAnimation(out);

        right_p02.setOutAnimation(out);

        mainScroll.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
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

    }


    private final Runnable mUpdateUITimerTask = new Runnable() {
        public void run() {
            progressBar.setVisibility(View.GONE);

            nextQuizQuestion();
        }
    };

    private void init() {


        Player1Name = Session.getUserData(Session.NAME, getApplicationContext());
        tvPlayer1Name.setText(Player1Name);
        profilePlayer1 = Session.getUserData(Session.PROFILE, getApplicationContext());
        imgPlayer1.setImageUrl(profilePlayer1, imageLoader);

        tvPlayer2Name.setText(Player2Name);
        imgPlayer2.setImageUrl(profilePlayer2, imageLoader);


        p2ans_a.setText(Player2Name);
        p2ans_b.setText(Player2Name);
        p2ans_c.setText(Player2Name);
        p2ans_d.setText(Player2Name);
        p2ans_e.setText(Player2Name);

        player1Listener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {

                        if (!isPlayerLeft) {
                            final int[] p1_que = new int[1];
                            final int[] r_1 = new int[1];
                            final int[] p2_que = new int[1];
                            final int[] r_2 = new int[1];
                            final String[] p2_sel = new String[1];
                            if (dataSnapshot.hasChild(Constant.QUESTIONS)) {
                                p1_que[0] = (int) dataSnapshot.child(Constant.QUESTIONS).getChildrenCount();

                            }
                            player2Key = getIntent().getStringExtra("opponentId");
                            player2Listener = myGameRef.child(player2Key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.child(Constant.LEFT_BATTLE).exists()) {
                                        if ((boolean) snapshot.child(Constant.LEFT_BATTLE).getValue()) {
                                            if (quitAlertDialog == null) {
                                                showOtherUserQuitDialog();
                                            }
                                            isPlayerLeft = true;
                                        }
                                    } else {
                                        if (snapshot.child(Constant.QUESTIONS).exists()) {
                                            p2_que[0] = (int) snapshot.child(Constant.QUESTIONS).getChildrenCount();
                                            p2_sel[0] = snapshot.child(Constant.QUESTIONS).child(String.valueOf(p2_que[0] - 1)).child(Constant.SEL_ANS).getValue(String.class);
                                            if (snapshot.child(Constant.RIGHT_ANS).exists()) {
                                                r_2[0] = Integer.parseInt(snapshot.child(Constant.RIGHT_ANS).getValue().toString());
                                            }
                                        }

                                        if (p1_que[0] == p2_que[0]) {
                                            if (optionClicked.equalsIgnoreCase("true")) {
                                                if (btnOpt1.getText().toString().equalsIgnoreCase(p2_sel[0])) {
                                                    p2ans_a.setVisibility(View.VISIBLE);
                                                    assert p2_sel[0] != null;
                                                    if (p2_sel[0].equalsIgnoreCase(question.getTrueAns())) {
                                                        p2ans_a.setTextColor(getResources().getColor(R.color.wrong_dark));
                                                    } else {
                                                        p2ans_a.setTextColor(getResources().getColor(R.color.right_dark));
                                                    }

                                                } else if (btnOpt2.getText().toString().equalsIgnoreCase(p2_sel[0])) {
                                                    p2ans_b.setVisibility(View.VISIBLE);
                                                    assert p2_sel[0] != null;
                                                    if (p2_sel[0].equalsIgnoreCase(question.getTrueAns())) {
                                                        p2ans_b.setTextColor(getResources().getColor(R.color.wrong_dark));
                                                    } else {
                                                        p2ans_b.setTextColor(getResources().getColor(R.color.right_dark));
                                                    }

                                                } else if (btnOpt3.getText().toString().equalsIgnoreCase(p2_sel[0])) {
                                                    p2ans_c.setVisibility(View.VISIBLE);
                                                    assert p2_sel[0] != null;
                                                    if (p2_sel[0].equalsIgnoreCase(question.getTrueAns())) {
                                                        p2ans_c.setTextColor(getResources().getColor(R.color.wrong_dark));
                                                    } else {
                                                        p2ans_c.setTextColor(getResources().getColor(R.color.right_dark));
                                                    }

                                                } else if (btnOpt4.getText().toString().equalsIgnoreCase(p2_sel[0])) {
                                                    p2ans_d.setVisibility(View.VISIBLE);
                                                    assert p2_sel[0] != null;
                                                    if (p2_sel[0].equalsIgnoreCase(question.getTrueAns())) {
                                                        p2ans_d.setTextColor(getResources().getColor(R.color.wrong_dark));
                                                    } else {
                                                        p2ans_d.setTextColor(getResources().getColor(R.color.right_dark));
                                                    }

                                                } else if (btnOpt5.getText().toString().equalsIgnoreCase(p2_sel[0])) {
                                                    p2ans_e.setVisibility(View.VISIBLE);
                                                    assert p2_sel[0] != null;
                                                    if (p2_sel[0].equalsIgnoreCase(question.getTrueAns())) {
                                                        p2ans_e.setTextColor(getResources().getColor(R.color.wrong_dark));
                                                    } else {
                                                        p2ans_e.setTextColor(getResources().getColor(R.color.right_dark));
                                                    }

                                                }
                                            }
                                            if (dataSnapshot.child(Constant.RIGHT_ANS).exists()) {
                                                r_1[0] = Integer.parseInt(dataSnapshot.child(Constant.RIGHT_ANS).getValue().toString());
                                            }

                                            if (r_2[0] == battleQuestionList.size()) {
                                                right_p02.setText("");
                                            }

                                            if (preScore != r_2[0]) {
                                                right_p2.setText(String.valueOf(r_2[0]));
                                                preScore = r_2[0];
                                            }
                                            if (p1_que[0] == battleQuestionList.size()) {
                                                if (attendedQue == battleQuestionList.size()) {
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (r_1[0] > r_2[0]) {
                                                                winnerMessage = Player1Name + getString(R.string.msg_win_battle);
                                                                winner = "you";
                                                                winDialogTitle = getString(R.string.congrats);
                                                                showWinnerDialog();

                                                            } else if (r_2[0] > r_1[0]) {
                                                                winnerMessage = Player2Name + getString(R.string.msg_opponent_win_battle);
                                                                winner = Player2Name;
                                                                winDialogTitle = getString(R.string.next_time);
                                                                showWinnerDialog();
                                                            } else {

                                                                showResetGameAlert();

                                                            }

                                                            //btnQuitGame.setText("GO BACK");
                                                        }
                                                    }, 1500);
                                                }
                                            } else {
                                                if (!index.equalsIgnoreCase("index" + questionIndex))
                                                    mHandler.postDelayed(mUpdateUITimerTask, 1000);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }


    public void getQuestionsFromJson() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.getRandomQuestion, "1");
        params.put(Constant.GAME_ROOM_KEY, matchingId);
        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext()))
            params.put(Constant.LANGUAGE_ID, Session.getCurrentLanguage(getApplicationContext()));
        if (Constant.isCateEnable)
            params.put(Constant.category, Constant.CATE_ID);
        //System.out.println("====battle params " + params.toString());
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                // System.out.println("========search result " + response);
                if (result) {
                    try {
                        battleQuestionList = new ArrayList<>();
                        battleQuestionList.clear();
                        // System.out.println("====battle ques " + response);
                        JSONObject jsonObject = new JSONObject(response);
                        String error = jsonObject.getString(Constant.ERROR);
                        if (error.equalsIgnoreCase("false")) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            battleQuestionList.addAll(Utils.getQuestions(jsonArray, BattlePlayActivity.this));
                            Constant.MAX_QUESTION_PER_BATTLE = battleQuestionList.size();
                            myGameRef.child(player1Key).addValueEventListener(player1Listener);
                            playLayout.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);

    }

    @Override
    public void onClick(View v) {
        if (questionIndex < battleQuestionList.size()) {
            question = battleQuestionList.get(questionIndex);
            layout_A.setClickable(false);
            layout_B.setClickable(false);
            layout_C.setClickable(false);
            layout_D.setClickable(false);
            layout_E.setClickable(false);


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


            optionClicked = "true";

        }
    }

    public void AddReview(Question question, TextView tvBtnOpt, RelativeLayout layout) {
        layout_A.setClickable(false);
        layout_B.setClickable(false);
        layout_C.setClickable(false);
        layout_D.setClickable(false);
        layout_E.setClickable(false);

        if (tvBtnOpt.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout.setBackgroundResource(R.drawable.right_gradient);
            correctQuestion = correctQuestion + 1;
            addScore(tvBtnOpt.getText().toString().trim());

        } else {
            layout.setBackgroundResource(R.drawable.wrong_gradient);
            inCorrectQuestion = inCorrectQuestion + 1;
            WrongQuestion(tvBtnOpt.getText().toString().trim());
        }

        question.setSelectedAns(tvBtnOpt.getText().toString());
        if (Constant.QUICK_ANSWER_ENABLE.equals("1"))
            RightAnswerBackgroundSet();
        question.setAttended(true);
        attendedQue = (attendedQue + 1);
        questionIndex++;

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

    private void showOtherUserQuitDialog() {
        stopTimer();
        try {
            pauseCheck = "win";
            final AlertDialog.Builder dialog = new AlertDialog.Builder(BattlePlayActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.dialog_reset_game, null);
            dialog.setView(dialogView);
            dialog.setCancelable(false);
            quitAlertDialog = dialog.create();

            TextView tvMessage = dialogView.findViewById(R.id.tv_message);
            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            tvTitle.setText(Player1Name);
            TextView btnok = dialogView.findViewById(R.id.btn_ok);

            tvMessage.setText(getString(R.string.you_win) + Player2Name + getString(R.string.leave_battle_txt));
            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myGameRef.child(player1Key).removeValue();
                    myGameRef.child(player2Key).removeValue();
                    DestroyKey(matchingId);
                    quitAlertDialog.dismiss();
                    finish();
                }
            });


            Objects.requireNonNull(quitAlertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            quitAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showQuitGameAlertDialog() {
        try {
            pauseCheck = "win";
            stopTimer();
            final AlertDialog.Builder dialog1 = new AlertDialog.Builder(BattlePlayActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.dialog_leave_battle, null);
            dialog1.setView(dialogView);
            dialog1.setCancelable(true);

            final AlertDialog alertDialog = dialog1.create();
            TextView tvMessage = dialogView.findViewById(R.id.tv_message);
            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            tvTitle.setText(Player1Name);
            TextView btnok = dialogView.findViewById(R.id.btn_ok);
            TextView btnNo = dialogView.findViewById(R.id.btnNo);
            tvMessage.setText(getString(R.string.msg_alert_leave));
            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myGameRef.child(player1Key).child(Constant.LEFT_BATTLE).setValue(true);
                    alertDialog.dismiss();
                    finish();

                }
            });
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timer = new MyCountDownTimer(leftTime, 1000);
                    timer.start();
                    alertDialog.dismiss();
                }
            });
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setCancelable(false);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DestroyKey(final String roomKey) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.getRandomQuestion, "1");
        params.put(Constant.GAME_ROOM_KEY, roomKey);
        params.put(Constant.DESTROY_GAME_KEY, "1");
        //System.out.println("===destroy params " + params.toString());
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {

                }
            }
        }, params);
    }

    private void showWinnerDialog() {
        try {
            pauseCheck = "win";
            stopTimer();

            clearQuestionList();
            final AlertDialog.Builder dialog1 = new AlertDialog.Builder(BattlePlayActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View sheetView = inflater.inflate(R.layout.bottom_complete, null);
            dialog1.setView(sheetView);
            dialog1.setCancelable(false);
            DestroyKey(matchingId);
            final AlertDialog alertDialog = dialog1.create();
            final CircleImageView player1, player2;
            final TextView wrong, tvScore, winnerstatus, looserstatus, txt_result_title, victorymsg, tvPlayNext, exit;
            final ImageView victoryimg;

            player1 = sheetView.findViewById(R.id.winnerImg);
            player2 = sheetView.findViewById(R.id.losserimage);
            wrong = sheetView.findViewById(R.id.wrong);
            tvScore = sheetView.findViewById(R.id.tvScore);
            winnerstatus = sheetView.findViewById(R.id.winnerstatus);
            looserstatus = sheetView.findViewById(R.id.looserstatus);
            txt_result_title = sheetView.findViewById(R.id.txt_result_title);
            victorymsg = sheetView.findViewById(R.id.victorymsg);
            victoryimg = sheetView.findViewById(R.id.victoryimg);
            tvPlayNext = sheetView.findViewById(R.id.tvPlayNext);
            exit = sheetView.findViewById(R.id.exit);
            if (winner.equals("you")) {
                txt_result_title.setText(getString(R.string.congrats));
                winnerstatus.setText(getString(R.string.winner));
                wrong.setText(Player1Name);
                tvScore.setText(Player2Name);
                victorymsg.setText(getString(R.string.victory_));
                victoryimg.setBackgroundResource(R.drawable.ic_victory);
                player1.setImageUrl(profilePlayer1, imageLoader);
                player2.setImageUrl(profilePlayer2, imageLoader);
                looserstatus.setText(getString(R.string.you_loss));
            } else {
                txt_result_title.setText(getString(R.string.next_time));
                winnerstatus.setText(getString(R.string.you_loss));
                wrong.setText(Player1Name);
                tvScore.setText(Player2Name);
                victorymsg.setText(getString(R.string.defeat));
                victoryimg.setBackgroundResource(R.drawable.ic_defeat);
                player1.setImageUrl(profilePlayer1, imageLoader);
                player2.setImageUrl(profilePlayer2, imageLoader);
                looserstatus.setText(getString(R.string.winner));
            }


            tvPlayNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myGameRef.child(player1Key).removeValue();
                    Intent intentReBattle = new Intent(BattlePlayActivity.this, SearchPlayerActivity.class);
                    intentReBattle.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentReBattle);
                    alertDialog.dismiss();
                    finish();
                }
            });
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myGameRef.child(player1Key).removeValue();
                    resetValues();
                    alertDialog.dismiss();
                    finish();
                }
            });
            alertDialog.show();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showResetGameAlert() {
        pauseCheck = "win";
        clearQuestionList();
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_reset_game);
            dialog.setCancelable(false);
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            TextView btnok = dialog.findViewById(R.id.btn_ok);
            tvMessage.setText(getString(R.string.msg_draw_game));
            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myGameRef.child(player1Key).removeValue();
                    DestroyKey(matchingId);
                    finish();
                    dialog.dismiss();
                }
            });
            dialog.show();
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void clearQuestionList() {
        if (battleQuestionList != null)
            battleQuestionList.clear();
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

    private ViewSwitcher.ViewFactory mFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(BattlePlayActivity.this);
            t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            t.setTextAppearance(BattlePlayActivity.this, android.R.style.TextAppearance_DeviceDefault_Medium);
            t.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            return t;
        }
    };


    public class MyCountDownTimer extends CountDownTimer {

        private MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            leftTime = millisUntilFinished;
            // System.out.println("=======timer " + isPlayerLeft + " === " + millisUntilFinished);
            int progress = (int) (millisUntilFinished / 1000);
            if (progressTimer == null)
                progressTimer = findViewById(R.id.circleTimer);
            else
                progressTimer.setCurrentProgress(progress);

            //when left last 5 second we show progress color red
            if (millisUntilFinished <= 6000)
                progressTimer.SetTimerAttributes(Color.RED, Color.RED);
            else
                progressTimer.SetTimerAttributes(Color.parseColor(Constant.PROGRESS_COLOR), Color.parseColor(Constant.PROGRESS_COLOR));
        }

        @Override
        public void onFinish() {
            if (questionIndex <= battleQuestionList.size()) {
                if (!isPlayerLeft)
                    if (optionClicked.equals("false")) {
                        layout_A.setClickable(false);
                        layout_B.setClickable(false);
                        layout_C.setClickable(false);
                        layout_D.setClickable(false);
                        layout_E.setClickable(false);
                        attendedQue = (attendedQue + 1);
                        WrongQuestion("");
                        questionIndex++;
                    }
            }

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        resetValues();
        UpdateOnlineStatus();
        //System.out.println("===stop call");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        if (tts.equals("tts")) {
            if (pauseCheck.equals("regular")) {
                myGameRef.child(player1Key).child(Constant.LEFT_BATTLE).setValue(true);
                UpdateOnlineStatus();
                resetValues();
                finish();

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateOnlineStatus();
        resetValues();
        finish();
    }

    public void resetValues() {

        clearQuestionList();
        player1Key = "";
        player2Key = "";
        matchingId = "";
        leftTime = 0;
        stopTimer();

        if (quitAlertDialog != null) {
            if (quitAlertDialog.isShowing()) {
                quitAlertDialog.dismiss();
            }
        }
    }

    public void UpdateOnlineStatus() {
        if (myGameRef != null && player1Listener != null) {
            myGameRef.child(player1Key).removeEventListener(player1Listener);

        }
        if (player2Listener != null)
            myGameRef.child(player2Key).removeEventListener(player2Listener);
    }

    private void addScore(final String sel_ans) {
        rightSound();
        if (correctQuestion == battleQuestionList.size()) {
            right_p01.setText("");
        }
        right_p1.setText(String.valueOf(correctQuestion));
        Map<String, Object> taskMap = new HashMap<String, Object>();
        taskMap.put(Constant.RIGHT_ANS, String.valueOf(correctQuestion));
        taskMap.put(Constant.QUESTIONS + "/" + questionIndex + "/" + Constant.SEL_ANS, sel_ans);
        myGameRef.child(player1Key).updateChildren(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        // System.out.println("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put(Constant.RIGHT_ANS, String.valueOf(correctQuestion));
                        taskMap.put(Constant.QUESTIONS + "/" + questionIndex + "/" + Constant.SEL_ANS, sel_ans);
                        myGameRef.child(player1Key).updateChildren(taskMap);
                    }
                });


    }

    private void WrongQuestion(final String sel_ans) {
        setAgain();
        playWrongSound();
        Map<String, Object> taskMap = new HashMap<String, Object>();
        // taskMap.put(Constant.RIGHT_ANS, correctQuestion);
        taskMap.put(Constant.QUESTIONS + "/" + questionIndex + "/" + Constant.SEL_ANS, sel_ans);
        myGameRef.child(player1Key).updateChildren(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        //  System.out.println("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // System.out.println("===== wrong fail");
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put(Constant.QUESTIONS + "/" + questionIndex + "/" + Constant.SEL_ANS, sel_ans);
                        myGameRef.child(player1Key).updateChildren(taskMap);
                    }
                });

    }

    /*
     * Save score in Preferences
     */
    //play sound when answer is correct
    public void rightSound() {
        if (Session.getSoundEnableDisable(mContext)) {
            Utils.setrightAnssound(mContext);
        }
        if (Session.getVibration(mContext)) {
            Utils.vibrate(mContext, Utils.VIBRATION_DURATION);
        }
    }

    //play sound when answer is incorrect
    private void playWrongSound() {
        if (Session.getSoundEnableDisable(mContext)) {
            Utils.setwronAnssound(mContext);
        }
        if (Session.getVibration(mContext)) {
            Utils.vibrate(mContext, Utils.VIBRATION_DURATION);
        }
    }

    //set progress again after next question
    private void setAgain() {

        p2ans_a.setVisibility(View.GONE);
        p2ans_b.setVisibility(View.GONE);
        p2ans_c.setVisibility(View.GONE);
        p2ans_d.setVisibility(View.GONE);
        p2ans_e.setVisibility(View.GONE);

    }

    private void nextQuizQuestion() {
        queScroll.scrollTo(0,0);
        optionClicked = "false";
        tts = "tts";
        setAgain();
        stopTimer();

        layout_A.setBackgroundColor(ContextCompat.getColor(BattlePlayActivity.this, R.color.white));
        layout_B.setBackgroundColor(ContextCompat.getColor(BattlePlayActivity.this, R.color.white));
        layout_C.setBackgroundColor(ContextCompat.getColor(BattlePlayActivity.this, R.color.white));
        layout_D.setBackgroundColor(ContextCompat.getColor(BattlePlayActivity.this, R.color.white));
        layout_E.setBackgroundColor(ContextCompat.getColor(BattlePlayActivity.this, R.color.white));
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

        if (questionIndex < battleQuestionList.size()) {
            mAdView.loadAd(adRequest);
            btnOpt1.startAnimation(RightSwipe_A);
            btnOpt2.startAnimation(RightSwipe_B);
            btnOpt3.startAnimation(RightSwipe_C);
            btnOpt4.startAnimation(RightSwipe_D);
            btnOpt5.startAnimation(RightSwipe_E);
            index = "index" + questionIndex;
            timer = new MyCountDownTimer(Constant.TIME_PER_QUESTION, Constant.COUNT_DOWN_TIMER);
            timer.start();
            question = battleQuestionList.get(questionIndex);
            int temp = questionIndex;
            imgQuestion.resetZoom();
            if (!question.getImage().isEmpty()) {
                imgZoom.setVisibility(View.VISIBLE);
                imgQuestion.setImageUrl(question.getImage(), imageLoader);
                imgQuestion.setVisibility(View.VISIBLE);
                tvIndex.setText(++temp + "/" + battleQuestionList.size());
                imgZoom.setOnClickListener(new View.OnClickListener() {
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
            options = new ArrayList<>();
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

            btnOpt1.setText(options.get(0).trim());
            btnOpt2.setText(options.get(1).trim());
            btnOpt3.setText(options.get(2).trim());
            btnOpt4.setText(options.get(3).trim());
            if (Session.getBoolean(Session.E_MODE, getApplicationContext()))
                if (options.size() == 5)
                    btnOpt5.setText(options.get(4).trim());

        }


    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setTelephoneListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    onPause();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onBackPressed() {
        showQuitGameAlertDialog();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.bookmark).setVisible(false);
        menu.findItem(R.id.report).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.setting:
                pauseCheck = "setting";
                Intent intent = new Intent(BattlePlayActivity.this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}