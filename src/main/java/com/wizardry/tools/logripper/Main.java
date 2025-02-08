// /////////////////////////////////////////////////////////////////////////////
// REFCODES.ORG
// /////////////////////////////////////////////////////////////////////////////
// This code is written and provided by Siegfried Steiner, Munich, Germany.
// Feel free to use it as skeleton for your own applications. Make sure you have
// considered the license conditions of the included artifacts (pom.xml).
// -----------------------------------------------------------------------------
// The REFCODES.ORG artifacts used by this template are copyright (c) by
// Siegfried Steiner, Munich, Germany and licensed under the following
// (see "http://en.wikipedia.org/wiki/Multi-licensing") licenses:
// -----------------------------------------------------------------------------
// GNU General Public License, v3.0 ("http://www.gnu.org/licenses/gpl-3.0.html")
// -----------------------------------------------------------------------------
// Apache License, v2.0 ("http://www.apache.org/licenses/LICENSE-2.0")
// -----------------------------------------------------------------------------
// Please contact the copyright holding author(s) of the software artifacts in
// question for licensing issues not being covered by the above listed licenses,
// also regarding commercial licensing models or regarding the compatibility
// with other open source licenses.
// /////////////////////////////////////////////////////////////////////////////

package com.wizardry.tools.logripper;

import static org.refcodes.cli.CliSugar.*;

import com.wizardry.tools.logripper.config.*;
import org.refcodes.archetype.CliHelper;
import org.refcodes.cli.*;
import org.refcodes.data.AsciiColorPalette;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;
import org.refcodes.properties.ext.application.ApplicationProperties;
import org.refcodes.textual.FontFamily;
import org.refcodes.textual.Font;
import org.refcodes.textual.FontStyle;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A minimum REFCODES.ORG enabled command line interface (CLI) application. Get
 * inspired by "https://bitbucket.org/funcodez".
 */
public class Main {

	// See "http://www.refcodes.org/blog/logging_like_the_nerds_log" |-->
	private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();
	// <--| See "http://www.refcodes.org/blog/logging_like_the_nerds_log"

	// /////////////////////////////////////////////////////////////////////////
	// CONSTANTS:
	// /////////////////////////////////////////////////////////////////////////

	private static final String NAME = "logripper";
	private static final String TITLE = ( NAME.lastIndexOf( '-' ) != -1 ? "<" + NAME.substring( NAME.lastIndexOf( '-' ) + 1 ) + ">" : NAME ).toUpperCase();
	private static final String DEFAULT_CONFIG = NAME + ".ini";
	private static final String DESCRIPTION = "A minimum REFCODES.ORG enabled command line interface (CLI) application. Get inspired by [https://bitbucket.org/funcodez].";
	private static final String LICENSE_NOTE = "Licensed under GNU General Public License, v3.0 and Apache License, v2.0";
	private static final String COPYRIGHT = "Copyright (c) by CLUB.FUNCODES (see [https://www.funcodes.club])";
	private static final char[] BANNER_PALETTE = AsciiColorPalette.MAX_LEVEL_GRAY.getPalette();
	private static final Font BANNER_FONT = new Font( FontFamily.DIALOG, FontStyle.BOLD );
	private static final String GREP_PROPERTY = "grep";
	private static final String FILE_PROPERTY = "file";
	private static final String DIR_PROPERTY = "dir";
	private static final String LINES_PROPERTY = "lines";
	private static final String LINES_BEFORE_PROPERTY = "lines-before";
	private static final String LINES_AFTER_PROPERTY = "lines-after";
	private static final String LIMIT_PROPERTY = "limit";

	// /////////////////////////////////////////////////////////////////////////
	// METHODS:
	// /////////////////////////////////////////////////////////////////////////

