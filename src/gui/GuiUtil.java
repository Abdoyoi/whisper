package gui;
import main.ThemeConstants;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Small helpers shared by the Swing screens. */
public class GuiUtil {

    /** Escapes text so it is safe to place inside the HTML used by Swing labels. */
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Shortens long text to at most max characters. */
    public static String shorten(String s, int max) {
        if (s == null) return "";
        String oneLine = s.replace('\n', ' ');
        return oneLine.length() <= max ? oneLine : oneLine.substring(0, max - 1) + "\u2026";
    }

    /** Escapes text for HTML and makes every occurrence of the keywords bold. */
    public static String highlight(String text, List<String> keywords) {
        String lower = text.toLowerCase();
        if (keywords == null || keywords.isEmpty() || lower.length() != text.length()) return escape(text);
        boolean[] bold = new boolean[text.length()];
        for (String k : keywords) {
            String kl = k.toLowerCase();
            if (kl.isEmpty()) continue;
            int i = lower.indexOf(kl);
            while (i >= 0) {
                for (int j = i; j < i + kl.length(); j++) bold[j] = true;
                i = lower.indexOf(kl, i + kl.length());
            }
        }
        StringBuilder sb = new StringBuilder();
        int start = 0;
        while (start < text.length()) {
            int end = start;
            while (end < text.length() && bold[end] == bold[start]) end++;
            String part = escape(text.substring(start, end));
            sb.append(bold[start] ? "<b>" + part + "</b>" : part);
            start = end;
        }
        return sb.toString();
    }

    public static JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ThemeConstants.MAROON_DARK);
        btn.setForeground(ThemeConstants.WHITE);
        btn.setFont(ThemeConstants.FONT_MAIN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return btn;
    }

    public static JPanel header(String title, JButton backButton) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeConstants.MAROON);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(ThemeConstants.WHITE);
        label.setFont(ThemeConstants.FONT_HEADER);
        header.add(label, BorderLayout.CENTER);
        if (backButton != null) header.add(backButton, BorderLayout.WEST);
        return header;
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Whispr", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Whispr", JOptionPane.INFORMATION_MESSAGE);
    }
}
