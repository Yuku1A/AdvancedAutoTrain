package io.github.yuku1a.advancedautotrain.routeedit;

import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.pathfinding.PathNode;
import com.bergerkiller.bukkit.tc.pathfinding.RouteManager;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRouteEdit implements TabExecutor {
    // routeの編集はRouteManagerで思った通りではないにせよ一応できる
    // destinationのサジェストは、TCからPathProvider、PathWorlds、
    // PathWorld.getNodes()、PathNode.getNames()の中身がどうにかdestinationNameかもしれない

    // addコマンド
    private void add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、ルート指定で1つ、destinationが無限
        if (args.length < 3) {
            commandsHelp(sender,"add <route> <destination...>");
            return;
        }

        // ルート指定
        var routeName = args[1];

        // ルートの取得、ソースみたらunmodifiableだった
        var rawlist = routeManager.findRoute(routeName);
        // ルートが空だったら登録されていないということらしい
        if (rawlist.isEmpty()) {
            sender.sendMessage("ルート " + routeName + " を作成しました。");
        }

        // 編集可能にする
        var list = new ArrayList<>(rawlist);

        // destinationを取り出す、複数一気にいけるかつサジェスト効くように作りたい
        var destinations = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));

        // 単に追加するのみ
        list.addAll(destinations);

        // 登録しなおす
        routeManager.storeRoute(routeName, list);

        // 終了メッセージ
        sender.sendMessage("要素の追加を完了しました。");
        // 追加したものの内容を表示する
        viewUI(sender);

        // 追加した分を全部表示するために複雑化、式あってるから見直さなくていいよ
        int prevSize = list.size() - destinations.size();
        for (int i = prevSize ; i < list.size() ; i++) {
            viewOne(sender, list.get(i), i);
        }
    }

    private List<String> addTab(String[] args) {
        // インスペクション対策
        if (args.length < 2)
            return null;

        // add <route> <destination...>
        // <route>
        var routeName = args[1];
        if (args.length == 2)
            return searchInStore(routeName);

        // <destination...>
        // とにかくdestinationをサジェストする
        var destQuery = args[args.length - 1];

        return searchInDestination(destQuery);
    }

    private void insert(CommandSender sender, String[] args) {
        // コマンドで1つ、ルート指定で1つ、インデックスで1つ、destinationが1つ
        if (args.length != 4) {
            commandsHelp(sender, "insert <route> <index> <destination>");
            return;
        }

        // ルート指定
        var routeName = args[1];

        // ルートの取得、これはunmodifiable
        var rawlist = routeManager.findRoute(routeName);
        // ルートが空だったら登録されていないということらしい
        if (rawlist.isEmpty()) {
            msgRouteNotFound(sender);
            return;
        }

        // 編集可能にする
        var list = new ArrayList<>(rawlist);

        // インデックス取得
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);
        // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
        if (index == -1)
            return;

        // destinationを取り出し
        var destination = args[3];

        // destinationを挿入
        list.add(index, destination);

        // 再登録する必要がある
        routeManager.storeRoute(routeName, list);

        // 完了メッセージ
        sender.sendMessage("要素の挿入を完了しました。");

        // 挿入したものを表示
        viewOneWithUI(sender, destination, index);
    }

    private List<String> insertTab(String[] args) {
        // インスペクション対策
        if (args.length < 2)
            return null;

        // insert <route> <index> <destination>
        // <route>
        var routeName = args[1];
        if (args.length == 2)
            return searchInStore(routeName);

        // <index>
        // indexはサジェストしない
        if (args.length == 3)
            return null;

        // <destination>
        // 一つだけサジェスト
        if (args.length == 4) {
            var destQuery = args[3];
            return searchInDestination(destQuery);
        }

        return null;
    }

    private void replace(CommandSender sender, String[] args) {
        // コマンドで1つ、ルート指定で1つ、インデックスで1つ、パラメータが4つ
        if (args.length < 4) {
            commandsHelp(sender, "replace <route> <index> <destination>");
            return;
        }

        // ルート指定
        var routeName = args[1];

        // ルートの取得、これはunmodifiable
        var rawlist = routeManager.findRoute(routeName);
        // 空だったら登録されていないということらしい
        if (rawlist.isEmpty()) {
            msgRouteNotFound(sender);
            return;
        }

        // 編集可能にする
        var list = new ArrayList<>(rawlist);

        // インデックス取得
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);
        // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
        if (index == -1)
            return;

        // destinationを取り出し
        var destination = args[3];

        // destinationを置き換え
        list.set(index, destination);

        // 再登録
        routeManager.storeRoute(routeName, list);

        // 完了メッセージ
        sender.sendMessage("要素の置換を完了しました。");
        viewOneWithUI(sender, destination, index);
    }

    private List<String> replaceTab(String[] args) {
        // インスペクション対策
        if (args.length < 2)
            return null;

        // replace <route> <index> <destination>
        // <route>
        var routeName = args[1];
        if (args.length == 2)
            return searchInStore(routeName);

        // <index>
        // indexはサジェストしない
        if (args.length == 3)
            return null;

        // <destination>
        // 一つだけサジェスト
        if (args.length == 4) {
            var destQuery = args[3];
            return searchInDestination(destQuery);
        }

        return null;
    }

    private List<String> searchInDestination(String query) {
        // だいたいのすべてのdestinationsの集め方
        var pathWorlds = TC.getPathProvider().getWorlds();
        var pathNodesAll = new ArrayList<PathNode>();
        pathWorlds.forEach((e) -> pathNodesAll.addAll(e.getNodes()));
        var pathNames = new ArrayList<String>();
        pathNodesAll.forEach((e) -> pathNames.addAll(e.getNames()));

         // とりあえず試験的にこのまま出す
        return TabCompleteUtil.searchInList(query, pathNames);
    }

    // removeコマンド
    private void remove(CommandSender sender, String[] args) {
        // コマンドで1つ、ルートで1つ、インデックスで1つ
        if (args.length != 3) {
            commandsHelp(sender, "remove <route> <index>");
            return;
        }

        // ルート名取得
        var routeName = args[1];

        // 指定されたルートを取得、unmodifiable
        var rawlist = routeManager.findRoute(routeName);
        // 空だと未登録
        if (rawlist.isEmpty()) {
            msgRouteNotFound(sender);
            return;
        }

        // 編集可能にする
        var list = new ArrayList<>(rawlist);

        // 検査
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // ひっかかってたら弾く
        if (index == -1)
            return;

        // 実際の削除処理
        list.remove(index);

        // 再登録
        routeManager.storeRoute(routeName, list);

        sender.sendMessage("ルート " + routeName + " の " + index + "番目の項目が削除されました。");
    }

    private List<String> removeTab(String[] args) {
        // 2つめのルート指定のみサジェストする
        if (args.length != 2)
            return null;

        return searchInStore(args[1]);
    }

    // viewコマンド
    private void view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、ルート指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3)) {
            commandsHelp(sender, "view <route> <page>");
            return;
        }

        // 名前を出しておく
        var routeName = args[1];

        // とりあえず取りに行く
        var rawlist = routeManager.findRoute(routeName);

        // 登録されてないときに空のリストが返ってくる
        if (rawlist.isEmpty()) {
            msgRouteNotFound(sender);
            return;
        }

        // ページ分割
        var pagedList = CommandUtil.pager2D(rawlist, 15);

        String indexstr;
        // 指定がなかったらインデックスを1として扱う
        if (args.length == 2)
            indexstr = "1";
        else
            indexstr = args[2];

        // インデックスをパース
        int pageIndex = CommandUtil.tryParsePagingIndex(sender, pagedList, indexstr);

        // 異常だったらすでにメッセージが送られてるのでreturn
        if (pageIndex == -1)
            return;

        // 1ページ分
        var listPage = pagedList.get(pageIndex);

        // 表示
        sender.sendMessage("--- Route " + routeName + " page " + indexstr + " of " + pagedList.size() + " ---");
        viewUI(sender);
        for (var entry : listPage) {
            viewOne(sender, entry.getData(), entry.getIndex());
        }
    }

    private List<String> viewTab(String[] args) {
        // 2つめのルート指定のみサジェストする
        if (args.length != 2)
            return null;

        return searchInStore(args[1]);
    }

    private void viewOneWithUI(CommandSender sender, String destination, int index) {
        viewUI(sender);
        viewOne(sender, destination, index);
    }

    private void viewUI(CommandSender sender) {
        sender.sendMessage("(index) (destination)");
    }

    private void viewOne(CommandSender sender, String destination, int index) {
        sender.sendMessage(index + " | " + destination);
    }

    private void msgRouteNotFound(CommandSender sender) {
        sender.sendMessage("指定された名前のルートは登録されていません。");
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
        var rawlist = routeManager.getRouteNames();

        // 数が少なければそのまま表示
        if (rawlist.size() < 16){
            sender.sendMessage("----- route list -----");
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
            "----- route list page " +
                index + " of " + CommandUtil.calcMaxPageIndex(rawlist) +
                " -----"
        );
        list.forEach(sender::sendMessage);

        // おわり
    }

    // removetコマンド
    private void rmroute(CommandSender sender, String[] args) {
        // コマンドで1つ、ルート指定で1つ
        if (args.length != 2) {
            commandsHelp(sender, "rmroute <route>");
            return;
        }

        // ルート名
        var routeName = args[1];

        // 指定されたルートを削除
        routeManager.storeRoute(routeName, null);
        sender.sendMessage(
            "指定されたルート " + routeName +
                " は、削除されました。"
        );
    }

    private List<String> rmrouteTab(String[] args) {
        // 2つめのルート指定のみサジェストする
        if (args.length != 2)
            return null;

        return searchInStore(args[1]);
    }

    // copyコマンド
    private void copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3) {
            commandsHelp(sender, "copy <from> <to>");
            return;
        }

        // ルート名
        var routeName = args[1];

        // 取得、unmodifiable
        var from = routeManager.findRoute(routeName);

        // 空のリストの場合は未登録らしい
        if (from.isEmpty()) {
            msgRouteNotFound(sender);
            return;
        }

        // コピー先の名前
        var toRouteName = args[2];

        // コピー、よくわからないので念のためArrayListを新規に作る
        routeManager.storeRoute(toRouteName, new ArrayList<>(from));

        // おわり
        sender.sendMessage("コピーが完了しました。");
    }

    private List<String> copyTab(String[] args) {
        // 2つめのルート指定のみサジェストする
        if (args.length != 2)
            return null;

        return searchInStore(args[1]);
    }

    /**
     * 途中までの入力に合うルートを探すメソッド
     * @param query 途中まで入れられた文字列
     * @return 入力に合うルート名のリスト
     */
    private List<String> searchInStore(String query) {
        var list = routeManager.getRouteNames();
        return TabCompleteUtil.searchInList(query, list);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // パーミッションチェック
        if (!hasPermission(sender)) {
            sender.sendMessage("必要な権限がありません");
            return true;
        }
        // 引数0の場合はコマンドが指定されていない
        if (args.length == 0)
            return help(sender);

        // コマンドごとに分岐やる、この構文めちゃ便利
        switch (args[0]) {
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "view" -> view(sender, args);
            case "list" -> list(sender, args);
            case "rmroute" -> rmroute(sender, args);
            case "copy" -> copy(sender, args);
            case "replace" -> replace(sender, args);
            case "insert" -> insert(sender, args);
            default -> help(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!hasPermission(sender))
            return null;

        // コマンドをサジェストする
        if (args.length <= 1) {
            // 管理コマンドはサジェストしなくたっていい
            return List.of("add", "remove", "view", "list",
                           "rmroute", "copy", "replace", "insert");
        }

        // コマンドごとのサジェストはそれぞれのメソッドへ
        return switch (args[0]) {
            case "add" -> addTab(args);
            case "insert" -> insertTab(args);
            case "replace" -> replaceTab(args);
            case "remove" -> removeTab(args);
            case "view" -> viewTab(args);
            case "rmroute" -> rmrouteTab(args);
            case "copy" -> copyTab(args);
            default -> null;
        };
    }

    // helpコマンド
    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "TrainCartsで記録されている列車用のルートを管理します",
            "利用可能なコマンド: ",
            "add: 項目を追加します",
            "remove: 項目を削除します",
            "view: 指定されたルートの項目を表示します",
            "list: ルートの一覧を表示します",
            "rmroute: 指定したルートを削除します",
            "copy: ルートのコピーを行います",
            "replace: ルート内の指定された項目を入れ替えます",
            "insert: ルート内の指定された位置に項目を追加します"
        );
        return false;
    }
    
    private boolean hasPermission(CommandSender sender) {
        if (Permission.PROPERTY_ROUTE.has(sender) && Permission.COMMAND_SAVE_ROUTE.has(sender)) {
            return true;
        }
        return false;
    }

    private void commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ",
                           LABEL + " " + usage);
    }

    public static final String LABEL = "redit";
    private final TrainCarts TC;
    private final RouteManager routeManager;

    public CommandRouteEdit(Advancedautotrain plugin) {
        this.TC = plugin.getTrainCarts();
        this.routeManager = TC.getRouteManager();
    }
}
