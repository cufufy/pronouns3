package net.cufufy.pronouns.common.cmd;

import cloud.commandframework.Command;
import cloud.commandframework.meta.CommandMeta;
import net.cufufy.pronouns.common.ProNouns;
import net.cufufy.pronouns.common.platform.CommandSender;
import net.cufufy.pronouns.common.platform.Platform;

public class VersionCommand implements ProNounsCommand {
    private final ProNouns plugin;
    private final Platform platform;

    public VersionCommand(ProNouns plugin, Platform platform) {
        this.plugin = plugin;
        this.platform = platform;
    }
    @Override
    public Command.Builder<CommandSender> build(Command.Builder<CommandSender> builder) {
        return builder.literal("version")
                .meta(CommandMeta.DESCRIPTION, CommandUtils.description("version"))
                .handler(ctx -> ctx.getSender().sendMessage(
                plugin.formatter().translated("pronouns.command.version",
                        platform.currentVersion(),
                        platform.name()
                )
        ));
    }
}
