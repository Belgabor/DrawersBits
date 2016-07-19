package mods.belgabor.bitdrawers.core;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Created by Belgabor on 19.07.2016.
 */
public class RecipeRegistry {
    public void init() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockRegistry.bitDrawer, 1), "xxx", "zwz", "xyx",
                'x', new ItemStack(Blocks.STONE), 'y', new ItemStack(ChiselsAndBits.getItems().itemChiselIron), 'z', new ItemStack(Blocks.PISTON), 'w', "drawerBasic"));
    }
}
