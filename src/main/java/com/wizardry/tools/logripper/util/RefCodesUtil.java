package com.wizardry.tools.logripper.util;

import org.refcodes.cli.StringOption;
import org.refcodes.properties.ext.application.ApplicationProperties;

import java.util.Optional;
import java.util.function.Predicate;

public final class RefCodesUtil {

    public static int parseIntegerOption(ApplicationProperties properties, StringOption option, int defaultValue) {
        return Optional.ofNullable(properties.getOr(option, null))
                .filter(Predicate.not(String::isBlank))
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }
}
