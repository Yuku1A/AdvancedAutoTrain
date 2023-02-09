package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.properties.TrainProperties;

import java.util.IdentityHashMap;

/**
 * CStationListをTrainPropertiesと紐づけるためのストアです。
 */
public class CStationListStore {
    private final IdentityHashMap<TrainProperties, CStationList> map = new IdentityHashMap<>();
    private static final CStationListStore instance = new CStationListStore();
    private CStationListStore() { }

    /**
     * このクラスの唯一のインスタンスを取得します。
     * @return このクラスの唯一のインスタンス
     */
    public static CStationListStore getInstance() {
        return instance;
    }

    /**
     * TrainPropertiesをキーとしてCStationListを追加します。
     * @param prop キー
     * @param list CStationList
     */
    public void add(TrainProperties prop, CStationList list) {
        map.put(prop, list);
    }

    /**
     * TrainPropertiesに紐づけられたCStationListを取得します。
     * @param prop TrainProperties
     * @return 紐づけられたCStationList、ない場合はnull
     */
    public CStationList get(TrainProperties prop) {
        return map.get(prop);
    }

    /**
     * TrainPropertiesに紐づけられたCStationListを削除します。
     * @param prop TrainProperties
     */
    public void remove(TrainProperties prop) {
        map.remove(prop);
    }

}
