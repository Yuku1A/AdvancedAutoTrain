package io.github.yuku1a.advancedautotrain.arrivallist;

import io.github.yuku1a.advancedautotrain.schedaction.ScheduledAction;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * 看板に表示される発車予定を表します。
 */
public class ScheduledSign extends ScheduledAction implements ConfigurationSerializable {
    /**
     * @param scheduletime アクションの予定時刻
     * @param trainName 列車の名前
     */
    public ScheduledSign(long scheduletime, String trainName) {
        this(scheduletime, trainName, null);
    }

    /**
     * @param scheduletime アクションの予定時刻
     * @param trainName 列車の名前
     * @param trainDescription 列車の説明
     */
    public ScheduledSign(long scheduletime, String trainName, String trainDescription) {
        this(scheduletime, trainName, trainDescription, null);
    }

    /**
     * @param scheduletime アクションの予定時刻
     * @param trainName 列車の名前
     * @param trainDescription 列車の説明
     */
    public ScheduledSign(long scheduletime, String trainName, String trainDescription, String trainInternalName) {
        super(scheduletime);
        this.trainName = trainName;
        this.trainDescription = trainDescription;
        this.trainInternalName = trainInternalName;
    }

    /**
     * 内部で保持する実際の列車の名前
     * @return 内部で保持する実際の列車の名前、ない場合null
     */
    public String getTrainInternalName() { return trainInternalName; }
    private final String trainInternalName;

    /**
     * 表示される列車の名前
     * @return 表示される列車の名前
     */
    public String getTrainName() { return trainName; }
    private final String trainName;

    /**
     * 列車の詳細な説明
     * @return 列車の詳細な説明、ない場合null
     */
    public String getTrainDescription() { return trainDescription; }
    private final String trainDescription;

    public static ScheduledSign deserialize(Map<String, Object> map) {
        var time = Long.parseLong((String) map.get("time"));
        var name = (String) map.get("name");
        var desc = (String) map.get("desc");
        var internalname = (String) map.get("internalname");
        return new ScheduledSign(time, name, desc, internalname);
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("name", trainName);
        map.put("desc", trainDescription);
        map.put("time", String.valueOf(getScheduletime()));
        map.put("internalname", trainInternalName);
        return map;
    }
}
