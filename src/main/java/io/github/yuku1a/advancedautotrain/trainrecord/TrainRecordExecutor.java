package io.github.yuku1a.advancedautotrain.trainrecord;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.GroupRemoveEvent;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CStationEnterEvent;
import io.github.yuku1a.advancedautotrain.CStationLeaveEvent;
import io.github.yuku1a.advancedautotrain.lspawn.NamedTrainSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class TrainRecordExecutor implements Listener {
    @EventHandler
    public void onTrainSpawn(NamedTrainSpawnEvent event){
        var train = event.getTrain();
        var trainname = event.getTrainName();
        // RecordingManagerに列車と名前の紐づけを登録
        recman.startRecording(train, trainname);
        // 新しいTrainRecordListを作る
        var recordList = new TrainRecordList();
        var loc = train.middle().getBlock().getLocation();
        // spawnを記録する
        var record = new TrainRecord(loc, null, "spawn", "spawn", true);
        recordList.startRecord(record);
        // 作ったものを登録
        store.put(trainname, recordList);
    }

    private TrainRecordList getRecordList(MinecartGroup train) {
        var trainName = recman.getTrainName(train);
        // 何もなければ先に帰す、多少パフォーマンス上がると信じたい
        if (trainName == null)
            return null;
        return store.get(trainName);
    }

    @EventHandler
    public void onCStatonEnter(CStationEnterEvent event) {
        var train = event.getTrain();
        var recordlist = getRecordList(train);
        // 何もなければ先に帰す
        if (recordlist == null)
            return;

        // 通過ならleaveだけで十分
        if (!event.isActed())
            return;

        recordlist.record(new TrainRecord(event.getRailLocation(), event.getCStationName(), "cstation", "cstation_enter", event.isActed()));
    }

    @EventHandler
    public void onCStatonLeave(CStationLeaveEvent event) {
        var train = event.getTrain();
        var recordlist = getRecordList(train);
        // 何もなければ先に帰す
        if (recordlist == null)
            return;

        recordlist.record(new TrainRecord(event.getRailLocation(), event.getCStationName(), "cstation", "cstation_leave", event.isActed()));
    }

    @EventHandler
    public void onGroupRemove(GroupRemoveEvent event) {
        var train = event.getGroup();
        var recordlist = getRecordList(train);
        // 何もなければ先に帰したいが一応安全のために
        if (recordlist == null) {
            endRecording(train);
            return;
        }

        // 消滅を記録する
        recordlist.record(new TrainRecord(train.middle().getBlock().getLocation(), null, "destroy", "destroy", true));
        // 後処理
        endRecording(train);
    }

    private void endRecording(MinecartGroup train) {
        recman.endRecording(train);
    }

    private final TrainRecordStore store;
    private final TrainRecordingManager recman;
    public TrainRecordExecutor(Advancedautotrain plugin) {
        this.recman = plugin.getTrainRecordingManager();
        this.store = plugin.getTrainRecordStore();
    }
}
