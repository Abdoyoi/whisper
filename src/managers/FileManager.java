package managers;
import model.User;
import model.Contact;
import model.Chat;
import model.Message;
import datastructures.ChatLinkedList;
import main.AppState;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Saves and loads the whole app to plain text files (user, contacts, chats, messages).
 * Fields are separated by '|'. Any '|', backslash or line break inside a field is escaped,
 * so messages can contain any text. Chats and messages are tied together by the chat ID.
 */
public class FileManager {

    // ---------------------------------------------------------------- saving

    public void saveAll(User user, ChatLinkedList chats, ContactManager contacts, String directory) throws IOException {
        Files.createDirectories(Paths.get(directory));
        saveUser(user, directory);
        saveContacts(contacts, directory);
        saveChats(chats, directory);
        saveMessages(chats, directory);
    }

    public boolean hasSavedData(String directory) {
        return Files.exists(Paths.get(directory, "user.txt"));
    }

    private void saveUser(User user, String directory) throws IOException {
        write(directory, "user.txt", List.of(
                line(user.getUserId(), user.getName(), user.getPhoneNumber(), user.getProfilePicturePath())));
    }

    private void saveContacts(ContactManager contacts, String directory) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Contact c : contacts.getContactsInAddedOrder())
            lines.add(line(c.getName(), c.getPhoneNumber(), c.getProfilePicturePath()));
        write(directory, "contacts.txt", lines);
    }

    private void saveChats(ChatLinkedList chats, String directory) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Chat c : chats.toList())
            lines.add(line(c.getChatId(), String.join(",", c.getParticipantIds()), String.valueOf(c.isHasUnread())));
        write(directory, "chats.txt", lines);
    }

    private void saveMessages(ChatLinkedList chats, String directory) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Chat c : chats.toList())
            for (Message m : c.getMessages().toList())
                lines.add(line(c.getChatId(), m.getSenderId(), m.getContent(),
                        m.getTimestamp().toString(), String.valueOf(m.isRead())));
        write(directory, "messages.txt", lines);
    }

    private void write(String directory, String file, List<String> lines) throws IOException {
        Files.write(Paths.get(directory, file), lines, StandardCharsets.UTF_8);
    }

    // --------------------------------------------------------------- loading

    /** Loads everything from the folder. Throws IOException if the user file is missing or unreadable. */
    public AppState loadAll(String directory) throws IOException {
        User user = loadUser(directory);
        ContactManager contacts = loadContacts(directory);
        ChatLinkedList chats = loadChats(directory);
        loadMessages(chats, directory);
        chats.sortByRecent();
        return new AppState(user, chats, contacts);
    }

    private User loadUser(String directory) throws IOException {
        List<String> lines = readLines(directory, "user.txt");
        if (lines.isEmpty()) throw new IOException("user.txt is empty");
        String[] p = fields(lines.get(0));
        if (p.length < 3) throw new IOException("user.txt is not in the expected format");
        return new User(p[0], p[1], p[2], p.length > 3 ? p[3] : "");
    }

    private ContactManager loadContacts(String directory) throws IOException {
        ContactManager cm = new ContactManager();
        for (String line : readLinesIfPresent(directory, "contacts.txt")) {
            String[] p = fields(line);
            if (p.length < 2) continue;
            try {
                cm.addContact(new Contact(p[0], p[1], p.length > 2 ? p[2] : ""));
            } catch (IllegalArgumentException duplicate) {
                // skip a duplicate phone number in a hand-edited file
            }
        }
        return cm;
    }

    private ChatLinkedList loadChats(String directory) throws IOException {
        ChatLinkedList chats = new ChatLinkedList();
        for (String line : readLinesIfPresent(directory, "chats.txt")) {
            String[] p = fields(line);
            if (p.length < 2) continue;
            Chat chat = new Chat(p[0], new ArrayList<>(Arrays.asList(p[1].split(","))));
            chat.setHasUnread(p.length > 2 && Boolean.parseBoolean(p[2]));
            chats.addToBack(chat);
        }
        return chats;
    }

    private void loadMessages(ChatLinkedList chats, String directory) throws IOException {
        for (String line : readLinesIfPresent(directory, "messages.txt")) {
            String[] p = fields(line);
            if (p.length < 4) continue;
            Chat chat = chats.findById(p[0]);
            if (chat == null) continue;
            try {
                boolean read = p.length < 5 || Boolean.parseBoolean(p[4]);
                chat.addMessage(new Message(p[1], p[2], LocalDateTime.parse(p[3]), read));
            } catch (java.time.format.DateTimeParseException badDate) {
                // skip a line with a damaged timestamp
            }
        }
    }

    private List<String> readLines(String directory, String file) throws IOException {
        return Files.readAllLines(Paths.get(directory, file), StandardCharsets.UTF_8);
    }

    private List<String> readLinesIfPresent(String directory, String file) throws IOException {
        Path path = Paths.get(directory, file);
        if (!Files.exists(path)) return new ArrayList<>();
        List<String> lines = new ArrayList<>();
        for (String l : Files.readAllLines(path, StandardCharsets.UTF_8))
            if (!l.isEmpty()) lines.add(l);
        return lines;
    }

    // ------------------------------------------------------ field escaping

    private static String line(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '|':  sb.append("\\p");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Splits a saved line into its fields, undoing the escaping. Keeps empty trailing fields. */
    static String[] fields(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                char n = line.charAt(++i);
                switch (n) {
                    case 'p': cur.append('|');  break;
                    case 'n': cur.append('\n'); break;
                    case 'r': cur.append('\r'); break;
                    default:  cur.append(n);
                }
            } else if (c == '|') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
