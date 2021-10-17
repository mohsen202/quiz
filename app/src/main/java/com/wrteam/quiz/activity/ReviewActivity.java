package com.wrteam.quiz.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import com.wrteam.quiz.fragment.ReviewFragment;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.model.Question;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wrteam.quiz.R;

import com.wrteam.quiz.Constant;
import com.wrteam.quiz.helper.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ReviewActivity extends AppCompatActivity {
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    public ImageView prev, next;
    public ArrayList<Question> reviews;
    public Toolbar toolbar;
    public Menu menu;
    AlertDialog alertDialog;
    String from;
    TextView questionNo;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_review);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.review_answer));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        from = getIntent().getStringExtra("from");
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        questionNo = findViewById(R.id.questionNo);
        if (from.equals("regular")) {
            reviews = PlayActivity.questionList;
        } /*else if (from.equals("daily") || from.equals("random") || from.equals("true_false")) {
            reviews = DailyActivity.questionList;
        }*/ else
            reviews = SelfChallengeQuestion.questionList;

        viewPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), reviews);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                questionNo.setText(((position+1)+"/"+reviews.size()));
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        questionNo.setText(((viewPager.getCurrentItem()+1)+"/"+reviews.size()));
        Utils.displayInterstitial(ReviewActivity.this);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });


    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ArrayList<Question> questionList;

        public ViewPagerAdapter(FragmentManager fm, ArrayList<Question> questionList) {
            super(fm);
            this.questionList = questionList;
        }

        @Override
        public Fragment getItem(int position) {
            return ReviewFragment.newInstance(position, questionList);
        }

        @Override
        public int getCount() {
            return questionList.size();
        }

    }

    public void ReportDialog(final Question question) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ReviewActivity.this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.report_dialog, null);
        dialog.setView(dialogView);
        TextView tvReport = dialogView.findViewById(R.id.tvReport);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        final EditText edtReport = dialogView.findViewById(R.id.edtReport);

        TextView tvQuestion = dialogView.findViewById(R.id.tvQuestion);
        tvQuestion.setText("Que : " + Html.fromHtml(question.getQuestion()));
        alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCancelable(true);
        tvReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!edtReport.getText().toString().isEmpty()) {
                    ReportQuestion(edtReport.getText().toString(), String.valueOf(question.getId()));
                    edtReport.setError(null);
                } else {
                    edtReport.setError("Please fill all the data and submit!");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void ReportQuestion(final String message, final String qId) {
        Map<String, String> params = new HashMap<>();

        params.put(Constant.reportQuestion, "1");
        params.put(Constant.questionId, qId);
        params.put(Constant.userId, Session.getUserData(Session.USER_ID, getApplicationContext()));
        params.put(Constant.messageReport, message);
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {

                        JSONObject obj = new JSONObject(response);
                        boolean error = obj.getBoolean("error");
                        String message = obj.getString("message");
                        if (!error) {
                            alertDialog.dismiss();
                        }
                        Toast.makeText(ReviewActivity.this, message, Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);

    }

    public void SetMark(final String like, final String id) {
        // progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();

        params.put(Constant.SetBookmark, "1");
        params.put(Constant.status, like);
        params.put(Constant.Question_Id, id);
        params.put(Constant.userId, Session.getUserData(Session.USER_ID, ReviewActivity.this));
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {

                }
            }
        }, params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.setting).setVisible(false);
        final MenuItem menuItem = menu.findItem(R.id.bookmark);
        menuItem.setTitle("unmark");
        if (Session.getBooleanValue(getApplicationContext(), "question_" + reviews.get(viewPager.getCurrentItem()).getId())) {
            menuItem.setIcon(R.drawable.ic_mark);
            menuItem.setTitle("mark");
        } else {
            menuItem.setIcon(R.drawable.ic_unmark);
            menuItem.setTitle("unmark");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.bookmark:
                Question review = reviews.get(viewPager.getCurrentItem());
                ArrayList<String> options = reviews.get(viewPager.getCurrentItem()).getOptions();
                if (!Session.getBooleanValue(getApplicationContext(), "question_" + review.getId())) {
                    SetMark("1", String.valueOf(review.getId()));
                    menuItem.setIcon(R.drawable.ic_mark);
                    menuItem.setTitle("mark");
                    Session.setMark(getApplicationContext(), "question_" + review.getId(), true);
                } else {
                    SetMark("0", String.valueOf(review.getId()));
                    menuItem.setIcon(R.drawable.ic_unmark);
                    menuItem.setTitle("unmark");
                    Session.setMark(getApplicationContext(), "question_" + review.getId(), false);
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.report:
                ReportDialog(reviews.get(viewPager.getCurrentItem()));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}