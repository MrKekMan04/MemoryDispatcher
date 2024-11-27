package ru.vitaliyefimov.memorydispatcher.page;

import ru.vitaliyefimov.memorydispatcher.process.Process;

public class Page {

    private final int id;
    private boolean isFree;
    private Process allocatedTo;

    public Page(int id) {
        this.id = id;
        this.isFree = true;
    }

    public void allocateTo(Process process) {
        allocatedTo = process;
        this.isFree = process == null;
        if (process != null) {
            process.addAllocatedPage(this);
        }
    }

    public Process getAllocatedTo() {
        return allocatedTo;
    }

    public int getId() {
        return id;
    }

    public boolean isFree() {
        return isFree;
    }
}
