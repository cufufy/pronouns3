package net.lucypoulton.pronouns.paper;

import net.lucypoulton.pronouns.api.set.PronounSet;
import net.lucypoulton.pronouns.api.PronounStore;
import net.lucypoulton.pronouns.api.supplier.PronounSupplier;
import net.lucypoulton.pronouns.api.PronounParser;
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
                        .map(PronounSet::toFullString)
                        .collect(Collectors.joining("/")));
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
