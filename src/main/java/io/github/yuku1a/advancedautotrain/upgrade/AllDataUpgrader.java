package io.github.yuku1a.advancedautotrain.upgrade;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CStationListTemplateStore;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class AllDataUpgrader {
    public static final String DATA_VERSION_YAML_KEY = "version";
    /// すべてのデータをアップグレードします <br>
    /// プレイヤーによる手動介入が必要な場合は、都度ログにその旨が記録されます
    /// @param plugin プラグイン本体
    public static void upgrade(Advancedautotrain plugin) {
        final var pluginDataVersion = plugin.DataVersion;
        // データバージョンとアップグレード
        plugin.getLogger().log(Level.INFO, "このプラグインのデータバージョンは " + pluginDataVersion + " です");
        while (true) {
            final var versionFile = new File(plugin.getDataFolder(), "Version.yml");
            final var versionYaml = new YamlConfiguration();
            final var isVersionFileExists = versionFile.exists();
            // このプラグインが最初に保存し始めたデータがCStationListTemplateなのでこれの有無でデータの有無を判断
            final var isDataExists = new File(plugin.getDataFolder(), CStationListTemplateStore.FILE_NAME).exists();
            int currentDataVersion;
            if (!isDataExists) {
                // データが存在しない(=新規作成)
                currentDataVersion = pluginDataVersion;
            } else if (!isVersionFileExists) {
                // データは存在する
                // バージョン2より前はVersion.ymlそのものがない
                currentDataVersion = 1;
            } else {
                // データが存在し、Version.ymlも存在する
                // バージョン2以降はVersion.ymlがあるのでまず読み込む
                try {
                    versionYaml.load(versionFile);
                } catch (IOException | InvalidConfigurationException e) {
                    // 募: onLoad()の最中に安全にプラグインのロードを拒否する方法
                    throw new RuntimeException(e);
                }
                currentDataVersion = versionYaml.getInt(DATA_VERSION_YAML_KEY);
            }
            if (!isDataExists) {
                plugin.getLogger().log(Level.INFO, "新規環境での実行です");
                return;
            }
            if (currentDataVersion == pluginDataVersion) {
                plugin.getLogger().log(
                    Level.INFO, "現在のデータバージョンは " + currentDataVersion + " です。" +
                    "アップグレードの必要はありません。"
                );
                return;
            }
            switch (currentDataVersion) {
                case 1 -> {
                    DataUpgraderV2.upgradeV2(plugin);
                    versionYaml.set(DATA_VERSION_YAML_KEY, 2);
                }
            }
            try {
                versionYaml.save(versionFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
