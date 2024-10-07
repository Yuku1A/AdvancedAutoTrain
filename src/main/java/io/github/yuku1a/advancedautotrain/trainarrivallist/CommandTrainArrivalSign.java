package io.github.yuku1a.advancedautotrain.trainarrivallist;

import com.bergerkiller.bukkit.sl.API.Variables;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CommandUtil;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSign;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSet;
import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandTrainArrivalSign implements CommandExecutor, TabCompleter {

    private void autoclean(CommandSender sender, String[] args) {
        // コマンド指定で1、lspawnlist指定で1
        if (args.length != 2) {
            commandsHelp(sender, "autoclean <lspawnlist>");
            return;
        }

        // 必要な情報を集める
        var autosetdata = gatheringAutoSetData(sender, args[1]);
        // 異常な場合nullでメッセージがすでに出てるのでreturn
        if (autosetdata == null)
            return;

        // 駅と列車の情報が集まったので次に駅のクリーンアップ
        cleanAndPause(autosetdata);

        // 作業中に止めたArrivalListを再開
        resumeArrivalSign(autosetdata);

        sender.sendMessage("処理を完了しました。");

    }

    private List<String> autocleanTab(String[] args) {
        // LSpawnSignをサジェスト
        if (args.length == 2) {
            var lspawnSigns = plugin.getSpawnListStore().keysList();
            return TabCompleteUtil.searchInList(args[1], lspawnSigns);
        }

        return null;
    }

    private void autoset(CommandSender sender, String[] args) {
        // コマンド指定で1、lspawnlist指定で1
        if (args.length != 2) {
            commandsHelp(sender, "autoset <lspawnlist>");
            return;
        }

        // 必要な情報を集めてくる
        var autosetdata = gatheringAutoSetData(sender, args[1]);
        // 異常な場合nullでメッセージがすでに出てるのでreturn
        if (autosetdata == null)
            return;

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

    }

    private List<String> autosetTab(String[] args) {
        // LSpawnSignをサジェスト
        if (args.length == 2) {
            var lspawnSigns = plugin.getSpawnListStore().keysList();
            return TabCompleteUtil.searchInList(args[1], lspawnSigns);
        }

        return null;
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
                if (!arrivalList.getTimerkey().equals(optimername)) {
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

    private void add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、インデックス1つ、パラメータが2つ、オプション2つで7つ
        if (args.length < 4) {
            commandsHelp(sender, "add <list> <displayname> <cstationname> [offset] [description]");
            return;
        }

        // リスト取得
        var trainName = args[1];
        var list = store.get(trainName);
        if (list == null) {
            msgListNotFound(sender);
            return;
        }

        // 必須引数
        var displayName = args[2];
        var cstationName = args[3];

        // オプション引数
        long offset;
        long defaultOffset = -2;
        String description;

        if (args.length == 4) {
            // オプションがない場合
            offset = defaultOffset;
            description = null;
        } else if (args.length == 5){
            // めんどくさい
            // オフセットかdescriptionかの片方のみのパターン
            long argOffset;
            String argDescription;
            try {
                argOffset = Long.parseLong(args[4]);
                argDescription = null;
            } catch (NumberFormatException e) {
                argOffset = defaultOffset;
                argDescription = args[4];
            }
            offset = argOffset;
            description = argDescription;
        } else {
            // 両方あるパターン、多少楽
            try {
                offset = Long.parseLong(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage("オフセットの形式が間違っています。");
                return;
            }
            description = args[5];
        }

        // 生成して追加
        var entry = new ArrivalSignEntry(cstationName, displayName, offset, description);
        list.add(entry);
        sender.sendMessage("要素の追加を完了しました。");
        // 追加したものの内容を表示する
        infoViewOne(sender, entry, list.size() - 1);

    }

    private List<String> addTab(String[] args) {
        // インスペクション対策
        if (args.length <= 1)
            return null;

        // add <list> <displayname> <cstationname> [offset] [description]

        // 列車名のサジェスト
        var trainName = args[1];
        if (args.length == 2) {
            var trainNames = store.keysList();
            return TabCompleteUtil.searchInList(trainName, trainNames);
        }

        // リストがあればそれに基づくサジェスト
        var list = store.get(trainName);
        var isListEmpty = list == null || list.isEmpty();

        // displayNameに使うためのSignLinkの変数をサジェスト
        if (args.length == 3) {
            if (isListEmpty)
                return searchIfVariable(args[2]);

            if (args[2].startsWith("%"))
                return searchIfVariable(args[2]);
            else
                return List.of(list.get(list.size() - 1).getTrainName());
        }

        // CStationの名前をサジェスト
        if (args.length == 4) {
            var cstations = plugin.getCStationCacheSet().get();
            return TabCompleteUtil.searchInList(args[3], cstations);
        }

        // offsetは前の項目次第
        if (args.length == 5) {
            if (isListEmpty)
                return null;

            // リストがあって項目もあるなら最後尾の項目からサジェストする
            var entry = list.get(list.size() - 1);
            return List.of(String.valueOf(entry.getSecondsOffset()));
        }

        // descriptionはSignLinkの変数を使うことも多いのでそれをサジェスト
        if (args.length == 6) {
            if (isListEmpty)
                return searchIfVariable(args[5]);

            if (args[5].startsWith("%"))
                return searchIfVariable(args[5]);
            else
                return List.of(list.get(list.size() - 1).getTrainDescription());
        }

        return null;
    }

    private List<String> searchIfVariable(String query) {
        if (!query.startsWith("%"))
            return null;

        // 先頭の%を取り除く
        var newQuery = query.substring(1);

        // 完全に入力し終わってる場合は返さない
        if (newQuery.endsWith("%"))
            return null;

        // SignLink内を検索
        var varNameList = searchInSignLink(newQuery);

        // そのままではサジェストにならないので変数表記に変換する
        var resultList = new ArrayList<String>();
        varNameList.forEach(e -> {
            var after = "%" + e + "%";
            resultList.add(after);
        });
        return resultList;
    }

    private List<String> searchInSignLink(String query) {
        // SignLinkの変数の名前を集めてくる
        var varList = Variables.getAllAsList();
        var varNameList = new ArrayList<String>();
        varList.forEach(e -> varNameList.add(e.getName()));

        return TabCompleteUtil.searchInList(query, varNameList);
    }

    private void insert(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、インデックス1つ、パラメータが2つ、オプション2つで7つ
        if (args.length < 5) {
            commandsHelp(sender, "insert <list> <index> <displayname> <cstationname> [offset] [description]");
            return;
        }

        // リスト取得
        var trainName = args[1];
        var list = store.get(trainName);
        if (list == null) {
            msgListNotFound(sender);
            return;
        }

        // インデックス
        var indexStr = args[2];
        var index = CommandUtil.tryParseIndex(sender, list, indexStr);
        // ダメだった場合、メッセージは表示済み
        if (index == -1)
            return;

        // 必須引数
        var displayName = args[3];
        var cstationName = args[4];

        // オプション引数
        long offset;
        long defaultOffset = -2;
        String description;

        if (args.length == 5) {
            // オプションがない場合
            offset = defaultOffset;
            description = null;
        } else if (args.length == 6){
            // めんどくさい
            // オフセットかdescriptionかの片方のみのパターン
            long argOffset;
            String argDescription;
            try {
                argOffset = Long.parseLong(args[5]);
                argDescription = null;
            } catch (NumberFormatException e) {
                argOffset = defaultOffset;
                argDescription = args[5];
            }
            offset = argOffset;
            description = argDescription;
        } else {
            // 両方あるパターン、多少楽
            try {
                offset = Long.parseLong(args[5]);
            } catch (NumberFormatException e) {
                sender.sendMessage("オフセットの形式が間違っています。");
                return;
            }
            description = args[6];
        }

        // 生成して追加
        var entry = new ArrivalSignEntry(cstationName, displayName, offset, description);
        list.add(index, entry);
        sender.sendMessage("項目の挿入が完了しました。");
        // 追加したものの内容を表示する
        infoViewOne(sender, entry, index);

    }

    private List<String> insertTab(String[] args) {
        // インスペクション対策
        if (args.length <= 1)
            return null;

        // insert <list> <index> <displayname> <cstationname> [offset] [description]

        // 列車名のサジェスト
        var trainName = args[1];
        if (args.length == 2) {
            var trainNames = store.keysList();
            return TabCompleteUtil.searchInList(trainName, trainNames);
        }

        // インデックスまでしかない
        if (args.length == 3)
            return null;

        // 挿入先のリストと一つ前の項目を探す
        var list = store.get(trainName);
        if (list == null || list.isEmpty())
            return null;

        var index = CommandUtil.tryParseIndexSilent(list, args[2]);
        if (index < 0)
            return null;

        var entry = list.get(index);

        // displayNameに使うためのSignLinkの変数をサジェスト
        if (args.length == 4) {
            if (args[3].startsWith("%"))
                return searchIfVariable(args[3]);
            else
                return List.of(entry.getTrainName());
        }

        // CStationの名前をサジェスト
        if (args.length == 5) {
            var cstations = plugin.getCStationCacheSet().get();
            return TabCompleteUtil.searchInList(args[4], cstations);
        }

        // offsetは前の項目次第
        if (args.length == 6) {
            // リストがあって項目もあるなら最後尾の項目からサジェストする
            return List.of(String.valueOf(entry.getSecondsOffset()));
        }

        // descriptionはSignLinkの変数を使うことも多いのでそれをサジェスト
        if (args.length == 7) {
            if (args[6].startsWith("%"))
                return searchIfVariable(args[6]);
            else
                return List.of(entry.getTrainDescription());
        }

        return null;
    }

    private void replace(CommandSender sender, String[] args) {
        // コマンド指定で1つ、リスト指定で1つ、インデックス1つ、パラメータが2つ、オプション2つで7つ
        if (args.length < 5) {
            commandsHelp(sender, "replace <list> <index> <displayname> <cstationname> [offset] [description]");
            return;
        }

        // リスト取得
        var trainName = args[1];
        var list = store.get(trainName);
        if (list == null) {
            msgListNotFound(sender);
            return;
        }

        // インデックス
        var indexStr = args[2];
        var index = CommandUtil.tryParseIndex(sender, list, indexStr);
        // ダメだった場合、メッセージは表示済み
        if (index == -1)
            return;

        // 必須引数
        var displayName = args[3];
        var cstationName = args[4];

        // オプション引数
        long offset;
        long defaultOffset = -2;
        String description;

        if (args.length == 5) {
            // オプションがない場合
            offset = defaultOffset;
            description = null;
        } else if (args.length == 6){
            // めんどくさい
            // オフセットかdescriptionかの片方のみのパターン
            long argOffset;
            String argDescription;
            try {
                argOffset = Long.parseLong(args[5]);
                argDescription = null;
            } catch (NumberFormatException e) {
                argOffset = defaultOffset;
                argDescription = args[5];
            }
            offset = argOffset;
            description = argDescription;
        } else {
            // 両方あるパターン、多少楽
            try {
                offset = Long.parseLong(args[5]);
            } catch (NumberFormatException e) {
                sender.sendMessage("オフセットの形式が間違っています。");
                return;
            }
            description = args[6];
        }

        // 生成して追加
        var entry = new ArrivalSignEntry(cstationName, displayName, offset, description);
        list.set(index, entry);
        sender.sendMessage("項目の置き換えが完了しました。");
        // 追加したものの内容を表示する
        infoViewOne(sender, entry, index);

    }

    private List<String> replaceTab(String[] args) {
        // インスペクション対策
        if (args.length <= 1)
            return null;

        // replace <list> <index> <displayname> <cstationname> [offset] [description]

        // 列車名のサジェスト
        var trainName = args[1];
        if (args.length == 2) {
            var trainNames = store.keysList();
            return TabCompleteUtil.searchInList(trainName, trainNames);
        }

        // インデックスまでしかない
        if (args.length == 3)
            return null;

        // インデックス以降がある
        // 置き換え対象があるかどうかをチェック
        var list = store.get(trainName);
        if (list == null)
            return null;

        var indexStr = args[2];
        var index = CommandUtil.tryParseIndexSilent(list, indexStr);
        // インデックスが不正
        if (index < 0)
            return null;

        var oldValue = list.get(index);

        // displayNameに使うためのSignLinkの変数をサジェスト
        // 置き換えなので最初は同じものをサジェスト
        if (args.length == 4) {
            if (args[3].startsWith("%"))
                return searchIfVariable(args[3]);
            else
                return List.of(oldValue.getTrainName());
        }

        // CStationの名前をサジェスト
        // 置き換えなので同じものをサジェスト
        if (args.length == 5) {
            return List.of(oldValue.getCStationName());
        }

        // offsetは前の項目次第
        if (args.length == 6) {// リストがあって項目もあるなら最後尾の項目からサジェストする
            return List.of(String.valueOf(oldValue.getSecondsOffset()));
        }

        // descriptionはSignLinkの変数を使うことも多いのでそれをサジェスト
        if (args.length == 7) {
            if (args[6].startsWith(("%")))
                return searchIfVariable(args[6]);
            else
                return List.of(oldValue.getTrainDescription());
        }

        return null;
    }

    // createコマンド
    private void create(CommandSender sender, String[] args) {
        // コマンドで1つ、リスト名で1つ
        if (args.length != 2) {
            commandsHelp(sender, "create <trainname>");
            return;
        }

        // とりあえず作る
        store.put(args[1], new ArrayList<>());

        sender.sendMessage("リスト " + args[1] + " を作成しました。");

    }

    private List<String> createTab(String[] args) {
        // 列車に紐づけて作るのでTrainPresetの登録名をサジェスト
        if (args.length == 2) {
            var trains = plugin.getTrainPresetStore().keysList();
            return TabCompleteUtil.searchInList(args[1], trains);
        }

        return null;
    }

    // removeコマンド
    private void remove(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、インデックスで1つ
        if (args.length != 3) {
            commandsHelp(sender, "remove <listname> <index>");
            return;
        }

        // リストを取ってくる
        var list = listOrNull(sender, args[1]);

        // nullだったらメッセージが出てるのでそのまま返す
        if (list == null)
            return;

        // 検査
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // ひっかかってたら弾く
        if (index == -1)
            return;

        // 実際の削除処理
        list.remove(index);
        sender.sendMessage("リスト " + args[1] + " の " + index + "番目の項目が削除されました。");

        // おわり
    }

    private List<String> removeTab(String[] args) {
        // 登録済みリストをサジェスト
        if (args.length == 2) {
            var trains = store.keysList();
            return TabCompleteUtil.searchInList(args[1], trains);
        }

        return null;
    }

    // viewコマンド
    private void view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3)) {
            commandsHelp(sender, "view <trainname> <page>");
            return;
        }

        // 名前を出しておく
        var listname = args[1];

        // リストを取ってくる
        var rawlist = listOrNull(sender, listname);

        // nullだったらメッセージが出てるのでそのまま返す
        if (rawlist == null)
            return;

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
            return;

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
    }

    private List<String> viewTab(String[] args) {
        // 登録済みリストをサジェスト
        if (args.length == 2) {
            var trains = store.keysList();
            return TabCompleteUtil.searchInList(args[1], trains);
        }

        return null;
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
    private void list(CommandSender sender, String[] args) {
        // コマンドで1つ、ページで1つまで
        if (args.length > 2) {
            commandsHelp(sender, "list [page]");
            return;
        }

        // storeからキーのコレクションを取得する
        var rawlist = store.keysList();

        // 指定がなければ1ページ扱い
        String index = args.length == 1 ? "1" : args[1];

        // ページングを丸投げ
        var list = CommandUtil.pager(sender, rawlist, index);

        // listがnullだったら警告文とかも出てるのでおわり
        if (list == null)
            return;

        // 分割されたやつを表示
        sender.sendMessage(
            "----- trainarrivallist page " +
                index + " of " + CommandUtil.calcMaxPageIndex(rawlist) +
                " -----"
        );
        list.forEach(sender::sendMessage);

        // おわり
    }

    // removetコマンド
    private void rmlist(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ
        if (args.length != 2) {
            commandsHelp(sender, "rmlist <list>");
            return;
        }

        // 指定されたテンプレートを削除
        store.remove(args[1]);
        sender.sendMessage(
            "指定されたリスト " + args[1] +
                " は、削除されました。"
        );
    }

    private List<String> rmlistTab(String[] args) {
        // 登録済みリストをサジェスト
        if (args.length == 2) {
            var trains = store.keysList();
            return TabCompleteUtil.searchInList(args[1], trains);
        }

        return null;
    }

    // copyコマンド
    private void copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3) {
            commandsHelp(sender, "copy <from> <to>");
            return;
        }

        // リストを取ってくる
        var from = listOrNull(sender, args[1]);

        // nullだったらメッセージが出てるのでそのまま返す
        if (from == null)
            return;

        // storeへコピーしたリストをset
        store.put(args[2], new ArrayList<>(from));

        // おわり
        sender.sendMessage("コピーが完了しました。");
    }

    private List<String> copyTab(String[] args) {
        // 登録済みリストをサジェスト
        if (args.length == 2) {
            var trains = store.keysList();
            return TabCompleteUtil.searchInList(args[1], trains);
        }

        // 列車に紐づけて作るのでTrainPresetの登録名をサジェスト
        if (args.length == 3) {
            var trains = plugin.getTrainPresetStore().keysList();
            return TabCompleteUtil.searchInList(args[2], trains);
        }

        return null;
    }

    // saveコマンド
    private void save(CommandSender sender) {
        if (store.save())
            sender.sendMessage("Save Successful!");
        else
            sender.sendMessage("Save Failed");
    }

    // loadコマンド
    private void load(CommandSender sender) {
        if (store.load())
            sender.sendMessage("Data Loaded");
        else
            sender.sendMessage("Data Load Failed");
    }

    private List<ArrivalSignEntry> listOrNull(CommandSender sender, String key) {
        // とりあえず取りに行く
        var list = store.get(key);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            msgListNotFound(sender);
            return null;
        }

        return list;
    }

    private void msgListNotFound(CommandSender sender) {
        sender.sendMessage("指定された名前のリストは登録されていません。");
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
        switch (args[0]) {
            case "list" -> list(sender, args);
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            case "add" -> add(sender, args);
            case "replace" -> replace(sender, args);
            case "insert" -> insert(sender,args);
            case "create" -> create(sender, args);
            case "remove" -> remove(sender, args);
            case "rmlist" -> rmlist(sender, args);
            case "copy" -> copy(sender, args);
            case "autoset" -> autoset(sender, args);
            case "autoclean" -> autoclean(sender, args);
            default -> help(sender);
        }

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // パーミッションチェック
        if (!sender.hasPermission(plugin.UsePermission))
            return null;

        // コマンドのサジェスト
        if (args.length == 1) {
            return List.of("create", "add", "remove", "view", "list", "rmlist", "copy",
                           "replace", "insert", "autoset", "autoclean");
        }

        // 各コマンドの詳細なサジェスト
        return switch (args[0]) {
            case "create" -> createTab(args);
            case "add" -> addTab(args);
            case "remove" -> removeTab(args);
            case "view" -> viewTab(args);
            case "rmlist" -> rmlistTab(args);
            case "copy" -> copyTab(args);
            case "replace" -> replaceTab(args);
            case "insert" -> insertTab(args);
            case "autoset" -> autosetTab(args);
            case "autoclean" -> autocleanTab(args);
            default -> null;
        };
    }

    // helpコマンド
    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "列車に紐づくArrivalSignのリストを管理します",
            "利用可能なコマンド: ",
            "create: リストを作成します",
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

    private void commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage("usage: ", LABEL + " " + usage);
    }

    public final static String LABEL = "tal";
    private final TrainArrivalSignStore store;
    private final Advancedautotrain plugin;

    // プラグインが生成する用
    public CommandTrainArrivalSign(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getTrainArrivalSignStore();
    }
}
