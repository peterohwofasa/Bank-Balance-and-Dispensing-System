package com.bank.balancedispense.util;

import com.bank.balancedispense.common.ErrorMessage;
import com.bank.balancedispense.entities.ATMNote;
import com.bank.balancedispense.exceptions.NoteCalculationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the NoteCalculator utility class.
 * These tests validate the correctness and robustness of the note calculation logic.
 */
public class NoteCalculatorTest {


    /**
     * Test that exact amount is calculated using available notes in optimal denominations.
     * Should return 1 x 200 and 1 x 100 for amount 300.
     */
    @Test
    public void testCalculateNotesExactAmount() {
        List<ATMNote> notes = List.of(
                createNote(200, 5),
                createNote(100, 5),
                createNote(50, 5)
        );
        Map<Integer, Integer> result = NoteCalculator.calculate(300, notes);

        assertEquals(2, result.size());
        assertEquals(1, result.get(200));
        assertEquals(1, result.get(100));
    }

    /**
     * Test that an exception is thrown if required notes are not available.
     * 100 is requested but quantity is zero.
     */
    @Test
    public void testCalculateInsufficientNotes_shouldThrowNoteCalculationException() {
        List<ATMNote> notes = List.of(createNote(100, 0));

        NoteCalculationException ex = assertThrows(NoteCalculationException.class, () ->
                NoteCalculator.calculate(100, notes));
        assertEquals(ErrorMessage.NOTE_CALCULATION_FAILED.get(), ex.getMessage());
    }

    /**
     * Test that an exception is thrown when no notes are available at all.
     * Should trigger the calculation failure logic.
     */
    @Test
    public void testNoATMNotesAvailable_shouldThrowNoteCalculationException() {
        List<ATMNote> notes = List.of();
        assertThrows(NoteCalculationException.class, () -> NoteCalculator.calculate(100, notes));
    }

    /**
     * Test fallback suggestion when the requested amount cannot be dispensed.
     * Should return the highest lower dispensable value (100 + 50 = 150).
     */
    @Test
    public void testSuggestFallbackAmount_returnsClosestLowerDispensable() {
        List<ATMNote> notes = List.of(
                createNote(100, 1),
                createNote(50, 1)
        );

        Optional<Integer> fallback = NoteCalculator.suggestFallbackAmount(300, notes);
        assertTrue(fallback.isPresent());
        assertEquals(150, fallback.get()); // Only 100 + 50 is possible
    }

    /**
     * Test fallback suggestion when no notes are available at all.
     * Should return empty optional since no fallback is possible.
     */
    @Test
    public void testSuggestFallbackAmount_noneAvailable_returnsEmpty() {
        List<ATMNote> notes = List.of(createNote(50, 0));
        Optional<Integer> fallback = NoteCalculator.suggestFallbackAmount(100, notes);
        assertTrue(fallback.isEmpty());
    }

    /**
     * Utility method to construct ATMNote test data.
     */
    private ATMNote createNote(int denomination, int quantity) {
        ATMNote note = new ATMNote();
        note.setDenomination(denomination);
        note.setQuantity(quantity);
        return note;
    }
}
