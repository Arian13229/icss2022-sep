package nl.han.ica.datastructures.stack;

import nl.han.ica.datastructures.linkedList.IHANLinkedList;
import nl.han.ica.datastructures.linkedList.HANLinkedList;

public class HANStack<T> implements IHANStack<T> {
    private final IHANLinkedList<T> list;

    public HANStack() {
        this.list = new HANLinkedList<>();
    }

    @Override
    public void push(T value) {
        list.addFirst(value);
    }

    @Override
    public T pop() {
        if (list.getSize() == 0) {
            throw new IllegalStateException("Stack is empty");
        }
        T value = list.getFirst();
        list.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        if (list.getSize() == 0) {
            throw new IllegalStateException("Stack is empty");
        }
        return list.getFirst();
    }

    public boolean isEmpty() {
        return list.getSize() == 0;
    }

    public int getSize() {
        return list.getSize();
    }
}
