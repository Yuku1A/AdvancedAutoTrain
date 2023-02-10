package io.github.yuku1a.advancedautotrain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

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
            case "list" -> list(sender);
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            case "add" -> add(sender, args);
            default -> help(sender);
        };
    }

    // listコマンド
    private boolean list(CommandSender sender) {
        // storeからキーのコレクションを取得する
        var list = store.getKeySet();

        // それを全部出力
        sender.sendMessage("----- template list -----");
        list.forEach(sender::sendMessage);

        // おわり
        return true;
    }

    // viewコマンド
    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ
        if (args.length != 2)
            return commandsHelp(sender, "cslt view <template>");

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

    // addコマンド
    private boolean add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、パラメータが6つで計8つ
        if (args.length != 8) {
            commandsHelp(
                sender,
                "cslt add <template> <blockpassenger> <eject> <section> <speed> <delay> <name>"
            );
        }

        // 順番に気をつけて
        String template = args[1];
        var block = Boolean.parseBoolean(args[2]);
        var eject = Boolean.parseBoolean(args[3]);
        String section = args[4];
        String speed = args[5];
        String delay = args[6];
        String name = args[7];

        // stationのsignを生成する
        var line2 = "station " + section;
        var line3 = delay;
        var line4 = "route continue " + speed;
        var lines = new String[]{line2, line3, line4};

        // CStationInfoを生成する
        var info = new CStationInfo(name, lines, eject, block);

        // templateを取り出していじる
        var list = store.get(template);
        if (list == null) {
            list = new ArrayList<>();
            store.set(template, list);
        }

        // infoをlistへ追加
        list.add(info);

        // 完了
        sender.sendMessage("Add Successful!");
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

    // 各コマンド用のヘルプをちょっと楽に実装する
    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage(
            "usage:",
            usage);
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
