package net.lucypoulton.pronouns.api.test;

import net.lucypoulton.pronouns.api.PronounParser;
import net.lucypoulton.pronouns.api.set.PronounSet;
import net.lucypoulton.pronouns.api.set.SimplePronounSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;


public class ParserTests {

    // Existing parser with custom sets for some specific tests
    private final PronounSet[] customTestSets = {
            PronounSet.from("one", "two", "three", "four", "five", false),
            PronounSet.from("six", "seven", "eight", "nine", "ten", false)
    };
    private final PronounParser customParser = new PronounParser(() -> Set.of(customTestSets));

    // New parser using PronounSet.builtins for most tests
    private final PronounParser parser = new PronounParser(PronounSet.builtins);

    @Test
    @DisplayName("Legacy: Parses predefined parts (custom sets)")
    void parsesPredefined_legacy() {
        for (final var part : List.of("one", "two", "three", "four", "five"))
            assertEquals(customTestSets[0], customParser.parse(part).get(0));
    }

    @Test
    @DisplayName("Legacy: Parses multiple predefined sets with semicolon (custom sets)")
    void parsesMultiple_legacy() {
        // This test is updated to use semicolon, matching current parser logic
        final var parsed = customParser.parse("one;six");
        assertEquals(2, parsed.size());
        assertIterableEquals(List.of(customTestSets[0], customTestSets[1]), parsed);
    }

    @Test
    @DisplayName("Legacy: Excludes repeated predefined sets (custom sets)")
    void excludesRepeated_legacy() {
        final var set = customParser.parse("one;two;one;two;one");
        assertEquals(1, set.size());
        assertEquals(customTestSets[0], set.get(0));
    }

    @Test
    @DisplayName("Legacy: Parses non-predefined singular sets (custom parser context)")
    void parsesSingular_legacy() {
        final var set = PronounSet.from("a", "b", "c", "d", "e", false);
        // Using customParser to ensure it can parse custom sets not in its predefined list
        assertEquals(set, customParser.parse("a/b/c/d/e").get(0));
    }

    @Test
    @DisplayName("Legacy: Parses non-predefined plural sets (custom parser context)")
    void parsesPlural_legacy() {
        final var set = PronounSet.from("a", "b", "c", "d", "e", true);
        assertEquals(set, customParser.parse("a/b/c/d/e:p").get(0));
    }

    @ParameterizedTest
    @DisplayName("Returns empty list for effectively zero or invalid inputs")
    @ValueSource(strings = {"", "/", " ", " / ", "arbitraryNonPronounString", "he/him/his/INVALID/himself"})
    void returnsEmptyListForInvalidInputs(String value) {
        // Updated: parser now returns empty list instead of throwing
        assertTrue(parser.parse(value).isEmpty(), "Input: " + value);
        assertTrue(customParser.parse(value).isEmpty(), "Input with custom parser: " + value);
    }

    @Test
    @DisplayName("Returns empty list for multiple empty parts")
    void returnsEmptyListForMultipleEmptyParts() {
        assertTrue(parser.parse(";;").isEmpty());
        assertTrue(parser.parse(" ; ; ").isEmpty());
    }

    @Nested
    @DisplayName("Tests with PronounSet.builtins")
    class BuiltinParserTests {

        @Test
        @DisplayName("Parses single predefined pronoun: he")
        void parsesHe() {
            assertIterableEquals(List.of(PronounSet.Builtins.HE), parser.parse("he"));
        }

        @Test
        @DisplayName("Parses single predefined pronoun: she")
        void parsesShe() {
            assertIterableEquals(List.of(PronounSet.Builtins.SHE), parser.parse("she"));
        }

        @Test
        @DisplayName("Parses single predefined pronoun: they")
        void parsesThey() {
            assertIterableEquals(List.of(PronounSet.Builtins.THEY), parser.parse("they"));
        }

        @Test
        @DisplayName("Parses single predefined pronoun by full string: he/him/his/his/himself")
        void parsesHeByFullString() {
            assertIterableEquals(List.of(PronounSet.Builtins.HE), parser.parse("he/him/his/his/himself"));
        }

        @Test
        @DisplayName("Parses single predefined pronoun by short string: He/Him")
        void parsesHeByShortString() {
            assertIterableEquals(List.of(PronounSet.Builtins.HE), parser.parse("He/Him"));
            assertIterableEquals(List.of(PronounSet.Builtins.HE), parser.parse("he/him")); // lowercase
        }

        @Test
        @DisplayName("Parses single predefined pronoun by objective form: him")
        void parsesHim() {
            assertIterableEquals(List.of(PronounSet.Builtins.HE), parser.parse("him"));
        }

        @Test
        @DisplayName("Parses multiple predefined pronouns: he;they")
        void parsesMultiplePredefined() {
            List<PronounSet> expected = List.of(PronounSet.Builtins.HE, PronounSet.Builtins.THEY);
            assertIterableEquals(expected, parser.parse("he;they"));
        }

        @Test
        @DisplayName("Parses multiple predefined pronouns with spaces and mixed formats: she ; he/him ")
        void parsesMultiplePredefinedWithSpacesAndMixedFormats() {
            List<PronounSet> expected = List.of(PronounSet.Builtins.SHE, PronounSet.Builtins.HE);
            assertIterableEquals(expected, parser.parse(" she ; he/him "));
        }

        @Test
        @DisplayName("Parses single custom pronoun: ze/zir/zir/zirs/zirself")
        void parsesSingleCustomPronoun() {
            PronounSet expected = new SimplePronounSet("ze", "zir", "zir", "zirs", "zirself", false);
            assertIterableEquals(List.of(expected), parser.parse("ze/zir/zir/zirs/zirself"));
        }

