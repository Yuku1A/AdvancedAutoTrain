package io.github.yuku1a.advancedautotrain.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * コマンドの自動補完を実装するための関数群
 */
public class TabCompleteUtil {

    /**
     * 途中まで入力された文字列にあう候補を探すメソッド
     * @param query 途中まで入力された文字列
     * @param list 候補全体のリスト
     * @return 文字列に合う候補
     */
    public static List<String> searchInList(String query, List<String> list) {
        return Util.searchInList(query, list);
    }
}
