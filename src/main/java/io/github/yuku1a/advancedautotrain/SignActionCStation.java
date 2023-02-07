package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionStation;
import com.bergerkiller.bukkit.tc.signactions.SignActionMode;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
/**
 * Configurable Station
 */
public class SignActionCStation extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        if (info.getMode() == SignActionMode.NONE)
            return false;
        return info.isType("cstation");
    }

    @Override
    public void execute(SignActionEvent info) {
        var name = info.getLine(2);
        var rail = info.getRailPiece();
        var face = info.getTrackedSign().getFacing();
        var fsign = new TrackedFakeSignimpl(rail, face);
        fsign.setLine(0, "[+train]");
        fsign.setLine(1, "station 30");
        fsign.setLine(2, "5");
        if (name.equals("rev"))
            fsign.setLine(3, "reverse");
        else
            fsign.setLine(3, "continue");
        var fevent = fsign.createEvent(info.getAction());
        // なんかわからんけどこれのときだけgroupがnull化する
        if (info.getAction() == SignActionType.GROUP_LEAVE)
            fevent.setGroup(info.getGroup());
        SignActionStation.executeAll(fevent);
    }

    @Override
    public boolean build(SignChangeActionEvent e) {
        return SignBuildOptions.create()
            .setName("cstation").handle(e.getPlayer());
    }

}
