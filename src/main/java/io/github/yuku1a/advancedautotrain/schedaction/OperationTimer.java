package io.github.yuku1a.advancedautotrain.schedaction;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 鉄道のネットワークにおける時間の基準を提供します。
 */
public class OperationTimer implements ConfigurationSerializable {
    private final long interval;
    private final List<TimerListener> followers = new ArrayList<>();
    private long startTime;
    private long diffTime;

    /**
     * 周期のみでOperationTimerを初期化します。
     * @param interval 周期(ミリ秒単位)
     */
    public OperationTimer(long interval) {
        this(interval, 0);
    }

    /**
     * 周期とdiffTimeでOperationTimerを初期化します。
     * @param interval 周期(ミリ秒単位)
     * @param diffTime 前回終了時の経過時間(ミリ秒単位)(周期内)
     */
    public OperationTimer(long interval, long diffTime) {
        this.startTime = System.currentTimeMillis();
        this.interval = interval;
        this.diffTime = diffTime;
    }

    public static OperationTimer deserialize(Map<String, Object> map) {
        return new OperationTimer(
            Long.parseUnsignedLong((String) map.get("Interval")),
            Long.parseUnsignedLong((String) map.get("DiffTime")));
    }

    /**
     * 通知を受け取らないようにします。
     * @param follower TimerListener
     */
    public void unregisterActionSet(TimerListener follower) {
        followers.remove(follower);
    }

    /**
     * このOperationTimerが変更されたときに通知を受け取れるようにします。
     * @param follower 通知を受けるTimerListener
     */
    public void registerActionSet(TimerListener follower) {
        if (follower == null)
            return;
        followers.add(follower);
    }

    /**
     * このタイマーが稼働を開始した時刻を取得します。
     * @return 稼働開始時刻(ミリ秒単位)(エポック時間との差)
     */
    protected long getStartTime() {
        return startTime;
    }

    /**
     * このタイマーの周期を取得します。
     * @return このタイマーの周期(ミリ秒単位)
     */
    public long getInterval() {
        return interval;
    }

    /**
     * 前回終了時の経過時間の情報を復元します。
     */
    public void restore() {
        this.startTime = System.currentTimeMillis() - diffTime;
        followers.forEach(TimerListener::onTimerModify);
    }

    /**
     * 状態を保存するためにフリーズします。
     */
    public void freeze() {
        this.diffTime = currentTime();
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("Interval", String.valueOf(interval));
        map.put("DiffTime", String.valueOf(diffTime));
        return map;
    }

    /**
     * 現在の周期の開始時刻を取得します。
     * @return 現在の周期の開始時刻(ミリ秒単位)(エポック時間との差)
     */
    public long recentBaseTime() {
        // startTimeからどれぐらい時間が経ってるか計算
        long elapsed = System.currentTimeMillis() - getStartTime();

        // 経過時間から何周してるかを計算
        long times = elapsed / getInterval();

        // 現在の周期の開始時間を計算
        return getStartTime() + times * getInterval();
    }

    /**
     * 現在の周期を指定された時間分ずらします。<br>
     * マイナスの値が入力されると周期内の経過時間が減少し(時間が戻る)、<br>
     * プラスの値が入力されると周期内の経過時間が増加します(時間が進む)。
     * @param diffTime ずらす時間(ミリ秒単位)
     */
    public void modifyBaseTime(long diffTime) {
        startTime = startTime - diffTime;
        followers.forEach(TimerListener::onTimerModify);
    }

    /**
     * 周期内の現在時刻
     * @return 周期内の現在時刻
     */
    public long currentTime() {
        return System.currentTimeMillis() - recentBaseTime();
    }
}
