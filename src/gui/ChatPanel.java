package gui;
import main.ThemeConstants;
import main.WhisprApp;
import model.Chat;
import model.Message;
import model.User;
import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.*;

/** Shows one conversation, lets the user reply, and delete messages or the whole chat. */
public class ChatPanel extends JPanel {
    private final WhisprApp app;
    private final JLabel headerLabel = new JLabel("Chat", SwingConstants.CENTER);
    private final DefaultListModel<Message> messageListModel = new DefaultListModel<>();
    private final JList<Message> messageList = new JList<>(messageListModel);
    private final JTextField inputField = new JTextField();
    private Chat chat;
    private String returnScreen = "LANDING";

    public ChatPanel(WhisprApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(6, 0));
        headerPanel.setBackground(ThemeConstants.MAROON);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JButton backButton = GuiUtil.button("< Back");
        headerLabel.setForeground(ThemeConstants.WHITE);
        headerLabel.setFont(ThemeConstants.FONT_HEADER.deriveFont(17f));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        JButton deleteMessageButton = GuiUtil.button("Delete Msg");
        JButton deleteChatButton = GuiUtil.button("Delete Chat");
        right.add(deleteMessageButton);
        right.add(deleteChatButton);
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(right, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Messages
        messageList.setBackground(ThemeConstants.BACKGROUND);
        messageList.setCellRenderer(new MessageCellRenderer());
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(messageList), BorderLayout.CENTER);

        // Reply box
        JPanel inputPanel = new JPanel(new BorderLayout(6, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        inputPanel.setBackground(ThemeConstants.BACKGROUND);
        JButton sendButton = GuiUtil.button("Send");
        sendButton.setBackground(ThemeConstants.MAROON);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        backButton.addActionListener(e -> app.showScreen(returnScreen));
        deleteMessageButton.addActionListener(e -> deleteSelectedMessage());
        deleteChatButton.addActionListener(e -> deleteChat());
    }

    /**
     * Opens a conversation.
     * @param highlight a message to select and scroll to (e.g. from search), or null to show the newest message
     * @param returnScreen where the Back button should go
     */
    public void showChat(Chat chat, Message highlight, String returnScreen) {
        this.chat = chat;
        this.returnScreen = returnScreen;
        User user = app.getAppState().getUser();
        headerLabel.setText(chat.getParticipantNames(user, app.getAppState().getContacts()));
        reload();
        int target = -1;
        if (highlight != null) {
            for (int i = 0; i < messageListModel.size(); i++)
                if (messageListModel.get(i) == highlight) { target = i; break; }
        }
        if (target >= 0) messageList.setSelectedIndex(target); else messageList.clearSelection();
        final int scrollTo = target >= 0 ? target : messageListModel.size() - 1;
        SwingUtilities.invokeLater(() -> { if (scrollTo >= 0) messageList.ensureIndexIsVisible(scrollTo); });
        inputField.requestFocusInWindow();
    }

    private void reload() {
        messageListModel.clear();
        for (Message m : chat.getMessages().toList()) messageListModel.addElement(m);
    }

    private void sendMessage() {
        if (chat == null) return;
        String content = inputField.getText().trim();
        if (content.isEmpty()) return;
        User user = app.getAppState().getUser();
        Message m = new Message(user.getUserId(), content, LocalDateTime.now(), true);
        chat.addMessage(m);
        chat.setHasUnread(false);
        messageListModel.addElement(m);
        inputField.setText("");
        messageList.ensureIndexIsVisible(messageListModel.getSize() - 1);
        app.markChanged();
    }

    private void deleteSelectedMessage() {
        int index = messageList.getSelectedIndex();
        if (index < 0) {
            GuiUtil.info(this, "Click a message first, then press Delete Msg.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete the selected message?", "Delete message",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        chat.getMessages().deleteMessage(index);
        chat.refreshLastMessageTime();
        reload();
        app.markChanged();
    }

    private void deleteChat() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete this whole chat? This cannot be undone.", "Delete chat", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) app.deleteChat(chat);
    }

    /** A speech bubble: sent messages on the right in maroon, received on the left in grey. */
    private class MessageCellRenderer implements ListCellRenderer<Message> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message m, int index,
                                                      boolean selected, boolean focus) {
            User user = app.getAppState().getUser();
            boolean sent = m.getSenderId().equals(user.getUserId());
            String sender = sent ? "You" : (chat == null ? "" : headerLabel.getText());

            JLabel bubble = new JLabel("<html><body style='width:230px'><small>"
                    + GuiUtil.escape(sender) + " &middot; " + GuiUtil.escape(m.formatTimestamp()) + "</small><br>"
                    + GuiUtil.escape(m.getContent()).replace("\n", "<br>") + "</body></html>");
            bubble.setOpaque(true);
            bubble.setFont(ThemeConstants.FONT_MAIN);
            bubble.setBackground(sent ? ThemeConstants.BUBBLE_SENT : ThemeConstants.BUBBLE_RECEIVED);
            bubble.setForeground(sent ? ThemeConstants.WHITE : ThemeConstants.TEXT_DARK);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? new Color(255, 170, 0) : bubble.getBackground(), 2),
                    BorderFactory.createEmptyBorder(5, 9, 5, 9)));

            JPanel row = new JPanel(new FlowLayout(sent ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 3));
            row.setBackground(ThemeConstants.BACKGROUND);
            row.add(bubble);
            return row;
        }
    }
}
