package main;
import model.User;
import model.Contact;
import model.Message;
import model.Chat;
import datastructures.ChatLinkedList;
import managers.ContactManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SampleData {
    public static AppState load() {
        User user = new User("1", "Alex Johnson", "07700000001", "");
        ContactManager cm = new ContactManager();
        cm.addContact(new Contact("Jamie", "07700000002", ""));
        cm.addContact(new Contact("Sam", "07700000003", ""));
        cm.addContact(new Contact("Priya", "07700000004", ""));
        cm.addContact(new Contact("Group Chat", "07700000005", ""));

        ChatLinkedList chats = new ChatLinkedList();

        Chat c1 = new Chat("chat1", List.of("1", "07700000002"));
        c1.addMessage(new Message("1", "Hey Jamie!", LocalDateTime.now().minusMinutes(5), true));
        c1.addMessage(new Message("07700000002", "Hi Alex!", LocalDateTime.now().minusMinutes(4), true));
        c1.addMessage(new Message("1", "How are you?", LocalDateTime.now().minusMinutes(3), true));
        c1.addMessage(new Message("07700000002", "I'm good, thanks!", LocalDateTime.now().minusMinutes(2), true));
        c1.addMessage(new Message("1", "Let's meet up!", LocalDateTime.now().minusMinutes(1), false));
        chats.addToFront(c1);

        Chat c2 = new Chat("chat2", List.of("1", "07700000004"));
        c2.addMessage(new Message("1", "Hi Priya!", LocalDateTime.now().minusDays(1).minusMinutes(10), true));
        c2.addMessage(new Message("07700000004", "Hello Alex!", LocalDateTime.now().minusDays(1).minusMinutes(9), true));
        c2.addMessage(new Message("1", "Are you free tomorrow?", LocalDateTime.now().minusDays(1).minusMinutes(8), true));
        c2.addMessage(new Message("07700000004", "Yes, let's catch up!", LocalDateTime.now().minusDays(1).minusMinutes(7), false));
        c2.setHasUnread(true);
        chats.addToFront(c2);

        Chat c3 = new Chat("chat3", List.of("1", "07700000003"));
        c3.addMessage(new Message("1", "Group meeting at 5 PM?", LocalDateTime.now().minusDays(2).minusMinutes(15), true));
        c3.addMessage(new Message("07700000003", "Sure, I'll be there!", LocalDateTime.now().minusDays(2).minusMinutes(14), true));
        c3.addMessage(new Message("1", "Don't forget to prepare!", LocalDateTime.now().minusDays(2).minusMinutes(13), true));
        chats.addToFront(c3);

        return new AppState(user, chats, cm);
    }
}