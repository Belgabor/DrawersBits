package mods.belgabor.bitdrawers.core;

import mod.chiselsandbits.api.*;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.helpers.ModUtil;
import mods.belgabor.bitdrawers.BitDrawers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Belgabor on 19.07.2016.
 */
public class BitHelper {
    public static boolean areItemsEqual(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }
    
    public static @Nonnull ItemStack getBit(ItemStack stack) {
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
        return ItemStack.EMPTY;
    }

    public static @Nonnull ItemStack getBlock(ItemStack stack) {
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
        return ItemStack.EMPTY;
    }
    
    public static @Nonnull ItemStack getMonochrome(ItemStack source, IBitBrush brush) {
        boolean set = BitDrawers.cnb_api.getItemType(source) == ItemType.NEGATIVE_DESIGN;
        BitHelper.BitCopy visitor;
        try {
            visitor = new BitHelper.BitCopy(BitDrawers.cnb_api.createBitItem(source), brush, set);
        } catch (APIExceptions.InvalidBitItem e) {
            return ItemStack.EMPTY;
        }
        //item = new ItemStack(ChiselsAndBits.getBlocks().getConversion(material.getDefaultState()), 1);
        IBitAccess resultAccessor = BitDrawers.cnb_api.createBitItem(ItemStack.EMPTY);
        resultAccessor.visitBits(visitor);
        if (visitor.count == 0)
            return ItemStack.EMPTY;
        ItemStack item = resultAccessor.getBitsAsItem(ModUtil.getSide(source), ItemType.CHISLED_BLOCK, false);
        item.setCount(visitor.count);
        return item;
    }

    public static class BitCopy implements IBitVisitor {
        private final IBitAccess source;
        private final IBitBrush bit;
        private final IBitBrush air;
        private final boolean negative;
        public int count = 0;
        
        public BitCopy(IBitAccess source, IBitBrush bit, boolean negative) throws APIExceptions.InvalidBitItem {
            if (source == null)
                throw new APIExceptions.InvalidBitItem();
            this.source = source;
            this.bit = bit;
            this.air = BitDrawers.cnb_api.createBrush(ItemStack.EMPTY);
            this.negative = negative;
        }

        @Override
        public IBitBrush visitBit(int x, int y, int z, @Nonnull IBitBrush dummy) {
            IBitBrush sourceBit = source.getBitAt(x, y, z);
            if (sourceBit.isAir() == negative) {
                count++;
                return bit;
            } else
                return air;
        }
    }
    
    public static class BitCounter implements IBitVisitor {
        public Map<Integer, Integer> counts = new HashMap<Integer, Integer>();

        @Override
        public IBitBrush visitBit(int x, int y, int z, @Nonnull IBitBrush brush) {
            if (!brush.isAir()) {
                if (!counts.containsKey(brush.getStateID()))
                    counts.put(brush.getStateID(), 0);
                counts.put(brush.getStateID(), counts.get(brush.getStateID()) + 1);
            }
            return brush;
        }
    }
}
