package io.github.yuku1a.advancedautotrain.lspawn;

import com.bergerkiller.bukkit.tc.events.GroupCreateEvent;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NamedTrainSpawnEventExecutor implements Listener {

    private final Advancedautotrain plugin;
    @EventHandler
    public void onGroupCreate(GroupCreateEvent event) {
        var train = event.getGroup();
        var prop = train.getProperties();
        var name = prop.getTrainName();

        // セーブされた列車についてる名前(どういう名前でセーブされたかではない)に
        // 紐づいたイベントを動かす
        plugin.getServer().getPluginManager().callEvent(new NamedTrainSpawnEvent(name, train));
    }

    public NamedTrainSpawnEventExecutor(Advancedautotrain plugin) {
        this.plugin = plugin;
    }
}
