package main;
import gui.LandingPanel;
import gui.ChatPanel;
import gui.ContactsPanel;
import gui.ProfilePanel;
import gui.SearchPanel;
import managers.FileManager;
import managers.SearchManager;
import model.Chat;
import model.Contact;
import model.Message;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main window. It owns the app state and moves between the screens.
 * If saved data exists in the "whispr_data" folder it is loaded on start-up; otherwise sample data is used.
 */
public class WhisprApp extends JFrame {
    public static final String SAVE_DIR = "whispr_data";

    private AppState state;
    private final FileManager fileManager = new FileManager();
    private final SearchManager searchManager = new SearchManager();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private LandingPanel landingPanel;
    private ChatPanel chatPanel;
    private ContactsPanel contactsPanel;
    private ProfilePanel profilePanel;
    private SearchPanel searchPanel;
    private boolean unsavedChanges = false;

    public WhisprApp() {
        super("Whispr");
        state = loadInitialState();
        buildScreens();
        add(mainPanel);
        setSize(450, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { exitApp(); }
        });
        setLocationRelativeTo(null);
        setVisible(true);
        showScreen("LANDING");
    }

    private AppState loadInitialState() {
        if (fileManager.hasSavedData(SAVE_DIR)) {
            try {
                return fileManager.loadAll(SAVE_DIR);
            } catch (IOException e) {
                System.err.println("Could not read saved data, using sample data: " + e.getMessage());
            }
        }
        return SampleData.load();
    }

    /** (Re)creates every screen from the current state, e.g. after loading a save file. */
    private void buildScreens() {
        mainPanel.removeAll();
        landingPanel = new LandingPanel(this);
        chatPanel = new ChatPanel(this);
        contactsPanel = new ContactsPanel(this);
        profilePanel = new ProfilePanel(this);
        searchPanel = new SearchPanel(this);
        mainPanel.add(landingPanel, "LANDING");
        mainPanel.add(chatPanel, "CHAT");
        mainPanel.add(contactsPanel, "CONTACTS");
        mainPanel.add(profilePanel, "PROFILE");
        mainPanel.add(searchPanel, "SEARCH");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ------------------------------------------------------------ navigation

    public void showScreen(String name) {
        switch (name) {
            case "LANDING":  landingPanel.refresh(); break;
            case "CONTACTS": contactsPanel.refresh(); break;
            case "PROFILE":  profilePanel.refresh(); break;
            case "SEARCH":   searchPanel.rerun(); break;
            default: break;
        }
        cardLayout.show(mainPanel, name);
    }

    /**
     * Opens a chat.
     * @param highlight a message to scroll to and select, or null
     * @param returnScreen the screen the chat's Back button returns to
     */
    public void openChat(Chat chat, Message highlight, String returnScreen) {
        if (chat.isHasUnread()) markChanged();
        chat.markAllRead();
        chatPanel.showChat(chat, highlight, returnScreen);
        cardLayout.show(mainPanel, "CHAT");
    }

    public void createOrOpenChatWith(Contact contact) {
        User user = state.getUser();
        Chat chat = state.getChats().findDirectChat(user.getUserId(), contact.getPhoneNumber());
        if (chat == null) {
            String id = "chat" + System.currentTimeMillis();
            while (state.getChats().findById(id) != null) id += "x";
            List<String> participants = new ArrayList<>();
            participants.add(user.getUserId());
            participants.add(contact.getPhoneNumber());
            chat = new Chat(id, participants);
            state.getChats().addToFront(chat);
            markChanged();
        }
        openChat(chat, null, "LANDING");
    }

    public void deleteChat(Chat chat) {
        state.getChats().remove(chat.getChatId());
        markChanged();
        searchPanel.rerun();
        showScreen("LANDING");
    }

    public void refreshLanding() { landingPanel.refresh(); }

    // ------------------------------------------------------- save and load

    public void markChanged() { unsavedChanges = true; }

    /** Saves everything to the whispr_data folder. Returns true if it worked. */
    public boolean saveData(boolean quiet) {
        try {
            fileManager.saveAll(state.getUser(), state.getChats(), state.getContacts(), SAVE_DIR);
            unsavedChanges = false;
            if (!quiet) JOptionPane.showMessageDialog(this,
                    "Saved to:\n" + new File(SAVE_DIR).getAbsolutePath(), "Save", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save: " + e.getMessage(), "Save", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void loadData() {
        if (!fileManager.hasSavedData(SAVE_DIR)) {
            JOptionPane.showMessageDialog(this, "No saved data found yet. Press Save first.", "Load", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (unsavedChanges) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Loading will replace your current unsaved changes. Continue?", "Load", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
        }
        try {
            state = fileManager.loadAll(SAVE_DIR);
            unsavedChanges = false;
            buildScreens();
            showScreen("LANDING");
            JOptionPane.showMessageDialog(this, "Data loaded.", "Load", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load: " + e.getMessage(), "Load", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitApp() {
        if (unsavedChanges) {
            String[] options = {"Save and exit", "Exit without saving", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this, "You have unsaved changes.", "Exit Whispr",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if (choice == 0) { if (!saveData(true)) return; }
            else if (choice != 1) return;
        }
        dispose();
        System.exit(0);
    }

    public AppState getAppState() { return state; }
    public SearchManager getSearchManager() { return searchManager; }
    public FileManager getFileManager() { return fileManager; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WhisprApp::new);
    }
}
