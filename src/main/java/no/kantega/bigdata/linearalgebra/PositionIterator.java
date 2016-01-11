package no.kantega.bigdata.linearalgebra;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Implements an iterator over matrix element positions
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class PositionIterator implements Iterator<Position> {

    private final Function<Position, Position> advancePosition;
    private final Position lastPos;
    private Position currentPos;

    public static PositionIterator rowMajor(Size size) {
        return new PositionIterator(Position.of(size, 0,0), Position.of(size, size.rows()-1, size.cols()-1), p -> {
            if (p.isLastColumn()) {
                p.row++;
                p.col = 0;
            } else {
                p.col++;
            }

            return p;
        });
    }

    public static PositionIterator columnMajor(Size size) {
        return new PositionIterator(Position.of(size, 0,0), Position.of(size, size.rows()-1, size.cols()-1), p -> {
            if (p.isLastRow()) {
                p.col++;
                p.row = 0;
            } else {
                p.row++;
            }

            return p;
        });
    }

    public static PositionIterator rowVector(Size size, int row) {
        return new PositionIterator(Position.of(size, row, 0), Position.of(size, row, size.cols()-1), p -> {
            p.col++;
            return p;
        });
    }

    public static PositionIterator colVector(Size size, int col) {
        return new PositionIterator(Position.of(size, 0,col), Position.of(size, size.rows()-1,col), p -> {
            p.row++;
            return p;
        });
    }

    public static PositionIterator diagonal(Size size) {
        return new PositionIterator(Position.of(size, 0,0), Position.of(size, size.rows()-1,size.cols()-1), p -> {
            p.row++;
            p.col++;
            return p;
        });
    }

    public static PositionIterator lowerTriangle(Size size) {
        return new PositionIterator(Position.of(size, 1,0), Position.of(size, size.rows()-1,size.cols()-2), p -> {
            p.col++;
            if (p.col == p.row) {
                p.col = 0;
                p.row++;
            }
            return p;
        });
    }

    public static PositionIterator upperTriangle(Size size) {
        return new PositionIterator(Position.of(size, 0,1), Position.of(size, size.rows()-2,size.cols()-1), p -> {
            p.row++;
            if (p.col == p.row) {
                p.row = 0;
                p.col++;
            }
            return p;
        });
    }

    private PositionIterator(Position first, Position last, Function<Position, Position> advancePosition) {
        this.advancePosition = advancePosition;
        this.currentPos = first;
        this.lastPos = last;
    }

    @Override
    public boolean hasNext() {
        return currentPos.row <= lastPos.row && currentPos.col <= lastPos.col;
    }

    @Override
    public Position next() {
        Position pos = new Position(currentPos);
        currentPos = advancePosition.apply(currentPos);
        return pos;
    }
}
