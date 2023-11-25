package io.github.yuku1a.advancedautotrain.arrivallist;

import com.bergerkiller.bukkit.sl.API.Variables;
import com.bergerkiller.bukkit.tc.ArrivalSigns;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.CStationLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CStationLeaveListener implements Listener {
    private final Advancedautotrain plugin;
    private final ScheduledSignSetStore store;

    public CStationLeaveListener(Advancedautotrain plugin) {
        this.plugin = plugin;
        this.store = plugin.getSignListStore();
    }

    @EventHandler
    public void onCStationLeave(CStationLeaveEvent event) {
        // イベントの情報を出す
        var name = event.getCStationName();

        // リストあるかどうかチェック、なければ蹴る
        var set = store.get(name);

        if (set == null)
            return;

        // レコードモードだったら全く別種の処理を実行する
        if (set.isRecording()) {
            record(set, event);
            return;
        }

        // 空じゃないかどうか確認
        if (set.getNextAction() == null)
            return;

        // MAX_VALUEの場合はなにかがおかしいので処理しない
        if (set.remaining() == Long.MAX_VALUE)
            return;

        // 現在時刻の次の項目を計算
        set.reCalculate();

        // こいつがあれば動かせるらしい
        var timesign = ArrivalSigns.getTimer(name);

        timesign.duration = set.remaining();
        timesign.trigger();
        timesign.update();

        // Variableを取る
        var variablename = Variables.get(name + "N");
        if (variablename == null)
            return;

        var variabledesc = Variables.get(name + "D");
        if (variabledesc == null)
            return;

        // 設定
        variablename.set(set.getNextAction().getTrainName());
        variabledesc.set(set.getNextAction().getTrainDescription());

        // おわり
    }

    private void record(ScheduledSignSet set, CStationLeaveEvent event) {
        // 時間を割り出す
        var rawcurrenttime = set.getTimer().currentTime();

        // 時間を1秒ごとに合わせてOffsetの分ずらす(ミリ秒分は切り捨てられる、時間が合わないことがあるのでいじる)
        var currenttime = ((rawcurrenttime / 1000) + set.getRecordOffset()) * 1000;

        // 時間がマイナスになることがあるのでチェックして修正
        if (currenttime < 0) {
            currenttime = set.getTimer().getInterval() + currenttime;
        }

        // この時間で生成
        var action = new ScheduledSign(currenttime, event.getTrainName());

        // そして登録
        set.add(action);
    }
}
