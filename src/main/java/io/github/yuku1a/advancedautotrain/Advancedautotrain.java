package io.github.yuku1a.advancedautotrain;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.properties.api.IPropertyRegistry;
import com.bergerkiller.bukkit.tc.signactions.SignAction;

import java.util.logging.Level;

public final class Advancedautotrain extends JavaPlugin {
    private SignActionCStation signActionCStation;
    private CStationListProperty cStationListProperty;
    private CStationListTemplateStore templateStore;
    private IPropertyRegistry propreg;

    @Override
    public void onLoad() {
        var traincarts = getPlugin(TrainCarts.class);
        propreg = traincarts.getPropertyRegistry();
        cStationListProperty = new CStationListProperty();
        propreg.register(cStationListProperty);
        getLogger().log(Level.INFO, "loaded!");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        // 他のものが動かないのでデータ系から有効化する
        // yamlから戻す場合は登録しないといけないらしい
        ConfigurationSerialization.registerClass(CStationInfo.class);

        // 登録してから各種ロード
        templateStore = new CStationListTemplateStore(this);

        // SignActionを登録する
        signActionCStation = new SignActionCStation(this);
        SignAction.register(signActionCStation);

        // CStationListPropertyをenableにする
        cStationListProperty.enable(this);

        // コマンドを登録
        getCommand("cstationlisttemplate").setExecutor(new CommandCStationListTemplate(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // 各種登録解除
        SignAction.unregister(signActionCStation);
        propreg.unregister(cStationListProperty);

        // 終了前にデータを保存する
        templateStore.save();
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
