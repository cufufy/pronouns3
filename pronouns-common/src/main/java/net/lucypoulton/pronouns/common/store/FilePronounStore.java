package net.lucypoulton.pronouns.common.store;

import net.lucypoulton.pronouns.api.PronounStore;
import net.lucypoulton.pronouns.api.ProNounsPlugin; // Added
import net.lucypoulton.pronouns.api.set.PronounSet;
import net.lucypoulton.pronouns.api.supplier.PronounSupplier;
import net.lucypoulton.pronouns.api.PronounParser;
// import net.lucypoulton.pronouns.common.ProNouns; // No longer needed for field type
import net.lucypoulton.pronouns.common.util.PropertiesUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FilePronounStore implements PronounStore {
    private final ProNounsPlugin plugin; // Changed type
    private final Path filePath;
    private final Map<UUID, List<PronounSet>> sets = new HashMap<>();

    private static final PronounParser parser = new PronounParser(PronounSet.builtins);

    // Parameter order changed: actualFilePath first, then plugin.
    // filePath parameter now means the exact file, not the directory.
    public FilePronounStore(final Path actualFilePath, final ProNounsPlugin plugin) {
        this.plugin = plugin;
        this.filePath = actualFilePath; // Use the provided path directly
        if (!Files.exists(this.filePath)) {
            save();
            return;
        }
        Properties properties;
        try {
            properties = PropertiesUtil.fromFile(this.filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties.forEach((key, value) -> sets.put(UUID.fromString((String) key), parser.parse((String) value)));
    }

    public static void writeToFile(Map<UUID, List<PronounSet>> sets, Path path, String header) throws IOException {
        final var props = new Properties();
        sets.forEach(((uuid, pronounSets) -> props.put(uuid.toString(),
                pronounSets.stream()
                        .map(PronounSet::toFullString)
                        .collect(Collectors.joining(";")) // Changed delimiter to semicolon
        )));
        try (final var outStream = Files.newOutputStream(path)) {
            props.store(outStream, header);
        }
    }

    private void save() {
        try {
            writeToFile(sets, filePath, "ProNouns storage file. This file should not be edited while the server is running");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PronounSupplier predefined() {
        return PronounSet.builtins;
    }

    @Override
    public List<PronounSet> sets(UUID player) {
        return sets.getOrDefault(player, UNSET_LIST);
    }

    @Override
    public void set(UUID player, @NotNull List<PronounSet> sets) {
        if (sets.size() == 0) this.sets.remove(player);
        else this.sets.put(player, sets);
        plugin.executorService().execute(this::save);
    }

    @Override
    public void setAll(Map<UUID, List<PronounSet>> sets) {
        sets.forEach(this.sets::putIfAbsent);
        plugin.executorService().execute(this::save);
    }

    @Override
    public Map<UUID, List<PronounSet>> dump() {
        return Collections.unmodifiableMap(sets);
    }

    @Override
    public void addPronouns(UUID player, @NotNull List<PronounSet> pronounsToAdd) {
        if (pronounsToAdd.isEmpty()) {
            return;
        }
        final List<PronounSet> currentSets = new ArrayList<>(sets(player));

        if (currentSets.size() == 1 && currentSets.get(0).equals(PronounSet.Builtins.UNSET)) {
            if (pronounsToAdd.size() == 1 && pronounsToAdd.get(0).equals(PronounSet.Builtins.UNSET)) {
                return;
            }
            currentSets.clear();
        }

        for (final PronounSet toAdd : pronounsToAdd) {
            if (!currentSets.contains(toAdd)) {
                currentSets.add(toAdd);
            }
        }
        set(player, currentSets); // set already calls save
    }

    @Override
    public void removePronouns(UUID player, @NotNull List<PronounSet> pronounsToRemove) {
        if (pronounsToRemove.isEmpty()) {
            return;
        }
        final List<PronounSet> currentSets = new ArrayList<>(sets(player));

        if (currentSets.size() == 1 && currentSets.get(0).equals(PronounSet.Builtins.UNSET)) {
            if (pronounsToRemove.contains(PronounSet.Builtins.UNSET)) {
                set(player, Collections.emptyList()); // Clears the entry and saves
            }
            return;
        }

        currentSets.removeAll(pronounsToRemove);
        set(player, currentSets); // set already calls save
    }
}
