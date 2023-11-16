package io.github.yuku1a.advancedautotrain.lspawn;

import io.github.yuku1a.advancedautotrain.schedaction.ScheduledActionSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ScheduledSpawnSet extends ScheduledActionSet<ScheduledSpawn> {

    /**
     * 既存のScheduledActionのリストからScheduledActionSetを生成します。
     * @param collection     既存のScheduledActionのコレクション
     * @param operationTimer 基準とするタイマー
     */
    public ScheduledSpawnSet(Collection<ScheduledSpawn> collection, String operationTimer) {
        super(collection, operationTimer);
    }

    /**
     * 新規にScheduledActionSetを生成します。
     * @param operationTimer 基準とするタイマー
     */
    public ScheduledSpawnSet(String operationTimer) {
        super(operationTimer);
    }

    @SuppressWarnings("unchecked")
    public static ScheduledSpawnSet deserialize(Map<String, Object> map) {
        return new ScheduledSpawnSet((List<ScheduledSpawn>) map.get("list"), (String) map.get("timer"));
    }
}
