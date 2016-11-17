package mods.belgabor.bitdrawers.block;

import com.jaquadro.minecraft.storagedrawers.block.BlockController;
import com.jaquadro.minecraft.storagedrawers.block.IBlockDestroyHandler;
import com.jaquadro.minecraft.storagedrawers.config.ConfigManager;
import com.jaquadro.minecraft.storagedrawers.config.PlayerConfigSetting;
import com.jaquadro.minecraft.storagedrawers.security.SecurityManager;
import mod.chiselsandbits.api.IBitBag;
import mod.chiselsandbits.api.ItemType;
import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.block.tile.TileBitController;
import mods.belgabor.bitdrawers.core.BDLogger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;

/**
 * Created by Belgabor on 24.07.2016.
 */
public class BlockBitController extends BlockController /*implements IBlockDestroyHandler*/ {
    public BlockBitController(String name) {
        super(name);
    }
    
    @Override
    public TileBitController createNewTileEntity (World world, int meta) {
        return new TileBitController();
    }
    
    public EnumFacing getDirection (IBlockAccess blockAccess, BlockPos pos) {
        IBlockState state = blockAccess.getBlockState(pos);
        EnumFacing facing = (state != null) ? state.getValue(FACING) : EnumFacing.NORTH;
        return (facing != null) ? facing : EnumFacing.NORTH;
    }

    @Override
    public boolean removedByPlayer (IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (player.capabilities.isCreativeMode) {
            float blockReachDistance = 0;
            if (world.isRemote) {
                blockReachDistance = Minecraft.getMinecraft().playerController.getBlockReachDistance() + 1;
            } else {
                blockReachDistance = (float) ((EntityPlayerMP) player).interactionManager.getBlockReachDistance() + 1;
            }

            RayTraceResult rayResult = net.minecraftforge.common.ForgeHooks.rayTraceEyes(player, blockReachDistance + 1);
            if (getDirection(world, pos) == rayResult.sideHit) {
                onBlockClicked(world, pos, player);
            } else {
                world.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
            }

            return false;
        }
        
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void onBlockClicked (World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }
        //RayTraceResult ray = Minecraft.getMinecraft().objectMouseOver;
        RayTraceResult ray = net.minecraftforge.common.ForgeHooks.rayTraceEyes(player, ((EntityPlayerMP) player).interactionManager.getBlockReachDistance() + 1);
        EnumFacing side = ray.sideHit;

        if (BitDrawers.config.debugTrace)
            BDLogger.info("BlockBitController.onBlockClicked with " + ray.toString());
        
        TileBitController tileDrawers = (TileBitController) getTileEntitySafe(world, pos);

        if (!SecurityManager.hasAccess(player.getGameProfile(), tileDrawers))
            return;
        
        if (getDirection(world, pos).ordinal() != side.ordinal())
            return;

        Map<String, PlayerConfigSetting<?>> configSettings = ConfigManager.serverPlayerConfigSettings.get(player.getUniqueID());
        boolean invertShift = false;
        if (configSettings != null) {
            PlayerConfigSetting<Boolean> setting = (PlayerConfigSetting<Boolean>) configSettings.get("invertShift");
            if (setting != null) {
                invertShift = setting.value;
            }
        }            

        ItemStack held = player.inventory.getCurrentItem();
        if (held == null && BitDrawers.config.debugTrace) {
            tileDrawers.updateCache();
        }
        ItemType heldType = BitDrawers.cnb_api.getItemType(held);
        IItemHandler handler = held==null?null:held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack item = null;
        if (handler instanceof IBitBag) {
            IBitBag bag = (IBitBag) handler;

            int retrieved = tileDrawers.fillBag(bag, player.getGameProfile());
            if (retrieved > 0 && !world.isRemote)
                world.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, ((world.rand.nextFloat() - world.rand.nextFloat()) * .7f + 1) * 2);
            item = null;
        } else if (heldType != null && heldType.isBitAccess && heldType != ItemType.NEGATIVE_DESIGN) {
            item = tileDrawers.retrieveByPattern(held, player, player.isSneaking() != invertShift);
        }
        IBlockState state = world.getBlockState(pos);
        if (item != null && item.stackSize > 0) {
            if (!player.inventory.addItemStackToInventory(item)) {
                dropItemStack(world, pos.offset(side), player, item);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
            else if (!world.isRemote)
                world.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, ((world.rand.nextFloat() - world.rand.nextFloat()) * .7f + 1) * 2);
        }

    }
    
    protected void dropItemStack (World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        EntityItem entity = new EntityItem(world, pos.getX() + .5f, pos.getY() + .1f, pos.getZ() + .5f, stack);
        entity.addVelocity(-entity.motionX, -entity.motionY, -entity.motionZ);
        world.spawnEntityInWorld(entity);
    }
}
