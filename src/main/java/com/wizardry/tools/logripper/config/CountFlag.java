package com.wizardry.tools.logripper.config;

import org.refcodes.cli.Flag;

/**
 * A predefined verbose {@link Flag}: A predefined {@link Flag} gives its
 * {@link #SHORT_OPTION}, its {@link #LONG_OPTION} as well as its {@link #ALIAS}
 * an according semantics regarded by other subsystems.
 */
public class CountFlag extends Flag {

    public static final String ALIAS = "count";
    public static final String LONG_OPTION = "count";
    public static final Character SHORT_OPTION = 'c';

    /**
     * Constructs the predefined verbose {@link Flag}.
     */
    public CountFlag() {
        this( true );
    }

    /**
     * Constructs the predefined verbose {@link Flag}.
     *
     * @param hasShortOption True in case to also enable the short option, else
     *        only the long option takes effect.
     */
    public CountFlag(boolean hasShortOption ) {
        super( hasShortOption ? SHORT_OPTION : null, LONG_OPTION, ALIAS, "Reports the total number of matches." );
    }

    /**
     * Constructs the predefined verbose {@link Flag}.
     *
     * @param aDescription The description to be used (without any line breaks).
     */
    public CountFlag(String aDescription ) {
        this( aDescription, true );
    }

    /**
     * Constructs the predefined clean {@link Flag}.
     *
     * @param aDescription The description to be used (without any line breaks).
     * @param hasShortOption True in case to also enable the short option, else
     *        only the long option takes effect.
     */
    public CountFlag(String aDescription, boolean hasShortOption ) {
        super( hasShortOption ? SHORT_OPTION : null, LONG_OPTION, ALIAS, aDescription );
    }
}
