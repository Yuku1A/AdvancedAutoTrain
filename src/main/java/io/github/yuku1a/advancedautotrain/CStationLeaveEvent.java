package io.github.yuku1a.advancedautotrain;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * CStationから列車が離れたことを通知するイベント
 */
public class CStationLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String cStationName;
    private final String trainName;

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
     *
     * @param cStationName CStationの名前
     * @param trainName 列車の名前
     */
    public CStationLeaveEvent(String cStationName, String trainName) {
        this.cStationName = cStationName;
        this.trainName = trainName;
    }

    /**
     * このCStationを離れた列車の名前
     * @return 列車の名前
     */
    public String getTrainName() {
        return trainName;
    }
}
