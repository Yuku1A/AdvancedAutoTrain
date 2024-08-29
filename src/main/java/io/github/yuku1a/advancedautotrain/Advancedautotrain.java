package io.github.yuku1a.advancedautotrain;

import io.github.yuku1a.advancedautotrain.arrivallist.CStationLeaveListener;
import io.github.yuku1a.advancedautotrain.arrivallist.CommandArrivalList;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSign;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSet;
import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSetStore;
import io.github.yuku1a.advancedautotrain.cstation.CStationCacheSet;
import io.github.yuku1a.advancedautotrain.cstation.CommandCStationCache;
import io.github.yuku1a.advancedautotrain.dump.CommandAATDump;
import io.github.yuku1a.advancedautotrain.lspawn.CommandLSpawn;
import io.github.yuku1a.advancedautotrain.lspawn.LSpawnSignManager;
import io.github.yuku1a.advancedautotrain.lspawn.NamedTrainSpawnEventExecutor;
import io.github.yuku1a.advancedautotrain.lspawn.ScheduledSpawn;
import io.github.yuku1a.advancedautotrain.lspawn.ScheduledSpawnSet;
import io.github.yuku1a.advancedautotrain.lspawn.SignActionLSpawn;
import io.github.yuku1a.advancedautotrain.routeedit.CommandRouteEdit;
import io.github.yuku1a.advancedautotrain.schedaction.CommandOperationTimer;
import io.github.yuku1a.advancedautotrain.lspawn.ScheduledSpawnSetStore;
import io.github.yuku1a.advancedautotrain.schedaction.OperationTimer;
import io.github.yuku1a.advancedautotrain.schedaction.OperationTimerStore;
import io.github.yuku1a.advancedautotrain.trainarrivallist.ArrivalSignEntry;
import io.github.yuku1a.advancedautotrain.trainarrivallist.CommandTrainArrivalSign;
import io.github.yuku1a.advancedautotrain.trainarrivallist.TrainArrivalSignStore;
import io.github.yuku1a.advancedautotrain.trainpreset.CommandTrainPreset;
import io.github.yuku1a.advancedautotrain.trainpreset.TrainPreset;
import io.github.yuku1a.advancedautotrain.trainpreset.TrainPresetExecutor;
import io.github.yuku1a.advancedautotrain.trainpreset.TrainPresetStore;
import io.github.yuku1a.advancedautotrain.trainrecord.CommandTrainRecord;
import io.github.yuku1a.advancedautotrain.trainrecord.TrainRecord;
import io.github.yuku1a.advancedautotrain.trainrecord.TrainRecordEntry;
import io.github.yuku1a.advancedautotrain.trainrecord.TrainRecordExecutor;
import io.github.yuku1a.advancedautotrain.trainrecord.TrainRecordList;
import io.github.yuku1a.advancedautotrain.trainrecord.TrainRecordStore;
import io.github.yuku1a.advancedautotrain.trainrecord.TrainRecordingManager;
import org.bukkit.command.CommandExecutor;
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
    private CStationCacheSet cStationCacheSet;
    public CStationCacheSet getCStationCacheSet() { return cStationCacheSet; }

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
    private LSpawnSignManager lSpawnSignManager;
    public LSpawnSignManager getLSpawnSignManager() { return lSpawnSignManager; }

    // ArrivalSign関連
    private ScheduledSignSetStore signListStore;
    public  ScheduledSignSetStore getSignListStore(){
        return signListStore;
    }

    // TrainPreset関連
    private TrainPresetStore trainPresetStore;
    public TrainPresetStore getTrainPresetStore() { return trainPresetStore; }

    // TrainRecord関連
    private TrainRecordingManager trainRecordingManager;
    public TrainRecordingManager getTrainRecordingManager() { return trainRecordingManager; }
    private TrainRecordStore trainRecordStore;
    public TrainRecordStore getTrainRecordStore() { return trainRecordStore; }

    // TrainArrivalSign関連
    private TrainArrivalSignStore trainArrivalSignStore;
    public TrainArrivalSignStore getTrainArrivalSignStore() { return trainArrivalSignStore; }

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

        cStationCacheSet = new CStationCacheSet(this);
        cStationCacheSet.load();

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
        ConfigurationSerialization.registerClass(ScheduledSpawn.class);
        ConfigurationSerialization.registerClass(ScheduledSpawnSet.class);
        spawnListStore = new ScheduledSpawnSetStore(this);
        spawnListStore.load();
        lSpawnSignManager = new LSpawnSignManager(this);
        lSpawnSignManager.load();
        SignAction.register(new SignActionLSpawn(this));

        // TrainPreset関連の初期化
        ConfigurationSerialization.registerClass(TrainPreset.class);
        trainPresetStore = new TrainPresetStore(this);
        trainPresetStore.load();

        // TrainRecord関連の初期化
        ConfigurationSerialization.registerClass(TrainRecord.class);
        ConfigurationSerialization.registerClass(TrainRecordEntry.class);
        ConfigurationSerialization.registerClass(TrainRecordList.class);
        trainRecordStore = new TrainRecordStore(this);
        trainRecordingManager = new TrainRecordingManager();

        // TrainArrivalList関連の初期化
        ConfigurationSerialization.registerClass(ArrivalSignEntry.class);
        trainArrivalSignStore = new TrainArrivalSignStore(this);
        trainArrivalSignStore.load();

        getLogger().log(Level.INFO, "loaded!");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Cstation絡みのenable
        cStationListProperty.enable(this);
        commandRegister(CommandCStationListTemplate.LABEL, new CommandCStationListTemplate(this));
        getServer().getPluginManager().registerEvents(new ListenerGroupRemove(), this);
        commandRegister(CommandCStationCache.LABEL, new CommandCStationCache(this));

        // OPTimer絡みのenable
        operationTimerStore.restore();
        commandRegister(CommandOperationTimer.LABEL, new CommandOperationTimer(this));

        // ArrivalList絡みのenable
        signListStore.enable();
        commandRegister(CommandArrivalList.LABEL, new CommandArrivalList(this));
        getServer().getPluginManager().registerEvents(new CStationLeaveListener(this), this);

        // LSpawn絡みのenable
        spawnListStore.enable();
        commandRegister(CommandLSpawn.LABEL, new CommandLSpawn(this));
        getServer().getPluginManager().registerEvents(new NamedTrainSpawnEventExecutor(this), this);
        lSpawnSignManager.enable();

        // TrainPreset絡みのenable
        commandRegister(CommandTrainPreset.LABEL, new CommandTrainPreset(this));
        getServer().getPluginManager().registerEvents(new TrainPresetExecutor(this, trainPresetStore), this);

        // TrainRecord絡みのenable
        trainRecordStore.load();
        commandRegister("trec", new CommandTrainRecord(this));
        getServer().getPluginManager().registerEvents(new TrainRecordExecutor(this), this);

        // TrainArrivalList絡みのenable
        commandRegister(CommandTrainArrivalSign.LABEL, new CommandTrainArrivalSign(this));

        // AATDump絡みのenable
        commandRegister("aatdump", new CommandAATDump(this));

        // RouteEdit絡みのenable
        commandRegister(CommandRouteEdit.LABEL, new CommandRouteEdit(this));
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
        cStationCacheSet.save();

        // OPTimer絡みの処理
        operationTimerStore.freeze();
        operationTimerStore.save();

        // ArrivalListの処理
        signListStore.save();

        // LSpawn絡みの処理
        spawnListStore.save();
        lSpawnSignManager.disable();

        // TrainPreset絡みの処理
        trainPresetStore.save();

        // TrainRecord絡みの処理
        trainRecordStore.save();

        // TrainArrivalList絡みの処理
        trainArrivalSignStore.save();
    }

    private void commandRegister(String label, CommandExecutor commandInstance) {
        var command = getCommand(label);

        if (command != null) {
            command.setExecutor(commandInstance);
        }
    }
}
