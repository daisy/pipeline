package org.daisy.dotify.translator.impl;

import org.daisy.dotify.api.translator.ResolvableText;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO: write java doc.
 */
public class StaticResolvable implements ResolvableText {
    private final String v;

    public StaticResolvable(String v) {
        this.v = v;
    }

    public static List<ResolvableText> with(String... values) {
        return with(Arrays.asList(values));
    }

    public static List<ResolvableText> with(List<String> values) {
        return values.stream()
                .map(StaticResolvable::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public String peek() {
        return v;
    }

    @Override
    public String resolve() {
        return v;
    }

    @Override
    public Optional<String> getLocale() {
        return Optional.empty();
    }

    @Override
    public boolean shouldHyphenate() {
        return false;
    }

    @Override
    public boolean shouldMarkCapitalLetters() {
        return true;
    }
}
