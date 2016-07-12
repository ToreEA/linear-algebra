package no.kantega.bigdata.linearalgebra;

import no.kantega.bigdata.linearalgebra.buffer.FixedVectorBuffer;
import no.kantega.bigdata.linearalgebra.buffer.VectorBuffer;
import no.kantega.bigdata.linearalgebra.utils.NumberFormatter;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.kantega.bigdata.linearalgebra.matcher.VectorIsCloseTo.closeToVector;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.*;

/**
 * Unit test for the Vector class
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class VectorTest {

    private static final double EPSILON = 0.000000000001;

    @Test
    public void shouldCreateFromValues() {
        Vector v = Vector.of(5, 6, 7, 8);

        assertThat(v.at(1), is(5.0d));
        assertThat(v.at(2), is(6.0d));
        assertThat(v.at(3), is(7.0d));
        assertThat(v.at(4), is(8.0d));
    }

    @Test
    public void shouldCreateWithSpecifiedDimension() {
        Vector v = Vector.newInstance(3);
        v.setAt(1, 5.0d);
        v.setAt(2, 6.0d);
        v.setAt(3, 7.0d);

        assertThat(v.dimension(), is(3));
        assertThat(v.at(1), is(5.0d));
        assertThat(v.at(2), is(6.0d));
        assertThat(v.at(3), is(7.0d));
    }

    @Test
    public void shouldCreateZeroVector() {
        Vector zv = Vector.zero(3);

        assertThat(zv.anyMatch((i, v) -> v != 0.0), is(false));
    }

    @Test
    public void shouldCreatePrefilledWithSingleValue() {
        Vector cv = Vector.constant(3, 3.14);

        assertThat(cv.anyMatch((i, v) -> v != 3.14), is(false));
    }

    @Test
    public void shouldCreateFromSpecifiedBuffer() {
        VectorBuffer buffer = FixedVectorBuffer.allocate(3);
        buffer.set(0, 5.0d);
        buffer.set(1, 6.0d);
        buffer.set(2, 7.0d);

        Vector v = Vector.from(buffer);
        assertThat(v.dimension(), is(3));
        assertThat(v.at(1), is(5.0d));
        assertThat(v.at(2), is(6.0d));
        assertThat(v.at(3), is(7.0d));
    }

    @Test
    public void shouldGetSameValueAsSet() {
        Vector v = Vector.zero(3);
        v.setAt(1, 5.0d);
        v.setAt(2, 6.0d);
        v.setAt(3, 7.0d);

        assertThat(v.at(1), is(5.0d));
        assertThat(v.at(2), is(6.0d));
        assertThat(v.at(3), is(7.0d));
    }

    @Test
    public void shouldReturnDimension() {
        Vector v = Vector.newInstance(4);

        assertThat(v.dimension(), is(4));
    }

    @Test
    public void shouldPopulateUsingSupplier() {
        Vector v = Vector.zero(3);
        v.populate(() -> 3.14d);

        assertThat(v.at(1), is(3.14d));
        assertThat(v.at(2), is(3.14d));
        assertThat(v.at(3), is(3.14d));
    }

    @Test
    public void shouldTransformUsingFunction() {
        Vector v = Vector.of(5.0d, 6.0d, 7.0d);
        v.transform((i,c) -> c + 10.0d);

        assertThat(v.at(1), is(15.0d));
        assertThat(v.at(2), is(16.0d));
        assertThat(v.at(3), is(17.0d));
    }

    @Test
    public void shouldDetectComponentsUsingPredicate() {
        Vector v = Vector.of(2.0d, 3.0d, 4.0d);

        assertThat(v.anyMatch((i,c) -> c == 2.0d), is(true));
        assertThat(v.anyMatch((i,c) -> c == 3.0d), is(true));
        assertThat(v.anyMatch((i,c) -> c == 4.0d), is(true));
        assertThat(v.anyMatch((i,c) -> c == 5.0d), is(false));
    }

    @Test
    public void shouldProvideComponentsAsAStream() {
        Vector v = Vector.of(1, 2, 3);

        List<Double> vl = v.components().boxed().collect(toList());
        assertThat(vl.size(), is(3));
        assertThat(vl.get(0), is(1.0d));
        assertThat(vl.get(1), is(2.0d));
        assertThat(vl.get(2), is(3.0d));
    }

    @Test
    public void shouldDuplicateItself() {
        Vector v = Vector.of(5.0d, 6.0d, 7.0d);
        Vector c = v.copy();

        assertThat(c.at(1), is(5.0d));
        assertThat(c.at(2), is(6.0d));
        assertThat(c.at(3), is(7.0d));
    }

    @Test
    public void shouldMultiplyByConstant() {
        Vector v = Vector.of(2, 3);
        Vector r = Vector.of(12, 18);

        assertThat(v.multiply(6), equalTo(r));
    }

    @Test
    public void shouldDivideByConstant() {
        Vector v = Vector.of(15, 5, 10);
        Vector r = Vector.of(3, 1, 2);

        assertThat(v.divide(5), equalTo(r));
    }

    @Test
    public void shouldAdd() {
        Vector v1 = Vector.of(4, 2, 7);
        Vector v2 = Vector.of(3, 1, 2);
        Vector r = Vector.of(7, 3, 9);

        assertThat(v1.add(v2), equalTo(r));
    }

    @Test
    public void shouldSubtract() {
        Vector v1 = Vector.of(4, 2, 7);
        Vector v2 = Vector.of(3, 1, 2);
        Vector r = Vector.of(1, 1, 5);

        assertThat(v1.subtract(v2), equalTo(r));
    }

    @Test
    public void shouldComputeLength() {
        Vector v = Vector.of(3, 1, 2);

        assertThat(v.length(), closeTo(3.742, 0.0004));
    }

    @Test
    public void shouldNormalize() {
        Vector v = Vector.of(3, 1, 2);
        Vector normalized = Vector.of(0.802, 0.267, 0.534);

        assertThat(v.normalize(), closeToVector(normalized, 0.001));
        assertThat(v.normalize().length(), closeTo(1.0d, EPSILON));
    }

    @Test
    public void shouldComputeInnerProduct() {
        Vector v = Vector.of(1, 1, 4);
        Vector u = Vector.of(1, 2, -1);

        assertThat(v.innerProduct(u), is(-1.0));
    }

    @Test
    public void shouldComputeCrossProduct() {
        Vector u = Vector.of(2, 3, 4);
        Vector v = Vector.of(5, 6, 7);
        Vector r = Vector.of(-3, 6, -3);

        assertThat(u.crossProduct(v), equalTo(r));
    }

    @Test
    public void shouldProjectOrthogonallyOnNonZeroVector() {
        Vector v = Vector.of(1, 1, 4);
        Vector u = Vector.of(1, 2, -1);
        Vector p = Vector.of(-1.0/6.0, -1.0/3.0, 1.0/6.0);

        assertThat(v.projectOnto(u), equalTo(p));
    }

    @Test
    public void shouldProjectOrthogonallyOnZeroVector() {
        Vector v = Vector.of(1, 1, 4);
        Vector u = Vector.zero(3);
        Vector p = Vector.zero(3);

        assertThat(v.projectOnto(u), equalTo(p));
    }

    @Test
    public void shouldDetectOrthogonality() {
        Vector u = Vector.of(1, 3, 2);
        Vector v = Vector.of(3, -1, 0);
        Vector w = Vector.of(1, 3, -5);

        assertThat(u.isOrthogonalTo(v), is(true));
        assertThat(u.isOrthogonalTo(w), is(true));
        assertThat(v.isOrthogonalTo(w), is(true));
    }

    @Test
    public void shouldDetectOrthonormality() {
        Vector u = Vector.of(-1, 0, 0);
        Vector v = Vector.of(0, 1, 0);
        Vector w = Vector.of(0, 0, -1);
        Vector x = Vector.of(-3, 0, 0);

        assertThat(u.isOrthonormalTo(v), is(true));
        assertThat(u.isOrthonormalTo(w), is(true));
        assertThat(v.isOrthonormalTo(w), is(true));
        assertThat(v.isOrthonormalTo(x), is(false));
    }

    @Test
    public void shouldDetectNormalizedVector() {
        Vector u = Vector.of(1, 3, 5);
        Vector v = Vector.of(1, 3, 5).normalize();

        assertThat(u.isNormalizedVector(), is(false));
        assertThat(v.isNormalizedVector(), is(true));
    }

    @Test
    public void shouldDetectZeroVector() {
        Vector u = Vector.of(1, 0, 0);
        Vector v = Vector.of(0, 0, 0);

        assertThat(u.isZeroVector(), is(false));
        assertThat(v.isZeroVector(), is(true));
    }

    @Test
    public void shouldOrthogonalize() {
        Vector v1 = Vector.of(1, 2, 2);
        Vector v2 = Vector.of(-1, 0, 2);
        Vector v3 = Vector.of(0, 0, 1);

        Vector.orthogonalize(asList(v1, v2, v3));

        assertThat(v1, closeToVector(Vector.of(1, 2, 2), EPSILON));
        assertThat(v2, closeToVector(Vector.of(-4.0 / 3.0, -2.0 / 3.0, 4.0 / 3.0), EPSILON));
        assertThat(v3, closeToVector(Vector.of(2.0 / 9.0, -2.0 / 9.0, 1.0 / 9.0), EPSILON));
    }

    @Test
    public void shouldOrthonormalize() {
        Vector v1 = Vector.of(1, 2, 2);
        Vector v2 = Vector.of(-1, 0, 2);
        Vector v3 = Vector.of(0, 0, 1);

        Vector.orthonormalize(asList(v1, v2, v3));

        assertThat(v1, closeToVector(Vector.of(1.0/3.0, 2.0/3.0, 2.0/3.0), EPSILON));
        assertThat(v2, closeToVector(Vector.of(-2.0/3.0, -1.0/3.0, 2.0/3.0), EPSILON));
        assertThat(v3, closeToVector(Vector.of(2.0/3.0, -2.0/3.0, 1.0/3.0), EPSILON));
    }

    @Test
    public void shouldDetectEqualVectors() {
        Vector v1 = Vector.of(1, -2, 5);
        Vector v2 = Vector.of(1, -2, 5);

        assertThat(v1, equalTo(v2));
    }

    @Test
    public void shouldFormatVectorAsString() {
        Vector v = Vector.of(3.14d, -2, 6);

        assertThat(v.toString(NumberFormatter.compactNoDecimals()), equalTo("3 -2 6"));
    }
}
