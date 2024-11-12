package nl.han.ica.datastructures.stack;

public interface IHANStack<T> {
    void push(T value);
    T pop();
    T peek();
}
