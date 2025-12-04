/**
 * EDUCATIONAL MODULE 3: ONLINE ALGORITHMS WITH STREAMS
 * * CONCEPTS COVERED:
 * 1. Online Algorithm Simulation:
 * - An online algorithm processes its input piece-by-piece in a serial fashion.
 * - Here, we simulate 'Insertion Sort' treating the stream as the incoming data feed.
 * * 2. Stream.reduce() for State Accumulation:
 * - We use reduction to maintain a "Sorted State" (accumulator).
 * - For every new element from the stream, we insert it into the correct index 
 * of our accumulated list immediately.
 * * 3. Infinite Streams & Limiting:
 * - Generating random data on the fly using Stream.generate().
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class OnlineStreamSort {

    public static void main(String[] args) {
        
        // 1. Setup the Source: An infinite stream of random integers (0-99)
        Random rand = new Random();
        Stream<Integer> liveDataStream = Stream.generate(() -> rand.nextInt(100)).limit(10);

        System.out.println("--- Starting Online Insertion Sort ---");

        // 2. The Online Logic: Reduce
        // Identity: New empty ArrayList (initial state)
        // Accumulator: Takes existing sorted list + new item -> Returns updated sorted list
        // Combiner: Merges two lists (needed for parallel streams, though we use sequential here)
        
        List<Integer> sortedResult = liveDataStream.reduce(
            new ArrayList<>(), 
            (currentSortedList, newItem) -> {
                System.out.println("Incoming item: " + newItem + " | Current State: " + currentSortedList);
                insertSorted(currentSortedList, newItem);
                return currentSortedList;
            },
            (list1, list2) -> { 
                list1.addAll(list2); 
                Collections.sort(list1); 
                return list1; 
            }
        );

        System.out.println("--- Final Sorted Result ---");
        System.out.println(sortedResult);
    }

    // Helper method to simulate the "Insertion" part of Insertion Sort
    // This finds the correct spot for the new item and adds it.
    private static void insertSorted(List<Integer> list, Integer value) {
        int insertIndex = 0;
        while (insertIndex < list.size() && list.get(insertIndex) < value) {
            insertIndex++;
        }
        list.add(insertIndex, value);
    }
}