package io.github.yuku1a.advancedautotrain;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * 各種セーブの必要なデータを管理します。<br>
 * データの型はSpigotのYamlConfigurationでシリアライズ可能な型である必要があります。
 */
public class SaveDataStore<T> {
    private final JavaPlugin plugin;
    private final File file;
    private final Map<String, T> store = new HashMap<>();

    /**
     * 内部ストアを取得します。
     * @return 内部ストア
     */
    protected Map<String, T> getStore() {
        return store;
    }

    public SaveDataStore(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    /**
     * キーの変更不可能なリストを取得します。
     * @return キーのリスト
     */
    public List<String> keysList() {
        return List.copyOf(store.keySet());
    }

    /**
     * 指定されたキーと同じキーのデータが格納されているか確認します。
     * @param key キー
     * @return 指定されたキーのデータが格納されているかどうか
     */
    public boolean containsKey(String key) {
        return store.containsKey(key);
    }

    /**
     * 内部のストアを反映したリストの変更不可能なビューを取得します。
     * @return 内部のストアを反映したリスト
     */
    public List<Map.Entry<String, T>> entryList() {
        return List.copyOf(store.entrySet());
    }

    /**
     * キーと紐づけられたデータを削除します。
     * @param key キー
     */
    public void remove(String key) {
        store.remove(key);
    }

    /**
     * データとキーを紐づけます。
     * @param key キー
     * @param value データ
     */
    public void put(String key, T value) {
        store.put(key, value);
    }

    /**
     * データを取得します。
     * @param key キー
     * @return データ
     */
    public T get(String key) {
        return store.get(key);
    }

    /**
     * ファイルから情報を読み込みます。
     * @return 成功可否
     */
    public boolean load() {
        // 繰り返すと無限に増えるので削除
        store.clear();

        // ファイルから読み込み
        var yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, file.getName() + " Load Error");
            plugin.getLogger().log(Level.WARNING, e.toString());
            return false;
        }

        // yamlからデータ出し
        var data = yaml.getValues(true);

        // 出したデータをすべてstoreへ
        data.forEach((key, value) -> store.put(key, deserialize(value)));

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
        store.forEach((key, value) -> yaml.set(key, serialize(value)));

        // 保存
        try {
            yaml.save(file);
        } catch (Exception e) {
            // ちゃんとログには記録しよう
            plugin.getLogger().log(Level.WARNING, file.getName() + " Save Error");
            plugin.getLogger().log(Level.WARNING, e.toString());
            return false;
        }

        return true;
    }

    /**
     * データをYAMLにシリアライズ可能なオブジェクトに変換します
     * @param data データ
     * @return YAMLでシリアライズする際の(String, Object)のObject
     */
    protected Object serialize(T data) {
        return data;
    }

    /**
     * YAMLから変換されたオブジェクトをデータに変換します
     * @param obj YAMLからデシリアライズされたときの(String, Object)のObject
     * @return データ
     */
    @SuppressWarnings("unchecked")
    protected T deserialize(Object obj) {
        return (T) obj;
    }
}
