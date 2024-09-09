package io.github.yuku1a.advancedautotrain.schedaction;

import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.tc.utils.TimeDurationFormat;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Map;

public class CommandOperationTimer implements CommandExecutor, TabCompleter {


    private void modify(CommandSender sender, String[] args) {
        // コマンドで1、名前で1、時間操作で1
        if (args.length != 3) {
            commandsHelp(sender, "modify <name> <time>");
            return;
        }

        // 指定されたタイマーがあるかどうかチェック
        if (!store.containsKey(args[1])) {
            sender.sendMessage("指定された名前のタイマーは登録されていません。");
            return;
        }

        // 引数の取り出し
        String timeText = args[2];

        // 時間のチェック
        if (!timeText.contains(":")) {
            sender.sendMessage("時間指定には1:00:00の形式または1:00の形式を使用してください");
            return;
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
            return;
        }

        // 記号を反映する
        diffTime = diffTime * prefix;

        // タイマーをずらす
        store.get(args[1]).modifyBaseTime(diffTime);

        // おわり
        sender.sendMessage("指定されたタイマーの時間をずらしました");
    }

    private void list(CommandSender sender, String[] args) {
        // リスト全部持ってくる
        var list = store.entryList();

        // 空だった場合
        if (list.isEmpty()) {
            sender.sendMessage("タイマーは一つも登録されていません。");
            return;
        }

        // ページングしないで済む場合
        if (list.size() < 16) {
            listView(sender, list, "1", 0);
            return;
        }

        // ページ数の部分の引数がない場合1として扱う
        var page = args.length == 1 ? "1" : args[1];

        // ページング処理
        var slist = CommandUtil.pager(sender, list, page);

        // nullだったらページングに失敗して警告が出てるので終了
        if (slist == null)
            return;

        // 分割されたページを表示
        listView(sender, slist, page, CommandUtil.calcMaxPageIndex(list));
    }

    // 内容の表示用
    private void listView(CommandSender sender, List<Map.Entry<String, OperationTimer>> list,
                          String pageIndex, int maxPage) {
        // UI
        sender.sendMessage("----- Timer List Page " + pageIndex + " of " + (maxPage + 1) + " -----");
        sender.sendMessage("(name) (elapsed time) (interval)");

        // 情報を表示
        list.forEach((e) -> {
            // 情報を表示する
            viewOne(sender, e.getKey(), e.getValue());
        });
    }

    // 一つずつ表示する用
    private void viewOne(CommandSender sender, String name, OperationTimer timer) {
        var fmt = new TimeDurationFormat("HH:mm:ss");

        sender.sendMessage(
            name + " | " +
                fmt.format(timer.currentTime()) + " | " +
                fmt.format(timer.getInterval())
        );
    }

    private void remove(CommandSender sender, String[] args) {
        // コマンド指定で1、名前で1
        if (args.length != 2) {
            commandsHelp(sender, "remove <name>");
            return;
        }

        // 削除
        store.remove(args[1]);

        // おわり
        sender.sendMessage("削除が完了しました。");
    }

    private void create(CommandSender sender, String[] args) {
        // コマンド指定で1、名前で1、周期指定で1
        if (args.length != 3) {
            commandsHelp(sender, "create <name> <cycle>");
            return;
        }

        // 新しいのに置き換えられたりしたらさすがに壊れる
        if (store.containsKey(args[1])){
            sender.sendMessage("同じ名前でタイマーを登録することはできません。");
            return;
        }

        // 周期指定をパースする前にチェック
        if (!args[2].contains(":")) {
            sender.sendMessage("時間指定には1:00:00の形式または1:00の形式を使用してください");
            return;
        }

        // 周期指定をパースする
        var interval = ParseUtil.parseTime(args[2]);

        // 不正がないかチェック
        if (interval == 0) {
            sender.sendMessage("時間指定が不正です");
            return;
        }

        // 指定された周期でタイマーを生成、登録
        store.put(args[1], new OperationTimer(interval));

        // おわり
        sender.sendMessage("タイマーの登録に成功しました。");
    }

    private void view(CommandSender sender, String[] args) {
        // コマンド指定で1、タイマー指定で1
        if (args.length != 2) {
            commandsHelp(sender, "view <name>");
            return;
        }

        // 存在確認
        var timerName = args[1];
        var timer = store.get(timerName);
        if (timer == null) {
            sender.sendMessage("指定された名前のタイマーは存在しません。");
            return;
        }

        // 表示
        sender.sendMessage("-- OPTimer View --");
        viewOne(sender, timerName, timer);
    }

    private List<String> viewTab(String[] args) {
        // OPTimerの名前以外サジェストしない
        if (args.length != 2)
            return null;

        var timerName = args[1];
        return TabCompleteUtil.searchInList(timerName, store.keysList());
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "OperationTimerを管理します",
            "利用可能なコマンド: ",
            "create: タイマーを作成します",
            "remove: タイマーを削除します",
            "list: タイマーの一覧を表示します",
            "modify: タイマーの時間をずらします",
            "view: タイマーの状況を表示します");
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
        switch (args[0]) {
            case "modify" -> modify(sender, args);
            case "list" -> list(sender, args);
            case "remove" -> remove(sender, args);
            case "create" -> create(sender, args);
            case "view" -> view(sender, args);
            default -> help(sender);
        };

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        // パーミッションチェック
        if (!commandSender.hasPermission(plugin.UsePermission))
            return null;

        // 最初の引数はコマンド指定
        if (args.length == 1) {
            return List.of("create", "remove", "list", "modify", "view");
        }

        // それぞれのコマンドのTab補完はそれぞれにメソッドを作る
        return switch (args[0]) {
            case "view" -> viewTab(args);
            default -> null;
        };
    }

    public static final String LABEL = "optimer";

    // コマンドごとのヘルプが多少楽になる
    private void commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", LABEL + " " + usage);
    }

    private final OperationTimerStore store;
    private final Advancedautotrain plugin;

    public CommandOperationTimer(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getOperationTimerStore();
    }
}
