package com.buseasy.view.home;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.buseasy.controller.HomeController;
import com.buseasy.model.BusSchedule;
import com.buseasy.util.DateUtil;
import com.buseasy.util.LanguageManager;
import com.buseasy.util.PriceCalculator;
import com.buseasy.view.UiTheme;
import com.buseasy.view.common.MilitaryRequestDialog;

/**
 * Modal dialog where the user sets quantity, passenger type,
 * and military discount, then adds the schedule to the cart.
 */
public class AddToCartDialog extends JDialog {

    private static final NumberFormat CURRENCY =
        NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final BusSchedule   schedule;
    private final HomeController homeController;

    private final JSpinner adultSpinner    = new JSpinner(new SpinnerNumberModel(1, 0, 99, 1));
    private final JSpinner childSpinner    = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
    private final JCheckBox militaryBox    = new JCheckBox(LanguageManager.text("Military discount (20% off)"));
    private final JLabel    totalLabel     = new JLabel("0 VND");
    private final JLabel    errorLabel     = new JLabel(" ");

    private AddToCartDialog(Window owner, BusSchedule schedule, HomeController controller) {
        super(owner, LanguageManager.text("Add to Cart"), ModalityType.APPLICATION_MODAL);
        this.schedule       = schedule;
        this.homeController = controller;
        this.militaryBox.setSelected(controller.isMilitaryDiscountEligible());

        setLayout(new GridBagLayout());
        getContentPane().setBackground(UiTheme.PAPER);
        setMinimumSize(new Dimension(500, 500));
        buildUI();
        updateTotal();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout());
        UiTheme.styleSurface(card);
        card.setBorder(UiTheme.createCardBorder());
        UiTheme.styleSpinner(adultSpinner);
        UiTheme.styleSpinner(childSpinner);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets    = new Insets(8, 10, 8, 10);
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx     = 0;
        gbc.gridy     = 0;
        gbc.weightx   = 1;

        // Header info
        JLabel eyebrow = UiTheme.createEyebrow(LanguageManager.text("ADD TRIP TO CART"));
        eyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(eyebrow, gbc);

        gbc.gridy++;
        JLabel busLabel = new JLabel(
            schedule.getBus().getBusNumber() + "  —  " + schedule.getRoute());
        busLabel.setHorizontalAlignment(SwingConstants.CENTER);
        busLabel.setFont(UiTheme.SECTION_TITLE);
        busLabel.setForeground(UiTheme.TEXT);
        card.add(busLabel, gbc);

        gbc.gridy++;
        JLabel departureLabel = new JLabel(LanguageManager.text("Departs") + ": " + DateUtil.formatDateTime(schedule.getDepartureTime()), SwingConstants.CENTER);
        departureLabel.setFont(UiTheme.BODY);
        departureLabel.setForeground(UiTheme.TEXT_SECONDARY);
        card.add(departureLabel, gbc);

        gbc.gridy++;
        card.add(new JSeparator(), gbc);

        // Adult qty
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        card.add(createQuantityRow(
            LanguageManager.text("Adult passengers"),
            LanguageManager.text("Standard fare"),
            adultSpinner), gbc);

        // Child qty
        gbc.gridy++;
        card.add(createQuantityRow(
            LanguageManager.text("Child passengers"),
            LanguageManager.text("50% of adult fare"),
            childSpinner), gbc);

        // Military checkbox
        gbc.gridy++;
        gbc.gridwidth = 2;
        UiTheme.styleCheckBox(militaryBox);
        card.add(militaryBox, gbc);

        // Total
        gbc.gridy++;
        card.add(createTotalPanel(), gbc);

        // Error label
        errorLabel.setForeground(UiTheme.ERROR);
        errorLabel.setFont(UiTheme.BODY);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        card.add(errorLabel, gbc);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton cancelButton = new JButton(LanguageManager.text("Cancel"));
        JButton addButton    = new JButton(LanguageManager.text("Add to Cart"));
        UiTheme.styleSecondaryButton(cancelButton);
        UiTheme.stylePrimaryButton(addButton);
        buttons.add(cancelButton);
        buttons.add(addButton);
        gbc.gridy++;
        card.add(buttons, gbc);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.insets = new Insets(16, 16, 16, 16);
        add(card, outer);

        // Live price update
        adultSpinner.addChangeListener(e -> updateTotal());
        childSpinner.addChangeListener(e -> updateTotal());
        militaryBox.addActionListener(e -> onMilitaryToggled());

        cancelButton.addActionListener(e -> dispose());
        addButton.addActionListener(e -> onAddClicked());
    }

    private void updateTotal() {
        int adult      = (int) adultSpinner.getValue();
        int child      = (int) childSpinner.getValue();
        boolean milita = militaryBox.isSelected();
        double total   = PriceCalculator.calculate(schedule.getPriceAdult(), adult, child, milita);
        totalLabel.setText(CURRENCY.format((long) total) + " VND");
    }

    private void onAddClicked() {
        int adult   = (int) adultSpinner.getValue();
        int child   = (int) childSpinner.getValue();
        boolean mil = militaryBox.isSelected();

        if (adult + child == 0) {
            errorLabel.setText(LanguageManager.text("Select at least one passenger."));
            return;
        }
        String error = homeController.addToCart(schedule.getId(), adult, child, mil);
        if (error == null) {
            dispose();
            return;
        }
        errorLabel.setText(error);
    }

    private void onMilitaryToggled() {
        if (!militaryBox.isSelected()) {
            updateTotal();
            return;
        }
        if (homeController.isMilitaryDiscountEligible()) {
            updateTotal();
            return;
        }
        MilitaryRequestDialog.MilitaryRequestForm form = MilitaryRequestDialog.show(this);
        if (form == null) {
            militaryBox.setSelected(false);
            updateTotal();
            return;
        }
        String error = homeController.submitMilitaryRequest(form.serviceNumber(), form.unitName(), form.note());
        militaryBox.setSelected(false);
        updateTotal();
        errorLabel.setText(error == null ? LanguageManager.text("military.pending") : error);
    }

    /**
     * Opens the dialog. Blocks until the user closes it.
     */
    public static void show(Window owner, BusSchedule schedule, HomeController controller) {
        new AddToCartDialog(owner, schedule, controller).setVisible(true);
    }

    private JPanel createQuantityRow(String title, String detail, JSpinner spinner) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(true);
        row.setBackground(UiTheme.SUBTLE);
        row.setBorder(UiTheme.createRoundedBorder(UiTheme.BORDER, 12, 14));

        JPanel labelStack = new JPanel();
        labelStack.setOpaque(false);
        labelStack.setLayout(new BoxLayout(labelStack, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UiTheme.HEADING);
        titleLabel.setForeground(UiTheme.TEXT);

        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(UiTheme.CAPTION);
        detailLabel.setForeground(UiTheme.TEXT_SECONDARY);

        labelStack.add(titleLabel);
        labelStack.add(detailLabel);

        row.add(labelStack, BorderLayout.CENTER);
        row.add(spinner, BorderLayout.EAST);
        return row;
    }

    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(true);
        panel.setBackground(UiTheme.INK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel label = new JLabel(LanguageManager.text("Estimated total"));
        label.setFont(UiTheme.META);
        label.setForeground(UiTheme.HOVER);

        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setFont(UiTheme.SECTION_TITLE);
        totalLabel.setForeground(UiTheme.SURFACE);

        panel.add(label, BorderLayout.WEST);
        panel.add(totalLabel, BorderLayout.EAST);
        return panel;
    }
}
