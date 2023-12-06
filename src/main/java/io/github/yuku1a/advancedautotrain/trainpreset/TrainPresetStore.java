package io.github.yuku1a.advancedautotrain.trainpreset;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

public class TrainPresetStore extends SaveDataStore<TrainPreset> {
    public TrainPresetStore(Advancedautotrain plugin) {
        super(plugin, "TrainPreset.yml");
    }
}
