package com.wizardry.tools.logripper.functions;

import com.wizardry.tools.logripper.config.LogRipperConfig;

import java.util.List;
import java.util.Map;

import static com.wizardry.tools.logripper.util.StringUtil.EMPTY;


public class MatchConsumer implements TriConsumer<List<String>, Map<Integer, String>, Integer> {

    private final LogRipperConfig config;

    public MatchConsumer(LogRipperConfig config) {
        this.config = config;
    }

    @Override
    public void accept(List<String> lines, Map<Integer, String> matches, Integer index) {
        if (config.isSilent()) {
            matches.put(index, EMPTY);
            return;
        }
        StringBuilder matchDetails = new StringBuilder();
        int start = Math.max(0, index - config.linesBeforeMatch());
        int end = Math.min(lines.size() - 1, index + config.linesAfterMatch());
        for (int j = start; j <= end; j++) {
            if (j == index) {
                matchDetails.append("> ").append(lines.get(j)).append("\n");
            } else {
                matchDetails.append("  ").append(lines.get(j)).append("\n");
            }
        }
        matches.put(index, matchDetails.toString());
    }

    @Override
    public TriConsumer<List<String>, Map<Integer, String>, Integer> andThen(TriConsumer<? super List<String>, ? super Map<Integer, String>, ? super Integer> after) {
        return TriConsumer.super.andThen(after);
    }
}
