package com.wrteam.quiz.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.helper.UserSessionManager;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RewardActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    TextView nodata, txtname, txtscore;
    ProgressBar progressbar;
    UserSessionManager session;
    private List<Model> historyList = new ArrayList<>();
    TopUserAdapter adapter;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    String quizid;
    CardView lytuser;
    LinearLayout lyttop;
    NetworkImageView banner;
    public static String url;
    RelativeLayout lyt_rank3, lyt_rank2, lyt_rank1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reward);

        quizid = getIntent().getStringExtra("data");
        session = new UserSessionManager(RewardActivity.this);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        nodata = (TextView) findViewById(R.id.nodata);
        recyclerView.setLayoutManager(new LinearLayoutManager(RewardActivity.this));
        lytuser = (CardView) findViewById(R.id.lytuser);

        txtname = (TextView) findViewById(R.id.txtname);
        txtscore = (TextView) findViewById(R.id.txtscore);


        lyttop = (LinearLayout) findViewById(R.id.lyttop);
        lyt_rank1 = (RelativeLayout) findViewById(R.id.lyt_rank1);
        lyt_rank2 = (RelativeLayout) findViewById(R.id.lyt_rank2);
        lyt_rank3 = (RelativeLayout) findViewById(R.id.lyt_rank3);

        if (Utils.isNetworkAvailable(RewardActivity.this)) {
            // getUserData();
            prepareData();
        }

    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void prepareData() {

        progressbar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GETLEADERBOARD, Constant.GETDATAKEY);
        params.put(Constant.CONTEST_ID, quizid);
        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        if (jsonObject1.getString("error").equalsIgnoreCase("false")) {

                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                            historyList.clear();

                            lyttop.setVisibility(View.VISIBLE);

                            for (int i = 1; i <= 3; i++) {
                                RelativeLayout lyt = (RelativeLayout) findViewById(getResources().getIdentifier("lyt_rank" + i, "id", getPackageName()));
                                lyt.setVisibility(View.INVISIBLE);
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                progressbar.setVisibility(View.GONE);

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

                                    System.out.println("PATH::-=- " + jsonObject.getString(Constant.PROFILE));

                                } else {
                                    Model model = new Model(jsonObject.getString(Constant.RANK), jsonObject.getString(Constant.userId), jsonObject.getString(Constant.name), jsonObject.getString(Constant.SCORE), jsonObject.getString(Constant.PROFILE));
                                    historyList.add(model);
                                }

                            }
                            adapter = new TopUserAdapter(historyList);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(adapter);

                        } else {
                            recyclerView.setVisibility(View.GONE);
                            nodata.setVisibility(View.VISIBLE);
                            nodata.setText(jsonObject1.getString("message"));
                            progressbar.setVisibility(View.GONE);
                            lyttop.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, params);

    }


    class TopUserAdapter extends RecyclerView.Adapter<TopUserAdapter.ViewHolder> {

        public List<Model> historyList;

        public TopUserAdapter(List<Model> historyList) {
            this.historyList = historyList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_tournametleaderboard, parent, false);
            return new ViewHolder(view);
        }

        @NonNull

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            Model model = historyList.get(position);

            holder.txtscore.setText(new DecimalFormat("##.##").format(Double.parseDouble(model.getScore())));
            holder.txtname.setText(model.getName());
            holder.txtno.setText(model.getRank());
            System.out.println("Model Image ::-= " + model.getImage());
            holder.profileimg.setImageUrl(model.getImage(), imageLoader);

        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView txtname, txtscore, txtno;
            CircleImageView profileimg;

            public ViewHolder(View itemView) {
                super(itemView);
                profileimg = (CircleImageView) itemView.findViewById(R.id.profileimg);
                txtno = (TextView) itemView.findViewById(R.id.txtno);
                txtscore = (TextView) itemView.findViewById(R.id.txtscore);
                txtname = (TextView) itemView.findViewById(R.id.txtname);
            }

        }
    }
}






