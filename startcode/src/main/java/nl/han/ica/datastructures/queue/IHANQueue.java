package nl.han.ica.datastructures.queue;

public interface IHANQueue<T> {
    void clear();
    boolean isEmpty();
    void enqueue(T value);
    T dequeue();
    T peek();
    int getSize();
}

