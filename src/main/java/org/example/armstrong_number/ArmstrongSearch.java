package org.example.armstrong_number;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class ArmstrongSearch {
    private static final long[][] DIGIT_POWERS = digitPowers();

    private ArmstrongSearch() {
    }

    record Range(int start, int end) {
        int size() {
            return end - start + 1;
        }
    }

    static List<Range> splitRanges(int num, int cnt) {
        if (num < 1) {
            throw new IllegalArgumentException("Upper limit must be positive.");
        }
        if (cnt < 1 || cnt > Math.min(num, 64)) {
            throw new IllegalArgumentException("Thread count must be between 1 and " + Math.min(num, 64) + ".");
        }

        int totalWeight = cnt * (cnt + 1) / 2;
        // At the threshold for distinct sizes, the weights already guarantee a number per worker.
        int reserved = num >= totalWeight && num < totalWeight + cnt ? 0 : 1;
        int available = num - reserved * cnt;
        int[] sizes = new int[cnt];
        long[] remainders = new long[cnt];
        Integer[] priority = new Integer[cnt];
        int leftover = available;
        for (int i = 0; i < cnt; i++) {
            long share = (long) available * (i + 1);
            int whole = (int) (share / totalWeight);
            sizes[i] = reserved + whole;
            leftover -= whole;
            remainders[i] = share % totalWeight;
            priority[i] = i;
        }
        Arrays.sort(priority, Comparator.<Integer>comparingLong(i -> remainders[i]).reversed()
                .thenComparing(Comparator.reverseOrder()));
        for (int i = 0; i < leftover; i++) {
            sizes[priority[i]]++;
        }
        if (cnt > 1 && sizes[0] > 1 && sizes[0] == sizes[cnt - 1]) {
            sizes[0]--;
            sizes[cnt - 1]++;
        }

        List<Range> ranges = new ArrayList<>(cnt);
        int end = 0;
        for (int size : sizes) {
            int start = end + 1;
            end += size;
            ranges.add(new Range(start, end));
        }
        return ranges;
    }

    static boolean isArmstrong(int number) {
        if (number < 1) {
            return false;
        }
        int length = 1;
        for (int digits = number; digits >= 10; digits /= 10) {
            length++;
        }
        long[] powers = DIGIT_POWERS[length];
        long sum = 0;
        for (int digits = number; digits > 0; digits /= 10) {
            sum += powers[digits % 10];
            if (sum > number) {
                return false;
            }
        }
        return sum == number;
    }

    private static long[][] digitPowers() {
        long[][] powers = new long[11][10];
        Arrays.fill(powers[0], 1L);
        for (int length = 1; length < powers.length; length++) {
            for (int digit = 0; digit < 10; digit++) {
                powers[length][digit] = powers[length - 1][digit] * digit;
            }
        }
        return powers;
    }
}
