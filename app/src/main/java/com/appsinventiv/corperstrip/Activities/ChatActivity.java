package com.appsinventiv.corperstrip.Activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsinventiv.corperstrip.Adapters.ChatAdapter;
import com.appsinventiv.corperstrip.Model.ChatModel;
import com.appsinventiv.corperstrip.Model.MediaModel;
import com.appsinventiv.corperstrip.Model.User;
import com.appsinventiv.corperstrip.R;
import com.appsinventiv.corperstrip.Utils.CommonUtils;
import com.appsinventiv.corperstrip.Utils.CompressImage;
import com.appsinventiv.corperstrip.Utils.Constants;
import com.appsinventiv.corperstrip.Utils.GifSizeFilter;
import com.appsinventiv.corperstrip.Utils.NotificationAsync;
import com.appsinventiv.corperstrip.Utils.SharedPrefs;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
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
import com.jaeger.library.StatusBarUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    RecordView recordView;
    RecordButton recordButton;
    DatabaseReference mDatabase;
    EditText message;
    ImageView send, back;
    RelativeLayout attachArea;
    ImageView attach;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    ChatAdapter adapter;
    ArrayList<ChatModel> chatModelArrayList = new ArrayList<>();
    int soundId;
    SoundPool sp;
    ArrayList<User> usersFcmKey = new ArrayList<>();
    private AdView mAdView;
    ImageView pick,document;
    List<Uri> mSelected = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    StorageReference mStorageRef;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private static final int REQUEST_CODE_FILE = 25;


    RelativeLayout recordingArea;
    RelativeLayout messagingArea;
    private static final String LOG_TAG = "AudioRecordTest";
    String groupName;
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;

    private MediaPlayer mPlayer = null;

    String recordingLocalUrl;
    long recordingTime = 0L;
    private InterstitialAd mInterstitialAd;
    String groupId;
    boolean isAttachAreaVisible=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        StatusBarUtil.setTransparent(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        getPermissions();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        send = findViewById(R.id.send);
        attach=findViewById(R.id.attach);
        attachArea=findViewById(R.id.attachArea);
        message = findViewById(R.id.message);
        pick = findViewById(R.id.pick);
        document=findViewById(R.id.document);

        recordView = (RecordView) findViewById(R.id.record_view);
        recordButton = (RecordButton) findViewById(R.id.record_button);
        recordingArea = findViewById(R.id.recordingArea);
        messagingArea = findViewById(R.id.messageArea);
        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");
        groupName = i.getStringExtra("groupName");
        this.setTitle(groupName);


        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachArea.setVisibility(View.GONE);
                isAttachAreaVisible=false;
                openFile(REQUEST_CODE_FILE);
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAttachAreaVisible){
                    attachArea.setVisibility(View.GONE);
                    isAttachAreaVisible=false;
                }else{
                    attachArea.setVisibility(View.VISIBLE);
                    isAttachAreaVisible=true;
                }
            }
        });



        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        soundId = sp.load(ChatActivity.this, R.raw.tick_sound, 1);



//        intersititialAd();
        recordButton.setRecordView(recordView);


        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    recordingArea.setVisibility(View.VISIBLE);
                    send.setVisibility(View.GONE);


                } else {
                    recordingArea.setVisibility(View.GONE);
                    send.setVisibility(View.VISIBLE);
                }
            }
        });

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
                mRecorder = null;
                setMargins(recyclerView, 0, 0, 0, 170);
                startRecording();


            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");
                mRecorder.release();
                mRecorder = null;
                setMargins(recyclerView, 0, 0, 0, 0);


            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                String time = CommonUtils.getFormattedDate(recordTime);
                Log.d("RecordView", "onFinish");

                setMargins(recyclerView, 0, 0, 0, 0);

                Log.d("RecordTime", time);
                recordingTime = recordTime;
                messagingArea.setVisibility(View.VISIBLE);
                stopRecording();
                recyclerView.scrollToPosition(chatModelArrayList.size() - 1);


            }

            @Override
            public void onLessThanSecond() {

                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");
                setMargins(recyclerView, 0, 0, 0, 0);

                messagingArea.setVisibility(View.VISIBLE);
                mRecorder = null;
                recyclerView.scrollToPosition(chatModelArrayList.size() - 1);


            }
        });


        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                setMargins(recyclerView, 0, 0, 0, 170);

                Log.d("RecordView", "Basket Animation Finished");
                messagingArea.setVisibility(View.VISIBLE);
                setMargins(recyclerView, 0, 0, 0, 0);


                recyclerView.scrollToPosition(chatModelArrayList.size() - 1);


            }
        });
        recordView.setSoundEnabled(true);

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachArea.setVisibility(View.GONE);
                isAttachAreaVisible=false;

                mSelected.clear();
                imageUrl.clear();
                initMatisse();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


