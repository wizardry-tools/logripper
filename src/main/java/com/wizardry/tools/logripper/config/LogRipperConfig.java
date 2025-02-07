package com.wizardry.tools.logripper.config;

/**
 * This class is a record for the LogRipper Configuration.
 *
 *
 * Features:
 *  1. Search for a token or pattern in a file composed of text.
 *  2. Split a file composed of text into multiple files
 *
 *
 * @param searchToken The Search token or pattern that will be used to find matches.
 * @param path The path to the File within the system.
 * @param linesBeforeMatch The number of additional lines to include before a match result.
 * @param linesAfterMatch The number of additional lines to include after a match result.
 * @param isIgnoreCase The flag that indicates if matches should be case-insensitive.
 * @param matchLimit The number of matches to find before satisfaction.
 * @param isSilent The flag that indicates if matches should not report their values.
 */
public record LogRipperConfig(
        String searchToken,
        String path,
        boolean isDir,
        int linesBeforeMatch,
        int linesAfterMatch,
        boolean isIgnoreCase,
        int matchLimit,
        boolean isSilent,
        boolean isCountOnly,
        boolean isNumbered
) {
    
    public LogRipperConfig(String searchToken, String path, boolean isDir, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit, boolean isSilent, boolean isCountOnly) {
        this( searchToken, path, isDir, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, isSilent, isCountOnly, false); // Default to false so that line numbers do not output.
    }

    public LogRipperConfig(String searchToken, String path, boolean isDir, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit, boolean isSilent) {
        this( searchToken, path, isDir, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, isSilent, false); // Default to false so that matches output.
    }

    public LogRipperConfig(String searchToken, String path, boolean isDir, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit) {
        this( searchToken, path, isDir, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, false); // Default to false so that matches output their values.
    }

    public LogRipperConfig(String searchToken, String path, boolean isDir, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase) {
        this( searchToken, path, isDir, linesBeforeMatch, linesAfterMatch, isIgnoreCase, 0); // Default to 0 matches if not specified.
    }
    public LogRipperConfig(String searchToken, String path, boolean isDir, int linesBeforeMatch, int linesAfterMatch) {
        this( searchToken, path, isDir, linesBeforeMatch, linesAfterMatch, false); // Default false for case-insensitive
    }

    public LogRipperConfig(String searchToken, String path, boolean isDir) {
        this(searchToken, path, isDir, 0, 0); // Default to no additional lines before or after match.
    }

    public LogRipperConfig(String searchToken, String path) {
        this(searchToken, path, false); // Default treating the path as a file
    }
    /**
     * Validates the configuration settings.
     *
     * @throws IllegalArgumentException if any of the validation checks fail.
     */
    public void validate() throws IllegalArgumentException {
        if (searchToken == null || searchToken.isEmpty()) {
            throw new IllegalArgumentException("Search token cannot be null or empty.");
        }

        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }

        if (linesBeforeMatch < 0) {
            throw new IllegalArgumentException("Lines before match must be non-negative.");
        }

        if (linesAfterMatch < 0) {
            throw new IllegalArgumentException("Lines after match must be non-negative.");
        }

        if (matchLimit < 0) {
            throw new IllegalArgumentException("Match Limit must be non-negative. Use '0' for no limit or remove the flag.");
        }
    }
}
