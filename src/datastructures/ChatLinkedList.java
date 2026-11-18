package datastructures;
import model.Chat;
import model.User;
import managers.ContactManager;
import java.util.ArrayList;

public class ChatLinkedList {
    private ChatNode head;
    private ChatNode tail;
    private int size;

    public ChatLinkedList() { head = null; tail = null; size = 0; }

    public void addToFront(Chat chat) {
        ChatNode newNode = new ChatNode(chat);
        if (head == null) { head = tail = newNode; }
        else { newNode.next = head; head.prev = newNode; head = newNode; }
        size++;
    }

    public void addToBack(Chat chat) {
        ChatNode newNode = new ChatNode(chat);
        if (tail == null) { head = tail = newNode; }
        else { tail.next = newNode; newNode.prev = tail; tail = newNode; }
        size++;
    }

    public void remove(String chatId) {
        ChatNode current = head;
        while (current != null) {
            if (current.chat.getChatId().equals(chatId)) {
                if (current.prev != null) current.prev.next = current.next;
                else head = current.next;
                if (current.next != null) current.next.prev = current.prev;
                else tail = current.prev;
                size--;
                return;
            }
            current = current.next;
        }
    }

    public Chat findById(String chatId) {
        ChatNode current = head;
        while (current != null) {
            if (current.chat.getChatId().equals(chatId)) return current.chat;
            current = current.next;
        }
        return null;
    }

    /** Finds the one-to-one chat between two participant IDs, or null if there is none. */
    public Chat findDirectChat(String idA, String idB) {
        ChatNode current = head;
        while (current != null) {
            java.util.List<String> p = current.chat.getParticipantIds();
            if (p.size() == 2 && p.contains(idA) && p.contains(idB)) return current.chat;
            current = current.next;
        }
        return null;
    }

    /** Removes every chat that includes the given participant. Returns how many were removed. */
    public int removeChatsWith(String participantId) {
        int removed = 0;
        for (Chat c : toList()) {
            if (c.hasParticipant(participantId)) { remove(c.getChatId()); removed++; }
        }
        return removed;
    }

    /** Updates every chat after a contact's ID (phone number) changed. */
    public void replaceParticipant(String oldId, String newId) {
        ChatNode current = head;
        while (current != null) {
            if (current.chat.hasParticipant(oldId)) current.chat.replaceParticipant(oldId, newId);
            current = current.next;
        }
    }

    public ArrayList<Chat> toList() {
        ArrayList<Chat> list = new ArrayList<>();
        ChatNode current = head;
        while (current != null) { list.add(current.chat); current = current.next; }
        return list;
    }

    public void sortByRecent() {
        ArrayList<Chat> list = toList();
        list.sort((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()));
        rebuild(list);
    }

    public void sortByName(ContactManager cm, User user) {
        ArrayList<Chat> list = toList();
        list.sort((a, b) -> a.getParticipantNames(user, cm).compareTo(b.getParticipantNames(user, cm)));
        rebuild(list);
    }

    private void rebuild(ArrayList<Chat> list) {
        head = tail = null; size = 0;
        for (Chat c : list) addToBack(c);
    }

    public int getSize() { return size; }
}