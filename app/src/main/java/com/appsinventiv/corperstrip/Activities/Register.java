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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.appsinventiv.corperstrip.Model.User;
import com.appsinventiv.corperstrip.R;
import com.appsinventiv.corperstrip.Utils.CommonUtils;
import com.appsinventiv.corperstrip.Utils.CompressImage;
import com.appsinventiv.corperstrip.Utils.Constants;
import com.appsinventiv.corperstrip.Utils.GifSizeFilter;
import com.appsinventiv.corperstrip.Utils.PrefManager;
import com.appsinventiv.corperstrip.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class Register extends AppCompatActivity {
    Button signup;
    Button login;
    DatabaseReference mDatabase;
    private PrefManager prefManager;
    ArrayList<String> userslist = new ArrayList<String>();
    EditText e_fullname, e_username, e_password, e_phone, e_school;
    String fullname, username, password, phone, school, city;
    long time;
    String profileUrl;
    List<Uri> mSelected = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    StorageReference mStorageRef;


    CircleImageView profile_image;
    private static final int REQUEST_CODE_CHOOSE = 23;
    RelativeLayout wholeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getPermissions();

        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }
        profile_image=findViewById(R.id.profile_image);
        wholeLayout = findViewById(R.id.wholeLayout);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        mDatabase.child(Constants.USERS_REFERENCE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                userslist.add(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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
        final String[] items = new String[]{"Select City", "Abia" , "Adamawa" , "Akwa Ibom" , "Anambra" , "Bauchi" , "Bayelsa" , "Benue" , "Borno" , "Cross River" , "Delta" , "Ebonyi" , "Enugu" , "Edo" , "Ekiti" , "Gombe" , "Imo" , "Jigawa" , "Kaduna" , "Kano" , "Katsina" , "Kebbi" , "Kogi" , "Kwara" , "Lagos" , "Nasarawa" , "Niger" , "Ogun" , "Ondo" , "Osun" , "Oyo" , "Plateau" , "Rivers" , "Sokoto" , "Taraba" , "Yobe" , "Zanfara" , "FCT Abuja"};
        Spinner spinner = findViewById(R.id.locationchoose);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {

                city = items[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        e_fullname = findViewById(R.id.name);
        e_username = findViewById(R.id.groupName);
        e_password = findViewById(R.id.password);
        e_phone = findViewById(R.id.phone);
        e_school = findViewById(R.id.school);

        signup = findViewById(R.id.register);
        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Register.this, Login.class);
                startActivity(i);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
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
                }  else if (e_school.getText().toString().length() == 0) {
                    e_school.setError("Cannot be null");
                } else if (mSelected.isEmpty()) {
                    CommonUtils.showToast("Please select picture");
                }else if (city.equals("Select City")) {
                    CommonUtils.showToast("Please select City");
                }
                else {
                    fullname = e_fullname.getText().toString();
                    username = e_username.getText().toString();
                    password = e_password.getText().toString();
                    phone = e_phone.getText().toString();
                    school = e_school.getText().toString();

                    if (userslist.contains("" + username)) {
                        Toast.makeText(Register.this, "Username is already taken\nPlease choose another", Toast.LENGTH_SHORT).show();
                    } else {
                        wholeLayout.setVisibility(View.VISIBLE);
                        time = System.currentTimeMillis();
                        ArrayList<String> groupList = new ArrayList<>();
                        mDatabase.child("Users")
                                .child(username)
                                .setValue(new User(username,
                                        fullname,
                                        password,
                                        phone,
                                        city,
                                        school,
                                        SharedPrefs.getFcmKey(),
                                        "",
                                        System.currentTimeMillis(),
                                        groupList
                                ))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
//                                        Toast.makeText(Register.this, "Thankyou for registering", Toast.LENGTH_SHORT).show();
                                        SharedPrefs.setUsername(username);
                                        SharedPrefs.setName(fullname);
                                        SharedPrefs.setIsLoggedIn("yes");

                                        for (String im : imageUrl) {
                                            putPictures(im);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Register.this, "There was some error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                }
            }
        });


    }

    private void initMatisse() {
        Matisse.from(Register.this)
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = Matisse.obtainResult(data);
            Glide.with(Register.this).load(mSelected.get(0)).into(profile_image);
            for (Uri img : mSelected) {
                CompressImage compressImage = new CompressImage(Register.this);
                imageUrl.add(compressImage.compressImage("" + img));
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
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
                                CommonUtils.showToast("Thank you for registering");
                                launchHomeScreen();
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


    private void launchHomeScreen() {
//        prefManager.setFirstTimeLaunch(false);

        startActivity(new Intent(Register.this, Login.class));

        finish();
    }

    private void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        };

        if (!hasPermissions(Register.this, PERMISSIONS)) {
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

}
