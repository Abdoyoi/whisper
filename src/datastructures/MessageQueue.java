package datastructures;

import model.Message;
import java.util.ArrayList;

/**
 * MessageQueue implements a queue data structure for messages.
 * It allows enqueueing, dequeueing, and managing messages in a FIFO manner.
 */
public class MessageQueue {
    private MessageNode head;
    private MessageNode tail;
    private int size;

    public MessageQueue() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Adds a message to the end of the queue.
     * 
     * @param message The message to be added.
     */
    public void enqueue(Message message) {
        MessageNode newNode = new MessageNode(message);
        if (tail != null) {
            tail.next = newNode;
        }
        tail = newNode;
        if (head == null) {
            head = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the message at the front of the queue.
     * 
     * @return The message at the front of the queue.
     */
    public Message dequeue() {
        if (isEmpty()) {
            return null;
        }
        Message message = head.message;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return message;
    }

    /**
     * Returns the message at the front of the queue without removing it.
     * 
     * @return The message at the front of the queue.
     */
    public Message peek() {
        return isEmpty() ? null : head.message;
    }

    /**
     * Returns the most recently added message (the back of the queue) without removing it.
     *
     * @return The newest message, or null if the queue is empty.
     */
    public Message peekLast() {
        return isEmpty() ? null : tail.message;
    }

    /**
     * Checks if the queue is empty.
     * 
     * @return true if the queue is empty, false otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of messages in the queue.
     * 
     * @return The size of the queue.
     */
    public int size() {
        return size;
    }

    /**
     * Converts the queue to an ArrayList of messages.
     * 
     * @return An ArrayList containing all messages in the queue.
     */
    public ArrayList<Message> toList() {
        ArrayList<Message> messages = new ArrayList<>();
        MessageNode current = head;
        while (current != null) {
            messages.add(current.message);
            current = current.next;
        }
        return messages;
    }

    /**
     * Deletes a message at the specified index.
     * 
     * @param index The index of the message to be deleted.
     */
    public void deleteMessage(int index) {
        if (index < 0 || index >= size) {
            return;
        }
        if (index == 0) {
            dequeue();
            return;
        }
        MessageNode current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }
        current.next = current.next.next;
        if (current.next == null) {
            tail = current;
        }
        size--;
    }
}

