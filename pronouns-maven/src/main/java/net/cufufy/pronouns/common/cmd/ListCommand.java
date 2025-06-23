package net.cufufy.pronouns.common.cmd;

import cloud.commandframework.Command;
import cloud.commandframework.meta.CommandMeta;
import net.cufufy.pronouns.common.ProNouns;
import net.cufufy.pronouns.common.platform.CommandSender;
import net.cufufy.pronouns.common.platform.Platform;
import net.cufufy.pronouns.common.platform.ProNounsPermission;
import net.cufufy.pronouns.api.PronounParser; // May need to adjust if new method is in ProNouns
import net.kyori.adventure.text.Component; // Added import

import java.util.List;
import java.util.stream.Collectors;

public class ListCommand implements ProNounsCommand {
    private final ProNouns plugin;
    private final Platform platform;

    public ListCommand(ProNouns plugin, Platform platform) {
        this.plugin = plugin;
        this.platform = platform;
    }

    public void execute(CommandSender sender) {
        final var formatter = plugin.formatter();
        // Assuming PronounParser has a method to get available pronoun names/strings
        // This might need to be plugin.getPronounParser().getAvailablePronounNames()
        // or plugin.getAvailablePronounNames() if we add a helper in ProNouns
        List<String> availablePronouns = plugin.parser().getAvailablePronounNames(); // Placeholder for now

        if (availablePronouns.isEmpty()) {
            sender.sendMessage(formatter.translated("pronouns.command.list.none"));
            return;
        }

        String pronounListString = availablePronouns.stream()
                .sorted() // Sort for consistent display
                .collect(Collectors.joining(", "));

        sender.sendMessage(formatter.translated("pronouns.command.list.header"));
        sender.sendMessage(Component.text(pronounListString)); // Use Component.text() for plain string
    }

    @Override
    public Command.Builder<CommandSender> build(Command.Builder<CommandSender> builder) {
        return builder.literal("list")
                .meta(CommandMeta.DESCRIPTION, CommandUtils.description("list"))
                .permission(ProNounsPermission.LIST.key) // Assuming a LIST permission, will define later
                .handler(ctx -> execute(ctx.getSender()));
    }
}
