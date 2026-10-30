package io.github.yuku1a.advancedautotrain.upgrade;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.lspawn.ScheduledSpawnSetStore;
import io.github.yuku1a.advancedautotrain.trainpreset.TrainPresetStore;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DataUpgraderV2 {
    /// v2へのアップグレード <br>
    /// 具体的には、ScheduledSpawnに設定されているsavedTrainNameをTrainPresetに移行する <br>
    /// このときspawnTrainNameに対して複数のsavedTrainNameが設定されていた場合、 <br>
    /// プレイヤーに手動介入を求める <br>
    /// @param plugin このプラグインのインスタンス
    public static void upgradeV2(Advancedautotrain plugin) {
        plugin.getLogger().log(Level.INFO, "現在のデータバージョンは 1 です。");
        plugin.getLogger().log(Level.INFO, "データバージョン 1 から 2 へのアップグレードを実行しています");
        // snakeyaml直叩き実装に書き換える
        final var dumpOptions = new DumperOptions();
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        final var spawnListFile = new File(plugin.getDataFolder(), ScheduledSpawnSetStore.FILE_NAME);
        final var trainPresetFile = new File(plugin.getDataFolder(), TrainPresetStore.FILE_NAME);
        final var yamlLoader = new Yaml(
            new SafeConstructor(new LoaderOptions()),
            new Representer(dumpOptions),
            dumpOptions
        );
        // リリース版を使っているならLSpawnとTrainPresetは同時に追加されているので、
        // ここでは片方のみ存在する場合については考慮しない
        if (!spawnListFile.exists() && !trainPresetFile.exists()) {
            plugin.getLogger().log(Level.INFO, "対象のデータが存在しないため、アップグレードを終了します。");
            return;
        }

        Map<String, Map<String, Object>> spawnListMap;
        Map<String, Map<String, Object>> trainPresetMap;

        try (
            final var spawnListFileStream = new FileInputStream(spawnListFile);
            final var trainPresetFileStream = new FileInputStream(trainPresetFile)
        ) {
            spawnListMap = yamlLoader.load(spawnListFileStream);
            trainPresetMap = yamlLoader.load(trainPresetFileStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var savedTrainNameMap = new HashMap<String, String>();
        // spawnListからsavedTrainNameの設定を取り出してついでに消す
        for (var spawnListKey : spawnListMap.keySet()) {
            plugin.getLogger().log(Level.INFO, "Converting LSpawnList: " + spawnListKey);
            var spawnList = spawnListMap.get(spawnListKey);
            for (var schedSpawnRaw : UpgradeUtils.<List<Map<String, String>>>cast(spawnList.get("list"))) {
                var spawnTrainName = schedSpawnRaw.get("spawnTrainName");
                var savedTrainName = schedSpawnRaw.get("savedTrainName");
                var previousSavedTrainName = savedTrainNameMap.put(spawnTrainName, savedTrainName);
                schedSpawnRaw.remove("savedTrainName");

                if (previousSavedTrainName != null && !savedTrainName.equals(previousSavedTrainName)) {
                    // 以前と違うspawnTrainNameとsavedTrainNameの組み合わせ
                    plugin.getLogger().log(Level.INFO,
                        spawnTrainName + " が使用するsavedTrainを一つに特定できませんでした。" +
                        "手動での再設定が必要です。"
                    );
                }
            }
        }

        for (var trainPresetKey : trainPresetMap.keySet()) {
            plugin.getLogger().log(Level.INFO, "Converting TrainPreset: " + trainPresetKey);
            var trainPreset = trainPresetMap.get(trainPresetKey);
            var savedTrainName = savedTrainNameMap.get(trainPresetKey);
            trainPreset.put("savedTrainName", savedTrainName);
        }

        try (
            final var spawnListFileStream = new FileWriter(spawnListFile, StandardCharsets.UTF_8);
            final var trainPresetFileStream = new FileWriter(trainPresetFile, StandardCharsets.UTF_8)
        ) {
            yamlLoader.dump(spawnListMap, spawnListFileStream);
            yamlLoader.dump(trainPresetMap, trainPresetFileStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        plugin.getLogger().log(Level.INFO, "データバージョン 2 へのアップグレードを完了しました。");
    }
}
