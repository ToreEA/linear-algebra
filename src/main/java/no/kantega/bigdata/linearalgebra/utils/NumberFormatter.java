package no.kantega.bigdata.linearalgebra.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Function;

/**
 * Formats a double precision number as a string
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class NumberFormatter implements Function<Double, String> {
    private final int width;
    private final DecimalFormat decimalFormat;

    public static NumberFormatter pretty() {
        return of("###,##0.0", 9);
    }

    public static NumberFormatter compact() {
        return of("###,##0.0", 1);
    }

    public static NumberFormatter compactNoDecimals() {
        return of("#", 1);
    }

    public static NumberFormatter of(String pattern) {
        return new NumberFormatter(pattern, 1);
    }

    public static NumberFormatter of(String pattern, int width) {
        return new NumberFormatter(pattern, width);
    }

    private NumberFormatter(String pattern, int width) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb_NO"));
        this.decimalFormat = new DecimalFormat(pattern, decimalFormatSymbols);
        this.width = width;
    }

    @Override
    public String apply(Double value) {
        return String.format("%" + width + "s", decimalFormat.format(value));
    }
}
