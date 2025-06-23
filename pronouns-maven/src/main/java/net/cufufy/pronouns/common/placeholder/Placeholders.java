package net.cufufy.pronouns.common.placeholder;

import net.cufufy.pronouns.api.Conjugator;
import net.cufufy.pronouns.api.set.PronounSet;
import net.cufufy.pronouns.common.ProNouns;
import net.cufufy.pronouns.common.placeholder.Placeholder.Result;
import net.cufufy.pronouns.common.util.EnumUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Placeholders {

    @FunctionalInterface
    private interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    private static class ParsedArgs {
        final List<String> coreArgs = new ArrayList<>();
        Optional<Integer> index = Optional.empty();
        final List<String> modifierStrings = new ArrayList<>();
        boolean indexExplicitlyPresent = false;

        ParsedArgs(String fullArgString) {
            String[] parts = fullArgString.split("[_ ]");
            Set<String> knownModifiers = Set.of("uppercase", "lowercase", "capital", "nounset");
            boolean foundIndexInThisPass = false;

            for (String part : parts) {
                if (part.isEmpty()) continue;

                if (!foundIndexInThisPass) {
                    try {
                        this.index = Optional.of(Integer.parseInt(part));
                        this.indexExplicitlyPresent = true;
                        foundIndexInThisPass = true;
                        continue;
                    } catch (NumberFormatException e) {
                        // Not an index
                    }
                }

                if (knownModifiers.contains(part.toLowerCase(Locale.ROOT))) {
                    this.modifierStrings.add(part.toLowerCase(Locale.ROOT));
                } else {
                    this.coreArgs.add(part);
                }
            }
        }
    }

    private static String applyModifiers(PronounSet selectedSet, String value, List<String> modifierStrings) {
        var out = value;

        // Handle nounset first: if no set is selected (e.g. invalid index) and nounset is a modifier, result is empty.
        // Also, if selectedSet is null because user has no pronouns, nounset should also make it empty.
        if (modifierStrings.contains("nounset")) {
            if (selectedSet == null || selectedSet.equals(PronounSet.Builtins.UNSET)) {
                return "";
            }
        }

        // If no set was successfully selected (e.g., invalid index) and we already returned for nounset,
        // other modifiers should not apply to an empty string that indicates an error.
        // However, if selectedSet is null because the user genuinely has no pronouns (userAllSets was empty),
        // then `value` would likely be "" already from the placeholder logic.
        if (selectedSet == null && value.isEmpty()) { // Typically means invalid index caused placeholder to return ""
             return ""; // Don't apply further modifiers like uppercase to an error indicator.
        }


        for (final String mod : modifierStrings) {
            out = switch (mod) {
                case "uppercase" -> out.toUpperCase(Locale.ROOT);
                case "lowercase" -> out.toLowerCase(Locale.ROOT);
                case "capital" -> out.isEmpty() ? "" :
                        (out.length() == 1 ? out.toUpperCase(Locale.ROOT) :
                                out.substring(0, 1).toUpperCase(Locale.ROOT) + out.substring(1).toLowerCase(Locale.ROOT));
                case "nounset" -> { /* Already handled */ yield out; }
                default -> out;
            };
        }
        return out;
    }

    private final ProNouns plugin;

    // Updated placeholder definitions using the new forPronoun structure

    public final Placeholder subjective = forPronoun("subjective", PronounSet::subjective);
    public final Placeholder objective = forPronoun("objective", PronounSet::objective);
    public final Placeholder possessiveAdj = forPronoun("possessiveadj", PronounSet::possessiveAdj);
    public final Placeholder possessive = forPronoun("possessive", PronounSet::possessive);
    public final Placeholder reflexive = forPronoun("reflexive", PronounSet::reflexive);

    public final Placeholder pronouns = forPronoun("pronouns", (userAllSets, parsedArgs, selectedSet) -> {
        if (userAllSets.isEmpty()) return Result.of("");
        return Result.of(PronounSet.format(userAllSets));
    });

    public final Placeholder all = forPronoun("all", (userAllSets, parsedArgs, selectedSet) -> {
        if (userAllSets.isEmpty()) return Result.of("");
        return Result.of(
                userAllSets.stream()
                        .map(PronounSet::toFullString)
                        .collect(Collectors.joining("; "))
        );
    });

    public final Placeholder verb = forPronoun("verb", (userAllSets, parsedArgs, selectedSet) -> {
        if (selectedSet == null) return Result.of(""); // Invalid index or no pronouns for user
        if (parsedArgs.coreArgs.isEmpty()) return Result.fail("Missing verb type for %pronouns_verb%");
        String verbName = parsedArgs.coreArgs.get(0);
        final var verbEnum = EnumUtil.getByName(Conjugator.class, verbName);
        return verbEnum.map(conjugator -> Result.of(conjugator.conjugate(selectedSet.plural())))
                .orElseGet(() -> Result.fail("Unknown verb " + verbName + " for %pronouns_verb%"));
    });

    public final Placeholder conjugate = forPronoun("conj", (userAllSets, parsedArgs, selectedSet) -> {
        if (selectedSet == null) return Result.of(""); // Invalid index or no pronouns for user
        if (parsedArgs.coreArgs.size() < 2) return Result.fail("Missing options for %pronouns_conj%");
        return Result.of(parsedArgs.coreArgs.get(selectedSet.plural() ? 1 : 0));
    });


    private Placeholder forPronoun(String name, TriFunction<List<PronounSet>, ParsedArgs, PronounSet, Result> valueProducer) {
        return new Placeholder(name, (sender, s_args) -> {
            if (sender.uuid().isEmpty()) return Result.fail("No player");
            final List<PronounSet> userAllSets = plugin.store().sets(sender.uuid().get());
            final ParsedArgs parsedArgs = new ParsedArgs(s_args);

            PronounSet selectedSet = null;
            boolean selectionFailedDueToInvalidIndex = false;

            if (userAllSets.isEmpty()) {
                if (parsedArgs.indexExplicitlyPresent) {
                    selectionFailedDueToInvalidIndex = true;
                }
                // selectedSet remains null
            } else {
                if (parsedArgs.index.isPresent()) {
                    int idx = parsedArgs.index.get();
                    if (idx > 0 && idx <= userAllSets.size()) {
                        selectedSet = userAllSets.get(idx - 1);
                    } else {
                        selectionFailedDueToInvalidIndex = true;
                        // selectedSet remains null
                    }
                } else {
                    selectedSet = userAllSets.get(0); // Default to first if no index
                }
            }

            if (selectionFailedDueToInvalidIndex) {
                // If an explicit, but invalid, index was given, return empty string after applying modifiers.
                // This allows "nounset" to still make it empty, or other modifiers if needed (though usually not on error).
                return Result.of(applyModifiers(null, "", parsedArgs.modifierStrings));
            }

            Result producedResult = valueProducer.apply(userAllSets, parsedArgs, selectedSet);

            if (producedResult.success()) {
                return Result.of(applyModifiers(selectedSet, producedResult.message(), parsedArgs.modifierStrings));
            }
            return producedResult; // Failures (e.g. unknown verb) are returned directly
        });
    }

    private Placeholder forPronoun(String name, Function<PronounSet, String> singleSetToString) {
        return forPronoun(name, (userAllSets, parsedArgs, selectedSet) -> {
            if (selectedSet == null) {
                // This case is hit if:
                // 1. User has no pronouns (userAllSets is empty) and no specific index was requested.
                // 2. An invalid index was specified (handled by selectionFailedDueToInvalidIndex in the calling forPronoun).
                //    In this case, the calling forPronoun already returned an empty modified string.
                //    So, if we reach here and selectedSet is null, it's typically case 1.
                // For single form placeholders, if no set is applicable (either no pronouns, or invalid index already handled),
                // outputting empty string is appropriate.
                return Result.of("");
            }
            return Result.of(singleSetToString.apply(selectedSet));
        });
    }


    public Placeholders(ProNouns plugin) {
        this.plugin = plugin;
    }

    public Set<Placeholder> placeholders() {
        return Set.of(subjective, objective, possessiveAdj, possessive, reflexive, pronouns, all, verb, conjugate);
    }
}
