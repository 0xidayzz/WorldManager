package fr.oxidayzz.world.util;

import org.bukkit.command.CommandSender;

/**
 * Shared message formatting utilities to avoid scattered color-code duplication.
 */
public final class MessageUtil {

    public static final String PREFIX_INFO = "\u00A76";
    public static final String PREFIX_SUCCESS = "\u00A7a\u00A7l";
    public static final String PREFIX_ERROR = "\u00A7c";
    public static final String PREFIX_WARN = "\u00A7e";
    public static final String COLOR_HIGHLIGHT = "\u00A7b";
    public static final String COLOR_GRAY = "\u00A77";

    private MessageUtil() {}

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(PREFIX_ERROR + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(PREFIX_SUCCESS + message);
    }

    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(PREFIX_INFO + message);
    }

    public static void sendWorldNotFound(CommandSender sender) {
        sendError(sender, "Monde introuvable.");
    }
}
