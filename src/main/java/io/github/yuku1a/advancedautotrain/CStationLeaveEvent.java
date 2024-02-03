package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 列車がCStationから出たことを示すイベント
 */
public class CStationLeaveEvent extends Event{
    /**
     * イベントが発火されたレールの位置
     * @return イベントが発火されたレールの位置
     */
    public Location getRailLocation(){ return railLocation; }
    private final Location railLocation;

    /**
     * このイベントを発火した列車
     * @return このイベントを発火した列車
     */
    public MinecartGroup getTrain() { return train; }
    private final MinecartGroup train;

    /**
     * このイベントを発火した看板につけられている名前
     * @return このイベントを発火した看板につけられている名前
     */
    public String getCStationName() { return CStationName; }
    private final String CStationName;

    /**
     * このイベントを発火した列車がこの看板で停車するかどうか
     * @return このイベントを発火した列車がこの看板で停車するかどうか
     */
    public boolean isActed() { return acted; }
    private final boolean acted;

    /**
     * コンストラクタ
     * @param railLocation イベントが発火したレールの位置
     * @param train イベントを発火した列車
     * @param CStationName イベントを発火した看板につけられた名前
     * @param acted イベントを発火した看板に列車が停車したかどうか
     *
     */
    public CStationLeaveEvent(Location railLocation, MinecartGroup train, String CStationName, boolean acted) {
        this.railLocation = railLocation;
        this.train = train;
        this.CStationName = CStationName;
        this.acted = acted;
    }

    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}