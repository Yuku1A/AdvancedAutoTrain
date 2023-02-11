package io.github.yuku1a.advancedautotrain;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * コマンド実装に必要な長いメソッドをここに集約
 */
public class CommandUtil {
    /**
     * 15個ごとにリストを切り出します。<br>
     * 主にMinecraftのコマンドに使用することを想定しています。
     * @param sender CommandSender
     * @param list 切り出し元のリスト
     * @param indexstr ページ番号(1から始まる)(入力されたものを直接使用することを想定しています)
     * @return 切り出されたリスト
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
     * 主にMinecraftのコマンドに使用することを想定しています。
     * @param sender CommandSender
     * @param list 切り出し元のリスト
     * @param index ページ番号(0から始まる)
     * @return 切り出されたリスト
     * @param <T> リストに格納される要素の型
     */
    public static <T> List<T> pager(CommandSender sender, List<T> list, int index) {
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
            to = list.size() - 1;

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
        else
            return (list.size() / 15);
    }
}
