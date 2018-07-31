package com.appsinventiv.corperstrip.Adapters;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.appsinventiv.corperstrip.Activities.UserProfile;
import com.appsinventiv.corperstrip.Activities.ViewPictures;
import com.appsinventiv.corperstrip.BuildConfig;
import com.appsinventiv.corperstrip.Callbacks.FileDownloaded;
import com.appsinventiv.corperstrip.Model.ChatModel;
import com.appsinventiv.corperstrip.R;
import com.appsinventiv.corperstrip.Utils.CommonUtils;
import com.appsinventiv.corperstrip.Utils.Constants;
import com.appsinventiv.corperstrip.Utils.DownloadFile;
import com.appsinventiv.corperstrip.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AliAh on 24/06/2018.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> implements Handler.Callback {
    Context context;
    ArrayList<ChatModel> chatList;

    public int RIGHT_CHAT = 1;
    public int LEFT_CHAT = 0;
    private MediaPlayer mediaPlayer;
    private ViewHolder mAudioPlayingHolder;
    private int mPlayingPosition = -1;
    private Handler uiUpdateHandler = new Handler(this);


    private static final int MSG_UPDATE_SEEK_BAR = 1845;

    public ChatAdapter(Context context, ArrayList<ChatModel> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (viewType == RIGHT_CHAT) {
            View view = LayoutInflater.from(context).inflate(R.layout.right_chat_layout, parent, false);
            viewHolder = new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.left_chat_layout, parent, false);
            viewHolder = new ViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ChatModel model = chatList.get(position);

        if (getItemViewType(position) == LEFT_CHAT) {
            if (model.getMessageType().equals(Constants.MESSAGE_TYPE_IMAGE)) {
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
                holder.msgtext.setVisibility(View.GONE);
                holder.name.setText(model.getName());
                holder.image.setVisibility(View.VISIBLE);
                holder.document.setVisibility(View.GONE);

                Glide.with(context).load(model.getPicUrl()).into(holder.profile);
                Glide.with(context).load(model.getImageUrl()).into(holder.image);
                holder.audio.setVisibility(View.GONE);


            } else if (model.getMessageType().equals(Constants.MESSAGE_TYPE_TEXT)) {
                holder.image.setVisibility(View.GONE);
                holder.name.setText(model.getName());
                holder.msgtext.setVisibility(View.VISIBLE);
                holder.document.setVisibility(View.GONE);

                holder.msgtext.setText(model.getText());
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
                Glide.with(context).load(model.getPicUrl()).into(holder.profile);
                holder.audio.setVisibility(View.GONE);


            } else if (model.getMessageType().equals(Constants.MESSAGE_TYPE_DOCUMENT)) {
                holder.image.setVisibility(View.GONE);
                holder.name.setText(model.getName());
                holder.msgtext.setVisibility(View.GONE);
                holder.document.setVisibility(View.VISIBLE);
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
                Glide.with(context).load(model.getPicUrl()).into(holder.profile);
                holder.audio.setVisibility(View.GONE);


            } else if (model.getMessageType().equals(Constants.MESSAGE_TYPE_AUDIO)) {
                holder.image.setVisibility(View.GONE);
                holder.msgtext.setVisibility(View.GONE);
                holder.audio.setVisibility(View.VISIBLE);
                holder.document.setVisibility(View.GONE);

                Glide.with(context).load(model.getPicUrl()).into(holder.profile);
                holder.name.setText(model.getName());
                holder.audioTime.setText(CommonUtils.getDuration(model.getMediaTime()));
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
                if (position == mPlayingPosition) {
                    mAudioPlayingHolder = holder;
                    updatePlayingView();
                } else {
                    updateInitialPlayerView(holder);
                }
            }

        } else {

            if (model.getMessageType().equals(Constants.MESSAGE_TYPE_IMAGE)) {
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
                holder.msgtext.setVisibility(View.GONE);
                holder.audio.setVisibility(View.GONE);
                holder.name.setText(model.getName());
                holder.document.setVisibility(View.GONE);
                holder.image.setVisibility(View.VISIBLE);
                Glide.with(context).load(model.getPicUrl()).into(holder.profile);
                Glide.with(context).load(model.getImageUrl()).into(holder.image);
            } else if (model.getMessageType().equals(Constants.MESSAGE_TYPE_TEXT)) {
                holder.image.setVisibility(View.GONE);
                holder.msgtext.setVisibility(View.VISIBLE);
                holder.name.setText(model.getName());
                holder.audio.setVisibility(View.GONE);
                holder.document.setVisibility(View.GONE);
                Glide.with(context).load(model.getPicUrl()).into(holder.profile);
                holder.msgtext.setText(model.getText());
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
            } else if (model.getMessageType().equals(Constants.MESSAGE_TYPE_DOCUMENT)) {
                holder.image.setVisibility(View.GONE);
                holder.name.setText(model.getName());
                holder.msgtext.setVisibility(View.GONE);
                holder.audio.setVisibility(View.GONE);
                holder.document.setVisibility(View.VISIBLE);
                Glide.with(context).load(model.getPicUrl()).into(holder.profile);
                holder.msgtext.setText(model.getText());
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
            } else if (model.getMessageType().equals(Constants.MESSAGE_TYPE_AUDIO)) {
                holder.image.setVisibility(View.GONE);
                holder.document.setVisibility(View.GONE);
                holder.name.setText(model.getName());
                holder.msgtext.setVisibility(View.GONE);
                holder.audio.setVisibility(View.VISIBLE);
                Glide.with(context).load(model.getPicUrl()).into(holder.profile);

                holder.audioTime.setText(CommonUtils.getDuration(model.getMediaTime()));
                holder.time.setText("" + CommonUtils.getFormattedDate(model.getTime()));
                if (position == mPlayingPosition) {
                    mAudioPlayingHolder = holder;
                    updatePlayingView();
                } else {
                    updateInitialPlayerView(holder);
                }
            }
        }

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserProfile.class);
                i.putExtra("username", model.getMessageBy());
                context.startActivity(i);
            }
        });
        holder.document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DownloadFile.fromUrl1(model.getDocumentUrl());
                String filename = "" + model.getDocumentUrl().substring(model.getDocumentUrl().length() - 7, model.getDocumentUrl().length());
                File applictionFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" + filename + model.getMediaType());

                if (applictionFile != null && applictionFile.exists()) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(applictionFile), getMimeType(applictionFile.getAbsolutePath()));
                    context.startActivity(intent);

                } else {
                    DownloadFile.fromUrl1(model.getDocumentUrl(), filename + model.getMediaType(), new FileDownloaded() {
                        @Override
                        public void onFileDownloaded(String filename) {
                            CommonUtils.showToast("downloaded");
                            File applictionFile = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);

                            intent.setDataAndType(Uri.fromFile(applictionFile), getMimeType(applictionFile.getAbsolutePath()));
                            context.startActivity(intent);
                        }
                    });
