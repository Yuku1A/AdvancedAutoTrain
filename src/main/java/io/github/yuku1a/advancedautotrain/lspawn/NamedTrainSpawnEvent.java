package io.github.yuku1a.advancedautotrain.lspawn;


import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

// スポーンした列車に対してTrainCarts側に保存してない設定などを適用したりするためのフック
// スポーンした列車の検出方法などとは独立した仕様
// これ系のコードの結果としてTrainCarts側で設定する情報の量を減らすことができる
// TrainCarts内で列車に大量の設定を投入するのはかなり面倒くさいのでその軽減策
// 現状の実装ではセーブされた列車にしてセーブする名前とは別に名前を設定する必要がある
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
