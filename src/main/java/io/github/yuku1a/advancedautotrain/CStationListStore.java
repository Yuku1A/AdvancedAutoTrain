package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;

import java.util.IdentityHashMap;

/**
 * CStationListをTrainPropertiesと紐づけるためのストアです。
 */
public class CStationListStore {
    private final IdentityHashMap<MinecartGroup, CStationList> map = new IdentityHashMap<>();
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
     * MinecartGroupをキーとしてCStationListを格納します。
     * @param train キー
     * @param list CStationList
     */
    public void put(MinecartGroup train, CStationList list) {
        map.put(train, list);
    }

    /**
     * MinecartGroupに紐づけられたCStationListを取得します。
     * @param prop MinecartGroup
     * @return 紐づけられたCStationList、ない場合はnull
     */
    public CStationList get(MinecartGroup prop) {
        return map.get(prop);
    }

    /**
     * MinecartGroupに紐づけられたCStationListを削除します。
     * @param prop MinecartGroup
     */
    public void remove(MinecartGroup prop) {
        map.remove(prop);
    }

}
