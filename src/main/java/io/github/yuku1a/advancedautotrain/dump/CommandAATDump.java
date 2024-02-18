package io.github.yuku1a.advancedautotrain.dump;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CommandAATDump implements CommandExecutor {
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
        var file = new File(plugin.getDataFolder(), "raildump" + File.pathSeparator + args[1] + ".yml");
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
        var trainnamelist = new ArrayList<String>();
        for (var entry : plugin.getSpawnListStore().entryList()) {
            // 対象のOPTimerと同じものを参照してるか確認
            if (entry.getValue().getTimerkey().equals(optimerkey)) {
                // 対象のすべての列車の名前を収集する
                for (var spn : entry.getValue().asList()) {
                    trainnamelist.add(spn.getSpawnTrainName());
                }
                ymllspn.set(entry.getKey(), entry.getValue());
            }
        }

        // TrainRecord
        var ymlrecord = yamlcfg.createSection("trainrecord");
        var recordstore = plugin.getTrainRecordStore();
        // 対象の列車のみ適用
        for (var trainname : trainnamelist) {
            var record = recordstore.get(trainname);
            if (record != null) {
                ymlrecord.set(trainname, record);
            }
        }

        // TrainPreset
        var ymlpreset = yamlcfg.createSection("trainpreset");
        var presetstore = plugin.getTrainPresetStore();
        // あとのroute用
        var routelist = new ArrayList<String>();
        for (var trainname : trainnamelist) {
            var preset = presetstore.get(trainname);
            if (preset != null) {
                // routeがnullでなければ次に収集する
                if (preset.getRouteName() != null) {
                    routelist.add(trainname);
                }
                ymlpreset.set(trainname, preset);
            }
        }

        // Route
        var ymlroute = yamlcfg.createSection("route");
        for (var routename : routelist) {
            ymlroute.set(routename, plugin.getTrainCarts().getRouteManager().findRoute(routename));
        }

        // どうにかして保存したいが・・・
        var file = new File(plugin.getDataFolder(), "aatdump" + File.pathSeparator + args[1] + ".yml");
        try {
            yamlcfg.save(file);
        } catch (IOException e) {
            sender.sendMessage("データの保存に失敗しました。");
        }

        sender.sendMessage("OPTimer " + optimerkey + " に関連するデータのダンプに成功しました。");
        return true;
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
