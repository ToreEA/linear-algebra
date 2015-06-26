// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra.buffer;

import no.kantega.bigdata.linearalgebra.Size;

/**
 * Implements a matrix buffer of fixed size
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class FixedMatrixBuffer implements MatrixBuffer {
    private Size size;
    private final double[] values;
    private final int base;
    private int rowStride;
    private int colStride;

    public static MatrixBuffer allocate(int rows, int cols) {
        return new FixedMatrixBuffer(rows, cols);
    }

    private FixedMatrixBuffer(int rows, int cols) {
        this.size = Size.of(rows, cols);
        this.values = new double[rows*cols];
        this.base = 0;
        this.rowStride = cols;
        this.colStride = 1;
    }

    private FixedMatrixBuffer(Size size, double[] values, int base, int rowStride, int colStride) {
        this.size = size;
        this.values = values;
        this.base = base;
        this.rowStride = rowStride;
        this.colStride = colStride;
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
        return FixedVectorBuffer.from(size.cols(), values, (row - 1) * rowStride, colStride);
    }

    @Override
    public VectorBuffer column(int col) {
        return FixedVectorBuffer.from(size.rows(), values, (col - 1) * colStride, rowStride);
    }

    @Override
    public Size size() {
        return size;
    }

    @Override
    public MatrixBuffer copy() {
        return new FixedMatrixBuffer(size, values.clone(), base, rowStride, colStride);
    }

    @Override
    public void transpose() {
        int tmp = rowStride;
        rowStride = colStride;
        colStride = tmp;
        size = Size.of(size.cols(), size.rows());
    }

    private int addressOf(int row, int col) {
        return base + (rowStride * (row - 1) + (colStride * (col - 1)));
    }
}
