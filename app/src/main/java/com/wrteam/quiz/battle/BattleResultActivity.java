package com.wrteam.quiz.battle;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.model.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class BattleResultActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;

    ArrayList<Room> resultList, mainResultList;
    DatabaseReference gameRoomRef;
    UserScoreAdapter adapter;
    Activity activity;
    String roomId, roomKey, authId, from;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_result);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity = BattleResultActivity.this;
        getAllWidgets();
        from = getIntent().getStringExtra("from");
        roomKey = getIntent().getStringExtra("roomKey");
        roomId = getIntent().getStringExtra("roomId");

        authId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        gameRoomRef = FirebaseDatabase.getInstance().getReference().child(from).child(roomKey);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.battle_result));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainResultList = MultiPlayerGame.gameUserList;

        resultList = new ArrayList<>();
        Collections.sort(mainResultList, new Comparator<Room>() {
            @Override
            public int compare(Room o1, Room o2) {
                return Integer.parseInt(o2.getRightAns()) - Integer.parseInt(o1.getRightAns());
            }
        });

        for (int i = 0; i < mainResultList.size(); i++) {
            Room room = mainResultList.get(i);
            resultList.add(room);

            //System.out.println("====result size pos " + i + "==" + room.getRightAns() + "result " + resultList.size() );
        }


        adapter = new UserScoreAdapter(activity, resultList);
        recyclerView.setAdapter(adapter);


    }

    public void BackToHome(View view) {
        onBackPressed();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_battle_result, parent, false);
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
                holder.relativeLyt.setBackgroundResource(R.drawable.white_rec_bg);
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

    @Override
    public void onBackPressed() {
        if (roomKey.equalsIgnoreCase(authId)) {
            gameRoomRef.removeValue();
        }
        finish();
        super.onBackPressed();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    public void getAllWidgets() {
        toolbar = findViewById(R.id.toolBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
}