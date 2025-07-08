package io.github.yuku1a.advancedautotrain.lspawn;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionMode;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import io.github.yuku1a.advancedautotrain.Advancedautotrain;

public class SignActionLSpawn extends SignAction {
    // TODO: TrainCarts-1.21.5-v2(or later)がリリースされたらTrainCartsSignActionを使うようにする
    private final Advancedautotrain plugin;
    public SignActionLSpawn(Advancedautotrain plugin) { this.plugin = plugin; }

    @Override
    public boolean match(SignActionEvent info) {
        if (info == null)
            return false;
        if (info.getMode() == SignActionMode.NONE)
            return false;
        return info.isType("lspawn");
    }

    @Override
    public boolean canSupportFakeSign(SignActionEvent info) {
        return false;
    }

    @Override
    public void execute(SignActionEvent info) { }

    @Override
    public boolean build(SignChangeActionEvent event) {
        // 権限チェックとメッセージ表示
        if (!SignBuildOptions.create()
            .setPermission(plugin.UsePermission)
            .setName("train spawner with list")
            .setDescription("spawn trains on the tracks from spawn list")
            .handle(event.getPlayer()))
        {
            return false;
        }

        // 実際に登録処理をしてみる、これの返り値が直接このメソッドの返り値になる
        return plugin.getLSpawnSignManager().register(event);
    }

    @Override
    public void destroy(SignActionEvent info) {
        plugin.getLSpawnSignManager().unregister(info);
    }
}
