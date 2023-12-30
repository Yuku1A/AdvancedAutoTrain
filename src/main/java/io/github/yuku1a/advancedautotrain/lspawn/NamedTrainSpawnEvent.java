package io.github.yuku1a.advancedautotrain.lspawn;


import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NamedTrainSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    private final String trainName;

    /**
     * スポーンした列車に設定された名前
     * @return スポーンした列車に設定された名前
     */
    public String getTrainName() { return trainName; }
    private final MinecartGroup train;

    /**
     * スポーンした列車
     * @return スポーンした列車
     */
    public MinecartGroup getTrain() { return train; }

    /**
     * コンストラクタ
     * @param trainName スポーンした列車に設定された名前
     * @param train スポーンした列車
     */
    public NamedTrainSpawnEvent(String trainName, MinecartGroup train) {
        this.trainName = trainName;
        this.train = train;
    }
}
