package io.github.yuku1a.advancedautotrain.schedaction;

/**
 * OperationTimerをベースにして動くオブジェクトを表します。
 */
public interface TimerListener {
    /**
     * タイマーの時間が自然経過以外に変更された場合に呼び出されます。
     */
    void onTimerModify();
}
