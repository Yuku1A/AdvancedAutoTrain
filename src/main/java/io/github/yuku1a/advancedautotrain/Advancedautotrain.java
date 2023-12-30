package io.github.yuku1a.advancedautotrain;

import io.github.yuku1a.advancedautotrain.arrivallist.CStationLeaveListener;
import io.github.yuku1a.advancedautotrain.arrivallist.CommandArrivalList;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSign;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSet;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSetStore;
import io.github.yuku1a.advancedautotrain.lspawn.CommandLSpawn;
import io.github.yuku1a.advancedautotrain.lspawn.NamedTrainSpawnEventExecutor;
import io.github.yuku1a.advancedautotrain.schedaction.CommandOperationTimer;
import io.github.yuku1a.advancedautotrain.lspawn.ScheduledSpawnSetStore;
import io.github.yuku1a.advancedautotrain.schedaction.OperationTimer;
import io.github.yuku1a.advancedautotrain.schedaction.OperationTimerStore;
import io.github.yuku1a.advancedautotrain.trainpreset.CommandTrainPreset;
import io.github.yuku1a.advancedautotrain.trainpreset.TrainPresetExecutor;
import io.github.yuku1a.advancedautotrain.trainpreset.TrainPresetStore;
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

    // TrainCarts関連
    private IPropertyRegistry propreg;
    private TrainCarts trainCarts;
    public TrainCarts getTrainCarts(){
        return trainCarts;
    }


    // このプラグインのもの
    // CStation関連
    private SignActionCStation signActionCStation;
    private CStationListProperty cStationListProperty;
    public CStationListProperty getcStationListProperty() { return cStationListProperty; }
    private CStationListTemplateStore templateStore;
    public CStationListTemplateStore getCStationListTemplateStore(){
        return templateStore;
    }

    // OPTimer関連
    private OperationTimerStore operationTimerStore;
    public OperationTimerStore getOperationTimerStore() {
        return operationTimerStore;
    }

    // LSpawn関連
    private ScheduledSpawnSetStore spawnListStore;
    public ScheduledSpawnSetStore getSpawnListStore() {
        return spawnListStore;
    }

    // ArrivalSign関連
    private ScheduledSignSetStore signListStore;
    public  ScheduledSignSetStore getSignListStore(){
        return signListStore;
    }

    // TrainPreset関連
    private TrainPresetStore trainPresetStore;
    public TrainPresetStore getTrainPresetStore() { return trainPresetStore; }

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

        // OPTimerの初期化
        ConfigurationSerialization.registerClass(OperationTimer.class);
        operationTimerStore = new OperationTimerStore(this);
        operationTimerStore.load();

        // ArrivalListの初期化
        ConfigurationSerialization.registerClass(ScheduledSign.class);
        ConfigurationSerialization.registerClass(ScheduledSignSet.class);
        signListStore = new ScheduledSignSetStore(this);
        signListStore.load();

        // LSpawn関連の初期化
        spawnListStore = new ScheduledSpawnSetStore(this);
        spawnListStore.load();
//        lSpawnSignManager = new LSpawnSignManager(this);
//        lSpawnSignManager.load();
//        SignAction.register(new SignActionLSpawn(this));

        // TrainPreset関連の初期化
        trainPresetStore = new TrainPresetStore(this);
        trainPresetStore.load();

        getLogger().log(Level.INFO, "loaded!");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Cstation絡みのenable
        cStationListProperty.enable(this);
        getCommand("cstationlisttemplate").setExecutor(new CommandCStationListTemplate(this));
        getServer().getPluginManager().registerEvents(new ListenerGroupRemove(), this);

        // OPTimer絡みのenable
        operationTimerStore.restore();
        getCommand("operationtimer").setExecutor(new CommandOperationTimer(this));

        // ArrivalList絡みのenable
        signListStore.enable();
        getCommand("arrivallist").setExecutor(new CommandArrivalList(this));
        getServer().getPluginManager().registerEvents(new CStationLeaveListener(this), this);

        // LSpawn絡みのenable
        spawnListStore.enable();
        getCommand("lspawn").setExecutor(new CommandLSpawn(this));
        getServer().getPluginManager().registerEvents(new NamedTrainSpawnEventExecutor(this), this);

        // TrainPreset絡みのenable
        getCommand("tpreset").setExecutor(new CommandTrainPreset(this));
        getServer().getPluginManager().registerEvents(new TrainPresetExecutor(this, trainPresetStore), this);

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

        // ArrivalListの処理
        signListStore.save();

        // LSpawn絡みの処理
        spawnListStore.save();
    }
}
