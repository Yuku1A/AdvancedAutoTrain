package io.github.yuku1a.advancedautotrain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * cstationlisttemplate、csltコマンドを実装するクラス
 */
public class CommandCStationListTemplate implements CommandExecutor {

    private final String csInfoArgText = "<acceleration> <speed> <delay> <name> [announce...]";

    // addコマンド
    private boolean add(CommandSender sender, String[] args) {
        // 引数のヘルプ用のテキスト
        var argtext = "<template> <acceleration> <speed> <delay> <name> [announce]";

        if (args[0].equals("add")) {
            // コマンド指定で1つ、テンプレート指定で1つ、パラメータが4つで計6つ、オプションありで7つ
            if ((6 > args.length) || (args.length > 7)) {
                return CommandUtil.commandsHelp(
                    sender,
                    "cslt add " + argtext
                );
            }
        } else if(args[0].equals("replace") || args[0].equals("insert")) {
            // コマンド指定で1つ、テンプレート指定で1つ、パラメータが4つとインデックスで計7つ、オプションありで8つ
            if ((7 > args.length) || (args.length > 8)) {
                return CommandUtil.commandsHelp(
                    sender,
                    "cslt " + args[0] + " " + argtext + " <index>"
                );
            }
        } else {
            return false;
        }

        // テンプレート指定
        var template = args[1];

        // 取り出し、型変換
        var section = args[2];
        var speed = args[3];
        var delay = args[4];
        var name = args[5];

        // stationのsignを生成する
        var line2 = "station " + section;
        var line3 = delay;
        var line4 = "route continue " + speed;
        var lines = new String[]{line2, line3, line4};

        // announceとindexを取り出す
        String announce;
        // addの7個ある場合のみ6にannounceがある
        if ((args.length == 7) && (args[0].equals("add")))
            announce = args[6];
            // それ以外は8個ある場合のみ6にannounceがある(7個だとそこにindexがある)
        else if(args.length == 8)
            announce = args[6];
        else
            announce = null;

        // CStationInfoを生成する
        var info = new CStationInfo(name, lines, announce);

        // テンプレートがちゃんとあるのを確認する
        // テンプレート指定
        var list = store.get(args[1]);
        // チェック用
        // add以外の操作だと元リストがないと意味がなくはある
        if (!args[0].equals("add") && list == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return true;
        }
        // addかつnullだったら新しく作って登録する(取り消すことはない・・・たぶん・・・
        else if (list == null) {
            list = new ArrayList<>();
            store.put(template, list);
        }

        // add以外だとindexが存在するので読み取る
        int index = -1;

        if (!args[0].equals("add")) {
            index = CommandUtil.tryParseIndex(sender, list, args[args.length - 1]);
            // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
            if (index == -1)
                return true;
        }

        // addコマンドだとそのままinfoをlistへ追加、そのまま完了
        switch (args[0]) {
            case "add" -> {
                list.add(info);
                sender.sendMessage("要素の追加を完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, info, list.size() - 1);
            }
            case "replace" -> {
                list.set(index, info);
                sender.sendMessage("項目の置き換えが完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, info, index);
            }
            case "insert" -> {
                list.add(index, info);
                sender.sendMessage("項目の挿入が完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, info, index);
            }
        }

        // 終わり
        return true;
    }

    private boolean insert(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ、インデックスで1つ、パラメータが4つ
        if (args.length < 7)
            return commandsHelp(sender, "insert <template> <index> " + csInfoArgText);

        // テンプレート指定
        var template = args[1];

        // テンプレートの取得
        var list = store.get(template);
        if (list == null) {
            msgTemplateNotFound(sender);
            return true;
        }

        // インデックス取得
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);
        // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
        if (index == -1)
            return true;

        // 引数のなかからCStationInfoの情報を抜き出す
        var csInfoParam = Arrays.copyOfRange(args, 3, args.length);

        // CStationInfoを生成
        var info = parseCSInfo(csInfoParam);

        // CStationInfoを挿入
        list.add(index, info);

        // 完了メッセージ
        sender.sendMessage("要素の挿入を完了しました。");
        infoViewOne(sender, info, index);

        return true;
    }

    /**
     * コマンド引数として入力される情報をパースして変換
     * @param args コマンド引数のうちCStationInfoに関連する部分<br>
     *             (acceleration) (speed) (delay) (name) [announce...] の順番
     * @return 完成したCStationInfo
     */
    private CStationInfo parseCSInfo(String[] args) {
        // 取り出し、型変換
        var section = args[0];
        var speed = args[1];
        var delay = args[2];
        var name = args[3];

        // stationのsignを生成する
        var line2 = "station " + section;
        var line4 = "route continue " + speed;
        var lines = new String[]{line2, delay, line4};

        // announceを取り出す
        StringBuilder announce;
        if (args.length > 4) {
            announce = new StringBuilder();
            for (int i = 4 ; i < args.length ; i++) {
                // 2回目以降は空白を追加する
                if (!announce.isEmpty())
                    announce.append(" ");
                announce.append(args[i]);
            }
        }
        else
            announce = null;

        // CStationInfoを生成する
        CStationInfo info;
        if (announce != null)
            info = new CStationInfo(name, lines, announce.toString());
        else
            info = new CStationInfo(name, lines, null);
        return info;
    }

    // removeコマンド
    private boolean remove(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、インデックスで1つ
        if (args.length != 3)
            return CommandUtil.commandsHelp(sender, "cslt remove <template> <index>");

        // 指定されたテンプレートを取得
        var list = store.get(args[1]);

        // nullだと存在しない
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return true;
        }

        // 検査
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // ひっかかってたら弾く
        if (index == -1)
            return true;

        // 実際の削除処理
        list.remove(index);
        sender.sendMessage("テンプレート " + args[1] + " の " + index + "番目の項目が削除されました。");

        // おわり
        return true;
    }

    // viewコマンド
    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3))
            return CommandUtil.commandsHelp(sender, "cslt view <template> <page>");

        // 名前を出しておく
        var templatename = args[1];

        // とりあえず取りに行く
        var rawlist = store.get(templatename);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (rawlist == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return true;
        }

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

    // infoコマンド
    private boolean info(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、インデックス指定で計3つ
        if (args.length != 3)
            return CommandUtil.commandsHelp(sender, "cslt info <template> <index>");

        // 名前を出しておく
        var templatename = args[1];

        // とりあえず取りに行く
        var list = store.get(templatename);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return true;
        }

        // インデックスをパース
        int index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // -1だと機能しない値かつちゃんとメッセージが出てるので蹴る
        if (index == -1)
            return true;

        // 表示
        infoViewOne(sender, list.get(index), index);

        // おわり
        return true;
    }

    // info(コマンドではない)を表示する用
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
        sender.sendMessage("(index) (name) (accel) (delay) (speed) (announce)");

        // indexとともに内容を表示
        int index;
        for (int i = 0 ; i < list.size() ; i++){
            if (pageindex != -1)
                index = CommandUtil.calcPagingIndex(i, pageindex);
            else
                index = i;
            infoViewSimple(sender, list.get(i), index, true);
        }
    }

    private void infoViewSimple(CommandSender sender, CStationInfo info, int index, boolean announcecut) {
        if (info.getSignText() == null) {
            sender.sendMessage(index + " | " + "broken data");
        }
        var accel = info.getSignText()[0].replace("station ", "");
        var speed = info.getSignText()[2].replace("route continue ", "");
        var announce = info.getAnnounce();
        if (announce == null)
            announce = "";
        else if (announcecut && announce.length() >= 10)
            announce = announce.substring(0, 9) + "...";
        sender.sendMessage(
            index + " | " +
                info.getName() + " | " +
                accel + " | " +
                // これはdelay
                info.getSignText()[1] + " | " +
                speed + " | " +
                announce
        );
    }

    // infoを一つだけ表示する用
    private void infoViewOne(CommandSender sender, CStationInfo info, int index) {
        // うまいこと内容を表示する
        sender.sendMessage("(index) (name) (accel) (delay) (speed) (announce)");
        infoViewSimple(sender, info, index, false);
    }

    private void msgTemplateNotFound(CommandSender sender) {
        sender.sendMessage("指定された名前のテンプレートは登録されていません。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // パーミッションチェック
        if (!sender.hasPermission(plugin.UsePermission)) {
            sender.sendMessage("必要な権限がありません");
            return true;
        }
        // 引数0の場合はコマンドが指定されていない
        if (args.length == 0)
            return help(sender);

        // 管理用コマンドだけ追加の権限チェック
        switch (args[0]) {
            case "load","save" -> {
                if (!sender.hasPermission(plugin.AdminPermission)){
                    sender.sendMessage("必要な権限がありません");
                    return true;
                }
            }
        }

        // コマンドごとに分岐やる、この構文めちゃ便利
        return switch (args[0]) {
            case "list" -> list(sender, args);
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            case "add","replace" -> add(sender, args);
            case "insert" -> insert(sender, args);
            case "remove" -> remove(sender, args);
            case "removet" -> removet(sender, args);
            case "copy" -> copy(sender, args);
            case "info" -> info(sender, args);
            default -> help(sender);
        };
    }

    // ボイラープレートじみたコード類
    // listコマンド
    private boolean list(CommandSender sender, String[] args) {
        // コマンドで1つ、ページで1つまで
        if (args.length > 2)
            return CommandUtil.commandsHelp(sender, "cslt list <page>");

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

    // removetコマンド
    private boolean removet(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ
        if (args.length != 2)
            return CommandUtil.commandsHelp(sender, "cslt removet <template>");

        // 指定されたテンプレートを削除
        store.remove(args[1]);
        sender.sendMessage(
            "指定されたテンプレート " + args[1] +
                " は、削除されました。"
        );
        return true;
    }

    // copyコマンド
    private boolean copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3)
            return CommandUtil.commandsHelp(sender, "cslt copy <from> <to>");

        // 普通にコピー
        var from = store.get(args[1]);

        // nullチェック
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (from == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return true;
        }

        // storeへset
        store.put(args[2], new ArrayList<>(from));

        // おわり
        sender.sendMessage("コピーが完了しました。");
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
            "copy: テンプレートのコピーを行います",
            "replace: テンプレート内の指定された項目を入れ替えます",
            "insert: テンプレート内の指定された位置に項目を追加します"
        );
        if (sender.hasPermission(plugin.AdminPermission)) {
            sender.sendMessage(
                "save: 全ての情報を保存します",
                "load: 全ての情報の再読み込みを行います"
            );
        }
        return false;
    }

    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage; ",
                           LABEL + " " + usage);
        return true;
    }

    public static final String LABEL = "cslt";

    private final CStationListTemplateStore store;
    private final Advancedautotrain plugin;

    // プラグインが生成する用
    public CommandCStationListTemplate(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getCStationListTemplateStore();
    }
}
