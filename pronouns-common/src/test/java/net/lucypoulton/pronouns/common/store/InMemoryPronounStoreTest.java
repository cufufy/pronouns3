package net.lucypoulton.pronouns.common.store;

import net.lucypoulton.pronouns.api.PronounStore;
import net.lucypoulton.pronouns.api.set.PronounSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class InMemoryPronounStoreTest {

    private InMemoryPronounStore store;
    private final UUID testUuid1 = UUID.randomUUID();
    private final UUID testUuid2 = UUID.randomUUID();

    private final PronounSet HE = PronounSet.Builtins.HE;
    private final PronounSet SHE = PronounSet.Builtins.SHE;
    private final PronounSet THEY = PronounSet.Builtins.THEY;
    private final PronounSet UNSET = PronounSet.Builtins.UNSET; // Convenience
    private final List<PronounSet> UNSET_LIST = PronounStore.UNSET_LIST;


    @BeforeEach
    void setUp() {
        store = new InMemoryPronounStore();
    }

    @Test
    @DisplayName("New store returns UNSET_LIST for unknown UUID")
    void newStoreReturnsUnset() {
        assertEquals(UNSET_LIST, store.sets(testUuid1));
    }

    @Test
    @DisplayName("Set and get a single pronoun set")
    void setAndGetSinglePronoun() {
        List<PronounSet> pronouns = List.of(SHE);
        store.set(testUuid1, pronouns);
        assertEquals(pronouns, store.sets(testUuid1));
    }

    @Test
    @DisplayName("Set and get multiple pronoun sets")
    void setAndGetMultiplePronouns() {
        List<PronounSet> pronouns = List.of(SHE, THEY);
        store.set(testUuid1, pronouns);
        assertIterableEquals(pronouns, store.sets(testUuid1));
    }

    @Test
    @DisplayName("Set pronouns for multiple players")
    void setAndGetMultiplePlayers() {
        List<PronounSet> pronouns1 = List.of(SHE, THEY);
        List<PronounSet> pronouns2 = List.of(HE);
        store.set(testUuid1, pronouns1);
        store.set(testUuid2, pronouns2);
        assertIterableEquals(pronouns1, store.sets(testUuid1));
        assertIterableEquals(pronouns2, store.sets(testUuid2));
    }

    @Test
    @DisplayName("Overwriting pronouns")
    void overwritePronouns() {
        store.set(testUuid1, List.of(HE));
        List<PronounSet> newPronouns = List.of(THEY, SHE);
        store.set(testUuid1, newPronouns);
        assertIterableEquals(newPronouns, store.sets(testUuid1));
    }

    @Test
    @DisplayName("Setting empty list removes player (should result in UNSET_LIST on get)")
    void setEmptyList() {
        store.set(testUuid1, List.of(HE)); // ensure player exists
        store.set(testUuid1, Collections.emptyList());
        // InMemoryPronounStore specific: removes player if list is empty, so get should return default
        assertEquals(UNSET_LIST, store.sets(testUuid1));
    }

    @Test
    @DisplayName("Setting UNSET_LIST clears pronouns")
    void setUnsetList() {
        store.set(testUuid1, List.of(HE));
        store.set(testUuid1, UNSET_LIST); // UNSET_LIST is List.of(PronounSet.Builtins.UNSET)
        // The behavior of setting UNSET_LIST might mean it stores UNSET_LIST or it clears to default UNSET_LIST
        // For InMemory, set(uuid, List.of(UNSET)) will store that list.
        assertEquals(UNSET_LIST, store.sets(testUuid1));
    }


    @Nested
    @DisplayName("addPronouns tests")
    class AddPronounsTests {
        @Test
        @DisplayName("Add to existing single pronoun")
        void addToExisting() {
            store.set(testUuid1, List.of(SHE));
            store.addPronouns(testUuid1, List.of(THEY));
            assertIterableEquals(List.of(SHE, THEY), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Add to existing multiple pronouns")
        void addToExistingMultiple() {
            store.set(testUuid1, List.of(SHE, HE));
            store.addPronouns(testUuid1, List.of(THEY));
            // Order might vary depending on underlying list and add logic, use containsAll for safety
            List<PronounSet> expected = List.of(SHE, HE, THEY);
            List<PronounSet> actual = store.sets(testUuid1);
            assertTrue(actual.containsAll(expected) && expected.containsAll(actual) && actual.size() == expected.size());
        }

        @Test
        @DisplayName("Add to unset player")
        void addToUnsetPlayer() {
            store.addPronouns(testUuid1, List.of(THEY));
            assertIterableEquals(List.of(THEY), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Add existing pronoun (uniqueness)")
        void addExistingPronoun() {
            store.set(testUuid1, List.of(SHE, THEY));
            store.addPronouns(testUuid1, List.of(SHE)); // Adding SHE again
            assertIterableEquals(List.of(SHE, THEY), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Add empty list (should do nothing)")
        void addEmptyList() {
            store.set(testUuid1, List.of(SHE));
            store.addPronouns(testUuid1, Collections.emptyList());
            assertIterableEquals(List.of(SHE), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Add UNSET to existing pronouns (should replace with UNSET if UNSET is not already primary)")
        void addUnsetToExisting() {
            // InMemoryPronounStore specific logic for addPronouns: if current is UNSET, it clears before adding.
            // If adding UNSET to non-UNSET, it should add UNSET to the list.
            // However, if the list has UNSET as only item, it's UNSET_LIST.
            // The implementation of addPronouns in InMemoryPronounStore clears currentSets if it was UNSET.
            // If currentSets was not UNSET, and we add UNSET, it gets added.
            // Let's test the case where current is SHE, and we add UNSET.
            store.set(testUuid1, List.of(SHE));
            store.addPronouns(testUuid1, List.of(UNSET));
            // Expected: [SHE, UNSET]
            List<PronounSet> expected = List.of(SHE, UNSET);
            List<PronounSet> actual = store.sets(testUuid1);
            assertTrue(actual.containsAll(expected) && expected.containsAll(actual) && actual.size() == expected.size());
        }

        @Test
        @DisplayName("Add pronouns to player who is explicitly UNSET (should replace UNSET)")
        void addPronounsToExplicitlyUnset() {
            store.set(testUuid1, UNSET_LIST); // Player is explicitly UNSET
            store.addPronouns(testUuid1, List.of(HE));
            assertIterableEquals(List.of(HE), store.sets(testUuid1));
        }
    }

    @Nested
    @DisplayName("removePronouns tests")
    class RemovePronounsTests {
        @Test
        @DisplayName("Remove from existing pronouns")
        void removeFromExisting() {
            store.set(testUuid1, List.of(SHE, THEY, HE));
            store.removePronouns(testUuid1, List.of(THEY));
            assertIterableEquals(List.of(SHE, HE), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Remove multiple pronouns")
        void removeMultiplePronouns() {
            store.set(testUuid1, List.of(SHE, THEY, HE));
            store.removePronouns(testUuid1, List.of(SHE, HE));
            assertIterableEquals(List.of(THEY), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Remove pronoun not present")
        void removePronounNotPresent() {
            store.set(testUuid1, List.of(SHE, HE));
            store.removePronouns(testUuid1, List.of(THEY)); // THEY is not there
            assertIterableEquals(List.of(SHE, HE), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Remove all pronouns (results in UNSET_LIST)")
        void removeAllPronouns() {
            List<PronounSet> initialPronouns = List.of(SHE, HE);
            store.set(testUuid1, initialPronouns);
            store.removePronouns(testUuid1, new ArrayList<>(initialPronouns)); // Pass a mutable copy
            assertEquals(UNSET_LIST, store.sets(testUuid1));
        }

        @Test
        @DisplayName("Remove from unset player (should do nothing)")
        void removeFromUnsetPlayer() {
            store.removePronouns(testUuid1, List.of(THEY));
            assertEquals(UNSET_LIST, store.sets(testUuid1));
        }

        @Test
        @DisplayName("Remove empty list (should do nothing)")
        void removeEmptyList() {
            store.set(testUuid1, List.of(SHE));
            store.removePronouns(testUuid1, Collections.emptyList());
            assertIterableEquals(List.of(SHE), store.sets(testUuid1));
        }

        @Test
        @DisplayName("Remove UNSET from explicitly UNSET player (results in UNSET_LIST via empty)")
        void removeUnsetFromUnsetPlayer() {
            store.set(testUuid1, UNSET_LIST);
            store.removePronouns(testUuid1, List.of(UNSET)); // or UNSET_LIST
            assertEquals(UNSET_LIST, store.sets(testUuid1));
        }
    }

    @Test
    @DisplayName("dump() returns an unmodifiable map containing current state")
    void dumpReturnsCorrectData() {
        store.set(testUuid1, List.of(HE, THEY));
        store.set(testUuid2, List.of(SHE));

        var dumpedMap = store.dump();
        assertEquals(2, dumpedMap.size());
        assertIterableEquals(List.of(HE, THEY), dumpedMap.get(testUuid1));
        assertIterableEquals(List.of(SHE), dumpedMap.get(testUuid2));

        // Check if unmodifiable
        try {
            dumpedMap.put(UUID.randomUUID(), List.of(UNSET));
            throw new AssertionError("dump() map should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    @DisplayName("predefined() returns PronounSet.builtins")
    void predefinedReturnsBuiltins() {
        assertEquals(PronounSet.builtins, store.predefined());
    }
}
