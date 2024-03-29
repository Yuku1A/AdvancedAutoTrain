package io.github.yuku1a.advancedautotrain.schedaction;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * 予定された複数のアクションを表現します。<br>
 * このクラスは実際にはConfigurationSerializableとしては機能しません。<br>
 * deserialize()などを実装してください。
 */
public abstract class ScheduledActionSet<T extends ScheduledAction> implements TimerListener, ConfigurationSerializable {

    /**
     * 次のタスクの予定時刻までの時間を計算します。
     * @return 次のタスクまでの時間、異常であったり停止されていればMAX_VALUE
     */
    public long remaining() {
        // immediateは計算せずに実行できるので0で返す
        if (immediate){
            long immremain = immediateTime - System.currentTimeMillis();
            if (immremain < 0)
                return 0;
            else
                return immremain;
        }

        // ちゃんと有効化されていないなら異常値を返す
        if (!isValid())
            return Long.MAX_VALUE;

        // 要素が0の場合計算をしない
        if (set.isEmpty())
            return Long.MAX_VALUE;

        // 時刻と周期の取得
        var currentTime = timer.currentTime();
        var interval = timer.getInterval();

        // 変数を置いとく
        long remain;

        if (set.first().equals(nextAction)) {
            // 最初のタスクが選択されている
            // つまり最後のタスクを通過している状態
            if (set.size() == 1) {
                // 1個だと猶予がないとスキップされてしまう
                if (set.last().getScheduletime() < currentTime - 1500) {
                    // タスクが実行されてから終わりまでの間
                    remain = passedTime(interval, currentTime);
                } else {
                    // 0からタスクまでの間
                    remain = normalTime(currentTime);
                }
            } else {
                // 2個以上あってここに来た場合適切にforwardされているので
                // 普通に判定できる
                if (set.last().getScheduletime() < currentTime) {
                    // 最後のタスクから終わりまでの間
                    remain = passedTime(interval, currentTime);
                } else {
                    // 0から最初のタスクまでの間
                    remain = normalTime(currentTime);
                }
            }
        } else {
            // 普通の状態
            // 普通に計算する
            // 周期の予定時刻との差を計算
            remain = normalTime(currentTime);
        }

        // 猶予があるならそのまま返す
        if (remain > 0)
            return remain;
        // 遅れている場合は1.5秒以内なら0で返す
        if (remain > -1500)
            return 0;
        // 1.5秒以上遅れている場合はなかったことにして次のアクションのremainingを返す
        forward();
        return remaining();
    }

    // 最後のタスクを通過した場合
    // 周期の残り時間+最初のタスクの予定時刻
    private long passedTime(long interval, long currentTime) {
        return (interval - currentTime) + nextAction.getScheduletime();
    }

    // 普通に計算する
    // 周期の予定時刻との差を計算
    private long normalTime(long currentTime) {
        return nextAction.getScheduletime() - currentTime;
    }

    /**
     * nextActionを一つ進めます。
     */
    public void forward() {
        // immediateが設定してあったら、
        // 時間に合わない可能性が高いので
        // 再計算する(再計算の過程でimmediateが解除される)
        if (immediate)
            reCalculate();

        // pause時は再計算もされずnextActionがnullなのでやらない
        if (!isValid())
            return;

        if (set.isEmpty())
            return;

        // nextActionがセットの最後だったら最初に戻る
        if (set.last().getScheduletime() == nextAction.getScheduletime()) {
            nextAction = as(set.first());
            return;
        }

        // そうでないなら今のnextActionの一つ次をnextActionへ
        nextAction = as(set.higher(nextAction));
    }

    /**
     * nextActionを再計算します。
     */
    public void reCalculate() {
        // immediateが設定してあったら元に戻す
        if (immediate)
            resetImmediate();

        // 正常じゃなければ蹴る
        if (!isValid())
            return;

        // 中身がなければ計算のしようがない
        if (set.isEmpty())
            return;

        // currentTimeを取得
        var currentTime = timer.currentTime();

        // セットの最後の時間より後ろだったら最初のオブジェクトをセットする
        if (set.last().getScheduletime() <= currentTime) {
            nextAction = as(set.first());
            return;
        }

        // そうでないなら現在時刻の次にあるオブジェクトをセット
        nextAction = as(set.higher(new ScheduledAction(currentTime)));
    }

