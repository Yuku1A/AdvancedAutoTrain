package io.github.yuku1a.advancedautotrain.lspawn;

import io.github.yuku1a.advancedautotrain.schedaction.ScheduledAction;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * 予定された列車のスポーンを表現します。
 */
public class ScheduledSpawn extends ScheduledAction implements ConfigurationSerializable {
    private final String savedTrainName;
    private final String spawnTrainName;

    /**
     * コンストラクタ。
     * @param scheduleTime 予定時刻
     * @param savedTrainName TrainCartsでセーブされている列車の名前
     * @param spawnTrainName スポーンしたときの列車の名前(nullの場合はセーブされた列車の名前)
     */
    public ScheduledSpawn(long scheduleTime, String savedTrainName, String spawnTrainName) {
        super(scheduleTime);
        this.savedTrainName = savedTrainName;
        this.spawnTrainName = spawnTrainName == null ? savedTrainName : spawnTrainName;
    }

    /**
     * TrainCartsでセーブされている列車の名前
     * @return TrainCartsでセーブされている列車の名前
     */
    public String getSavedTrainName() {
        return savedTrainName;
    }

    /**
     * スポーンした後の列車の名前
     * @return スポーンした後の列車の名前
     */
    public String getSpawnTrainName() {
        return spawnTrainName;
    }

    public static ScheduledSpawn deserialize(Map<String, Object> map) {
        return new ScheduledSpawn(
            Long.parseLong((String) map.get("scheduleTime")),
            (String) map.get("savedTrainName"),
            (String) map.get("spawnTrainName")
        );
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("scheduleTime", Long.valueOf(getScheduletime()).toString());
        map.put("savedTrainName", savedTrainName);
        map.put("spawnTrainName", spawnTrainName);
        return map;
    }
}
