# Armstrong Number

This JavaFX application searches for Armstrong numbers from 1 through an inclusive upper limit. An Armstrong number equals the sum of its decimal digits raised to the number of digits. For example, 153 has three digits and 1³ + 5³ + 3³ = 153.

The default search covers 1 through 10,000 with four worker threads. Each worker checks a separate, contiguous range. Its progress indicator shows the fraction of that range checked. The upper limit accepts positive 32-bit integers, and the thread count ranges from 1 to the smaller of the upper limit and 64.

Completed searches display the matches in ascending order, followed by the Armstrong, non-Armstrong, and total counts. Stopping a search cancels the workers and clears partial results.

| Upper limit | Threads | Matches | Armstrong | Non-Armstrong | Total checked |
| ---: | ---: | --- | ---: | ---: | ---: |
| 100 | 4 | 1, 2, 3, 4, 5, 6, 7, 8, 9 | 9 | 91 | 100 |
