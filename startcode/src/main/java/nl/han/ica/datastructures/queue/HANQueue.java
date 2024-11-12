package nl.han.ica.datastructures.queue;

import nl.han.ica.datastructures.linkedList.IHANLinkedList;
import nl.han.ica.datastructures.linkedList.HANLinkedList;

public class HANQueue<T> implements IHANQueue<T> {
    private final IHANLinkedList<T> list;

    public HANQueue() {
        this.list = new HANLinkedList<>();
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean isEmpty() {
        return list.getSize() == 0;
    }

    @Override
    public void enqueue(T value) {
        list.insert(list.getSize(), value);
    }

    @Override
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        T value = list.getFirst();
        list.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return list.getFirst();
    }

    @Override
    public int getSize() {
        return list.getSize();
    }
}
