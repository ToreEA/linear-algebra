package no.kantega.bigdata.linearalgebra.utils;

/**
 * Provides assertions constructs for arguments and object state
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
