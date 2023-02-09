package io.github.yuku1a.advancedautotrain;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.bergerkiller.bukkit.tc.signactions.SignAction;

import java.util.logging.Level;

public final class Advancedautotrain extends JavaPlugin {
    private final SignActionCStation signActionCStation = new SignActionCStation();
    private CStationListTemplateStore templateStore;

    @Override
    public void onLoad() {
        getLogger().log(Level.INFO, "loaded!");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        // SignActionを登録する
        SignAction.register(signActionCStation);

        // yamlから戻す場合は登録しないといけないらしい
        ConfigurationSerialization.registerClass(CStationInfo.class);

        // 登録してから各種ロード
        templateStore = new CStationListTemplateStore(this);

        // コマンドを登録
        getCommand("cstationlisttemplate").setExecutor(new CommandCStationListTemplate(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        SignAction.unregister(signActionCStation);

        // 終了前にデータを保存する
        templateStore.save();
    }

    /**
     * CStationListTemplateStoreのプラグインによって管理されるインスタンスを取得します
     * @return CStationListTemplateStoreのインスタンス
     */
    public CStationListTemplateStore getCStationListTemplateStore(){
        return templateStore;
    }
}
