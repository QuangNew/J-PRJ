package com.buseasy.util;

/**
 * All ticket pricing logic lives here — one class, one place.
 * Never duplicate these formulas elsewhere in the codebase.
 *
 * Rules:
 *   - Child ticket  = adult price × CHILD_RATE  (50%)
 *   - Military disc = MILITARY_DISCOUNT (20%) off the total
 */
public class PriceCalculator {

    public static final double CHILD_RATE        = 0.50;
    public static final double MILITARY_DISCOUNT = 0.20;

    private PriceCalculator() {}

    /**
     * Calculates the total price for one cart item / ticket.
     *
     * @param priceAdult  base adult seat price
     * @param qtyAdult    number of adult passengers
     * @param qtyChild    number of child passengers
     * @param isMilitary  whether the military discount applies
     * @return the final total price
     */
    public static double calculate(double priceAdult,
                                   int qtyAdult,
                                   int qtyChild,
                                   boolean isMilitary) {
        double adultTotal = qtyAdult * priceAdult;
        double childTotal = qtyChild * priceAdult * CHILD_RATE;
        double subtotal   = adultTotal + childTotal;

        if (isMilitary) {
            subtotal = subtotal * (1.0 - MILITARY_DISCOUNT);
        }
        return subtotal;
    }
}
