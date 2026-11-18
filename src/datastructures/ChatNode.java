package datastructures;
import model.Chat;

public class ChatNode {
    Chat chat;
    ChatNode next;
    ChatNode prev;

    public ChatNode(Chat chat) {
        this.chat = chat;
        this.next = null;
        this.prev = null;
    }
    public Chat getChat() { return chat; }
}