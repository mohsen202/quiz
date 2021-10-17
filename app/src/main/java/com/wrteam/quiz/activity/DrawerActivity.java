package com.wrteam.quiz.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.wrteam.quiz.Constant;
import com.wrteam.quiz.R;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.ArcNavigationView;
import com.wrteam.quiz.helper.CircleImageView;
import com.wrteam.quiz.helper.Session;
import com.wrteam.quiz.helper.Utils;
import com.google.android.material.navigation.NavigationView;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public ArcNavigationView navigationView;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle drawerToggle;
    protected FrameLayout frameLayout;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    public static CircleImageView imgProfile;
    public TextView tvEmail;
    public static TextView tvName;
    static final float END_SCALE = 0.7f;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.transparentStatusAndNavigation(DrawerActivity.this);
        setContentView(R.layout.activity_drawer);
        frameLayout = findViewById(R.id.content_frame);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);

        Utils.loadAd(DrawerActivity.this);
        animateNavigationDrawer();
    }

    private void animateNavigationDrawer() {

        //Add any color or remove it to use the default one!
        //To make it transparent use Color.Transparent in side setScrimColor();
        //drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                frameLayout.setScaleX(offsetScale);
                frameLayout.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = frameLayout.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                frameLayout.setTranslationX(xTranslation);
            }
        });

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {


            case R.id.leaderboard:

                Intent leaderBoard = new Intent(getApplicationContext(), LeaderboardTabActivity.class);
                startActivity(leaderBoard);
                drawerLayout.closeDrawers();
                break;

            case R.id.statistic:

                Intent statistic = new Intent(getApplicationContext(), UserStatistics.class);
                startActivity(statistic);

                Utils.displayInterstitial(DrawerActivity.this);
                drawerLayout.closeDrawers();
                break;

            case R.id.setting:
                Intent setting = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(setting);
                break;

            case R.id.notification:
                Intent notification = new Intent(getApplicationContext(), NotificationList.class);
                startActivity(notification);
                drawerLayout.closeDrawers();
                Utils.displayInterstitial(DrawerActivity.this);
                break;
            case R.id.bookmark:

                Intent bookmark = new Intent(getApplicationContext(), BookmarkList.class);
                startActivity(bookmark);
                drawerLayout.closeDrawers();
                Utils.displayInterstitial(DrawerActivity.this);
                break;

            case R.id.invite:

                Intent invite = new Intent(getApplicationContext(), InviteFriendActivity.class);
                startActivity(invite);
                Utils.displayInterstitial(DrawerActivity.this);
                drawerLayout.closeDrawers();
                break;
            case R.id.instruction:
                Intent instruction = new Intent(getApplicationContext(), InstructionActivity.class);
                instruction.putExtra("type", "instruction");
                startActivity(instruction);
                break;
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, Constant.SHARE_APP_TEXT + " " + Constant.APP_LINK);
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                startActivity(Intent.createChooser(intent, getString(R.string.share_via)));
                break;
            case R.id.about:
                Intent about = new Intent(getApplicationContext(), PrivacyPolicy.class);
                about.putExtra("type", "about");
                startActivity(about);
                drawerLayout.closeDrawers();
                break;
            case R.id.terms:
                Intent terms = new Intent(getApplicationContext(), PrivacyPolicy.class);
                terms.putExtra("type", "terms");
                startActivity(terms);
                drawerLayout.closeDrawers();
                break;
            case R.id.privacy:
                Intent privacy = new Intent(getApplicationContext(), PrivacyPolicy.class);
                privacy.putExtra("type", "privacy");
                startActivity(privacy);
                drawerLayout.closeDrawers();
                break;
            case R.id.logout:
                Utils.SignOutWarningDialog(DrawerActivity.this);
                break;
            default:
        }
        return false;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //Sync the toggle state after onRestoreInstanceState has occurred.
        /*drawerToggle.syncState();*/
    }
}
