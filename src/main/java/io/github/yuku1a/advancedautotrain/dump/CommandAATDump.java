package io.github.yuku1a.advancedautotrain.dump;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandAATDump implements CommandExecutor, TabCompleter {
    public boolean rail(CommandSender sender, String[] args) {
        // コマンドとワールド選択で2
        if (args.length != 2)
            return commandsHelp(sender, "rail <world>");

        // PathProviderは存在しないワールドのものを取得しようとすると
        // 存在しないワールドのものを本当に生成して返してしまうので
        // ワールドがあるかないかを含め別で取得する
        var world = plugin.getServer().getWorld(args[1]);
        if (world == null) {
            sender.sendMessage("指定されたワールドは存在しません。");
            return true;
        }

        // PathWorldを取得
        var pathworld = plugin.getTrainCarts().getPathProvider().getWorld(world);
        var pathlist = new ArrayList<String>(pathworld.getNodes().size());
        for (var path : pathworld.getNodes()) {
            pathlist.add(path.getDisplayName());
        }

        // コンフィグを構築する
        var yamlcfg = new YamlConfiguration();
        yamlcfg.set("pathlist", pathlist);

        // どうにかして保存したいが・・・
        var file = new File(plugin.getDataFolder(), "raildump" + File.separator + args[1] + ".yml");
        try {
            yamlcfg.save(file);
        } catch (IOException e) {
            sender.sendMessage("データの保存に失敗しました。");
        }

        // 成功した
        sender.sendMessage("ワールド " + args[1] + " のデータのダンプに成功しました");
        return true;
    }

    public boolean optimer(CommandSender sender, String[] args) {
        // コマンドで1、optimer指定で1
        if (args.length != 2)
            return commandsHelp(sender, "optimer <optimer>");

        // YamlConfigurationをまず作る
        var yamlcfg = new YamlConfiguration();

        var optimerkey = args[1];

        // 端から端まで集める
        // OPTimer
        // 存在確認は一応する
        var optimer = plugin.getOperationTimerStore().get(optimerkey);
        if (optimer == null) {
            sender.sendMessage("指定されたoptimerは存在しません。");
            return true;
        }
        yamlcfg.set("optimer", optimer);

        // ここから存在確認がいらないがルックアップが大変
        // LSpawn
        var ymllspn = yamlcfg.createSection("lspawn");
        for (var entry : plugin.getSpawnListStore().entryList()) {
            // 対象のOPTimerと同じものを参照してるか確認
            if (entry.getValue().getTimerkey().equals(optimerkey)) {
                ymllspn.set(entry.getKey(), entry.getValue());
            }
        }

        // TrainRecord
        var ymlrecord = yamlcfg.createSection("trainrecord");
        var recordstore = plugin.getTrainRecordStore();
        // めんどくさいのでまるごと吐く
        recordstore.entryList().forEach(e -> ymlrecord.set(e.getKey(), e.getValue()));

        // TrainPreset
        var ymlpreset = yamlcfg.createSection("trainpreset");
        var presetstore = plugin.getTrainPresetStore();
        // あとのroute用
        var routelist = new ArrayList<String>();
        presetstore.entryList().forEach(e -> {
            var routename = e.getValue().getRouteName();
            if (routename != null)
                routelist.add(routename);
            ymlpreset.set(e.getKey(), e.getValue());
        });

        // Route
        var ymlroute = yamlcfg.createSection("route");
        for (var routename : routelist) {
            ymlroute.set(routename, plugin.getTrainCarts().getRouteManager().findRoute(routename));
        }

        // どうにかして保存したいが・・・
        var file = new File(plugin.getDataFolder(), "aatdump" + File.separator + args[1] + ".yml");
        try {
            yamlcfg.save(file);
        } catch (IOException e) {
            sender.sendMessage("データの保存に失敗しました。");
        }

        sender.sendMessage("OPTimer " + optimerkey + " に関連するデータのダンプに成功しました。");
        return true;
    }

    private List<String> optimerTab(String[] args) {
        if (args.length == 2) {
            var optimers = plugin.getOperationTimerStore().keysList();
            return TabCompleteUtil.searchInList(args[1], optimers);
        }

        return null;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "AdvancedAutoTrainやTrainCartsのデータを",
                    "外部アプリで解析できる形式にサーバー内でダンプします",
                    "取り出しについてはサーバーの管理者に問い合わせてください",
                    "利用可能なコマンド: ",
                    "rail: TrainCartsに登録されている線路のノードのデータなどをダンプします",
                    "optimer: 指定されたOPTimerに紐づけられたすべてのデータをダンプします"
        );
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(plugin.UsePermission))
            return null;

        if (args.length == 1) {
            return List.of("rail", "optimer");
        }

        return switch (args[0]) {
            case "optimer" -> optimerTab(args);
            default -> null;
        };
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
            case "rail" -> rail(sender, args);
            case "optimer" -> optimer(sender, args);
            default -> help(sender);
        };
    }

    private final Advancedautotrain plugin;
    public CommandAATDump(Advancedautotrain plugin) {
        this.plugin = plugin;
    }

    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", "aatdump " + usage);
        return true;
    }
}
