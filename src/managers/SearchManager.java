package managers;
import model.Chat;
import model.Message;
import model.User;
import datastructures.ChatLinkedList;
import java.util.ArrayList;
import java.util.List;

public class SearchManager {

    public static class SearchResult {
        public Chat chat;
        public Message message;
        public SearchResult(Chat chat, Message message) {
            this.chat = chat;
            this.message = message;
        }
    }

    /** Splits a search string into lower-case keywords (separated by whitespace). */
    public static List<String> parseKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        if (text == null) return keywords;
        for (String k : text.trim().toLowerCase().split("\\s+"))
            if (!k.isEmpty()) keywords.add(k);
        return keywords;
    }

    /**
     * Finds individual messages that contain ALL of the keywords (case-insensitive).
     * Results are returned newest first.
     */
    public List<SearchResult> search(String keyword, ChatLinkedList chats, ContactManager cm, User user) {
        List<SearchResult> results = new ArrayList<>();
        List<String> keywords = parseKeywords(keyword);
        if (keywords.isEmpty()) return results;
        for (Chat chat : chats.toList()) {
            for (Message message : chat.getMessages().toList()) {
                String text = message.getContent().toLowerCase();
                boolean matchesAll = true;
                for (String k : keywords) {
                    if (!text.contains(k)) { matchesAll = false; break; }
                }
                if (matchesAll) results.add(new SearchResult(chat, message));
            }
        }
        results.sort((a, b) -> b.message.getTimestamp().compareTo(a.message.getTimestamp()));
        return results;
    }

    /** The most recent messages received from one sender (by ID / phone number), newest first. */
    public List<SearchResult> recentMessagesFrom(String senderId, ChatLinkedList chats, int limit) {
        List<SearchResult> all = new ArrayList<>();
        for (Chat chat : chats.toList())
            for (Message message : chat.getMessages().toList())
                if (message.getSenderId().equals(senderId)) all.add(new SearchResult(chat, message));
        all.sort((a, b) -> b.message.getTimestamp().compareTo(a.message.getTimestamp()));
        return all.size() > limit ? new ArrayList<>(all.subList(0, limit)) : all;
    }
}
