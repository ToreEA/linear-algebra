package no.kantega.bigdata.linearalgebra.matcher;

import no.kantega.bigdata.linearalgebra.Matrix;
import no.kantega.bigdata.linearalgebra.Position;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class MatrixIsCloseTo extends TypeSafeMatcher<Matrix> {
    private final double delta;
    private final Matrix value;

    private Position errorPos;
    private boolean differentSizes;

    public MatrixIsCloseTo(Matrix value, double error) {
        this.delta = error;
        this.value = value;
    }

    @Override
    public boolean matchesSafely(Matrix item) {
        this.differentSizes = !value.size().equals(item.size());
        if (differentSizes) {
            return false;
        } else {
            Iterable<Position> allPositions = value::rowMajorPositionIterator;
            for (Position pos : allPositions) {
                if (actualDelta(pos, item) > 0.0d) {
                    errorPos = pos;
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public void describeMismatchSafely(Matrix item, Description mismatchDescription) {
        if (differentSizes) {
            mismatchDescription.appendText(" first matrix has size " + value.size() + ", the second has " + item.size());
        } else {
            mismatchDescription.appendValue(item.at(errorPos)).appendText(" differed by ").appendValue(this.actualDelta(errorPos, item));
        }
    }

    @Override
    public void describeTo(Description description) {
        if (differentSizes) {
            description.appendText("matrix with same size, " + value.size());
        } else {
            description.appendText("an element value at " + errorPos + " within ").appendValue(this.delta).appendText(" of ").appendValue(this.value.at(errorPos));
        }
    }

    private double actualDelta(Position pos, Matrix item) {
        double valueElement = value.at(pos);
        double itemElement = item.at(pos);
        return Math.abs(itemElement - valueElement) - this.delta;
    }

    @Factory
    public static Matcher<Matrix> closeToMatrix(Matrix operand, double error) {
        return new MatrixIsCloseTo(operand, error);
    }
}