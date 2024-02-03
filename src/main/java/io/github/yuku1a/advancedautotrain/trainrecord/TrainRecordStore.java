package io.github.yuku1a.advancedautotrain.trainrecord;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

public class TrainRecordStore extends SaveDataStore<TrainRecordList> {
    public TrainRecordStore(Advancedautotrain plugin) {
        super(plugin, "TrainRecords.yml");
    }
}
