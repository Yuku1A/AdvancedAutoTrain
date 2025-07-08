package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionAnnounce;
import com.bergerkiller.bukkit.tc.signactions.SignActionStation;
import com.bergerkiller.bukkit.tc.signactions.SignActionMode;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;

/**
 * Configurable Station SignActionを実装するためのクラス
 */
public class SignActionCStation extends SignAction {
    // TODO: TrainCarts-1.21.5-v2(or later)がリリースされたらTrainCartsSignActionを使うようにする
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
        if (stationlist == null) {
            fireEvent(info, name, false);
            return;
        }

        // CStationListからInfoを取り出す
        var stationinfo = stationlist.get();

        // stationinfoがnullだった場合はリストが空
        if (stationinfo == null) {
            fireEvent(info, name, false);
            return;
        }

        // 名前と一致しなければなかったことにする
        if (!name.equals(stationinfo.getName())) {
            fireEvent(info, name, false);
            return;
        }

        // fakesignをあまり頻繁に作りたくないので作らずに済む場合先に返す
        if(type == SignActionType.GROUP_LEAVE) {
            // stationlistを次に進める
            stationlist.forward();

            // イベントを動かす
            fireEvent(info, name, true);
            return;
        }

        // ↓の処理はGROUP_ENTER時のもの
        // SignTextのnullチェック
        if (stationinfo.getSignText() == null) {
            fireEvent(info, name, true);
            return;
        }

        // announceをする、nullだったらなし
        if (stationinfo.getAnnounce() != null) {
            SignActionAnnounce.sendMessage(info, train, stationinfo.getAnnounce());
        }

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
        fireEvent(info, name, true);
    }

    private void fireEvent(SignActionEvent event, String CStationName, boolean acted) {
        // 必要な情報を集めてイベントを発火
        var railloc = event.getRailLocation();
        var train = event.getGroup();

        switch (event.getAction()) {
            case GROUP_ENTER ->
                plugin.getServer().getPluginManager().callEvent(new CStationEnterEvent(railloc, train, CStationName, acted));
            case GROUP_LEAVE ->
                plugin.getServer().getPluginManager().callEvent(new CStationLeaveEvent(railloc, train, CStationName, acted));
        }
    }

    @Override
    public boolean isRailSwitcher(SignActionEvent info) {
        return true;
    }

    @Override
    public boolean build(SignChangeActionEvent e) {
        var player = e.getPlayer();

        // とりあえずこれはやる必要がある
        var opt = SignBuildOptions.create()
            .setPermission(plugin.UsePermission)
            .setName("cstation")
            .setDescription("configurable station");
        var success = opt.handle(player);

        // 設置が失敗した場合
        if (!success)
            return false;

        // 設定が成功した場合、その内容をユーザーのチャットログに流す
        var cstationName = e.getLine(2);
        player.sendMessage("CStation Name : " + cstationName);

        // CStationCacheへこのCStationの内容を登録
        plugin.getCStationCacheSet().add(cstationName);

        // opt.handle()の実質的な実行結果を返す
        return true;
    }

    @Override
    public void destroy(SignActionEvent info) {
        // このCStationの情報を削除
        var cstationName = info.getLine(2);
        plugin.getCStationCacheSet().remove(cstationName);

        super.destroy(info);
    }

    public SignActionCStation(Advancedautotrain plugin) {
        this.plugin = plugin;
    }
}
