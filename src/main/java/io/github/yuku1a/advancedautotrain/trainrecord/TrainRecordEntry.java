package io.github.yuku1a.advancedautotrain.trainrecord;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class TrainRecordEntry implements ConfigurationSerializable {
    /**
     * イベントが記録された(相対的な)時間
     * @return イベントが記録された(相対的な)時間
     */
    public long getRecordedAt() { return recordedAt; }
    private final long recordedAt;

    /**
     * このインスタンスに紐づけられたTrainRecord
     * @return このインスタンスに紐づけられたTrainRecord
     */
    public TrainRecord getTrainRecord() { return trainRecord; }
    private final TrainRecord trainRecord;

    /**
     * コンストラクタ
     * @param recordedAt イベントが記録された(相対的な)時間
     * @param trainRecord このインスタンスに紐づけるTrainRecord
     */
    public TrainRecordEntry(long recordedAt, TrainRecord trainRecord) {
        this.recordedAt = recordedAt;
        this.trainRecord = trainRecord;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("recordedAt", Long.valueOf(recordedAt).toString());
        map.put("trainRecord", trainRecord);
        return map;
    }

    public static TrainRecordEntry deserialize(Map<String, Object> map) {
        return new TrainRecordEntry(
            Long.parseLong((String) map.get("recordedAt")),
            (TrainRecord) map.get("trainRecord")
        );
    }
}
