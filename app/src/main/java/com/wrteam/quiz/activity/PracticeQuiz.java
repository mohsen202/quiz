package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.fragment.PracticeFragment;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PracticeQuiz extends AppCompatActivity {
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    public static ArrayList<Question> questionList;
    ImageView imgNext;
    ProgressBar progressBar;
    TextView questionNo;
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_quiz);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        viewPager = findViewById(R.id.viewPager);
        imgNext = findViewById(R.id.imgNext);
        toolbar = findViewById(R.id.toolBar);
        questionNo = findViewById(R.id.questionNo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.practice));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = findViewById(R.id.progressBar);
        getQuestionsFromJson();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (questionList != null) {
                    questionNo.setText(((position + 1) + "/" + questionList.size()));

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Utils.loadAd(PracticeQuiz.this);
    }

    public void PreviousQuestion(View view) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    public void NextQuestion(View view) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        if (viewPager.getCurrentItem() == (questionList.size() - 1)) {
            imgNext.setVisibility(View.GONE);
        } else {
            imgNext.setVisibility(View.VISIBLE);
        }
    }

    public void getQuestionsFromJson() {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.getSelfChallengeQuestions, "1");
        params.put(Constant.LIMIT, "" + Constant.PRACTICE_QUE_LIMIT);
        params.put(Constant.category, Constant.CATE_ID);

        if (Session.getBoolean(Session.LANG_MODE, getApplicationContext()))
            params.put(Constant.LANGUAGE_ID, Session.getCurrentLanguage(getApplicationContext()));
        //System.out.println("======params " + params.toString());
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    //System.out.println("==== practice " + response);
                    progressBar.setVisibility(View.VISIBLE);
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean(Constant.ERROR);
                    if (!error) {
                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                        questionList = new ArrayList<>();
                        questionList.addAll(Utils.getQuestions(jsonArray, PracticeQuiz.this));
                        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), questionList);
                        viewPager.setAdapter(viewPagerAdapter);
                        questionNo.setText(((viewPager.getCurrentItem() + 1) + "/" + questionList.size()));

                    }
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, params);

    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ArrayList<Question> questionList;

        public ViewPagerAdapter(FragmentManager fm, ArrayList<Question> questionList) {
            super(fm);
            this.questionList = questionList;
        }

        @Override
        public Fragment getItem(int position) {
            return PracticeFragment.newInstance(position, questionList);
        }

        @Override
        public int getCount() {
            return questionList.size();
        }

    }


    @SuppressLint("SetTextI18n")
    public void PlayAreaLeaveDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(PracticeQuiz.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_leave_test, null);
        dialog.setView(dialogView);
        TextView text = dialogView.findViewById(R.id.text);
        text.setText(R.string.levea_msg3);
        Button btnLeave = dialogView.findViewById(R.id.btnLeave);
        Button btnResume = dialogView.findViewById(R.id.btnResume);

        final AlertDialog alertDialog = dialog.create();
        Utils.setDialogBg(alertDialog);
        alertDialog.show();

        alertDialog.setCancelable(false);
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                finish();
            }
        });


        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
    }


    @Override
    protected void onPause() {

        AppController.StopSound();
        super.onPause();
    }

    @Override
    protected void onResume() {
        AppController.playSound();
        super.onResume();
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

    @Override
    public void onBackPressed() {
        PlayAreaLeaveDialog();
    }

}