package io.github.yuku1a.advancedautotrain.arrivallist;

import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.tc.utils.TimeDurationFormat;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class CommandArrivalList implements CommandExecutor, TabCompleter {

    private boolean record(CommandSender sender, String[] args) {
        // コマンドで1、リストで1、オフセットで1の合計2または3
        if (args.length < 2 || args.length > 3)
            return CommandUtil.commandsHelp(sender, "ar record <list> [offset]");

        // キーの取得
        var key = args[1];

        // 取りに行く
        var set = store.get(key);

        // なかったらnullが返ってくるのでチェック
        if (set == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        var offset = -2L;

        if (args.length == 3){
            var offsetstr = args[2];

            try {
                offset = Long.parseLong(offsetstr);
            } catch (NumberFormatException e) {
                sender.sendMessage("オフセットの値が不正です。");
                return true;
            }
        }

        // 切り替えを実行する
        set.toggleRecord(offset);

        // 結果を表示する
        if (set.isRecording()) {
            sender.sendMessage("リスト "+ key + " の記録を開始しました。");
            sender.sendMessage("オフセット:" + offset + "秒");
        }
        else
            sender.sendMessage("リスト " + key + " の記録を停止しました。");

        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        // コマンドで1、ページで1
        if (args.length > 2)
            return CommandUtil.commandsHelp(sender, "ar list <page>");

        // キー一覧の取得
        var rawlist = store.keysList();

        // 指定がなければ1ページ扱い
        String index = args.length == 1 ? "1" : args[1];

        // ページング
        var list = CommandUtil.pager(sender, rawlist, index);

        // nullだとエラー
        if (list == null)
            return true;

        // 表示
        // UI
        sender.sendMessage(
            "----- list of arrivallist page " + index + " of " +
            CommandUtil.calcMaxPageIndex(rawlist) + " page -----"
        );

        list.forEach(sender::sendMessage);

        return true;
    }

    // ↓ここからリストへの操作
    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、ページ指定で1つ
        if (2 > args.length || args.length > 3)
            return CommandUtil.commandsHelp(sender, "ar view <listname> <page>");

        // キーの取得
        var key = args[1];

        // 取りに行く
        var set = store.get(key);

        // なかったらnullが返ってくるのでチェック
        if (set == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        // リストに変換
        var rawlist = set.asList();

        // ページ数指定の取得、なかったら1扱い
        String index = args.length == 2 ? "1" : args[2];

        // ページング
        var list = CommandUtil.pager(sender, rawlist, index);

        // 分割されたのがnullだったら終わり
        if (list == null)
            return true;

        // 時間表示のセットアップ
        var fmt = new TimeDurationFormat("HH:mm:ss");

        // 表示する
        // UI
        sender.sendMessage(
            "----- arrivallist " + key +
            " page " + index + " of " + CommandUtil.calcMaxPageIndex(rawlist) + " -----"
        );

        // タイマーと時刻表示
        sender.sendMessage(
            "timer: " + set.getTimerkey() + " | " +
            fmt.format(set.getTimer().currentTime())
        );

        // リストを表示
        WidgetsArrivalList.arrivalTrainView(sender, list);

        return true;
    }

    private boolean copy(CommandSender sender, String[] args) {
        // コマンド1、元で1、先で1
        if (args.length != 3)
            return CommandUtil.commandsHelp(sender, "ar copy <from> <to>");

        // 元があるか確認
        var from = store.get(args[1]);

        if (from == null) {
            sender.sendMessage("指定されたリストは存在しません");
            return true;
        }

        // 実行
        var to = new ScheduledSignSet(from.asList(), from.getTimerkey());
        to.enable(plugin);
        store.put(args[2], to);

        // おわり
        sender.sendMessage("コピーが完了しました。");
        return true;
    }

    private boolean removelist(CommandSender sender, String[] args) {
        // コマンドで1、リストで1
        if (args.length != 2)
            return CommandUtil.commandsHelp(sender, "ar rmlist <list>");

        // 削除
        store.remove(args[1]);

        // おわり
        sender.sendMessage("削除完了しました");
        return true;
    }

    private List<String> rmlistTab(String[] args) {
        // リストの名前のみサジェスト
        if (args.length != 2)
            return null;

        var lists = store.keysList();
        return TabCompleteUtil.searchInList(args[1], lists);
    }

    private boolean create(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、タイマー指定で1つ
        if (args.length != 3)
            return CommandUtil.commandsHelp(sender, "ar create <timer> <listname>");

        // タイマーがあるかどうかだけ確認
        if (!plugin.getOperationTimerStore().containsKey(args[1])) {
            sender.sendMessage("指定されたタイマーは存在しません。");
            return true;
        }

        // 普通に作る
        var set = new ScheduledSignSet(args[1]);
        if (!set.enable(plugin)) {
            sender.sendMessage("指定されたタイマーは存在しません。");
            return true;
        }

        // ちゃんとしたものを登録する
        store.put(args[2], set);

        // おわり
        sender.sendMessage("リスト " + args[2] +  " を作成しました");
        return true;
    }

    // ↓ここから個別の項目に対しての操作
    private boolean remove(CommandSender sender, String[] args) {
        // コマンドで1、リストで1、時間指定で1
        if (args.length != 3)
            return CommandUtil.commandsHelp(sender, "ar remove <list> <time>");

        // リストを出す
        var list = store.get(args[1]);

        if (list == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        // 時間を出す
        var time = ParseUtil.parseTime(args[2]);

        // 削除
        list.remove(time);

        // おわり
        sender.sendMessage("指定された項目を削除しました。");
        return true;
    }

    // addとreplaceを兼ねる
    private boolean add(CommandSender sender, String[] args) {
        // コマンドで1つ、リストで1つ、
        // trainname、traindescription、時刻で3
        // 合計5
        if (args.length != 5) {
            return CommandUtil.commandsHelp(
                sender,
                "ar " + args[0] + " <listname> " +
                "<displayname> <traindescription> <time>"
            );
        }

        // リストがあるかどうか確認する
        var set = store.get(args[1]);

        if (set == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        // 引数の取り出し
        var trainname = args[2];
        var traindescription = args[3];
        var timetext = args[4];

        // 時刻指定が正確かチェックする
        var time = ParseUtil.parseTime(timetext);

        // 生成
        var info = new ScheduledSign(time, trainname, traindescription);

        // 新規登録の代わりに置き換え
        set.remove(time);
        set.add(info);

        // おわり
        sender.sendMessage("登録完了しました");

        // 結果の表示用
        WidgetsArrivalList.arrivalTrainView(sender, Collections.singletonList(info));
        return true;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "ar(ArrivalList)コマンドの使い方",
            "usage: ",
            "add: 項目をリストに追加、入れ替え",
            "remove: 項目をリストから削除",
            "copy: リストをコピー",
            "view: リストの内容を表示",
            "rmlist: リストを削除",
            "create: リストを作成",
            "list: リストの一覧",
            "record: 時刻の記録の切り替え"
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!sender.hasPermission(plugin.UsePermission))
            return null;

        // コマンドのサジェスト
        if (args.length == 1) {
            return List.of("add", "remove", "copy", "view", "rmlist", "create", "list", "record");
        }

        // 各コマンドの引数のサジェスト
        return switch (args[0]) {
            case "rmlist" -> rmlistTab(args);
            default -> null;
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // パーミッションチェック
        if (!sender.hasPermission(plugin.UsePermission)) {
            sender.sendMessage("必要な権限がありません");
            return true;
        }

        // 引数が0のときはコマンドが指定されていない
        if (args.length == 0)
            return false;

        return switch (args[0]) {
            case "list" -> list(sender, args);
            case "view" -> view(sender, args);
            case "copy" -> copy(sender, args);
            case "rmlist", "removelist" -> removelist(sender, args);
            case "create" -> create(sender, args);
            case "replace", "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "record" -> record(sender, args);
            default -> help(sender);
        };
    }

    public static final String LABEL = "ar";

    private final Advancedautotrain plugin;
    private final ScheduledSignSetStore store;

    public CommandArrivalList(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getSignListStore();
    }
}
