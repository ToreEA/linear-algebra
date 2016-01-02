package no.kantega.bigdata.linearalgebra.matcher;

import no.kantega.bigdata.linearalgebra.Vector;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class VectorIsCloseTo extends TypeSafeMatcher<Vector> {
    private final double delta;
    private final Vector value;

    private int errorIndex;
    private boolean differentSizes;

    public VectorIsCloseTo(Vector value, double error) {
        this.delta = error;
        this.value = value;
    }

    @Override
    public boolean matchesSafely(Vector item) {
        this.differentSizes = value.dimension() != item.dimension();
        if (differentSizes) {
            return false;
        } else {
            for (int i=1; i< value.dimension(); i++) {
                if (actualDelta(i, item) > 0.0d) {
                    errorIndex = i;
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public void describeMismatchSafely(Vector item, Description mismatchDescription) {
        if (differentSizes) {
            mismatchDescription.appendText(" first vector has dimension " + value.dimension() + ", the second has " + item.dimension());
        } else {
            mismatchDescription.appendValue(item.at(errorIndex)).appendText(" differed by ").appendValue(this.actualDelta(errorIndex, item));
        }
    }

    @Override
    public void describeTo(Description description) {
        if (differentSizes) {
            description.appendText("vector with same dimension, " + value.dimension());
        } else {
            description.appendText("an component value at " + errorIndex + " within ").appendValue(this.delta).appendText(" of ").appendValue(this.value.at(errorIndex));
        }
    }

    private double actualDelta(int index, Vector item) {
        double valueComponent = value.at(index);
        double itemComponent = item.at(index);
        return Math.abs(itemComponent - valueComponent) - this.delta;
    }

    @Factory
    public static Matcher<Vector> closeToVector(Vector operand, double error) {
        return new VectorIsCloseTo(operand, error);
    }
}