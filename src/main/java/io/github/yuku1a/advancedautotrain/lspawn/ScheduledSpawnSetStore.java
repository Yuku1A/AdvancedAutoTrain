package io.github.yuku1a.advancedautotrain.lspawn;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

public class ScheduledSpawnSetStore extends SaveDataStore<ScheduledSpawnSet> {
    private final Advancedautotrain plugin;
    public ScheduledSpawnSetStore(Advancedautotrain plugin) {
        super(plugin, "SpawnList.yml");
        this.plugin = plugin;
    }

    /**
     * OperationTimerの読み込み後いつでも実行できます
     */
    public void enable() {
        getStore().forEach((k, v) -> v.enable(plugin));
    }
}
