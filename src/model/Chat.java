package model;
import datastructures.MessageQueue;
import managers.ContactManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participantIds;
    private MessageQueue messages;
    private boolean hasUnread;
    private LocalDateTime lastMessageTime;

    public Chat(String chatId, List<String> participantIds) {
        this.chatId = chatId;
        this.participantIds = participantIds;
        this.messages = new MessageQueue();
        this.hasUnread = false;
        this.lastMessageTime = LocalDateTime.now();
    }

    public void addMessage(Message m) { messages.enqueue(m); lastMessageTime = m.getTimestamp(); }
    public String getChatId() { return chatId; }
    public List<String> getParticipantIds() { return participantIds; }
    public MessageQueue getMessages() { return messages; }
    public boolean isHasUnread() { return hasUnread; }
    public void setHasUnread(boolean hasUnread) { this.hasUnread = hasUnread; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }

    /** The newest message in the chat (the queue stores oldest first, so this is the back). */
    public Message getLastMessage() { return messages.peekLast(); }

    /** Marks the chat and every message in it as read. */
    public void markAllRead() {
        hasUnread = false;
        for (Message m : messages.toList()) m.setRead(true);
    }

    /** Recalculates the last-message time, e.g. after a message has been deleted. */
    public void refreshLastMessageTime() {
        Message last = messages.peekLast();
        if (last != null) lastMessageTime = last.getTimestamp();
    }

    public boolean hasParticipant(String id) { return participantIds.contains(id); }

    /**
     * Replaces one participant ID with another, e.g. when a contact's phone number is edited,
     * so the chat stays linked to that contact.
     */
    public void replaceParticipant(String oldId, String newId) {
        List<String> updated = new ArrayList<>();
        for (String id : participantIds) updated.add(id.equals(oldId) ? newId : id);
        participantIds = updated;
        for (Message m : messages.toList())
            if (m.getSenderId().equals(oldId)) m.setSenderId(newId);
    }

    public String getParticipantNames(User loggedInUser, ContactManager cm) {
        StringBuilder sb = new StringBuilder();
        for (String id : participantIds) {
            if (!id.equals(loggedInUser.getUserId())) {
                model.Contact c = cm.getContactByPhone(id);
                sb.append(c != null ? c.getName() : id).append(", ");
            }
        }
        return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "Unknown";
    }
}
