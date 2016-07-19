package mods.belgabor.bitdrawers.core;

import mod.chiselsandbits.api.APIExceptions;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.ItemType;
import mods.belgabor.bitdrawers.BitDrawers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Created by Belgabor on 19.07.2016.
 */
public class BitHelper {
    public static ItemStack getBit(ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block != null) {
                IBlockState blockState = block.getStateFromMeta(stack.getItemDamage());
                if (blockState != null) {
                    try {
                        ItemStack bitStack = BitDrawers.cnb_api.getBitItem(blockState);
                        if (bitStack.getItem() != null) {
                            return bitStack;
                        }
                    } catch (APIExceptions.InvalidBitItem e) {}
                }
            }
        }
        return null;
    }

    public static ItemStack getBlock(ItemStack stack) {
        if (BitDrawers.cnb_api.getItemType(stack) == ItemType.CHISLED_BIT) {
            IBitBrush bitBrush = null;
            try {
                bitBrush = BitDrawers.cnb_api.createBrush(stack);
            } catch (APIExceptions.InvalidBitItem e) {}
            if (bitBrush != null) {
                IBlockState blockState = bitBrush.getState();
                if (blockState != null) {
                    Block block = blockState.getBlock();
                    ItemStack newStack =  new ItemStack(block, 1, block.damageDropped(blockState));
                    if (newStack.getItem() != null)
                        return newStack;
                }
            }
        }
        return null;
    }
}
