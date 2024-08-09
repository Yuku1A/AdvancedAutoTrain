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
    private final ForcedChunk chunk;

    /**
     * 0だったら未ロード、1から2までがロード中、3がロード完了
     */
    private int chunkStatus;

    /**
     * enableフェーズが完了した状態で実行してください
     * @param offlineSign OfflineSign
     */
    public ChunkHolderFromOffilineSign(OfflineSign offlineSign) {
        this.worldUUID = offlineSign.getWorldUUID();
        this.chunkX = MathUtil.toChunk(offlineSign.getBlock().getX());
        this.chunkZ = MathUtil.toChunk(offlineSign.getBlock().getZ());
        this.chunk = ForcedChunk.none();
        this.chunkStatus = 0;
    }

    /**
     * チャンクがロードされているかどうか、ロードされている保証はない
     * @return チャンクがロードされているかどうか
     */
    public boolean isLoaded() {
        // chunkがnullだったらfalse
        if (chunk == null)
            return false;

        // chunkがnoneだったらfalse
        if (chunk.isNone())
            return false;

        // ロード完了じゃなければfalse
        return chunkStatus == 3;
    }

    /**
     * チャンクをロードします。すでにロードされている場合はしません。<br>
     * 非同期でロードされるのでこのメソッドを実行することに続いて<br>
     * 現地のブロックなどを参照することは推奨されません。<br>
     * このメソッドは実際にチャンク読み込みが必要になる前に<br>
     * 十分な余裕をもって間隔をあけて3回実行されることを想定しています。
     */
    public void loadChunk() {
        if (!isLoaded()) {
            if (chunk != null) {
                chunkStatus++;
                this.chunk.move(ForcedChunk.load(Bukkit.getWorld(worldUUID), chunkX, chunkZ, chunkStatus));
            }
        }
    }

    /**
     * チャンクをアンロードします
     */
    public void unloadChunk() {
        if (chunk != null) {
            this.chunk.move(ForcedChunk.none());
            chunkStatus = 0;
        }
    }
}
