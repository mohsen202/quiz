package com.wrteam.quiz.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.material.snackbar.Snackbar;
import com.wrteam.quiz.AdUtils;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.UnifiedNativeAdViewHolder;
import com.wrteam.quiz.activity.RewardActivity;

import com.wrteam.quiz.activity.TournamentPlay;
import com.wrteam.quiz.helper.ApiConfig;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TournamentFragment extends Fragment {

    RecyclerView recyclerView;
    TextView tvAlert;
    ProgressBar progressbar;

    List<Model> historyList = new ArrayList<>();
    QuizListAdapter adapter;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    String type;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_list, container, false);
        getAllWidgets(view);
        assert getArguments() != null;
        type = getArguments().getString("current_page");
        prepareData(type);
        return view;
    }

    public void getAllWidgets(View view) {
        progressbar = view.findViewById(R.id.progressbar);
        recyclerView = view.findViewById(R.id.recycleview);
        tvAlert = view.findViewById(R.id.tvAlert);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void prepareData(final String type) {
        if (Utils.isNetworkAvailable(getActivity())) {
            progressbar.setVisibility(View.VISIBLE);
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GETCONTEST, Constant.GETDATAKEY);
            params.put(Constant.userId, Session.getUserData(Session.USER_ID, getActivity()));
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        //System.out.println("Values::==" + response);
                        JSONObject jsonObject = null;
                        if (type.equalsIgnoreCase(getString(R.string.live)))
                            jsonObject = jsonObject1.getJSONObject(Constant.LIVE_CONTEST);
                        else if (type.equalsIgnoreCase(getString(R.string.upcoming)))
                            jsonObject = jsonObject1.getJSONObject(Constant.UPCOMING_CONTEST);
                        else if (type.equalsIgnoreCase(getString(R.string.past)))
                            jsonObject = jsonObject1.getJSONObject(Constant.PAST_CONTEST);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            historyList.clear();
                            JSONArray object = jsonObject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < object.length(); i++) {
                                JSONObject obj = object.getJSONObject(i);
                                Model model = new Model(obj.getString(Constant.ID), obj.getString(Constant.name), obj.getString(Constant.STARTDATE), obj.getString(Constant.ENDDATE), obj.getString(Constant.DESCRIPTION), obj.getString(Constant.IMAGE), obj.getString(Constant.ENTRY), obj.getString(Constant.TOPUSERS), obj.getString(Constant.POINTS), obj.getString(Constant.DATECREATED), obj.getString(Constant.PARTICIPANTS), "");
                                if (i != 0 && i % 2 == 0) {
                                    historyList.add(new Model(true));
                                }
                                historyList.add(model);
                                progressbar.setVisibility(View.GONE);
                            }
                            adapter = new QuizListAdapter(historyList);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(adapter);

                        } else {
                            recyclerView.setVisibility(View.GONE);
                            tvAlert.setText(jsonObject.getString(Constant.messageReport));
                            tvAlert.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, params);
        } else {
            Snackbar snackbar = Snackbar
                    .make(getActivity().findViewById(android.R.id.content), getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.YELLOW)
                    .setAction(getString(R.string.retry), view -> {
                        Intent intent = getActivity().getIntent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });
            snackbar.show();
        }
    }

    public void reWardsNotLoad() {
        final android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.lifeline_dialog, null);
        dialog.setView(dialogView);
        TextView ok = dialogView.findViewById(R.id.ok);
        TextView title = dialogView.findViewById(R.id.title);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(getResources().getString(R.string.not_enough_entry_coin));
        title.setText(getResources().getString(R.string.not_enough_coin));
        final android.app.AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        ok.setOnClickListener(view -> alertDialog.dismiss());
    }

    public class QuizListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public List<Model> historyList;
        private final int MENU_ITEM_VIEW_TYPE = 0;
        private final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

        public QuizListAdapter(List<Model> historyList) {
            this.historyList = historyList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case UNIFIED_NATIVE_AD_VIEW_TYPE:
                    View unifiedNativeLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_unified, parent, false);
                    return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
                case MENU_ITEM_VIEW_TYPE:
                default:
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_contestpage, parent, false);
                    return new ItemViewHolder(v);
            }

        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder1, int position) {

            int viewType = getItemViewType(position);
            switch (viewType) {
                case UNIFIED_NATIVE_AD_VIEW_TYPE:
                    AdUtils.loadNativeAd(getActivity(), holder1);
                    break;
                case MENU_ITEM_VIEW_TYPE:
                    ItemViewHolder holder = (ItemViewHolder) holder1;
                    final Model model = historyList.get(position);

                    holder.tvName.setText(model.getName());
                    holder.tvDes.setText(model.getDescription());

                    holder.tvEntry.setText(model.getEntry() + getString(R.string._coins));
                    holder.tvParticipant.setText(model.getParticipants());

                    holder.imgQuiz.setDefaultImageResId(R.drawable.ic_contestplaceholder);
                    holder.imgQuiz.setErrorImageResId(R.drawable.ic_contestplaceholder);

                    if (!model.getImage().equalsIgnoreCase(""))
                        holder.imgQuiz.setImageUrl(model.getImage(), imageLoader);

                    if (type.equalsIgnoreCase(getString(R.string.upcoming))) {
                        holder.tvPlay.setVisibility(View.INVISIBLE);
                        holder.lytParticipant.setVisibility(View.INVISIBLE);
                        holder.tvDateHeader.setText(getResources().getString(R.string.start_on));
                        holder.tvEndDate.setText(model.getStart_date());
                    } else if (type.equalsIgnoreCase(getString(R.string.live))) {
                        holder.tvDateHeader.setText(getResources().getString(R.string.ends_on));
                        holder.tvEndDate.setText(model.getEnd_date());
                    } else if (type.equalsIgnoreCase(getString(R.string.past))) {
                        holder.tvDateHeader.setText(getResources().getString(R.string.ending_on));
                        holder.tvEndDate.setText(model.getEnd_date());
                        holder.tvPlay.setText(getResources().getString(R.string.leaderboard));
                    }

                    holder.lytMain.setOnClickListener(v -> {
                        if (holder.tvDes.getVisibility() == View.VISIBLE) {
                            holder.imgNarrow.setImageResource(R.drawable.ic_arrow_expand);
                            holder.tvDes.setVisibility(View.GONE);
                        } else {
                            holder.imgNarrow.setImageResource(R.drawable.ic_arrow_collapse);
                            holder.tvDes.setVisibility(View.VISIBLE);
                        }
                    });

                    holder.tvPlay.setOnClickListener(v -> {
                        if (type.equalsIgnoreCase(getString(R.string.past))) {
                            Intent i = new Intent(getActivity(), RewardActivity.class);
                            i.putExtra("data", model.getId());
                            startActivity(i);
                        } else {
                            if (Constant.TOTAL_COINS < Double.parseDouble(model.getEntry())) {
                                reWardsNotLoad();
                                // Toast.makeText(getActivity(), getResources().getString(R.string.not_enough_entry_coin), Toast.LENGTH_SHORT).show();
                            } else {
                                Constant.TOTAL_COINS = Constant.TOTAL_COINS - Integer.parseInt(model.getEntry());
                                Utils.UpdateCoin(getActivity(), "-" + model.getEntry());
                                Intent i = new Intent(getActivity(), TournamentPlay.class);
                                i.putExtra("id", model.getId());
                                i.putExtra("entrypoint", model.getEntry());
                                i.putExtra("title", model.getName());
                                i.putExtra("from", "contest");
                                startActivity(i);
                                getActivity().finish();
                            }
                        }
                    });

                    holder.imgInfo.setOnClickListener(v -> showPriceDialog(getActivity(), model.getPoints()));
            }
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (historyList.get(position).isAdsShow()) {
                return UNIFIED_NATIVE_AD_VIEW_TYPE;
            }
            return MENU_ITEM_VIEW_TYPE;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {

            public TextView tvName, tvDes, tvPlay, tvEndDate, tvEntry, tvDateHeader, tvParticipant;
            NetworkImageView imgQuiz;
            LinearLayout lytParticipant, lytMain;
            ImageView imgNarrow, imgInfo;


            public ItemViewHolder(View itemView) {
                super(itemView);

                tvPlay = itemView.findViewById(R.id.btnplay);
                tvPlay.setVisibility(View.VISIBLE);
                lytMain = itemView.findViewById(R.id.lytmain);
                imgNarrow = itemView.findViewById(R.id.btnarrow);
                imgInfo = itemView.findViewById(R.id.imgInfo);
                tvName = itemView.findViewById(R.id.txttitle);
                tvDes = itemView.findViewById(R.id.txtdesc);
                tvEndDate = itemView.findViewById(R.id.txtendDate);
                tvEntry = itemView.findViewById(R.id.txtentry);
                imgQuiz = itemView.findViewById(R.id.imgquiz);
                tvDateHeader = itemView.findViewById(R.id.txtdateheader);
                tvParticipant = itemView.findViewById(R.id.txtparticipatns);
                lytParticipant = itemView.findViewById(R.id.lytparticipats);
            }

        }
    }

    public void showPriceDialog(Activity activity, String priceArray) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater1 = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater1.inflate(R.layout.lyt_price_dialog, null);
        dialog.setView(dialogView);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        try {
            JSONArray jsonArray = new JSONArray(priceArray);
            ArrayList<Model> priceList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                priceList.add(new Model(jsonObject.getString(Constant.TOP_WINNERS), jsonObject.getString(Constant.POINTS)));
            }

            PriceAdapter priceAdapter = new PriceAdapter(activity, priceList);
            recyclerView.setAdapter(priceAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        imgClose.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    public static class PriceAdapter extends RecyclerView.Adapter<PriceAdapter.ItemRowHolder> {
        ArrayList<Model> dataList;
        Context mContext;

        public PriceAdapter(Context context, ArrayList<Model> dataList) {
            this.dataList = dataList;
            this.mContext = context;

        }

        @NonNull
        @Override
        public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_price, parent, false);
            return new ItemRowHolder(v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ItemRowHolder holder, final int position) {

            final Model model = dataList.get(position);
            holder.tvPrice.setText(mContext.getResources().getString(R.string.price) + model.getPoints() + mContext.getResources().getString(R.string._coins));
            holder.tvWinner.setText(mContext.getResources().getString(R.string.top_user) + model.getTop_users());

        }

        @Override
        public int getItemCount() {
            return (dataList.size());
        }

        public static class ItemRowHolder extends RecyclerView.ViewHolder {

            TextView tvWinner, tvPrice;


            public ItemRowHolder(View itemView) {
                super(itemView);

                tvWinner = itemView.findViewById(R.id.tvWinner);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }
}