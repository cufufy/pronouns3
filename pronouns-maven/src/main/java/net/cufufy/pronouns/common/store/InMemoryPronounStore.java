package net.cufufy.pronouns.common.store;

import net.cufufy.pronouns.api.set.PronounSet;
import net.cufufy.pronouns.api.PronounStore;
import net.cufufy.pronouns.api.supplier.PronounSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A non-persistent store intended for testing.
 */
public class InMemoryPronounStore implements PronounStore {

    private final Map<UUID, List<PronounSet>> storage = new HashMap<>();
    @Override
    public PronounSupplier predefined() {
        return PronounSet.builtins;
    }


    @Override
    public List<PronounSet> sets(UUID player) {
        return storage.getOrDefault(player, UNSET_LIST);
    }

    @Override
    public void set(UUID player, @NotNull List<PronounSet> sets) {
        if (sets.size() == 0) storage.remove(player);
        else storage.put(player, sets);
    }

    @Override
    public void setAll(Map<UUID, List<PronounSet>> sets) {
        sets.forEach(storage::putIfAbsent);
    }

    @Override
    public void addPronouns(UUID player, @NotNull List<PronounSet> pronounsToAdd) {
        if (pronounsToAdd.isEmpty()) {
            return;
        }
        final List<PronounSet> currentSets = new ArrayList<>(sets(player));

        // If current is UNSET, replace with new pronouns if new ones are not UNSET.
        // If pronounsToAdd contains UNSET and currentSets is UNSET, do nothing.
        if (currentSets.size() == 1 && currentSets.get(0).equals(PronounSet.Builtins.UNSET)) {
            if (pronounsToAdd.size() == 1 && pronounsToAdd.get(0).equals(PronounSet.Builtins.UNSET)) {
                return; // Adding UNSET to UNSET does nothing
            }
            // If adding actual pronouns to an UNSET record, treat currentSets as empty before adding.
            // unless pronounsToAdd is also UNSET (already handled)
            currentSets.clear();
        }

        for (final PronounSet toAdd : pronounsToAdd) {
            if (!currentSets.contains(toAdd)) {
                currentSets.add(toAdd);
            }
        }
        set(player, currentSets);
    }

    @Override
    public void removePronouns(UUID player, @NotNull List<PronounSet> pronounsToRemove) {
        if (pronounsToRemove.isEmpty()) {
            return;
        }
        final List<PronounSet> currentSets = new ArrayList<>(sets(player));

        // If current is UNSET, there's nothing to remove unless removing UNSET itself
        if (currentSets.size() == 1 && currentSets.get(0).equals(PronounSet.Builtins.UNSET)) {
            if (pronounsToRemove.contains(PronounSet.Builtins.UNSET)) {
                set(player, Collections.emptyList()); // Clears the entry
            }
            return;
        }

        currentSets.removeAll(pronounsToRemove);
        set(player, currentSets);
    }

    @Override
    public Map<UUID, List<PronounSet>> dump() {
        return Collections.unmodifiableMap(storage);
    }
}
