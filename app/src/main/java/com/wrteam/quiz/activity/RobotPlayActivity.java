package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.toolbox.ImageLoader;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
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


public class RobotPlayActivity extends AppCompatActivity implements View.OnClickListener {
    public static TextView btnOpt1, btnOpt2, btnOpt3, btnOpt4, btnOpt5, txtQuestion,
            p2ans_a, p2ans_b, p2ans_c, p2ans_d, p2ans_e, tvPlayer1Name, tvPlayer2Name, btnQuitGame,tvIndex;
    public static ArrayList<String> options;
    public static Boolean virtual_play = false;
    public Question question;

    public RelativeLayout playLayout;
    public Animation RightSwipe_A, RightSwipe_B, RightSwipe_C, RightSwipe_D, RightSwipe_E, Fade_in;
    CircleImageView imgPlayer1, imgPlayer2;
    ImageView imgZoom;
    private final Handler mHandler = new Handler();

    RelativeLayout layout_A, layout_B, layout_C, layout_D, layout_E;
    ArrayList<Question> questionList;
    TouchImageView imgQuestion;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    private Activity activity;
    private Animation animation;


    public long leftTime;
    public MyCountDownTimer timer;
    public AlertDialog quitAlertDialog;
    public TextSwitcher right_p1, right_p2, right_p01, right_p02;
    public Animation in, out;
    public ProgressBar progressBar;

    public ScrollView mainScroll, queScroll;
    public int questionIndex = 0, correctQuestion = 0, inCorrectQuestion = 0,
            questionIndex_vplayer = 0, correctQuestion_vplayer = 0, inCorrectQuestion_vplayer = 0,
            click = 0, textSize, preScore = 0;
    public String Player1Name, Player2Name, gameId, winner, winnerMessage,
            player1Key = "", player2Key = "", battlePlayer, optionClicked = "false",
            tts = "tts", profilePlayer1, profilePlayer2, winDialogTitle, pauseCheck = "regular";
    public static CircleTimer progressTimer;
    AdView mAdView;
    AdRequest adRequest;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity = RobotPlayActivity.this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gameId = getIntent().getStringExtra("gameid");
        battlePlayer = getIntent().getStringExtra("battlePlayer");

        int[] CLICKABLE = new int[]{R.id.a_layout, R.id.b_layout, R.id.c_layout, R.id.d_layout, R.id.e_layout};
        for (int i : CLICKABLE) {
            findViewById(i).setOnClickListener(this);
        }
        mAdView = findViewById(R.id.banner_AdView);
        adRequest = new AdRequest.Builder().build();

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

        tvIndex = findViewById(R.id.tvIndex);
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
        textSize = Integer.parseInt(Session.getSavedTextSize(activity));
        Session.removeSharedPreferencesData(activity);

        RightSwipe_A = AnimationUtils.loadAnimation(activity, R.anim.anim_right_a);
        RightSwipe_B = AnimationUtils.loadAnimation(activity, R.anim.anim_right_b);
        RightSwipe_C = AnimationUtils.loadAnimation(activity, R.anim.anim_right_c);
        RightSwipe_D = AnimationUtils.loadAnimation(activity, R.anim.anim_right_d);
        RightSwipe_E = AnimationUtils.loadAnimation(activity, R.anim.anim_right_e);
        Fade_in = AnimationUtils.loadAnimation(activity, R.anim.fade_out);


        playLayout = findViewById(R.id.innerLayout);
        playLayout.setVisibility(View.GONE);


        txtQuestion = findViewById(R.id.txtQuestion);
        layout_A = findViewById(R.id.a_layout);
        layout_B = findViewById(R.id.b_layout);
        layout_C = findViewById(R.id.c_layout);
        layout_D = findViewById(R.id.d_layout);
        layout_E = findViewById(R.id.e_layout);


