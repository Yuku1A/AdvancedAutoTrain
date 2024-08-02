package io.github.yuku1a.advancedautotrain.lspawn;

import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.tc.utils.TimeDurationFormat;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandLSpawn implements CommandExecutor {
    private final Advancedautotrain plugin;
    private final ScheduledSpawnSetStore store;

    public CommandLSpawn(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getSpawnListStore();
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
            case "pause" -> pause(sender, args);
            case "resume" -> resume(sender, args);
            case "imm" -> immediate(sender, args);
            default -> help(sender);
        };
    }

    private boolean list(CommandSender sender, String[] args) {
        // コマンドで1、ページで1
        if (args.length > 2)
            return commandsHelp(sender, "lspn list <page>");

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
            "----- list of spawnlist page " + index + " of " +
            CommandUtil.calcMaxPageIndex(rawlist) + " page -----"
        );

        list.forEach(sender::sendMessage);

        return true;
    }

    // ↓ここからリストへの操作
    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、ページ指定で1つ
        if (2 > args.length || args.length > 3)
            return commandsHelp(sender, "lspn view <listname> <page>");

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
            "----- spawnlist " + key +
            " page " + index + " of " + CommandUtil.calcMaxPageIndex(rawlist) + " -----"
        );

        // タイマーと時刻表示
        sender.sendMessage(
            "timer: " + set.getTimerkey() + " | " +
            fmt.format(set.getTimer().currentTime())
        );

        // 表示
        viewParts(sender, list);

        return true;
    }

    private boolean copy(CommandSender sender, String[] args) {
        // コマンド1、元で1、先で1
        if (args.length != 3)
            return commandsHelp(sender, "lspn copy <from> <to>");

        // 元があるか確認
        var from = store.get(args[1]);

        if (from == null) {
            sender.sendMessage("指定されたリストは存在しません");
            return true;
        }

        // 実行
        var to = new ScheduledSpawnSet(from.asList(), from.getTimerkey());
        to.enable(plugin);
        store.put(args[2], to);

        // おわり
        sender.sendMessage("コピーが完了しました。");
        return true;
    }

    private boolean removelist(CommandSender sender, String[] args) {
        // コマンドで1、リストで1
        if (args.length != 2)
            return commandsHelp(sender, "lspn rmlist <list>");

        // 削除
        store.remove(args[1]);

        // おわり
        sender.sendMessage("削除完了しました");
        return true;
    }

    private boolean create(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、タイマー指定で1つ
        if (args.length != 3)
            return commandsHelp(sender, "lspn create <listname> <timer>");

        // タイマーがあるかどうかだけ確認
        if (!plugin.getOperationTimerStore().containsKey(args[2])) {
            sender.sendMessage("指定されたタイマーは存在しません。");
            return true;
        }

        // 普通に作る
        var set = new ScheduledSpawnSet(args[2]);
        if (!set.enable(plugin)) {
            sender.sendMessage("指定されたタイマーは存在しません。");
            return true;
        }

        // ちゃんとしたものを登録する
        store.put(args[1], set);

        // おわり
        sender.sendMessage("リストを作成しました");
        return true;
    }

    private boolean pause(CommandSender sender, String[] args) {
        // コマンドで1、リストで1
        if (args.length != 2)
            return commandsHelp(sender, "lspn pause <list>");

        // リストを出し検証
        var list = store.get(args[1]);
        if (list == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        // 止める
        list.pause();
        sender.sendMessage("リスト " + args[1] + " の動作を一時停止しました。");
        return true;
    }

    private boolean resume(CommandSender sender, String[] args) {
        // コマンドで1、リストで1
        if (args.length != 2)
            return commandsHelp(sender, "lspn pause <list>");

        // リストを出し検証
        var list = store.get(args[1]);
        if (list == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        // 動かす
        list.resume();
        sender.sendMessage("リスト " + args[1] + " の動作を再開しました。");
        return true;
    }

    private boolean immediate(CommandSender sender, String[] args) {
        // コマンドで1、リストで1、内容で1
        if (args.length != 3) {
            return commandsHelp(sender, "lspn imm <list> <savedtrainname>");
        }

        // リストを出し検証
        var list = store.get(args[1]);
        if (list == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        // 引数の取り出し
        var savedtrainname = args[2];

        // 新規登録の代わりに置き換え
        list.setImmediate(new ScheduledSpawn(0, savedtrainname, null));

        // おわり
        sender.sendMessage("登録した列車が約10秒後にスポーンします。");
        // UI
        sender.sendMessage("(savedtrainname)");

        // 情報の表示
        sender.sendMessage(args[2]);
        return true;
    }

    // ↓ここから個別の項目に対しての操作
    private boolean remove(CommandSender sender, String[] args) {
        // コマンドで1、リストで1、時間指定で1
        if (args.length != 3)
            return commandsHelp(sender, "lspn remove <list> <time>");

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
        // savedtrainname、時刻で2
        // 合計4個
        if (args.length != 4) {
            return commandsHelp(
                sender,
                "lspn " + args[0] + " <listname> " +
                "<savedtrainname> <time>"
            );
        }

        // リストがあるかどうか確認する
        var set = store.get(args[1]);

        if (set == null) {
            sender.sendMessage("指定されたリストは存在しません。");
            return true;
        }

        // 引数の取り出し
        var savedtrainname = args[2];
        var timetext = args[3];

        // 時刻指定が正確かチェックする
        var time = ParseUtil.parseTime(timetext);

        // インスタンス作成
        var sspawn = new ScheduledSpawn(time, savedtrainname, null);

        // 新規登録の代わりに置き換え
        set.remove(time);
        set.add(sspawn);

        // 登録した内容を表示する
        viewParts(sender, List.of(sspawn));

        // おわり
        sender.sendMessage("登録完了しました");
        return true;
    }

    private void viewParts(CommandSender sender, List<ScheduledSpawn> list) {
        // 時間表示のセットアップ
        var fmt = new TimeDurationFormat("HH:mm:ss");

        // UI
        sender.sendMessage("(time) (savedtrainname)");

        // 情報の表示
        list.forEach((v) -> viewOne(sender, v, fmt));
    }

    private void viewOne(CommandSender sender, ScheduledSpawn entry, TimeDurationFormat fmt) {
        sender.sendMessage(
            fmt.format(entry.getScheduletime()) + " | " +
                entry.getSavedTrainName());
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "usage: ",
            "add: 項目をリストに追加、入れ替え",
            "remove: 項目をリストから削除",
            "copy: リストをコピー",
            "view: リストの内容を表示",
            "rmlist: リストを削除",
            "create: リストを作成",
            "list: リストの一覧",
            "pause: スポーンの一時停止",
            "resume: スポーンの再開",
            "imm: 指定した列車の即時スポーン"
        );
        return true;
    }

    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", usage);
        return true;
    }
}
