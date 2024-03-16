package io.github.yuku1a.advancedautotrain.lspawn;

import com.bergerkiller.bukkit.common.chunk.ForcedChunk;
import com.bergerkiller.bukkit.common.utils.MathUtil;
import com.bergerkiller.bukkit.tc.offline.sign.OfflineSign;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ChunkHolderFromOffilineSign {
    private final UUID worldUUID;
    private final int chunkX;
    private final int chunkZ;
    private ForcedChunk chunk;

    /**
     * enableフェーズが完了した状態で実行してください
     * @param offlineSign OfflineSign
     */
    public ChunkHolderFromOffilineSign(OfflineSign offlineSign) {
        this.worldUUID = offlineSign.getWorldUUID();
        this.chunkX = MathUtil.toChunk(offlineSign.getBlock().getX());
        this.chunkZ = MathUtil.toChunk(offlineSign.getBlock().getZ());
    }

    /**
     * チャンクがロードされているかどうか、ロードされている保証はない
     * @return チャンクがロードされているかどうか
     */
    public boolean isLoaded() {
        return chunk != null;
    }

    /**
     * チャンクをロードします。すでにロードされている場合はしません。<br>
     * 非同期でロードされるのでこのメソッドを実行することに続いて<br>
     * 現地のブロックなどを参照することは推奨されません。
     */
    public void loadChunk() {
        if (!isLoaded())
            this.chunk = ForcedChunk.load(Bukkit.getWorld(worldUUID), chunkX, chunkZ, 3);
    }

    /**
     * チャンクをアンロードします
     */
    public void unloadChunk() {
        if (isLoaded()) {
            this.chunk.close();
            this.chunk = null;
        }

    }

}
