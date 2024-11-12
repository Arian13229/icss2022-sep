package nl.han.ica.datastructures.linkedList;

public interface IHANLinkedList<T> extends Iterable<T> {
    void addFirst(T value);
    void clear();
    void insert(int index, T value);
    void delete(int pos);
    T get(int pos);
    void removeFirst();
    T getFirst();
    int getSize();
}