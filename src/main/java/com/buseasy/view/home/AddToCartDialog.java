package com.buseasy.view.home;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.NumberFormat;
import java.util.Locale;

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
import com.buseasy.util.PriceCalculator;
import com.buseasy.view.UiTheme;

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
    private final JCheckBox militaryBox    = new JCheckBox("Military discount (20% off)");
    private final JLabel    totalLabel     = new JLabel("0 VND");
    private final JLabel    errorLabel     = new JLabel(" ");

    private AddToCartDialog(Window owner, BusSchedule schedule, HomeController controller) {
        super(owner, "Add to Cart", ModalityType.APPLICATION_MODAL);
        this.schedule       = schedule;
        this.homeController = controller;

        setLayout(new GridBagLayout());
        getContentPane().setBackground(UiTheme.PAPER);
        setMinimumSize(new Dimension(440, 380));
        buildUI();
        updateTotal();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout());
        UiTheme.styleSurface(card);
        card.setBorder(UiTheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets    = new Insets(8, 10, 8, 10);
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx     = 0;
        gbc.gridy     = 0;
        gbc.weightx   = 1;

        // Header info
        JLabel eyebrow = UiTheme.createEyebrow("ADD TRIP TO CART");
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
        JLabel departureLabel = new JLabel("Departs: " + DateUtil.formatDateTime(schedule.getDepartureTime()), SwingConstants.CENTER);
        departureLabel.setFont(UiTheme.BODY);
        departureLabel.setForeground(UiTheme.TEXT_SECONDARY);
        card.add(departureLabel, gbc);

        gbc.gridy++;
        card.add(new JSeparator(), gbc);

        // Adult qty
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx   = 0;
        card.add(createFieldLabel("Adult passengers"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        card.add(adultSpinner, gbc);

        // Child qty
        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0;
        card.add(createFieldLabel("Child passengers (50% off)"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        card.add(childSpinner, gbc);

        // Military checkbox
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        UiTheme.styleCheckBox(militaryBox);
        card.add(militaryBox, gbc);

        // Total
        gbc.gridy++;
        JLabel totalTextLabel = new JLabel("Estimated total", SwingConstants.CENTER);
        totalTextLabel.setFont(UiTheme.META);
        totalTextLabel.setForeground(UiTheme.TEXT_MUTED);
        card.add(totalTextLabel, gbc);
        gbc.gridy++;
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setFont(UiTheme.SECTION_TITLE);
        totalLabel.setForeground(UiTheme.TEXT);
        card.add(totalLabel, gbc);

        // Error label
        errorLabel.setForeground(UiTheme.ERROR);
        errorLabel.setFont(UiTheme.BODY);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        card.add(errorLabel, gbc);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton cancelButton = new JButton("Cancel");
        JButton addButton    = new JButton("Add to Cart");
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
        militaryBox.addActionListener(e -> updateTotal());

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
            errorLabel.setText("Select at least one passenger.");
            return;
        }
        String error = homeController.addToCart(schedule.getId(), adult, child, mil);
        if (error == null) {
            dispose();
            return;
        }
        errorLabel.setText(error);
    }

    /**
     * Opens the dialog. Blocks until the user closes it.
     */
    public static void show(Window owner, BusSchedule schedule, HomeController controller) {
        new AddToCartDialog(owner, schedule, controller).setVisible(true);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(UiTheme.META);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }
}
