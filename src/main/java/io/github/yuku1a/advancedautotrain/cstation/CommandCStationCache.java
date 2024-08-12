package io.github.yuku1a.advancedautotrain.cstation;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandCStationCache implements CommandExecutor, TabCompleter {

    private boolean add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、追加指定で1つ
        if (args.length != 2)
            return commandsHelp(sender, "add <cstationname>");

        // 特にチェックは行わずに追加できる
        var cstationName = args[1];
        var set = plugin.getCStationCacheSet();

        set.add(cstationName);

        // 何を追加したのかを表示
        sender.sendMessage("CStation " + cstationName + " の情報を追加しました。");
        return true;
    }

    private boolean remove(CommandSender sender, String[] args) {
        // コマンド指定で1つ、削除指定で1つ
        if (args.length != 2)
            return commandsHelp(sender, "remove <cstationname>");

        // 特にチェックなく削除できる
        var cstationName = args[1];
        var set = plugin.getCStationCacheSet();

        set.remove(cstationName);

        // 何を削除したのかを表示
        sender.sendMessage("CStation " + cstationName + " の情報を削除しました。");
        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        // コマンド指定で1つ、ページ指定がある場合1つ
        if (args.length > 2)
            return commandsHelp(sender, "list [page]");

        // とりあえず取得
        var pageStr = args.length == 2 ? args[1] : "1";
        var set = plugin.getCStationCacheSet();
        var baseList = set.get();

        // ページング
        var plist = CommandUtil.pager2D(baseList, 15);

        var pageIndex = CommandUtil.tryParsePagingIndex(sender, plist, pageStr);
        if (pageIndex == -1)
            return true;

        // 表示
        sender.sendMessage("--- CStationCache page " + pageStr + " of " + plist.size() + " ---");
        for (var entry : plist.get(pageIndex)) {
            sender.sendMessage(entry.getData());
        }

        return true;
    }

    private boolean build(CommandSender sender, String[] args) {
        // 引数はない
        if (args.length != 1) {
            return commandsHelp(sender, "build");
        }

        sender.sendMessage("キャッシュのビルドを開始します。");

        // クソ雑実装
        var csCacheSet = plugin.getCStationCacheSet();
        var csltStore = plugin.getCStationListTemplateStore();
        for (var csltEntry : csltStore.entryList()) {
            var cslt = csltEntry.getValue();
            for (var csinfo : cslt) {
                csCacheSet.add(csinfo.getName());
            }
        }

        sender.sendMessage("キャッシュのビルドを完了しました。");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        // 権限チェック
        if (sender.hasPermission(plugin.UsePermission))
            return null;

        // コマンドをサジェストする
        if (args.length <= 1) {
            return List.of("add", "remove", "list", "build");
        }

        // コマンドに対する引数のサジェスト
        switch (args[0]) {
            case "remove" -> {
                // 2個以上の引数は取らない
                if (args.length > 2)
                    return null;

                // removeするなら既存の内容からだろうからそれをサジェスト
                var list = plugin.getCStationCacheSet().get();

                // 途中まで入力されていればそれに沿うようにサジェスト
                return TabCompleteUtil.searchInList(args[1], list);
            }
            default -> {
                return null;
            }
        }
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "このサーバーにあるCStationの名前をキャッシュする",
            "機能を管理するためのコマンドです",
            "キャッシュはコマンドの自動補完に使われます", 
            "利用可能なコマンド: ",
            "add: CStationの名前を追加します",
            "remove: CStationの名前を削除します",
            "list: キャッシュの内容を表示します",
            "build: CStationListTemplateからキャッシュを再構築します"
        );
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // パーミッション
        if (!sender.hasPermission(plugin.UsePermission)) {
            sender.sendMessage("必要な権限がありません");
            return true;
        }
        // 引数0の場合はコマンドが指定されていない
        if (args.length == 0)
            return help(sender);

        // コマンドごとに分岐
        return switch (args[0]) {
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "list" -> list(sender, args);
            case "build" -> build(sender, args);
            default -> help(sender);
        };
    }

    private final Advancedautotrain plugin;
    public CommandCStationCache(Advancedautotrain plugin) {
        this.plugin = plugin;
    }

    public static final String LABEL = "cstationcache";

    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", LABEL + " " + usage);
        return true;
    }
}
