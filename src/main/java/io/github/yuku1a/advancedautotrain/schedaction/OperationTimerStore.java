package io.github.yuku1a.advancedautotrain.schedaction;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

/**
 * OperationTimerを管理します。
 */
public class OperationTimerStore extends SaveDataStore<OperationTimer> {
    public OperationTimerStore(Advancedautotrain plugin) {
        super(plugin, "OperationTimer.yml");
    }

    /**
     * OperationTimerを保存可能な状態にします。
     */
    public void freeze() {
        getStore().forEach((key, value) -> value.freeze());
    }

    /**
     * 保存されているOperationTimerを前回終了時の状態に復元します。
     */
    public void restore() {
        getStore().forEach((key, value) -> value.restore());
    }
}
