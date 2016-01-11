package no.kantega.bigdata.linearalgebra.buffer;

/**
 * Implements a vector buffer of fixed size
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class FixedVectorBuffer implements VectorBuffer {
    private final int size;
    private final double[] values;
    private final int base;
    private final int stride;

    public static VectorBuffer allocate(int size) {
        return new FixedVectorBuffer(size);
    }

    public static VectorBuffer from(int size, double[] values, int base, int stride) {
        return new FixedVectorBuffer(size, values, base, stride);
    }

    private FixedVectorBuffer(int size) {
        this.size = size;
        this.values = new double[size];
        this.base = 0;
        this.stride = 1;
    }

    private FixedVectorBuffer(int size, double[] values, int base, int stride) {
        this.size = size;
        this.values = values;
        this.base = base;
        this.stride = stride;
    }

    @Override
    public double get(int index) {
        return values[addressOf(index)];
    }

    @Override
    public void set(int index, double value) {
        values[addressOf(index)] = value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public VectorBuffer copy() {
        double[] valuesCopy = new double[size];
        for (int i = 0; i < size; i++) {
            valuesCopy[i] = get(i);
        }
        return new FixedVectorBuffer(size, valuesCopy, 0, 1);
    }

    private int addressOf(int index) {
        return base + (stride * index);
    }
}
