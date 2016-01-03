package no.kantega.bigdata.linearalgebra;

import no.kantega.bigdata.linearalgebra.algorithms.LUDecompositionResult;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Performance test of the Matrix class
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class MatrixPerformanceTest {

    @Test
    public void multiplication() {
        Matrix m1 = Matrix.random(1000, 1000, -99.9d, +99.9d);
        Matrix m2 = Matrix.random(1000, 1000, -99.9d, +99.9d);

        Matrix product = timedExecution("Multiplication of 1000x1000 matrices", () -> m1.multiply(m2));
        //timedExecution("Multiplication of 10000x10000 matrices", () -> m1.multiply(m2));
        //timedExecution("Multiplication of 10000x10000 matrices", () -> m1.multiply(m2));
    }

    @Test
    public void gaussJordanElimination() {
        Matrix m = Matrix.random(1000, 1000, -99.9d, +99.9d);

        Matrix mm = timedExecution("Gauss-Jordan elimination of 1000x1000 matrix", m::gaussJordanElimination);
    }

    @Test
    public void luDecomposition() {
        Matrix m = Matrix.random(1000, 1000, -99.9d, +99.9d);

        LUDecompositionResult result = timedExecution("LU decomposition of 1000x1000 matrix", m::luDecomposition);
    }

    private <T> T timedExecution(String caption, Supplier<T> executable) {
        Instant startTime = Instant.now();
        T result = executable.get();
        Instant endTime = Instant.now();

        Duration duration = Duration.between(startTime, endTime);
        System.out.println(caption + ": " + formatDuration(duration));

        return result;
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.toMillis() / 1000;
        long milliSeconds = duration.toMillis() - seconds * 1000;
        return String.format("%d,%03d secs", seconds, milliSeconds);
    }
}
