package io.github.yuku1a.advancedautotrain;

import io.github.yuku1a.advancedautotrain.utils.TabCompleteUtil;
import io.github.yuku1a.advancedautotrain.utils.commands.CommonMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * cstationlisttemplate、csltコマンドを実装するクラス
 */
public class CommandCStationListTemplate implements CommandExecutor, TabCompleter {

    /**
     * CStationInfoの追加関連のコマンドのヘルプ用テキスト
     */
    private final String csInfoArgText = "<acceleration> <speed> <delay> <name> [announce...]";

    // addコマンド
    private void add(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、パラメータが4つで計6つ
        if (args.length < 6) {
            commandsHelp(sender,"add <template>" + csInfoArgText);
            return;
        }

        // テンプレート指定
        var template = args[1];

        // テンプレートの取得
        var list = store.get(template);
        // テンプレートが登録されていなければ情報の登録もしない
        if (list == null) {
            msgTemplateNotFound(sender);
            return;
        }

        // 引数からCStationInfoの内容だけを切り出す
        var csInfoArgs = Arrays.copyOfRange(args, 2, args.length);

        // CStationInfoを生成する
        var info = parseCSInfo(csInfoArgs);

        // 単に追加するのみ
        list.add(info);

        // 終了メッセージ
        sender.sendMessage("要素の追加を完了しました。");
        // 追加したものの内容を表示する
        infoViewOne(sender, info, list.size() - 1);
    }

    private List<String> addTab(String[] args) {
        // インスペクション対策
        if (args.length < 2)
            return null;

        // add <template> <acceleration> <speed> <delay> <name> [announce...]
        // <template>
        var templateName = args[1];
        if (args.length == 2)
            return searchInStore(templateName);

        // 追加の場合、最後の要素と同じようなものを追加することが多いのでそれに合うようにサジェスト
        var template = store.get(templateName);
        if (template == null)
            return null;

        var lastInfo = template.get(template.size() - 1);

        // <acceleration> <speed> <delay> だけだったら共通化できる
        if (args.length <= 5) {
            return configTabComplete(Arrays.copyOfRange(args, 2, args.length), lastInfo);
        }

        // <name>
        if (args.length == 6) {
            var csList = plugin.getCStationCacheSet().get();
            return TabCompleteUtil.searchInList(args[5], csList);
        }

        // [announce...] はここではやる必要がない
        return null;
    }

    private void insert(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ、インデックスで1つ、パラメータが4つ
        if (args.length < 7) {
            commandsHelp(sender, "insert <template> <index> " + csInfoArgText);
            return;
        }

        // テンプレート指定
        var template = args[1];

        // テンプレートの取得
        var list = store.get(template);
        if (list == null) {
            msgTemplateNotFound(sender);
            return;
        }

        // インデックス取得
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);
        // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
        if (index == -1)
            return;

        // 引数のなかからCStationInfoの情報を抜き出す
        var csInfoParam = Arrays.copyOfRange(args, 3, args.length);

        // CStationInfoを生成
        var info = parseCSInfo(csInfoParam);

        // CStationInfoを挿入
        list.add(index, info);

