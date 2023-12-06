package io.github.yuku1a.advancedautotrain.trainpreset;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.GroupCreateEvent;
import com.bergerkiller.bukkit.tc.properties.TrainPropertiesStore;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CStationList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TrainPresetExecutor implements Listener {
    private final Advancedautotrain plugin;
    private final TrainPresetStore store;

    public TrainPresetExecutor(Advancedautotrain plugin, TrainPresetStore store){
        this.plugin = plugin;
        this.store = store;
    }

    @EventHandler
    public void onGroupCreate(GroupCreateEvent event) {
        var train = event.getGroup();
        var prop = train.getProperties();
        var name = prop.getTrainName();

        // セーブされた列車についてる名前(どういう名前でセーブされたかではない)に
        // 紐づいたpresetを探して適用を試みる
        apply(train, name);
    }

    public void apply(MinecartGroup train, String trainName) {
        var prop = train.getProperties();

        var preset = store.get(trainName);

        // 呼び出された名前に対してpresetがなければここで引く
        if (preset == null)
            return;

        // ここで番号をつける
        prop.setTrainName(TrainPropertiesStore.generateTrainName(trainName + "#"));

        // CStationList
        var csl = plugin.getCStationListTemplateStore().get(preset.getCstationListName());
        if (csl != null)
            prop.set(plugin.getcStationListProperty(), new CStationList(csl));

        // DestinationRoute
        var route = plugin.getTrainCarts().getRouteManager().findRoute(preset.getRouteName());
        if (!route.isEmpty()) {
            prop.setDestinationRoute(route);
            prop.setDestination(route.get(0));
        }

        // Tag
        var tag = preset.getTag();
        if (tag != null) {
            prop.setTags(tag.toArray(new String[0]));
        }
    }
}
