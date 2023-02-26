package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionStation;
import com.bergerkiller.bukkit.tc.signactions.SignActionMode;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;

/**
 * Configurable Station SignActionを実装するためのクラス
 */
public class SignActionCStation extends SignAction {
    private final Advancedautotrain plugin;
    @Override
    public boolean match(SignActionEvent info) {
        if (info.getMode() == SignActionMode.NONE)
            return false;
        return info.isType("cstation");
    }

    @Override
    public void execute(SignActionEvent info) {
        // SignActionTypeを取得しておく
        var type = info.getAction();

        // ここに書いてある以外のイベントをすべて蹴る
        switch (type){
            case GROUP_ENTER,
                GROUP_LEAVE -> {  }
            default -> { return; }
        }

        // 看板につけられた名前を取得
        var name = info.getLine(2);

        // Train(Group)を取得
        var train = info.getGroup();

        // TrainPropertiesからCStationListを取得
        var stationlist = train.getProperties().get(plugin.getcStationListProperty());

        // TrainPropertiesにListが関連付けされていないとnullになるのでチェック
        // そのままなかったことに
        if (stationlist == null)
            return;

        // CStationListからInfoを取り出す
        var stationinfo = stationlist.get();

        // 名前と一致しなければなかったことにする
        if (!name.equals(stationinfo.getName()))
            return;

        // fakesignをあまり頻繁に作りたくないので作らずに済む場合先に返す
        if(type == SignActionType.GROUP_LEAVE) {
            // stationlistを次に進める
            stationlist.forward();

            // イベントを動かす
            plugin.getServer().getPluginManager().callEvent(new CStationLeaveEvent(name));
            return;
        }

        // ↓の処理はGROUP_ENTER時のもの
        // fakesignの準備
        var rail = info.getRailPiece();
        var face = info.getTrackedSign().getFacing();
        var fsign = new TrackedFakeSignimpl(rail, face);

        // fakesignを実際に作る
        fsign.setLine(0, "[+train]");
        fsign.setLine(1, stationinfo.getSignText()[0]);
        fsign.setLine(2, stationinfo.getSignText()[1]);
        fsign.setLine(3, stationinfo.getSignText()[2]);

        // fakesignからイベントを作る
        var fevent = fsign.createEvent(info.getAction());

        // 念のためにfeventにgroupを登録
        fevent.setGroup(train);

        // station看板を実行する、GROUP_ENTERの1回だけでよさそう
        SignActionStation.executeAll(fevent);
    }

    @Override
    public boolean isRailSwitcher(SignActionEvent info) {
        return true;
    }

    @Override
    public boolean build(SignChangeActionEvent e) {
        return SignBuildOptions.create()
            .setName("cstation").handle(e.getPlayer());
    }

    public SignActionCStation(Advancedautotrain plugin) {
        this.plugin = plugin;
    }
}
