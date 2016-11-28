package mods.belgabor.bitdrawers.core;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import mod.chiselsandbits.core.ChiselsAndBits;
import mods.belgabor.bitdrawers.BitDrawers;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Created by Belgabor on 19.07.2016.
 */
public class RecipeRegistry {
    public void init() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockRegistry.bitDrawer, 1), "xxx", "zwz", "xyx",
                'x', new ItemStack(Blocks.STONE), 'y', new ItemStack(ChiselsAndBits.getItems().itemChiselIron), 'z', new ItemStack(Blocks.PISTON), 'w', "drawerBasic"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(BlockRegistry.bitDrawer, 1), new ItemStack(StorageDrawers.blocks.compDrawers, 1), new ItemStack(ChiselsAndBits.getItems().itemBlockBit, 1, OreDictionary.WILDCARD_VALUE)));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(StorageDrawers.blocks.compDrawers, 1), new ItemStack(BlockRegistry.bitDrawer, 1)));
        
        if (BitDrawers.config.enableBitController) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockRegistry.bitController, 1), "xxx", "zwz", "xyx",
                    'x', new ItemStack(Blocks.STONE), 'y', new ItemStack(ChiselsAndBits.getItems().itemChiselDiamond), 'z', new ItemStack(Items.COMPARATOR), 'w', "drawerBasic"));
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(BlockRegistry.bitController, 1), new ItemStack(StorageDrawers.blocks.controller, 1), new ItemStack(ChiselsAndBits.getItems().itemBlockBit, 1, OreDictionary.WILDCARD_VALUE)));
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(StorageDrawers.blocks.controller, 1), new ItemStack(BlockRegistry.bitController, 1)));
        }
    }
}
