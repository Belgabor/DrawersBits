package mods.belgabor.bitdrawers.event;

import mod.chiselsandbits.items.ItemChiseledBit;
import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.block.BlockBitDrawers;
import mods.belgabor.bitdrawers.core.BDLogger;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Belgabor on 19.07.2016.
 */
public class PlayerEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void interaction(
            final PlayerInteractEvent.LeftClickBlock event )
    {
        if (BitDrawers.config.debugTrace) {
            Vec3d vec = event.getHitVec();
            BDLogger.info("Left Click %s %s %f %f %f", resultToString(event.getUseItem()), resultToString(event.getUseBlock()), vec.xCoord, vec.yCoord, vec.zCoord);
        }
        if ( event.getEntityPlayer() != null && event.getUseItem() != Event.Result.DENY )
        {
            final ItemStack is = event.getItemStack();
            if ( is != null && (is.getItem() instanceof ItemChiseledBit) && event.getWorld().isRemote)
            {
                Block target = event.getWorld().getBlockState(event.getPos()).getBlock();
                if (target instanceof BlockBitDrawers) {
                    target.onBlockClicked(event.getWorld(), event.getPos(), event.getEntityPlayer());
                }
                if (BitDrawers.config.debugTrace)
                    BDLogger.info("Bit Left Click");
            }
        }
    }

    private String resultToString(Event.Result result) {
        switch (result) {
            case ALLOW:
                return "ALLOW";
            case DEFAULT:
                return "DEFAULT";
            case DENY:
                return "DENY";
        }
        return "UNKNOWN";
    }
}
