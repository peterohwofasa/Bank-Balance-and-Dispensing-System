package com.bank.balancedispense.util;

import com.bank.balancedispense.common.ErrorMessage;
import com.bank.balancedispense.entities.ATMNote;
import com.bank.balancedispense.exceptions.NoteCalculationException;

import java.util.*;

/**
 * Service interface for processing ATM withdrawal requests.
 */
public class NoteCalculator {

    /**
     * Calculates the optimal combination of notes to fulfill the requested amount.
     * Throws NoteCalculationException if exact match is not possible.
     */
    public static Map<Integer, Integer> calculate(double amount, List<ATMNote> notes) {
        int amt = (int) amount;
        Map<Integer, Integer> result = new TreeMap<>(Comparator.reverseOrder());

        for (ATMNote note : notes.stream().sorted(Comparator.comparingInt(ATMNote::getDenomination).reversed()).toList()) {
            int denom = note.getDenomination();
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
    public static Optional<Integer> suggestFallbackAmount(double requested, List<ATMNote> notes) {
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