	public static void main( String args[] ) {

		// ---------------------------------------------------------------------
		// CLI:
		// ---------------------------------------------------------------------

		// See "http://www.refcodes.org/refcodes/refcodes-cli" |-->

		final StringOption theSearchOption = stringOption( 'g', "grep", GREP_PROPERTY, "GREP the log file for a token or pattern." );
		final StringOption theFileOption = stringOption( 'f', "file", FILE_PROPERTY, "The log file that needs a rip'n" );
		final StringOption theDirOption = stringOption( 'd', "dir", DIR_PROPERTY, "The log directory that needs a rip'n" );
		final StringOption theLinesOption = stringOption( 'C', "lines-around", LINES_PROPERTY, "The number of lines before and after a match that should be included." );
		final StringOption theLinesBeforeOption = stringOption( 'B', "lines-before", LINES_BEFORE_PROPERTY, "The number of lines before a match that should be included." );
		final StringOption theLinesAfterOption = stringOption( 'A', "lines-after", LINES_AFTER_PROPERTY, "The number of lines after a match that should be included." );
		final StringOption theLimitOption = stringOption( 'L', "limit", LIMIT_PROPERTY, "The amount of matches to record before stopping early." );
		final ConfigOption theConfigOption = configOption();
		final Flag theInitFlag = initFlag();
		final Flag theVerboseFlag = verboseFlag();
		final Flag theSysInfoFlag = sysInfoFlag();
		final Flag theHelpFlag = helpFlag();
		final Flag theDebugFlag = debugFlag();
		final Flag theIgnoreCaseFlag = ignoreCaseFlag();
		final Flag theSilentFlag = silentFlag();
		final Flag theCountFlag = countFlag();
		final Flag theNumberFlag = numberFlag();

		// @formatter:off
		final Term theArgsSyntax = cases(
			and( theInitFlag, optional( theConfigOption, theVerboseFlag, theDebugFlag ) ),
			and( theSearchOption, xor( theFileOption, theDirOption ), optional(
					xor(theLinesOption, optional( theLinesBeforeOption, theLinesAfterOption, theCountFlag ) ),
					theIgnoreCaseFlag, theVerboseFlag, theDebugFlag, theSilentFlag, theNumberFlag, theLimitOption )
			),
			xor( theHelpFlag, and( theSysInfoFlag, any ( theVerboseFlag ) ) )
		);
		final Example[] theExamples = examples(
			example( "Grep a file", theSearchOption, theFileOption),
			example( "Grep a folder", theSearchOption, theDirOption),
			example( "Grep a file and ignore case", theSearchOption, theIgnoreCaseFlag, theFileOption),
			example( "Grep a file and include #n lines surrounding matches", theSearchOption, theLinesOption, theFileOption),
			example( "Grep a file and include #n lines before matches", theSearchOption, theLinesBeforeOption, theFileOption),
			example( "Grep a file and include #n lines after matches", theSearchOption, theLinesAfterOption, theFileOption),
			example( "Grep a file and only count total matches", theSearchOption, theFileOption, theCountFlag),
			example( "Grep a file and include line numbers with matches", theSearchOption, theFileOption, theNumberFlag),
			example( "Grep a file and silence the matches", theSearchOption, theFileOption, theSilentFlag),
			example( "Grep a file, print stack trace upon failure", theSearchOption, theFileOption, theDebugFlag),
			example( "Load specific config file", theConfigOption),
			example( "Initialize default config file", theInitFlag, theVerboseFlag),
			example( "Initialize specific config file", theConfigOption, theInitFlag, theVerboseFlag),
			example( "To show the help text", theHelpFlag ),
			example( "To print the system info", theSysInfoFlag )
		);
		final CliHelper theCliHelper = CliHelper.builder().
			withArgs( args ).
			// withArgs( args, ArgsFilter.D_XX ).
			withArgsSyntax( theArgsSyntax ).
			withExamples( theExamples ).
			withFilePath( DEFAULT_CONFIG ). // Must be the name of the default (template) configuration file below "/src/main/resources"
			withResourceClass( Main.class ).
			withName( NAME ).
			withTitle( TITLE ).
			withDescription( DESCRIPTION ).
			withLicense( LICENSE_NOTE ).
			withCopyright( COPYRIGHT ).
			withBannerFont( BANNER_FONT ).
			withBannerFontPalette( BANNER_PALETTE ).
			withLogger( LOGGER ).build();
		// @formatter:on

		final ApplicationProperties theArgsProperties = theCliHelper.getApplicationProperties();

		// <--| See "http://www.refcodes.org/refcodes/refcodes-cli"

		// ---------------------------------------------------------------------
		// MAIN:
		// ---------------------------------------------------------------------

		final boolean isVerbose = theCliHelper.isVerbose();
		final boolean isDebug = theArgsProperties.getBoolean( theDebugFlag );

		if ( isVerbose ) {
			LOGGER.info( "Starting application <" + NAME + "> ..." );
		}

		if ( isDebug ) {
			LOGGER.info( "Additional debug output enabled ..." );
		}

		try {
			if ( isVerbose ) {
				LOGGER.printSeparator();
				LOGGER.info( "Name: \"" + theArgsProperties.get( "application/name" ) + "\"" );
				LOGGER.info( "Company: \"" + theArgsProperties.get( "application/company" ) + "\"" );
				LOGGER.info( "Version: \"" + theArgsProperties.get( "application/version" ) + "\"" );
				LOGGER.printSeparator();
			}

			String theFile = theArgsProperties.getOr( theFileOption, "");
			boolean isDir = theFile.isEmpty();

			// handle file
			String theToken = theArgsProperties.getOr( theSearchOption, "");
			final boolean isIgnoreCase = theArgsProperties.getBoolean(theIgnoreCaseFlag);
			int linesBeforeCount = parseIntegerOption(theArgsProperties, theLinesBeforeOption, 0);
			int linesAfterCount = parseIntegerOption(theArgsProperties, theLinesAfterOption, 0);
			int linesCount = parseIntegerOption(theArgsProperties, theLinesOption, 0);
			int matchLimit = parseIntegerOption(theArgsProperties, theLimitOption, 0);
			if (linesCount != 0) {
				linesBeforeCount = linesCount;
				linesAfterCount = linesCount;
			}

			final boolean isSilent = theArgsProperties.getBoolean( theSilentFlag );
			final boolean isCountOnly = theArgsProperties.getBoolean(theCountFlag);
			final boolean isNumbered = theArgsProperties.getBoolean(theNumberFlag);

			if (isVerbose) {
				LOGGER.info("Rip'n file: \"" + theFile + "\"");
				if (!theToken.isBlank()) {
					LOGGER.info("GREP with token [" + theToken + "]");
				}
			}


			Instant startTime = Instant.now();
			LogRipperConfig config = new LogRipperConfig(
					theToken, theFile, isDir,
					linesBeforeCount, linesAfterCount,
					isIgnoreCase, matchLimit,
					isSilent, isCountOnly, isNumbered,
					isVerbose, isDebug);
			LogRipper logRipper = new LogRipper(config);
			logRipper.scanAndReport();

			Instant endTime = Instant.now();
			Duration duration = Duration.between(startTime, endTime);
			LOGGER.info("LogRipper execution took: " + duration.toMillis() + " milliseconds");


		}
		catch ( Exception e ) {
			theCliHelper.printException( e );
			System.exit( e.hashCode() % 0xFF );
		}
	}

	private static int parseIntegerOption(ApplicationProperties properties, StringOption option, int defaultValue) {
		return Optional.ofNullable(properties.getOr(option, null))
				.filter(Predicate.not(String::isBlank))
				.map(Integer::parseInt)
				.orElse(defaultValue);
	}

	private static IgnoreCaseFlag ignoreCaseFlag() {
		return new IgnoreCaseFlag(true);
	}

	private static SilentFlag silentFlag() {
		return new SilentFlag(true);
	}

	private static CountFlag countFlag() {
		return new CountFlag(true);
	}

	private static NumberFlag numberFlag() {
		return new NumberFlag(true);
	}
}
