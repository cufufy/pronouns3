package net.cufufy.pronouns.common.cmd;

import cloud.commandframework.Command;
import net.cufufy.pronouns.common.platform.CommandSender;

public interface ProNounsCommand {
    Command.Builder<CommandSender> build(Command.Builder<CommandSender> builder);

    default boolean shouldRegister() {
        return true;
    }
}
