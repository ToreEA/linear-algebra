// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Implements an iterator over matrix element positions
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class PositionIterator implements Iterator<Position> {
    private final Size size;
    private final Function<Position, Position> advancePosition;
    private final Position lastPos;
    private Position currentPos;

    public static PositionIterator rowMajor(Size size) {
        return new PositionIterator(size, Position.of(1,1), Position.of(size.rows(), size.cols()), p -> {
            int nextRow = p.row();
            int nextCol = p.col() + 1;
            if (nextCol > size.cols()) {
                nextRow++;
                nextCol = 1;
            }
            return Position.of(nextRow, nextCol);
        });
    }

    public static PositionIterator columnMajor(Size size) {
        return new PositionIterator(size, Position.of(1,1), Position.of(size.rows(), size.cols()), p -> {
            int nextCol = p.col();
            int nextRow = p.row() + 1;
            if (nextRow > size.rows()) {
                nextCol++;
                nextRow = 1;
            }
            return Position.of(nextRow, nextCol);
        });
    }

    public static PositionIterator rowVector(Size size, int row) {
        return new PositionIterator(size, Position.of(row,1), Position.of(row,size.cols()), p -> {
            int nextRow = p.row();
            int nextCol = p.col() + 1;
            return Position.of(nextRow, nextCol);
        });
    }

    public static PositionIterator colVector(Size size, int col) {
        return new PositionIterator(size, Position.of(1,col), Position.of(size.rows(),col), p -> {
            int nextCol = p.col();
            int nextRow = p.row() + 1;
            return Position.of(nextRow, nextCol);
        });
    }

    public static PositionIterator diagonal(Size size) {
        return new PositionIterator(size, Position.of(1,1), Position.of(size.rows(),size.cols()), p -> {
            int nextCol = p.col() + 1;
            int nextRow = p.row() + 1;
            return Position.of(nextRow, nextCol);
        });

    }

    public static PositionIterator lowerTriangle(Size size) {
        return new PositionIterator(size, Position.of(2,1), Position.of(size.rows(),size.cols()-1), p -> {
            int nextRow = p.row();
            int nextCol = p.col() + 1;
            if (nextCol == p.row()) {
                nextCol = 1;
                nextRow++;
            }
            return Position.of(nextRow, nextCol);
        });
    }

/*
    public static PositionIterator upperTriangle(Size size) {

    }

*/
    private PositionIterator(Size size, Position first, Position last, Function<Position, Position> advancePosition) {
        this.size = size;
        this.advancePosition = advancePosition;
        this.currentPos = first;
        this.lastPos = last;
    }

    @Override
    public boolean hasNext() {
        return currentPos.row() <= lastPos.row() && currentPos.col() <= lastPos.col();
    }

    @Override
    public Position next() {
        Position pos = currentPos;
        currentPos = advancePosition.apply(currentPos);
        return pos;
    }
}
