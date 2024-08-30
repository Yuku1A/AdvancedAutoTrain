package io.github.yuku1a.advancedautotrain.dump;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CStationInfo;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.HashSet;
import java.util.List;

public class CommandTrainRelation implements TabExecutor {
    /**
     * CStationInfoの追加関連のコマンドのヘルプ用テキスト
     */
    private final String csInfoArgText = "<acceleration> <speed> <delay> <name> [announce...]";

    // addコマンド
    private void add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、パラメータが1つで計3つ
        if (args.length < 3) {
            commandsHelp(sender,"add <optimer> <nameprefix>");
            return;
        }

        // リスト指定
        var optimerName = args[1];

        // リストの取得
        var list = store.get(optimerName);
        // リストが登録されていなければ情報の登録もしない
        if (list == null) {
            msgListNotFound(sender);
            return;
        }

        var trainPrefix = args[2];

        // 単に追加するのみ
        list.add(trainPrefix);

        // 終了メッセージ
        sender.sendMessage("OPTimer " + optimerName + " に関連する列車名の接頭辞として " + trainPrefix + "を登録しました");
    }

    private List<String> addTab(String[] args) {
        // インスペクション対策
        if (args.length < 2)
            return null;

        // add <optimer> <nameprefix>
        // <optimer>
        var optimerName = args[1];
        if (args.length == 2)
            return searchInStore(optimerName);

        return null;
    }

    private void create(CommandSender sender, String[] args) {
        // コマンドで1つ、名前指定で1つ
        if (args.length != 2) {
            commandsHelp(sender, "create <optimername>");
            return;
        }

        // 新規作成するリストの名前
        var name = args[1];

        // あるかどうかを確認する、すでにあったら作らない
        var list = store.get(name);
        if (list != null) {
            sender.sendMessage("その名前のリストは既に作成されています。");
            return;
        }

        // 同じ名前のものはないので新規作成する
        store.put(name, new HashSet<>());

        // 終了メッセージ
        sender.sendMessage("リスト " + name + " を新規作成しました。");
    }

    private List<String> createTab(String[] args) {
        // OPTimerに紐づけるのでそれをサジェストする
        if (args.length == 2) {
            var optimerList = plugin.getOperationTimerStore().keysList();
            return TabCompleteUtil.searchInList(args[1], optimerList);
        }

        return null;
    }


    // removeコマンド
    private void remove(CommandSender sender, String[] args) {
        // コマンドで1つ、リストで1つ、インデックスで1つ
        if (args.length != 3) {
            commandsHelp(sender, "remove <optimer> <trainprefix>");
            return;
        }

        // 指定されたリストを取得
        var optimerName = args[1];
        var list = store.get(optimerName);

        // nullだと存在しない
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            msgListNotFound(sender);
            return;
        }

        var trainPrefix = args[2];

        // 実際の削除処理
        list.remove(trainPrefix);
        sender.sendMessage("列車名の接頭辞 " + trainPrefix + " は、OPTimer " + optimerName + " と関連する列車としての登録が解除されました。");

        // おわり
    }

    private List<String> removeTab(String[] args) {
        // インスペクション対策
        if (args.length <= 1)
            return null;

        // リスト名
        var listName = args[1];
        if (args.length == 2) {
            searchInStore(listName);
        }

        // リスト内容
        if (args.length == 3) {
            var set = store.get(listName);
            return TabCompleteUtil.searchInList(args[2], List.copyOf(set));
        }

        return null;
    }

    // viewコマンド
    private void view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3)) {
            commandsHelp(sender, "view <optimer> <page>");
            return;
        }

        // 名前を出しておく
        var optimerName = args[1];

        // とりあえず取りに行く
        var rawset = store.get(optimerName);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (rawset == null) {
            sender.sendMessage("指定された名前のリストは登録されていません。");
            return;
        }

        // リストとして扱えるように変換
        var rawlist = List.copyOf(rawset);

        // ページ分割
        var pagedList = CommandUtil.pager2D(rawlist, 15);

        // ページ指定
        String indexStr = args.length == 3 ? args[2] : "1";
        var pageIndex = CommandUtil.tryParsePagingIndex(sender, pagedList, indexStr);
        if (pageIndex == -1)
            return;

        // 表示
        sender.sendMessage("--- Related Train Prefix of OPTimer " + optimerName + " ---");
        pagedList.get(pageIndex).forEach(e -> sender.sendMessage(e.getData()));
    }

    private List<String> viewTab(String[] args) {
        // 2つめのリスト指定のみサジェストする
        if (args.length != 2)
            return null;

        return searchInStore(args[1]);
    }

    private void msgListNotFound(CommandSender sender) {
        sender.sendMessage("指定された名前のリストは登録されていません。");
    }

    // ボイラープレートじみたコード類
    // listコマンド
    private void list(CommandSender sender, String[] args) {
        // コマンドで1つ、ページで1つまで
        if (args.length > 2) {
            commandsHelp(sender, "list <page>");
            return;
        }

        // storeからキーのコレクションを取得する
        var rawlist = store.keysList();

        // 数が少なければそのまま表示
        if (rawlist.size() < 16){
            sender.sendMessage("----- train relation list list -----");
            rawlist.forEach(sender::sendMessage);
            return;
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
            return;

        // 分割されたやつを表示
        sender.sendMessage(
            "----- train relation list list page " +
                index + " of " + CommandUtil.calcMaxPageIndex(rawlist) +
                " -----"
        );
        list.forEach(sender::sendMessage);

        // おわり
    }

    // removetコマンド
    private void rmlist(CommandSender sender, String[] args) {
        // コマンドで1つ、リスト指定で1つ
        if (args.length != 2) {
            commandsHelp(sender, "rmlist <list>");
            return;
        }

        // 指定されたリストを削除
        store.remove(args[1]);
        sender.sendMessage(
            "指定されたリスト " + args[1] +
                " は、削除されました。"
        );
    }

    private List<String> rmlistTab(String[] args) {
        // 2つめのリスト指定のみサジェストする
        if (args.length != 2)
            return null;

        return searchInStore(args[1]);
    }

    /**
     * 途中までの入力に合うリストを探すメソッド
     * @param query 途中まで入れられた文字列
     * @return 入力に合うリスト名のリスト
     */
    private List<String> searchInStore(String query) {
        var list = store.keysList();
        return TabCompleteUtil.searchInList(query, list);
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

        // コマンドごとに分岐やる、この構文めちゃ便利
        switch (args[0]) {
            case "list" -> list(sender, args);
            case "view" -> view(sender, args);
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "rmlist" -> rmlist(sender, args);
            case "create" -> create(sender, args);
            default -> help(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!sender.hasPermission(plugin.UsePermission))
            return null;

        // コマンドをサジェストする
        if (args.length <= 1) {
            // 管理コマンドはサジェストしなくたっていい
            return List.of("create", "add", "remove", "view", "list",
                           "rmlist");
        }

        // コマンドごとのサジェストはそれぞれのメソッドへ
        return switch (args[0]) {
            case "add" -> addTab(args);
            case "remove" -> removeTab(args);
            case "view" -> viewTab(args);
            case "rmlist" -> rmlistTab(args);
            case "create" -> createTab(args);
            default -> null;
        };
    }

    // helpコマンド
    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "OPTimerに関連する列車を登録します",
            "情報をダンプする際に量を減らすために使用できます", 
            "利用可能なコマンド: ",
            "create: リストを新規作成します",
            "add: 項目を追加します",
            "remove: 項目を削除します",
            "view: 指定されたリストの項目を表示します",
            "list: リストの一覧を表示します",
            "rmlist: 指定したリストを削除します"
        );
        return false;
    }

    private void commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage; ",
                           LABEL + " " + usage);
    }

    public static final String LABEL = "trl";

    private final TrainRelationStore store;
    private final Advancedautotrain plugin;

    // プラグインが生成する用
    public CommandTrainRelation(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getTrainRelationStore();
    }
}
