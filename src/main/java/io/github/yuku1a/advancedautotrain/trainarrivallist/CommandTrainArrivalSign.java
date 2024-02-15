package io.github.yuku1a.advancedautotrain.trainarrivallist;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandTrainArrivalSign implements CommandExecutor {
    // addコマンド
    private boolean add(CommandSender sender, String[] args) {
        // 引数のヘルプ用のテキスト
        var argtext = "<list> <displayname> [offset] [description]";

        var command = args[0];

        if (command.equals("add")) {
            // コマンド指定で1つ、リスト指定で1つ、パラメータが1つ、オプション2つで5つ
            if ((3 > args.length) || (args.length > 5)) {
                return commandsHelp(
                    sender,
                    "add " + argtext
                );
            }
        } else if(command.equals("replace") || command.equals("insert")) {
            // コマンド指定で1つ、リスト指定で1つ、パラメータが1つ、オプション2つ、インデックス1つで6つ
            if ((4 > args.length) || (args.length > 6)) {
                return commandsHelp(
                    sender,
                    args[0] + " " + argtext + " <index>"
                );
            }
        } else {
            return false;
        }

        // リスト指定
        var key = args[1];
        // リストを取ってくる
        var list = listOrNull(sender, args[1]);
        // nullだったらメッセージが出てるのでそのまま返す
        if (list == null)
            return true;

        // 取り出し、型変換
        var displayname = args[2];

        // displayname以外がオプションであるかインデックスなので慎重に調べる
        long offset;
        long defaultoffset = -2;
        String description;

        // オプションがない場合
        if ((command.equals("add") && args.length == 3) || (!(command.equals("add"))) && args.length == 4) {
            offset = defaultoffset;
            description = null;
        } else {
            // オプションがあるパターンだとindex抜きの引数がいくつあるか計算する
            int argnum;
            if (command.equals("add"))
                argnum = args.length;
            else
                argnum = args.length - 1;

            // OffSetを取り出す
            long argoffset;
            boolean arg3IsOffset;
            try {
                argoffset = Long.parseLong(args[3]);
                arg3IsOffset = true;
            } catch (NumberFormatException e) {
                if (argnum == 5) {
                    sender.sendMessage("オフセットの値が不正です。");
                    return true;
                }
                argoffset = defaultoffset;
                arg3IsOffset = false;
            }
            offset = argoffset;

            // コメントを書きたくない
            if (arg3IsOffset){
                if (argnum == 4)
                    description = null;
                else
                    description = args[4];
            }
            else {
                description = args[3];
            }
        }

        var entry = new ArrivalSignEntry(displayname, offset, description);

        // add以外だとindexが存在するので読み取る
        int index;
        if (!command.equals("add")) {
            index = CommandUtil.tryParseIndex(sender, list, args[args.length - 1]);
            // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
            if (index == -1)
                return true;
        } else
            index = -1;

        // addコマンドだとそのままinfoをlistへ追加、そのまま完了
        switch (command) {
            case "add" -> {
                list.add(entry);
                sender.sendMessage("要素の追加を完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, entry, list.size() - 1);
            }
            case "replace" -> {
                list.set(index, entry);
                sender.sendMessage("項目の置き換えが完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, entry, index);
            }
            case "insert" -> {
                list.add(index, entry);
                sender.sendMessage("項目の挿入が完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, entry, index);
            }
        }

        // 終わり
        return true;
    }

    // createコマンド
    private boolean create(CommandSender sender, String[] args) {
        // コマンドで1つ、リスト名で1つ
        if (args.length != 2)
            return commandsHelp(sender, "create <trainname>");

        // とりあえず作る
        store.put(args[1], new ArrayList<>());

        sender.sendMessage("リスト " + args[1] + " を作成しました。");

        return true;
    }

    // removeコマンド
    private boolean remove(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、インデックスで1つ
        if (args.length != 3)
            return commandsHelp(sender, "remove <listname> <index>");

        // リストを取ってくる
        var list = listOrNull(sender, args[1]);

        // nullだったらメッセージが出てるのでそのまま返す
        if (list == null)
            return true;

        // 検査
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // ひっかかってたら弾く
        if (index == -1)
            return true;

        // 実際の削除処理
        list.remove(index);
        sender.sendMessage("リスト " + args[1] + " の " + index + "番目の項目が削除されました。");

        // おわり
        return true;
    }

    // viewコマンド
    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3))
            return commandsHelp(sender, "view <template> <page>");

        // 名前を出しておく
        var listname = args[1];

        // リストを取ってくる
        var rawlist = listOrNull(sender, listname);

        // nullだったらメッセージが出てるのでそのまま返す
        if (rawlist == null)
            return true;

        // ページングを丸投げ
        var list = CommandUtil.pager2D(rawlist, 9);

        String pageindexstr;
        // 指定がなかったらインデックスを1として扱う
        if (args.length == 2)
            pageindexstr = "1";
        else
            pageindexstr = args[2];

        // インデックスをパース
        int pageindex = CommandUtil.tryParseIndex(sender, list, pageindexstr);
        // インデックスが異常だったらすでにメッセージが送られている
        if (pageindex == -1)
            return true;

        // 分割されたリストの指定されたやつ
        var pagedlist = list.get(pageindex);

        // 分割されたやつを表示するUI部分
        sender.sendMessage(
            "----- " + listname + " trainarrivallist page " + pageindexstr + " of " + list.size() + " -----"
        );
        sender.sendMessage("(index) (displayname) (description) (offset)");

        // 分割されたリストの中身を表示
        for (var entry : pagedlist) {
            infoViewSimple(sender, entry.getData(), entry.getIndex());
        }

        // おわり
        return true;
    }

    // infoを一つだけ表示する用
    private void infoViewOne(CommandSender sender, ArrivalSignEntry entry, int index) {
        // うまいこと内容を表示する
        sender.sendMessage("(index) (displayname) (description) (offset)");
        infoViewSimple(sender, entry, index);
    }

    // 内容を実際に表示する
    private void infoViewSimple(CommandSender sender, ArrivalSignEntry entry, int index) {
        sender.sendMessage(index + " | " + entry.getTrainName() + " | " +
                               entry.getTrainDescription() + " | " + entry.getSecondsOffset());
    }

    // ボイラープレートじみたコード類
    // listコマンド
    private boolean list(CommandSender sender, String[] args) {
        // コマンドで1つ、ページで1つまで
        if (args.length > 2)
            return commandsHelp(sender, "list [page]");

        // storeからキーのコレクションを取得する
        var rawlist = store.keysList();

        // 指定がなければ1ページ扱い
        String index = args.length == 1 ? "1" : args[1];

        // ページングを丸投げ
        var list = CommandUtil.pager(sender, rawlist, index);

        // listがnullだったら警告文とかも出てるのでおわり
        if (list == null)
            return true;

        // 分割されたやつを表示
        sender.sendMessage(
            "----- trainarrivallist page " +
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
            return commandsHelp(sender, "rmlist <list>");

        // 指定されたテンプレートを削除
        store.remove(args[1]);
        sender.sendMessage(
            "指定されたリスト " + args[1] +
                " は、削除されました。"
        );
        return true;
    }

    // copyコマンド
    private boolean copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3)
            return commandsHelp(sender, "copy <from> <to>");

        // リストを取ってくる
        var from = listOrNull(sender, args[1]);

        // nullだったらメッセージが出てるのでそのまま返す
        if (from == null)
            return true;

        // storeへコピーしたリストをset
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

    private List<ArrivalSignEntry> listOrNull(CommandSender sender, String key) {
        // とりあえず取りに行く
        var list = store.get(key);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            sender.sendMessage("指定された名前のリストは登録されていません。");
            return null;
        }

        return list;
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
            case "add","replace","insert" -> add(sender, args);
            case "create" -> create(sender, args);
            case "remove" -> remove(sender, args);
            case "rmlist" -> removet(sender, args);
            case "copy" -> copy(sender, args);
            default -> help(sender);
        };
    }

    // helpコマンド
    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "列車に紐づくArrivalSignのリストを管理します",
            "利用可能なコマンド: ",
            "add: 項目を追加します",
            "remove: 項目を削除します",
            "view: 指定されたリストの項目を表示します",
            "list: リストの一覧を表示します",
            "rmlist: 指定したリストを削除します",
            "copy: リストのコピーを行います",
            "replace: リスト内の指定された項目を入れ替えます",
            "insert: リスト内の指定された位置に項目を追加します"
        );
        if (sender.hasPermission(plugin.AdminPermission)) {
            sender.sendMessage(
                "save: 全ての情報を保存します",
                "load: 全ての情報の再読み込みを行います"
            );
        }
        return false;
    }

    private final TrainArrivalSignStore store;
    private final Advancedautotrain plugin;

    // プラグインが生成する用
    public CommandTrainArrivalSign(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getTrainArrivalSignStore();
    }

    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", "tal " + usage);
        return true;
    }
}
