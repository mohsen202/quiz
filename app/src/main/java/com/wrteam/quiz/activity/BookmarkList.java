package com.wrteam.quiz.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import com.google.android.material.snackbar.Snackbar;
import com.wrteam.quiz.AdUtils;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.UnifiedNativeAdViewHolder;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Category;
import com.wrteam.quiz.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookmarkList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    TextView tvNoBookmarked;
    public static ArrayList<Question> bookmarks;
    ProgressBar imgProgress, rightProgress, wrongProgress, progressBar;
    TextView btnPlay;
    public RelativeLayout mainLayout;
    BookMarkAdapter bookMarkAdapter;
    public Toolbar toolbar;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_list);
        mainLayout = findViewById(R.id.mainLayout);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity = BookmarkList.this;
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.bookmark_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnPlay = findViewById(R.id.btnPlay);
        progressBar = findViewById(R.id.progressBar);
        tvNoBookmarked = findViewById(R.id.emptyMsg);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        //when bookmark note available show message
 /*       BookMarkAdapter adapter = new BookMarkAdapter(getApplicationContext(), bookmarks);
        recyclerView.setAdapter(adapter);*/
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playIntent = new Intent(BookmarkList.this, BookmarkPlay.class);
                startActivity(playIntent);
            }
        });

        getQuestionsFromJson();

    }

    public void setSnackBar() {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getQuestionsFromJson();
                    }
                });

        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }


    public void getQuestionsFromJson() {
        if (Utils.isNetworkAvailable(BookmarkList.this)) {
            progressBar.setVisibility(View.VISIBLE);
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GetBookmark, "1");
            params.put(Constant.userId, Session.getUserData(Session.USER_ID, BookmarkList.this));
            ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    //System.out.println("========search result " + response);
                    if (result) {
                        try {
                            //System.out.println("====" + response);
                            progressBar.setVisibility(View.VISIBLE);
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean(Constant.ERROR);
                            if (!error) {
                                JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);

                                bookmarks = new ArrayList<>();
                                if (jsonArray.length() < 0) {
                                    tvNoBookmarked.setVisibility(View.VISIBLE);
                                    btnPlay.setVisibility(View.GONE);
                                } else {
                                    btnPlay.setVisibility(View.VISIBLE);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Question question = new Question();
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        question.setId(Integer.parseInt(object.getString(Constant.ID)));
                                        question.setQuestion(object.getString(Constant.QUESTION));
                                        question.setImage(object.getString(Constant.IMAGE));
                                        question.addOption(object.getString(Constant.OPTION_A).trim());
                                        question.addOption(object.getString(Constant.OPTION_B).trim());
                                        question.addOption(object.getString(Constant.OPTION_C).trim());
                                        question.addOption(object.getString(Constant.OPTION_D).trim());
                                        question.setQueType(object.getString(Constant.QUE_TYPE));
                                        if (Session.getBoolean(Session.E_MODE, getApplicationContext())) {
                                            if (!object.getString(Constant.OPTION_E).isEmpty() || !object.getString(Constant.OPTION_E).equals(""))
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
                              /*          if (i != 0 && i % 5 == 0) {
                                            bookmarks.add(new Question(true));
                                        }*/
                                        bookmarks.add(question);

                                    }
                                    bookMarkAdapter = new BookMarkAdapter(BookmarkList.this, bookmarks);
                                    recyclerView.setAdapter(bookMarkAdapter);
                                    progressBar.setVisibility(View.GONE);
                                }


                            } else {
                                progressBar.setVisibility(View.GONE);
                                tvNoBookmarked.setVisibility(View.VISIBLE);
                                btnPlay.setVisibility(View.GONE);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, params);

        } else {
            setSnackBar();
        }
    }

    public void replayforum(final String like, final String id) {
        // progressBar.setVisibility(View.VISIBLE);
        if (Utils.isNetworkAvailable(BookmarkList.this)) {
            progressBar.setVisibility(View.VISIBLE);

            Map<String, String> params = new HashMap<>();
            params.put(Constant.SetBookmark, "1");
            params.put(Constant.status, like);
            params.put(Constant.Question_Id, id);
            params.put(Constant.userId, Session.getUserData(Session.USER_ID, BookmarkList.this));
            ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    // System.out.println("========search result " + response);
                    if (result) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean(Constant.ERROR);

                            if (!error) {
                                String message = jsonObject.getString("message");
                                progressBar.setVisibility(View.GONE);
                            }
                            progressBar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, params);

        } else {
            setSnackBar();
        }
    }



    public class BookMarkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<Question> bookmarks;
        private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        private final int MENU_ITEM_VIEW_TYPE = 0;
        private final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

        public BookMarkAdapter(Context context, ArrayList<Question> bookmarks) {
            this.bookmarks = bookmarks;

        }
