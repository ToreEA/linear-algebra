// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra.utils;


/**
 * TODO: Purpose and responsibility
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public abstract class Assert {
    public static void require(Requirement requirement, String msg, Object... args) {
        if (!requirement.test()) {
            throw new IllegalArgumentException(String.format(msg, args));
        }
    }

    public static void precondition(Requirement requirement, String msg, Object... args) {
        if (!requirement.test()) {
            throw new IllegalStateException(String.format(msg, args));
        }
    }

    @FunctionalInterface
    public interface Requirement {
        boolean test();
    }
}
