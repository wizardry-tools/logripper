package com.wizardry.tools.logripper.tasks.pathmapper;

import com.wizardry.tools.logripper.util.filesystem.Crawlable;
import com.wizardry.tools.logripper.util.filesystem.Readable;
import com.wizardry.tools.logripper.util.filesystem.Sizable;
import com.wizardry.tools.logripper.util.functions.LineReader;
import com.wizardry.tools.logripper.util.matching.Match;
import com.wizardry.tools.logripper.util.printing.Printable;

public interface FileTreeNode<K,T extends Crawlable<T>> extends Readable<LineReader, Match>,Sizable,Printable, Crawlable<T> {
    K getPath();
    String getName();
    void display(int level);
    void sortChildrenBySize();
}
