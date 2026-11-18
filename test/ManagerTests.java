import datastructures.*;
import main.AppState;
import managers.*;
import managers.SearchManager.SearchResult;
import model.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/** Plain-Java tests for the data structures and managers. Run: java -cp out:test-out ManagerTests */
public class ManagerTests {
    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("  PASS  " + name); }
        else    { failed++; System.out.println("  FAIL  " + name); }
    }

    public static void main(String[] args) throws Exception {
        LocalDateTime t0 = LocalDateTime.of(2026, 11, 18, 12, 0);

        System.out.println("MessageQueue");
        MessageQueue q = new MessageQueue();
        for (int i = 0; i < 4; i++) q.enqueue(new Message("1", "m" + i, t0.plusMinutes(i), true));
        check("peek is oldest, peekLast is newest", q.peek().getContent().equals("m0") && q.peekLast().getContent().equals("m3"));
        q.deleteMessage(3);
        check("deleting the last message moves the tail", q.peekLast().getContent().equals("m2") && q.size() == 3);
        q.enqueue(new Message("1", "m4", t0, true));
        check("enqueue after deleting the tail still works", q.toList().size() == 4 && q.peekLast().getContent().equals("m4"));
        q.deleteMessage(0);
        check("deleting the first message works", q.peek().getContent().equals("m1") && q.size() == 3);
        q.deleteMessage(99);
        check("out-of-range delete is ignored", q.size() == 3);

        System.out.println("ContactManager");
        ContactManager cm = new ContactManager();
        cm.addContact(new Contact("Zoe", "07700000010", ""));
        cm.addContact(new Contact("Adam", "07700000011", ""));
        cm.addContact(new Contact("Maya", "07700000012", ""));
        boolean dup = false;
        try { cm.addContact(new Contact("Other", "07700000011", "")); } catch (IllegalArgumentException e) { dup = true; }
        check("duplicate phone number is rejected", dup && cm.size() == 3);
        cm.sortAlphabetically();
        check("sort A-Z", cm.getAllContacts().get(0).getName().equals("Adam") && cm.getAllContacts().get(2).getName().equals("Zoe"));
        cm.sortByRecent();
        check("sort recent = newest added first (even after A-Z)", cm.getAllContacts().get(0).getName().equals("Maya")
                && cm.getAllContacts().get(2).getName().equals("Zoe"));
        boolean rejected = false;
        try { cm.editContact(0, new Contact("Maya2", "07700000010", "")); } catch (IllegalArgumentException e) { rejected = true; }
        boolean stillBlocked = false;
        try { cm.addContact(new Contact("X", "07700000012", "")); } catch (IllegalArgumentException e) { stillBlocked = true; }
        check("a rejected edit keeps the old phone number reserved", rejected && stillBlocked);
        cm.editContact(0, new Contact("Maya Renamed", "07700000099", ""));
        check("edit works and new number resolves", cm.getContactByPhone("07700000099").getName().equals("Maya Renamed")
                && cm.getContactByPhone("07700000012") == null);
        cm.deleteContact(0);
        check("delete removes from both lists", cm.size() == 2 && cm.getContactsInAddedOrder().size() == 2);
        cm.addContact(new Contact("Maya Again", "07700000099", ""));
        check("a deleted number can be reused", cm.size() == 3);

        System.out.println("Chat / ChatLinkedList");
        ChatLinkedList chats = new ChatLinkedList();
        Chat a = new Chat("chatA", new ArrayList<>(List.of("1", "111111")));
        a.addMessage(new Message("1", "Hello Tom, meeting at five", t0, true));
        a.addMessage(new Message("111111", "OK see you THERE", t0.plusMinutes(1), false));
        a.addMessage(new Message("111111", "Bring the report", t0.plusMinutes(2), false));
        a.addMessage(new Message("111111", "And snacks", t0.plusMinutes(3), false));
        Chat b = new Chat("chatB", new ArrayList<>(List.of("1", "222222")));
        b.addMessage(new Message("222222", "meeting moved to six there", t0.plusMinutes(10), true));
        b.setHasUnread(true);
        chats.addToBack(a); chats.addToBack(b);
        check("getLastMessage returns the newest message", a.getLastMessage().getContent().equals("And snacks"));
        check("findDirectChat", chats.findDirectChat("1", "222222") == b && chats.findDirectChat("1", "999") == null);
        chats.sortByRecent();
        check("sortByRecent puts the chat with the newest message first", chats.toList().get(0) == b);
        a.markAllRead();
        check("markAllRead clears unread flags", !a.isHasUnread() && a.getMessages().toList().stream().allMatch(Message::isRead));
        chats.replaceParticipant("222222", "333333");
        check("replaceParticipant updates chat links", b.hasParticipant("333333") && b.getMessages().toList().get(0).getSenderId().equals("333333"));

        System.out.println("SearchManager");
        SearchManager sm = new SearchManager();
        User me = new User("1", "Me", "000000", "");
        check("one keyword, case-insensitive", sm.search("THERE", chats, cm, me).size() == 2);
        check("all keywords must match (AND)", sm.search("meeting there", chats, cm, me).size() == 1);
        check("blank search returns nothing", sm.search("   ", chats, cm, me).isEmpty());
        List<SearchResult> r = sm.search("meeting", chats, cm, me);
        check("results are newest first", r.size() == 2 && r.get(0).chat == b);
        List<SearchResult> from = sm.recentMessagesFrom("111111", chats, 3);
        check("three most recent messages from a contact, newest first", from.size() == 3
                && from.get(0).message.getContent().equals("And snacks") && from.get(2).message.getContent().equals("OK see you THERE"));
        check("removeChatsWith", chats.removeChatsWith("111111") == 1 && chats.getSize() == 1);

        System.out.println("FileManager round trip");
        Path dir = Files.createTempDirectory("whispr-test");
        User user = new User("1", "Alex | \"Al\" Johnson", "07700000001", "");   // empty picture path + pipe in name
        ContactManager contacts = new ContactManager();
        contacts.addContact(new Contact("Jamie", "07700000002", ""));          // empty picture path
        contacts.addContact(new Contact("Sam", "07700000003", ""));
        ChatLinkedList saved = new ChatLinkedList();
        Chat c = new Chat("chat1", new ArrayList<>(List.of("1", "07700000002")));
        String nasty = "pipes | and \\ backslash \\n literal\nsecond line, with commas";
        c.addMessage(new Message("1", "first", t0, true));
        c.addMessage(new Message("07700000002", nasty, t0.plusMinutes(1), false));
        c.setHasUnread(true);
        Chat empty = new Chat("chat2", new ArrayList<>(List.of("1", "07700000003")));
        saved.addToBack(c); saved.addToBack(empty);
        FileManager fm = new FileManager();
        check("hasSavedData is false before saving", !fm.hasSavedData(dir.toString()));
        fm.saveAll(user, saved, contacts, dir.toString());
        check("hasSavedData is true after saving", fm.hasSavedData(dir.toString()));
        AppState loaded = fm.loadAll(dir.toString());
        check("user loads (including empty picture path and a pipe in the name)",
                loaded.getUser().getName().equals(user.getName()) && loaded.getUser().getPhoneNumber().equals("07700000001"));
        check("contacts load (empty picture path)", loaded.getContacts().size() == 2
                && loaded.getContacts().getContactByPhone("07700000002").getName().equals("Jamie"));
        Chat lc = loaded.getChats().findById("chat1");
        check("chats load with unread flag", lc != null && lc.isHasUnread() && lc.getParticipantIds().equals(List.of("1", "07700000002")));
        List<Message> lm = lc.getMessages().toList();
        check("messages load in order with all text intact", lm.size() == 2 && lm.get(1).getContent().equals(nasty));
        check("message timestamps and read flags survive", lm.get(1).getTimestamp().equals(t0.plusMinutes(1)) && !lm.get(1).isRead() && lm.get(0).isRead());
        check("a chat with no messages loads", loaded.getChats().findById("chat2") != null && loaded.getChats().findById("chat2").getMessages().isEmpty());
        boolean threw = false;
        try { fm.loadAll(Files.createTempDirectory("whispr-empty").toString()); } catch (IOException e) { threw = true; }
        check("loading from a folder with no save throws IOException", threw);

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
