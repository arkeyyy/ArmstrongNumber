package org.example.armstrong_number;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmstrongSearchTest {
    @Test
    void matchesThrough100AreExactlyTheSingleDigitNumbers() {
        List<Integer> matches = IntStream.rangeClosed(1, 100)
                .filter(ArmstrongSearch::isArmstrong)
                .boxed()
                .toList();

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), matches);
        assertEquals(91, 100 - matches.size());
    }

    @Test
    void powersUseTheCandidatesDecimalDigitCount() {
        assertTrue(ArmstrongSearch.isArmstrong(153));
        assertFalse(ArmstrongSearch.isArmstrong(154));
        assertTrue(ArmstrongSearch.isArmstrong(1634));
        assertTrue(ArmstrongSearch.isArmstrong(912985153));
        assertFalse(ArmstrongSearch.isArmstrong(Integer.MAX_VALUE));
    }

    @Test
    void sampleRangesMatchTheFourWorkerExample() {
        assertEquals(List.of(
                new ArmstrongSearch.Range(1, 11),
                new ArmstrongSearch.Range(12, 31),
                new ArmstrongSearch.Range(32, 61),
                new ArmstrongSearch.Range(62, 100)),
                ArmstrongSearch.splitRanges(100, 4));
    }

    @Test
    void eachNumberGetsItsOwnRangeWhenWorkersEqualTheUpperLimit() {
        List<ArmstrongSearch.Range> ranges = ArmstrongSearch.splitRanges(64, 64);
        assertEquals(64, ranges.size());
        for (int i = 0; i < ranges.size(); i++) {
            assertEquals(new ArmstrongSearch.Range(i + 1, i + 1), ranges.get(i));
            assertEquals(1, ranges.get(i).size());
        }
    }

    @Test
    void rangesAreUnequalWhenOnlyOneExtraNumberIsAvailable() {
        List<ArmstrongSearch.Range> ranges = ArmstrongSearch.splitRanges(65, 64);
        assertPartition(65, 64, ranges);
        assertEquals(63, ranges.stream().filter(range -> range.size() == 1).count());
        assertEquals(1, ranges.stream().filter(range -> range.size() == 2).count());
    }

    @Test
    void maximumUpperLimitHasNoGapsOverlapsOrOverflow() {
        assertPartition(Integer.MAX_VALUE, 1, ArmstrongSearch.splitRanges(Integer.MAX_VALUE, 1));
        assertPartition(Integer.MAX_VALUE, 64, ArmstrongSearch.splitRanges(Integer.MAX_VALUE, 64));
    }

    private static void assertPartition(int limit, int workers, List<ArmstrongSearch.Range> ranges) {
        assertEquals(workers, ranges.size());
        long next = 1;
        for (ArmstrongSearch.Range range : ranges) {
            assertEquals(next, range.start());
            assertTrue(range.end() >= range.start());
            assertEquals((long) range.end() - range.start() + 1, range.size());
            next = (long) range.end() + 1;
        }
        assertEquals((long) limit + 1, next);
    }
}
