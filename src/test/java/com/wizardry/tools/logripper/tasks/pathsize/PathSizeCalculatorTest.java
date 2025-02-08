package com.wizardry.tools.logripper.tasks.pathsize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test for the PathSizeCalculator
 * For some reason, the mocked statics aren't working correctly.
 * Using real files to test functionality.
 */
public class PathSizeCalculatorTest {

    private PathSizeCalculator pathSizeCalculator;

    @BeforeEach
    public void setUp() {
        pathSizeCalculator = new PathSizeCalculator();
    }

    @Test
    public void testCalculatePathSize_EmptyDirectory() throws IOException {
        Path path = Paths.get("./src/test/resources/mocks/data/empty-dir");
//        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
//            filesMock.when(() -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)).thenReturn(true);
//            filesMock.when(() -> Files.walkFileTree(any(Path.class), any(SimpleFileVisitor.class))).thenAnswer(invocation -> {
//                SimpleFileVisitor<Path> visitor = invocation.getArgument(1);
//                visitor.preVisitDirectory(path, mock(BasicFileAttributes.class));
//                visitor.postVisitDirectory(path, null);
//                return FileVisitResult.CONTINUE;
//            });
//
//            long size = pathSizeCalculator.calculatePathSize(path, false);
//
//            assertEquals(0L, size);
//        }
        long size = pathSizeCalculator.rip(path, true);
        assertEquals(0L, size);
    }

    @Test
    public void testCalculatePathSize_SingleFile() throws IOException {
        Path path = Paths.get("./src/test/resources/mocks/data/mock.log");
//        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
//            filesMock.when(() -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)).thenReturn(false);
//            filesMock.when(() -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)).thenReturn(true);
//            filesMock.when(() -> Files.size(path)).thenReturn(1024L);
//
//            long size = pathSizeCalculator.calculatePathSize(path, false);
//
//            assertEquals(1024L, size);
//        }
        long size = pathSizeCalculator.rip(path, true);
        assertEquals(1510L, size);
    }

    @Test
    public void testCalculatePathSize_NestedDirectories() throws IOException {
        Path rootPath = Paths.get("./src/test/resources/mocks");
        Path subDirPath = rootPath.resolve("data");
        Path file1Path = subDirPath.resolve("mock.json");
        Path file2Path = subDirPath.resolve("mock.log");

//        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
//            filesMock.when(() -> Files.isDirectory(rootPath, LinkOption.NOFOLLOW_LINKS)).thenReturn(true);
//            filesMock.when(() -> Files.walkFileTree(any(Path.class), any(SimpleFileVisitor.class))).thenAnswer(invocation -> {
//                SimpleFileVisitor<Path> visitor = invocation.getArgument(1);
//                visitor.preVisitDirectory(rootPath, mock(BasicFileAttributes.class));
//                visitor.preVisitDirectory(subDirPath, mock(BasicFileAttributes.class));
//                visitor.visitFile(file1Path, mock(BasicFileAttributes.class));
//                visitor.visitFile(file2Path, mock(BasicFileAttributes.class));
//                visitor.postVisitDirectory(subDirPath, null);
//                visitor.postVisitDirectory(rootPath, null);
//                return FileVisitResult.CONTINUE;
//            });
//            filesMock.when(() -> Files.size(file1Path)).thenReturn(512L);
//            filesMock.when(() -> Files.size(file2Path)).thenReturn(768L);
//
//            long size = pathSizeCalculator.calculatePathSize(rootPath, false);
//
//            assertEquals(1280L, size);
//        }
        long size = pathSizeCalculator.rip(rootPath, true);
        assertEquals(7380L, size);
    }
}
