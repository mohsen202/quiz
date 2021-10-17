package com.wrteam.quiz.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.AppController;

import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.wrteam.quiz.model.Room;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class WaitingRoomActivity extends AppCompatActivity {
    String FORMAT = "%02d";
    Activity activity;
    RecyclerView gameUserView;
    RelativeLayout shareLyt;
    TextView tvAlert, tvGameCode, tvTimer, tvGameStart, tvGameTitle;
    LinearLayout  lytGameCode;
    String from, roomKey, roomId, authId, type;
    DatabaseReference roomDBRef, gameUserRef;
    ArrayList<Room> gameUserList;
    ChildEventListener childGameUserListener;
    ValueEventListener roomListener;
    Room roomData;
    CountDownTimer countDownTimer;
    FloatingActionButton fabShare;
    int playerCount = 1;
    boolean exit;
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getAllWidgets();
        activity = WaitingRoomActivity.this;
        from = getIntent().getStringExtra("from");
        roomKey = getIntent().getStringExtra("roomKey");
        roomId = getIntent().getStringExtra("roomId");
        type = getIntent().getStringExtra("type");
        roomDBRef = FirebaseDatabase.getInstance().getReference(Constant.MULTIPLAYER_ROOM).child(roomId);
        authId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.multiplayer_game);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent));
        toolbar.setTitleTextColor(ContextCompat.getColor(activity, R.color.white));
        getData();
        if (roomKey.equalsIgnoreCase(authId)) {
            tvGameStart.setText(getString(R.string.lets_start));
            lytGameCode.setVisibility(View.VISIBLE);
            shareLyt.setVisibility(View.VISIBLE);
            tvGameCode.setText(roomId);
            startTimer();
        } else {
            tvGameStart.setText(getString(R.string.wait_for_players));
            shareLyt.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
        }
        tvGameStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameUserList != null && gameUserList.size() != 0) {

                    if (getJoinUserCount() > 1) {
                        if (roomKey.equalsIgnoreCase(authId)) {
                            roomDBRef.child(Constant.isStarted).setValue(Constant.TRUE);
                            goToPlayArea();
                        }
                    } else {
                        Toast.makeText(activity, getString(R.string.minmum_user_join_alert), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private void startTimer() {
        tvTimer.setVisibility(View.VISIBLE);
        stopTimer();
        countDownTimer = new CountDownTimer(Constant.GROUP_WAIT_TIME, Constant.COUNT_DOWN_TIMER) {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            public void onTick(long millisUntilFinished) {
                int progress = (int) (millisUntilFinished / 1000);
                long totalSecs = (long) (millisUntilFinished / 1000.0);
                long minutes = (totalSecs / 60);
                long seconds = totalSecs % 60;
                tvTimer.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }

            public void onFinish() {

                shareLyt.setVisibility(View.GONE);
                showTimeUpAlert();
            }
        }.start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void showTimeUpAlert() {
        try {
            stopTimer();
            final androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(activity);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.dialog_join_alert, null);
            dialog.setView(dialogView);
            TextView tvExit = dialogView.findViewById(R.id.tvExit);
            TextView tvTry = dialogView.findViewById(R.id.tvTry);
            androidx.appcompat.app.AlertDialog alertDialog = dialog.create();
            tvExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    alertDialog.dismiss();
                }
            });
            tvTry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setCancelable(false);
            alertDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getJoinUserCount() {
        int count = 0;
        for (Room s : gameUserList) {
            if (s.getIsJoined().equalsIgnoreCase(Constant.TRUE)) {

                count = (count + 1);
            }

        }

        return count;
    }

    public void getData() {
        if (Utils.isNetworkAvailable(activity)) {
            exit = true;

            if (type.equals("invite")) {
                HashMap<String, String> joinMap = new HashMap<>();
                joinMap.put(Constant.UID, authId);
                joinMap.put(Constant.IMAGE, Session.getUserData(Session.PROFILE, activity));
                joinMap.put(Constant.NAME, Session.getUserData(Session.NAME, activity));
                joinMap.put(Constant.IS_JOINED, Constant.TRUE);
                joinMap.put(Constant.USER_ID, Session.getUserData(Session.USER_ID, activity));
                roomDBRef.child(Constant.joinUser).child(authId).setValue(joinMap);
            }

            // if (!roomKey.equals(authId))
            checkGameRoomStatus();
            getGameUSerList();
        }
    }

    public void checkGameRoomStatus() {
        roomListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Room roomData = snapshot.getValue(Room.class);
                    if (!roomKey.equalsIgnoreCase(authId)) {
                        if (roomData.getIsRoomActive().equalsIgnoreCase(Constant.FALSE))
                            showRoomDeActivateAlert();

                        if (roomData.getIsStarted().equalsIgnoreCase(Constant.TRUE))
                            goToPlayArea();
                    }

                }
                // System.out.println("===call===");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //  System.out.println("===call===onCancelled");
            }
        };
        roomDBRef.addValueEventListener(roomListener);
    }

    public void goToPlayArea() {
        exit = false;
        Intent intent = new Intent(activity, MultiPlayerGame.class);
        intent.putExtra("roomKey", roomKey);
        intent.putExtra("roomId", roomId);
        intent.putExtra("from", from);
        startActivity(intent);
        finish();
    }

    public void getGameUSerList() {
        gameUserList = new ArrayList<>();
        final DataAdapter adapter = new DataAdapter(activity, gameUserList);
        gameUserView.setAdapter(adapter);
        gameUserRef = roomDBRef.child(Constant.joinUser);
        childGameUserListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Room book = dataSnapshot.getValue(Room.class);
                assert book != null;

                gameUserList.add(book);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Room book = dataSnapshot.getValue(Room.class);
                assert book != null;
                int index = -1;
                for (int i = 0; i < gameUserList.size(); i++) {
                    String uID = gameUserList.get(i).getUID();
                    if (uID.equals(book.getUID())) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    gameUserList.set(index, book);
                    adapter.notifyDataSetChanged();
                }

                //A comment has changed, use the key to determine if we are displaying this
                //comment and if so displayed the changed comment.

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Room book = dataSnapshot.getValue(Room.class);
                assert book != null;
                int index = -1;
                for (int i = 0; i < gameUserList.size(); i++) {
                    String uID = gameUserList.get(i).getUID();
                    if (uID.equals(book.getUID())) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    gameUserList.remove(index);
                    adapter.notifyDataSetChanged();
                }
                playerCount--;
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        gameUserRef.addChildEventListener(childGameUserListener);
    }

    public void ShareGameCode(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name) + "\n" + getString(R.string.share_code_msg) + roomId);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name) + "\n" + getString(R.string.share_code_msg) + roomId);
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        startActivity(Intent.createChooser(intent, getString(R.string.share_via)));
    }

    public class DataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<Room> dataList;
        private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        public Context context;

        public DataAdapter(Context context, ArrayList<Room> dataList) {
            this.dataList = dataList;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user, parent, false);
            return new ItemRowHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder1, final int position) {
            Room user = dataList.get(position);
            ItemRowHolder holder = (ItemRowHolder) holder1;
            holder.tvName.setText(user.getName());
            holder.image.setImageUrl(user.getImage(), imageLoader);

            if (roomKey.equals(user.getUID())) {
                holder.image.setBackgroundResource(R.drawable.circle_bg);
            }
            if (user.getIsJoined().equals(Constant.FALSE)) {
                holder.tvName.setTextColor(Color.GRAY);
            } else {
                holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.white));
            }

        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            CircleImageView image;
            RelativeLayout imageBg;

            public ItemRowHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                image = itemView.findViewById(R.id.image);
                imageBg = itemView.findViewById(R.id.imageBg);


            }
        }

    }

    public void sendInvitation(final Room user) {

        HashMap<String, String> joinMap = new HashMap<>();
        joinMap.put(Constant.UID, user.getAuthId());
        joinMap.put(Constant.IMAGE, user.getImage());
        joinMap.put(Constant.NAME, user.getName());
        joinMap.put(Constant.IS_JOINED, "false");
        joinMap.put(Constant.USER_ID, user.getUserID());
        roomDBRef.child(Constant.joinUser).child(user.getAuthId()).setValue(joinMap);
    }

    public void getAllWidgets() {
        fabShare = findViewById(R.id.fabShare);
        shareLyt = findViewById(R.id.shareLyt);
        tvAlert = findViewById(R.id.tvAlert);
        tvTimer = findViewById(R.id.tvTimer);
        toolbar = findViewById(R.id.toolBar);
        tvGameCode = findViewById(R.id.tvGameCode);
        tvGameCode.setTextColor(Color.WHITE);
        tvGameStart = findViewById(R.id.tvGameStart);
        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvGameTitle.setTextColor(Color.WHITE);
        gameUserView = findViewById(R.id.gameUserView);
        lytGameCode = findViewById(R.id.lytGameCode);
        gameUserView.setLayoutManager(new GridLayoutManager(activity, 3));
        gameUserView.setNestedScrollingEnabled(false);

    }

    public void showRoomDeActivateAlert() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.room_deactivate_alert));
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();
        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                roomDBRef.child(Constant.joinUser).child(authId).removeValue();
                gameUserRef.removeEventListener(childGameUserListener);
                roomDBRef.removeEventListener(roomListener);
                alertDialog1.dismiss();
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
                updateUserStatus();
                removeListeners();
                alertDialog1.dismiss();
                finish();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }

    public void setTelephoneListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    exit = false;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        removeListeners();
    }

    public void updateUserStatus() {
        if (roomKey.equals(authId)) {
            roomDBRef.child(Constant.isRoomActive).setValue(Constant.FALSE);
        } else {
            roomDBRef.child(Constant.joinUser).child(authId).removeValue();
        }

    }

    public void removeListeners() {
        gameUserRef.removeEventListener(childGameUserListener);
        roomDBRef.removeEventListener(roomListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        leaveGameDialog();
    }
}