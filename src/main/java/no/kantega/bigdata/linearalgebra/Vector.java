package no.kantega.bigdata.linearalgebra;

import no.kantega.bigdata.linearalgebra.buffer.FixedVectorBuffer;
import no.kantega.bigdata.linearalgebra.buffer.VectorBuffer;
import no.kantega.bigdata.linearalgebra.utils.NumberFormatter;

import java.util.List;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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

    /**
     * Creates a vector from specified sequence of values
     *
     * @param values the sequence of values to form a vector
     * @return a new vector
     */
    public static Vector of(double... values) {
        requireNonNull(values, "values can't be null");
        return new Vector(values.length).transform((i,v) -> values[i]);
    }

    /**
     * Creates a non-populated vector of specified dimension
     *
     * @param dimension the vector dimension
     * @return a new vector
     */
    public static Vector ofDimension(int dimension) {
        return new Vector(dimension);
    }

    /**
     * Creates a vector of specified dimension populated with zero values
     *
     * @param dimension the vector dimension
     * @return a new vector
     */
    public static Vector zero(int dimension) {
        return Vector.constant(dimension, 0.0d);
    }

    /**
     * Creates a vector of specified dimension populated with specified constant values
     *
     * @param dimension the vector dimension
     * @param value the constant value
     * @return a new vector
     */
    public static Vector constant(int dimension, double value) {
        return new Vector(dimension).populate(() -> value);
    }

    /**
     * Creates a vector using specified buffer
     *
     * @param buffer the buffer to consume
     * @return a new vector
     */
    public static Vector from(VectorBuffer buffer) {
        return new Vector(buffer);
    }

    /**
     * Creates a new vector of specified dimension
     *
     * @param dimension the vector dimension
     */
    private Vector(int dimension) {
        require(() -> dimension >= 1, "dimension must be 1 or higher");

        this.dimension = dimension;
        this.components = FixedVectorBuffer.allocate(dimension);
    }

    /**
     * Creates a new vector by consuming specified buffer
     *
     * @param buffer the buffer to consume
     */
    private Vector(VectorBuffer buffer) {
        requireNonNull(buffer, "buffer can't be null");

        this.dimension = buffer.size();
        this.components = buffer;
    }

    /**
     * Gets the vector dimension, which is >= 1
     *
     * @return the vector dimension
     */
    public int dimension() {
        return dimension;
    }

    /**
     * Gets the component at specified index
     *
     * @param index the component index (1-based)
     * @return the component value
     */
    public double at(int index) {
        requireValidIndex(index);

        return components.get(index-1);
    }

    /**
     * Sets the component at specified index
     *
     * @param index the component index (1-based)
     * @param value the component value to set
     */
    public void setAt(int index, double value) {
        requireValidIndex(index);

        components.set(index-1, value);
    }

    /**
     * Populates the vector with component values from specified supplier
     *
     * @param valueSupplier the component value supplier
     * @return the populated vector
     */
    public Vector populate(DoubleSupplier valueSupplier) {
        return transform((i, v) -> valueSupplier.getAsDouble());
    }

    /**
     * Gets a stream of the component values
     *
     * @return a stream of component values
     */
    public DoubleStream components() {
        return indices().mapToDouble(components::get);
    }

    /**
     * Creates a copy of this vector
     *
     * @return a copy of this vector
     */
    public Vector copy() {
        return new Vector(components.copy());
    }

    /**
     * Gets the length of this vector. The length, aka norm or magnitude, of a vector is the inner product
     * of the vector with itself, and is always non-negative.
     *
     * @return the length of this vector
     */
    public double length() {
        return Math.sqrt(components()
                .map(v -> v * v)
                .reduce(0.0d, Double::sum));
    }

    /**
     * Adds specified vector to this vector. The vectors must have the same dimension.
     *
     * @param other the vector to add
     * @return this vector after addition
     */
    public Vector add(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't add vectors of different dimension");

        return transform((i, v) -> v + other.components.get(i));
    }

    /**
     * Subtracts specified vector from this vector. The vectors must have the same dimension.
     *
     * @param other the vector to subtract
     * @return this vector after subtraction
     */
    public Vector subtract(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't subtract vectors of different dimension");

        return transform((i, v) -> v - other.components.get(i));
    }

    /**
     * Multiplies with specified scalar value
     *
     * @param value the value to multiply with
     * @return this vector after multiplication
     */
    public Vector multiply(double value) {
        return transform((i, v) -> v * value);
    }

    /**
     * Divides by specified scalar value
     *
     * @param value the value to divide by
     * @return this vector after division
     */
    public Vector divide(double value) {
        return transform((i, v) -> v / value);
    }

    /**
     * Calculates the inner product of this and the specified vector.
     *
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
     *
     * @param other the vector to calculate inner product with
     * @return the inner product
     */
    public double innerProduct(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't get inner product for vectors of different dimension");

        return indices()
                .mapToDouble(i -> components.get(i) * other.components.get(i))
                .reduce(0.0d, Double::sum);
    }

    /**
     * Calculates the cross product of this and the specified vector.
     *
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
     *
     * @param other the vector to calculate cross product with
     * @return the cross product
     */
    public Vector crossProduct(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't get cross product for vectors of different dimension");
        require(() -> this.dimension() == 3, "can compute cross product for three dimensional vectors only");

        return Vector.of(components.get(1)*other.components.get(2) - components.get(2)*other.components.get(1),
                         components.get(2)*other.components.get(0) - components.get(0)*other.components.get(2),
                         components.get(0)*other.components.get(1) - components.get(1)*other.components.get(0));
    }

    /**
     * Calculates the projection of this vector onto the specified vector.
     *
     * The projection operator projects the vector v (this) orthogonally onto the line spanned by vector u (other).
     * If u is the zero vector, the projection is also the zero vector.
     *
     * @param other the vector to project onto
     * @return the projection
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

    /**
     * Normalizes this vector, i.e. divides by its length making the new length 1.
     * The term normalized vector is sometimes used as a synonym for unit vector.
     *
     * @return the normalized vector
     */
    public Vector normalize() {
        return divide(length());
    }

    /**
     * Gets whether this vector is orthogonal to specified vector.
     * Two vectors are orthogonal if their inner product is zero.
     *
     * @param other the vector to check orthogonality with
     * @return true if this vector is orthogonal to specified vector; else false
     */
    public boolean isOrthogonalTo(Vector other) {
        return innerProduct(other) == 0.0d;
    }

    /**
     * Gets whether this vector is zero vector.
     * A zero vector contains only zero components.
     *
     * @return true if this is a zero vector; else false
     */
    public boolean isZeroVector() {
        return !anyMatch((i,v) -> v != 0.0d);
    }

    /**
     * Gets whether this is a normalized vector.
     * A normalized vector, aka unit vector, has length 1.
     *
     * @return true if this is a normalized vector; else false
     */
    public boolean isNormalizedVector() {
        return length() == 1.0d;
    }

    /**
     * Gets whether this vector is orthonormal to specified vector.
     * Two vectors are orthonormal if both are normalized and orthogonal.
     *
     * @param other the vector to check orthonormality with
     * @return true if this vector is orthonormal to specified vector; else false
     */
    public boolean isOrthonormalTo(Vector other) {
        requireNonNull(other, "other can't be null");
        return isNormalizedVector() && other.isNormalizedVector() && isOrthogonalTo(other);
    }

    /**
     * Modified the component values using the specfied bi-function
     *
     * @param func the function transforming component values
     * @return this vector after transformation
     */
    public Vector transform(BiFunction<Integer, Double, Double> func) {
        requireNonNull(func, "func can't be null");
        indices().forEach(i -> components.set(i, func.apply(i, components.get(i))));
        return this;
    }

    /**
     * Iterates over the component values, invoking specified consumer
     *
     * @param consumer the consumer to invoke on each component value
     * @return this vector
     */
    public Vector forEach(BiConsumer<Integer, Double> consumer) {
        requireNonNull(consumer, "consumer can't be null");
        indices().forEach(i -> consumer.accept(i, components.get(i)));
        return this;
    }

    /**
     * Tests whether specified predicate holds for any component value
     *
     * @param predicate the predicate to test
     * @return true if predicate holds for at least one component value
     */
    public boolean anyMatch(BiPredicate<Integer, Double> predicate) {
        requireNonNull(predicate, "predicate can't be null");
        return indices().anyMatch(i -> predicate.test(i, components.get(i)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(NumberFormatter.pretty());
    }

    /**
     * Formats the component values into a string using the specified value formatter
     *
     * @param formatter the component value formatter
     * @return a string representation of this vector
     */
    public String toString(Function<Double, String> formatter) {
        requireNonNull(formatter, "formatter can't be null");
        StringBuilder sb = new StringBuilder();
        indices().forEachOrdered(i -> {
            sb.append(formatter.apply(components.get(i)));
            sb.append(i < dimension-1 ? " " : "");
        });
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector)) return false;

        Vector other = (Vector) o;

        return dimension == other.dimension() && !anyMatch((i,v) -> v != other.components.get(i));
    }

    /**
     * Performes the (modified) Gram-Schmidt orthonormalization (MGS) on the specified set of vectors.
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
     *
     * @param vectors the vectors to orthonormalize
     */
    public static void orthonormalize(List<Vector> vectors) {
        requireNonNull(vectors, "vectors can't be null");
        require(() -> vectors.size() >= 2, "need two or more vectors to orthonormalize");
        require(() -> vectors.stream().map(Vector::dimension).distinct().count() == 1, "can't orthonormalize vectors with different dimensions");

        int k = vectors.size();
        for (int i = 0; i < k; i++) {
            Vector vi = vectors.get(i).normalize();
            for (int j = i + 1; j < k; j++) {
                // Remove component in direction vi from vj
                Vector vj = vectors.get(j);
                vj.subtract(vj.projectOnto(vi));
            }
        }
    }

    /**
     * Performs the Gram-Schmidt orthogonalization on the specified set of vectors
     *
     * Same as the MGS orthonormalization above, but without the normalization step.
     *
     * @param vectors the vectors to orthonormalize
     */
    public static void orthogonalize(List<Vector> vectors) {
        requireNonNull(vectors, "vectors can't be null");
        require(() -> vectors.size() >= 2, "need two or more vectors to orthogonalize");
        require(() -> vectors.stream().map(Vector::dimension).distinct().count() == 1, "can't orthogonalize vectors with different dimensions");

        int k = vectors.size();
        for (int i = 0; i < k; i++) {
            Vector vi = vectors.get(i);
            for (int j = i + 1; j < k; j++) {
                // Remove component in direction vi from each vj
                Vector vj = vectors.get(j);
                vj.subtract(vj.projectOnto(vi));
            }
        }
    }

    private int requireValidIndex(int index) {
        require(() -> index >= 1 && index <= dimension, "index must be between 1 and %d", dimension);
        return index;
    }

    private IntStream indices() {
        return IntStream.rangeClosed(0, dimension-1).parallel();
    }
}