/*
        @Override
        public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_layout, parent, false);
            return new ItemRowHolder(v);
        }*/

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case UNIFIED_NATIVE_AD_VIEW_TYPE:
                    View unifiedNativeLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_unified_rec, parent, false);
                    return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
                case MENU_ITEM_VIEW_TYPE:
                default:
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_layout, parent, false);
                    return new ItemRowHolder(v);
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder1, int position) {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case MENU_ITEM_VIEW_TYPE:
                    ItemRowHolder holder = (ItemRowHolder) holder1;
                    final Question bookmark = bookmarks.get(position);

                    if (position % 6 == 0) {
                        holder.lytBookmark.setBackgroundResource(R.drawable.blue_white_bg);
                    } else if (position % 6 == 1) {
                        holder.lytBookmark.setBackgroundResource(R.drawable.purple_white_bg);
                    } else if (position % 6 == 2) {
                        holder.lytBookmark.setBackgroundResource(R.drawable.pink_white_bg);
                    } else if (position % 6 == 3) {
                        holder.lytBookmark.setBackgroundResource(R.drawable.green_white_bg);
                    } else if (position % 6 == 4) {
                        holder.lytBookmark.setBackgroundResource(R.drawable.orange_white_bg);
                    } else if (position % 6 == 5) {
                        holder.lytBookmark.setBackgroundResource(R.drawable.sky_white_bg);
                    }

                    holder.tvNo.setText((position + 1) + "");
                    holder.tvQue.setText(Html.fromHtml(bookmark.getQuestion()));
                    holder.tvAns.setText(getString(R.string.answerbookmark) + " " + Html.fromHtml(bookmark.getTrueAns()));
                    holder.tvNote.setText(getString(R.string.extra_note) + " " + Html.fromHtml(bookmark.getNote()));
                    holder.remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            replayforum("0", String.valueOf(bookmark.getId()));
                            Session.setMark(getApplicationContext(), "question_" + bookmark.getId(), false);
                            bookmarks.remove(position);
                            notifyDataSetChanged();
                            if (bookmarks.size() == 0) {
                                tvNoBookmarked.setVisibility(View.VISIBLE);
                                btnPlay.setVisibility(View.GONE);
                            }
                        }
                    });

                    if (!bookmark.getImage().isEmpty()) {
                        holder.imgQuestion.setVisibility(View.VISIBLE);
                        holder.imgQuestion.setImageUrl(bookmark.getImage(), imageLoader);
                    }
                    if (bookmark.getNote().isEmpty())
                        holder.tvNote.setVisibility(View.GONE);
                    else
                        holder.tvNote.setVisibility(View.VISIBLE);

                    break;
            }
        }


/*
        @Override
        public void onBindViewHolder(@NonNull ItemRowHolder holder, final int position) {


            final Question bookmark = bookmarks.get(position);

            if (position % 6 == 0) {
                holder.lytBookmark.setBackgroundResource(R.drawable.blue_white_bg);
            } else if (position % 6 == 1) {
                holder.lytBookmark.setBackgroundResource(R.drawable.purple_white_bg);
            }else if(position%6==2){
                holder.lytBookmark.setBackgroundResource(R.drawable.pink_white_bg);
            }else if(position%6==3){
                holder.lytBookmark.setBackgroundResource(R.drawable.green_white_bg);
            }else if(position%6==4){
                holder.lytBookmark.setBackgroundResource(R.drawable.orange_white_bg);
            }else if(position%6==5){
                holder.lytBookmark.setBackgroundResource(R.drawable.sky_white_bg);
            }

            holder.tvNo.setText((position + 1) + "");
            holder.tvQue.setText(Html.fromHtml(bookmark.getQuestion()));
            holder.tvAns.setText(getString(R.string.answerbookmark) + " " + Html.fromHtml(bookmark.getTrueAns()));
            holder.tvNote.setText(getString(R.string.extra_note) + " " + Html.fromHtml(bookmark.getNote()));
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    replayforum("0", String.valueOf(bookmark.getId()));
                    Session.setMark(getApplicationContext(), "question_" + bookmark.getId(), false);
                    bookmarks.remove(position);
                    notifyDataSetChanged();
                    if (bookmarks.size() == 0) {
                        tvNoBookmarked.setVisibility(View.VISIBLE);
                        btnPlay.setVisibility(View.GONE);
                    }
                }
            });

            if (!bookmark.getImage().isEmpty()) {
                holder.imgQuestion.setVisibility(View.VISIBLE);
                holder.imgQuestion.setImageUrl(bookmark.getImage(), imageLoader);
            }
            if (bookmark.getNote().isEmpty())
                holder.tvNote.setVisibility(View.GONE);
            else
                holder.tvNote.setVisibility(View.VISIBLE);


        }
*/

        @Override
        public int getItemCount() {
            return bookmarks.size();
        }


        @Override
        public int getItemViewType(int position) {
            if (bookmarks.get(position).isAdsShow()) {
                return UNIFIED_NATIVE_AD_VIEW_TYPE;
            }
            return MENU_ITEM_VIEW_TYPE;
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            TextView tvNo, tvQue, tvAns, tvNote;
            ImageView remove;
            RelativeLayout noteLyt, lytBookmark;
            NetworkImageView imgQuestion;


            public ItemRowHolder(View itemView) {
                super(itemView);
                tvNo = itemView.findViewById(R.id.tvIndex);
                tvQue = itemView.findViewById(R.id.tvQuestion);
                tvAns = itemView.findViewById(R.id.tvAnswer);
                tvNote = itemView.findViewById(R.id.tvNote);
                remove = itemView.findViewById(R.id.imgDelete);
                imgQuestion = itemView.findViewById(R.id.queImg);
                noteLyt = itemView.findViewById(R.id.noteLyt);
                lytBookmark = itemView.findViewById(R.id.lytBookmark);


            }
        }
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
                Utils.CheckVibrateOrSound(BookmarkList.this);
                Intent playQuiz = new Intent(BookmarkList.this, SettingActivity.class);
                startActivity(playQuiz);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}