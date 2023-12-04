package io.github.yuku1a.advancedautotrain.lspawn;

import com.bergerkiller.bukkit.common.Task;
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
        this.task.start(20, 20);
    }

    public void disable() {
        this.task.stop();
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
