package io.github.yuku1a.advancedautotrain;

import java.util.List;

/**
 * CStationInfoのListをテンプレートとしてファイルに保存します。
 */
public class CStationListTemplateStore extends SaveDataStore<List<CStationInfo>> {
    public static final String FILE_NAME = "CStationListTemplate.yml";

    /**
     * プラグインがインスタンスを生成するためのメソッド
     * @param plugin プラグイン本体クラス
     */
    CStationListTemplateStore(Advancedautotrain plugin) {
        super(plugin, FILE_NAME);
    }
}
