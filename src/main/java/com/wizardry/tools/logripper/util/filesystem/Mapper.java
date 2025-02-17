package com.wizardry.tools.logripper.util.filesystem;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface Mapper<T extends Mappable<T>,R> {
    R map(T mappable) throws InterruptedException, ExecutionException, IOException;
}
