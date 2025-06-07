package net.lucypoulton.pronouns.api;

import java.util.concurrent.ScheduledExecutorService;

/**
 * The entry point for the plugin.
 */
public interface ProNounsPlugin {
    /**
     * Gets a pronoun parser that is aware of the predefined sets from {@link #store()}.
     */
    PronounParser parser();

    /**
     * Gets the pronoun store.
     */
    PronounStore store();

    /**
     * Gets the plugin's main scheduled executor service.
     */
    ScheduledExecutorService executorService();
}
