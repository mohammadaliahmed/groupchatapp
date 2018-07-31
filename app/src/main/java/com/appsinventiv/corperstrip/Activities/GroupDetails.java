package com.appsinventiv.corperstrip.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsinventiv.corperstrip.Model.GroupModel;
import com.appsinventiv.corperstrip.Model.User;
import com.appsinventiv.corperstrip.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupDetails extends AppCompatActivity {
    DatabaseReference mDatabase;
    TextView name, description;
    String groupId;
    ImageView img;
    String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        name = findViewById(R.id.name);
        description = findViewById(R.id.desctription);


        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");
        img = findViewById(R.id.img);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GroupDetails.this, ViewPictures.class);
                i.putExtra("url", imgUrl);
                startActivity(i);
            }
        });


        initUi();


    }

    private void initUi() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");

        mDatabase.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GroupModel model = dataSnapshot.getValue(GroupModel.class);
                    if (model != null) {
                        imgUrl = model.getPicUrl();
                        Glide.with(GroupDetails.this).load(model.getPicUrl()).placeholder(R.drawable.ic_group).into(img);
                        GroupDetails.this.setTitle("" + model.getName());
                        name.setText("Group Name: "+model.getName());
                        description.setText("Description: "+model.getDescription());

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
