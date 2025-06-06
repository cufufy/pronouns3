package net.lucypoulton.pronouns.common.cmd;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import net.lucypoulton.pronouns.api.set.PronounSet;
import net.lucypoulton.pronouns.common.ProNouns;
import net.lucypoulton.pronouns.common.message.ProNounsTranslations; // Added import
import net.lucypoulton.pronouns.common.platform.CommandSender;
import net.lucypoulton.pronouns.common.platform.ProNounsPermission;
import net.lucypoulton.pronouns.common.platform.Platform;
import org.jetbrains.annotations.Nullable;

import java.util.List; // Added import

public class SetCommand implements ProNounsCommand {
    private final ProNouns plugin;
    private final Platform platform;

    public SetCommand(ProNouns plugin, Platform platform) {
        this.plugin = plugin;
        this.platform = platform;
    }

    public void execute(CommandSender sender, String value, @Nullable String targetPlayer) {
        final var f = plugin.formatter();
        final var target = CommandUtils.getPlayerOrSender(sender, targetPlayer, platform);
        final var player = target.sender();
        if (player.uuid().isEmpty()) {
            sender.sendMessage(f.translated("pronouns.command.noPlayer"));
            return;
        }

        final List<PronounSet> pronouns;
        try {
            pronouns = plugin.parser().parse(value);
        } catch (Exception e) {
            // Catch any unexpected parsing errors, though PronounParser is expected to return empty list for bad user input
            plugin.platform().logger().warn("Pronoun parsing unexpectedly failed for input '" + value + "': " + e.getMessage()); // Changed to warn
            sender.sendMessage(f.translated("pronouns.command.set.parseError", value));
            return;
        }

        if (pronouns.isEmpty()) {
            sender.sendMessage(f.translated("pronouns.command.set.badSet", value));
            return;
        }

        // Implement "Ask" exclusivity
        boolean containsAsk = pronouns.stream().anyMatch(p -> p.equals(PronounSet.Builtins.ASK));
        final List<PronounSet> finalPronounsToSet;
        if (containsAsk && pronouns.size() > 1) {
            finalPronounsToSet = List.of(PronounSet.Builtins.ASK);
            // Send a message to the player informing them of this override.
            // The original sender gets this message, even if they are setting for someone else.
            sender.sendMessage(f.translated("pronouns.command.set.askOverride")); // Use f.translated with string key
        } else {
            finalPronounsToSet = pronouns;
        }

        plugin.store().set(player.uuid().get(), finalPronounsToSet);
        sender.sendMessage(
                f.translated("pronouns.command.set." + (target.isNotSender() ? "other" : "self"),
                        PronounSet.format(finalPronounsToSet),
                        player.name()
                )
        );
    }

    @Override
    public Command.Builder<CommandSender> build(Command.Builder<CommandSender> builder) {
        return builder.literal("set")
                .meta(CommandMeta.DESCRIPTION, CommandUtils.description("set"))
                .permission(ProNounsPermission.SET.key)
                .argument(StringArgument.greedyFlagYielding("pronouns"))
                .flag(CommandFlag.builder("player")
                        .withPermission(cloud.commandframework.permission.Permission.of(ProNounsPermission.SET_OTHER.key))
                        .withDescription(ArgumentDescription.of("A username of a player to set pronouns for."))
                        .withArgument(
                                CommandUtils.optionalPlayer("player", platform))
                ).handler(ctx -> execute(ctx.getSender(),
                        ctx.get("pronouns"),
                        ctx.getOrDefault("player", null)));
    }
}
