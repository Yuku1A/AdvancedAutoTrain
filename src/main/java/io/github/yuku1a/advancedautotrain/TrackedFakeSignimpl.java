package io.github.yuku1a.advancedautotrain;

import com.bergerkiller.bukkit.tc.PowerState;
import com.bergerkiller.bukkit.tc.controller.components.RailPiece;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TrackedFakeSignimpl extends RailLookup.TrackedFakeSign {
    private String[] store = new String[4];
    private BlockFace face;
    public TrackedFakeSignimpl(RailPiece rail, BlockFace face) {
        super(rail);
        this.face = face;
    }

    @Override
    public String getLine(int index) throws IndexOutOfBoundsException {
        return store[index];
    }

    @Override
    public void setLine(int index, String line) throws IndexOutOfBoundsException {
        store[index] = line;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    public BlockFace getFacing() {
        return face;
    }

    @Override
    public Block getAttachedBlock() {
        return null;
    }

    @Override
    public String[] getExtraLines() {
        return new String[0];
    }

    @Override
    public PowerState getPower(BlockFace from) {
        return PowerState.ON;
    }
}
