// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra.buffer;

import no.kantega.bigdata.linearalgebra.Size;

/**
 * TODO: Purpose and responsibility
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public interface MatrixBuffer {
    double get(int row, int col);

    void set(int row, int col, double value);

    VectorBuffer row(int row);

    VectorBuffer column(int col);

    Size size();

    MatrixBuffer copy();

    void transpose();
}
