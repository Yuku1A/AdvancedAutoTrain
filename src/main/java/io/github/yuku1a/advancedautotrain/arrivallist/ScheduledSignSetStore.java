package io.github.yuku1a.extremeautotrain;

import io.github.yuku1a.advancedautotrain.SaveDataStore;

/**
 * 看板に表示される発車予定を保存します。
 */
public class ScheduledSignSetStore extends SaveDataStore<ScheduledSignSet> {
    private final ExtremeAutoTrain plugin;

    public ScheduledSignSetStore(ExtremeAutoTrain plugin) {
        super(plugin, "ArrivalSignList.yml");
        this.plugin = plugin;
    }

    public void enable() {
        getStore().forEach((k, v) -> v.enable(plugin));
    }
}
