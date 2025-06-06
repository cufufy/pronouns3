package net.lucypoulton.pronouns.common.store;

import net.lucypoulton.pronouns.api.PronounParser;
import net.lucypoulton.pronouns.api.PronounStore;
import net.lucypoulton.pronouns.api.set.PronounSet;
import net.lucypoulton.pronouns.common.ProNouns;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class FilePronounStoreTest {

    private FilePronounStore store;
    @TempDir
    Path tempDir;
    private Path storePath;

    private final UUID testUuid1 = UUID.randomUUID();
    private final UUID testUuid2 = UUID.randomUUID();

    private final PronounSet HE = PronounSet.Builtins.HE;
    private final PronounSet SHE = PronounSet.Builtins.SHE;
    private final PronounSet THEY = PronounSet.Builtins.THEY;
    private final PronounSet UNSET = PronounSet.Builtins.UNSET;
    private final List<PronounSet> UNSET_LIST = PronounStore.UNSET_LIST;

    // Mock ProNouns plugin and its components as FilePronounStore requires it
    private ProNouns mockPlugin;
    private ExecutorService executorService; // For save operations

    @BeforeEach
    void setUp() {
        storePath = tempDir.resolve("test-pronouns.properties");
        // Mock ProNouns and its ExecutorService for save operations
        mockPlugin = Mockito.mock(ProNouns.class);
        executorService = Executors.newSingleThreadExecutor(); // Use a real executor for testing save/load
        Mockito.when(mockPlugin.executorService()).thenReturn(executorService);
        // FilePronounStore uses a static parser, so no need to mock that via plugin
        store = new FilePronounStore(mockPlugin, tempDir); // Path to directory, file is named inside
    }

    // Helper to ensure save operations complete before assertion by new instances
    private void waitForSave() {
        //This is a bit of a hack for testing. In real usage, saves are async.
        //Forcing the executor to complete submitted tasks.
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        // Reinitialize for next test operations if needed, or ensure each test has its own executor management
        executorService = Executors.newSingleThreadExecutor();
        Mockito.when(mockPlugin.executorService()).thenReturn(executorService);
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
    @DisplayName("Persistence: Data survives store recreation")
    void dataSurvivesRecreation() {
        List<PronounSet> pronouns = List.of(HE, THEY);
        store.set(testUuid1, pronouns);
        waitForSave();

        // New store instance loading from the same file path
        FilePronounStore newStore = new FilePronounStore(mockPlugin, tempDir);
        assertIterableEquals(pronouns, newStore.sets(testUuid1));
    }

    @Test
    @DisplayName("Persistence: File content check")
    void fileContentCheck() throws IOException {
        // Use a known parser for expected string. FilePronounStore uses a static internal one.
        PronounParser parserForFileCheck = new PronounParser(PronounSet.builtins);

        List<PronounSet> pronouns1 = List.of(SHE, THEY);
        store.set(testUuid1, pronouns1);

        List<PronounSet> pronouns2 = List.of(HE);
        store.set(testUuid2, pronouns2);
        waitForSave();

        List<String> lines = Files.readAllLines(storePath.resolve("pronouns-store.properties"));
        // Remove comments and blank lines
        lines.removeIf(line -> line.startsWith("#") || line.trim().isEmpty());

        String expectedLine1 = testUuid1.toString() + "=" + parserForFileCheck.toString(pronouns1);
        String expectedLine2 = testUuid2.toString() + "=" + parserForFileCheck.toString(pronouns2);

        assertTrue(lines.stream().anyMatch(line -> line.replace("\\","").equals(expectedLine1)), "Expected line for UUID1 not found or incorrect: " + expectedLine1);
        assertTrue(lines.stream().anyMatch(line -> line.replace("\\","").equals(expectedLine2)), "Expected line for UUID2 not found or incorrect: " + expectedLine2);
        assertEquals(2, lines.size(), "File should contain exactly two data lines.");
    }


    @Test
    @DisplayName("Overwriting pronouns")
    void overwritePronouns() {
        store.set(testUuid1, List.of(HE));
        waitForSave();
        List<PronounSet> newPronouns = List.of(THEY, SHE);
        store.set(testUuid1, newPronouns);
        waitForSave();

        FilePronounStore newStore = new FilePronounStore(mockPlugin, tempDir);
        assertIterableEquals(newPronouns, newStore.sets(testUuid1));
    }

    @Test
    @DisplayName("Setting empty list removes player (results in UNSET_LIST on get)")
    void setEmptyList() {
        store.set(testUuid1, List.of(HE));
        waitForSave();
        store.set(testUuid1, Collections.emptyList());
        waitForSave();

        assertEquals(UNSET_LIST, store.sets(testUuid1)); // Check current instance
        FilePronounStore newStore = new FilePronounStore(mockPlugin, tempDir); // Check persisted
        assertEquals(UNSET_LIST, newStore.sets(testUuid1));
    }

    @Nested
    @DisplayName("addPronouns tests")
    class AddPronounsTests {
        @Test
        @DisplayName("Add to existing single pronoun")
        void addToExisting() {
            store.set(testUuid1, List.of(SHE));
            waitForSave();
            store.addPronouns(testUuid1, List.of(THEY));
            waitForSave();

            List<PronounSet> expected = List.of(SHE, THEY);
            assertIterableEquals(expected, store.sets(testUuid1));
            FilePronounStore newStore = new FilePronounStore(mockPlugin, tempDir);
            assertIterableEquals(expected, newStore.sets(testUuid1));
        }

        // Other addPronoun tests similar to InMemory, ensuring waitForSave and recreation for persistence checks
    }

    @Nested
    @DisplayName("removePronouns tests")
    class RemovePronounsTests {
        @Test
        @DisplayName("Remove from existing pronouns")
        void removeFromExisting() {
            store.set(testUuid1, List.of(SHE, THEY, HE));
            waitForSave();
            store.removePronouns(testUuid1, List.of(THEY));
            waitForSave();

            List<PronounSet> expected = List.of(SHE, HE);
            assertIterableEquals(expected, store.sets(testUuid1));
            FilePronounStore newStore = new FilePronounStore(mockPlugin, tempDir);
            assertIterableEquals(expected, newStore.sets(testUuid1));
        }

        @Test
        @DisplayName("Remove all pronouns (results in UNSET_LIST)")
        void removeAllPronouns() {
            List<PronounSet> initialPronouns = List.of(SHE, HE);
            store.set(testUuid1, initialPronouns);
            waitForSave();
            store.removePronouns(testUuid1, new ArrayList<>(initialPronouns));
            waitForSave();

            assertEquals(UNSET_LIST, store.sets(testUuid1));
            FilePronounStore newStore = new FilePronounStore(mockPlugin, tempDir);
            assertEquals(UNSET_LIST, newStore.sets(testUuid1));
        }
    }

    // Note: Most behavioral tests for add/remove (edge cases, UNSET handling)
    // are identical to InMemoryPronounStore once basic persistence of these operations is confirmed.
    // For brevity, not all minor variations are re-tested with persistence checks
    // if the core add/remove mechanism is shown to persist.

    @Test
    @DisplayName("dump() provides current data (FilePronounStore doesn't modify map directly for unmodifiability)")
    void dumpReturnsCorrectData() {
        // Dump for FilePronounStore returns a copy of its internal map.
        store.set(testUuid1, List.of(HE, THEY));
        store.set(testUuid2, List.of(SHE));
        // No need to save for dump, it reflects current memory state.

        var dumpedMap = store.dump();
        assertEquals(2, dumpedMap.size());
        assertIterableEquals(List.of(HE, THEY), dumpedMap.get(testUuid1));
        assertIterableEquals(List.of(SHE), dumpedMap.get(testUuid2));
    }
}
