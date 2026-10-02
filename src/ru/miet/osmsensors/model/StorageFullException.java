package ru.miet.osmsensors.model;

/** В хранилище нет свободного места. */
public class StorageFullException extends Exception {
    private final int capacity;

    public StorageFullException(int capacity) {
        super("Хранилище заполнено (ёмкость " + capacity + ")");
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}
