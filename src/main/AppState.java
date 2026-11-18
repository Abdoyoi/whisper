package main;
import model.User;
import datastructures.ChatLinkedList;
import managers.ContactManager;

public class AppState {
    private User user;
    private ChatLinkedList chats;
    private ContactManager contacts;

    public AppState(User user, ChatLinkedList chats, ContactManager contacts) {
        this.user = user;
        this.chats = chats;
        this.contacts = contacts;
    }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ChatLinkedList getChats() { return chats; }
    public void setChats(ChatLinkedList chats) { this.chats = chats; }
    public ContactManager getContacts() { return contacts; }
    public void setContacts(ContactManager contacts) { this.contacts = contacts; }
}