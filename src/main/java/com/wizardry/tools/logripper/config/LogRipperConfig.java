package com.wizardry.tools.logripper.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class is a record for the LogRipper Configuration.
 *
 * @param searchToken The Search token or pattern that will be used to find matches.
 * @param path The path to the File or Directory within the system.
 * @param linesBeforeMatch The number of additional lines to include before a match result.
 * @param linesAfterMatch The number of additional lines to include after a match result.
 * @param isIgnoreCase The flag that indicates if matches should be case-insensitive.
 * @param matchLimit The number of matches to find before satisfaction.
 * @param isSilent The flag that indicates if matches should not report their values.
 * @param isCountOnly The flag that indicates if the operation should only count matches without reporting them.
 * @param isNumbered The flag that indicates if each line in the output should be numbered.
 * @param isVerbose The flag that indicates if verbose logging should be enabled.
 * @param isDebug The flag that indicates if debug information should be logged.
 */
public record LogRipperConfig(
        String searchToken,
        Path path,
        int linesBeforeMatch,
        int linesAfterMatch,
        boolean isIgnoreCase,
        int matchLimit,
        boolean isSilent,
        boolean isCountOnly,
        boolean isNumbered,
        boolean isVerbose,
        boolean isDebug
) {

    public LogRipperConfig(String searchToken, Path path, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit, boolean isSilent, boolean isCountOnly, boolean isNumbered, boolean isVerbose) {
        this( searchToken, path, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, isSilent, isCountOnly, isNumbered, isVerbose, false); // Default to false
    }

    public LogRipperConfig(String searchToken, Path path, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit, boolean isSilent, boolean isCountOnly, boolean isNumbered) {
        this( searchToken, path, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, isSilent, isCountOnly, isNumbered, false); // Default to false
    }

    public LogRipperConfig(String searchToken, Path path, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit, boolean isSilent, boolean isCountOnly) {
        this( searchToken, path, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, isSilent, isCountOnly, false); // Default to false so that line numbers do not output.
    }

    public LogRipperConfig(String searchToken, Path path, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit, boolean isSilent) {
        this( searchToken, path, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, isSilent, false); // Default to false so that matches output.
    }

    public LogRipperConfig(String searchToken, Path path, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase, int matchLimit) {
        this( searchToken, path, linesBeforeMatch, linesAfterMatch, isIgnoreCase, matchLimit, false); // Default to false so that matches output their values.
    }

    public LogRipperConfig(String searchToken, Path path, int linesBeforeMatch, int linesAfterMatch, boolean isIgnoreCase) {
        this( searchToken, path, linesBeforeMatch, linesAfterMatch, isIgnoreCase, 0); // Default to 0 matches if not specified.
    }
    public LogRipperConfig(String searchToken, Path path, int linesBeforeMatch, int linesAfterMatch) {
        this( searchToken, path, linesBeforeMatch, linesAfterMatch, false); // Default false for case-insensitive
    }

    public LogRipperConfig(String searchToken, Path path) {
        this(searchToken, path, 0, 0); // Default to no additional lines before or after match.
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

        if (path == null) {
            throw new IllegalArgumentException("File path cannot be null");
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

    public Pattern getTokenPattern() {
        return isIgnoreCase ? Pattern.compile(searchToken, Pattern.CASE_INSENSITIVE) : Pattern.compile(searchToken);
    }

    public List<String> getPaths() {
        return collectPaths(path.toString());
    }

    private static List<String> collectPaths(String directoryPath) {
        List<String> paths = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(Paths.get(directoryPath)).parallel()) {
            stream.filter(path ->
                Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
            ).forEach(path ->
                paths.add(path.toString())
            );
        } catch (IOException e) {
            System.err.println("Error traversing the directory: " + e.getMessage());
        }
        return paths;
    }
}
