package gui;
import main.AppState;
import main.ThemeConstants;
import main.WhisprApp;
import model.Chat;
import model.Contact;
import model.Message;
import model.User;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

/** The home screen: the user's name, the list of chats (newest first) and the main menu. */
public class LandingPanel extends JPanel {
    private final WhisprApp app;
    private final DefaultListModel<Chat> chatListModel = new DefaultListModel<>();
    private final JList<Chat> chatList = new JList<>(chatListModel);
    private final JLabel userNameLabel = new JLabel();
    private final JLabel emptyLabel = new JLabel("No chats yet. Press \"New Chat\" to start one.", SwingConstants.CENTER);

    public LandingPanel(WhisprApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BACKGROUND);

        // Header: app name and user name on top, menu buttons underneath
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeConstants.MAROON);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLabel = new JLabel("  Whispr");
        titleLabel.setForeground(ThemeConstants.WHITE);
        titleLabel.setFont(ThemeConstants.FONT_HEADER);
        top.add(titleLabel, BorderLayout.WEST);
        userNameLabel.setForeground(ThemeConstants.WHITE);
        userNameLabel.setFont(ThemeConstants.FONT_MAIN);
        top.add(userNameLabel, BorderLayout.EAST);
        headerPanel.add(top, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        buttonPanel.setOpaque(false);
        JButton profileButton  = GuiUtil.button("Profile");
        JButton contactsButton = GuiUtil.button("Contacts");
        JButton searchButton   = GuiUtil.button("Search");
        JButton newChatButton  = GuiUtil.button("New Chat");
        JButton saveButton     = GuiUtil.button("Save");
        JButton loadButton     = GuiUtil.button("Load");
        for (JButton b : new JButton[]{profileButton, contactsButton, searchButton, newChatButton, saveButton, loadButton})
            buttonPanel.add(b);
        headerPanel.add(buttonPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Chat list
        chatList.setBackground(ThemeConstants.BACKGROUND);
        chatList.setCellRenderer(new ChatCellRenderer());
        chatList.setFixedCellHeight(58);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(chatList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        emptyLabel.setFont(ThemeConstants.FONT_MAIN);
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(emptyLabel, BorderLayout.SOUTH);

        // Actions
        profileButton.addActionListener(e -> app.showScreen("PROFILE"));
        contactsButton.addActionListener(e -> app.showScreen("CONTACTS"));
        searchButton.addActionListener(e -> app.showScreen("SEARCH"));
        newChatButton.addActionListener(e -> onNewChat());
        saveButton.addActionListener(e -> app.saveData(false));
        loadButton.addActionListener(e -> app.loadData());

        // Open a chat with a click, or with Enter on the selected chat
        chatList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int i = chatList.locationToIndex(e.getPoint());
                if (i >= 0 && chatList.getCellBounds(i, i).contains(e.getPoint()))
                    app.openChat(chatListModel.get(i), null, "LANDING");
            }
        });
        chatList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openChat");
        chatList.getActionMap().put("openChat", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Chat c = chatList.getSelectedValue();
                if (c != null) app.openChat(c, null, "LANDING");
            }
        });

        refresh();
    }

    /** Reloads the chat list from the app state, newest chat first. */
    public void refresh() {
        AppState state = app.getAppState();
        state.getChats().sortByRecent();
        userNameLabel.setText(state.getUser().getName() + "  ");
        chatListModel.clear();
        for (Chat c : state.getChats().toList()) chatListModel.addElement(c);
        emptyLabel.setVisible(chatListModel.isEmpty());
    }

    private void onNewChat() {
        List<Contact> contacts = app.getAppState().getContacts().getAllContacts();
        if (contacts.isEmpty()) {
            GuiUtil.info(this, "You have no contacts yet. Add a contact first.");
            return;
        }
        JComboBox<Contact> combo = new JComboBox<>(contacts.toArray(new Contact[0]));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                Contact c = (Contact) v;
                return super.getListCellRendererComponent(l, c.getName() + "  (" + c.getPhoneNumber() + ")", i, s, f);
            }
        });
        int choice = JOptionPane.showConfirmDialog(this, combo, "Start a chat with...",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.OK_OPTION && combo.getSelectedItem() != null)
            app.createOrOpenChatWith((Contact) combo.getSelectedItem());
    }

    /** One row: name (with an unread marker), the last message, and its date and time. */
    private class ChatCellRenderer extends JPanel implements ListCellRenderer<Chat> {
        private final JLabel nameLabel = new JLabel();
        private final JLabel previewLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();

        ChatCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            JPanel text = new JPanel(new GridLayout(2, 1));
            text.setOpaque(false);
            text.add(nameLabel);
            text.add(previewLabel);
            add(text, BorderLayout.CENTER);
            add(timeLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Chat> list, Chat chat, int index,
                                                      boolean selected, boolean focus) {
            AppState st = app.getAppState();
            User user = st.getUser();
            Message last = chat.getLastMessage();
            boolean unread = chat.isHasUnread();
            String name = chat.getParticipantNames(user, st.getContacts());

            nameLabel.setText((unread ? "\u25CF " : "") + name + (unread ? "   NEW" : ""));
            nameLabel.setFont(unread ? ThemeConstants.FONT_BOLD : ThemeConstants.FONT_MAIN);
            if (last == null) {
                previewLabel.setText("No messages yet");
                timeLabel.setText("");
            } else {
                String prefix = last.getSenderId().equals(user.getUserId()) ? "You: " : "";
                previewLabel.setText(prefix + GuiUtil.shorten(last.getContent(), 34));
                timeLabel.setText(last.formatTimestamp());
            }
            previewLabel.setFont(ThemeConstants.FONT_MAIN);
            timeLabel.setFont(ThemeConstants.FONT_MAIN);

            Color bg = selected ? ThemeConstants.MAROON_LIGHT : (index % 2 == 0 ? ThemeConstants.BACKGROUND : ThemeConstants.WHITE);
            Color fg = selected ? ThemeConstants.WHITE : ThemeConstants.TEXT_DARK;
            setBackground(bg);
            nameLabel.setForeground(selected ? fg : (unread ? ThemeConstants.MAROON : fg));
            previewLabel.setForeground(selected ? fg : Color.DARK_GRAY);
            timeLabel.setForeground(selected ? fg : Color.DARK_GRAY);
            return this;
        }
    }
}
