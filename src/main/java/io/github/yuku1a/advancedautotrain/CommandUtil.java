package io.github.yuku1a.advancedautotrain;

import io.github.yuku1a.advancedautotrain.utils.PagedListEntry;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * コマンド実装に必要な長いメソッドをここに集約
 */
public class CommandUtil {

    /**
     * 指定された個数ごとに元のインデックスを保持した二次元リストに切り出します
     * @param list 元のリスト
     * @param itemInPage ページごとのアイテムの数
     * @return 切り出されたリストの入った二次元リスト
     * @param <T> 実際のデータの型
     */
    public static <T> List<List<PagedListEntry<T>>> pager2D(List<T> list, int itemInPage) {
        // 先に全部で何ページになるかを計算
        int totalPages = (list.size() / itemInPage) + 1;

        // そのページ数をもとにリストを生成
        var returnlist = new ArrayList<List<PagedListEntry<T>>>(totalPages);

        // ページごとに中のリストを作る
        for (int i = 0; i < totalPages; i++) {
            returnlist.add(new ArrayList<>(itemInPage));
            // ページ内の最大インデックス(元のリスト基準)かリスト自体の最大値か
            int maxIndex = Math.min((i + 1) * itemInPage, list.size());
            // 中のリストを作る
            for (int k = i * itemInPage; k < maxIndex; k++) {
                returnlist.get(i).add(new PagedListEntry<>(k, list.get(k)));
            }
        }

        // たぶんこれでだいじょうぶ
        return returnlist;
    }

    /**
     * 15個ごとにリストを切り出します。<br>
     * 主にMinecraftのコマンドに使用することを想定しています。<br>
     * 失敗した場合はその胸のメッセージを直接プレイヤーに送信します。
     * @param sender CommandSender
     * @param list 切り出し元のリスト
     * @param indexstr ページ番号(1から始まる)(入力されたものを直接使用することを想定しています)
     * @return 切り出されたリスト、nullの場合失敗
     * @param <T> リストに格納される要素の型
     */
    public static <T> List<T> pager(CommandSender sender, List<T> list, String indexstr) {
        int index;
        // ページのインデックスが正しいか検証する
        try {
            index = Integer.parseUnsignedInt(indexstr) - 1;
        } catch (Exception ignored) {
            sender.sendMessage("ページは1以上の数値で指定してください。");
            return null;
        }

        return pager(sender, list, index);
    }

    /**
     * 15個ごとにリストを切り出します。<br>
     * 主にMinecraftのコマンドに使用することを想定しています。<br>
     * 失敗した場合はその胸のメッセージを直接プレイヤーに送信します。
     * @param sender CommandSender
     * @param list 切り出し元のリスト
     * @param index ページ番号(0から始まる)
     * @return 切り出されたリスト、nullの場合失敗
     * @param <T> リストに格納される要素の型
     */
    public static <T> List<T> pager(CommandSender sender, List<T> list, int index) {
        // listがemptyか確認
        if (list.isEmpty())
            return list;

        // ページ番号が0以下じゃないか確認
        if (index < 0){
            sender.sendMessage("ページは1以上の数値で指定してください。");
            return null;
        }

        // 計算してはみ出ないかチェック
        var from = 15 * index;
        if (from >= list.size()) {
            sender.sendMessage("指定されたページは存在しません。");
            return null;
        }

        // 終端が計算してはみ出たらうまいこと収める
        var to = 15 + index * 15;
        if (to >= list.size())
            to = list.size();

        // Listをうまいこと切り出し
        return list.subList(from, to);
    }

    /**
     * ページング時の全体のインデックス番号を計算します。
     * @param index ページング中のリスト内のインデックス番号
     * @param pageindex ページ番号(0から始まる)
     * @return 計算されたインデックス番号
     */
    public static int calcPagingIndex(int index, int pageindex) {
        return index + 15 * pageindex;
    }

    /**
     * 最大のページ番号を計算します。
     * @param list 元リスト
     * @return 最大のページ番号(1から始まる)
     */
    public static int calcMaxPageIndex(List<?> list) {
        // 15個ずつ表示するので、それ+最後の余り部分の数を合わせる
        if ((list.size() % 15) > 0)
            return (list.size() / 15) + 1;
        else {
            if (list.size() < 16)
                return 1;
            else
                return (list.size() / 15);
        }

    }

    /**
     * 正常であればindexそのまま、不正だったら-1が返ってくる <br>
     * 不正だった場合はメッセージがプレイヤーに直接送信されます。
     * @param sender CommandSender
     * @param list リスト
     * @param strindex stringで表現されるインデックス
     * @return パースされたindex、パースできなかった場合-1
     */
    public static int tryParseIndex(CommandSender sender, List<?> list, String strindex) {
        // インデックスがintに変換できることを確認
        int index;
        try {
            index = Integer.parseUnsignedInt(strindex);
        } catch (NumberFormatException ignored) {
            sender.sendMessage("インデックスは数値である必要があります。");
            return -1;
        }

        // インデックスが範囲外であれば処理をしない
        if (index >= list.size()) {
            sender.sendMessage("インデックスが範囲外です。");
            return -1;
        }

        // 変換できた結果をreturn
        return index;
    }

    /**
     * 正常であればindexそのまま、不正だったら-1が返ってくる <br>
     * 不正だった場合はメッセージがプレイヤーに直接送信されます。
     * @param sender CommandSender
     * @param list リスト
     * @param strindex stringで表現されるインデックス
     * @return パースされたindex、パースできなかった場合-1
     */
    public static int tryParsePagingIndex(CommandSender sender, List<?> list, String strindex) {
        // インデックスがintに変換できることを確認
        int index;
        try {
            index = Integer.parseUnsignedInt(strindex);
        } catch (NumberFormatException ignored) {
            sender.sendMessage("インデックスは数値である必要があります。");
            return -1;
        }

        // インデックスが範囲外であれば処理をしない
        if (index > list.size()) {
            sender.sendMessage("インデックスが範囲外です。");
            return -1;
        }

        // 変換できた結果をreturn、インデックスとして動かなきゃいけないので-1
        return index - 1;
    }

    /**
     * 各コマンド用のヘルプをちょっと楽に実装する
     * @param sender CommandSender
     * @param usage "usage:"の次の行に表示する使用法
     * @return onCommandのreturnに使うためのtrue
     */
    public static boolean commandsHelp(CommandSender sender, String usage) {
        sender.sendMessage(
            "usage:",
            usage);
        return true;
    }
}
