// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra;

import static no.kantega.bigdata.linearalgebra.utils.Assert.require;

/**
 * Represents the size of a matrix
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class Size {
    private final int rows, cols;

    public static Size of(int rows, int cols) {
        return new Size(rows, cols);
    }

    private Size(int rows, int cols) {
        require(() -> rows >= 1, "rows must be 1 or higher");
        require(() -> cols >= 1, "cols must be 1 or higher");
        this.rows = rows;
        this.cols = cols;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public int count() {
        return rows * cols;
    }

    @Override
    public String toString() {
        return rows + " x " + cols;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Size size = (Size) o;

        if (rows != size.rows) return false;
        return cols == size.cols;
    }

    @Override
    public int hashCode() {
        int result = rows;
        result = 31 * result + cols;
        return result;
    }
}
