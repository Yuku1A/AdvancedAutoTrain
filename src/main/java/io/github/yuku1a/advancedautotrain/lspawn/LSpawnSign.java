package io.github.yuku1a.advancedautotrain.lspawn;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.tc.offline.sign.OfflineSign;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.signactions.spawner.SpawnSignManager;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.TrackedFakeSignimpl;

public class LSpawnSign {
    private final Advancedautotrain plugin;
    private OfflineSign offlineSign;
    private String firstLine;
    private String secondLineOptions;
    private String spawnListName;
    private ScheduledSpawnSet spawnList;
    private ChunkHolderFromOffilineSign chunkHolder = null;

    public LSpawnSign(Advancedautotrain plugin, String firstLine, String secondLineOptions, String spawnListName, OfflineSign offlineSign) {
        this.plugin = plugin;
        this.offlineSign = offlineSign;
        plugin.getSpawnListStore().get(spawnListName);
    }

    public String getFirstLine(){
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }

    public String getSecondLineOptions() {
        return secondLineOptions;
    }

    public void setSecondLineOptions(String secondLineOptions) {
        this.secondLineOptions = secondLineOptions;
    }

    public String getSpawnListName() {
        return spawnListName;
    }

    public void setSpawnListName(String spawnListName) {
        this.spawnListName = spawnListName;
        spawnList = plugin.getSpawnListStore().get(spawnListName);
    }

    /**
     * 現在実行すべき動作があるかチェックします。
     */
    public void checkTime() {
        if (spawnList == null)
            return;

        var remaining = spawnList.remaining();

        // 正常に動作していない
        if (remaining == Long.MAX_VALUE)
            return;

        // SpawnSignManager.UpdateTask.run()と同じような挙動にしたい
        if (remaining > SpawnSignManager.SPAWN_LOAD_DEBOUNCE)
            unloadChunk();
        else if (remaining == 0)
            spawn();
        else if (remaining < SpawnSignManager.SPAWN_WARMUP_TIME)
            warmUp();
    }

    public void updateState(OfflineSign offlineSign) {
        this.offlineSign = offlineSign;
        this.selfParse();
    }

    private void spawn() {
        // warmUp()でチャンクがロードされていない場合諦める
        if (!chunkHolder.isLoaded())
            return;

        // ロードされているのでここでFakeSignを作る
        var signBlock = offlineSign.getLoadedBlock();
        var rail = RailLookup.discoverRailPieceFromSign(signBlock);
        var fakesign = new TrackedFakeSignimpl(rail, BlockUtil.getFacing(signBlock));
        fakesign.setLine(0, firstLine);
        fakesign.setLine(1, "spawn" + secondLineOptions);
        fakesign.setLine(2, spawnList.getNextAction().getSavedTrainName());

        // 実行
        var event = fakesign.createEvent(SignActionType.REDSTONE_ON);
        var spawnSign = plugin.getTrainCarts().getSpawnSignManager().create(event);
        spawnSign.spawn(event);
    }

    private void unloadChunk(){
        if (chunkHolder != null)
            chunkHolder.unloadChunk();
    }

    private void warmUp() {
        if (chunkHolder == null) {
            chunkHolder = new ChunkHolderFromOffilineSign(offlineSign);
        }

        chunkHolder.loadChunk();
    }

    private void selfParse() {
        firstLine = offlineSign.getLine(0);
        var secondLine = offlineSign.getLine(1);
        var index = secondLine.indexOf(" ");
        if (index == -1)
            secondLineOptions = "";
        else
            secondLineOptions = secondLine.substring(index);
        spawnListName = offlineSign.getLine(2);
    }

}
