package gui;
import main.ThemeConstants;
import main.WhisprApp;
import managers.SearchManager.SearchResult;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

/** Draws a message found by search: chat name, time, and the text with keywords in bold. */
public class ResultCellRenderer extends DefaultListCellRenderer {
    private final WhisprApp app;
    private final Supplier<List<String>> keywords;

    public ResultCellRenderer(WhisprApp app, Supplier<List<String>> keywords) {
        this.app = app;
        this.keywords = keywords;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean selected, boolean focus) {
        super.getListCellRendererComponent(list, "", index, selected, focus);
        SearchResult r = (SearchResult) value;
        User user = app.getAppState().getUser();
        String chatName = r.chat.getParticipantNames(user, app.getAppState().getContacts());
        String who = r.message.getSenderId().equals(user.getUserId()) ? "You" : chatName;
        setText("<html><b>" + GuiUtil.escape(chatName) + "</b> &nbsp;<small>"
                + GuiUtil.escape(r.message.formatTimestamp()) + "</small><br>"
                + GuiUtil.escape(who) + ": " + GuiUtil.highlight(GuiUtil.shorten(r.message.getContent(), 70), keywords.get())
                + "</html>");
        setFont(ThemeConstants.FONT_MAIN);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        if (selected) {
            setBackground(ThemeConstants.MAROON_LIGHT);
            setForeground(ThemeConstants.WHITE);
        } else {
            setBackground(index % 2 == 0 ? ThemeConstants.BACKGROUND : ThemeConstants.WHITE);
            setForeground(ThemeConstants.TEXT_DARK);
        }
        return this;
    }
}
