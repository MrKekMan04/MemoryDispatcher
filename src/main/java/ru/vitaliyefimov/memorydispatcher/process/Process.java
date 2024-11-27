package ru.vitaliyefimov.memorydispatcher.process;

import ru.vitaliyefimov.memorydispatcher.manager.MemoryManager;
import ru.vitaliyefimov.memorydispatcher.page.Page;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Process extends Thread {

    private static final Random RANDOM = new Random();

    private final int processId;
    private final List<Page> allocatedPages = new LinkedList<>();
    private final MemoryManager memoryManager;

    public Process(int processId, MemoryManager memoryManager) {
        this.processId = processId;
        this.memoryManager = memoryManager;
    }

    @Override
    public void run() {
        while (true) {
            if (RANDOM.nextInt() % 2 == 1) {
                requestPage();
            } else {
                if (RANDOM.nextInt() % 10 == 5) {
                    memoryManager.removeProcess(this);
                    break;
                } else {
                    if (!allocatedPages.isEmpty()) {
                        releasePage(allocatedPages.getFirst());
                    }
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void requestPage() {
        memoryManager.allocatePage(this);
    }

    private void releasePage(Page page) {
        memoryManager.freePage(page);
        allocatedPages.remove(page);
    }

    public int getProcessId() {
        return processId;
    }

    public void addAllocatedPage(Page page) {
        allocatedPages.add(page);
    }
}
