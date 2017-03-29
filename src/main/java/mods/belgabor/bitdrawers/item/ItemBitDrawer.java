package mods.belgabor.bitdrawers.item;

import com.jaquadro.minecraft.chameleon.resources.IItemMeshMapper;
import com.jaquadro.minecraft.chameleon.resources.IItemVariantProvider;
import com.jaquadro.minecraft.storagedrawers.block.EnumCompDrawer;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.mojang.realmsclient.gui.ChatFormatting;
import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.block.tile.TileBitDrawers;
import mods.belgabor.bitdrawers.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Belgabor on 19.07.2016.
 * Based on ItemCompDrawers by jaquadro
 */

public class ItemBitDrawer extends ItemBlock implements IItemMeshMapper, IItemVariantProvider
{
    public ItemBitDrawer(Block block) {
        super(block);
    }

    @Override
    public int getMetadata (int damage) {
        return damage;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean placeBlockAt (ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
            return false;

        TileBitDrawers tile = (TileBitDrawers) world.getTileEntity(pos);
        if (tile != null) {
            if (side != EnumFacing.UP && side != EnumFacing.DOWN)
                tile.setDirection(side.ordinal());

            int initCapacity = BitDrawers.config.bitdrawerStorage;
            tile.setDrawerCapacity(initCapacity);

            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("tile"))
                tile.readFromPortableNBT(stack.getTagCompound().getCompoundTag("tile"));

            tile.setIsSealed(false);
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation (@Nullable ItemStack itemStack, @Nullable EntityPlayer player, @Nullable List<String> list, boolean par4) {
        if (list == null)
            return;
        
        ConfigManager config = BitDrawers.config;

        list.add(I18n.translateToLocalFormatted("storageDrawers.drawers.description", config.bitdrawerStorage));

        if ((itemStack != null) && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("tile")) {
            list.add(ChatFormatting.YELLOW + I18n.translateToLocal("storageDrawers.drawers.sealed"));
        }
    }

    @Override
    public List<ResourceLocation> getItemVariants () {
        ResourceLocation location = GameData.getItemRegistry().getNameForObject(this);
        List<ResourceLocation> variants = new ArrayList<ResourceLocation>();

        if (location != null)
            for (EnumCompDrawer type : EnumCompDrawer.values())
                variants.add(new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + '_' + type.getName()));

        return variants;
    }

    @Override
    public List<Pair<ItemStack, ModelResourceLocation>> getMeshMappings () {
        List<Pair<ItemStack, ModelResourceLocation>> mappings = new ArrayList<Pair<ItemStack, ModelResourceLocation>>();

        for (EnumCompDrawer type : EnumCompDrawer.values()) {
            ModelResourceLocation location = new ModelResourceLocation(BitDrawers.MODID + ":bitdrawer_" + type.getName(), "inventory");
            mappings.add(Pair.of(new ItemStack(this, 1, type.getMetadata()), location));
        }

        return mappings;
    }
}