        ChangeTextSize(textSize);
        animation = AnimationUtils.loadAnimation(activity, R.anim.right_ans_anim); // Change alpha from fully visible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
        progressTimer = findViewById(R.id.circleTimer);
        progressTimer.setMaxProgress(Constant.CIRCULAR_MAX_PROGRESS);
        progressTimer.setCurrentProgress(Constant.CIRCULAR_MAX_PROGRESS);
        if (Utils.isNetworkAvailable(activity)) {


            init();
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

        virtual_play = true;
        Player2Name = getString(R.string.robot);
        tvPlayer2Name.setText(Player2Name);
        imgPlayer2.setDefaultImageResId(R.drawable.ic_android);
        p2ans_a.setText(Player2Name);
        p2ans_b.setText(Player2Name);
        p2ans_c.setText(Player2Name);
        p2ans_d.setText(Player2Name);
        p2ans_e.setText(Player2Name);
        getQuestionForComputer();
    }

    public void getQuestionForComputer() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.getQuestionForRobot, "1");
        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext()))
            params.put(Constant.LANGUAGE_ID, Session.getCurrentLanguage(getApplicationContext()));
        if (Constant.isCateEnable)
            params.put(Constant.category, Constant.CATE_ID);
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        System.out.println("Responsee::="+response);
                        String error = jsonObject.getString(Constant.ERROR);
                        if (error.equalsIgnoreCase("false")) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            questionList = new ArrayList<>();
                            questionList.addAll(Utils.getQuestions(jsonArray, activity));
                            Constant.MAX_QUESTION_PER_BATTLE = questionList.size();
                            playLayout.setVisibility(View.VISIBLE);
                            mHandler.postDelayed(mUpdateUITimerTask, 1000);
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
        if (questionIndex < questionList.size()) {
            question = questionList.get(questionIndex);
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

            if (virtual_play) {
                PerformVirtualClick();
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

    private void showQuitGameAlertDialog() {
        try {
            stopTimer();
            final AlertDialog.Builder dialog1 = new AlertDialog.Builder(activity);
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


    @SuppressLint("SetTextI18n")
    private void showWinnerDialog() {
        try {

            stopTimer();


            final AlertDialog.Builder dialog1 = new AlertDialog.Builder(activity);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View sheetView = inflater.inflate(R.layout.bottom_complete, null);
            dialog1.setView(sheetView);
            dialog1.setCancelable(false);

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
            if (winner.equals(getString(R.string.you))) {
                txt_result_title.setText(getString(R.string.congrats));
                winnerstatus.setText(R.string.winner);
                wrong.setText(Player1Name);
                tvScore.setText(Player2Name);
                victorymsg.setText(R.string.victory);
                victoryimg.setBackgroundResource(R.drawable.ic_victory);
                player1.setImageUrl(profilePlayer1, imageLoader);
                player2.setDefaultImageResId(R.drawable.ic_android);
                looserstatus.setText(R.string.youloss);
            } else {
                txt_result_title.setText(getString(R.string.next_time));
                winnerstatus.setText(R.string.youloss);
                wrong.setText(Player1Name);
                tvScore.setText(Player2Name);
                victorymsg.setText(R.string.defeat);
                victoryimg.setBackgroundResource(R.drawable.ic_defeat);
                player1.setImageUrl(profilePlayer1, imageLoader);
                player2.setDefaultImageResId(R.drawable.ic_android);
                looserstatus.setText(R.string.winner);
            }


            tvPlayNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intentReBattle = new Intent(activity, SearchPlayerActivity.class);
                    intentReBattle.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentReBattle);
                    alertDialog.dismiss();
                    finish();
                }
            });
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
             /*       myGameRef.child(player1Key).removeValue();
                    resetValues();*/
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


    public void PerformVirtualClick() {
        String option;
        if (Session.getBoolean(Session.E_MODE, getApplicationContext())) {
            option = randomAlphaNumericWith_E();
        } else {
            option = randomAlphaNumeric();
        }
        switch (option) {
            case "A":

                if (btnOpt1.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt1.getText().toString().trim());
                else if (!btnOpt1.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt1.getText().toString().trim());

                break;
            case "B":

                if (btnOpt2.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt2.getText().toString().trim());
                else if (!btnOpt2.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt2.getText().toString().trim());

                break;
            case "C":

                if (btnOpt3.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt3.getText().toString().trim());
                else if (!btnOpt3.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt3.getText().toString().trim());

                break;
            case "D":

                if (btnOpt4.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt4.getText().toString().trim());
                else if (!btnOpt4.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt4.getText().toString().trim());

                break;
            case "E":

                if (btnOpt5.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt5.getText().toString().trim());
                else if (!btnOpt5.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt5.getText().toString().trim());

                break;
        }
    }


    private void showResetGameAlert() {

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

    public String randomAlphaNumeric() {

        String option = "ABCD";
        int character = (int) (Math.random() * 4);
        return String.valueOf(option.charAt(character));
    }

    public String randomAlphaNumericWith_E() {

        String option = "ABCDE";
        int character = (int) (Math.random() * 5);
        return String.valueOf(option.charAt(character));
    }

    private ViewSwitcher.ViewFactory mFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(activity);
            t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            t.setTextAppearance(activity, android.R.style.TextAppearance_DeviceDefault_Medium);
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
            if (questionIndex >= questionList.size()) {

            } else {
                //WrongQuestion();
                if (optionClicked.equals("false")) {
                    layout_A.setClickable(false);
                    layout_B.setClickable(false);
                    layout_C.setClickable(false);
                    layout_D.setClickable(false);
                    layout_E.setClickable(false);
                    if (virtual_play) {
                        WrongVirtualAnswer("wrong");
                    }
                    WrongQuestion("wrong");
                    questionIndex++;
                    mHandler.postDelayed(mUpdateUITimerTask, 1000);

                }
            }

        }
    }


    public void RightVirtualAnswer(final String sel_ans) {
        questionIndex_vplayer++;
        correctQuestion_vplayer++;
        right_p2.setText(String.valueOf(correctQuestion_vplayer));
        showRobotAnswer(sel_ans);

    }

    public void showRobotAnswer(String selectedAns) {
        if (btnOpt1.getText().toString().equalsIgnoreCase(selectedAns)) {
            p2ans_a.setVisibility(View.VISIBLE);
            assert selectedAns != null;
            if (selectedAns.equalsIgnoreCase(question.getTrueAns())) {
                p2ans_a.setTextColor(getResources().getColor(R.color.wrong_dark));
            } else {
                p2ans_a.setTextColor(getResources().getColor(R.color.right_dark));
            }

        } else if (btnOpt2.getText().toString().equalsIgnoreCase(selectedAns)) {
            p2ans_b.setVisibility(View.VISIBLE);
            assert selectedAns != null;
            if (selectedAns.equalsIgnoreCase(question.getTrueAns())) {
                p2ans_b.setTextColor(getResources().getColor(R.color.wrong_dark));
            } else {
                p2ans_b.setTextColor(getResources().getColor(R.color.right_dark));
            }

        } else if (btnOpt3.getText().toString().equalsIgnoreCase(selectedAns)) {
            p2ans_c.setVisibility(View.VISIBLE);
            assert selectedAns != null;
            if (selectedAns.equalsIgnoreCase(question.getTrueAns())) {
                p2ans_c.setTextColor(getResources().getColor(R.color.wrong_dark));
            } else {
                p2ans_c.setTextColor(getResources().getColor(R.color.right_dark));
            }

        } else if (btnOpt4.getText().toString().equalsIgnoreCase(selectedAns)) {
            p2ans_d.setVisibility(View.VISIBLE);
            assert selectedAns != null;
            if (selectedAns.equalsIgnoreCase(question.getTrueAns())) {
                p2ans_d.setTextColor(getResources().getColor(R.color.wrong_dark));
            } else {
                p2ans_d.setTextColor(getResources().getColor(R.color.right_dark));
            }

        } else if (btnOpt5.getText().toString().equalsIgnoreCase(selectedAns)) {
            p2ans_e.setVisibility(View.VISIBLE);
            assert selectedAns != null;
            if (selectedAns.equalsIgnoreCase(question.getTrueAns())) {
                p2ans_e.setTextColor(getResources().getColor(R.color.wrong_dark));
            } else {
                p2ans_e.setTextColor(getResources().getColor(R.color.right_dark));
            }

        }
    }

    public void WrongVirtualAnswer(final String sel_ans) {

        inCorrectQuestion_vplayer++;
        questionIndex_vplayer++;
        showRobotAnswer(sel_ans);
    }

    private void addScore(final String sel_ans) {
        rightSound();
        if (correctQuestion == questionList.size()) {
            right_p01.setText("");
        }
        right_p1.setText(String.valueOf(correctQuestion));
    }

    private void WrongQuestion(final String sel_ans) {
        setAgain();
        playWrongSound();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();

    }

    @Override
    protected void onDestroy() {

        stopTimer();
        if (quitAlertDialog != null) {
            if (quitAlertDialog.isShowing()) {
                quitAlertDialog.dismiss();
            }
        }
        super.onDestroy();

    }


    public void rightSound() {
        if (Session.getSoundEnableDisable(activity)) {
            Utils.setrightAnssound(activity);
        }
        if (Session.getVibration(activity)) {
            Utils.vibrate(activity, Utils.VIBRATION_DURATION);
        }
    }

    //play sound when answer is incorrect
    private void playWrongSound() {
        if (Session.getSoundEnableDisable(activity)) {
            Utils.setwronAnssound(activity);
        }
        if (Session.getVibration(activity)) {
            Utils.vibrate(activity, Utils.VIBRATION_DURATION);
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
        if (questionIndex >= questionList.size()) {
            showBattleResult();
        }
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
        if (questionIndex < questionList.size()) {
            mAdView.loadAd(adRequest);
            timer = new MyCountDownTimer(Constant.TIME_PER_QUESTION, Constant.COUNT_DOWN_TIMER);
            timer.start();
            question = questionList.get(questionIndex);
            int temp = questionIndex;
            imgQuestion.resetZoom();
            tvIndex.setText(++temp + "/" + questionList.size());
            if (!question.getImage().isEmpty()) {
                imgZoom.setVisibility(View.VISIBLE);


                imgQuestion.setImageUrl(question.getImage(), imageLoader);
                imgQuestion.setVisibility(View.VISIBLE);

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

            btnOpt1.setText(options.get(0).trim());
            btnOpt2.setText(options.get(1).trim());
            btnOpt3.setText(options.get(2).trim());
            btnOpt4.setText(options.get(3).trim());
            if (Session.getBoolean(Session.E_MODE, getApplicationContext()))
                if (options.size() == 5)
                    btnOpt5.setText(options.get(4).trim());

        }


    }

    public void showBattleResult() {
        stopTimer();
        leftTime = 0;

        if (correctQuestion > correctQuestion_vplayer) {
            winnerMessage = Player1Name + getString(R.string.msg_win_battle);
            winner = "you";
            winDialogTitle = getString(R.string.congrats);
            showWinnerDialog();

        } else if (correctQuestion_vplayer > correctQuestion) {
            winnerMessage = Player2Name + getString(R.string.msg_opponent_win_battle);
            winner = Player2Name;
            winDialogTitle = getString(R.string.next_time);
            showWinnerDialog();
        } else {
            showResetGameAlert();

        }


    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void CheckSound() {
        if (Session.getSoundEnableDisable(activity)) {
            Utils.backSoundonclick(activity);
        }
        if (Session.getVibration(activity)) {
            Utils.vibrate(activity, Utils.VIBRATION_DURATION);
        }
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
                Intent intent = new Intent(activity, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}