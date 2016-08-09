package mods.belgabor.bitdrawers.block;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockController;
import com.jaquadro.minecraft.storagedrawers.block.IBlockDestroyHandler;
import com.jaquadro.minecraft.storagedrawers.block.IExtendedBlockClickHandler;
import com.jaquadro.minecraft.storagedrawers.network.BlockClickMessage;
import com.jaquadro.minecraft.storagedrawers.network.BlockDestroyMessage;
import mod.chiselsandbits.api.IBitBag;
import mod.chiselsandbits.api.ItemType;
import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.block.tile.TileBitController;
import mods.belgabor.bitdrawers.core.BDLogger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Created by Belgabor on 24.07.2016.
 */
public class BlockBitController extends BlockController implements IExtendedBlockClickHandler, IBlockDestroyHandler {
    public BlockBitController(String name) {
        super(name);
    }
    
    @Override
    public TileBitController createNewTileEntity (World world, int meta) {
        return new TileBitController();
    }
    
    public EnumFacing getDirection (IBlockAccess blockAccess, BlockPos pos) {
        IBlockState state = blockAccess.getBlockState(pos);
        //return (tile != null) ? EnumFacing.getFront(tile.getDirection()) : EnumFacing.NORTH;
        EnumFacing facing = (state != null) ? state.getValue(FACING) : EnumFacing.NORTH;
        return (facing != null) ? facing : EnumFacing.NORTH;
    }


    @Override
    public boolean removedByPlayer (IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (player.capabilities.isCreativeMode) {
            if (world.isRemote) {
                RayTraceResult ray = Minecraft.getMinecraft().objectMouseOver;

                if (getDirection(world, pos) == ray.sideHit) {
                    onBlockClicked(world, pos, player);
                    if (BitDrawers.config.debugTrace)
                        BDLogger.info(StorageDrawers.MOD_ID, "BlockBitController.removedByPlayer with " + ray.toString());
                } else {
                    StorageDrawers.network.sendToServer(new BlockDestroyMessage(pos));
                }
            }

            return false;
        }

        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void onBlockDestroyed(final World world, final BlockPos pos) {
        if(!world.isRemote) {
            ((WorldServer)world).addScheduledTask(() -> BlockBitController.this.onBlockDestroyedAsync(world, pos));
        }
    }

    private void onBlockDestroyedAsync(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote?11:3);
    }
    
    @Override
    public void onBlockClicked (World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            RayTraceResult ray = Minecraft.getMinecraft().objectMouseOver;
            BlockPos posb = ray.getBlockPos();
            float hitX = (float)(ray.hitVec.xCoord - posb.getX());
            float hitY = (float)(ray.hitVec.yCoord - posb.getY());
            float hitZ = (float)(ray.hitVec.zCoord - posb.getZ());

            StorageDrawers.network.sendToServer(new BlockClickMessage(pos.getX(), pos.getY(), pos.getZ(), ray.sideHit.ordinal(), hitX, hitY, hitZ, StorageDrawers.config.cache.invertShift));

            if (BitDrawers.config.debugTrace)
                BDLogger.info("BlockBitController.onBlockClicked with " + ray.toString());
        }
    }

    @Override
    public void onBlockClicked (final World world, final BlockPos pos, final EntityPlayer player, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final boolean invertShift) {
        if (world.isRemote)
            return;

        if (BitDrawers.config.debugTrace)
            BDLogger.info("BlockBitController:onBlockClicked %f %f %f", hitX, hitY, hitZ);

        ((WorldServer)world).addScheduledTask(() -> BlockBitController.this.onBlockClickedAsync(world, pos, player, side, hitX, hitY, hitZ, invertShift));
    }

    protected void onBlockClickedAsync (World world, BlockPos pos, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ, boolean invertShift) {
        if (BitDrawers.config.debugTrace)
            BDLogger.info("IExtendedBlockClickHandler.onBlockClicked");

        if (!player.capabilities.isCreativeMode) {
            PlayerInteractEvent.LeftClickBlock event = new PlayerInteractEvent.LeftClickBlock(player, pos, side, new Vec3d(hitX, hitY, hitZ));
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled())
                return;
        }
        TileBitController tileDrawers = (TileBitController) getTileEntitySafe(world, pos);
        if (getDirection(world, pos).ordinal() != side.ordinal())
            return;
        
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