    /**
     * 任意のアクションをこのクラスから<br>
     * 即座に実行するように設定します。
     * @param action アクション
     */
    public void setImmediate(T action) {
        immediate = true;
        immediateTime = System.currentTimeMillis() + 10000;
        nextAction = action;
    }

    private void resetImmediate() {
        immediate = false;
        immediateTime = Long.MAX_VALUE;
        nextAction = null;
    }

    private boolean immediate = false;
    private long immediateTime = Long.MAX_VALUE;

    /**
     * このクラスが正常かどうか
     * @return このクラスが正常かどうか
     */
    public boolean isValid() {
        if (paused)
            return false;
        return timer != null;
    }

    /**
     * このセットからアクションが取得されるのを一時停止します。
     */
    public void pause() {
        if (paused)
            return;

        paused = true;
        nextAction = null;
    }

    /**
     * このセットからアクションが取得されるのが一時停止されている場合、再開します。
     */
    public void resume() {
        if (!paused)
            return;

        paused = false;
        reCalculate();
    }
    private boolean paused = false;

    /**
     * このセットを有効化します。<br>
     * OperationTimerの読み込みが完了して以降いつでも実行できます。
     * @param plugin このクラスを実装するプラグイン
     * @return 有効化が成功したかどうか
     */
    public boolean enable(Advancedautotrain plugin) {
        this.timer = plugin.getOperationTimerStore().get(timerkey);
        if (isValid()) {
            timer.registerActionSet(this);
            reCalculate();
        }
        return isValid();
    }

    /**
     * このセットに格納されている要素の一覧をリストとして取得します。
     * @return 変更不可能なリスト
     */
    @SuppressWarnings("unchecked")
    public List<T> asList() {
        return (List<T>) List.copyOf(set);
    }

    /**
     * アクションの予定を削除します。
     * @param actionTime アクションが予定されている時刻
     */
    public void remove(long actionTime) {
        set.remove(new ScheduledAction(actionTime));
        reCalculate();
    }

    /**
     * アクションの予定を追加します。
     * @param action アクション
     */
    public void add(T action) {
        set.add(action);
        reCalculate();
    }
    private final NavigableSet<ScheduledAction> set;
    /**
     * 次のアクションを取得します。
     * @return 次のアクション。クラス生成時に空のコレクションが登録されていればnull
     */
    public T getNextAction() {
        return nextAction;
    }
    private T nextAction = null;

    /**
     * このセットが基準としているタイマーを取得します。
     * @return このセットが基準としているタイマー
     */
    public OperationTimer getTimer() {
        return timer;
    }
    private OperationTimer timer = null;

    /**
     * このセットが基準とするタイマーの名前を取得します。
     * @return このセットが基準とするタイマーの名前
     */
    public String getTimerkey() {
        return timerkey;
    }
    private final String timerkey;

    /**
     * 既存のScheduledActionのリストからScheduledActionSetを生成します。
     * @param operationTimer 基準とするタイマー
     * @param collection 既存のScheduledActionのコレクション
     */
    public ScheduledActionSet(Collection<T> collection, String operationTimer) {
        // 渡されたCollectionから生成する
        set = new TreeSet<>(collection);

        // 仕方なくkeyのみ
        timerkey = operationTimer;

        // 何もないとバグるので入れる
        // できればコンストラクタに例外をつけたいが呼び出し側がめんどいのでやめ
        if (!set.isEmpty())
            nextAction = as(set.first());
    }

    /**
     * 新規にScheduledActionSetを生成します。
     * @param operationTimer 基準とするタイマー
     */
    public ScheduledActionSet(String operationTimer) {
        this(Collections.emptySet(), operationTimer);
    }

    @Override
    public void onTimerModify() {
        reCalculate();
    }

    @SuppressWarnings("unchecked")
    private T as(ScheduledAction o) {
        return (T) o;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("list", asList());
        map.put("timer", getTimerkey());
        return map;
    }
}
