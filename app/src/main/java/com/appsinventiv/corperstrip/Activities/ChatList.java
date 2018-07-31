package com.appsinventiv.corperstrip.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsinventiv.corperstrip.Adapters.GroupsListAdapter;
import com.appsinventiv.corperstrip.Model.GroupModel;
import com.appsinventiv.corperstrip.Model.User;
import com.appsinventiv.corperstrip.R;
import com.appsinventiv.corperstrip.Utils.CommonUtils;
import com.appsinventiv.corperstrip.Utils.Constants;
import com.appsinventiv.corperstrip.Utils.PrefManager;
import com.appsinventiv.corperstrip.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ArrayList<GroupModel> groupModelArrayList = new ArrayList<>();
    DatabaseReference mDatabase;
    RecyclerView recyclerView;
    private long mBackPressed;
    private AdView mAdView;

    GroupsListAdapter adapter;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("Corpers Trip");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Constants.USERS_REFERENCE).child(SharedPrefs.getUsername()).child("fcmKey").setValue(SharedPrefs.getFcmKey());

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupsListAdapter(this, groupModelArrayList);
        recyclerView.setAdapter(adapter);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.name_drawer);
        navUsername.setText(SharedPrefs.getName());
        ImageView img = headerView.findViewById(R.id.profile_image);
        Glide.with(this).load(SharedPrefs.getPicUrl()).into(img);


    }

    @Override
    protected void onResume() {
        super.onResume();
        getGroupsFromDb();
    }


    private void getGroupsFromDb() {
        mDatabase.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    groupModelArrayList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        if (model != null) {
                            if (SharedPrefs.loadArray().contains(model.getId())) {
                                groupModelArrayList.add(model);
                                Collections.sort(groupModelArrayList, new Comparator<GroupModel>() {
                                    @Override
                                    public int compare(GroupModel listData, GroupModel t1) {
                                        Long ob1 = listData.getTime();
                                        Long ob2 = t1.getTime();

                                        return ob2.compareTo(ob1);

                                    }
                                });
                                adapter.notifyDataSetChanged();

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), "Tap back button in order to exit", Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            Intent i = new Intent(ChatList.this, EditProfile.class);
            startActivity(i);

            // Handle the camera action
        } else if (id == R.id.groups) {
            Intent i = new Intent(ChatList.this, GroupsList.class);
            startActivity(i);

        } else if (id == R.id.share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Corpers Trip\n Download Now\n" + "http://play.google.com/store/apps/details?id=" + ChatList.this.getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Share App via.."));

        } else if (id == R.id.signout) {
            PrefManager prefManager = new PrefManager(ChatList.this);
            prefManager.setFirstTimeLaunch(true);
            SharedPrefs.setIsLoggedIn("no");

            Intent i = new Intent(ChatList.this, Login.class);
            startActivity(i);
            deleteAppData();
            finish();


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void deleteAppData() {
        try {
            // clearing app data
            String packageName = getApplicationContext().getPackageName();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear " + packageName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
