package io.github.yuku1a.advancedautotrain.schedaction;

import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.tc.utils.TimeDurationFormat;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class CommandOperationTimer implements CommandExecutor {


    private boolean modify(CommandSender sender, String[] args) {
        // コマンドで1、名前で1、時間操作で1
        if (args.length != 3)
            return commandsHelp(sender, "modify <name> <time>");

        // 指定されたタイマーがあるかどうかチェック
        if (!store.containsKey(args[1])) {
            sender.sendMessage("指定された名前のタイマーは登録されていません。");
            return true;
        }

        // 引数の取り出し
        String timeText = args[2];

        // 時間のチェック
        if (!timeText.contains(":")) {
            sender.sendMessage("時間指定には1:00:00の形式または1:00の形式を使用してください");
            return true;
        }

        // 記号のパース
        long prefix = 1;
        if (timeText.startsWith("-")) {
            prefix = -1;
            timeText = timeText.substring(1);
        }

        // 時間のパース
        long diffTime = ParseUtil.parseTime(timeText);

        // 時間のチェック
        if (diffTime == 0) {
            sender.sendMessage("時間指定が不正です");
            return true;
        }

        // 記号を反映する
        diffTime = diffTime * prefix;

        // タイマーをずらす
        store.get(args[1]).modifyBaseTime(diffTime);

        // おわり
        sender.sendMessage("指定されたタイマーの時間をずらしました");
        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        // リスト全部持ってくる
        var list = store.entryList();

        // 空だった場合
        if (list.isEmpty()) {
            sender.sendMessage("タイマーは一つも登録されていません。");
            return true;
        }

        // ページングしないで済む場合
        if (list.size() < 16)
            return listView(sender, list, "1", 0);

        // ページ数の部分の引数がない場合1として扱う
        var page = args.length == 1 ? "1" : args[1];

        // ページング処理
        var slist = CommandUtil.pager(sender, list, page);

        // nullだったらページングに失敗して警告が出てるので終了
        if (slist == null)
            return true;

        // 分割されたページを表示
        return listView(sender, slist, page, CommandUtil.calcMaxPageIndex(list));
    }

    // 内容の表示用
    private boolean listView(CommandSender sender, List<Map.Entry<String, OperationTimer>> list,
                             String pageIndex, int maxPage) {
        // UI
        sender.sendMessage("----- Timer List Page " + pageIndex + " of " + (maxPage + 1) + " -----");
        sender.sendMessage("(name) (elapsed time) (interval)");

        // 時間表示のセットアップ
        var fmt = new TimeDurationFormat("HH:mm:ss");

        // 情報を表示
        list.forEach((e) -> {
            // タイマーだけ先に出す
            var timer = e.getValue();

            // 情報を表示する
            sender.sendMessage(
                e.getKey() + " | " +
                fmt.format(timer.currentTime()) + " | " +
                fmt.format(timer.getInterval())
            );
        });

        // おわり
        return true;
    }

    private boolean remove(CommandSender sender, String[] args) {
        // コマンド指定で1、名前で1
        if (args.length != 2)
            return commandsHelp(sender, "remove <name>");

        // 削除
        store.remove(args[1]);

        // おわり
        sender.sendMessage("削除が完了しました。");
        return true;
    }

    private boolean create(CommandSender sender, String[] args) {
        // コマンド指定で1、名前で1、周期指定で1
        if (args.length != 3)
            return commandsHelp(sender, "create <name> <cycle>");

        // 新しいのに置き換えられたりしたらさすがに壊れる
        if (store.containsKey(args[1])){
            sender.sendMessage("同じ名前でタイマーを登録することはできません。");
            return true;
        }

        // 周期指定をパースする前にチェック
        if (!args[2].contains(":")) {
            sender.sendMessage("時間指定には1:00:00の形式または1:00の形式を使用してください");
            return true;
        }

        // 周期指定をパースする
        var interval = ParseUtil.parseTime(args[2]);

        // 不正がないかチェック
        if (interval == 0) {
            sender.sendMessage("時間指定が不正です");
            return true;
        }

        // 指定された周期でタイマーを生成、登録
        store.put(args[1], new OperationTimer(interval));

        // おわり
        sender.sendMessage("タイマーの登録に成功しました。");
        return true;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "OperationTimerを管理します",
            "利用可能なコマンド: ",
            "create: タイマーを作成します",
            "remove: タイマーを削除します",
            "list: タイマーの一覧を表示します",
            "modify: タイマーの時間をずらします");
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // パーミッションチェック
        if (!sender.hasPermission(plugin.UsePermission)) {
            sender.sendMessage("必要な権限がありません");
            return true;
        }

        // 0の場合コマンドが指定されていない
        if (args.length == 0)
            return help(sender);

        // 各コマンドへ振り分け
        return switch (args[0]) {
            case "modify" -> modify(sender, args);
            case "list" -> list(sender, args);
            case "remove" -> remove(sender, args);
            case "create" -> create(sender, args);
            default -> help(sender);
        };
    }

    public static final String LABEL = "optimer";

    // コマンドごとのヘルプが多少楽になる
    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", LABEL + " " + usage);
        return true;
    }

    private final OperationTimerStore store;
    private final Advancedautotrain plugin;

    public CommandOperationTimer(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getOperationTimerStore();
    }
}
