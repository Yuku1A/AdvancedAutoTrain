package io.github.yuku1a.advancedautotrain.arrivallist;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

/**
 * 看板に表示される発車予定を保存します。
 */
public class ScheduledSignSetStore extends SaveDataStore<ScheduledSignSet> {
    private final Advancedautotrain plugin;

    public ScheduledSignSetStore(Advancedautotrain plugin) {
        super(plugin, "ArrivalSignList.yml");
        this.plugin = plugin;
    }

    public void enable() {
        getStore().forEach((k, v) -> v.enable(plugin));
    }
}