        // 完了メッセージ
        sender.sendMessage("要素の挿入を完了しました。");
        infoViewOne(sender, info, index);

    }

    private List<String> insertTab(CommandSender sender, String[] args) {
        // インスペクション対策
        if (args.length < 2)
            return null;

        // insert <template> <index> <acceleration> <speed> <delay> <name> [announce...]
        // <template>
        var templateName = args[1];
        if (args.length == 2)
            return searchInStore(templateName);

        // indexはサジェストしない
        if (args.length == 3)
            return null;

        // 挿入の場合、一つ前の要素と同じようなものを追加することが多いのでそれに合うようにサジェスト
        var template = store.get(templateName);
        if (template == null)
            return null;

        var indexStr = args[2];
        var tmpindex = CommandUtil.tryParseIndex(sender, template, indexStr);
        if (tmpindex == -1)
            return null;

        // 指定された位置の一つ前の要素または0番目の要素(-1は無理)
        int index;
        if (tmpindex == 0)
            index = 0;
        else
            index = tmpindex - 1;

        var lastInfo = template.get(index);

        // <acceleration> <speed> <delay> だけだったら共通化できる
        if (args.length <= 6) {
            return configTabComplete(Arrays.copyOfRange(args, 3, args.length), lastInfo);
        }

        // <name>
        if (args.length == 7) {
            var csList = plugin.getCStationCacheSet().get();
            return TabCompleteUtil.searchInList(args[6], csList);
        }

        // [announce...] はここではやる必要がない
        return null;
    }

    private void replace(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ、インデックスで1つ、パラメータが4つ
        if (args.length < 7) {
            commandsHelp(sender, "replace <template> <index> " + csInfoArgText);
            return;
        }

        // テンプレート指定
        var template = args[1];

        // テンプレートの取得
        var list = store.get(template);
        if (list == null) {
            msgTemplateNotFound(sender);
            return;
        }

        // インデックス取得
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);
        // 変換だめだったら-1でかつメッセージがすでに送信されているのでそのままreturn
        if (index == -1)
            return;

        // 引数のなかからCStationInfoの情報を抜き出す
        var csInfoParam = Arrays.copyOfRange(args, 3, args.length);

        // csInfoParamの2番目が時間の情報なのでこれを編集しやすくする
        var delayArg = csInfoParam[2];

        // 元のCStationInfoを持ってくる
        var prevCSInfo = list.get(index);
        var signText = prevCSInfo.getSignText();

        // 正常な場合のみ元の情報をベースに動くようにする
        if (signText != null && signText.length == 3) {
            // 元のdelayをここで持ってくる
            var prevDelay = signText[1];

            // プラスかマイナスを認識した場合、その通りに増減させる
            if (delayArg.startsWith("+") || delayArg.startsWith("-")) {
                // 元データが数値である保証がないのでチェック
                int prevDelayInt;
                try {
                    prevDelayInt = Integer.parseUnsignedInt(prevDelay);
                } catch (NumberFormatException e) {
                    sender.sendMessage("元データが壊れているため、相対指定はできません");
                    return;
                }

                // 当然新しいものも数値である保証はない
                int delayArgInt;
                try {
                    delayArgInt = Integer.parseInt(delayArg);
                } catch (NumberFormatException e){
                    sender.sendMessage("delayが数値ではありません");
                    return;
                }

                // マイナスに行くとまあ壊れるはずなので検証もする
                int newDelayInt = prevDelayInt + delayArgInt;
                if (newDelayInt < 0) {
                    sender.sendMessage("delayが短すぎます。");
                    return;
                }

                // 新しいdelay値を文字列に変換してcsInfoParamを入れ替え
                String newDelay = String.valueOf(newDelayInt);
                csInfoParam[2] = newDelay;
            }
        }

        // CStationInfoを生成
        var info = parseCSInfo(csInfoParam);

        // CStationInfoを挿入
        list.set(index, info);

        // 完了メッセージ
        sender.sendMessage("要素の置換を完了しました。");
        infoViewOne(sender, info, index);
    }

    private List<String> replaceTab(CommandSender sender, String[] args) {
        // インスペクション対策
        if (args.length < 2)
            return null;

        // replace <template> <index> <acceleration> <speed> <delay> <name> [announce...]
        // <template>
        var templateName = args[1];
        if (args.length == 2)
            return searchInStore(templateName);

        // indexはサジェストしない
        if (args.length == 3)
            return null;

        // 置換の場合、同じ番号の要素と同じようなものを追加することが多いのでそれに合うようにサジェスト
        var template = store.get(templateName);
        if (template == null)
            return null;

        var indexStr = args[2];
        var index = CommandUtil.tryParseIndex(sender, template, indexStr);
        if (index == -1)
            return null;

        var lastInfo = template.get(index);

        // <acceleration> <speed> <delay> だけだったら共通化できる
        if (args.length <= 6) {
            return configTabComplete(Arrays.copyOfRange(args, 3, args.length), lastInfo);
        }

        // <name>
        if (args.length == 7) {
            if (args[6].isEmpty()) {
                return List.of(lastInfo.getName());
            }
            var csList = plugin.getCStationCacheSet().get();
            return TabCompleteUtil.searchInList(args[6], csList);
        }

        // [announce...]
        var announce = lastInfo.getAnnounce();
        if (announce == null)
            return null;

        // 別々の引数をスペースで繋いでスペースを表現するので、それに沿った対応
        var announceArray = announce.split(" ");

        // 元データを過ぎてる場合は表示しない
        if (announceArray.length < (args.length - 7))
            return null;

        return List.of(announceArray[args.length - 8]);
    }

    /**
     * CStationの設定に関する部分だけ<br>
     * 指定されたCStationから自動補完をする
     * @param args 引数 [acceleration, speed, delay]の部分のみ
     * @param csInfo CStationInfo
     * @return そのままonTabCompleteに使えるリスト
     */
    private List<String> configTabComplete(String[] args, CStationInfo csInfo) {
        // CStationInfoのチェック
        var signText = csInfo.getSignText();
        if (signText == null || signText.length != 3)
            return null;

        // <acceleration>
        if (args.length == 1) {
            // 情報は2行目の一部
            var line2Text = signText[0];
            if (line2Text == null)
                return null;

            var line2Array = line2Text.split(" ");

            // 最初から2つめのテキストが加速度設定
            if (line2Array.length < 2)
                return null;

            var accelText = line2Array[1];
            if (accelText == null)
                return null;

            return List.of(accelText);
        }

        // <speed>
        if (args.length == 2) {
            // 情報は4行目の一部
            var line4Text = signText[2];
            if (line4Text == null)
                return null;

            var line4Array = line4Text.split(" ");

            // 最初から3つめのテキストが速度設定
            if (line4Array.length < 3)
                return null;

            var speedText = line4Array[2];
            if (speedText == null)
                return null;

            return List.of(speedText);
        }

        // <delay>
        if (args.length == 3) {
            var delayText = signText[1];
            if (delayText == null)
                return null;

            return List.of(delayText);
        }

        return null;
    }

    /**
     * コマンド引数として入力される情報をパースして変換
     * @param args コマンド引数のうちCStationInfoに関連する部分<br>
     *             (acceleration) (speed) (delay) (name) [announce...] の順番
     * @return 完成したCStationInfo
     */
    private CStationInfo parseCSInfo(String[] args) {
        // 取り出し、型変換
        var section = args[0];
        var speed = args[1];
        var delay = args[2];
        var name = args[3];

        // stationのsignを生成する
        var line2 = "station " + section;
        var line4 = "route continue " + speed;
        var lines = new String[]{line2, delay, line4};

        // announceを取り出す
        StringBuilder announce;
        if (args.length > 4) {
            announce = new StringBuilder();
            for (int i = 4 ; i < args.length ; i++) {
                // 2回目以降は空白を追加する
                if (!announce.isEmpty())
                    announce.append(" ");
                announce.append(args[i]);
            }
        }
        else
            announce = null;

        // CStationInfoを生成する
        CStationInfo info;
        if (announce != null)
            info = new CStationInfo(name, lines, announce.toString());
        else
            info = new CStationInfo(name, lines, null);
        return info;
    }

    private void create(CommandSender sender, String[] args) {
        // コマンドで1つ、名前指定で1つ
        if (args.length != 2) {
            commandsHelp(sender, "create <templatename>");
            return;
        }

        // 新規作成するテンプレートの名前
        var name = args[1];

        // あるかどうかを確認する、すでにあったら作らない
        var list = store.get(name);
        if (list != null) {
            sender.sendMessage("その名前のテンプレートは既に作成されています。");
            return;
        }

        // 同じ名前のものはないので新規作成する
        store.put(name, new ArrayList<>());

        // 終了メッセージ
        sender.sendMessage("テンプレート " + name + " を新規作成しました。");
    }
    // createコマンドは自動補完のやりようがないので作らない

    // removeコマンド
    private void remove(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレートで1つ、インデックスで1つ
        if (args.length != 3) {
            CommandUtil.commandsHelp(sender, "cslt remove <template> <index>");
            return;
        }

        // 指定されたテンプレートを取得
        var list = store.get(args[1]);

        // nullだと存在しない
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return;
        }

        // 検査
        var index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // ひっかかってたら弾く
        if (index == -1)
            return;

        // 実際の削除処理
        list.remove(index);
        sender.sendMessage("テンプレート " + args[1] + " の " + index + "番目の項目が削除されました。");

        // おわり
    }

    // viewコマンド
    private void view(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、ページ指定含め計3つ
        if ((2 > args.length) || (args.length > 3)) {
            CommandUtil.commandsHelp(sender, "cslt view <template> <page>");
            return;
        }

        // 名前を出しておく
        var templatename = args[1];

        // とりあえず取りに行く
        var rawlist = store.get(templatename);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (rawlist == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return;
        }

        // 数が少なければそのまま表示
        if (rawlist.size() < 16){
            infoView(sender, templatename, rawlist, -1, 0);
            return;
        }

        String indexstr;
        // 指定がなかったらインデックスを1として扱う
        if (args.length == 2)
            indexstr = "1";
        else
            indexstr = args[2];

        // インデックスをパース
        int index;
        try {
            index = Integer.parseUnsignedInt(indexstr) - 1;
        } catch (Exception e) {
            sender.sendMessage("ページ番号は1以上の数値で指定してください。");
            return;
        }

        // ページングを丸投げ
        var list = CommandUtil.pager(sender, rawlist, index);

        // listがnullだったら警告文とかも出てるのでおわり
        if (list == null)
            return;

        // 分割されたやつを表示
        infoView(sender, templatename, list, index, CommandUtil.calcMaxPageIndex(rawlist));

        // おわり
    }

    // infoコマンド
    private void info(CommandSender sender, String[] args) {
        // コマンド指定で1つ、テンプレート指定で1つ、インデックス指定で計3つ
        if (args.length != 3) {
            CommandUtil.commandsHelp(sender, "cslt info <template> <index>");
            return;
        }

        // 名前を出しておく
        var templatename = args[1];

        // とりあえず取りに行く
        var list = store.get(templatename);

        // 登録されてないときにnullが返ってくる
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (list == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return;
        }

        // インデックスをパース
        int index = CommandUtil.tryParseIndex(sender, list, args[2]);

        // -1だと機能しない値かつちゃんとメッセージが出てるので蹴る
        if (index == -1)
            return;

        // 表示
        infoViewOne(sender, list.get(index), index);

        // おわり
    }

    // info(コマンドではない)を表示する用
    private void infoView(CommandSender sender, String name, List<CStationInfo> list,
                          int pageindex, int maxpageindex) {
        // indexが-1だったらページ指定がないことにする
        if (pageindex == -1)
            sender.sendMessage("----- " + name + " template content -----");
        else {
            sender.sendMessage(
                "----- " + name + " template content page " +
                (pageindex + 1) + " of " + maxpageindex +
                " -----"
            );
        }

        // うまいこと内容を表示する
        sender.sendMessage("(index) (name) (accel) (delay) (speed) (announce)");

        // indexとともに内容を表示
        int index;
        for (int i = 0 ; i < list.size() ; i++){
            if (pageindex != -1)
                index = CommandUtil.calcPagingIndex(i, pageindex);
            else
                index = i;
            infoViewSimple(sender, list.get(i), index, true);
        }
    }

    private void infoViewSimple(CommandSender sender, CStationInfo info, int index, boolean announcecut) {
        if (info.getSignText() == null) {
            sender.sendMessage(index + " | " + "broken data");
        }
        var accel = info.getSignText()[0].replace("station ", "");
        var speed = info.getSignText()[2].replace("route continue ", "");
        var announce = info.getAnnounce();
        if (announce == null)
            announce = "";
        else if (announcecut && announce.length() >= 10)
            announce = announce.substring(0, 9) + "...";
        sender.sendMessage(
            index + " | " +
                info.getName() + " | " +
                accel + " | " +
                // これはdelay
                info.getSignText()[1] + " | " +
                speed + " | " +
                announce
        );
    }

    // infoを一つだけ表示する用
    private void infoViewOne(CommandSender sender, CStationInfo info, int index) {
        // うまいこと内容を表示する
        sender.sendMessage("(index) (name) (accel) (delay) (speed) (announce)");
        infoViewSimple(sender, info, index, false);
    }

    private void msgTemplateNotFound(CommandSender sender) {
        sender.sendMessage("指定された名前のテンプレートは登録されていません。");
    }

    // ボイラープレートじみたコード類
    // listコマンド
    private void list(CommandSender sender, String[] args) {
        // コマンドで1つ、ページで1つまで
        if (args.length > 2) {
            CommandUtil.commandsHelp(sender, "cslt list <page>");
            return;
        }

        // storeからキーのコレクションを取得する
        var rawlist = store.keysList();

        // 数が少なければそのまま表示
        if (rawlist.size() < 16){
            sender.sendMessage("----- template list -----");
            rawlist.forEach(sender::sendMessage);
            return;
        }

        String index;
        // 指定がなかったらインデックスを1として扱う
        if (args.length == 1)
            index = "1";
        else
            index = args[1];

        // ページングを丸投げ
        var list = CommandUtil.pager(sender, rawlist, index);

        // listがnullだったら警告文とかも出てるのでおわり
        if (list == null)
            return;

        // 分割されたやつを表示
        sender.sendMessage(
            "----- template list page " +
                index + " of " + CommandUtil.calcMaxPageIndex(rawlist) +
                " -----"
        );
        list.forEach(sender::sendMessage);

        // おわり
    }

    // removetコマンド
    private void removet(CommandSender sender, String[] args) {
        // コマンドで1つ、テンプレート指定で1つ
        if (args.length != 2) {
            CommandUtil.commandsHelp(sender, "cslt removet <template>");
            return;
        }

        // 指定されたテンプレートを削除
        store.remove(args[1]);
        sender.sendMessage(
            "指定されたテンプレート " + args[1] +
                " は、削除されました。"
        );
    }

    // copyコマンド
    private void copy(CommandSender sender, String[] args) {
        // コマンド指定で1つ、コピー元と先指定で2つ
        if (args.length != 3) {
            CommandUtil.commandsHelp(sender, "cslt copy <from> <to>");
            return;
        }

        // 普通にコピー
        var from = store.get(args[1]);

        // nullチェック
        // チェック用
        // nullが投げ込まれたら相応のメッセージを出すだけ
        if (from == null) {
            sender.sendMessage("指定された名前のテンプレートは登録されていません。");
            return;
        }

        // storeへset
        store.put(args[2], new ArrayList<>(from));

        // おわり
        sender.sendMessage("コピーが完了しました。");
    }

    /**
     * 途中までの入力に合うテンプレートを探すメソッド
     * @param query 途中まで入れられた文字列
     * @return 入力に合うテンプレート名のリスト
     */
    private List<String> searchInStore(String query) {
        var list = store.keysList();
        return TabCompleteUtil.searchInList(query, list);
    }

    // saveコマンド
    private void save(CommandSender sender) {
        if (!sender.hasPermission(plugin.AdminPermission)){
            CommonMessage.dontHavePermission(sender);
            return;
        }

        if (store.save())
            sender.sendMessage("Save Successful!");
        else
            sender.sendMessage("Save Failed");
    }

    // loadコマンド
    private void load(CommandSender sender) {
        if (!sender.hasPermission(plugin.AdminPermission)){
            CommonMessage.dontHavePermission(sender);
            return;
        }

        if (store.load())
            sender.sendMessage("Data Loaded");
        else
            sender.sendMessage("Data Load Failed");
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

        // コマンドごとに分岐やる、この構文めちゃ便利
        switch (args[0]) {
            case "list" -> list(sender, args);
            case "save" -> save(sender);
            case "load" -> load(sender);
            case "view" -> view(sender, args);
            case "add" -> add(sender, args);
            case "replace" -> replace(sender, args);
            case "insert" -> insert(sender, args);
            case "remove" -> remove(sender, args);
            case "removet" -> removet(sender, args);
            case "copy" -> copy(sender, args);
            case "info" -> info(sender, args);
            case "create" -> create(sender, args);
            default -> help(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!sender.hasPermission(plugin.UsePermission))
            return null;

        // コマンドをサジェストする
        if (args.length <= 1) {
            // 管理コマンドはサジェストしなくたっていい
            return List.of("create", "add", "remove", "view", "list",
                           "removet", "copy", "replace", "insert");
        }

        // コマンドごとのサジェストはそれぞれのメソッドへ
        return switch (args[0]) {
            case "add" -> addTab(args);
            case "insert" -> insertTab(sender, args);
            case "replace" -> replaceTab(sender, args);
            default -> null;
        };
    }

    // helpコマンド
    private boolean help(CommandSender sender) {
        sender.sendMessage(
            "CStationに対する動作のテンプレートを管理します",
            "利用可能なコマンド: ",
            "create: テンプレートを新規作成します",
            "add: 項目を追加します",
            "remove: 項目を削除します",
            "view: 指定されたテンプレートの項目を表示します",
            "list: テンプレートの一覧を表示します",
            "removet: 指定したテンプレートを削除します",
            "copy: テンプレートのコピーを行います",
            "replace: テンプレート内の指定された項目を入れ替えます",
            "insert: テンプレート内の指定された位置に項目を追加します"
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
        sender.sendMessage("usage; ",
                           LABEL + " " + usage);
    }

    public static final String LABEL = "cslt";

    private final CStationListTemplateStore store;
    private final Advancedautotrain plugin;

    // プラグインが生成する用
    public CommandCStationListTemplate(Advancedautotrain plugin) {
        this.plugin = plugin;
        store = plugin.getCStationListTemplateStore();
    }
}
