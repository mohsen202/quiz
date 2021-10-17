package com.wrteam.quiz.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.material.snackbar.Snackbar;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.activity.LeaderboardAdapter;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.UserSessionManager;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.LeaderBoard;
import com.wrteam.quiz.model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wrteam.quiz.Constant.PAGE_LIMIT;


public class MonthlyLeaderboardFragment extends Fragment {

    RecyclerView recyclerView;
    TextView nodata;
    ProgressBar progressbar;
    UserSessionManager session;
    private List<Model> historyList = new ArrayList<>();
    private CircleImageView imgProfile;
    private RelativeLayout rankLyt;
    String quiztype;
    public ArrayList<LeaderBoard> lbList, topList;
    int PAGE_START = 0;
    LeaderboardAdapter adapter;
    int offset = 0;
    int total;
    private String USER_RANK;
    public String SCORE, USER_ID;

    protected Handler handler;
    private TextView tvTitle, tvRank, tvScore, tvName, tvAlert;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    String type;
    String formattedDate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_leaderboard, container, false);


        session = new UserSessionManager(getActivity());
        progressbar = (ProgressBar) v.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        tvTitle = (TextView)v.findViewById(R.id.tvTitle);
        tvAlert =(TextView)v. findViewById(R.id.tvAlert);
        tvRank = (TextView)v.findViewById(R.id.tvRank);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvScore =(TextView)v. findViewById(R.id.tvScore);
        imgProfile = (CircleImageView)v. findViewById(R.id.imgProfile);
        rankLyt =(RelativeLayout)v. findViewById(R.id.rankLyt);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        handler = new Handler();
        // Spinner on item click listener

        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c);
        rankLyt.setVisibility(View.GONE);

        if (lbList != null)
            lbList.clear();
        if (topList != null)
            topList.clear();

        PAGE_START = 0;
        offset = 0;
        total = 0;
        LeaderBoardData(formattedDate, "", 0);
        return v;
    }



    public void LeaderBoardData(final String date, final String type, final int startoffset) {

            if (Utils.isNetworkAvailable(getActivity())) {
                progressbar.setVisibility(View.VISIBLE);
                Map<String, String> params = new HashMap<>();
                params.put(Constant.getMontlyLeaderboard, "1");
                params.put(Constant.DATE, date);
                /*setLeaderboardApiParams(date, type, params);*/
                params.put(Constant.OFFSET, String.valueOf(startoffset));
                params.put(Constant.LIMIT, String.valueOf(PAGE_LIMIT));
                params.put(Constant.userId, Session.getUserData(Session.USER_ID, getActivity()));
                ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
                    @Override
                    public void onSuccess(boolean result, String response) {

                        if (result) {
                            try {
                                if(getActivity()!=null) {
                                  //  System.out.println("MonthlyResponse::=" + response);
                                    lbList = new ArrayList<>();
                                    topList = new ArrayList<>();
                                    JSONObject obj = new JSONObject(response);
                                    tvAlert.setVisibility(View.GONE);
                                    JSONArray jsonArray = obj.getJSONArray("data");
                                    JSONObject userObject = jsonArray.getJSONObject(0).getJSONObject("my_rank");
                                    if (userObject.length() > 0) {
                                        if (!userObject.getString(Constant.RANK).equals("0")) {
                                            if (userObject.getString(Constant.userId).equals(Session.getUserData(Session.USER_ID, getActivity()))) {
                                                rankLyt.setVisibility(View.VISIBLE);
                                                imgProfile.setImageUrl(Session.getUserData(Constant.PROFILE, getActivity()), imageLoader);
                                                tvScore.setText("" + userObject.getString(Constant.SCORE));
                                                tvName.setText(Session.getUserData(Constant.USER_NAME, getActivity()));
                                                tvRank.setText("" + userObject.getString(Constant.RANK));
                                            }
                                        }
                                    } else {
                                        rankLyt.setVisibility(View.GONE);
                                    }
                                    progressbar.setVisibility(View.GONE);
                                    if (jsonArray.length() > 1) {
                                        total = Integer.parseInt(obj.getString(Constant.TOTAL));

                                        for (int i = 1; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            USER_RANK = object.getString(Constant.RANK);

                                            switch (USER_RANK) {
                                                case "1":

                                                case "2":

                                                case "3":
                                                    topList.add(new LeaderBoard(object.getString(Constant.RANK),
                                                            object.getString(Constant.name), object.getString(Constant.SCORE),
                                                            object.getString(Constant.userId), object.getString(Constant.PROFILE)));
                                                    break;

                                                default:
                                                    //lbList.add(0, new LeaderBoard(topList));
                                                    LeaderBoard leaderBoard = new LeaderBoard(object.getString(Constant.RANK),
                                                            object.getString(Constant.name), object.getString(Constant.SCORE),
                                                            object.getString(Constant.userId), object.getString(Constant.PROFILE));
                                                    lbList.add(leaderBoard);
                                                    break;
                                            }
                                        }

                                        if (jsonArray.length() == 2)
                                            lbList.add(0, new LeaderBoard(topList));
                                        else if (jsonArray.length() == 3)
                                            lbList.add(0, new LeaderBoard(topList));
                                        else if (jsonArray.length() == 4)
                                            lbList.add(0, new LeaderBoard(topList));
                                        else
                                            lbList.add(0, new LeaderBoard(topList));


                                        if (startoffset == 0) {
                                            adapter = new LeaderboardAdapter(getActivity(), lbList, recyclerView);
                                            adapter.setHasStableIds(true);
                                            recyclerView.setAdapter(adapter);
                                            adapter.setOnLoadMoreListener(new LeaderboardAdapter.OnLoadMoreListener() {
                                                @Override
                                                public void onLoadMore() {
                                                    //add null , so the adapter will check view_type and show progress bar at bottom
                                                    if (lbList.size() < total) {
                                                        lbList.add(null);
                                                        adapter.notifyItemInserted(lbList.size() - 1);

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                //   remove progress item
                                                           /*     if (lbList.contains(null)) {
                                                                    lbList.remove(lbList.size() - 1);
                                                                    adapter.notifyItemRemoved(lbList.size());
                                                                    for (int i = 0; i < lbList.size(); i++) {
                                                                        if (lbList.get(i) == null) {
                                                                            lbList.remove(i);
                                                                            break;
                                                                        }
                                                                    }
                                                                }*/

                                                                offset = offset + PAGE_LIMIT;
                                                                Map<String, String> params = new HashMap<>();

                                                                params.put(Constant.getMontlyLeaderboard, "1");
                                                                params.put(Constant.DATE, date);
                                                                /*setLeaderboardApiParams(date, type, params);*/
                                                                params.put(Constant.OFFSET, String.valueOf(offset));
                                                                params.put(Constant.LIMIT, String.valueOf(PAGE_LIMIT));
                                                                if (getActivity() != null) {
                                                                    params.put(Constant.userId, Session.getUserData(Session.USER_ID, getActivity()));
                                                                }
                                                                ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
                                                                    @Override
                                                                    public void onSuccess(boolean result, String response) {

                                                                        if (result) {
                                                                            try {
                                                                                lbList.remove(lbList.size() - 1);
                                                                                adapter.notifyItemRemoved(lbList.size());
                                                                                SCORE = Constant.SCORE;
                                                                                USER_ID = Constant.userId;
                                                                                JSONObject obj = new JSONObject(response);
                                                                                JSONArray jsonArray = obj.getJSONArray("data");
                                                                                if (jsonArray.length() > 1) {
                                                                                    for (int i = 1; i < jsonArray.length(); i++) {
                                                                                        JSONObject object = jsonArray.getJSONObject(i);
                                                                                        USER_RANK = object.getString(Constant.RANK);
                                                                                        LeaderBoard leaderBoard = new LeaderBoard(object.getString(Constant.RANK), object.getString(Constant.name), object.getString(SCORE), object.getString(USER_ID), object.getString(Constant.PROFILE));
                                                                                        lbList.add(leaderBoard);
                                                                                        //adapter.notifyItemInserted(lbList.size());
                                                                                    }
                                                                                    adapter.notifyDataSetChanged();
                                                                                    adapter.setLoaded();
                                                                                } else {
                                                                                    progressbar.setVisibility(View.GONE);
                                                                                }
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
                                                                }, params);
                                                            }
                                                        }, 1000);
                                                    }
                                                }
                                            });

                                        }
                                    } else {

                                        progressbar.setVisibility(View.GONE);
                                        tvAlert.setText(getString(R.string.no_data));
                                        tvAlert.setVisibility(View.VISIBLE);
                                        if (adapter != null)
                                            adapter.notifyDataSetChanged();

                                    }
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

    public void setSnackBar() {
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LeaderBoardData(formattedDate, "", 0);
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }


    @Override
    public void onResume() {
        super.onResume();

    }
}
