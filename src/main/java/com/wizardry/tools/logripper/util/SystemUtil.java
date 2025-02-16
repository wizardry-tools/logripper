package com.wizardry.tools.logripper.util;

import com.wizardry.tools.logripper.config.LogRipperConfig;
import com.wizardry.tools.logripper.util.functions.LineReader;
import com.wizardry.tools.logripper.util.matching.Match;
import org.refcodes.logger.RuntimeLogger;
import org.refcodes.logger.RuntimeLoggerFactorySingleton;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public final class SystemUtil {

    private static final RuntimeLogger LOGGER = RuntimeLoggerFactorySingleton.createRuntimeLogger();

    private SystemUtil() {
        //private constructor
    }

    public static int calculateOptimalPartSize(long fileSize) {
        // Simple heuristic
        long partSize = (fileSize / (Runtime.getRuntime().availableProcessors() * 2L)) + 1L;
        if (partSize < Integer.MAX_VALUE) {
            return (int) partSize;
        }
        return Integer.MAX_VALUE;
    }

    public static List<String> readFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().toList();
        }
    }

    /**
     * Read all lines from a file as a {@code Stream}. Unlike {@link
     * Files#readAllLines(Path, Charset) readAllLines}, this method does not read
     * all lines into a {@code List}, but instead populates lazily as the stream
     * is consumed.
     *
     * <p> Bytes from the file are decoded into characters using the specified
     * charset and the same line terminators as specified by {@code
     * readAllLines} are supported.
     *
     * <p> The returned stream contains a reference to an open file. The file
     * is closed by closing the stream.
     *
     * <p> The file contents should not be modified during the execution of the
     * terminal stream operation. Otherwise, the result of the terminal stream
     * operation is undefined.
     *
     * <p> After this method returns, then any subsequent I/O exception that
     * occurs while reading from the file or when a malformed or unmappable byte
     * sequence is read, is wrapped in an {@link UncheckedIOException} that will
     * be thrown from the
     * {@link java.util.stream.Stream} method that caused the read to take
     * place. In case an {@code IOException} is thrown when closing the file,
     * it is also wrapped as an {@code UncheckedIOException}.
     *
     * @apiNote
     * This method must be used within a try-with-resources statement or similar
     * control structure to ensure that the stream's open file is closed promptly
     * after the stream's operations have completed.
     *
     * @implNote
     * This implementation supports good parallel stream performance for the
     * standard charsets {@link StandardCharsets#UTF_8 UTF-8},
     * {@link StandardCharsets#US_ASCII US-ASCII} and
     * {@link StandardCharsets#ISO_8859_1 ISO-8859-1}.  Such
     * <em>line-optimal</em> charsets have the property that the encoded bytes
     * of a line feed ('\n') or a carriage return ('\r') are efficiently
     * identifiable from other encoded characters when randomly accessing the
     * bytes of the file.
     *
     * <p> For non-<em>line-optimal</em> charsets the stream source's
     * spliterator has poor splitting properties, similar to that of a
     * spliterator associated with an iterator or that associated with a stream
     * returned from {@link BufferedReader#lines()}.  Poor splitting properties
     * can result in poor parallel stream performance.
     *
     * <p> For <em>line-optimal</em> charsets the stream source's spliterator
     * has good splitting properties, assuming the file contains a regular
     * sequence of lines.  Good splitting properties can result in good parallel
     * stream performance.  The spliterator for a <em>line-optimal</em> charset
     * takes advantage of the charset properties (a line feed or a carriage
     * return being efficient identifiable) such that when splitting it can
     * approximately divide the number of covered lines in half.
     *
     * @param   path
     *          the path to the file
     * @param   cs
     *          the charset to use for decoding
     *
     * @return  the lines from the file as a {@code Stream}
     *
     * @throws  IOException
     *          if an I/O error occurs opening the file
     * @throws  SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     *
     * @see     Files#readAllLines(Path, Charset)
     * @see     Files#newBufferedReader(Path, Charset)
     * @see     java.io.BufferedReader#lines()
     * @since   1.8
     */
    public static Stream<String> lines(Path path, Charset cs) throws IOException {
        return Files.lines(path, StandardCharsets.UTF_8);
    }

    public static ConcurrentLinkedQueue<Match> randomRead(Path path, LogRipperConfig config) {
        ConcurrentLinkedQueue<Match> matches = new ConcurrentLinkedQueue<>();
        if (!Files.isReadable(path)) {
            LOGGER.error("File not Readable: " + path.toAbsolutePath());
            return matches;
        }
        AtomicInteger totalLines = new AtomicInteger(0);
        AtomicInteger totalMatches = new AtomicInteger(0);

        LineReader lineReader = LineReader.of(config, totalMatches);
        try (RandomAccessFile aFile = new RandomAccessFile(path.toFile(), "r");
             FileChannel inChannel = aFile.getChannel();) {

            //Buffer size is 1024
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuilder sb = new StringBuilder();
            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                char c;
                for (int i = 0; i < buffer.limit(); i++) {
                    if (buffer.hasRemaining()) {
                        c = (char) buffer.get();
                        if (c != '\n') {
                            sb.append(c);
                        } else {
                            // end of line
                            String line = sb.toString();
                            boolean matched = lineReader.apply(line, totalLines.incrementAndGet(), matches);
                            sb = new StringBuilder();
                        }
                    }
                }
                buffer.clear(); // do something with the data and clear/compact it.
            }
        } catch (IOException e) {
            System.out.println("IOException occurred: " + e.getMessage());
        }

        return matches;
    }




}
