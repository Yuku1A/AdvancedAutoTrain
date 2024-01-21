package io.github.yuku1a.advancedautotrain.lspawn;

import com.bergerkiller.bukkit.common.Task;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.offline.sign.OfflineSign;
import com.bergerkiller.bukkit.tc.offline.sign.OfflineSignMetadataHandler;
import com.bergerkiller.bukkit.tc.offline.sign.OfflineSignSide;
import com.bergerkiller.bukkit.tc.offline.sign.OfflineSignStore;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LSpawnSignManager {
    private final Advancedautotrain plugin;
    private final Task task;
    private final Map<OfflineSignSide, LSpawnSign> spawnSignList;

    public LSpawnSignManager(Advancedautotrain plugin) {
        this.plugin = plugin;
        this.task = newTask();
        this.spawnSignList = new HashMap<>();
    }

    public void load() {
        plugin.getTrainCarts().getOfflineSigns().registerHandler(LSpawnSignMetadata.class, new OfflineSignMetadataHandler<LSpawnSignMetadata>() {
            @Override
            public void onUpdated(OfflineSignStore store, OfflineSign sign, LSpawnSignMetadata oldValue, LSpawnSignMetadata newValue) {
                var spawnSign = spawnSignList.get(sign.getSide());
                if (spawnSign != null)
                    spawnSign.updateState(sign);
            }

            @Override
            public void onAdded(OfflineSignStore store, OfflineSign sign, LSpawnSignMetadata metadata) {
                var spawnSign = new LSpawnSign(plugin, sign);
                spawnSignList.put(sign.getSide(), spawnSign);
            }

            @Override
            public void onRemoved(OfflineSignStore store, OfflineSign sign, LSpawnSignMetadata metadata) {
                var ssign = spawnSignList.remove(sign.getSide());
                if (ssign != null)
                    ssign.unloadChunk();
            }
            @Override
            public LSpawnSignMetadata onSignChanged(OfflineSignStore store, OfflineSign oldSign, OfflineSign newSign, LSpawnSignMetadata metadata) {
                return new LSpawnSignMetadata(true);
            }

            @Override
            public void onEncode(DataOutputStream stream, OfflineSign sign, LSpawnSignMetadata value) throws IOException {
                stream.writeBoolean(true);
            }

            @Override
            public LSpawnSignMetadata onDecode(DataInputStream stream, OfflineSign sign) throws IOException {
                return new LSpawnSignMetadata(true);
            }
        });
    }

    public void enable() {
        this.task.start(10, 10);
    }

    public void disable() {
        this.plugin.getTrainCarts().getOfflineSigns().unregisterHandler(LSpawnSignMetadata.class);
        this.task.stop();
    }

    public boolean register(SignActionEvent event) {
        // nullチェック
        if (event == null)
            return false;

        // 実際に置かれている看板じゃない場合蹴る
        if (!event.getTrackedSign().isRealSign())
            return false;

        // すでに登録されているかどうか確認する
        var signside = OfflineSignSide.of(event.getTrackedSign());
        var stored = spawnSignList.get(signside);

        if (stored != null){
            // すでに登録されている場合、verifysignで更新ができる
            plugin.getTrainCarts().getOfflineSigns().verifySign(event.getSign(), signside.isFrontText(), LSpawnSignMetadata.class);
        }

        // 新規登録の場合の処理
        var metadata = new LSpawnSignMetadata(true);

        // OfflineSignStoreに登録
        plugin.getTrainCarts().getOfflineSigns().put(event.getTrackedSign(), metadata);

        // ↑に登録してあるハンドラー経由でこっちにも自動的に登録されてるはずだから確認
        if (spawnSignList.get(signside) != null)
            return true;
        return false;
    }

    public void unregister(SignActionEvent event) {
        plugin.getTrainCarts().getOfflineSigns().remove(event.getTrackedSign(), LSpawnSignMetadata.class);
    }

    private Task newTask() {
        return new Task(plugin) {
            @Override
            public void run() {
                for (var sign : spawnSignList.values()) {
                    sign.checkTime();
                }
            }
        };
    }
}
