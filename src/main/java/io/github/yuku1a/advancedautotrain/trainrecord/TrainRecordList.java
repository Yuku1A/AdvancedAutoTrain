package io.github.yuku1a.advancedautotrain.trainrecord;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TrainRecordEntryのリスト<br>
 * 記録機能付き
 */
public class TrainRecordList implements ConfigurationSerializable {
    private final List<TrainRecordEntry> trainRecords;
    private long startTime = Long.MAX_VALUE;

    /**
     * 記録を開始する
     */
    public void startRecord() {
        startRecord(null);
    }

    /**
     * 記録を開始すると同時にイベントを一つ記録する
     * @param trainRecord 記録するイベント、なければnull
     */
    public void startRecord(TrainRecord trainRecord) {
        this.startTime = System.currentTimeMillis();
        if (trainRecord != null)
            trainRecords.add(new TrainRecordEntry(0, trainRecord));
    }

    /**
     * 記録する
     * @param trainRecord 記録したいTrainRecord
     */
    public void record(TrainRecord trainRecord) {
        long timeNow = System.currentTimeMillis() - startTime;
        trainRecords.add(new TrainRecordEntry(timeNow, trainRecord));
    }

    /**
     * 内部リストのプレビュー(変更不可能)
     * @return 内部リストのプレビュー(変更不可能)
     */
    public List<TrainRecordEntry> preview() {
        return Collections.unmodifiableList(trainRecords);
    }

    /**
     * 特定の記録の時間をずらす<br>
     * リストの途中の項目をずらすと<br>
     * 最後の項目まで連動して同じだけずれる
     * @param index preview()で得られるリストにおけるインデックス
     * @param addendTime ずらす時間(プラスでもマイナスでもいい)
     */
    public void timeModify(int index, long addendTime) {
        // indexから終わりの部分までを作り直して入れ替える
        for (int i = index; i < trainRecords.size(); i++) {
            var oldRecord = trainRecords.get(i);
            trainRecords.set(index, new TrainRecordEntry(
                oldRecord.getRecordedAt() + addendTime,
                oldRecord.getTrainRecord()));
        }
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("list", trainRecords);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static TrainRecordList deserialize(Map<String, Object> map) {
        return new TrainRecordList((List<TrainRecordEntry>) map.get("list"));
    }

    /**
     * 新規生成用コンストラクタ
     */
    public TrainRecordList() {
        this.trainRecords = new ArrayList<>();
    }

    /**
     * 保存してあるのを読み込んだりする用コンストラクタ
     * @param trainRecords すでにあるTrainRecordのリスト
     */
    public TrainRecordList(List<TrainRecordEntry> trainRecords) {
        this.trainRecords = new ArrayList<>(trainRecords);
    }
}
