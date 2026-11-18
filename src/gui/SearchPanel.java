package gui;
import main.AppState;
import main.ThemeConstants;
import main.WhisprApp;
import managers.SearchManager;
import managers.SearchManager.SearchResult;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;

/**
 * Keyword search across every message. Several keywords can be entered; a message must contain all of them.
 * Picking a result opens its chat scrolled to that message, and Back returns to these results.
 */
public class SearchPanel extends JPanel {
    private final WhisprApp app;
    private final JTextField searchField = new JTextField(18);
    private final DefaultListModel<SearchResult> resultsModel = new DefaultListModel<>();
    private final JList<SearchResult> resultsList = new JList<>(resultsModel);
    private final JLabel statusLabel = new JLabel("Type one or more keywords, then press Search.");
    private List<String> currentKeywords = new ArrayList<>();
    private String lastQuery = "";

    public SearchPanel(WhisprApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BACKGROUND);

        JButton backButton = GuiUtil.button("< Back");
        backButton.addActionListener(e -> app.showScreen("LANDING"));
        add(GuiUtil.header("Search", backButton), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(ThemeConstants.BACKGROUND);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        searchPanel.setBackground(ThemeConstants.BACKGROUND);
        JButton searchButton = GuiUtil.button("Search");
        searchButton.setBackground(ThemeConstants.MAROON);
        JButton clearButton = GuiUtil.button("Clear");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(ThemeConstants.BACKGROUND);
        top.add(searchPanel, BorderLayout.NORTH);
        statusLabel.setFont(ThemeConstants.FONT_MAIN);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 6, 12));
        top.add(statusLabel, BorderLayout.SOUTH);
        body.add(top, BorderLayout.NORTH);

        resultsList.setCellRenderer(new ResultCellRenderer(app, () -> currentKeywords));
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        body.add(new JScrollPane(resultsList), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        searchButton.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            lastQuery = "";
            currentKeywords = new ArrayList<>();
            resultsModel.clear();
            statusLabel.setText("Type one or more keywords, then press Search.");
        });
        resultsList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int i = resultsList.locationToIndex(e.getPoint());
                if (i >= 0 && resultsList.getCellBounds(i, i).contains(e.getPoint())) {
                    SearchResult r = resultsModel.get(i);
                    app.openChat(r.chat, r.message, "SEARCH");
                }
            }
        });
    }

    private void doSearch() {
        lastQuery = searchField.getText().trim();
        runSearch();
    }

    /** Repeats the last search so the list is up to date after chats or messages were deleted. */
    public void rerun() {
        if (!lastQuery.isEmpty()) runSearch();
    }

    private void runSearch() {
        currentKeywords = SearchManager.parseKeywords(lastQuery);
        resultsModel.clear();
        if (currentKeywords.isEmpty()) {
            statusLabel.setText("Please enter at least one keyword.");
            return;
        }
        AppState state = app.getAppState();
        List<SearchResult> results = app.getSearchManager().search(lastQuery, state.getChats(), state.getContacts(), state.getUser());
        Set<String> chatIds = new HashSet<>();
        for (SearchResult r : results) { resultsModel.addElement(r); chatIds.add(r.chat.getChatId()); }
        statusLabel.setText(results.isEmpty()
                ? "No messages found for \"" + lastQuery + "\"."
                : results.size() + " message(s) found in " + chatIds.size() + " chat(s). Click one to open it.");
    }
}
