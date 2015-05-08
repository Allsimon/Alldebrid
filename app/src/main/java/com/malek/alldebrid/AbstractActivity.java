package com.malek.alldebrid;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.malek.alldebrid.ui.adapter.NavDrawerItem;
import com.malek.alldebrid.ui.adapter.NavDrawerListAdapter;

import java.util.List;


public abstract class AbstractActivity extends AppCompatActivity {
    protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected CharSequence mDrawerTitle;
    protected CharSequence mTitle;
    protected List<NavDrawerItem> mNavDrawerItems;
    protected NavDrawerListAdapter mNavDrawerAdapter;
    protected String FRAGMENT_TAG = "fragment_tag";
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            Fragment oldFragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            changeFragment(oldFragment);
        } else {
            initFragment();
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        final ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.header_user_account, mDrawerList, false);
        mDrawerList.addHeaderView(header, null, false);
        mDrawerList.setAdapter(mNavDrawerAdapter);
        prepareListData();
        refreshNavDrawer();
        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.app_name,
                R.string.app_name
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }


    public abstract void prepareListData();

    public abstract void initFragment();

    public void refreshNavDrawer() {
        mNavDrawerAdapter = new NavDrawerListAdapter(this, mNavDrawerItems);
        mDrawerList.setAdapter(mNavDrawerAdapter);
    }

    public void changeFragment(Fragment f) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, f, FRAGMENT_TAG);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void onNavDrawerClickListener(int groupPosition) {
        // There is -1 because of the header !
        mNavDrawerItems.get(groupPosition - 1).onClick();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle(final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToolbar.setTitle(title);
            }
        });
    }

    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            onNavDrawerClickListener(position);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

}