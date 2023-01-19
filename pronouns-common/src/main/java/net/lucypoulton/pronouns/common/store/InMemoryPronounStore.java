package net.lucypoulton.pronouns.common.store;

import net.lucypoulton.pronouns.api.set.PronounSet;
import net.lucypoulton.pronouns.api.PronounStore;
import net.lucypoulton.pronouns.api.supplier.PronounSupplier;
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
    public Map<UUID, List<PronounSet>> dump() {
        return Collections.unmodifiableMap(storage);
    }
}
