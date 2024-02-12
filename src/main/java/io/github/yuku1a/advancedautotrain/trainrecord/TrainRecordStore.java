package io.github.yuku1a.advancedautotrain.trainrecord;

import com.bergerkiller.bukkit.common.Task;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;
/**
 * {@inheritDoc}
 * enableされる前にload()するとLocaitonがnullになる可能性があります。<br>
 * 実際にコンテンツが読み込まれるタイミングはすべてのワールドが読み込まれて<br>
 * tickループが始まって以降です。
 */
public class TrainRecordStore extends SaveDataStore<TrainRecordList> {
    private final Task task;
    public TrainRecordStore(Advancedautotrain plugin) {
        super(plugin, "TrainRecords.yml");
        task = new Task(plugin) {
            @Override
            public void run() {
                loadInternal();
            }
        };
    }

    private void loadInternal() {
        super.load();
    }

    @Override
    public boolean load() {
        task.start(10);
        return true;
    }
}
