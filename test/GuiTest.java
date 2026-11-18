import main.*;
import model.*;
import managers.SearchManager.SearchResult;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Drives the real Swing UI on a virtual display: clicks buttons and lists, checks results, takes screenshots. */
public class GuiTest {
    static WhisprApp app; static Robot robot;
    static int passed = 0, failed = 0;
    static final AtomicInteger dialogCount = new AtomicInteger();
    static volatile String[] preferred = {"Delete contact and their chats", "Yes", "OK", "Close"};
    static volatile String[] formFill = null;
    static final List<String> dialogLog = Collections.synchronizedList(new ArrayList<>());

    static void check(String n, boolean ok) { if (ok) passed++; else failed++; System.out.println((ok ? "  PASS  " : "  FAIL  ") + n); }
    static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
    static void pause(int ms) throws Exception { Thread.sleep(ms); }

    static void shot(String name, Window w) throws Exception {
        Rectangle b = w.getBounds();
        BufferedImage img = robot.createScreenCapture(b);
        ImageIO.write(img, "png", new File(System.getProperty("shots", "shots") + "/" + name + ".png"));
    }

    static List<Component> all(Container c) {
        List<Component> out = new ArrayList<>();
        for (Component k : c.getComponents()) { out.add(k); if (k instanceof Container) out.addAll(all((Container) k)); }
        return out;
    }
    @SuppressWarnings("unchecked")
    static <T extends Component> List<T> showing(Container root, Class<T> type) {
        List<T> out = new ArrayList<>();
        for (Component k : all(root)) if (type.isInstance(k) && k.isShowing()) out.add((T) k);
        return out;
    }
    static JButton button(String text) {
        for (JButton b : showing(app, JButton.class)) if (text.equals(b.getText())) return b;
        throw new RuntimeException("No showing button: " + text);
    }
    static void click(String text) throws Exception { JButton b = button(text); edt(b::doClick); pause(250); }
    static JList<?> theList() { return showing(app, JList.class).get(0); }
    static void clickRow(JList<?> list, int row) throws Exception {
        edt(() -> {
            Rectangle r = list.getCellBounds(row, row);
            Point p = new Point(r.x + 10, r.y + r.height / 2);
            long now = System.currentTimeMillis();
            for (java.awt.event.MouseListener ml : list.getMouseListeners())
                ml.mouseClicked(new MouseEvent(list, MouseEvent.MOUSE_CLICKED, now, 0, p.x, p.y, 1, false));
        });
        pause(300);
    }
    static String visibleCard() {
        for (Component k : app.getContentPane().getComponents())
            if (k instanceof JPanel) for (Component c : ((JPanel) k).getComponents()) if (c.isVisible()) return c.getClass().getSimpleName();
        return "?";
    }

