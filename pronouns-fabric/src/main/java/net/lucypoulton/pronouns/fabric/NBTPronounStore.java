package net.lucypoulton.pronouns.fabric;

import net.lucypoulton.pronouns.api.PronounSet;
import net.lucypoulton.pronouns.api.PronounStore;
import net.lucypoulton.pronouns.api.PronounSupplier;
import net.lucypoulton.pronouns.api.impl.PronounParser;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A store that stores pronoun info as NBT data alongside a world.
 */
public class NBTPronounStore extends PersistentState implements PronounStore {
    private static final List<PronounSet> UNSET_LIST = List.of(PronounSet.Builtins.UNSET);
    private static final PronounParser PARSER = new PronounParser(PronounSet.builtins);

    private static NBTPronounStore fromNbt(NbtCompound tag) {
        final var store = new NBTPronounStore();

        final var pronounsTag = tag.getCompound("pronouns");
        for (final var key : pronounsTag.getKeys()) {
            final var list = pronounsTag.getList(key, NbtElement.STRING_TYPE)
                    .stream()
                    .map(e -> (NbtString) e)
                    .map(NbtString::asString)
                    .map(NBTPronounStore.PARSER::parse)
                    .map(l -> l.get(0))
                    .toList();

            store.pronounSets.put(
                    UUID.fromString(key),
                    list
            );
        }

        return store;
    }

    public static NBTPronounStore server(MinecraftServer server) {
        final var stateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        final var store = stateManager.getOrCreate(
                NBTPronounStore::fromNbt,
                NBTPronounStore::new,
                "pronouns");

        store.markDirty();

        return store;
    }


    private final Map<UUID, List<PronounSet>> pronounSets = new HashMap<>();

    private NBTPronounStore() {
    }

    @Override
    public PronounSupplier predefined() {
        return PronounSet.builtins;
    }

    @Override
    public List<PronounSet> sets(UUID player) {
        return pronounSets.getOrDefault(player, UNSET_LIST);
    }

    @Override
    public void set(UUID player, @NotNull List<PronounSet> sets) {
        if (sets.size() == 0) pronounSets.remove(player);
        else pronounSets.put(player, sets);
        markDirty();

    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        final var compound = new NbtCompound();
        pronounSets.forEach((UUID, sets) -> {
            NbtList playerStateNbt = new NbtList();
            for (final var set : sets) {
                playerStateNbt.add(NbtString.of(set.toFullString()));
            }

            compound.put(String.valueOf(UUID), playerStateNbt);
        });
        nbt.put("pronouns", compound);
        return nbt;
    }
}
