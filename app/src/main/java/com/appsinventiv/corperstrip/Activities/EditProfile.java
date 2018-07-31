package com.appsinventiv.corperstrip.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.appsinventiv.corperstrip.Model.User;
import com.appsinventiv.corperstrip.R;
import com.appsinventiv.corperstrip.Utils.CommonUtils;
import com.appsinventiv.corperstrip.Utils.CompressImage;
import com.appsinventiv.corperstrip.Utils.GifSizeFilter;
import com.appsinventiv.corperstrip.Utils.PrefManager;
import com.appsinventiv.corperstrip.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    Button update;
    DatabaseReference mDatabase;
    EditText e_fullname, e_username, e_password, e_phone, e_city, e_school;
    String fullname, username, password, phone, school, city;
    long time;
    String profileUrl;
    List<Uri> mSelected = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    StorageReference mStorageRef;
    CircleImageView profile_image;
    private static final int REQUEST_CODE_CHOOSE = 23;
    RelativeLayout wholeLayout;
    ArrayList<String> groupList = new ArrayList<>();

    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getPermissions();

        profile_image=findViewById(R.id.profile_image);
        wholeLayout = findViewById(R.id.wholeLayout);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.setTitle("My Profile");


        e_fullname = findViewById(R.id.name);
        e_username = findViewById(R.id.groupName);
        e_password = findViewById(R.id.password);
        e_phone = findViewById(R.id.phone);
        e_city = findViewById(R.id.city);
        e_school = findViewById(R.id.school);

        update = findViewById(R.id.update);


        mDatabase.child("Users").child(SharedPrefs.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    user=dataSnapshot.getValue(User.class);
                    if(user!=null){
                        Glide.with(EditProfile.this).load(user.getPicUrl()).into(profile_image);
                        e_fullname.setText(user.getName());
                        e_username.setText(user.getUsername());
                        e_password.setText(user.getPassword());
                        e_phone.setText(user.getPhone());
                        e_city.setText(user.getCity());
                        e_school.setText(user.getSchool());
                        for (String s:user.getInGroupsList()){
                          groupList.add(s);
                        }


                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelected.clear();
                imageUrl.clear();
                initMatisse();
            }
        });



        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (e_fullname.getText().toString().length() == 0) {
                    e_fullname.setError("Cannot be null");
                } else if (e_username.getText().toString().length() == 0) {
                    e_username.setError("Cannot be null");
                } else if (e_password.getText().toString().length() == 0) {
                    e_password.setError("Cannot be null");
                } else if (e_phone.getText().toString().length() == 0) {
                    e_phone.setError("Cannot be null");
                } else if (e_city.getText().toString().length() == 0) {
                    e_city.setError("Cannot be null");
                } else if (e_city.getText().toString().length() == 0) {
                    e_city.setError("Cannot be null");
                }  else {
                    fullname = e_fullname.getText().toString();
                    username = e_username.getText().toString();
                    password = e_password.getText().toString();
                    phone = e_phone.getText().toString();
                    city = e_city.getText().toString();
                    school = e_school.getText().toString();
                    wholeLayout.setVisibility(View.VISIBLE);
                    time = System.currentTimeMillis();
                    mDatabase.child("Users")
                            .child(SharedPrefs.getUsername())
                            .setValue(new User(username,
                                    fullname,
                                    password,
                                    phone,
                                    city,
                                    school,
                                    SharedPrefs.getFcmKey(),
                                    "",
                                    user.getTime(),
                                    groupList
                            ))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    CommonUtils.showToast("Profile Updated");
//                                        Toast.makeText(Register.this, "Thankyou for registering", Toast.LENGTH_SHORT).show();
                                    SharedPrefs.setName(fullname);
                                    Intent i=new Intent(EditProfile.this,ChatList.class);
                                    startActivity(i);

                                    if(imageUrl.size()>0) {
//                                        CommonUtils.showToast("not empty");
                                        for (String im : imageUrl) {
                                            putPictures(im);
                                        }
                                    }else {
//                                        CommonUtils.showToast(" empty");

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfile.this, "There was some error", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
















    }
    public void putPictures(String path) {
        String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

        ;
        Uri file = Uri.fromFile(new File(path));

        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        profileUrl = "" + downloadUrl;
                        SharedPrefs.setPicUrl(profileUrl);
                        mDatabase.child("Users").child(username).child("picUrl").setValue("" + downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {


                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        CommonUtils.showToast(exception.getMessage() + "");

                    }
                });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = Matisse.obtainResult(data);
            Glide.with(EditProfile.this).load(mSelected.get(0)).into(profile_image);
            for (Uri img : mSelected) {
                CompressImage compressImage = new CompressImage(EditProfile.this);
                imageUrl.add(compressImage.compressImage("" + img));
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void initMatisse() {
        Matisse.from(EditProfile.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(1)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }
    private void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        };

        if (!hasPermissions(EditProfile.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }


    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {

                }
            }
        }
        return true;
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