    /** Background thread that photographs any dialog, then presses the preferred button. */
    static void startDismisser() {
        Thread t = new Thread(() -> {
            Set<Window> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            while (true) {
                try {
                    Thread.sleep(150);
                    for (Window w : Window.getWindows()) {
                        if (!(w instanceof JDialog) || !w.isShowing() || seen.contains(w)) continue;
                        seen.add(w);
                        Thread.sleep(400);
                        String title = ((JDialog) w).getTitle();
                        dialogLog.add(title);
                        shot(String.format("dialog_%02d_%s", dialogCount.incrementAndGet(), title.replaceAll("\\W+", "_")), w);
                        String[] fill = formFill;
                        List<JTextField> tf = new ArrayList<>();
                        for (Component c : all(w)) if (c instanceof JTextField) tf.add((JTextField) c);
                        if (fill != null && tf.size() == 2) {
                            formFill = null;
                            SwingUtilities.invokeAndWait(() -> { tf.get(0).setText(fill[0]); tf.get(1).setText(fill[1]); });
                            Thread.sleep(200);
                            shot(String.format("dialog_form_%02d", dialogCount.get()), w);
                        }
                        JButton chosen = null;
                        outer:
                        for (String want : preferred)
                            for (Component c : all(w)) if (c instanceof JButton && want.equals(((JButton) c).getText())) { chosen = (JButton) c; break outer; }
                        if (chosen != null) { JButton fc = chosen; SwingUtilities.invokeLater(fc::doClick); }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void main(String[] args) throws Exception {
        new File(System.getProperty("shots", "shots")).mkdirs();
        startDismisser();
        edt(() -> app = new WhisprApp());
        pause(1000);
        robot = new Robot();
        AppState st = app.getAppState();

        System.out.println("Landing screen");
        shot("01_landing", app);
        JList<?> chatList = theList();
        check("three chats listed", chatList.getModel().getSize() == 3);
        check("newest chat first", ((Chat) chatList.getModel().getElementAt(0)).getChatId().equals("chat1"));
        check("unread chat is flagged", st.getChats().findById("chat2").isHasUnread());

        System.out.println("Open a chat, reply, go back");
        clickRow(chatList, 0);
        check("clicking a chat opens the chat screen", visibleCard().equals("ChatPanel"));
        check("chat shows all 5 messages", theList().getModel().getSize() == 5);
        JTextField input = showing(app, JTextField.class).get(0);
        edt(() -> input.setText("See you at <b>6</b> & bring | snacks"));
        click("Send");
        check("reply is added to the conversation", theList().getModel().getSize() == 6);
        check("reply is stored in the chat", st.getChats().findById("chat1").getLastMessage().getContent().contains("bring | snacks"));
        check("input box is cleared after sending", input.getText().isEmpty());
        shot("02_chat", app);
        click("< Back");
        check("Back returns to the landing page", visibleCard().equals("LandingPanel"));

        System.out.println("Unread chat is marked read when opened");
        JList<?> list2 = theList();
        int idxChat2 = -1;
        for (int i = 0; i < list2.getModel().getSize(); i++) if (((Chat) list2.getModel().getElementAt(i)).getChatId().equals("chat2")) idxChat2 = i;
        clickRow(list2, idxChat2);
        check("chat2 no longer unread", !st.getChats().findById("chat2").isHasUnread());
        click("< Back");

        System.out.println("Search");
        click("Search");
        check("Search screen opens", visibleCard().equals("SearchPanel"));
        JTextField sf = showing(app, JTextField.class).get(0);
        edt(() -> sf.setText("let's"));
        click("Search");
        JList<?> results = theList();
        check("search finds 2 messages", results.getModel().getSize() == 2);
        shot("03_search_results", app);
        clickRow(results, 0);
        check("clicking a result opens its chat", visibleCard().equals("ChatPanel"));
        check("the matching message is selected in the chat", theList().getSelectedIndex() >= 0);
        shot("04_chat_from_search", app);
        click("< Back");
        check("Back returns to the search results", visibleCard().equals("SearchPanel") && theList().getModel().getSize() == 2);
        edt(() -> sf.setText("zzzznotfound"));
        click("Search");
        check("search with no matches shows an empty list", theList().getModel().getSize() == 0);
        click("< Back");

        System.out.println("Contacts");
        click("Contacts");
        check("Contacts screen opens", visibleCard().equals("ContactsPanel"));
        JList<?> cl = theList();
        click("Sort A-Z");
        check("Sort A-Z", ((Contact) cl.getModel().getElementAt(1)).getName().equals("Jamie"));
        click("Sort Recent");
        check("Sort Recent puts the newest contact first and Jamie last", ((Contact) cl.getModel().getElementAt(3)).getName().equals("Jamie"));
        edt(() -> cl.setSelectedIndex(3));
        preferred = new String[]{"Close"};
        click("View Contact");
        check("View Contact dialog appeared", dialogLog.stream().anyMatch(s -> s.equals("Contact")));
        shot("05_contacts", app);

        System.out.println("Profile");
        click("< Back");
        click("Profile");
        check("Profile screen opens", visibleCard().equals("ProfilePanel"));
        List<JTextField> pf = showing(app, JTextField.class);
        edt(() -> pf.get(1).setText("Alex Test"));
        preferred = new String[]{"OK"};
        click("Save Changes");
        check("profile name saved", st.getUser().getName().equals("Alex Test"));
        edt(() -> pf.get(2).setText("abc"));
        int before = dialogCount.get();
        click("Save Changes");
        pause(1200);
        check("invalid phone number is rejected with an error", dialogCount.get() > before && !st.getUser().getPhoneNumber().equals("abc"));
        shot("06_profile", app);
        click("< Back");
        check("landing header shows the new name", showing(app, JLabel.class).stream().anyMatch(l -> l.getText().contains("Alex Test")));

        System.out.println("Add and edit contacts");
        click("Contacts");
        JList<?> cl3 = theList();
        int n0 = cl3.getModel().getSize();
        preferred = new String[]{"OK"};
        formFill = new String[]{"Riley Stone", "07712 345678"};
        click("Add Contact");
        pause(600);
        check("Add Contact adds a contact (spaces removed from number)", cl3.getModel().getSize() == n0 + 1
                && app.getAppState().getContacts().getContactByPhone("07712345678") != null);
        int before2 = dialogCount.get();
        formFill = new String[]{"Copycat", "07712345678"};
        click("Add Contact");
        pause(1500);
        check("adding a duplicate number shows an error and adds nothing", cl3.getModel().getSize() == n0 + 1 && dialogCount.get() >= before2 + 2);
        int jamieRow = -1;
        for (int i = 0; i < cl3.getModel().getSize(); i++) if (((Contact) cl3.getModel().getElementAt(i)).getName().equals("Jamie")) jamieRow = i;
        final int jr = jamieRow;
        edt(() -> cl3.setSelectedIndex(jr));
        formFill = new String[]{"Jamie Rose", "07799999999"};
        click("Edit Contact");
        pause(600);
        AppState se = app.getAppState();
        Chat jc = se.getChats().findById("chat1");
        check("editing a phone number keeps the chat linked to the contact",
                jc.hasParticipant("07799999999") && !jc.hasParticipant("07700000002")
                && jc.getParticipantNames(se.getUser(), se.getContacts()).equals("Jamie Rose"));
        shot("09_contacts_after_edit", app);
        click("< Back");
        check("landing shows the renamed contact", showing(app, JList.class).size() > 0 && ((Chat) theList().getModel().getElementAt(0)).getParticipantNames(se.getUser(), se.getContacts()).equals("Jamie Rose"));

        System.out.println("Save, change, load");
        preferred = new String[]{"Yes", "OK", "Close"};
        click("Save");
        pause(800);
        check("save files written", new File("whispr_data/messages.txt").exists() && new File("whispr_data/user.txt").exists());
        click("New Chat");
        pause(300);
        check("New Chat asked for a contact", dialogLog.stream().anyMatch(s -> s.startsWith("Start a chat")));
        check("New Chat opened a fresh empty chat", visibleCard().equals("ChatPanel") && app.getAppState().getChats().getSize() == 4);
        shot("08_new_chat", app);
        click("< Back");
        // delete a contact and its chat, then load the saved copy back
        click("Contacts");
        JList<?> cl2 = theList();
        int sam = -1;
        for (int i = 0; i < cl2.getModel().getSize(); i++) if (((Contact) cl2.getModel().getElementAt(i)).getName().equals("Sam")) sam = i;
        final int samIdx = sam;
        edt(() -> cl2.setSelectedIndex(samIdx));
        preferred = new String[]{"Delete contact and their chats"};
        click("Delete Contact");
        pause(800);
        check("contact and their chat deleted", st.getContacts().getContactByPhone("07700000003") == null && st.getChats().findById("chat3") == null);
        click("< Back");
        preferred = new String[]{"Yes", "OK"};
        click("Load");
        pause(1500);
        AppState st2 = app.getAppState();
        check("Load restores the saved chat list (3 chats, not 4)", st2.getChats().getSize() == 3);
        check("Load restores the saved data", st2.getContacts().getContactByPhone("07700000003") != null && st2.getChats().findById("chat3") != null);
        check("Load restores the reply and read state", st2.getChats().findById("chat1").getLastMessage().getContent().contains("bring | snacks")
                && !st2.getChats().findById("chat2").isHasUnread());
        check("Load restores the edited profile", st2.getUser().getName().equals("Alex Test"));
        check("landing is shown after loading", visibleCard().equals("LandingPanel"));
        shot("07_after_load", app);

        System.out.println("Delete a chat");
        JList<?> l3 = theList();
        clickRow(l3, 0);
        preferred = new String[]{"Yes"};
        click("Delete Chat");
        pause(800);
        check("chat deleted and landing shown", app.getAppState().getChats().getSize() == 2 && visibleCard().equals("LandingPanel"));

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
