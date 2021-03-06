package no.kantega.bigdata.linearalgebra.buffer;

import no.kantega.bigdata.linearalgebra.Size;

/**
 * Defines storage buffer for matrix elements
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public interface MatrixBuffer {
    /**
     * Gets the element value at given position
     * @param row the row number (zero-based)
     * @param col the column number (zero-based)
     * @return the element value
     */
    double get(int row, int col);

    /**
     * Sets the value of element given position
     * @param row the row number (zero-based)
     * @param col the column number (zero-based)
     * @param value the element value
     */
    void set(int row, int col, double value);

    /**
     * Gets the specified row vector
     * @param row the row number (zero-based)
     * @return the row vector
     */
    VectorBuffer row(int row);

    /**
     * Gets the specified column vector
     * @param col the column number (zero-based)
     * @return the column vector
     */
    VectorBuffer column(int col);

    /**
     * Gets the size of the buffer
     * @return the buffer size
     */
    Size size();

    /**
     * Creates a copy of this object
     * @return the copy
     */
    MatrixBuffer copy();

    /**
     * Returns a transposed version of this matrix buffer
     * @return the transposed matrix buffer
     */
    MatrixBuffer transpose();
}
