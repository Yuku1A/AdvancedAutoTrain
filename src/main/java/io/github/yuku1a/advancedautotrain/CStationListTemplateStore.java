package io.github.yuku1a.advancedautotrain;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * CStationInfoのListをテンプレートとしてファイルに保存します。
 */
public class CStationListTemplateStore {
    private final Map<String, List<CStationInfo>> store = new HashMap<>();
    private final File file;
    private final Advancedautotrain plugin;

    /**
     * すべてのキーのセットを取得します。
     * @return すべてのキー
     */
    public Set<String> getKeySet() {
        return store.keySet();
    }

    /**
     * キーに対してリストを追加します。
     * @param key キー
     * @param list リスト
     */
    public void set(String key, List<CStationInfo> list) {
        store.put(key, list);
    }

    /**
     * キーに紐づけされたリストを取得します。
     * @param key キー
     * @return リスト
     */
    public List<CStationInfo> get(String key) {
        return store.get(key);
    }

    /**
     * ファイルからの読み込みを行います
     * @return 成功可否
     */
    @SuppressWarnings("unchecked")
    public boolean load() {
        // 繰り返すと無限に増えるので削除
        store.clear();

        // ファイルから読み込み
        var yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Template Load Error");
            plugin.getLogger().log(Level.WARNING, e.toString());
            return false;
        }

        // yamlからデータ出し
        var data = yaml.getValues(true);

        // 出したデータをすべてstoreへ
        data.forEach((key, value) -> store.put(key, (List<CStationInfo>) value));

        return true;
    }

    /**
     * ファイルへの保存を行います
     * @return 成功可否
     */
    public boolean save() {
        // yamlを作る
        var yaml = new YamlConfiguration();

        // mapならそのまま入れることができる
        store.forEach(yaml::set);

        // 保存
        try {
            yaml.save(file);
        } catch (Exception e) {
            // ちゃんとログには記録しよう
            plugin.getLogger().log(Level.WARNING, "Template Save Error");
            plugin.getLogger().log(Level.WARNING, e.toString());
            return false;
        }

        return true;
    }

    /**
     * プラグインがインスタンスを生成するためのメソッド
     * @param plugin プラグイン本体クラス
     */
    CStationListTemplateStore(Advancedautotrain plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "CStationListTemplate.yml");
        load();
    }
}
