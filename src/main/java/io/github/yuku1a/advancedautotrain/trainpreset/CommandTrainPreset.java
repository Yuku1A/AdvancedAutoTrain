package io.github.yuku1a.advancedautotrain.trainpreset;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandTrainPreset implements CommandExecutor, TabCompleter {

    private void list(CommandSender sender, String[] args) {
        // リスト全部持ってくる
        var list = store.entryList();

        // 空だった場合
        if (list.isEmpty()) {
            sender.sendMessage("プリセットは一つも登録されていません。");
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
    private void listView(CommandSender sender, List<Map.Entry<String, TrainPreset>> list,
                          String pageIndex, int maxPage) {
        // UI
        sender.sendMessage("----- TrainPreset List Page " + pageIndex + " of " + (maxPage + 1) + " -----");
        sender.sendMessage("(trainname) (cstationlist) (route) (tag)");

        // 情報を表示
        list.forEach((e) -> itemView(sender, e.getKey(), e.getValue()));

        // おわり
    }

    private static void itemView(CommandSender sender, String key, TrainPreset preset) {
        String tags = "null";
        if (preset.getTag() != null) {
            StringBuilder tagsbuilder = new StringBuilder();
            for (var t : preset.getTag()) {
                if (tagsbuilder.isEmpty())
                    tagsbuilder.append(t);
                else
                    tagsbuilder.append(",").append(t);
            }

            tags = tagsbuilder.toString();
        }

        // 情報を表示する
        sender.sendMessage(
            key + " | " +
            preset.getCstationListName() + " | " +
            preset.getRouteName() + " | " +
            tags
        );
    }

    private void view(CommandSender sender, String[] args) {
        // コマンド指定で1、名前で1
        if (args.length != 2) {
            commandsHelp(sender, "view <name>");
            return;
        }

        // 取得
        var presetName = args[1];
        var preset = store.get(presetName);
        if (preset == null) {
            sender.sendMessage("指定されたTrainPresetは存在しません。");
            return;
        }

        // 表示
        sender.sendMessage("--- TrainPreset " + presetName + " ---");
        itemView(sender, presetName, preset);
    }

    private List<String> viewTab(String[] args) {
        // 名前以外はサジェストしない
        if (args.length == 2) {
            var presets = store.keysList();
            return TabCompleteUtil.searchInList(args[1], presets);
        }

        return null;
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

    private List<String> removeTab(String[] args) {
        // 名前以外はサジェストしない
        if (args.length == 2) {
            var presets = store.keysList();
            return TabCompleteUtil.searchInList(args[1], presets);
        }

        return null;
    }

    private void add(CommandSender sender, String[] args) {
        // コマンド指定で1、タグを無制限に受け付けるので無限
        if (args.length == 1) {
            commandsHelp(sender, "add <trainname> [cstationlist] [route] [tags...]");
            return;
        }

        // パース
        // TrainName
        var trainname = args[1];
        // CStationList
        String cstationlist;
        if (args.length >= 3)
            cstationlist = args[2];
        else
            cstationlist = null;
        // Route
        String route;
        if (args.length >= 4)
            route = args[3];
        else
            route = null;
        // Tags
        List<String> tags;
        if (args.length >= 5)
            tags = Arrays.asList(Arrays.copyOfRange(args, 4, args.length));
        else
            tags = null;

        // 生成して登録
        var preset = new TrainPreset(trainname, cstationlist, route, tags);
        store.put(trainname, preset);

        // おわり
        sender.sendMessage("プリセットの登録に成功しました。");
        sender.sendMessage("(trainname) (cstationlist) (route) (tag)");
        itemView(sender, trainname, preset);
    }

    private List<String> addTab(String[] args) {
        // インスペクション対策
        if (args.length <= 1)
            return null;

        // savedTrainNameからサジェスト
        if (args.length == 2) {
            var savedTrains = plugin.getTrainCarts().getSavedTrains().getNames();
            return TabCompleteUtil.searchInList(args[1], savedTrains);
        }

        // CStationListTemplate
        if (args.length == 3) {
            var cslts = plugin.getCStationListTemplateStore().keysList();
            return TabCompleteUtil.searchInList(args[2], cslts);
        }

        // Route
        if (args.length == 4) {
            var routes = plugin.getTrainCarts().getRouteManager().getRouteNames();
            return TabCompleteUtil.searchInList(args[3], routes);
        }

        // tagは今のところサジェストしないものとする
        return null;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "TrainPresetを管理します",
            "利用可能なコマンド: ",
            "add: プリセットを登録します",
            "remove: プリセットを削除します",
            "list: プリセットの一覧を表示します",
            "view: 指定されたプリセットの内容を表示します");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(plugin.UsePermission))
            return null;

        if (args.length == 1) {
            return List.of("add", "list", "remove", "view");
        }

        return switch (args[0]) {
            case "add" -> addTab(args);
            case "remove" -> removeTab(args);
            case "view" -> viewTab(args);
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

        // 0の場合コマンドが指定されていない
        if (args.length == 0)
            return help(sender);

        // 各コマンドへ振り分け
        switch (args[0]) {
            case "list" -> list(sender, args);
            case "remove" -> remove(sender, args);
            case "add" -> add(sender, args);
            case "view" -> view(sender, args);
            default -> help(sender);
        };

        return true;
    }

    // コマンドごとのヘルプが多少楽になる
    private void commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ",
                           LABEL + " " + usage);
    }

    public static final String LABEL = "tpreset";
    private final TrainPresetStore store;
    private final Advancedautotrain plugin;

    public CommandTrainPreset(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getTrainPresetStore();
    }
}
