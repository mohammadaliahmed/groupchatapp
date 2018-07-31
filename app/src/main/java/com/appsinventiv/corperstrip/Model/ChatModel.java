package com.appsinventiv.corperstrip.Model;

/**
 * Created by AliAh on 09/07/2018.
 */

public class ChatModel {
    String id,text,messageBy,imageUrl,audioUrl,videoUrl,documentUrl;
    String username,name,picUrl;
    String messageType,mediaType;
    String groupId;
    long time;
    long mediaTime;

    public ChatModel(String id, String text, String messageBy, String imageUrl, String audioUrl, String videoUrl, String documentUrl, String username, String name, String picUrl, String messageType, String mediaType, String groupId, long time, long mediaTime) {
        this.id = id;
        this.text = text;
        this.messageBy = messageBy;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.videoUrl = videoUrl;
        this.documentUrl = documentUrl;
        this.username = username;
        this.name = name;
        this.picUrl = picUrl;
        this.messageType = messageType;
        this.mediaType = mediaType;
        this.groupId = groupId;
        this.time = time;
        this.mediaTime = mediaTime;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getMediaTime() {
        return mediaTime;
    }

    public void setMediaTime(long mediaTime) {
        this.mediaTime = mediaTime;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ChatModel() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMessageBy() {
        return messageBy;
    }

    public void setMessageBy(String messageBy) {
        this.messageBy = messageBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
