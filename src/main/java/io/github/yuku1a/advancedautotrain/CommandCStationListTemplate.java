package io.github.yuku1a.advancedautotrain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * cstationlisttemplate、csltコマンドを実装するクラス
 */
public class CommandCStationListTemplate implements CommandExecutor {
    private final CStationListTemplateStore store;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 引数0の場合はコマンドが指定されていない
        if (args.length == 0)
            return false;
        return switch (args[0]) {
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            default -> help(sender);
        };
    }

    // viewコマンド
    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ
        if (args.length != 2)
            return viewHelp(sender);

        // とりあえず取りに行く
        var list = store.get(args[1]);

        // 登録されてないときにnullが返ってくる
        if (list == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return true;
        }

        // うまいこと内容を表示する
        sender.sendMessage(
            "----- " + args[1] + " template content -----",
            "(index) (name) (line2) (line3) (line4) (eject) (block)");

        // indexとともに内容を表示
        for (int i = 0 ; i < list.size() ; i++){
            sender.sendMessage(
                i + " | " +
                list.get(i).getName() + " | " +
                list.get(i).getSignText()[0] + " | " +
                list.get(i).getSignText()[1] + " | " +
                list.get(i).getSignText()[2] + " | " +
                list.get(i).isEjectPassenger() + " | " +
                list.get(i).isBlockPassenger());
        }

        return true;
    }

    // viewコマンドのヘルプ
    private boolean viewHelp(CommandSender sender) {
        sender.sendMessage(
            "usage: ",
            "cslt view <template>"
        );
        return true;
    }

    // saveコマンド
    private boolean save(CommandSender sender) {
        if (store.save())
            sender.sendMessage("Save Successful!");
        else
            sender.sendMessage("Save Failed");
        return true;
    }

    // loadコマンド
    private boolean load(CommandSender sender) {
        if (store.load())
            sender.sendMessage("Data Loaded");
        else
            sender.sendMessage("Data Load Failed");
        return true;
    }

    // helpコマンド
    private boolean help(CommandSender sender) {
        return true;
    }

    // プラグインが生成する用
    public CommandCStationListTemplate(Advancedautotrain plugin) {
        store = plugin.getCStationListTemplateStore();
    }
}
