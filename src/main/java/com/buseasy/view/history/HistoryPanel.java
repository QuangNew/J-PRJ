package com.buseasy.view.history;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.buseasy.controller.HistoryController;
import com.buseasy.model.Ticket;
import com.buseasy.util.DateUtil;
import com.buseasy.view.UiTheme;

/**
 * Tab 2 — History.
 * Two sub-tabs: "Valid Tickets" (future departures) and "Expired Tickets" (past).
 */
public class HistoryPanel extends JPanel {

    private static final NumberFormat CURRENCY =
        NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private HistoryController historyController;

    private final JPanel validListPanel   = new JPanel();
    private final JPanel expiredListPanel = new JPanel();
    private final JLabel errorLabel       = new JLabel(" ", SwingConstants.CENTER);

    public HistoryPanel() {
        setLayout(new BorderLayout(0, 4));
        setOpaque(true);
        setBackground(UiTheme.PAPER);

        JLabel title = new JLabel("My Ticket History", SwingConstants.CENTER);
        title.setFont(UiTheme.SECTION_TITLE);
        title.setForeground(UiTheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 12, 0));

        validListPanel.setLayout(new BoxLayout(validListPanel, BoxLayout.Y_AXIS));
        expiredListPanel.setLayout(new BoxLayout(expiredListPanel, BoxLayout.Y_AXIS));
        validListPanel.setOpaque(true);
        validListPanel.setBackground(UiTheme.PAPER);
        expiredListPanel.setOpaque(true);
        expiredListPanel.setBackground(UiTheme.PAPER);

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab("Valid Tickets",   new JScrollPane(validListPanel));
        subTabs.addTab("Expired Tickets", new JScrollPane(expiredListPanel));
        UiTheme.styleTabs(subTabs);
        subTabs.setFont(UiTheme.HEADING);
        UiTheme.styleScrollPane((JScrollPane) subTabs.getComponentAt(0));
        UiTheme.styleScrollPane((JScrollPane) subTabs.getComponentAt(1));

        errorLabel.setForeground(UiTheme.ERROR);
        errorLabel.setFont(UiTheme.BODY);

        add(title,      BorderLayout.NORTH);
        add(subTabs,    BorderLayout.CENTER);
        add(errorLabel, BorderLayout.SOUTH);
    }

    public void setHistoryController(HistoryController controller) {
        this.historyController = controller;
    }

    /**
     * Populates both sub-tab lists with the provided ticket data.
     */
    public void renderTickets(List<Ticket> validTickets, List<Ticket> expiredTickets) {
        populateList(validListPanel,   validTickets,   "No upcoming tickets.");
        populateList(expiredListPanel, expiredTickets, "No past tickets.");
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void populateList(JPanel panel, List<Ticket> tickets, String emptyMessage) {
        panel.removeAll();
        if (tickets.isEmpty()) {
            panel.add(createEmptyState(emptyMessage));
        } else {
            for (Ticket ticket : tickets) {
                panel.add(buildTicketRow(ticket));
                panel.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private JPanel buildTicketRow(Ticket ticket) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        UiTheme.styleSurface(row);
        row.setBorder(UiTheme.createCardBorder());

        String passengers = ticket.getQtyAdult() + " Adult"
            + (ticket.getQtyChild() > 0 ? ", " + ticket.getQtyChild() + " Child" : "")
            + (ticket.isMilitary() ? "  [Military]" : "");

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);
        infoPanel.add(boldLabel("#T-" + String.format("%04d", ticket.getId())
            + "  |  " + ticket.getSchedule().getBus().getBusNumber()
            + "  —  " + ticket.getSchedule().getRoute()));
        infoPanel.add(createMetaLabel("Departs: " + DateUtil.formatDateTime(ticket.getSchedule().getDepartureTime())));
        infoPanel.add(createMetaLabel(passengers));

        JLabel priceLabel = new JLabel(
            CURRENCY.format((long) ticket.getTotalPrice()) + " VND",
            SwingConstants.RIGHT);
        priceLabel.setFont(UiTheme.HEADING);
        priceLabel.setForeground(UiTheme.TEXT);

        JButton detailButton = new JButton("Detail");
        UiTheme.styleSecondaryButton(detailButton);
        detailButton.addActionListener(e -> showTicketDetail(ticket));

        // Cancel button — only shown for valid tickets with departure > 48 h away
        boolean cancellable = "VALID".equals(ticket.getStatus())
            && ticket.getSchedule().getDepartureTime().isAfter(LocalDateTime.now().plusHours(48));

        JPanel rightPanel;
        if (cancellable) {
            JButton cancelButton = new JButton("Cancel");
            UiTheme.styleSecondaryButton(cancelButton);
            cancelButton.setForeground(UiTheme.ERROR);
            cancelButton.addActionListener(e -> {
                int choice = javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "Cancel ticket #T-" + String.format("%04d", ticket.getId()) + "?\nSeats will be returned.",
                    "Confirm Cancellation",
                    javax.swing.JOptionPane.YES_NO_OPTION);
                if (choice == javax.swing.JOptionPane.YES_OPTION && historyController != null) {
                    historyController.cancelTicket(ticket);
                }
            });
            rightPanel = new JPanel(new GridLayout(3, 1, 0, 4));
            rightPanel.setOpaque(false);
            rightPanel.add(priceLabel);
            rightPanel.add(detailButton);
            rightPanel.add(cancelButton);
        } else {
            rightPanel = new JPanel(new GridLayout(2, 1));
            rightPanel.setOpaque(false);
            rightPanel.add(priceLabel);
            rightPanel.add(detailButton);
        }

        row.add(infoPanel,  BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Shows full booking detail in a simple information dialog. */
    private void showTicketDetail(Ticket ticket) {
        String details = "<html><b>Ticket #T-" + String.format("%04d", ticket.getId()) + "</b><br>"
            + "Bus:       " + ticket.getSchedule().getBus().getBusNumber()
            + " — " + ticket.getSchedule().getBus().getBusName() + "<br>"
            + "Route:     " + ticket.getSchedule().getRoute() + "<br>"
            + "Departs:   " + DateUtil.formatDateTime(ticket.getSchedule().getDepartureTime()) + "<br>"
            + "Arrives:   " + DateUtil.formatDateTime(ticket.getSchedule().getArrivalTime()) + "<br>"
            + "Adults:    " + ticket.getQtyAdult() + "<br>"
            + "Children:  " + ticket.getQtyChild() + "<br>"
            + "Military:  " + (ticket.isMilitary() ? "Yes" : "No") + "<br>"
            + "Total:     " + CURRENCY.format((long) ticket.getTotalPrice()) + " VND<br>"
            + "Purchased: " + DateUtil.formatDateTime(ticket.getPurchasedAt()) + "<br>"
            + "Status:    " + ticket.getStatus()
            + "</html>";

        JOptionPane.showMessageDialog(this, details, "Ticket Detail", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.HEADING);
        lbl.setForeground(UiTheme.TEXT);
        return lbl;
    }

    private JLabel createMetaLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.BODY);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }

    private JPanel createEmptyState(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        UiTheme.styleSurface(panel);
        panel.setBorder(UiTheme.createCardBorder());

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(UiTheme.BODY);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        panel.add(label, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height + 40));
        return panel;
    }
}
