package com.app.chatapp.models;

import java.util.Date;

public class Message {
    private String senderId, receiverId, message, dateTime;
    private Date date;
    private String conversionId, conversionName, conversionImage;

    public Message() {
    }

    public Message(String senderId, String receiverId, String message, String dateTime, Date date) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.dateTime = dateTime;
        this.date = date;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setConversionId(String conversionId) {
        this.conversionId = conversionId;
    }

    public void setConversionName(String conversionName) {
        this.conversionName = conversionName;
    }

    public void setConversionImage(String conversionImage) {
        this.conversionImage = conversionImage;
    }

    public Date getDate() {
        return date;
    }

    public String getConversionName() {
        return conversionName;
    }

    public String getConversionImage() {
        return conversionImage;
    }

    public String getConversionId() {
        return conversionId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public String getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", message='" + message + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", date=" + date +
                ", conversionId='" + conversionId + '\'' +
                ", conversionName='" + conversionName + '\'' +
                ", conversionImage='" + conversionImage + '\'' +
                '}';
    }
}
