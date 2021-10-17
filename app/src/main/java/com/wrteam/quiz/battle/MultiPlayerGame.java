package com.wrteam.quiz.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.activity.SettingActivity;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.helper.CircleTimer;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.TouchImageView;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Question;
import com.wrteam.quiz.model.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MultiPlayerGame extends AppCompatActivity implements View.OnClickListener {
    public TextView tvAlert, tvTime, btnOpt1, btnOpt2, btnOpt3, btnOpt4, btnOpt5, tvRegQuestion;
    TextView txtQuestionIndex;
    public ArrayList<String> options;
    RecyclerView recyclerView;
    private final Handler mHandler = new Handler();
    public Question question;
    public Toolbar toolbar;
    public Animation RightSwipe_A, RightSwipe_B, RightSwipe_C, RightSwipe_D, RightSwipe_E, Fade_in;
    ImageView imgZoom;
    RelativeLayout layout_A, layout_B, layout_C, layout_D, layout_E;
    public RelativeLayout mainLayout;
    LinearLayout lytOption;
    TouchImageView imgQuestion;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private Animation animation;
    public long leftTime;
    public MyCountDownTimer timer;
    public AlertDialog quitAlertDialog;
    public Animation in, out, right_anim;
    public ProgressBar progressBar;
    public ScrollView mainScroll, queScroll;
    public int questionIndex = 0, correctQuestion = 0, inCorrectQuestion = 0, click = 0, textSize, preScore = 0;
    public String optionClicked = "false", winDialogTitle, pauseCheck = "regular";
    public ArrayList<Question> questionList;
    public String index = "index00";
    int attendedQue = 0;
    String roomId, roomKey, authId, from;
    Activity activity;
    DatabaseReference playerRef, gameUserRef, userScoreRef;
    ChildEventListener childUserScoreListener;
    ValueEventListener roomListener;
    public static ArrayList<Room> gameUserList;
    LinearLayout queLyt;
    boolean isDialogShow;

    @SuppressLint({"NewApi", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Utils.transparentStatusAndNavigation(MultiPlayerGame.this);
        setContentView(R.layout.activity_multi_player_game);
        activity = MultiPlayerGame.this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getAllWidgets();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setTelephoneListener();
        from = getIntent().getStringExtra("from");
        roomKey = getIntent().getStringExtra("roomKey");
        roomId = getIntent().getStringExtra("roomId");


        authId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        gameUserRef = FirebaseDatabase.getInstance().getReference(Constant.MULTIPLAYER_ROOM).child(roomId);

        userScoreRef = gameUserRef.child(Constant.joinUser);
        playerRef = userScoreRef.child(authId);


        if (Utils.isNetworkAvailable(activity)) {
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


        in = AnimationUtils.loadAnimation(this, R.anim.slide_up1);
        out = AnimationUtils.loadAnimation(this, R.anim.slide_up);

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

        checkGameRoomStatus();
    }


    private final Runnable mUpdateUITimerTask = new Runnable() {
        public void run() {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            nextQuizQuestion();
        }
    };


    public void getQuestionsFromJson() {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.accessKey, Constant.accessKeyValue);
        params.put(Constant.GET_QUES_BY_ROOM, "1");
        params.put(Constant.ROOM_ID, roomId);
        System.out.println("====battle params " + params.toString());

        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    System.out.println("====battle ques " + response);
                    questionList = new ArrayList<>();

                    try {

                        JSONObject jsonObject = new JSONObject(response);
                        String error = jsonObject.getString(Constant.ERROR);
                        if (error.equalsIgnoreCase("false")) {
                            JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                            System.out.println("====battle ques " + jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Question question = new Question();
                                JSONObject object = jsonArray.getJSONObject(i);

                                question.setId(Integer.parseInt(object.getString(Constant.ID)));
                                question.setQuestion(object.getString(Constant.QUESTION));
                                question.setImage(object.getString(Constant.IMAGE));
                                question.setQueType(object.getString(Constant.QUE_TYPE));
                                question.addOption(object.getString(Constant.OPTION_A).trim());
                                question.addOption(object.getString(Constant.OPTION_B).trim());
                                question.addOption(object.getString(Constant.OPTION_C).trim());
                                question.addOption(object.getString(Constant.OPTION_D).trim());

                                if (Session.getBoolean(Session.E_MODE, getApplicationContext())) {
                                    if (!object.getString(Constant.OPTION_E).trim().isEmpty() || !object.getString(Constant.OPTION_E).trim().equals(""))
                                        question.addOption(object.getString(Constant.OPTION_E).trim());
                                }
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
                                question.setLevel(object.getString(Constant.LEVEL));
                                question.setNote(object.getString(Constant.NOTE));
                                questionList.add(question);
                            }
                            timer = new MyCountDownTimer(Constant.GROUP_WAIT_TIME, Constant.COUNT_DOWN_TIMER);
                            //timer = new MyCountDownTimer(25000, Constant.COUNT_DOWN_TIMER);
                            timer.start();
                            getUserScoreList();
                            mHandler.postDelayed(mUpdateUITimerTask, 1000);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);

    }


    @SuppressLint("NonConstantResourceId")
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
            addScore();

        } else {
            layout.setBackgroundResource(R.drawable.wrong_gradient);
            inCorrectQuestion = inCorrectQuestion + 1;
            WrongQuestion();
        }

        question.setSelectedAns(tvBtnOpt.getText().toString());
        RightAnswerBackgroundSet();
        question.setAttended(true);
        attendedQue = (attendedQue + 1);
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


    public void DestroyKey() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.accessKey, Constant.accessKeyValue);
        params.put(Constant.DESTROY_ROOM_BY_ID, "1");
        params.put(Constant.ROOM_ID, roomId);
        ApiConfig.RequestToVolley((result, response) -> {
        }, params);
    }


    public void clearQuestionList() {
        if (questionList != null)
            questionList.clear();


    }

    public void ChangeTextSize(int size) {

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
        if (tvRegQuestion != null)
            tvRegQuestion.setTextSize(size);
    }

    private ViewSwitcher.ViewFactory mFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(activity);
            t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            t.setTextAppearance(activity, android.R.style.TextAppearance_Large);
            t.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
            return t;
        }
    };


    public class MyCountDownTimer extends CountDownTimer {

        private MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public void onTick(long millisUntilFinished) {
            leftTime = millisUntilFinished;
            // System.out.println("=======timer " + isPlayerLeft + " === " + millisUntilFinished);

            long totalSecs = (long) (int) (millisUntilFinished / 1000.0);
            long minutes = (totalSecs / 60);
            long seconds = totalSecs % 60;
            tvTime.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
           /* if (circleTimer == null) {
                circleTimer = findViewById(R.id.circleTimer);
            } else {
                circleTimer.setCurrentProgress(progress);
            }
            //when left last 5 second we show progress color red
            if (millisUntilFinished <= 6000) {
                circleTimer.SetTimerAttributes(Color.RED, Color.RED);
            } else {
                circleTimer.SetTimerAttributes(Color.parseColor(Constant.PROGRESS_COLOR), Color.parseColor(Constant.PROGRESS_COLOR));
            }*/
        }

        @Override
        public void onFinish() {
            goToResultPage();

        }
    }

    public void goToResultPage() {
        stopTimer();
        gameUserRef.removeEventListener(roomListener);
        userScoreRef.removeEventListener(childUserScoreListener);
        DestroyKey();
        Intent intent = new Intent(activity, BattleResultActivity.class);
        intent.putExtra("roomKey", roomKey);
        intent.putExtra("roomId", roomId);
        intent.putExtra("from", from);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        resetValues();
        gameUserRef.removeEventListener(roomListener);
        userScoreRef.removeEventListener(childUserScoreListener);
        System.out.println("===stop call");
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseCallActions();


        System.out.println("===pause call");
    }

    public void pauseCallActions() {

        if (pauseCheck.equals("regular")) {
            stopTimer();
            if (roomKey.equals(authId)) {
                gameUserRef.child(Constant.isRoomActive).setValue(Constant.FALSE);
            }
            playerRef.child(Constant.IS_LEAVE).setValue(Constant.TRUE);
            resetValues();
            finish();

        }

    }

    public void setTelephoneListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    pauseCallActions();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetValues();
        finish();
        System.out.println("====destroy call ");
    }

    public void resetValues() {

        clearQuestionList();
        if (quitAlertDialog != null) {
            if (quitAlertDialog.isShowing()) {
                quitAlertDialog.dismiss();
            }
        }
    }

    private void addScore() {
        rightSound();
        playerRef.child(Constant.RIGHT_ANS).setValue("" + correctQuestion);

    }

    private void WrongQuestion() {

        playWrongSound();
        playerRef.child(Constant.WRONG_ANS).setValue("" + inCorrectQuestion);

    }

    /*
     * Save score in Preferences
     */
    //play sound when answer is correct
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


    private void nextQuizQuestion() {
        queScroll.scrollTo(0,0);
        optionClicked = "false";
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
        tvRegQuestion.startAnimation(Fade_in);

        //hideShowProgress();
        if (questionIndex < questionList.size()) {
            question = questionList.get(questionIndex);
            int temp = (questionIndex + 1);
            imgQuestion.resetZoom();
            txtQuestionIndex.setText(temp + "/" + questionList.size());
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

            tvRegQuestion.setText(question.getQuestion());
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

        } else {
            tvAlert.setVisibility(View.VISIBLE);
            queLyt.setVisibility(View.GONE);
        }


    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }


    public void getAllWidgets() {
        toolbar = findViewById(R.id.toolBar);
        tvAlert = findViewById(R.id.tvAlert);
        tvTime = findViewById(R.id.tvTime);
        lytOption = findViewById(R.id.lytOption);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        int[] CLICKABLE = new int[]{R.id.a_layout, R.id.b_layout, R.id.c_layout, R.id.d_layout, R.id.e_layout};
        for (int i : CLICKABLE) {
            findViewById(i).setOnClickListener(this);
        }

        progressBar = findViewById(R.id.progressBar);
        txtQuestionIndex = findViewById(R.id.tvIndex);
        queLyt = findViewById(R.id.queLyt);

        mainScroll = findViewById(R.id.mainScroll);
        queScroll = findViewById(R.id.queScroll);
        imgQuestion = findViewById(R.id.imgQuestion);
        tvRegQuestion = findViewById(R.id.txtQuestion);

        btnOpt1 = findViewById(R.id.btnOpt1);
        btnOpt2 = findViewById(R.id.btnOpt2);
        btnOpt3 = findViewById(R.id.btnOpt3);
        btnOpt4 = findViewById(R.id.btnOpt4);
        btnOpt5 = findViewById(R.id.btnOpt5);

        imgZoom = findViewById(R.id.imgZoom);

        mainLayout = findViewById(R.id.main_layout);


        RightSwipe_A = AnimationUtils.loadAnimation(activity, R.anim.anim_right_a);
        RightSwipe_B = AnimationUtils.loadAnimation(activity, R.anim.anim_right_b);
        RightSwipe_C = AnimationUtils.loadAnimation(activity, R.anim.anim_right_c);
        RightSwipe_D = AnimationUtils.loadAnimation(activity, R.anim.anim_right_d);
        RightSwipe_E = AnimationUtils.loadAnimation(activity, R.anim.anim_right_e);
        Fade_in = AnimationUtils.loadAnimation(activity, R.anim.anim_right);
        right_anim = AnimationUtils.loadAnimation(activity, R.anim.anim_right);

        layout_A = findViewById(R.id.a_layout);
        layout_B = findViewById(R.id.b_layout);
        layout_C = findViewById(R.id.c_layout);
        layout_D = findViewById(R.id.d_layout);
        layout_E = findViewById(R.id.e_layout);

        textSize = Integer.parseInt(Session.getSavedTextSize(activity));
        ChangeTextSize(textSize);

        animation = AnimationUtils.loadAnimation(activity, R.anim.right_ans_anim); // Change alpha from fully visible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
    }

    public class UserScoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<Room> dataList;
        private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        public Context context;

        public UserScoreAdapter(Context context, ArrayList<Room> dataList) {
            this.dataList = dataList;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_player_score, parent, false);
            return new ItemRowHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder1, final int position) {
            Room user = dataList.get(position);
            ItemRowHolder holder = (ItemRowHolder) holder1;
            holder.tvName.setText(user.getName());
            holder.tvRight.setText(user.getRightAns());
            holder.tvWrong.setText(user.getWrongAns());
            holder.image.setImageUrl(user.getImage(), imageLoader);

            if (user.getIsLeave().equalsIgnoreCase(Constant.TRUE))
                holder.relativeLyt.setBackgroundResource(R.drawable.gray_rec_bg);
            else
                holder.relativeLyt.setBackgroundResource(R.drawable.gradient_cat_blue);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRight, tvWrong;
            CircleImageView image;
            RelativeLayout relativeLyt;

            public ItemRowHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvRight = itemView.findViewById(R.id.tvRight);
                tvWrong = itemView.findViewById(R.id.tvWrong);
                image = itemView.findViewById(R.id.image);
                relativeLyt = itemView.findViewById(R.id.relativeLyt);


            }
        }

    }

    public void getUserScoreList() {
        gameUserList = new ArrayList<>();
        final UserScoreAdapter adapter = new UserScoreAdapter(activity, gameUserList);
        recyclerView.setAdapter(adapter);

        childUserScoreListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Room book = dataSnapshot.getValue(Room.class);
                assert book != null;
                gameUserList.add(book);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Room book = dataSnapshot.getValue(Room.class);
                assert book != null;
                int index = -1;

                for (int i = 0; i < gameUserList.size(); i++) {
                    Room room = gameUserList.get(i);
                    String uID = room.getUID();
                    if (uID.equals(book.getUID())) {
                        index = i;
                        break;
                    }


                }
                if (index != -1) {
                    gameUserList.set(index, book);
                    adapter.notifyDataSetChanged();
                }

                if (verifyAllEqualUsingALoop(gameUserList)) {
                    goToResultPage();
                }
                if (totalActiveUser(gameUserList) == 1) {
                    allUserLeftGameAlert();
                }
                //A comment has changed, use the key t
                // o determine if we are displaying this
                //comment and if so displayed the changed comment.

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userScoreRef.addChildEventListener(childUserScoreListener);
    }

    public boolean verifyAllEqualUsingALoop(ArrayList<Room> list) {
        for (Room s : list) {
            if (s.getIsJoined().equalsIgnoreCase(Constant.TRUE) && s.getIsLeave().equalsIgnoreCase(Constant.FALSE)) {
                int userAttended = (Integer.parseInt(s.getRightAns()) + Integer.parseInt(s.getWrongAns()));
                if (userAttended != questionList.size())
                    return false;
            }
        }
        return true;
    }


    public int totalActiveUser(ArrayList<Room> list) {
        int activeUser = 0;
        for (Room s : list) {
            if (s.getIsJoined().equalsIgnoreCase(Constant.TRUE) && s.getIsLeave().equalsIgnoreCase(Constant.FALSE)) {
                activeUser++;
            }
        }
        return activeUser;
    }

    public void checkGameRoomStatus() {
        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Room roomData = snapshot.getValue(Room.class);
                    if (!roomKey.equals(authId)) {
                        assert roomData != null;
                        if (roomData.getIsRoomActive() != null)
                            if (roomData.getIsRoomActive().equalsIgnoreCase(Constant.FALSE))
                                if (!isDialogShow)
                                    showRoomDeActivateAlert();
                    }
                }
                //System.out.println("===call===");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        gameUserRef.addValueEventListener(roomListener);
    }

    public void showRoomDeActivateAlert() {
        isDialogShow = true;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.room_deactivate_alert));
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();
        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopTimer();
                gameUserRef.child(Constant.joinUser).child(authId).removeValue();
                gameUserRef.removeEventListener(roomListener);
                userScoreRef.removeEventListener(childUserScoreListener);
                alertDialog1.dismiss();
                finish();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }

    public void allUserLeftGameAlert() {
        stopTimer();
        isDialogShow = true;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.all_user_left_game));
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();
        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopTimer();
                updateUserStatus();
                alertDialog1.dismiss();
                gameUserRef.removeEventListener(roomListener);
                userScoreRef.removeEventListener(childUserScoreListener);
                finish();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }

    public void leaveGameDialog() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        if (roomKey.equals(authId)) {
            alertDialog.setMessage(getString(R.string.destroy_game_room_msg));
        } else {
            alertDialog.setMessage(getString(R.string.leave_game_room_msg));
        }
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();
        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.stay_back), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog1.dismiss();
            }
        });

        alertDialog.setNegativeButton(getString(R.string.leave), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopTimer();
                updateUserStatus();
                alertDialog1.dismiss();
                gameUserRef.removeEventListener(roomListener);
                userScoreRef.removeEventListener(childUserScoreListener);
                finish();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }

    public void updateUserStatus() {
        if (roomKey.equals(authId)) { // Room owner
            gameUserRef.child(Constant.isRoomActive).setValue(Constant.FALSE);

        } else { // other users

            playerRef.child(Constant.IS_LEAVE).setValue(Constant.TRUE);
        }
    }

    @Override
    public void onBackPressed() {
        leaveGameDialog();
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