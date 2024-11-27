package ru.vitaliyefimov.memorydispatcher;

import ru.vitaliyefimov.memorydispatcher.manager.MemoryManager;
import ru.vitaliyefimov.memorydispatcher.process.Process;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Main {

    private static final int PROCESS_COUNT = 4;

    public static void main(String[] args) {
        MemoryManager memoryManager = new MemoryManager(3);

        try (ExecutorService threadPool = Executors.newFixedThreadPool(PROCESS_COUNT)) {
            CompletableFuture.allOf(IntStream.range(0, PROCESS_COUNT)
                            .mapToObj(i -> new Process(i, memoryManager))
                            .map(process -> CompletableFuture.runAsync(process, threadPool))
                            .toArray(CompletableFuture[]::new))
                    .join();
        }
    }
}
