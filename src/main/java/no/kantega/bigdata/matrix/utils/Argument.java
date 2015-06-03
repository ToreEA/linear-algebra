// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.matrix.utils;

import java.util.function.Predicate;

/**
 * TODO: Purpose and responsibility
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public abstract class Argument {
    public static void require(Requirement requirement, String msg) {
        if (!requirement.test()) {
            throw new IllegalArgumentException(msg);
        }
    }

    @FunctionalInterface
    public interface Requirement {
        boolean test();
    }
}
