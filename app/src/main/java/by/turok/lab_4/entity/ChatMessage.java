package by.turok.lab_4.entity;

import java.util.Date;

public class ChatMessage {

    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private String imageName;
    private String fileName;
    private boolean withImage;
    private boolean withFile;
    private String dateTime;
    private Date dateObject;

    public ChatMessage() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }


    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isWithFile() {
        return withFile;
    }

    public void setWithFile(boolean withFile) {
        this.withFile = withFile;
    }

    public boolean isWithImage() {
        return withImage;
    }

    public void setWithImage(boolean withImage) {
        this.withImage = withImage;
    }
}
