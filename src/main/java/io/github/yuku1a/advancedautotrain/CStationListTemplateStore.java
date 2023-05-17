package io.github.yuku1a.advancedautotrain;

import java.util.List;

/**
 * CStationInfoのListをテンプレートとしてファイルに保存します。
 */
public class CStationListTemplateStore extends SaveDataStore<List<CStationInfo>> {
    /**
     * プラグインがインスタンスを生成するためのメソッド
     * @param plugin プラグイン本体クラス
     */
    CStationListTemplateStore(Advancedautotrain plugin) {
        super(plugin, "CStationListTemplate.yml");
    }
}
