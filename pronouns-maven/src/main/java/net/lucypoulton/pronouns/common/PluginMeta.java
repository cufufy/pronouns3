package net.lucypoulton.pronouns.common;

import net.lucypoulton.pronouns.common.platform.Platform;
import net.lucypoulton.pronouns.common.util.PropertiesUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public class PluginMeta {
    private final boolean isFirstRun;
    private final String lastPluginVersion;
    private final Platform platform;
    private final Path filePath;

    private void save() {
        try (final var stream = Files.newOutputStream(filePath)) {
            final var props = new Properties();
            props.put("lastPluginVersion", platform.currentVersion());
            props.store(stream, """
                    ProNouns meta file.
                    Do not edit!
                    """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PluginMeta(final Platform platform) {
        this.platform = platform;
        filePath = platform.dataDir().resolve("pronouns-meta.cfg");
        try {
            if (Files.exists(filePath)) {
                final var fileData = PropertiesUtil.fromFile(filePath);
                final var lastPluginVersion = (String) fileData.get("lastPluginVersion");
                if (lastPluginVersion == null) {
                    platform.logger().warn("""
                    Meta file has been tampered with!
                    pronouns-meta.cfg is not intended for editing.
                    Recreating it now.""");
                    // Recreate the file if lastPluginVersion is missing
                    this.lastPluginVersion = platform.currentVersion();
                    this.isFirstRun = true;
                    save();
                    return;
                }

                this.lastPluginVersion = lastPluginVersion;
                this.isFirstRun = false;
                // Update lastPluginVersion if it's different from current
                if (!this.lastPluginVersion.equals(platform.currentVersion())) {
                    save();
                }
                return;
            }
            lastPluginVersion = platform.currentVersion();
            this.isFirstRun = true;
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String lastPluginVersion() {
        return lastPluginVersion;
    }

    public boolean isFirstRun() {
        return isFirstRun;
    }
}
