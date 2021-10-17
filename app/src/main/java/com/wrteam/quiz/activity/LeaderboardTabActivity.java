package com.wrteam.quiz.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.material.tabs.TabLayout;
import com.wrteam.quiz.R;
import com.wrteam.quiz.fragment.AllLeaderboardFragment;
import com.wrteam.quiz.fragment.DailyLeaderboardFragment;
import com.wrteam.quiz.fragment.MonthlyLeaderboardFragment;
import com.wrteam.quiz.helper.AppController;
import com.wrteam.quiz.helper.CircleImageView;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardTabActivity extends AppCompatActivity {


    private ViewPager viewPager;
    private TabLayout tabLayout;
    Toolbar toolbar;
    static Button redeem;
    public static TextView score, coin, tvName;
    public static int txtcoin = 0;
    private CircleImageView imgProfile;
    public static ViewPagerAdapter adapter;
    private String[] tabs ;
    Boolean firstTime = null;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tableader);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        tabs = new String[]{getString(R.string.today),getString(R.string.month),getString(R.string.all)};
        viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.leaderboard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        imgProfile = findViewById(R.id.imgProfile);
        tvName = findViewById(R.id.tvName);
        coin = findViewById(R.id.tvScore);
        score = findViewById(R.id.tvRank);
        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        setUpCustomTabs();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View selected = tab.getCustomView();
                TextView iv_text = (TextView) selected.findViewById(R.id.custom_tab_label);
                iv_text.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                iv_text.setBackground(getResources().getDrawable(R.drawable.right_btn_bg));
                iv_text.setPadding(10, 10, 10, 10);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View selected = tab.getCustomView();
                TextView iv_text = (TextView) selected.findViewById(R.id.custom_tab_label);
                iv_text.setTextColor(getResources().getColor(R.color.white));
                iv_text.setBackground(getResources().getDrawable(R.drawable.storktabbtn));
                iv_text.setPadding(10, 10, 10, 10);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


    }


    private void setUpCustomTabs() {
        for (int i = 0; i < tabs.length; i++) {
            TextView customTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab_layout, null);//get custom view

            customTab.setText(tabs[i]);//set text over view
            if (i == 0) {
                customTab.setBackground(getResources().getDrawable(R.drawable.right_btn_bg));
                customTab.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                customTab.setPadding(10, 10, 10, 10);

            } else {
                customTab.setBackground(getResources().getDrawable(R.drawable.storktabbtn));
                customTab.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                customTab.setPadding(10, 10, 10, 10);
            }
            TabLayout.Tab tab = tabLayout.getTabAt(i);//get tab via position
            if (tab != null)
                tab.setCustomView(customTab);//set custom view
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new LeaderboardTabActivity.ViewPagerAdapter(this.getSupportFragmentManager());
        adapter.addFrag(new DailyLeaderboardFragment(), tabs[0]);
        adapter.addFrag(new MonthlyLeaderboardFragment(), tabs[1]);
        adapter.addFrag(new AllLeaderboardFragment(), tabs[2]);
        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {

            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
