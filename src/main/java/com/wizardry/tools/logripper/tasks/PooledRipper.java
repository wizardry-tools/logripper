package com.wizardry.tools.logripper.tasks;

import java.io.IOException;

public interface PooledRipper<T,U> {

    U rip(T toRip, boolean isDebug) throws IOException;

}
