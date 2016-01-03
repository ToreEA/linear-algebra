// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra;

/**
 * Represents the position of a matrix element
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class Position {
    private final int row, col;
    private Size size;

    public static Position of(Size size, int row, int col) {
        return new Position(size, row, col);
    }

    private Position(Size size, int row, int col) {
        this.size = size;
        this.row = row;
        this.col = col;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public boolean isLastRow() {
        return row == size.rows()-1;
    }

    public boolean isLastColumn() {
        return col == size.cols()-1;
    }

    public boolean isOnDiagonal() {
        return row == col;
    }

    public boolean isAboveDiagonal() {
        return col > row;
    }

    public boolean isBelowDiagonal() {
        return col < row;
    }

    @Override
    public String toString() {
        return "row " + row + ", col " + col;
    }
}
