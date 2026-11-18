package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a message in the Whispr application.
 */
public class Message {
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    /**
     * Constructs a Message object.
     *
     * @param senderId  the ID of the sender
     * @param content   the content of the message
     * @param timestamp the timestamp of when the message was sent
     * @param isRead    the read status of the message
     */
    public Message(String senderId, String content, LocalDateTime timestamp, boolean isRead) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Formats the timestamp of the message.
     *
     * @return a string representation of the timestamp in "HH:mm dd/MM/yyyy" format
     */
    public String formatTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        return timestamp.format(formatter);
    }
}

