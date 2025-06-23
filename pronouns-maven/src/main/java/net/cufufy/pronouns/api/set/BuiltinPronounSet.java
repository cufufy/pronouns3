package net.cufufy.pronouns.api.set;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static net.cufufy.pronouns.api.util.StringUtils.capitalize;

/**
 * A built-in pronoun set.
 */
record BuiltinPronounSet(
        @NotNull String subjective,
        @NotNull String objective,
        @NotNull String possessiveAdj,
        @NotNull String possessive,
        @NotNull String reflexive,
        boolean plural
) implements PronounSet {
    @Override
    public String toString() {
        return capitalize(subjective()) + "/" + capitalize(objective());
    }

    // Removed toFullString override to use the default PronounSet interface implementation
}