//
                if (message.getText().length() == 0) {
                    message.setError("Can't send empty message");
                } else {
                    sendMessageToServer(Constants.MESSAGE_TYPE_TEXT, "","");
                }
            }
        });
    }

//    private void intersititialAd() {
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId("ca-app-pub-8010167933323069/8411687508");
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());
//
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
//                } else {
//                    Log.d("TAG", "The interstitial wasn't loaded yet.");
//                }
//                handler.postDelayed(this, 120000);
//            }
//        }, 0);
//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdClosed() {
//                // Load the next interstitial.
//                mInterstitialAd.loadAd(new AdRequest.Builder().build());
//            }
//
//        });
//
//    }


    @Override
    protected void onResume() {
        super.onResume();
        sendFcmKeyToServer();
        getUserFcmKeys();
        getMessagesFromServer();

    }

    private void startRecording() {
        messagingArea.setVisibility(View.GONE);

        recordingLocalUrl = Long.toHexString(Double.doubleToLongBits(Math.random()));
        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(mFileName + recordingLocalUrl + ".mp3");
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "prepare() failed");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    messagingArea.setVisibility(View.VISIBLE);

                }

            }
        }, 100);


    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        putAudio(mFileName + recordingLocalUrl + ".mp3");
    }

    public ChatActivity() {

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/r";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = Matisse.obtainResult(data);
            for (Uri img : mSelected) {
                CompressImage compressImage = new CompressImage(ChatActivity.this);
                imageUrl.add(compressImage.compressImage("" + img));
            }
            for (String img : imageUrl) {
                putPictures(img);
            }
        }

        if(requestCode==REQUEST_CODE_FILE && data!=null){
            Uri Fpath = data.getData();
            putDocument(Fpath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    public void putDocument(Uri path) {
        String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

//        Uri file = Uri.fromFile(new File(path));

        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = mStorageRef.child("Documents").child(imgName);

        riversRef.putFile(path)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        sendMessageToServer(Constants.MESSAGE_TYPE_DOCUMENT, "" + downloadUrl,getMimeType(ChatActivity.this,path));
//                        mDatabase.child("Media").push().setValue()
                        String k = mDatabase.push().getKey();
                        mDatabase.child("Documents").child(k).setValue(new MediaModel(k, Constants.MESSAGE_TYPE_DOCUMENT, "" + downloadUrl, System.currentTimeMillis()));

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


    public void putPictures(String path) {
        String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

        Uri file = Uri.fromFile(new File(path));

        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        sendMessageToServer(Constants.MESSAGE_TYPE_IMAGE, "" + downloadUrl,getMimeType(ChatActivity.this,file));
//                        mDatabase.child("Media").push().setValue()
                        String k = mDatabase.push().getKey();
                        mDatabase.child("Images").child(k).setValue(new MediaModel(k, Constants.MESSAGE_TYPE_IMAGE, "" + downloadUrl, System.currentTimeMillis()));

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

    public void putAudio(String path) {
        String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

        Uri file = Uri.fromFile(new File(path));


        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = mStorageRef.child("Audio").child(imgName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        sendMessageToServer(Constants.MESSAGE_TYPE_AUDIO, "" + downloadUrl,getMimeType(ChatActivity.this,file));
//                        mDatabase.child("Media").push().setValue()
                        String k = mDatabase.push().getKey();
                        mDatabase.child("Audio").child(k).setValue(new MediaModel(k, Constants.MESSAGE_TYPE_AUDIO, "" + downloadUrl, System.currentTimeMillis()));

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

    private void initMatisse() {
        Matisse.from(ChatActivity.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(10)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private void sendFcmKeyToServer() {
        mDatabase.child("Users").child(SharedPrefs.getUsername()).child("fcmKey").setValue(SharedPrefs.getFcmKey());
    }

    private void getUserFcmKeys() {
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    usersFcmKey.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (!user.getUsername().equals(SharedPrefs.getUsername())) {
                                usersFcmKey.add(user);
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
    private void openFile(Integer  CODE) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, CODE);
    }

    private void sendMessageToServer(final String type, final String url,String extension) {

        final String msg = message.getText().toString();
        message.setText(null);
        final String key = mDatabase.push().getKey();
        mDatabase.child("Chats").child(key).setValue(new ChatModel(
                key,
                msg,
                SharedPrefs.getUsername(),
                type.equals(Constants.MESSAGE_TYPE_IMAGE) ? url : "",
                type.equals(Constants.MESSAGE_TYPE_AUDIO) ? url : "",
                type.equals(Constants.MESSAGE_TYPE_VIDEO) ? url : "",
                type.equals(Constants.MESSAGE_TYPE_DOCUMENT) ? url : "",
                SharedPrefs.getUsername(),
                SharedPrefs.getName(),
                SharedPrefs.getPicUrl(),
                type,
                "."+extension,
                groupId,
                System.currentTimeMillis(),
                recordingTime

        )).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sp.play(soundId, 1, 1, 0, 0, 1);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatModelArrayList.size() - 1);

                if (!usersFcmKey.isEmpty()) {


                    for (User u : usersFcmKey) {
                        NotificationAsync notificationAsync = new NotificationAsync(ChatActivity.this);
                        String NotificationTitle = "New message in " +groupName;
                        String NotificationMessage = "";
                        if (type.equals(Constants.MESSAGE_TYPE_TEXT)) {
                            NotificationMessage = SharedPrefs.getName()+": " + msg;
                        } else if (type.equals(Constants.MESSAGE_TYPE_IMAGE)) {
                            NotificationMessage = SharedPrefs.getName()+": \uD83D\uDCF7 Image";
                        } else if (type.equals(Constants.MESSAGE_TYPE_AUDIO)) {
                            NotificationMessage = SharedPrefs.getName()+": \uD83C\uDFB5 Audio";
                        }
                        else if (type.equals(Constants.MESSAGE_TYPE_DOCUMENT)) {
                            NotificationMessage = SharedPrefs.getName()+": \uD83D\uDCC4 Document";
                        }
                        notificationAsync.execute("ali", u.getFcmKey(), NotificationTitle, NotificationMessage, Constants.MESSAGE_TYPE_TEXT, groupId,groupName);
                    }

                }
                setLastMessageToserver(msg, type, System.currentTimeMillis());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    private void setLastMessageToserver(String msg, String type, long tim) {
        mDatabase.child("Groups").child(groupId).child("lastMessage").setValue(msg);
        mDatabase.child("Groups").child(groupId).child("type").setValue(type);
        mDatabase.child("Groups").child(groupId).child("time").setValue(tim);


    }
    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    private void getMessagesFromServer() {
        recyclerView = findViewById(R.id.chats);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(ChatActivity.this, chatModelArrayList);
        recyclerView.setAdapter(adapter);

        mDatabase.child("Chats").limitToLast(200).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    chatModelArrayList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatModel model = snapshot.getValue(ChatModel.class);
                        if (model != null) {
                            if (model.getGroupId().equals(groupId)) {
                                chatModelArrayList.add(model);
                                Collections.sort(chatModelArrayList, new Comparator<ChatModel>() {
                                    @Override
                                    public int compare(ChatModel listData, ChatModel t1) {
                                        Long ob1 = listData.getTime();
                                        Long ob2 = t1.getTime();

                                        return ob1.compareTo(ob2);

                                    }
                                });
                            }
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(chatModelArrayList.size() - 1);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    recyclerView.scrollToPosition(chatModelArrayList.size() - 1);
                }

            }
        });

    }

    private void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions(ChatActivity.this, PERMISSIONS)) {
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

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {

            finish();
        }

        //noinspection SimplifiableIfStatement
       else if (id == R.id.action_info) {
            Intent i=new Intent(ChatActivity.this,GroupDetails.class);
            i.putExtra("groupId",groupId);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }
}