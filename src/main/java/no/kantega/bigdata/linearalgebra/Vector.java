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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
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
        return new Vector(values.length).transform((i,v) -> values[i]);
    }

    public static Vector ofDimension(int dimension) {
        return new Vector(dimension);
    }

    public static Vector zero(int dimension) {
        return new Vector(dimension).fill(0.0d);
    }

    public static Vector prefilled(int dimension, double value) {
        return new Vector(dimension).fill(value);
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

    public IntStream indices() {
        return IntStream.rangeClosed(1, dimension).parallel();
    }

    public Stream<Double> components() {
        return indices().boxed().map(components::get);
    }

    public Vector copy() {
        return new Vector(components.copy());
    }

    public Vector fill(double value) {
        return transform((i, v) -> value);
    }

    public double length() {
        return components()
                .map(v -> v * v)
                .reduce(0.0d, Double::sum);
    }

    public Vector add(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't add vectors of different dimension");

        return transform((i, v) -> v + other.components.get(i));
    }

    public Vector multiply(double value) {
        return transform((i, v) -> v * value);
    }

    // = dot product or scalar product
    public double innerProduct(Vector other) {
        requireNonNull(other, "other can't be null");
        require(() -> other.dimension() == this.dimension(), "can't get inner product for vectors of different dimension");

        return indices().boxed()
                .map(i -> components.get(i) * other.components.get(i))
                .reduce(0.0d, Double::sum);
    }

    public Vector normalize() {
        return multiply(1 / length());
    }

    public boolean isOrthogonalTo(Vector other) {
        return innerProduct(other) == 0.0d;
    }

    public boolean isZeroVector() {
        return !anyMatch((i,v) -> v != 0.0);
    }

    // = unit vector
    public boolean isNormalVector() {
        return length() == 1.0d;
    }

    public boolean isOrthonormalTo(Vector other) {
        requireNonNull(other, "other can't be null");
        return isNormalVector() && other.isNormalVector() && isOrthogonalTo(other);
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
        indices().forEach(i -> {
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

    // Gram-Schmidt Orthonormalization
    public static List<Vector> orthonormalize(List<Vector> vectors) {
        return vectors;
    }

    private int requireValidIndex(int index) {
        require(() -> index >= 1 && index <= dimension, "index must be between 1 and %d", dimension);
        return index;
    }
}
