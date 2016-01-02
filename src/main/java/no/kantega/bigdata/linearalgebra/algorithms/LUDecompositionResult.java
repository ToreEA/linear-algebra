package no.kantega.bigdata.linearalgebra.algorithms;

import no.kantega.bigdata.linearalgebra.Matrix;
import no.kantega.bigdata.linearalgebra.Vector;

public class LUDecompositionResult {
    private final Matrix lu;
    private final int[] pi;
    private final double signOfDeterminant;

    /**
     * Constructs a LU decomposition result object.
     *
     * @param lu the compact storage of the L and U matrices in one physical matrix
     * @param pi the permuation indices.
     * @param signOfDeterminant the sign of the determinant before multiplying the diagonal elements.
     */
    public LUDecompositionResult(Matrix lu, int[] pi, double signOfDeterminant) {
        this.lu = lu;
        this.pi = pi;
        this.signOfDeterminant = signOfDeterminant;
    }

    public double determinant() {
        double det = signOfDeterminant;
        for (int i = 0; i < lu.size().rows(); i++) {
            det *= lu.at(i + 1, i + 1);
        }
        return det;
    }

    /**
     * Gets the permutation matrix representing the total set of row swap operations performed
     * during LU decomposition. The product of this and the original matrix equals the product of
     * the lower and upper matrix: PA = LU
     *
     * @return the permutation matrix.
     */
    public Matrix permutationMatrix() {
        int d = lu.size().rows();
        Matrix matrix = Matrix.zero(d, d);
        for (int i = 0; i < d; i++) {
            matrix.setAt(i + 1, pi[i] + 1, 1);
        }
        return matrix;
    }

    /**
     * Gets the lower triangular matrix. Has a unit diagonal.
     *
     * @return the lower triangular matrix.
     */
    public Matrix lowerMatrix() {
        return lu.copy().transform((p,v) -> {
            if (p.isAboveDiagonal()) {
                return 0.0d;
            } else if (p.isOnDiagonal()) {
                return 1.0d;
            } else {
                return v;
            }
        });
    }

    /**
     * Gets the upper triangular matrix.
     *
     * @return the upper triangular matrix.
     */
    public Matrix getU() {
        return lu.copy().transform((p,v) -> {
            if (p.isBelowDiagonal()) {
                return 0.0d;
            } else {
                return v;
            }
        });
    }

    /**
     * Solves the linear system of equations Ax = b, given that A is decomposed into L and U,
     * with permutation of A given by P.
     *
     * The method is based on that A is the product of L and U, i.e. Ax = b => PAx = Pb => LUx = Pb
     * The solution proceeds by solving the linear equation Ly = Pb for y and
     * subsequently solving the linear equation Ux = y for x.
     *
     * This algorithm requires a compact LU matrix produced by the Doolittle LU decomposition algorithm.
     *
     * @param b the right hand side values of the equations
     * @return values of x1, x2, ..., i.e. the x vector containing the solution.
     */
    public Vector solve(Vector b){
        int d = lu.size().rows();

        Vector x = Vector.zero(d);
        Vector y = Vector.zero(d);

        // Solves the linear equation Ly = Pb for y
        for (int i=0; i<d; ++i) {
            double sum = 0.0d;
            for (int k=0; k<i; ++k) {
                sum += lu.at(i+1, k+1) * y.at(k+1);
            }
            // Uses pivot index vector to pick correct b value
            y.setAt(i+1, b.at(pi[i]+1) - sum); // not dividing by diagonals
        }

        // Solves the linear equation Ux = y for x
        for (int i=d-1; i>=0; --i) {
            double sum = 0.0d;
            for (int k=i+1; k<d; ++k) {
                sum += lu.at(i+1, k+1) * x.at(k+1);
            }

            x.setAt(i+1, (y.at(i+1) - sum) / lu.at(i+1, i+1));
        }

        return x;
    }

    /**
     * Calculates the inverse of the original matrix (A) having been decomposed into L and U.
     * The inverse is found by solving Ax = b, where b is columns in the identity matrix. Each solution vector x
     * is inserted into the corresponding column of the resulting inverse matrix.
     *
     * @return the inverse of the original matrix (A) having been decomposed into L and U
     */
    public Matrix inverse() {
        int d = lu.size().rows();

        Matrix x = Matrix.zero(d,d);
        Vector y = Vector.zero(d);

        // Solve for each column b of the identity matrix
        for (int e=0; e<d; ++e) {
            Vector b = Vector.zero(d);
            b.setAt(e + 1, 1);

            // Solves the linear equation Ly = Pb for y
            for (int i = 0; i < d; ++i) {
                double sum = 0.0d;
                for (int k = 0; k < i; ++k) {
                    sum += lu.at(i + 1, k + 1) * y.at(k + 1);
                }

                // Uses pivot index vector to pick correct b value
                y.setAt(i + 1, b.at(pi[i] + 1) - sum); // not dividing by diagonals
            }

            // Solves the linear equation Ux = y for x
            for (int i = d - 1; i >= 0; --i) {
                double sum = 0.0d;
                for (int k = i + 1; k < d; ++k) {
                    sum += lu.at(i + 1, k + 1) * x.at(k + 1, e + 1);
                }

                // Insert solution vector as a column in the inverse matrix
                x.setAt(i + 1, e + 1, (y.at(i + 1) - sum) / lu.at(i + 1, i + 1));
            }
        }

        return x;
    }
}
