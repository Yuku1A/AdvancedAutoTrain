package io.github.yuku1a.advancedautotrain.trainarrivallist;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSign;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandTrainArrivalSign implements CommandExecutor {

    private boolean autoclean(CommandSender sender, String[] args) {
        // コマンド指定で1、lspawnlist指定で1
        if (args.length != 2)
            return commandsHelp(sender, "autoclean");

        // 必要な情報を集める
        var autosetdata = gatheringAutoSetData(sender, args[1]);
        // 異常な場合nullでメッセージがすでに出てるのでreturn
        if (autosetdata == null)
            return true;

        // 駅と列車の情報が集まったので次に駅のクリーンアップ
        cleanAndPause(autosetdata);

        // 作業中に止めたArrivalListを再開
        resumeArrivalSign(autosetdata);

        sender.sendMessage("処理を完了しました。");

        return true;
    }
    private boolean autoset(CommandSender sender, String[] args) {
        // コマンド指定で1、lspawnlist指定で1
        if (args.length != 2)
            return commandsHelp(sender, "autoset <lspawnlist>");

        // 必要な情報を集めてくる
        var autosetdata = gatheringAutoSetData(sender, args[1]);
        // 異常な場合nullでメッセージがすでに出てるのでreturn
        if (autosetdata == null)
            return true;

        // 駅と列車の情報が集まったので次に駅のクリーンアップ
        cleanAndPause(autosetdata);

        // 作業開始
        for (var train : autosetdata.trainDataList()) {
            for (var station : train.stationDataList()) {
                // 作業前にarrivallistを止めておく
                station.arrivalList().pause();

                // ここから計算開始
                var timeinterval = autosetdata.opTimer().getInterval();
                var spawntime = train.lSpawn().getScheduletime();
                var offsetsec = station.arrivalSignEntry().getSecondsOffset();
                var recordtime = station.recordEntry().getRecordedAt();

                // 計算
                var arrivetime = calculateArriveTime(spawntime, recordtime, timeinterval, offsetsec);

                // 他に必要な情報を集める
                var displayname = station.arrivalSignEntry().getTrainName();
                var traindescription = station.arrivalSignEntry().getTrainDescription();
                var internalname = train.lSpawn().getSpawnTrainName();

                // 登録
                station.arrivalList().add(new ScheduledSign(arrivetime, displayname, traindescription, internalname));
            }
        }

        // 作業中に止めたArrivalListを再開
        resumeArrivalSign(autosetdata);

        sender.sendMessage("処理を完了しました。");

        return true;
    }

    private void resumeArrivalSign(AutoSetData autosetdata) {
        for (var arrivalsign : autosetdata.stationlist()) {
            arrivalsign.resume();
        }
    }

    private void cleanAndPause(AutoSetData autosetdata){
        // クリーンアップをする
        for (var al : autosetdata.stationlist()) {
            for (var as : al.asList()) {
                // 必要になってからpauseしたほうが若干コストが下がる
                // 内部名が設定されていなければ削除してしまう
                if (as.getTrainInternalName() == null) {
                    al.pause();
                    al.remove(as.getScheduletime());
                }
                // 全部探してクリーンアップする
                if (autosetdata.trainNameList().contains(as.getTrainInternalName())) {
                    al.pause();
                    al.remove(as.getScheduletime());
                }
            }
        }
    }

    private long calculateArriveTime(long spawntimemillis, long recordtimemillis, long timerintervalmillis, long offsetseconds) {
        // 到着時刻の計算、単純に足してからオーバーしてる分をintervalに合わせる
        var arrivetimeraw = (spawntimemillis + recordtimemillis) % timerintervalmillis;

        // 1秒単位にしつつオフセットも適用
        var offsetapplied = ((arrivetimeraw / 1000) + offsetseconds) * 1000;

        // マイナス防止
        long arrivetime;
        if (offsetapplied < 0)
            arrivetime = timerintervalmillis + offsetapplied;
        else
            arrivetime = offsetapplied;

        // おわり
        return arrivetime;
    }

    private AutoSetData gatheringAutoSetData(CommandSender sender, String lspnlistkey) {
        sender.sendMessage("処理を開始します。");
        // 情報集め
        var lspnset = plugin.getSpawnListStore().get(lspnlistkey);
        if (lspnset == null) {
            sender.sendMessage("指定されたlspawnのリストは存在しません。");
            sender.sendMessage("処理を中止します。");
            return null;
        }

        // まず中身のリストを取り出す
        var lspnlist = lspnset.asList();

        // 必要なものを集める
        // まずOPTimerの名前から
        var optimername = lspnset.getTimerkey();
        var optimer = plugin.getOperationTimerStore().get(optimername);
        if (optimer == null) {
            sender.sendMessage("設定されているOPTimerは存在しません。");
            sender.sendMessage("処理を中止します。");
            return null;
        }

        // 列車と駅の名前を過不足なくすべて集める必要がある
        var trainnamelist = new ArrayList<String>();

        var stationlist = new ArrayList<ScheduledSignSet>();
        var traindatalist = new ArrayList<AutoSetTrainData>();

        var traintempmap = new HashMap<String, AutoSetTrainData>();

        // 賢い方法がわかんないので力技。ラグなど知らぬ
        // 列車ごと
        for (var spawn : lspnlist) {
            // 検証を行いながら列車名を集める
            // CStationにArrivalSignが連動するのでCStationと、
            // 時間を登録するのに時間の情報がいるが、TrainRecordのワンストップで済む。いいね
            var trainname = spawn.getSpawnTrainName();

            // 駅の情報はコンテキストが違ったり列車が違ったりするので被り上等だが
            // 列車の情報は名前ごとに固有である想定でいってるのでここでコピーにしたつもり
            var temptraindata = traintempmap.get(trainname);
            if (temptraindata != null) {
                var newtraindata = new AutoSetTrainData(spawn, temptraindata.stationDataList(), trainname);
                traindatalist.add(newtraindata);
                continue;
            }

            var trec = plugin.getTrainRecordStore().get(trainname);
            if (trec == null) {
                sender.sendMessage("列車 " + trainname + " のTrainRecordが存在しません。");
                continue;
            }

            // 当然TrainArrivalListもいる
            var tal = store.get(trainname);
            if (tal == null) {
                sender.sendMessage("列車 " + trainname + " のTrainArrivalListが存在しません。");
                continue;
            }

            // TrainArrivalListのインデックスを別に用意
            int talindex = 0;

            var stationdatalist = new ArrayList<AutoSetStationData>();
            // TrainRecordEntryごと
            // 駅ごとに処理
            for (var recordentry : trec.asList()) {
                var entryTrainRecord = recordentry.getTrainRecord();
                // cstation_leaveさえあればいい
                if (!entryTrainRecord.getActionType().equals("cstation_leave"))
                    continue;

                // Actedじゃなければ通過してるので使わない
                if (!entryTrainRecord.isActed())
                    continue;

                // 駅の名前のデータをとる
                var entryTrainRecordSignName = entryTrainRecord.getSignName();

                // 停車してる分のデータだけになったので
                // これがTrainArrivalListと整合性があるかチェックする
                // これの整合性がないのはreturn相当
                var arrivalSignEntry = tal.get(talindex);
                if (!arrivalSignEntry.getCStationName().equals(entryTrainRecordSignName)) {
                    sender.sendMessage("列車 " + trainname + " では、TrainArrivalListとTrainRecordの整合性がありません。");
                    sender.sendMessage("処理を中止します。");
                    return null;
                }

                // CStation側のArrivalListを持ってきて検証
                var arrivalList = plugin.getSignListStore().get(entryTrainRecordSignName);
                if (arrivalList == null) {
                    sender.sendMessage("CStation " + entryTrainRecordSignName + " に関連するArrivalListの登録がありません。");
                    arrivalList = new ScheduledSignSet(optimername);
                    arrivalList.enable(plugin);
                    plugin.getSignListStore().put(entryTrainRecordSignName, arrivalList);
                    sender.sendMessage("CStation " + entryTrainRecordSignName + " に関連するArrivalListの登録をしました。");
                }

                // OPTimerが違うとこれをやる意味が完全になくなる
                if (!arrivalList.getTimerkey().equals(optimername)){
                    sender.sendMessage("LSpawnList " + lspnlistkey + " とArrivalList " + entryTrainRecordSignName + " では参照しているOPTimerが異なります。");
                    sender.sendMessage("処理を中止します。");
                    return null;
                }

                // リストに追加
                var stationdata = new AutoSetStationData(recordentry, arrivalSignEntry, arrivalList, entryTrainRecordSignName);

                stationdatalist.add(stationdata);
                if (!stationlist.contains(arrivalList))
                    stationlist.add(arrivalList);

                // これで一駅終わって次へ
                talindex++;
            }

            var traindata = new AutoSetTrainData(spawn, stationdatalist, trainname);
            traindatalist.add(traindata);
            trainnamelist.add(trainname);

            // 二回以上同じ列車の駅データを新規生成しない
            traintempmap.put(trainname, traindata);
        }

        return new AutoSetData(optimer, traindatalist, stationlist, trainnamelist);
    }

    // addコマンド
    private boolean add(CommandSender sender, String[] args) {
        // 引数のヘルプ用のテキスト
        var argtext = "<list> <displayname> <cstationname> [offset] [description]";

        var command = args[0];

        if (command.equals("add")) {
            // コマンド指定で1つ、リスト指定で1つ、パラメータが2つ、オプション2つで6つ
            if ((4 > args.length) || (args.length > 6)) {
                return commandsHelp(
                    sender,
                    "add " + argtext
                );
            }
        } else if(command.equals("replace") || command.equals("insert")) {
            // コマンド指定で1つ、リスト指定で1つ、パラメータが2つ、オプション2つ、インデックス1つで7つ
            if ((5 > args.length) || (args.length > 7)) {
                return commandsHelp(
                    sender,
                    args[0] + " " + argtext + " <index>"
                );
            }
        } else {
            return false;
        }

        // リスト指定
        var key = args[1];
        // リストを取ってくる
        var list = listOrNull(sender, key);
        // nullだったらメッセージが出てるのでそのまま返す
        if (list == null)
            return true;

        // 取り出し、型変換
        var displayname = args[2];
        var cstationname = args[3];

        // displayname以外がオプションであるかインデックスなので慎重に調べる
        long offset;
        long defaultoffset = -2;
        String description;

        // オプションがない場合
        if ((command.equals("add") && args.length == 4) || (!(command.equals("add"))) && args.length == 5) {
            offset = defaultoffset;
            description = null;
        } else {
            // 最後の判定に必要
            // addコマンド以外だとindexがあるのでargs.lengthでは判定できない
            // オプションなしが4
            int argnum;
            if (command.equals("add"))
                argnum = args.length;
            else
                argnum = args.length - 1;

            // インデックスを進めていってあれこれ
            int argindex = 4;

            // OffSetを取り出す
            long argoffset;
            try {
                argoffset = Long.parseLong(args[argindex]);
                argindex++;
            } catch (NumberFormatException e) {
                argoffset = defaultoffset;
            }
            offset = argoffset;

            // descriptionを取り出す
            // 全部乗せだったらindexは5
            if (argindex < argnum){
                description = args[argindex];
                argindex++;
            } else
                description = null;

            // 正しければこれはelse側を通る
            if (argindex < argnum) {
                sender.sendMessage("引数が不正です");
                return true;
            }
        }

        var entry = new ArrivalSignEntry(cstationname, displayname, offset, description);

        // add以外だとindexが存在するので読み取る
        int index;
        if (!command.equals("add")) {
            index = CommandUtil.tryParseIndex(sender, list, args[args.length - 1]);
            // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
            if (index == -1)
                return true;
        } else
            index = -1;

        // addコマンドだとそのままinfoをlistへ追加、そのまま完了
        switch (command) {
            case "add" -> {
                list.add(entry);
                sender.sendMessage("要素の追加を完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, entry, list.size() - 1);
            }
            case "replace" -> {
                list.set(index, entry);
                sender.sendMessage("項目の置き換えが完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, entry, index);
            }
            case "insert" -> {
                list.add(index, entry);
                sender.sendMessage("項目の挿入が完了しました。");
                // 追加したものの内容を表示する
                infoViewOne(sender, entry, index);
            }
        }

        // 終わり
        return true;
    }

    // createコマンド
    private boolean create(CommandSender sender, String[] args) {
        // コマンドで1つ、リスト名で1つ
        if (args.length != 2)
            return commandsHelp(sender, "create <trainname>");

        // とりあえず作る
        store.put(args[1], new ArrayList<>());

        sender.sendMessage("リスト " + args[1] + " を作成しました。");

        return true;
    }

    // removeコマンド
    private boolean remove(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、インデックスで1つ
        if (args.length != 3)
            return commandsHelp(sender, "remove <listname> <index>");

        // リストを取ってくる
        var list = listOrNull(sender, args[1]);

        // nullだったらメッセージが出てるのでそのまま返す
        if (list == null)
            return true;

        // 検査
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // ひっかかってたら弾く
        if (index == -1)
            return true;

        // 実際の削除処理
        list.remove(index);
        sender.sendMessage("リスト " + args[1] + " の " + index + "番目の項目が削除されました。");

        // おわり
        return true;
    }

    // viewコマンド
    private boolean view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3))
            return commandsHelp(sender, "view <template> <page>");

        // 名前を出しておく
        var listname = args[1];

        // リストを取ってくる
        var rawlist = listOrNull(sender, listname);

        // nullだったらメッセージが出てるのでそのまま返す
        if (rawlist == null)
            return true;

        // ページングを丸投げ
        var list = CommandUtil.pager2D(rawlist, 9);

        String pageindexstr;
        // 指定がなかったらインデックスを1として扱う
        if (args.length == 2)
            pageindexstr = "1";
        else
            pageindexstr = args[2];

        // インデックスをパース
        int pageindex = CommandUtil.tryParsePagingIndex(sender, list, pageindexstr);
        // インデックスが異常だったらすでにメッセージが送られている
        if (pageindex == -1)
            return true;

        // 分割されたリストの指定されたやつ
        var pagedlist = list.get(pageindex);

        // 分割されたやつを表示するUI部分
        sender.sendMessage(
            "----- " + listname + " trainarrivallist page " + pageindexstr + " of " + list.size() + " -----"
        );
        sender.sendMessage("(index) (cstationname) (displayname) (description) (offset)");

        // 分割されたリストの中身を表示
        for (var entry : pagedlist) {
            infoViewSimple(sender, entry.getData(), entry.getIndex());
        }

        // おわり
        return true;
    }

    // infoを一つだけ表示する用
    private void infoViewOne(CommandSender sender, ArrivalSignEntry entry, int index) {
        // うまいこと内容を表示する
        sender.sendMessage("(index) (cstationname) (displayname) (description) (offset)");
        infoViewSimple(sender, entry, index);
    }

    // 内容を実際に表示する
    private void infoViewSimple(CommandSender sender, ArrivalSignEntry entry, int index) {
        sender.sendMessage(index + " | " + entry.getCStationName() + " | " + entry.getTrainName() + " | " +
                               entry.getTrainDescription() + " | " + entry.getSecondsOffset());
    }

    // ボイラープレートじみたコード類
    // listコマンド
    private boolean list(CommandSender sender, String[] args) {
        // コマンドで1つ、ページで1つまで
        if (args.length > 2)
            return commandsHelp(sender, "list [page]");

        // storeからキーのコレクションを取得する
        var rawlist = store.keysList();

        // 指定がなければ1ページ扱い
        String index = args.length == 1 ? "1" : args[1];

        // ページングを丸投げ
        var list = CommandUtil.pager(sender, rawlist, index);

        // listがnullだったら警告文とかも出てるのでおわり
        if (list == null)
            return true;

        // 分割されたやつを表示
        sender.sendMessage(
            "----- trainarrivallist page " +
                index + " of " + CommandUtil.calcMaxPageIndex(rawlist) +
                " -----"
        );
        list.forEach(sender::sendMessage);

        // おわり
        return true;
    }

    // removetコマンド
    private boolean removet(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ
        if (args.length != 2)
            return commandsHelp(sender, "rmlist <list>");

        // 指定されたテンプレートを削除
        store.remove(args[1]);
        sender.sendMessage(
            "指定されたリスト " + args[1] +
                " は、削除されました。"
        );
        return true;
    }

    // copyコマンド
    private boolean copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3)
            return commandsHelp(sender, "copy <from> <to>");

        // リストを取ってくる
        var from = listOrNull(sender, args[1]);

        // nullだったらメッセージが出てるのでそのまま返す
        if (from == null)
            return true;

        // storeへコピーしたリストをset
        store.put(args[2], new ArrayList<>(from));

        // おわり
        sender.sendMessage("コピーが完了しました。");
        return true;
    }

    // saveコマンド
    private boolean save(CommandSender sender) {
        if (store.save())
            sender.sendMessage("Save Successful!");
        else
            sender.sendMessage("Save Failed");
        return true;
    }

    // loadコマンド
    private boolean load(CommandSender sender) {
        if (store.load())
            sender.sendMessage("Data Loaded");
        else
            sender.sendMessage("Data Load Failed");
        return true;
    }

    private List<ArrivalSignEntry> listOrNull(CommandSender sender, String key) {
        // とりあえず取りに行く
        var list = store.get(key);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            sender.sendMessage("指定された名前のリストは登録されていません。");
            return null;
        }

        return list;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // パーミッションチェック
        if (!sender.hasPermission(plugin.UsePermission)) {
            sender.sendMessage("必要な権限がありません");
            return true;
        }
        // 引数0の場合はコマンドが指定されていない
        if (args.length == 0)
            return help(sender);

        // 管理用コマンドだけ追加の権限チェック
        switch (args[0]) {
            case "load","save" -> {
                if (!sender.hasPermission(plugin.AdminPermission)){
                    sender.sendMessage("必要な権限がありません");
                    return true;
                }
            }
        }

        // コマンドごとに分岐やる、この構文めちゃ便利
        return switch (args[0]) {
            case "list" -> list(sender, args);
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            case "add","replace","insert" -> add(sender, args);
            case "create" -> create(sender, args);
            case "remove" -> remove(sender, args);
            case "rmlist" -> removet(sender, args);
            case "copy" -> copy(sender, args);
            case "autoset" -> autoset(sender, args);
            case "autoclean" -> autoclean(sender, args);
            default -> help(sender);
        };
    }

    // helpコマンド
    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "列車に紐づくArrivalSignのリストを管理します",
            "利用可能なコマンド: ",
            "add: 項目を追加します",
            "remove: 項目を削除します",
            "view: 指定されたリストの項目を表示します",
            "list: リストの一覧を表示します",
            "rmlist: 指定したリストを削除します",
            "copy: リストのコピーを行います",
            "replace: リスト内の指定された項目を入れ替えます",
            "insert: リスト内の指定された位置に項目を追加します",
            "autoset: LSpawnのリストと他の関連するデータ基づいてArrivalSignを自動設定します",
            "autoclean: LSpawnのリストにある列車に関連するArrivalSignのデータを削除します"
        );
        if (sender.hasPermission(plugin.AdminPermission)) {
            sender.sendMessage(
                "save: 全ての情報を保存します",
                "load: 全ての情報の再読み込みを行います"
            );
        }
        return false;
    }

    private final TrainArrivalSignStore store;
    private final Advancedautotrain plugin;

    // プラグインが生成する用
    public CommandTrainArrivalSign(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getTrainArrivalSignStore();
    }

    private boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", "tal " + usage);
        return true;
    }
}
