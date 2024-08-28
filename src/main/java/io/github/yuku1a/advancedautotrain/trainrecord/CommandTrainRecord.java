package io.github.yuku1a.advancedautotrain.trainrecord;

import com.bergerkiller.bukkit.tc.utils.TimeDurationFormat;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandTrainRecord implements CommandExecutor, TabCompleter {

    private boolean start(CommandSender sender, String[] args) {
        // コマンド指定で1、列車指定で1
        if (args.length != 2)
            return CommandUtil.commandsHelp(sender, "trec start <trainname>");

        plugin.getTrainRecordingManager().recordingRegister(args[1]);
        sender.sendMessage(args[1] + " のイベントの記録を開始します。");
        return true;
    }

    private boolean stop(CommandSender sender, String[] args) {
        // コマンド指定で1、列車指定で1
        if (args.length != 2)
            return CommandUtil.commandsHelp(sender, "trec stop <trainname>");

        plugin.getTrainRecordingManager().recordingUnRegister(args[1]);
        sender.sendMessage(args[1] + " のイベントの記録を終了しました。");
        return true;
    }

    private boolean modify(CommandSender sender, String[] args) {
        // コマンド指定で1、列車指定で1、インデックス指定で1、秒数指定で1
        if (args.length != 4)
            return CommandUtil.commandsHelp(sender, "trec modify <recordname> <index> <seconds>");

        // レコードを取ってきてnullチェック
        var key = args[1];
        var list = store.get(key);
        if (list == null){
            sender.sendMessage("指定されたレコードは存在しません。");
            return true;
        }

        // indexのチェック
        String indexstr = args[2];
        int index = CommandUtil.tryParseIndex(sender, list.asList(), indexstr);
        if (index == -1)
            return true;

        // 秒数をパースしてみる
        String secondsstr = args[3];
        long seconds;
        try {
            seconds = Long.parseLong(secondsstr) * 1000;
        } catch (NumberFormatException e){
            sender.sendMessage("秒数は数値で指定する必要があります。");
            return true;
        }

        // 実際に変更
        list.timeModify(index, seconds);
        sender.sendMessage(key + "の" + indexstr + "番目からの記録を" + secondsstr + "秒ずらしました。");

        return true;
    }

    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、ページ指定で1つ
        if (2 > args.length || args.length > 3)
            return CommandUtil.commandsHelp(sender, "trec view <recordname> <page>");

        // キーの取得
        var key = args[1];

        // 取りに行く
        var set = store.get(key);

        // なかったらnullが返ってくるのでチェック
        if (set == null) {
            sender.sendMessage("指定されたレコードは存在しません。");
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

        // 表示する
        // UI
        sender.sendMessage(
            "----- trainrecord " + key +
                " page " + index + " of " + CommandUtil.calcMaxPageIndex(rawlist) + " -----"
        );

        // リストを表示
        trainRecordView(sender, list, Integer.parseInt(index) - 1);

        return true;
    }

    private void trainRecordView(CommandSender sender, List<TrainRecordEntry> list, int pageindex) {
        // 時間表示のセットアップ
        var fmt = new TimeDurationFormat("HH:mm:ss");

        // UI
        sender.sendMessage("(index) (time) (location) (actiontype)");

        // インデックスをちゃんと表示する
        for (int i = 0 ; i < list.size() ; i++){
            int index = CommandUtil.calcPagingIndex(i, pageindex);
            var v = list.get(i);
            // 座標or看板の名前で表示
            var loc = v.getTrainRecord().getLocation();
            String locstr;
            if (v.getTrainRecord().getSignName() == null)
                locstr = loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ();
            else
                locstr = v.getTrainRecord().getSignName();
            sender.sendMessage(
                index + " | " + fmt.format(v.getRecordedAt()) + " | " + locstr + " | " + v.getTrainRecord().getActionType()
            );
        }
    }

    private boolean copy(CommandSender sender, String[] args) {
        // コマンド1、元で1、先で1
        if (args.length != 3)
            return CommandUtil.commandsHelp(sender, "trec copy <from> <to>");

        // 元があるか確認
        var from = store.get(args[1]);

        if (from == null) {
            sender.sendMessage("指定されたレコードは存在しません");
            return true;
        }

        // 実行
        var to = new TrainRecordList(from.asList());
        store.put(args[2], to);

        // おわり
        sender.sendMessage("コピーが完了しました。");
        return true;
    }

    private boolean rmrec(CommandSender sender, String[] args) {
        // コマンドで1、リストで1
        if (args.length != 2)
            return CommandUtil.commandsHelp(sender, "trec rmrec <list>");

        // 削除
        store.remove(args[1]);

        // おわり
        sender.sendMessage("削除完了しました");
        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        // コマンドで1、ページで1
        if (args.length > 2)
            return CommandUtil.commandsHelp(sender, "trec list <page>");

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
            "----- list of trainrecord page " + index + " of " +
                CommandUtil.calcMaxPageIndex(rawlist) + " page -----"
        );

        list.forEach(sender::sendMessage);

        return true;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "trec(TrainRecord)コマンドの使い方",
            "usage: ",
            "copy: リストをコピー",
            "view: レコードの内容を表示",
            "rmrec: レコードを削除",
            "list: レコードの一覧",
            "start: 記録を開始",
            "stop: 記録を停止",
            "modify: 記録を編集"
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(plugin.UsePermission))
            return null;

        // コマンドのサジェスト
        if (args.length <= 1) {
            return List.of("copy", "view", "rmrec", "list", "start", "stop", "modify");
        }

        return switch (args[0]) {
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
            case "rmrec" -> rmrec(sender, args);
            case "start" -> start(sender, args);
            case "stop" -> stop(sender, args);
            case "modify" -> modify(sender, args);
            default -> help(sender);
        };
    }

    private final Advancedautotrain plugin;
    private final TrainRecordStore store;

    public CommandTrainRecord(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getTrainRecordStore();
    }
}
