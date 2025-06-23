package net.cufufy.pronouns.common.store;

import com.zaxxer.hikari.HikariDataSource;
import net.cufufy.pronouns.api.PronounParser;
import net.cufufy.pronouns.api.set.PronounSet;
import net.cufufy.pronouns.api.supplier.PronounSupplier;
import net.cufufy.pronouns.common.ProNouns;
import net.cufufy.pronouns.common.platform.config.Config;
import net.cufufy.pronouns.common.util.UuidUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList; // Added import
import java.util.Collections; // Added import
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MySqlPronounStore implements CachedPronounStore, AutoCloseable {

    private final HikariDataSource dataSource;
    private final Map<UUID, List<PronounSet>> cache = new ConcurrentHashMap<>();
    private final ProNouns plugin;
    private static final PronounParser parser = new PronounParser(PronounSet.builtins);

    private Instant lastTimestamp = Instant.now();

    public MySqlPronounStore(final ProNouns plugin, final Config.MySqlConnectionInfo connectionInfo) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.plugin = plugin;
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(connectionInfo.jdbcUrl());
        dataSource.setUsername(connectionInfo.username());
        dataSource.setPassword(connectionInfo.password());

        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.addDataSourceProperty("useServerPrepStmts ", "true");
        dataSource.addDataSourceProperty("rewriteBatchedStatements", "true");

        try (final var con = dataSource.getConnection()) {
            con.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS pronouns (
                        player BINARY(16) PRIMARY KEY,
                        pronouns TEXT NOT NULL,
                        last_updated_from TEXT NOT NULL,
                        last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        plugin.platform().logger().info("Connected to MySQL");

        plugin.executorService().scheduleWithFixedDelay(this::poll, 10, 10, TimeUnit.SECONDS);
    }

    private void push(UUID uuid, List<PronounSet> sets) {
        try (final var con = dataSource.getConnection()) {
            if (sets.size() == 0) {
                final var stmt = con.prepareStatement("DELETE FROM pronouns WHERE player=?");
                stmt.setBytes(1, UuidUtil.toBytes(uuid));
                stmt.execute();
                return;
            }
            final var stmt = con.prepareStatement("REPLACE INTO pronouns (player, pronouns, last_updated_from) VALUES (?, ?, ?)");
            stmt.setBytes(1, UuidUtil.toBytes(uuid));
            stmt.setString(2, sets.stream().map(PronounSet::toFullString).collect(java.util.stream.Collectors.joining(";")));
            stmt.setString(3, "pronouns_plugin_instance"); // Replaced plugin.meta().identifier()
            stmt.execute();
        } catch (SQLException e) {
            plugin.platform().logger().error("Failed to write pronouns to MySQL: " + e.getMessage());
        }
    }

    private void pushAll(Map<UUID, List<PronounSet>> sets) {
        try (final var con = dataSource.getConnection()) {
            final var stmt = con.prepareStatement("REPLACE INTO pronouns (player, pronouns, last_updated_from) VALUES (?, ?, ?)");
            for (final var entry : sets.entrySet()) {
                stmt.setBytes(1, UuidUtil.toBytes(entry.getKey()));
                stmt.setString(2, entry.getValue().stream().map(PronounSet::toFullString).collect(java.util.stream.Collectors.joining(";")));
                stmt.setString(3, "pronouns_plugin_instance"); // Replaced plugin.meta().identifier()
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            plugin.platform().logger().error("Failed to write pronouns to MySQL: " + e.getMessage());
        }
    }

    /**
     * Polls the database to keep the cache up to date.
     * Selects all entries where:
     * <ul>
     *     <li>a cache entry already exists (i.e. they're on the server)</li>
     *     <li>it has changed since the last time the server polled</li>
     *     <li>it was last updated by a different server</li>
     * </ul>
     */
    private void poll() {
        try (final var con = dataSource.getConnection()) {
            if (cache.size() == 0) return;
            final var stmt = con.prepareStatement(
                    "SELECT * FROM pronouns WHERE last_updated_at > ? AND last_updated_from != ?"
            );
            stmt.setTimestamp(1, Timestamp.from(lastTimestamp));
            stmt.setString(2, "pronouns_plugin_instance"); // Replaced plugin.meta().identifier()
            final var results = stmt.executeQuery();
            while (results.next()) {
                final var uuid = UuidUtil.fromBytes(results.getBytes("player"));
                if (!cache.containsKey(uuid)) continue;
                final var newSets = parser.parse(results.getString("pronouns"));
                cache.put(uuid, newSets);

                // this is safe - if the player is not present they won't be in the cache
                //noinspection OptionalGetWithoutIsPresent
                plugin.platform().logger().info("Player " +
                        plugin.platform().getPlayer(uuid).get().name() +
                        " changed pronouns to " + PronounSet.format(newSets) + " on another server");
            }
            lastTimestamp = Instant.now();
        } catch (Exception e) {
            plugin.platform().logger().error("Failed to update pronoun cache from MySQL: " + e.getMessage());
        }
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
        // Update cache immediately
        if (currentSets.isEmpty()) {
            cache.remove(player);
        } else {
            cache.put(player, currentSets);
        }
        // Asynchronously push to database
        final List<PronounSet> setsToPush = List.copyOf(currentSets); // Make a copy for the async task
        plugin.executorService().submit(() -> push(player, setsToPush));
    }

    @Override
    public void removePronouns(UUID player, @NotNull List<PronounSet> pronounsToRemove) {
        if (pronounsToRemove.isEmpty()) {
            return;
        }
        final List<PronounSet> currentSets = new ArrayList<>(sets(player));

        if (currentSets.size() == 1 && currentSets.get(0).equals(PronounSet.Builtins.UNSET)) {
            if (pronounsToRemove.contains(PronounSet.Builtins.UNSET)) {
                cache.remove(player);
                plugin.executorService().submit(() -> push(player, Collections.emptyList()));
            }
            return;
        }

        boolean changed = currentSets.removeAll(pronounsToRemove);
        if (changed) {
            // Update cache immediately
            if (currentSets.isEmpty()) {
                cache.remove(player);
            } else {
                cache.put(player, currentSets);
            }
            // Asynchronously push to database
            final List<PronounSet> setsToPush = List.copyOf(currentSets); // Make a copy for the async task
            plugin.executorService().submit(() -> push(player, setsToPush));
        }
    }

    @Override
    public PronounSupplier predefined() {
        return PronounSet.builtins;
    }

    @Override
    public List<PronounSet> sets(UUID player) {
        return cache.getOrDefault(player, UNSET_LIST);
    }

    @Override
    public void set(UUID player, @NotNull List<PronounSet> sets) {
        if (sets.size() == 0) cache.remove(player);
        else cache.put(player, sets);
        plugin.executorService().submit(() -> push(player, sets));
    }

    @Override
    public void setAll(Map<UUID, List<PronounSet>> sets) {
        sets.forEach(cache::putIfAbsent);
        plugin.executorService().submit(() -> pushAll(sets));
    }

    @Override
    public Map<UUID, List<PronounSet>> dump() {
        // fixme
        throw new RuntimeException("L + ratio + get better");
    }

    @Override
    public void onPlayerJoin(UUID uuid) {
        try (final var con = dataSource.getConnection()) {
            final var stmt = con.prepareStatement("SELECT pronouns FROM pronouns WHERE player=?");
            stmt.setBytes(1, UuidUtil.toBytes(uuid));
            final var resultSet = stmt.executeQuery();
            if (!resultSet.next()) return;
            cache.put(uuid, parser.parse(resultSet.getString("pronouns")));
        } catch (SQLException e) {
            plugin.platform().logger().error("Failed to fetch pronouns from MySQL: " + e.getMessage());
        }
    }

    @Override
    public void onPlayerLeave(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
