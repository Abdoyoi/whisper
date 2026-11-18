package datastructures;
import model.Message;

public class MessageNode {
    Message message;
    MessageNode next;

    public MessageNode(Message message) {
        this.message = message;
        this.next = null;
    }
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    public MessageNode getNext() { return next; }
    public void setNext(MessageNode next) { this.next = next; }
}