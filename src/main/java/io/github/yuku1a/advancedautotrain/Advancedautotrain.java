package io.github.yuku1a.advancedautotrain;

import io.github.yuku1a.advancedautotrain.schedaction.CommandOperationTimer;
import io.github.yuku1a.advancedautotrain.schedaction.OperationTimer;
import io.github.yuku1a.advancedautotrain.schedaction.OperationTimerStore;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.properties.api.IPropertyRegistry;
import com.bergerkiller.bukkit.tc.signactions.SignAction;

import java.util.logging.Level;

public final class Advancedautotrain extends JavaPlugin {

    // 一般的なコマンドを使用するためのパーミッション
    public final String UsePermission = "advancedautotrain.use";

    // 管理用コマンドを使用するためのパーミッション
    public final String AdminPermission = "advancedautotrain.admin";

    // このプラグインのもの
    private SignActionCStation signActionCStation;
    private CStationListProperty cStationListProperty;
    private CStationListTemplateStore templateStore;
    private OperationTimerStore operationTimerStore;

    public OperationTimerStore getOperationTimerStore() {
        return operationTimerStore;
    }
    private IPropertyRegistry propreg;
    private TrainCarts trainCarts;
    public TrainCarts getTrainCarts(){
        return trainCarts;
    }
    @Override
    public void onLoad() {
        // TrainCartsの参照の取得
        trainCarts = getPlugin(TrainCarts.class);
        propreg = trainCarts.getPropertyRegistry();

        // CStationの初期化
        // 他のものが動かないのでデータ系から有効化する
        // yamlから戻す場合は登録しないといけないらしい
        ConfigurationSerialization.registerClass(CStationInfo.class);
        // 登録してから各種ロード
        templateStore = new CStationListTemplateStore(this);
        templateStore.load();
        // CStationListPropertyの初期化
        cStationListProperty = new CStationListProperty();
        propreg.register(cStationListProperty);
        // SignActionを登録する
        signActionCStation = new SignActionCStation(this);
        SignAction.register(signActionCStation);

        // OperationTimerの初期化
        ConfigurationSerialization.registerClass(OperationTimer.class);
        operationTimerStore = new OperationTimerStore(this);
        operationTimerStore.load();

        getLogger().log(Level.INFO, "loaded!");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        // イベントの登録
        getServer().getPluginManager().registerEvents(new ListenerGroupRemove(), this);

        // Cstation絡みのenable
        cStationListProperty.enable(this);
        getCommand("cstationlisttemplate").setExecutor(new CommandCStationListTemplate(this));

        // OPTimer絡みのenable
        operationTimerStore.restore();
        getCommand("operationtimer").setExecutor(new CommandOperationTimer(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // 各種登録解除
        // CStation絡みの処理
        // CStation絡みの登録解除
        SignAction.unregister(signActionCStation);
        propreg.unregister(cStationListProperty);
        // 終了前にデータを保存する
        templateStore.save();

        // OPTimer絡みの処理
        operationTimerStore.freeze();
        operationTimerStore.save();
    }

    /**
     * CStationListPropertyのプラグインによって管理されるインスタンスを取得します
     * @return CStationListPropertyのインスタンス
     */
    public CStationListProperty getcStationListProperty() { return cStationListProperty; }

    /**
     * CStationListTemplateStoreのプラグインによって管理されるインスタンスを取得します
     * @return CStationListTemplateStoreのインスタンス
     */
    public CStationListTemplateStore getCStationListTemplateStore(){
        return templateStore;
    }
}
