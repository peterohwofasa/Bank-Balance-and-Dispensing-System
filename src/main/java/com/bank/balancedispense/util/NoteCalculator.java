package com.bank.balancedispense.util;

import com.bank.balancedispense.common.ErrorMessage;
import com.bank.balancedispense.entities.ATMAllocation;
import com.bank.balancedispense.exceptions.NoteCalculationException;

import java.util.*;

/**
 * Utility for calculating ATM note combinations for withdrawals.
 */
public class NoteCalculator {

    /**
     * Calculates the optimal combination of notes to fulfill the requested amount.
     * Throws NoteCalculationException if exact match is not possible.
     */
    public static Map<Integer, Integer> calculate(double amount, List<ATMAllocation> notes) {
        int amt = (int) amount;
        Map<Integer, Integer> result = new TreeMap<>(Comparator.reverseOrder());

        // Sort denominations from largest to smallest by value
        List<ATMAllocation> sorted = notes.stream()
                .sorted(Comparator.comparingInt((ATMAllocation a) -> a.getDenomination().getValue().intValue()).reversed())
                .toList();

        for (ATMAllocation note : sorted) {
            int denom = note.getDenomination().getValue().intValue();
            int maxUsable = amt / denom;
            int useQty = Math.min(note.getQuantity(), maxUsable);

            if (useQty > 0) {
                result.put(denom, useQty);
                amt -= denom * useQty;
            }
        }

        if (amt > 0) {
            throw new NoteCalculationException(ErrorMessage.NOTE_CALCULATION_FAILED.get());
        }

        return result;
    }

    /**
     * Suggests the closest available amount that can be dispensed if the requested amount is not possible.
     */
    public static Optional<Integer> suggestFallbackAmount(double requested, List<ATMAllocation> notes) {
        List<Integer> availableAmounts = new ArrayList<>();
        for (int amount = (int) requested - 10; amount > 0; amount -= 10) {
            try {
                NoteCalculator.calculate(amount, notes);
                availableAmounts.add(amount);
            } catch (Exception ignored) {
            }
        }
        return availableAmounts.stream().max(Integer::compareTo);
    }
}