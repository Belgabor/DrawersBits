package mods.belgabor.bitdrawers.block;

import com.jaquadro.minecraft.storagedrawers.api.pack.BlockType;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.api.storage.INetworked;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.EnumCompDrawer;
import com.jaquadro.minecraft.storagedrawers.block.dynamic.StatusModelData;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawersComp;
import com.jaquadro.minecraft.storagedrawers.inventory.DrawerInventoryHelper;
import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.block.tile.TileBitDrawers;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by Belgabor on 02.06.2016.
 * Based on BlockCompDrawers by jaquadro
 */
public class BlockBitDrawers extends BlockDrawers implements INetworked
{
    public static final PropertyEnum SLOTS = PropertyEnum.create("slots", EnumCompDrawer.class);

    @SideOnly(Side.CLIENT)
    private StatusModelData statusInfo;

    public BlockBitDrawers (String blockName) {
        super(Material.ROCK, blockName);

        setSoundType(SoundType.STONE);
    }

    @Override
    protected void initDefaultState () {
        setDefaultState(blockState.getBaseState().withProperty(SLOTS, EnumCompDrawer.OPEN1).withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initDynamic () {
        ResourceLocation location = new ResourceLocation(BitDrawers.MODID + ":models/dynamic/bitDrawers.json");
        statusInfo = new StatusModelData(3, location);
    }

    @Override
    public StatusModelData getStatusInfo (IBlockState state) {
        return statusInfo;
    }

    @Override
    public int getDrawerCount (IBlockState state) {
        return 3;
    }

    @Override
    public boolean isHalfDepth (IBlockState state) {
        return false;
    }

    @Override
    protected int getDrawerSlot (int drawerCount, int side, float hitX, float hitY, float hitZ) {
        if (hitTop(hitY))
            return 0;

        if (hitLeft(side, hitX, hitZ))
            return 1;
        else
            return 2;
    }

    @Override
    public IBlockState onBlockPlaced (World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState();
    }

    @Override
    public BlockType retrimType () {
        return null;
    }

    @Override
    public TileEntityDrawers createNewTileEntity (World world, int meta) {
        return new TileBitDrawers();
    }

    @Override
    public void getSubBlocks (Item item, CreativeTabs creativeTabs, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public IBlockState getStateFromMeta (int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState (IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState () {
        return new ExtendedBlockState(this, new IProperty[] { SLOTS, FACING }, new IUnlistedProperty[] { TILE });
    }

    @Override
    public IBlockState getActualState (IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityDrawers tile = getTileEntity(world, pos);
        if (tile == null)
            return state;

        EnumFacing facing = EnumFacing.getFront(tile.getDirection());
        if (facing.getAxis() == EnumFacing.Axis.Y)
            facing = EnumFacing.NORTH;

        EnumCompDrawer slots = EnumCompDrawer.OPEN1;
        if (tile.isDrawerEnabled(1))
            slots = EnumCompDrawer.OPEN2;
        if (tile.isDrawerEnabled(2))
            slots = EnumCompDrawer.OPEN3;

        return state.withProperty(FACING, facing).withProperty(SLOTS, slots);
    }
    
    @Override
    public void breakBlock (World world, BlockPos pos, IBlockState state) {
        TileEntityDrawers tile = getTileEntity(world, pos);

        if (tile != null && !tile.isSealed()) {
            for (int i = 0; i < tile.getUpgradeSlotCount(); i++) {
                ItemStack stack = tile.getUpgrade(i);
                if (stack != null)
                    spawnAsEntity(world, pos, stack);
            }

            if (!tile.isVending())
                DrawerInventoryHelper.dropInventoryItems(world, pos, tile);
        }

        super.breakBlock(world, pos, state);
    }
    
    @Override
    public void onBlockClicked (final World world, final BlockPos pos, final EntityPlayer player, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final boolean invertShift) {
        if (world.isRemote)
            return;

        System.out.println("onBlockClicked");
        super.onBlockClicked(world, pos, player, side, hitX, hitY, hitZ, invertShift);
    }


}
