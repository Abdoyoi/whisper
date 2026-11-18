package gui;
import main.ThemeConstants;
import main.WhisprApp;
import model.User;
import java.awt.*;
import javax.swing.*;

/** View and edit the user's own profile. */
public class ProfilePanel extends JPanel {
    private final WhisprApp app;
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();

    public ProfilePanel(WhisprApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BACKGROUND);

        JButton backButton = GuiUtil.button("< Back");
        backButton.addActionListener(e -> { refresh(); app.showScreen("LANDING"); });
        add(GuiUtil.header("Profile", backButton), BorderLayout.NORTH);

        idField.setEditable(false);
        idField.setToolTipText("Your user ID links your messages to you, so it cannot be changed.");
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
        form.setBackground(ThemeConstants.BACKGROUND);
        form.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));
        form.add(new JLabel("User ID (fixed):"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Phone number:"));
        form.add(phoneField);
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(ThemeConstants.BACKGROUND);
        formWrapper.add(form, BorderLayout.NORTH);
        add(formWrapper, BorderLayout.CENTER);

        JButton saveButton = GuiUtil.button("Save Changes");
        saveButton.setBackground(ThemeConstants.MAROON);
        saveButton.addActionListener(e -> saveChanges());
        JPanel south = new JPanel();
        south.setBackground(ThemeConstants.BACKGROUND);
        south.add(saveButton);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    /** Fills the fields from the current user (also discards unsaved edits). */
    public void refresh() {
        User user = app.getAppState().getUser();
        idField.setText(user.getUserId());
        nameField.setText(user.getName());
        phoneField.setText(user.getPhoneNumber());
    }

    private void saveChanges() {
        String newName = nameField.getText().trim();
        String newPhone = phoneField.getText().replaceAll("\\s", "");
        if (newName.isEmpty()) { GuiUtil.error(this, "Please enter a name."); return; }
        if (!newPhone.matches("\\+?[0-9]{6,15}")) {
            GuiUtil.error(this, "Please enter a valid phone number (6 to 15 digits, optionally starting with +).");
            return;
        }
        User user = app.getAppState().getUser();
        user.setName(newName);
        user.setPhoneNumber(newPhone);
        app.markChanged();
        app.refreshLanding();
        GuiUtil.info(this, "Profile updated successfully!");
    }
}
