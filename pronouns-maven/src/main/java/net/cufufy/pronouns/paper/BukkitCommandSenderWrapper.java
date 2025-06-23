package net.cufufy.pronouns.paper;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.cufufy.pronouns.common.platform.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record BukkitCommandSenderWrapper(
        org.bukkit.command.CommandSender bukkitSender) implements CommandSender, ForwardingAudience.Single {

    @Override
    public Optional<UUID> uuid() {
        return bukkitSender instanceof Player player ? Optional.of(player.getUniqueId()) : Optional.empty();
    }

    @Override
    public String name() {
        return bukkitSender.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return bukkitSender.hasPermission(permission);
    }

    @Override
    public @NotNull Audience audience() {
        return bukkitSender;
    }
}
