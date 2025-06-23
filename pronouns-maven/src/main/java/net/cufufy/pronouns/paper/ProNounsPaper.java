package net.cufufy.pronouns.paper;

import net.cufufy.pronouns.common.ProNouns;
import net.cufufy.pronouns.common.analytics.Metrics;
import net.cufufy.pronouns.common.platform.ProNounsPermission;
import net.cufufy.pronouns.common.store.StoreFactory;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProNounsPaper extends JavaPlugin {

    private static final StoreFactory factory = new StoreFactory();

    static {
        factory.register("nbt", PersistentDataContainerStore::new);
    }

    private ProNouns plugin;

    @Override
    public void onEnable() {
        if (!PaperDetector.IS_PAPER) {
            throw new RuntimeException("ProNouns requires Paper to run. Get it at https://papermc.io. The plugin will now disable itself.");
        }

        //noinspection ResultOfMethodCallIgnored
        getDataFolder().mkdir();

        for (final var perm : ProNounsPermission.values()) {
            getServer().getPluginManager().addPermission(new Permission(
                    perm.key,
                    perm.description,
                    perm.defaultLevel > 0 ? PermissionDefault.OP : PermissionDefault.TRUE
            ));
        }

        final var platform = new PaperPlatform(this);
        plugin = new ProNouns(platform);
        plugin.createStore(factory);
        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Registering PlaceholderAPI hook.");
            new PlaceholderAPIHook(plugin, platform).register();
        }

        // bStats Metrics
        if (platform.config().stats()) {
            int pluginId = 26256; // Your plugin ID
            Metrics metrics = new Metrics(this, pluginId);
            getLogger().info("bStats metrics enabled.");
        } else {
            getLogger().info("bStats metrics disabled by config.");
        }
    }

    public ProNouns getPlugin() {
        return plugin;
    }
}
