package mods.belgabor.bitdrawers.core;

import com.jaquadro.minecraft.chameleon.Chameleon;
import com.jaquadro.minecraft.chameleon.resources.ModelRegistry;
import com.jaquadro.minecraft.storagedrawers.client.renderer.TileEntityDrawersRenderer;
import mods.belgabor.bitdrawers.block.BlockBitDrawers;
import mods.belgabor.bitdrawers.block.tile.TileBitDrawers;
import mods.belgabor.bitdrawers.client.model.BitDrawerModel;
import mods.belgabor.bitdrawers.item.ItemBitDrawer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Belgabor on 18.07.2016.
 */
public class BlockRegistry {
    public static BlockBitDrawers bitDrawer;
    
    public void init() {
        bitDrawer = new BlockBitDrawers("bitdrawer");
        GameRegistry.register(bitDrawer);
        GameRegistry.register((new ItemBitDrawer(bitDrawer)).setRegistryName(bitDrawer.getRegistryName()));
        GameRegistry.registerTileEntity(TileBitDrawers.class, bitDrawer.getRegistryName().toString());
    }

    @SideOnly(Side.CLIENT)
    public void initClient() {
        bitDrawer.initDynamic();
        ClientRegistry.bindTileEntitySpecialRenderer(TileBitDrawers.class, new TileEntityDrawersRenderer());
        ModelRegistry modelRegistry = Chameleon.instance.modelRegistry;

        modelRegistry.registerModel(new BitDrawerModel.Register());
    }
}
