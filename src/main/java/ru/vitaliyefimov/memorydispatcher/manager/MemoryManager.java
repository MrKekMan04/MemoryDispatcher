package ru.vitaliyefimov.memorydispatcher.manager;

import ru.vitaliyefimov.memorydispatcher.page.Page;
import ru.vitaliyefimov.memorydispatcher.process.Process;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class MemoryManager {

    private static final Logger LOGGER = Logger.getLogger(MemoryManager.class.getName());

    private final List<Page> pages = new LinkedList<>();
    private final Queue<Page> swap = new LinkedList<>();
    private final Lock lock = new ReentrantLock();

    public MemoryManager(int numPages) {
        IntStream.range(0, numPages)
                .mapToObj(Page::new)
                .forEach(pages::add);
    }

    public void allocatePage(Process process) {
        lock.lock();
        try {
            if (pages.isEmpty()) {
                returnFromSwap();
            }
            pages.stream()
                    .filter(Page::isFree)
                    .findFirst()
                    .or(() -> {
                        swapPages();
                        return Optional.empty();
                    })
                    .ifPresent(page -> {
                        page.allocateTo(process);
                        LOGGER.info("Allocated page %d to process %d".formatted(page.getId(), process.getProcessId()));
                    });
        } finally {
            lock.unlock();
        }
    }

    public void freePage(Page page) {
        lock.lock();
        try {
            Process allocatedProcess = page.getAllocatedTo();
            if (allocatedProcess == null) {
                return;
            }
            page.allocateTo(null);
            LOGGER.info("Freed page %d from process %d".formatted(page.getId(), allocatedProcess.getProcessId()));
        } finally {
            lock.unlock();
        }
    }

    public void removeProcess(Process process) {
        lock.lock();
        try {
            pages.stream()
                    .filter(page -> page.getAllocatedTo() == process)
                    .forEach(page -> page.allocateTo(null));
        } finally {
            lock.unlock();
        }
    }

    private void swapPages() {
        if (!pages.isEmpty()) {
            Page pageToSwap = pages.getFirst();
            pages.removeFirst();
            pageToSwap.allocateTo(null);
            swap.offer(pageToSwap);
            LOGGER.info("Swapped page %d to swap space".formatted(pageToSwap.getId()));
        }
    }

    public void returnFromSwap() {
        if (!swap.isEmpty()) {
            Page page = swap.poll();
            pages.add(page);
            LOGGER.info("Returned page %d from swap space".formatted(page.getId()));
        }
    }
}
