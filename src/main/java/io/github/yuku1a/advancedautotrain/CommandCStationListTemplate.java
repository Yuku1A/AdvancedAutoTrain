package io.github.yuku1a.advancedautotrain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

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
            case "list" -> list(sender, args);
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "removet" -> removet(sender, args);
            case "copy" -> copy(sender, args);
            case "insert" -> insert(sender, args);
            case "replace" -> replace(sender, args);
            default -> help(sender);
        };
    }

    // replaceコマンド
    private boolean replace(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、パラメータ指定で4つ、インデックス含め9つ
        if (args.length != 7) {
            return commandsHelp(
                sender,
                "cslt replace <template>  <section> <speed> <delay> <name> <index>"
            );
        }

        // テンプレート指定
        var list = store.get(args[1]);
        if (isNullList(sender, list))
            return true;

        // インデックスのチェック
        var index = tryParseIndex(sender, list, args[6]);
        if (index == -1)
            return true;

        // info生成
        var info = infoFromStrings(args);

        // 置き換え
        list.set(index, info);

        // おわり
        sender.sendMessage("項目の置き換えが完了しました。");
        return true;
    }

    // insertコマンド
    private boolean insert(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、パラメータ指定で4つ、インデックス含め9つ
        if (args.length != 7) {
            return commandsHelp(
                sender,
                "cslt insert <template> <section> <speed> <delay> <name> <index>"
            );
        }

        // テンプレート指定
        var list = store.get(args[1]);
        if (isNullList(sender, list))
            return true;

        // インデックスのチェック
        var index = tryParseIndex(sender, list, args[6]);
        if (index == -1)
            return true;

        // info生成
        var info = infoFromStrings(args);

        // 挿入
        list.add(index, info);

        // おわり
        sender.sendMessage("項目の挿入が完了しました。");
        return true;
    }

    // copyコマンド
    private boolean copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3)
            return commandsHelp(sender, "cslt copy <from> <to>");

        // 普通にコピー
        var from = store.get(args[1]);

        // nullチェック
        if (isNullList(sender, from))
            return true;

        // storeへset
        store.put(args[2], new ArrayList<>(from));

        // おわり
        sender.sendMessage("コピーが完了しました。");
        return true;
    }

    // listコマンド
    private boolean list(CommandSender sender, String[] args) {
        // コマンドで1つ、ページで1つまで
        if (args.length > 2)
            return commandsHelp(sender, "cslt list <page>");

        // storeからキーのコレクションを取得する
        var rawlist = store.keysList();

        // 数が少なければそのまま表示
        if (rawlist.size() < 16){
            sender.sendMessage("----- template list -----");
            rawlist.forEach(sender::sendMessage);
            return true;
        }

        String index;
        // 指定がなかったらインデックスを1として扱う
        if (args.length == 1)
            index = "1";
        else
            index = args[1];

        // ページングを丸投げ
        var list = CommandUtil.pager(sender, rawlist, index);

        // listがnullだったら警告文とかも出てるのでおわり
        if (list == null)
            return true;

        // 分割されたやつを表示
        sender.sendMessage(
            "----- template list page " +
            index + " of " + CommandUtil.calcMaxPageIndex(rawlist) +
            " -----"
        );
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
        if (isNullList(sender, list))
            return true;

        // 検査
        var index = tryParseIndex(sender, list, args[2]);

        // ひっかかってたら弾く
        if (index == -1)
            return true;

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
        // コマンド指定で1つ、テンプレート指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3))
            return commandsHelp(sender, "cslt view <template> <page>");

        // 名前を出しておく
        var templatename = args[1];

        // とりあえず取りに行く
        var rawlist = store.get(templatename);

        // 登録されてないときにnullが返ってくる
        if (isNullList(sender, rawlist))
            return true;

        // 数が少なければそのまま表示
        if (rawlist.size() < 16){
            infoView(sender, templatename, rawlist, -1, 0);
            return true;
        }

        String indexstr;
        // 指定がなかったらインデックスを1として扱う
        if (args.length == 2)
            indexstr = "1";
        else
            indexstr = args[2];

        // インデックスをパース
        int index;
        try {
            index = Integer.parseUnsignedInt(indexstr) - 1;
        } catch (Exception e) {
            sender.sendMessage("ページ番号は1以上の数値で指定してください。");
            return true;
        }

        // ページングを丸投げ
        var list = CommandUtil.pager(sender, rawlist, index);

        // listがnullだったら警告文とかも出てるのでおわり
        if (list == null)
            return true;

        // 分割されたやつを表示
        infoView(sender, templatename, list, index, CommandUtil.calcMaxPageIndex(rawlist));

        // おわり
        return true;
    }

    // infoを表示する用
    private void infoView(CommandSender sender, String name, List<CStationInfo> list,
                          int pageindex, int maxpageindex) {
        // indexが-1だったらページ指定がないことにする
        if (pageindex == -1)
            sender.sendMessage("----- " + name + " template content -----");
        else {
            sender.sendMessage(
                "----- " + name + " template content page " +
                (pageindex + 1) + " of " + maxpageindex +
                " -----"
            );
        }


        // うまいこと内容を表示する
        sender.sendMessage("(index) (name) (line2) (line3) (line4)");

        // indexとともに内容を表示
        int index;
        for (int i = 0 ; i < list.size() ; i++){
            if (pageindex != -1)
                index = CommandUtil.calcPagingIndex(i, pageindex);
            else
                index = i;
            sender.sendMessage(
                index + " | " +
                list.get(i).getName() + " | " +
                list.get(i).getSignText()[0] + " | " +
                list.get(i).getSignText()[1] + " | " +
                list.get(i).getSignText()[2]);
        }
    }

    // addコマンド
    private boolean add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、パラメータが4つで計8つ
        if (args.length != 6) {
            return commandsHelp(
                sender,
                "cslt add <template> <section> <speed> <delay> <name>"
            );
        }

        // テンプレート指定
        String template = args[1];

        // CStationInfoを生成する
        var info = infoFromStrings(args);

        // templateを取り出していじる
        var list = store.get(template);
        if (list == null) {
            list = new ArrayList<>();
            store.put(template, list);
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

    // 生成用
    private CStationInfo infoFromStrings(String[] args) {
        // 取り出し、型変換
        String section = args[2];
        String speed = args[3];
        String delay = args[4];
        String name = args[5];

        // stationのsignを生成する
        var line2 = "station " + section;
        var line3 = delay;
        var line4 = "route continue " + speed;
        var lines = new String[]{line2, line3, line4};

        // CStationInfoを生成して返す
        return new CStationInfo(name, lines);
    }

    /**
     * 正常であればindexそのまま、不正だったら-1が返ってくる
     */
    private int tryParseIndex(CommandSender sender, List<CStationInfo> list, String strindex) {
        // インデックスがintに変換できることを確認
        int index;
        try {
            index = Integer.parseUnsignedInt(strindex);
        } catch (NumberFormatException ignored) {
            sender.sendMessage("インデックスは数値である必要があります。");
            return -1;
        }

        // インデックスが範囲外であれば処理をしない
        if (index >= list.size()) {
            sender.sendMessage("インデックスが範囲外です。");
            return -1;
        }

        // 変換できた結果をreturn
        return index;
    }

    // チェック用
    private boolean isNullList(CommandSender sender, List<CStationInfo> list) {
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return true;
        }
        return false;
    }

    // 各コマンド用のヘルプをちょっと楽に実装する
    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage(
            "usage:",
            usage);
        return true;
    }

    // プラグインが生成する用
    public CommandCStationListTemplate(Advancedautotrain plugin) {
        store = plugin.getCStationListTemplateStore();
    }
}
