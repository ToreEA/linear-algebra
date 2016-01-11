package no.kantega.bigdata.linearalgebra.buffer;

import no.kantega.bigdata.linearalgebra.Size;

/**
 * Implements a matrix buffer of fixed size storing elements column wise
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class FixedColumnMajorMatrixBuffer implements MatrixBuffer {
    private Size size;
    private final double[] values;
    private int stride;

    public static MatrixBuffer allocate(int rows, int cols) {
        return new FixedColumnMajorMatrixBuffer(rows, cols);
    }

    private FixedColumnMajorMatrixBuffer(int rows, int cols) {
        this.size = Size.of(rows, cols);
        this.values = new double[rows*cols];
        this.stride = rows;
    }

    FixedColumnMajorMatrixBuffer(Size size, double[] values, int stride) {
        this.size = size;
        this.values = values;
        this.stride = stride;
    }

    @Override
    public double get(int row, int col) {
        return values[addressOf(row, col)];
    }

    @Override
    public void set(int row, int col, double value) {
        values[addressOf(row, col)] = value;
    }

    @Override
    public VectorBuffer row(int row) {
        return FixedVectorBuffer.from(size.cols(), values, row, stride);
    }

    @Override
    public VectorBuffer column(int col) {
        return FixedVectorBuffer.from(size.rows(), values, col * stride, 1);
    }

    @Override
    public Size size() {
        return size;
    }

    @Override
    public MatrixBuffer copy() {
        return new FixedColumnMajorMatrixBuffer(size, values.clone(), stride);
    }

    @Override
    public MatrixBuffer transpose() {
        return new FixedRowMajorMatrixBuffer(Size.of(size.cols(), size.rows()), values, size.rows());
    }

    private int addressOf(int row, int col) {
        return stride * col + row;
    }
}
