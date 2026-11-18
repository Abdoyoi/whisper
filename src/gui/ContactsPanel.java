package gui;
import main.AppState;
import main.ThemeConstants;
import main.WhisprApp;
import managers.ContactManager;
import managers.SearchManager.SearchResult;
import model.Contact;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

/** Contacts: add, edit, delete, sort, and view a contact with their three latest messages. */
public class ContactsPanel extends JPanel {
    private final WhisprApp app;
    private final DefaultListModel<Contact> contactsListModel = new DefaultListModel<>();
    private final JList<Contact> contactsList = new JList<>(contactsListModel);
    private final JLabel sortLabel = new JLabel(" ", SwingConstants.CENTER);

    public ContactsPanel(WhisprApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BACKGROUND);

        JButton backButton = GuiUtil.button("< Back");
        backButton.addActionListener(e -> app.showScreen("LANDING"));
        add(GuiUtil.header("Contacts", backButton), BorderLayout.NORTH);

        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setFixedCellHeight(52);
        contactsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                Contact c = (Contact) v;
                super.getListCellRendererComponent(l, "", i, s, f);
                setText("<html><b>" + GuiUtil.escape(c.getName()) + "</b><br>" + GuiUtil.escape(c.getPhoneNumber()) + "</html>");
                setFont(ThemeConstants.FONT_MAIN);
                setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                setBackground(s ? ThemeConstants.MAROON_LIGHT : (i % 2 == 0 ? ThemeConstants.BACKGROUND : ThemeConstants.WHITE));
                setForeground(s ? ThemeConstants.WHITE : ThemeConstants.TEXT_DARK);
                return this;
            }
        });
        contactsList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onViewContact();
            }
        });
        add(new JScrollPane(contactsList), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(ThemeConstants.BACKGROUND);
        sortLabel.setFont(ThemeConstants.FONT_MAIN);
        south.add(sortLabel, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 4, 4));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
        buttonPanel.setBackground(ThemeConstants.BACKGROUND);
        String[] names = {"Add Contact", "Edit Contact", "Delete Contact", "View Contact", "Sort A-Z", "Sort Recent"};
        Runnable[] actions = {this::onAddContact, this::onEditContact, this::onDeleteContact,
                this::onViewContact, this::onSortAlphabetically, this::onSortByRecent};
        for (int i = 0; i < names.length; i++) {
            JButton b = GuiUtil.button(names[i]);
            Runnable action = actions[i];
            b.addActionListener(e -> action.run());
            buttonPanel.add(b);
        }
        south.add(buttonPanel, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        contactsListModel.clear();
        for (Contact c : app.getAppState().getContacts().getAllContacts()) contactsListModel.addElement(c);
    }

    private Contact selected() {
        Contact c = contactsList.getSelectedValue();
        if (c == null) GuiUtil.info(this, "Select a contact first.");
        return c;
    }

    /** Shows a name/phone form. Returns the new contact, or null if cancelled. */
    private Contact promptContact(String title, Contact existing) {
        JTextField nameField = new JTextField(existing == null ? "" : existing.getName(), 18);
        JTextField phoneField = new JTextField(existing == null ? "" : existing.getPhoneNumber(), 18);
        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Phone number:"));
        form.add(phoneField);
        while (true) {
            int result = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return null;
            String name = nameField.getText().trim();
            String phone = phoneField.getText().replaceAll("\\s", "");
            if (name.isEmpty()) { GuiUtil.error(this, "Please enter a name."); continue; }
            if (!phone.matches("\\+?[0-9]{6,15}")) {
                GuiUtil.error(this, "Please enter a valid phone number (6 to 15 digits, optionally starting with +).");
                continue;
            }
            return new Contact(name, phone, existing == null ? "" : existing.getProfilePicturePath());
        }
    }

    private void onAddContact() {
        Contact c = promptContact("Add contact", null);
        if (c == null) return;
        try {
            app.getAppState().getContacts().addContact(c);
        } catch (IllegalArgumentException ex) {
            GuiUtil.error(this, ex.getMessage());
            return;
        }
        app.markChanged();
        refresh();
    }

    private void onEditContact() {
        Contact old = selected();
        if (old == null) return;
        int index = contactsList.getSelectedIndex();
        Contact updated = promptContact("Edit contact", old);
        if (updated == null) return;
        try {
            app.getAppState().getContacts().editContact(index, updated);
        } catch (IllegalArgumentException ex) {
            GuiUtil.error(this, ex.getMessage());
            return;
        }
        // Chats are linked to a contact by phone number, so keep them linked if it changed
        if (!old.getPhoneNumber().equals(updated.getPhoneNumber()))
            app.getAppState().getChats().replaceParticipant(old.getPhoneNumber(), updated.getPhoneNumber());
        app.markChanged();
        refresh();
        contactsList.setSelectedIndex(index);
    }

    private void onDeleteContact() {
        Contact c = selected();
        if (c == null) return;
        String[] options = {"Delete contact only", "Delete contact and their chats", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Delete " + c.getName() + "?\n\nIf you keep their chats, the old chats stay in your list\n"
                        + "and show the phone number instead of the name.",
                "Delete contact", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
        if (choice != 0 && choice != 1) return;
        AppState state = app.getAppState();
        state.getContacts().deleteContact(contactsList.getSelectedIndex());
        if (choice == 1) state.getChats().removeChatsWith(c.getPhoneNumber());
        app.markChanged();
        refresh();
    }

    private void onSortAlphabetically() {
        app.getAppState().getContacts().sortAlphabetically();
        sortLabel.setText("Sorted A-Z");
        refresh();
    }

    private void onSortByRecent() {
        app.getAppState().getContacts().sortByRecent();
        sortLabel.setText("Sorted by most recently added");
        refresh();
    }

    /** Contact profile plus the three most recent messages received from them. */
    private void onViewContact() {
        Contact c = selected();
        if (c == null) return;
        AppState state = app.getAppState();
        List<SearchResult> recent = app.getSearchManager().recentMessagesFrom(c.getPhoneNumber(), state.getChats(), 3);

        JDialog dialog = new JDialog(app, "Contact", true);
        dialog.setLayout(new BorderLayout(0, 8));
        JLabel info = new JLabel("<html><div style='padding:6px'><b style='font-size:14px'>" + GuiUtil.escape(c.getName())
                + "</b><br>Phone: " + GuiUtil.escape(c.getPhoneNumber())
                + "<br><br><b>Three most recent messages from them</b> (double-click to open the chat):</div></html>");
        dialog.add(info, BorderLayout.NORTH);

        DefaultListModel<SearchResult> model = new DefaultListModel<>();
        for (SearchResult r : recent) model.addElement(r);
        JList<SearchResult> list = new JList<>(model);
        list.setCellRenderer(new ResultCellRenderer(app, Collections::emptyList));
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                SearchResult r = list.getSelectedValue();
                if (e.getClickCount() == 2 && r != null) {
                    dialog.dispose();
                    app.openChat(r.chat, r.message, "CONTACTS");
                }
            }
        });
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);
        if (recent.isEmpty()) dialog.add(new JLabel("  No messages received from this contact yet."), BorderLayout.CENTER);

        JButton close = GuiUtil.button("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel south = new JPanel();
        south.add(close);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.setSize(400, 330);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
