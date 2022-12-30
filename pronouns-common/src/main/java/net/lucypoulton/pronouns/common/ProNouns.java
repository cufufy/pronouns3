package net.lucypoulton.pronouns.common;

import net.kyori.adventure.translation.GlobalTranslator;
import net.lucypoulton.pronouns.api.ProNounsPlugin;
import net.lucypoulton.pronouns.api.PronounStore;
import net.lucypoulton.pronouns.api.impl.PronounParser;
import net.lucypoulton.pronouns.common.cmd.*;
import net.lucypoulton.pronouns.common.message.Formatter;
import net.lucypoulton.pronouns.common.message.ProNounsTranslations;
import net.lucypoulton.pronouns.common.platform.Platform;
import net.lucypoulton.pronouns.common.store.StoreFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ProNouns implements ProNounsPlugin {

    private final PronounParser parser;
    private final Platform platform;
    private PronounStore store;
    private @Nullable
    final UpdateChecker checker;

    private final Formatter formatter = new Formatter();

    public ProNouns(Platform platform) {
        this.platform = platform;

        this.parser = new PronounParser(() -> store.predefined().get());

        GlobalTranslator.translator().addSource(ProNounsTranslations.registry());
        final var commandManager = platform.commandManager();

        final var commands = List.of(
                new GetCommand(this, platform),
                new SetCommand(this, platform),
                new ClearCommand(this, platform),
                new VersionCommand(this, platform),
                new DebugCommand(this, platform),
                new UpdateCommand(this),
                new HelpCommand(this, commandManager)
        );

        for (final var command : commands) {
            commandManager.command(
                    commandManager.commandBuilder("pronouns", "pn").apply(command::build)
            );
        }

        if (platform.config().checkForUpdates()) {
            checker = new UpdateChecker(this, platform);
            checker.checkForUpdates(false);
        } else {
            checker = null;
            platform.logger().warning(ProNounsTranslations.translate("pronouns.update.disabled"));
        }
    }

    public void createStore(StoreFactory store) {
        this.store = store.create("nbt", platform);
    }

    @Override
    public PronounStore store() {
        return this.store;
    }

    @Override
    public PronounParser parser() {
        return parser;
    }

    public Formatter formatter() {
        return formatter;
    }

    public Optional<UpdateChecker> updateChecker() {
        return Optional.ofNullable(checker);
    }
}
