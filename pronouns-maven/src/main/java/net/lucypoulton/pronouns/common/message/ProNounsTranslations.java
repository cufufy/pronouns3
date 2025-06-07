package net.lucypoulton.pronouns.common.message;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.TranslationRegistry;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

public class ProNounsTranslations {
    private ProNounsTranslations() {
    }

    public static final Key PRONOUNS_TRANSLATIONS = Key.key("pronouns", "translations");
    private static final TranslationRegistry registry = TranslationRegistry.create(PRONOUNS_TRANSLATIONS);

    static {
        try (final var stream = ProNounsTranslations.class.getResourceAsStream("/lang/en_us.properties")) {
            final var props = new Properties();
            props.load(stream);
            for (final var entry : props.entrySet()) {
                registry.register(
                        (String) entry.getKey(),
                        Locale.US,
                        new MessageFormat((String) entry.getValue())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TranslationRegistry registry() {
        return registry;
    }

    public static String translate(String key, Object... args) {
        final var format =  registry().translate(key, Locale.ROOT);
        return format != null ? format.format(args) : key;
    }
}
