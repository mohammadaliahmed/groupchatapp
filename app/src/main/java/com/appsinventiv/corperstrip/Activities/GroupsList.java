package com.appsinventiv.corperstrip.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.appsinventiv.corperstrip.Adapters.GroupsAdapter;
import com.appsinventiv.corperstrip.Model.GroupModel;
import com.appsinventiv.corperstrip.Model.User;
import com.appsinventiv.corperstrip.R;
import com.appsinventiv.corperstrip.Utils.CommonUtils;
import com.appsinventiv.corperstrip.Utils.SharedPrefs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GroupsList extends AppCompatActivity {

    DatabaseReference mDatabase;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    ArrayList<GroupModel> groupModelArrayList = new ArrayList<>();
    ArrayList<String> userGroupsArrayList = new ArrayList<>();
    GroupsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);
        this.setTitle("Groups");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupsAdapter(this, groupModelArrayList, userGroupsArrayList, new GroupsAdapter.JoinOrLeave() {
            @Override
            public void onJoin(GroupModel groupModel) {
                addMemberToGroup(groupModel);
            }

            @Override
            public void onLeave(GroupModel groupModel) {
                removeFromGroup(groupModel);
            }
        });
        recyclerView.setAdapter(adapter);

    }

    private void removeFromGroup(GroupModel groupModel) {

        if (userGroupsArrayList.contains(groupModel.getId())) {
            userGroupsArrayList.remove(userGroupsArrayList.indexOf(groupModel.getId()));
        }
        mDatabase.child("Users").child(SharedPrefs.getUsername()).child("inGroupsList").setValue(userGroupsArrayList).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                CommonUtils.showToast("Removed from group");
                getUserGroupsFromDb();
                SharedPrefs.saveArray(userGroupsArrayList);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CommonUtils.showToast("" + e.getMessage());
            }
        });

    }

    private void addMemberToGroup(GroupModel groupModel) {
        userGroupsArrayList.add(groupModel.getId());
        mDatabase.child("Users").child(SharedPrefs.getUsername()).child("inGroupsList").setValue(userGroupsArrayList).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                CommonUtils.showToast("Added to group");
                getUserGroupsFromDb();
                SharedPrefs.saveArray(userGroupsArrayList);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CommonUtils.showToast("" + e.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllGroupsFromServer();
        getUserGroupsFromDb();
    }

    private void getAllGroupsFromServer() {
        mDatabase.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    groupModelArrayList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        if (model != null) {
                            if (model.isActive()) {
                                groupModelArrayList.add(model);
                                Collections.sort(groupModelArrayList, new Comparator<GroupModel>() {
                                    @Override
                                    public int compare(GroupModel listData, GroupModel t1) {
                                        String ob1 = listData.getName();
                                        String ob2 = t1.getName();

                                        return ob1.compareTo(ob2);

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

    private void getUserGroupsFromDb() {
        mDatabase.child("Users").child(SharedPrefs.getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    userGroupsArrayList.clear();

                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getInGroupsList() != null) {
                            for (String groupId : user.getInGroupsList()) {
                                userGroupsArrayList.add(groupId);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
