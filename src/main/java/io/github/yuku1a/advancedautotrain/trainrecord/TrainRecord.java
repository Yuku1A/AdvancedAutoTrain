package io.github.yuku1a.advancedautotrain.trainrecord;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * 列車がイベントを発火したことの記録<br>
 * それぞれの項目にnullを記録できる
 */
public class TrainRecord implements ConfigurationSerializable {
    /**
     * イベントが記録された場所
     * @return イベントが記録された場所
     */
    public Location getLocation() { return location; }
    private final Location location;

    /**
     * イベントを発火した看板につけられた名前
     * @return イベントを発火した看板につけられた名前
     */
    public String getSignName() { return signName; }
    private final String signName;

    /**
     * イベントを発火した看板に付与されていた機能の種類
     * @return イベントを発火した看板の機能の種類
     */
    public String getSignType() { return signType; }
    private final String signType;

    /**
     * イベントを発火した看板に対する列車の動作
     * @return イベントを発火した看板に対する列車の動作
     */
    public String getActionType() { return actionType; }
    private final String actionType;

    /**
     * イベントを発火した列車に対して看板が動作したかどうか
     * @return イベントを発火した列車に対して看板が動作したかどうか
     */
    public boolean isActed() { return acted; }
    private final boolean acted;

    /**
     * コンストラクタ
     * @param location イベントが記録された場所
     * @param signName イベントを発火した看板につけられた名前
     * @param signType イベントを発火した看板の機能の種類
     * @param actionType イベントを発火した看板に対する列車の動作
     * @param acted イベントを発火した列車に対して看板が動作したかどうか
     */
    public TrainRecord(
        Location location,
        String signName, String signType,
        String actionType, boolean acted) {
        this.location = location;
        this.signName = signName;
        this.signType = signType;
        this.actionType = actionType;
        this.acted = acted;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("location", location);
        map.put("signName", signName);
        map.put("signType", signType);
        map.put("actionType", actionType);
        map.put("acted", acted);
        return map;
    }

    public static TrainRecord deserialize(Map<String, Object> map) {
        return new TrainRecord(
            (Location) map.get("location"),
            (String) map.get("signName"), (String) map.get("signType"),
            (String) map.get("actionType"), (boolean) map.get("acted")
        );
    }
}
