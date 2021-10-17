package com.wrteam.quiz.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.TouchImageView;
import com.wrteam.quiz.model.Question;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class PracticeFragment extends Fragment {

    private ArrayList<Question> questionList;
    ArrayList<String> options;
    private static final String QUESTION_INDEX = "question_index";
    public ScrollView mainScroll, queScroll;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    TouchImageView imgQuestion;
    public RelativeLayout layout_A, layout_B, layout_C, layout_D, layout_E, noteLyt;

    public TextView tvSolution;
    public TextView txtQuestion, btnOpt1, btnOpt2, btnOpt3, btnOpt4, btnOpt5, tvExtraNote;
    ImageView imgZoom;
    int click = 0;
    AdView mAdView;
    AdRequest adRequest;

    public PracticeFragment() {
        // Required empty public constructor
    }


    public PracticeFragment(ArrayList<Question> questionList) {
        this.questionList = questionList;

    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        mAdView = view.findViewById(R.id.banner_AdView);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        btnOpt1 = view.findViewById(R.id.btnOpt1);
        btnOpt2 = view.findViewById(R.id.btnOpt2);
        btnOpt3 = view.findViewById(R.id.btnOpt3);
        btnOpt4 = view.findViewById(R.id.btnOpt4);
        btnOpt5 = view.findViewById(R.id.btnOpt5);
        txtQuestion = view.findViewById(R.id.txtQuestion);

        tvExtraNote = view.findViewById(R.id.tvExtraNote);
        tvSolution = view.findViewById(R.id.tvSolution);

        imgQuestion = view.findViewById(R.id.imgQuestion);

        imgZoom = view.findViewById(R.id.imgZoom);
        mainScroll = view.findViewById(R.id.mainScroll);
        queScroll = view.findViewById(R.id.queScroll);
        noteLyt = view.findViewById(R.id.noteLyt);
        layout_A = view.findViewById(R.id.a_layout);
        layout_B = view.findViewById(R.id.b_layout);
        layout_C = view.findViewById(R.id.c_layout);
        layout_D = view.findViewById(R.id.d_layout);
        layout_E = view.findViewById(R.id.e_layout);
        assert getArguments() != null;
        final Question question = questionList.get(getArguments().getInt(QUESTION_INDEX));

        mainScroll.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.findViewById(R.id.queScroll).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        queScroll.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                //Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        imgQuestion.resetZoom();
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
            btnOpt1.setGravity(Gravity.NO_GRAVITY);
            btnOpt2.setGravity(Gravity.NO_GRAVITY);
        }
        if (Session.getBoolean(Session.E_MODE, getActivity())) {
            if (options.size() == 4)
                layout_E.setVisibility(View.GONE);
            else
                layout_E.setVisibility(View.VISIBLE);

        }
        btnOpt1.setText(Html.fromHtml(options.get(0).trim()));
        btnOpt2.setText(Html.fromHtml(options.get(1).trim()));
        btnOpt3.setText(Html.fromHtml(options.get(2).trim()));
        btnOpt4.setText(Html.fromHtml(options.get(3).trim()));
        if (Session.getBoolean(Session.E_MODE, getActivity())) {
            if (options.size() == 5)
                btnOpt5.setText(Html.fromHtml(options.get(4).trim()));

        }
        layout_A.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        layout_B.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        layout_C.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        layout_D.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        layout_E.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));

        if (question.getNote().isEmpty()) {
            noteLyt.setVisibility(View.GONE);
        } else {
            noteLyt.setVisibility(View.VISIBLE);
        }

        tvSolution.setVisibility(View.GONE);
        tvExtraNote.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0);
        tvExtraNote.setTag("up");
        tvExtraNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String solution = question.getNote();
                if (tvExtraNote.getTag().equals("up")) {
                    tvExtraNote.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up, 0);
                    tvSolution.setVisibility(View.VISIBLE);
                    tvSolution.setText(Html.fromHtml(solution));
                    tvExtraNote.setTag("down");
                } else {
                    tvExtraNote.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0);
                    tvSolution.setVisibility(View.GONE);
                    tvExtraNote.setTag("up");
                }

            }
        });


        if (!question.getImage().isEmpty()) {
            imgZoom.setVisibility(View.VISIBLE);
            imgQuestion.setImageUrl(question.getImage(), imageLoader);
            imgQuestion.setVisibility(View.VISIBLE);

            imgQuestion.setImageUrl(question.getImage(), imageLoader);
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

        String wrongAns;
        if (question.getSelectedAns() != null) {
            wrongAns = question.getSelectedAns().trim();
        } else {
            wrongAns = "";
        }

        if (btnOpt1.getText().toString().equalsIgnoreCase(wrongAns)) {
            layout_A.setBackgroundResource(R.drawable.wrong_gradient);
        } else if (btnOpt2.getText().toString().equalsIgnoreCase(wrongAns)) {
            layout_B.setBackgroundResource(R.drawable.wrong_gradient);
        } else if (btnOpt3.getText().toString().equalsIgnoreCase(wrongAns)) {
            layout_C.setBackgroundResource(R.drawable.wrong_gradient);
        } else if (btnOpt4.getText().toString().equalsIgnoreCase(wrongAns)) {
            layout_D.setBackgroundResource(R.drawable.wrong_gradient);
        } else if (btnOpt5.getText().toString().equalsIgnoreCase(wrongAns)) {
            layout_E.setBackgroundResource(R.drawable.wrong_gradient);
        }
        RightAnswerBackgroundSet(question);
        queScroll.scrollTo(0,0);


        return view;
    }




    public void RightAnswerBackgroundSet(Question question) {
        if (btnOpt1.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_A.setBackgroundResource(R.drawable.right_gradient);


        } else if (btnOpt2.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_B.setBackgroundResource(R.drawable.right_gradient);


        } else if (btnOpt3.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_C.setBackgroundResource(R.drawable.right_gradient);


        } else if (btnOpt4.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_D.setBackgroundResource(R.drawable.right_gradient);

        } else if (btnOpt5.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_E.setBackgroundResource(R.drawable.right_gradient);

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static PracticeFragment newInstance(int sectionNumber, ArrayList<Question> questionList) {
        PracticeFragment fragment = new PracticeFragment(questionList);
        Bundle args = new Bundle();
        args.putInt(QUESTION_INDEX, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
}