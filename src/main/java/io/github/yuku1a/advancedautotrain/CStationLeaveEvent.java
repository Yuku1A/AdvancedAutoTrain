package io.github.yuku1a.advancedautotrain;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * CStationから列車が離れたことを通知するイベント
 */
public class CStationLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String cStationName;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * CStationの名前
     * @return CStationの名前
     */
    public String getCStationName() {
        return cStationName;
    }

    /**
     * コンストラクタ
     * @param cStationName CStationの名前
     */
    public CStationLeaveEvent(String cStationName) {
        this.cStationName = cStationName;
    }
}
