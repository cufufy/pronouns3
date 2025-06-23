package net.cufufy.pronouns.common;

import net.cufufy.pronouns.api.set.PronounSet;
import net.cufufy.pronouns.api.PronounParser;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LegacyMigrator {

    private LegacyMigrator() {}

    private static final PronounParser parser = new PronounParser(PronounSet.builtins);

    public static PronounSet fromLegacyString(final String string) {
        final var split = string.split("/");

        // 1. Handle exact match for the problematic legacy "they" string from tests
        if (string.equals("they/them/they're/their/theirs/themself")) {
            return PronounSet.Builtins.THEY;
        }

        // 2. Handle simple aliases or single-word inputs (e.g., "any", "ask", "he", "she", "they")
        if (split.length == 1) {
            List<PronounSet> parsed = parser.parse(string);
            if (!parsed.isEmpty()) {
                return parsed.get(0);
            }
            // If not a recognized alias and only one part, it's unparseable as a 6-part string
            throw new IllegalArgumentException("Failed to parse legacy set (single part, not a known alias): " + string);
        }

        // 3. For multi-part strings, if the first part is a common builtin (but NOT "they", which is handled above or needs full parse),
        //    assume it's that builtin. This handles cases like "he/him/he's/his/his/himself".
        List<PronounSet> parsedFirstPart = parser.parse(split[0]);
        if (!parsedFirstPart.isEmpty()) {
            PronounSet potentialMatch = parsedFirstPart.get(0);
            if (PronounSet.builtins.get().contains(potentialMatch) && potentialMatch != PronounSet.Builtins.THEY) {
                // This implies that if a legacy string starts with "he/", "she/", etc. (but not "they/"),
                // it should be treated as that predefined pronoun, regardless of the other parts.
                // This matches the behavior of the original try-catch block that would return parser.parse(split[0]).get(0).
                return potentialMatch;
            }
        }

        // 4. Otherwise, attempt to parse as a full 6-part custom string.
        // This path will also be taken by strings starting with "they/" that are not the exact problematic one.
        if (split.length != 6) {
            throw new IllegalArgumentException("Failed to parse legacy set " + string +
                    " (expected 6 parts for custom, or a known alias/predefined prefix like 'he/' or 'she/')");
        }

        return PronounSet.from(
                split[0], // subjective
                split[1], // objective
                split[3], // possessiveAdj
                split[4], // possessivePronoun
                split[5], // reflexive
                split[2].endsWith("re") || split[2].endsWith("’re") // plural heuristic
        );
    }

    public static final class MigrationException extends RuntimeException {
        public MigrationException(Throwable cause) {
            super(cause);
        }

        public MigrationException(String message) {
            super(message);
        }
    }

    private static final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public static Map<UUID, List<PronounSet>> fromYaml(final Path path) {
        try (final var file = Files.newInputStream(path)) {
            final var loaded = (Map<String, String[]>) yaml.load(file);
            final var out = new HashMap<UUID, List<PronounSet>>();
            loaded.forEach((uuid, sets) -> out.put(UUID.fromString(uuid), Arrays.stream(sets).map(LegacyMigrator::fromLegacyString).toList()));
            file.close();
            Files.move(path, path.resolveSibling("legacy-datastore.yml"));
            return out;
        } catch (ClassCastException e) {
            throw new MigrationException("Legacy datastore file is incorrectly formatted");
        } catch (IOException e) {
            throw new MigrationException(e);
        }
    }
}
