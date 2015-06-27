package no.kantega.bigdata.linearalgebra;

import org.junit.Test;

import static java.util.Arrays.asList;
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

    @Test
    public void shouldCreateFromValues() {
        Vector v = Vector.of(5, 6, 7, 8);
        assertThat(v.at(1), is(5.0));
        assertThat(v.at(2), is(6.0));
        assertThat(v.at(3), is(7.0));
        assertThat(v.at(4), is(8.0));
    }

    @Test
    public void shouldCreateZeroVector() {
        Vector zv = Vector.zero(3);
        assertThat(zv.anyMatch((i, v) -> v != 0.0), is(false));
    }

    @Test
    public void shouldPrefillWithSingleValue() {
        Vector cv = Vector.constant(3, 3.14);
        assertThat(cv.anyMatch((i, v) -> v != 3.14), is(false));
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
        assertEqual(v.normalize(), normalized, 0.001);
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
    public void shouldProjectOrthogonally() {
        Vector v = Vector.of(1, 1, 4);
        Vector u = Vector.of(1, 2, -1);
        Vector p = Vector.of(-1.0/6.0, -1.0/3.0, 1.0/6.0);
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

        assertEqual(v1, Vector.of(1, 2, 2), 0.001);
        assertEqual(v2, Vector.of(-4.0 / 3.0, -2.0 / 3.0, 4.0 / 3.0), 0.001);
        assertEqual(v3, Vector.of(2.0 / 9.0, -2.0 / 9.0, 1.0 / 9.0), 0.001);
    }

    @Test
    public void shouldOrthonormalize() {
        Vector v1 = Vector.of(1, 2, 2);
        Vector v2 = Vector.of(-1, 0, 2);
        Vector v3 = Vector.of(0, 0, 1);

        Vector.orthonormalize(asList(v1, v2, v3));

        assertEqual(v1, Vector.of(1.0/3.0, 2.0/3.0, 2.0/3.0), 0.001);
        assertEqual(v2, Vector.of(-2.0/3.0, -1.0/3.0, 2.0/3.0), 0.001);
        assertEqual(v3, Vector.of(2.0/3.0, -2.0/3.0, 1.0/3.0), 0.001);
    }

    @Test
    public void shouldDetectEqualVectors() {
        Vector v1 = Vector.of(1, -2, 5);
        Vector v2 = Vector.of(1, -2, 5);
        assertThat(v1, equalTo(v2));
    }

    private void assertEqual(Vector v1, Vector v2, double epsilon) {
        assertThat(v1.dimension(), is(v2.dimension()));
        v1.indices().forEachOrdered(i -> assertThat("Component " + i, v1.at(i), closeTo(v2.at(i), epsilon)));
    }
}