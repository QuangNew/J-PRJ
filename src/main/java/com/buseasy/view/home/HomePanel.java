package com.buseasy.view.home;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.buseasy.controller.HomeController;
import com.buseasy.view.UiTheme;

/**
 * Tab 1 — Home.
 * Contains a CalendarPanel and a TimelinePanel; switches between them
 * using a CardLayout so only one is visible at a time.
 */
public class HomePanel extends JPanel {

    private static final String CARD_CALENDAR = "CALENDAR";
    private static final String CARD_TIMELINE = "TIMELINE";

    private final CardLayout    cardLayout    = new CardLayout();
    private final JPanel        cardContainer = new JPanel(cardLayout);

    private final CalendarPanel calendarPanel = new CalendarPanel();
    private final TimelinePanel timelinePanel = new TimelinePanel();

    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);

    public HomePanel() {
        setLayout(new BorderLayout(0, 4));
        setOpaque(true);
        setBackground(UiTheme.PAPER);
        cardContainer.setOpaque(false);
        cardContainer.add(calendarPanel, CARD_CALENDAR);
        cardContainer.add(timelinePanel, CARD_TIMELINE);

        UiTheme.styleStatusLabel(statusLabel);
        add(cardContainer,  BorderLayout.CENTER);
        add(statusLabel,    BorderLayout.SOUTH);
    }

    public void setHomeController(HomeController controller) {
        calendarPanel.setHomeController(controller);
        timelinePanel.setHomeController(controller);
    }

    public CalendarPanel getCalendarPanel() { return calendarPanel; }
    public TimelinePanel getTimelinePanel() { return timelinePanel; }

    public void showCalendarPanel() { cardLayout.show(cardContainer, CARD_CALENDAR); }
    public void showTimelinePanel() { cardLayout.show(cardContainer, CARD_TIMELINE); }

    public void showError(String message) {
        statusLabel.setForeground(UiTheme.ERROR);
        statusLabel.setText(message);
    }

    public void showSuccess(String message) {
        statusLabel.setForeground(UiTheme.SUCCESS);
        statusLabel.setText(message);
    }

    public void showInfo(String message) {
        statusLabel.setForeground(UiTheme.TEXT_SECONDARY);
        statusLabel.setText(message);
    }

    public void clearStatus() {
        statusLabel.setForeground(UiTheme.TEXT_SECONDARY);
        statusLabel.setText(" ");
    }
}
