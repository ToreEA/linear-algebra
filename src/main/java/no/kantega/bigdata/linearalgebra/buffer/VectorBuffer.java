package no.kantega.bigdata.linearalgebra.buffer;

/**
 * Defines storage buffer for vector components
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public interface VectorBuffer {
    double get(int index);

    void set(int index, double value);

    int size();

    VectorBuffer copy();
}
