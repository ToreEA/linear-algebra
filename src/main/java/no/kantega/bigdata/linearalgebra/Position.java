package no.kantega.bigdata.linearalgebra;

/**
 * Represents the row and column position of a matrix element
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class Position {
    int lastRow, lastCol;
    int row, col;

    public static Position of(Size size, int row, int col) {
        return new Position(size, row, col);
    }

    private Position(Size size, int row, int col) {
        this.lastRow = size.rows() - 1;
        this.lastCol = size.cols() - 1;
        this.row = row;
        this.col = col;
    }

    public Position(Position that) {
        this.lastRow = that.lastRow;
        this.lastCol = that.lastCol;
        this.row = that.row;
        this.col = that.col;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public boolean isLastRow() {
        return row == lastRow;
    }

    public boolean isLastColumn() {
        return col == lastCol;
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
