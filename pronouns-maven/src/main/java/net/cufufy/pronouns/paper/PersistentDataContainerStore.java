package net.cufufy.pronouns.paper;

import net.cufufy.pronouns.api.set.PronounSet;
import net.cufufy.pronouns.api.PronounStore;
import net.cufufy.pronouns.api.supplier.PronounSupplier;
import net.cufufy.pronouns.api.PronounParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PersistentDataContainerStore implements PronounStore {
    private static final NamespacedKey KEY = new NamespacedKey("pronouns", "pronouns");

    private static final PronounParser parser = new PronounParser(PronounSet.builtins);


    @Override
    public PronounSupplier predefined() {
        return PronounSet.builtins;
    }

    private Player getPlayer(UUID uuid) {
        final var player = Bukkit.getPlayer(uuid);
        if (player == null) throw new IllegalArgumentException("Unknown player " + uuid);
        return player;
    }

    @Override
    public List<PronounSet> sets(UUID player) {
        final var key = getPlayer(player).getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        if (key == null) return UNSET_LIST;
        return parser.parse(key);
    }

    @Override
    public void set(UUID player, @NotNull List<PronounSet> sets) {
        final var pdc = getPlayer(player).getPersistentDataContainer();
        if (sets.size() == 0) pdc.remove(KEY);
        else pdc.set(KEY,
                PersistentDataType.STRING,
                sets.stream()
                        // Use PronounParser's toString method for correct serialization, including ; delimiter
                        .map(PronounSet::toFullString) // Keep toFullString for individual sets
                        .collect(Collectors.joining(";"))); // Change delimiter to ;
    }

    // TODO: Consider if addPronouns should ensure uniqueness or rely on the final `set` operation with parsing.
    // For now, simple concatenation and let parsing handle uniqueness if the string is reparsed later.
    @Override
    public void addPronouns(UUID player, @NotNull List<PronounSet> setsToAdd) {
        if (setsToAdd.isEmpty()) {
            return;
        }
        List<PronounSet> currentSets = new java.util.ArrayList<>(sets(player));
        // Avoid modifying UNSET_LIST directly if it's immutable and shared
        if (currentSets == UNSET_LIST) {
            currentSets = new java.util.ArrayList<>();
        }
        currentSets.addAll(setsToAdd);
        // Let PronounParser handle uniqueness if string is parsed by it later.
        // For now, PronounStore API implies List, so duplicates could exist here.
        // However, our parser produces unique sets, so `set` will make it unique.
        set(player, currentSets);
    }

    @Override
    public void removePronouns(UUID player, @NotNull List<PronounSet> setsToRemove) {
        if (setsToRemove.isEmpty()) {
            return;
        }
        List<PronounSet> currentSets = sets(player);
        if (currentSets.isEmpty() || currentSets == UNSET_LIST) {
            return;
        }
        List<PronounSet> mutableCurrentSets = new java.util.ArrayList<>(currentSets);
        mutableCurrentSets.removeAll(setsToRemove);
        set(player, mutableCurrentSets);
    }

    @Override
    public void setAll(Map<UUID, List<PronounSet>> sets) {
        throw new UnsupportedOperationException("PDC (paper NBT) store does not support setting all");
    }

    @Override
    public Map<UUID, List<PronounSet>> dump() {
        throw new UnsupportedOperationException("PDC (paper NBT) store does not support dumping");
    }
}
