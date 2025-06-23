package net.cufufy.pronouns.paper;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.cufufy.pronouns.common.platform.CommandSender;
import net.cufufy.pronouns.common.platform.Platform;
import net.cufufy.pronouns.common.platform.config.Config;
import net.cufufy.pronouns.common.platform.config.PropertiesConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaperPlatform implements Platform {

    private final ProNounsPaper plugin;
    private final CommandManager<CommandSender> manager;
    private final Config config;

    public PaperPlatform(ProNounsPaper plugin) {
        this.plugin = plugin;
        try {
            this.manager = new PaperCommandManager<>(plugin,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    BukkitCommandSenderWrapper::new,
                    sender -> ((BukkitCommandSenderWrapper) sender).bukkitSender());
            this.config = new PropertiesConfig(plugin.getDataFolder().toPath().resolve("pronouns.cfg"), logger()).reloadConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "Paper";
    }

    @Override
    public String currentVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String minecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    @Override
    public Path dataDir() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public Logger logger() {
        return plugin.getSLF4JLogger();
    }

    @Override
    public void broadcast(Component component, String permission) {
        Bukkit.broadcast(component, permission);
    }

    @Override
    public CommandManager<CommandSender> commandManager() {
        return manager;
    }

    @Override
    public Optional<CommandSender> getPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name)).map(BukkitCommandSenderWrapper::new);
    }

    @Override
    public Optional<CommandSender> getPlayer(UUID uuid) {
        return Optional.ofNullable(Bukkit.getPlayer(uuid)).map(BukkitCommandSenderWrapper::new);
    }

    @Override
    public List<String> listPlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public boolean migratable() {
        return true;
    }

    @Override
    public String[] commandAliases() {
        return new String[]{"pronounspaper", "pnpaper", "pnp"};
    }
}
