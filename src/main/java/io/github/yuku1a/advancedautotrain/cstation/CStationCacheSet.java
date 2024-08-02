package io.github.yuku1a.advancedautotrain.cstation;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * どんなCStationがあるかをキャッシュします。
 * これの情報はすべてのCStationを追跡したりできるわけではありません。
 */
public class CStationCacheSet {
    private Set<String> cacheSet;

    /**
     * 内部にあるデータを返します。
     * @return 変更不可能なリスト
     */
    public List<String> get() {
        return List.copyOf(cacheSet);
    }

    /**
     * このクラスに要素を追加します。<br>
     * nullを追加しようとすると無視されます。
     * @param cStationName 追加するCStationの名前
     */
    public void add(String cStationName) {
        if (cStationName != null)
            cacheSet.add(cStationName);
    }

    /**
     * 引数に取られた要素をこのクラスから削除します。
     * @param cStationName 削除するCStationの名前
     */
    public void remove(String cStationName) {
        cacheSet.remove(cStationName);
    }

    private final String key = "CStationCache";
    private final SaveDataStore<List<String>> store;

    public CStationCacheSet(Advancedautotrain plugin) {
        this.store = new SaveDataStore<>(plugin, "CStationCache.yml");
        this.cacheSet = new HashSet<>();
    }

    /**
     * ファイルの情報を読み込みます。
     * @return 成功可否
     */
    public boolean load() {
        var loaded = store.load();
        if (!loaded)
            return false;

        var list = store.get(key);
        if (list == null)
            return false;

        this.cacheSet = new HashSet<>(list);
        return true;
    }

    /**
     * ファイルに情報を保存します。
     * @return 成功可否
     */
    public boolean save() {
        var list = new ArrayList<>(cacheSet);
        store.put(key, list);

        return store.save();
    }
}
