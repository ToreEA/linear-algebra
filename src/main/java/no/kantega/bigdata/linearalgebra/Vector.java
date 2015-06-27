// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra;

import no.kantega.bigdata.linearalgebra.buffer.FixedVectorBuffer;
import no.kantega.bigdata.linearalgebra.buffer.VectorBuffer;
import no.kantega.bigdata.linearalgebra.utils.NumberFormatter;

import java.util.List;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static no.kantega.bigdata.linearalgebra.utils.Assert.require;

/**
 * Implements a vector
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class Vector {
    private final int dimension;
    private final VectorBuffer components;

    public static Vector of(double... values) {
        requireNonNull(values, "values can't be null");
        return new Vector(values.length).transform((i,v) -> values[i-1]);
    }

    public static Vector ofDimension(int dimension) {
        return new Vector(dimension);
    }

    public static Vector zero(int dimension) {
        return Vector.constant(dimension, 0.0d);
    }

    public static Vector constant(int dimension, double value) {
        return new Vector(dimension).populate(() -> value);
    }

    public static Vector from(VectorBuffer buffer) {
        return new Vector(buffer);
    }

    private Vector(int dimension) {
        require(() -> dimension >= 1, "dimension must be 1 or higher");

        this.dimension = dimension;
        this.components = FixedVectorBuffer.allocate(dimension);
    }

    private Vector(VectorBuffer components) {
        requireNonNull(components, "components can't be null");

        this.dimension = components.size();
        this.components = components;
    }

    public int dimension() {
        return dimension;
    }

    public double at(int index) {
        requireValidIndex(index);

        return components.get(index);
    }

    public void setAt(int index, double value) {
        requireValidIndex(index);

        components.set(index, value);
    }

    public Vector populate(Supplier<Double> valueSupplier) {
        return transform((i, v) -> valueSupplier.get());
    }

    public IntStream indices() {
        return IntStream.rangeClosed(1, dimension).parallel();
    }

    public Stream<Double> components() {
        return indices().boxed().map(components::get);
    }

    public Vector copy() {
        return new Vector(components.copy());
    }

    // aka norm or magnitude
    public double length() {
        return Math.sqrt(components()
                .map(v -> v * v)
                .reduce(0.0d, Double::sum));
    }

    public Vector add(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't add vectors of different dimension");

        return transform((i, v) -> v + other.components.get(i));
    }

    public Vector subtract(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't subtract vectors of different dimension");

        return transform((i, v) -> v - other.components.get(i));
    }

    public Vector multiply(double value) {
        return transform((i, v) -> v * value);
    }

    public Vector divide(double value) {
        return transform((i, v) -> v / value);
    }

    /**
     * The dot product, or scalar product (or sometimes inner product in the context of Euclidean space), is an algebraic operation
     * that takes two equal-length sequences of numbers (usually coordinate vectors) and returns a single number. This operation can
     * be defined either algebraically or geometrically. Algebraically, it is the sum of the products of the corresponding entries of
     * the two sequences of numbers. Geometrically, it is the product of the Euclidean magnitudes of the two vectors and the cosine
     * of the angle between them. The name "dot product" is derived from the centered dot " · " that is often used to designate this
     * operation; the alternative name "scalar product" emphasizes the scalar (rather than vectorial) nature of the result.
     *
     * In three-dimensional space, the dot product contrasts with the cross product of two vectors, which produces a pseudovector as
     * the result. The dot product is directly related to the cosine of the angle between two vectors in Euclidean space of any
     * number of dimensions. Thus, the dot product can be thought of as a measure of parallelism between the vectors.
     */
    public double innerProduct(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't get inner product for vectors of different dimension");

        return indices().boxed()
                .map(i -> components.get(i) * other.components.get(i))
                .reduce(0.0d, Double::sum);
    }

    /**
     * The cross product or vector product is a binary operation on two vectors in three-dimensional space and is denoted by the symbol ×.
     * The cross product a × b of two linearly independent vectors a and b is a vector that is perpendicular to both and therefore normal to
     * the plane containing them. It has many applications in mathematics, physics, engineering, and computer programming.
     *
     * If two vectors have the same direction (or have the exact opposite direction from one another, i.e. are not linearly independent)
     * or if either one has zero length, then their cross product is the zero vector.
     *
     * Because the magnitude of the cross product goes by the sine of the angle between its arguments, the cross product can be thought of
     * as a measure of perpendicularity between the two vectors.
     *
     * Properties of the cross product:
     *
     * b x a = -(a x b)
     */
    public Vector crossProduct(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't get inner product for vectors of different dimension");
        require(() -> this.dimension() == 3, "can compute cross product for three dimensional vectors only");

        return Vector.of(at(2)*other.at(3) - at(3)*other.at(2), at(3)*other.at(1) - at(1)*other.at(3), at(1)*other.at(2) - at(2)*other.at(1));
    }

    /**
     * The projection operator projects the vector v (this) orthogonally onto the line spanned by vector u (other).
     * If u is the zero vector, the projection is also the zero vector.
     */
    public Vector projectOnto(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't project a vector onto a vector of different dimension");

        if (other.isZeroVector()) {
            return Vector.zero(other.dimension());
        } else {
            double innerProductVU = innerProduct(other);
            double innerProductUU = other.innerProduct(other);

            return other.copy().multiply(innerProductVU/innerProductUU);
        }
    }

    public Vector normalize() {
        return divide(length());
    }

    public boolean isOrthogonalTo(Vector other) {
        return innerProduct(other) == 0.0d;
    }

    public boolean isZeroVector() {
        return !anyMatch((i,v) -> v != 0.0d);
    }

    // = unit vector
    public boolean isNormalizedVector() {
        return length() == 1.0d;
    }

    public boolean isOrthonormalTo(Vector other) {
        requireNonNull(other, "other can't be null");
        return isNormalizedVector() && other.isNormalizedVector() && isOrthogonalTo(other);
    }

    public Vector transform(BiFunction<Integer, Double, Double> func) {
        requireNonNull(func, "func can't be null");
        indices().forEach(i -> components.set(i, func.apply(i, components.get(i))));
        return this;
    }

    public Vector forEach(BiConsumer<Integer, Double> consumer) {
        requireNonNull(consumer, "consumer can't be null");
        indices().forEach(i -> consumer.accept(i, components.get(i)));
        return this;
    }

    public boolean anyMatch(BiPredicate<Integer, Double> predicate) {
        requireNonNull(predicate, "predicate can't be null");
        return indices().anyMatch(i -> predicate.test(i, components.get(i)));
    }

    @Override
    public String toString() {
        return toString(NumberFormatter.pretty());
    }

    public String toString(Function<Double, String> formatter) {
        requireNonNull(formatter, "formatter can't be null");
        StringBuilder sb = new StringBuilder();
        indices().forEachOrdered(i -> {
            sb.append(formatter.apply(components.get(i)));
            sb.append(i == dimension ? "\n" : " ");
        });
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector)) return false;

        Vector other = (Vector) o;

        return dimension == other.dimension() && !anyMatch((i,v) -> v != other.components.get(i));
    }

    /**
     * Gram-Schmidt Orthonormalization
     *
     * The Gram–Schmidt process is a method for orthonormalising a set of vectors in an inner product space, most commonly the Euclidean space Rn.
     * The Gram–Schmidt process takes a finite, linearly independent set S = {v1, ..., vk} for k ≤ n and generates an orthogonal
     * set S′ = {u1, ..., uk} that spans the same k-dimensional subspace of Rn as S.
     *
     * The application of the Gram–Schmidt process to the column vectors of a full column rank matrix yields the QR decomposition
     * (it is decomposed into an orthogonal and a triangular matrix).
     *
     * This method implements the modified Gram-Schmidt process (MGS), which is more numerical stable than the classical algorithm.
     * Other orthogonalization algorithms use Householder transformations or Givens rotations. The algorithms using Householder transformations
     * are more stable than the stabilized Gram–Schmidt process. On the other hand, the Gram–Schmidt process produces the jth orthogonalized
     * vector after the jth iteration, while orthogonalization using Householder reflections produces all the vectors only at the end.
     * This makes only the Gram–Schmidt process applicable for iterative methods.
     */
    public static void orthonormalize(List<Vector> vectors) {
        requireNonNull(vectors, "vectors can't be null");
        require(() -> vectors.size() >= 2, "need two or more vectors to orthonormalize");
        require(() -> vectors.stream().map(Vector::dimension).distinct().count() == 1, "can't orthonormalize vectors with different dimensions");

        int k = vectors.size();
        for (int i = 1; i <= k; i++) {
            Vector vi = vectors.get(i-1).normalize();
            for (int j = i + 1; j <= k; j++) {
                // Remove component in direction vi from vj
                Vector vj = vectors.get(j - 1);
                vj.subtract(vj.projectOnto(vi));
            }
        }
    }

    /**
     * Gram-Schmidt Orthogonalization
     *
     * Same as the MGS ortonormalization, but without the normalization step.
     */
    public static void orthogonalize(List<Vector> vectors) {
        requireNonNull(vectors, "vectors can't be null");
        require(() -> vectors.size() >= 2, "need two or more vectors to orthogonalize");
        require(() -> vectors.stream().map(Vector::dimension).distinct().count() == 1, "can't orthogonalize vectors with different dimensions");

        int k = vectors.size();
        for (int i = 1; i <= k; i++) {
            Vector vi = vectors.get(i - 1);
            for (int j = i + 1; j <= k; j++) {
                // Remove component in direction vi from each vj
                Vector vj = vectors.get(j - 1);
                vj.subtract(vj.projectOnto(vi));
            }
        }
    }

    private int requireValidIndex(int index) {
        require(() -> index >= 1 && index <= dimension, "index must be between 1 and %d", dimension);
        return index;
    }
}