        @Test
        @DisplayName("Parses single custom pronoun with plural: xe/xem/xyr/xyrs/xemself:p")
        void parsesSingleCustomPronounPlural() {
            PronounSet expected = new SimplePronounSet("xe", "xem", "xyr", "xyrs", "xemself", true);
            assertIterableEquals(List.of(expected), parser.parse("xe/xem/xyr/xyrs/xemself:p"));
        }

        @Test
        @DisplayName("Parses custom pronoun with spaces in parts: ze / zir / zir / zirs / zirself")
        void parsesCustomPronounWithSpacesInParts() {
            PronounSet expected = new SimplePronounSet("ze", "zir", "zir", "zirs", "zirself", false);
            assertIterableEquals(List.of(expected), parser.parse("ze / zir / zir / zirs / zirself"));
        }


        @Test
        @DisplayName("Parses multiple custom pronouns")
        void parsesMultipleCustomPronouns() {
            PronounSet custom1 = new SimplePronounSet("ze", "zir", "zir", "zirs", "zirself", false);
            PronounSet custom2 = new SimplePronounSet("xe", "xem", "xyr", "xyrs", "xemself", true);
            List<PronounSet> expected = List.of(custom1, custom2);
            assertIterableEquals(expected, parser.parse("ze/zir/zir/zirs/zirself;xe/xem/xyr/xyrs/xemself:p"));
        }

        @Test
        @DisplayName("Parses mixed predefined and custom pronouns: she;ze/zir/zir/zirs/zirself;they")
        void parsesMixedPredefinedAndCustom() {
            PronounSet custom = new SimplePronounSet("ze", "zir", "zir", "zirs", "zirself", false);
            List<PronounSet> expected = List.of(PronounSet.Builtins.SHE, custom, PronounSet.Builtins.THEY);
            assertIterableEquals(expected, parser.parse("she;ze/zir/zir/zirs/zirself;they"));
        }

        @Test
        @DisplayName("Preserves order and ensures uniqueness: he;they;he")
        void preservesOrderAndEnsuresUniqueness() {
            List<PronounSet> expected = List.of(PronounSet.Builtins.HE, PronounSet.Builtins.THEY);
            assertIterableEquals(expected, parser.parse("he;they;he"));
        }

        @Test
        @DisplayName("Ignores invalid parts among valid ones: he;invalid;they")
        void ignoresInvalidPartsAmongValid() {
            List<PronounSet> expected = List.of(PronounSet.Builtins.HE, PronounSet.Builtins.THEY);
            assertIterableEquals(expected, parser.parse("he;invalidStringPart;they"));
        }

        @Test
        @DisplayName("Ignores malformed custom pronoun parts: he;a/b/c;they")
        void ignoresMalformedCustomParts() {
            List<PronounSet> expected = List.of(PronounSet.Builtins.HE, PronounSet.Builtins.THEY);
            assertIterableEquals(expected, parser.parse("he;a/b/c;they")); // a/b/c is not 5 parts
        }


        @Test
        @DisplayName("Parses Builtins.ANY")
        void parsesAny() {
            assertIterableEquals(List.of(PronounSet.Builtins.ANY), parser.parse("any"));
        }

        @Test
        @DisplayName("Parses Builtins.ASK and Builtins.UNSET")
        void parsesAskAndUnset() {
            List<PronounSet> expected = List.of(PronounSet.Builtins.ASK, PronounSet.Builtins.UNSET);
            assertIterableEquals(expected, parser.parse("ask;unset"));
        }

        @Test
        @DisplayName("Parses PronounSet.Builtins.THEY.toString()")
        void parsesPredefinedToString() {
            assertIterableEquals(List.of(PronounSet.Builtins.THEY), parser.parse(PronounSet.Builtins.THEY.toString()));
        }

        @Test
        @DisplayName("Parses PronounSet.Builtins.SHE.toFullString()")
        void parsesPredefinedToFullString() {
            assertIterableEquals(List.of(PronounSet.Builtins.SHE), parser.parse(PronounSet.Builtins.SHE.toFullString()));
        }

        @Test
        @DisplayName("Parses a mix of full string, short string, and subjective for predefined")
        void parsesMixedPredefinedFormats() {
            List<PronounSet> expected = List.of(PronounSet.Builtins.HE, PronounSet.Builtins.SHE, PronounSet.Builtins.THEY);
            assertIterableEquals(expected, parser.parse("he/him/his/his/himself;She/Her;they"));
        }

        @Test
        @DisplayName("Serializes a list of PronounSets correctly")
        void serializesListToString() {
            PronounSet customZe = PronounSet.from("ze", "zir", "zir", "zirs", "zirself", false);
            List<PronounSet> sets = List.of(PronounSet.Builtins.SHE, customZe, PronounSet.Builtins.THEY);
            String expected = "she/her/her/hers/herself;ze/zir/zir/zirs/zirself;they/them/their/theirs/themselves:p";
            assertEquals(expected, parser.toString(sets));
        }

        @Test
        @DisplayName("Serializes an empty list to an empty string")
        void serializesEmptyList() {
            assertEquals("", parser.toString(Collections.emptyList()));
        }

        @Test
        @DisplayName("Serializes a list with one set")
        void serializesSingletonList() {
            List<PronounSet> sets = List.of(PronounSet.Builtins.HE);
            String expected = "he/him/his/his/himself";
            assertEquals(expected, parser.toString(sets));
        }
    }
}
