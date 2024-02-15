package io.github.yuku1a.advancedautotrain.trainarrivallist;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

import java.util.List;

public class TrainArrivalSignStore extends SaveDataStore<List<ArrivalSignEntry>> {
    public TrainArrivalSignStore(Advancedautotrain plugin) {
        super(plugin, "TrainArrivalSign.yml");
    }
}