//                    Intent intent = new Intent();
//                    intent.setAction(android.content.Intent.ACTION_VIEW);
//
//                    intent.setDataAndType(Uri.fromFile(applictionFile),getMimeType(applictionFile.getAbsolutePath()));
//                    context.startActivity(intent);
                }
            }
        });


        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ViewPictures.class);
                i.putExtra("url", model.getImageUrl());
                context.startActivity(i);
                String filename = "" + model.getImageUrl().substring(model.getImageUrl().length() - 7, model.getImageUrl().length());


                File applictionFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" + filename + ".png");

                if (applictionFile != null && applictionFile.exists()) {
                } else {
                    DownloadFile.fromUrl(model.getImageUrl(), filename + ".png");


                }


            }
        });

        holder.playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performPlayButtonClick(model, holder);
//                if (!isPlaying[0]) {
//
//                    stopPlayer(holder);
//                    holder.playPause.setImageResource(R.drawable.play_btn);
//                    startMediaPlayer(model);
//                    isPlaying[0] = true;
//                } else {
//                    holder.playPause.setImageResource(R.drawable.play_btn);
//                    isPlaying[0] = false;
//                    releaseMediaPlayer();
//
//                }


            }
        });


    }


    private String getMimeType(String url) {
        String parts[] = url.split("\\.");
        String extension = parts[parts.length - 1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private void updateInitialPlayerView(ViewHolder holder) {
        if (holder == mAudioPlayingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
        holder.seekBar.setProgress(0);
        holder.playPause.setImageResource(R.drawable.play_btn);
    }

    private void updatePlayingView() {
        if (mediaPlayer == null || mAudioPlayingHolder == null) return;
        mAudioPlayingHolder.seekBar.setProgress(mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());

        if (mediaPlayer.isPlaying()) {
            uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
            mAudioPlayingHolder.playPause.setImageResource(R.drawable.stop);

        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
            mAudioPlayingHolder.playPause.setImageResource(R.drawable.play_btn);
        }
        mAudioPlayingHolder.audioTime.setText(CommonUtils.getDuration(mediaPlayer.getCurrentPosition()));

    }

    private void startMediaPlayer(ChatModel model) {


        try {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer = MediaPlayer.create(context, Uri.parse(model.getAudioUrl()));
            } catch (Exception e) {
                e.printStackTrace();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(model.getAudioUrl());
            }
            if (mediaPlayer == null) return;
            mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
            if (mAudioPlayingHolder != null)
                mediaPlayer.seekTo(mAudioPlayingHolder.seekBar.getProgress());
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemViewType(int position) {
        ChatModel model = chatList.get(position);
        if (model.getMessageBy().equals(SharedPrefs.getUsername())) {
            return RIGHT_CHAT;
        } else {
            return LEFT_CHAT;
        }

    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_UPDATE_SEEK_BAR: {

                int percentage = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
                mAudioPlayingHolder.seekBar.setProgress(percentage);
                mAudioPlayingHolder.audioTime.setText(CommonUtils.getDuration(mediaPlayer.getCurrentPosition()));
                uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
                return true;
            }
        }
        return false;
    }

    private void performPlayButtonClick(ChatModel recordingItem, ViewHolder myViewHolder) {

        int currentPosition = chatList.indexOf(recordingItem);
        if (currentPosition == mPlayingPosition) {
            // toggle between play/pause of audio
            if (mediaPlayer == null) return;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        } else {
            // start another audio playback
            ChatModel previousPlayObject = mPlayingPosition == -1 ? null : chatList.get(mPlayingPosition);
            mPlayingPosition = currentPosition;
            if (mediaPlayer != null) {
                if (null != mAudioPlayingHolder) {
                    if (previousPlayObject != null)
                        mAudioPlayingHolder.audioTime.setText(CommonUtils.getDuration(previousPlayObject.getMediaTime()));
                    updateNonPlayingView(mAudioPlayingHolder);
                }
                mediaPlayer.release();
            }
            mAudioPlayingHolder = myViewHolder;
            startMediaPlayer(recordingItem);
        }
        updatePlayingView();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements SeekBar.OnSeekBarChangeListener {
        TextView msgtext, time, name, audioTime;
        CircleImageView profile;
        ImageView image, playPause, document;
        RelativeLayout audio;
        SeekBar seekBar;

        public ViewHolder(View itemView) {
            super(itemView);
            msgtext = itemView.findViewById(R.id.msgtext);
            time = itemView.findViewById(R.id.time);
            profile = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            audio = itemView.findViewById(R.id.audio);
            playPause = itemView.findViewById(R.id.playPause);
            audioTime = itemView.findViewById(R.id.audioTime);
            seekBar = itemView.findViewById(R.id.seek);
            document = itemView.findViewById(R.id.document);

//            seekBar.setOnSeekBarChangeListener(this);


        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            if (b && mediaPlayer != null && getAdapterPosition() == mPlayingPosition)
                mediaPlayer.seekTo(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private void updateNonPlayingView(ViewHolder holder) {
        if (holder == mAudioPlayingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
        holder.seekBar.setProgress(0);
        holder.playPause.setImageResource(R.drawable.play_btn);
    }

    private void releaseMediaPlayer() {

        if (null != mAudioPlayingHolder) {
            updateNonPlayingView(mAudioPlayingHolder);
        }
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mPlayingPosition = -1;
    }

    public void stopPlayer() {
        if (null != mediaPlayer) {
            releaseMediaPlayer();
        }
    }


}
