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
            return help(sender);
        return switch (args[0]) {
            case "list" -> list(sender);
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "removet" -> removet(sender, args);
            case "copy" -> copy(sender, args);
            default -> help(sender);
        };
    }

    // copyコマンド
    private boolean copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3)
            return commandsHelp(sender, "cslt copy <from> <to>");

        // 普通にコピー
        var from = store.get(args[1]);
        store.set(args[2], new ArrayList<>(from));

        // おわり
        return true;
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

    // removeコマンド
    private boolean remove(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、インデックスで1つ
        if (args.length != 3)
            return commandsHelp(sender, "cslt remove <template> <index>");

        // 指定されたテンプレートを取得
        var list = store.get(args[1]);

        // nullだと存在しない
        if (list == null) {
            sender.sendMessage("指定されたテンプレートは存在しません。");
            return true;
        }

        // インデックスがintに変換できることを確認
        int index;
        try {
            index = Integer.parseUnsignedInt(args[2]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage("インデックスは数値である必要があります。");
            return true;
        }

        // インデックスが範囲外であれば処理をしない
        if (index >= list.size()) {
            sender.sendMessage("インデックスが範囲外です。");
            return true;
        }

        // 実際の削除処理
        list.remove(index);
        sender.sendMessage("テンプレート " + args[1] + " の " + index + "番目の項目が削除されました。");

        // おわり
        return true;
    }

    // removetコマンド
    private boolean removet(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ
        if (args.length != 2)
            return commandsHelp(sender, "cslt removet <template>");

        // 指定されたテンプレートを削除
        store.remove(args[1]);
        sender.sendMessage(
            "指定されたテンプレート " + args[1] +
            " は、削除されました。"
        );
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

    // addコマンド
    private boolean add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、パラメータが6つで計8つ
        if (args.length != 8) {
            return commandsHelp(
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
        sender.sendMessage(
            "CStationに対する動作のテンプレートを管理します",
            "利用可能なコマンド: ",
            "add: 項目を追加します",
            "remove: 項目を削除します",
            "view: 指定されたテンプレートの項目を表示します",
            "list: テンプレートの一覧を表示します",
            "removet: 指定したテンプレートを削除します",
            "save: 全ての情報を保存します",
            "load: 全ての情報の再読み込みを行います",
            "copy: テンプレートのコピーを行います",
            "replace: テンプレート内の指定された項目を入れ替えます",
            "insert: テンプレート内の指定された位置に項目を追加します"
        );
        return false;
    }

    // プラグインが生成する用
    public CommandCStationListTemplate(Advancedautotrain plugin) {
        store = plugin.getCStationListTemplateStore();
    }
}
