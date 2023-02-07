package io.github.yuku1a.advancedautotrain;

import org.bukkit.plugin.java.JavaPlugin;

import com.bergerkiller.bukkit.tc.signactions.SignAction;
public final class Advancedautotrain extends JavaPlugin {
    private final SignActionCStation signActionCStation = new SignActionCStation();

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        SignAction.register(signActionCStation);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        SignAction.unregister(signActionCStation);
    }
}
