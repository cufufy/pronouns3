package net.lucypoulton.pronouns.api;

import net.lucypoulton.pronouns.api.set.PronounSet;
import net.lucypoulton.pronouns.api.set.SpecialPronounSet;
import net.lucypoulton.pronouns.api.supplier.PronounSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
// It might be useful to have a logger, but for now, unparseable parts will be silently ignored or return empty.
// import java.util.logging.Logger;

public class PronounParser {
    // private static final Logger LOGGER = Logger.getLogger(PronounParser.class.getName());
	private final PronounSupplier predefinedSupplier;

	public PronounParser(final PronounSupplier predefinedSupplier) {
		this.predefinedSupplier = predefinedSupplier;
	}

	public @NotNull List<PronounSet> parse(@NotNull String input) {
		if (input.trim().isEmpty()) {
			return Collections.emptyList();
		}

		final List<PronounSet> parsedSets = new ArrayList<>();
		final String[] parts = input.split(";");
		final List<PronounSet> predefinedSets = predefinedSupplier.get();

		for (String part : parts) {
			String trimmedPart = part.trim();
			if (trimmedPart.isEmpty()) {
				continue;
			}

			Optional<PronounSet> foundSet = Optional.empty();

            // 1. Check for exact match with predefinedSet.toString() (e.g. "He/Him", "Any")
            // or predefinedSet.toFullString() (e.g. "he/him/his/his/himself")
            // or predefinedSet.subjective() (e.g. "he")
            for (PronounSet predef : predefinedSets) {
                if (predef.toString().equalsIgnoreCase(trimmedPart) ||
                    predef.toFullString().equalsIgnoreCase(trimmedPart) ||
                    predef.subjective().equalsIgnoreCase(trimmedPart)) {
                    foundSet = Optional.of(predef);
                    break;
                }
                // For SpecialPronounSet, toString is usually the main identifier (e.g., "Any")
                // For other sets, also check simple subjective if not already caught by above
                 if (!(predef instanceof SpecialPronounSet) && predef.subjective().equalsIgnoreCase(trimmedPart)) {
                    foundSet = Optional.of(predef);
                    break;
                }
            }

			if (foundSet.isPresent()) {
				parsedSets.add(foundSet.get());
				continue;
			}

			// 2. Try parsing as custom s/o/pa/p/r(:p)
			// This regex ensures 5 parts, with the last optionally having :p
            // String[] customParts = trimmedPart.split("/"); // Original
            // More robust splitting that doesn't create empty strings if input is like "a//b"
            String[] customParts = trimmedPart.split("/", -1);


			if (customParts.length == 5) {
				try {
					String subjective = customParts[0].trim();
					String objective = customParts[1].trim();
					String possessiveAdj = customParts[2].trim();
					String possessive = customParts[3].trim();
					String reflexivePart = customParts[4].trim();
					boolean isPlural = false;

					if (reflexivePart.toLowerCase().endsWith(":p")) {
						isPlural = true;
						reflexivePart = reflexivePart.substring(0, reflexivePart.length() - 2).trim();
					}

					if (subjective.isEmpty() || objective.isEmpty() || possessiveAdj.isEmpty() || possessive.isEmpty() || reflexivePart.isEmpty()) {
                        // LOGGER.warning("Skipping malformed custom pronoun part (empty components): " + trimmedPart);
						continue;
					}
                    // Ensure that a custom definition doesn't accidentally match a predefined one's partial forms
                    // e.g. custom "any/else/goes/here/now" shouldn't be parsed if "any" was meant for Builtins.ANY
                    // This is implicitly handled because predefined check is done first.
					parsedSets.add(PronounSet.from(subjective, objective, possessiveAdj, possessive, reflexivePart, isPlural));
				} catch (Exception e) {
                    // LOGGER.warning("Failed to parse custom pronoun part: " + trimmedPart + " due to " + e.getMessage());
				}
				continue; // whether successful or failed custom parse, move to next part
			}

            // 3. If not custom format, and not already matched, check if the trimmedPart (single token)
            // matches any form of a predefined set (e.g. "him", "herself")
            // This is for cases like "she;him"
            if (!trimmedPart.contains("/")) { // Only if it's a single word
                for (PronounSet predef : predefinedSets) {
                    if (predef.includesPronoun(trimmedPart)) {
                        foundSet = Optional.of(predef);
                        break;
                    }
                }
                if (foundSet.isPresent()) {
                    parsedSets.add(foundSet.get());
                    continue;
                }
            }
            // If we reach here, the part is unparseable
            // LOGGER.info("Skipping unparseable pronoun part: " + trimmedPart);
		}
        // Return a list with unique pronouns, preserving order of first appearance
		return List.copyOf(new LinkedHashSet<>(parsedSets));
	}

	public String toString(@NotNull List<PronounSet> pronounSets) {
		return pronounSets.stream()
				.map(PronounSet::toFullString)
				.collect(Collectors.joining(";"));
	}
}
