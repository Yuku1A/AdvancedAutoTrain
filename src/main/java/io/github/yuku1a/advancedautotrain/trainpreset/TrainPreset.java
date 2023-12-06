package io.github.yuku1a.advancedautotrain.trainpreset;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列車に適用される設定を表現します。
 */
public class TrainPreset implements ConfigurationSerializable {
    private final String trainName;
    private final String cstationListName;
    private final String routeName;
    private final List<String> tag;

    /**
     * コンストラクタ。設定されていない要素にはnullを使用してください。
     * @param trainName 列車につける(ついている)名前
     * @param cstationListName CStationListの名前
     * @param routeName DestinationRouteの名前
     * @param tag 列車につけるタグ
     */
    public TrainPreset(String trainName, String cstationListName, String routeName, List<String> tag) {
        this.trainName = trainName;
        this.cstationListName = cstationListName;
        this.routeName = routeName;
        this.tag = tag;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getCstationListName() {
        return cstationListName;
    }

    public String getRouteName() {
        return routeName;
    }

    @SuppressWarnings("unchecked")
    public static TrainPreset deserialize(Map<String, Object> map) {
        return new TrainPreset(
            (String) map.get("trainName"),
            (String) map.get("cstationListName"),
            (String) map.get("routeName"),
            (ArrayList<String>) map.get("tag")
        );
    }

    public List<String> getTag() {
        return tag;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("trainName", trainName);
        map.put("cstationListName", cstationListName);
        map.put("routeName", routeName);
        map.put("tag", tag);
        return map;
    }
}
