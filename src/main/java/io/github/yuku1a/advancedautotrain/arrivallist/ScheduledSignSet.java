package io.github.yuku1a.extremeautotrain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 看板に表示される発車予定の集まりです。
 */
public class ScheduledSignSet extends ScheduledActionSet<ScheduledSign> {

    private boolean recording = false;
    private long recordOffset = -2;

    /**
     * 既存のScheduledActionのリストからScheduledActionSetを生成します。
     *
     * @param collection     既存のScheduledActionのコレクション
     * @param operationTimer 基準とするタイマー
     */
    public ScheduledSignSet(Collection<ScheduledSign> collection, String operationTimer) {
        super(collection, operationTimer);
    }

    /**
     * 新規にScheduledActionSetを生成します。
     *
     * @param operationTimer 基準とするタイマー
     */
    public ScheduledSignSet(String operationTimer) {
        super(operationTimer);
    }

    @SuppressWarnings("unchecked")
    public static ScheduledSignSet deserialize(Map<String, Object> map) {
        var list = (List<ScheduledSign>)map.get("list");
        var timer = (String) map.get("timer");
        return new ScheduledSignSet(list, timer);
    }

    /**
     * 時刻の記録を実行しているかどうか
     * @return 時刻の記録を実行しているかどうか
     */
    public boolean isRecording() {
        return recording;
    }

    /**
     * 記録の時間のオフセット
     * @return 記録の時間のオフセット
     */
    public long getRecordOffset() {
        return recordOffset;
    }

    /**
     * 時刻の記録をするかどうかを切り替えます
     */
    public void toggleRecord() {
        toggleRecord(recordOffset);
    }

    /**
     * 時刻の記録をするかどうかを切り替えます
     * @param recordOffset 記録の時間のオフセット
     */
    public void toggleRecord(long recordOffset) {
        this.recordOffset = recordOffset;
        recording = !recording;
    }
}
