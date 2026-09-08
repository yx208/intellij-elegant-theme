package com.github.yx208.eleganttheme;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.cl.PluginAwareClassLoader;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shows a one-time release-highlights notification after the plugin is installed or updated.
 *
 * Compatibility: since-build is 223, so only APIs present in both 2022.3 and current platforms
 * may be used. The plugin's own descriptor is read off this class's PluginAwareClassLoader —
 * the PluginManagerCore/PluginManager descriptor lookups are all ApiStatus.Internal and are
 * rejected by Marketplace compatibility verification.
 *
 * The notification body is the first &lt;p&gt; of the change-notes in plugin.xml — put the
 * release headline there and keep it free of &lt;a&gt; links (they are not clickable here).
 * The "Full release notes" action derives its URL from the plugin version, so
 * _docs/release-&lt;version&gt;.en.md must exist on main when a version ships.
 */
public final class UpdateNotificationActivity implements StartupActivity.DumbAware {

    private static final String LAST_NOTIFIED_VERSION_KEY = "elegant.theme.last.notified.version";
    private static final String RELEASE_NOTES_URL_TEMPLATE =
            "https://github.com/yx208/elegant-theme/blob/main/_docs/release-%s.en.md";
    private static final Pattern FIRST_PARAGRAPH =
            Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @Override
    public void runActivity(@NotNull Project project) {
        PluginDescriptor plugin = findOwnDescriptor();
        if (plugin == null || plugin.getVersion() == null) {
            return;
        }
        String version = plugin.getVersion();
        String previousVersion = takePreviousVersionOnce(version);
        if (version.equals(previousVersion)) {
            return;
        }
        String title = previousVersion == null
                ? "Welcome to Elegant Theme " + version
                : "Elegant Theme updated to " + version;
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Elegant Theme Updates")
                .createNotification(title, releaseHighlights(plugin), NotificationType.INFORMATION)
                .addAction(NotificationAction.createSimple("Full release notes",
                        () -> BrowserUtil.browse(String.format(RELEASE_NOTES_URL_TEMPLATE, version))))
                .notify(project);
    }

    @Nullable
    private static PluginDescriptor findOwnDescriptor() {
        ClassLoader classLoader = UpdateNotificationActivity.class.getClassLoader();
        return classLoader instanceof PluginAwareClassLoader
                ? ((PluginAwareClassLoader) classLoader).getPluginDescriptor()
                : null;
    }

    /**
     * Marks the current version as notified and returns what was stored before; synchronized so
     * that projects opening concurrently at IDE start produce a single notification.
     */
    @Nullable
    private static synchronized String takePreviousVersionOnce(@NotNull String currentVersion) {
        PropertiesComponent properties = PropertiesComponent.getInstance();
        String previous = properties.getValue(LAST_NOTIFIED_VERSION_KEY);
        if (!currentVersion.equals(previous)) {
            properties.setValue(LAST_NOTIFIED_VERSION_KEY, currentVersion);
        }
        return previous;
    }

    @NotNull
    private static String releaseHighlights(@NotNull PluginDescriptor plugin) {
        String changeNotes = plugin.getChangeNotes();
        if (changeNotes == null) {
            return "See the plugin page for what's new in this release.";
        }
        Matcher paragraph = FIRST_PARAGRAPH.matcher(changeNotes);
        return paragraph.find() ? paragraph.group(1).trim() : changeNotes.trim();
    }
}
