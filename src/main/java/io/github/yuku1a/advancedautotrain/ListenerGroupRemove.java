package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.events.GroupRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Level;

/**
 * Train(Group)削除の通知を受け取るためのクラスです。
 */
public class ListenerGroupRemove implements Listener {
    private final CStationListStore store = CStationListStore.getInstance();

    @EventHandler
    public void onGroupRemove(GroupRemoveEvent e) {
        // 通知が来たらCStationListStoreから当該Trainのデータを消す
        store.remove(e.getGroup().getProperties());
    }
}
