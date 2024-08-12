package io.github.yuku1a.advancedautotrain.utils.commands;

import org.bukkit.command.CommandSender;

/**
 * よく使われるメッセージ集
 */
public class CommonMessage {
    /**
     * 「権限がありません」
     * @param sender CommandSender
     */
    public static void dontHavePermission(CommandSender sender) {
        sender.sendMessage("権限がありません");
    }
}
