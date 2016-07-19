package mods.belgabor.bitdrawers.block.tile;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.network.CountUpdateMessage;
import com.jaquadro.minecraft.storagedrawers.storage.BaseDrawerData;
import com.jaquadro.minecraft.storagedrawers.storage.ICentralInventory;
import mod.chiselsandbits.api.APIExceptions;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.ItemType;
import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.core.BitHelper;
import mods.belgabor.bitdrawers.storage.BitDrawerData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Belgabor on 02.06.2016.
 * Based on TileEntityDrawersComp by jaquadro
 */
public class TileBitDrawers extends TileEntityDrawers
{
    private static InventoryLookup lookup1 = new InventoryLookup(1, 1);
    private static InventoryLookup lookup2 = new InventoryLookup(2, 2);
    private static InventoryLookup lookup3 = new InventoryLookup(3, 3);

    private ICentralInventory centralInventory;

    private int pooledCount;
    private int lookupSizeResult;

    private ItemStack[] protoStack;
    private int[] convRate;

    public TileBitDrawers () {
        super(3);

        protoStack = new ItemStack[getDrawerCount()];
        convRate = new int[getDrawerCount()];
    }

    protected ICentralInventory getCentralInventory () {
        if (centralInventory == null)
            centralInventory = new BitCentralInventory();
        return centralInventory;
    }

    public int getStoredItemRemainder (int slot) {
        int count = centralInventory.getStoredItemCount(slot);
        if (slot > 0 && convRate[slot] > 0)
            count -= centralInventory.getStoredItemCount(slot - 1) * (convRate[slot - 1] / convRate[slot]);

        return count;
    }

    @Override
    protected IDrawer createDrawer (int slot) {
        System.out.println("createDrawer");
        return new BitDrawerData(getCentralInventory(), slot);
    }

    @Override
    public boolean isDrawerEnabled (int slot) {
        if (slot > 0 && convRate[slot] == 0)
            return false;

        return super.isDrawerEnabled(slot);
    }

    @Override
    public int putItemsIntoSlot (int slot, ItemStack stack, int count) {
        System.out.println(String.format("putItemsIntoSlot %d %s %d", slot, stack==null?"null":stack.getDisplayName(), count));
        int added = 0;
        if (stack != null && convRate != null && convRate[0] == 0) {
            System.out.println("calling populateSlots");
            populateSlots(stack);

            for (int i = 0; i < getDrawerCount(); i++) {
                if (BaseDrawerData.areItemsEqual(protoStack[i], stack))
                    added = super.putItemsIntoSlot(i, stack, count);
            }

            for (int i = 0; i < getDrawerCount(); i++) {
                IDrawer drawer = getDrawer(i);
                if (drawer instanceof BitDrawerData)
                    ((BitDrawerData) drawer).refresh();
            }


        }

        return added + super.putItemsIntoSlot(slot, stack, count);
    }

    @Override
    public void readFromPortableNBT (NBTTagCompound tag) {
        pooledCount = 0;

        for (int i = 0; i < getDrawerCount(); i++) {
            protoStack[i] = null;
            convRate[i] = 0;
        }

        super.readFromPortableNBT(tag);

        pooledCount = tag.getInteger("Count");

        if (tag.hasKey("Conv0"))
            convRate[0] = tag.getInteger("Conv0");
        if (tag.hasKey("Conv1"))
            convRate[1] = tag.getInteger("Conv1");
        if (tag.hasKey("Conv2"))
            convRate[2] = tag.getInteger("Conv2");

        for (int i = 0; i < getDrawerCount(); i++) {
            IDrawer drawer = getDrawer(i);
            if (drawer instanceof BitDrawerData)
                ((BitDrawerData) drawer).refresh();
        }

        if (worldObj != null && !worldObj.isRemote) {
            IBlockState state = worldObj.getBlockState(getPos());
            worldObj.notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public void writeToPortableNBT (NBTTagCompound tag) {
        super.writeToPortableNBT(tag);

        tag.setInteger("Count", pooledCount);

        if (convRate[0] > 0)
            tag.setInteger("Conv0", convRate[0]);
        if (convRate[1] > 0)
            tag.setInteger("Conv1", convRate[1]);
        if (convRate[2] > 0)
            tag.setInteger("Conv2", convRate[2]);
    }

    @Override
    public void clientUpdateCount (int slot, int count) {
        if (count != pooledCount) {
            pooledCount = count;
            IBlockState state = worldObj.getBlockState(getPos());
            worldObj.notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public String getName () {
        return "bitDrawers.container.bitDrawers";
    }

    private void populateSlots (ItemStack stack) {
        System.out.println("populateSlots");
        if (BitDrawers.cnb_api.getItemType(stack) == ItemType.CHISLED_BIT) {
            ItemStack fullStack = BitHelper.getBlock(stack);
            if (fullStack != null) {
                populateSlot(0, fullStack, 4096);
                populateSlot(1, stack, 1);
                System.out.println("Chiseled bit");
            }
        } else {
            ItemStack bitStack = BitHelper.getBit(stack);
            if (bitStack != null) {
                populateSlot(0, stack, 4096);
                populateSlot(1, bitStack, 1);
                System.out.println("Something else");
            }
        }

    }
    
    private void populateSlot (int slot, ItemStack stack, int conversion) {
        convRate[slot] = conversion;
        protoStack[slot] = stack.copy();
        //centralInventory.setStoredItem(slot, stack, 0);
        //getDrawer(slot).setStoredItem(stack, 0);
    }
    
    private class BitCentralInventory implements ICentralInventory
    {
        @Override
        public ItemStack getStoredItemPrototype (int slot) {
            return protoStack[slot];
        }

        @Override
        public void setStoredItem (int slot, ItemStack itemPrototype, int amount) {
            System.out.println(String.format("setStoredItem %d %s %d", slot, itemPrototype==null?"null":itemPrototype.getDisplayName(), amount));
            if (itemPrototype != null && convRate != null && convRate[0] == 0) {
                populateSlots(itemPrototype);
                for (int i = 0; i < getDrawerCount(); i++) {
                    if (BaseDrawerData.areItemsEqual(protoStack[i], itemPrototype))
                        pooledCount = (pooledCount % convRate[i]) + convRate[i] * amount;
                }

                for (int i = 0; i < getDrawerCount(); i++) {
                    if (i == slot)
                        continue;

                    IDrawer drawer = getDrawer(i);
                    if (drawer instanceof BitDrawerData)
                        ((BitDrawerData) drawer).refresh();
                }

                if (worldObj != null && !worldObj.isRemote) {
                    IBlockState state = worldObj.getBlockState(getPos());
                    worldObj.notifyBlockUpdate(getPos(), state, state, 3);
                }
            }
            else if (itemPrototype == null) {
                setStoredItemCount(slot, 0);
            }
        }

        @Override
        public int getStoredItemCount (int slot) {
            //System.out.println(String.format("getStoredItemCount %d", slot));
            if (convRate == null || convRate[slot] == 0)
                return 0;

            if (TileBitDrawers.this.isVending())
                return Integer.MAX_VALUE;

            return pooledCount / convRate[slot];
        }

        @Override
        public void setStoredItemCount (int slot, int amount) {
            System.out.println(String.format("setStoredItemCount %d %d", slot, amount));
            if (convRate == null || convRate[slot] == 0)
                return;

            if (TileBitDrawers.this.isVending())
                return;

            int oldCount = pooledCount;
            pooledCount = (pooledCount % convRate[slot]) + convRate[slot] * amount;

            int poolMax = getMaxCapacity(0) * convRate[0];
            if (pooledCount > poolMax)
                pooledCount = poolMax;

            if (pooledCount != oldCount) {
                if (pooledCount != 0 || TileBitDrawers.this.isLocked(LockAttribute.LOCK_POPULATED))
                    markAmountDirty();
                else {
                    clear();
                    if (worldObj != null && !worldObj.isRemote) {
                        IBlockState state = worldObj.getBlockState(getPos());
                        worldObj.notifyBlockUpdate(getPos(), state, state, 3);
                    }
                }
            }
        }

        @Override
        public int getMaxCapacity (int slot) {
            if (protoStack[slot] == null || convRate == null || convRate[slot] == 0)
                return 0;

            if (TileBitDrawers.this.isUnlimited() || TileBitDrawers.this.isVending()) {
                if (convRate == null || protoStack[slot] == null || convRate[slot] == 0)
                    return Integer.MAX_VALUE;
                return Integer.MAX_VALUE / convRate[slot];
            }

            return protoStack[slot].getItem().getItemStackLimit(protoStack[slot]) * getStackCapacity(slot);
        }

        @Override
        public int getMaxCapacity (int slot, ItemStack itemPrototype) {
            if (itemPrototype == null || itemPrototype.getItem() == null)
                return 0;

            if (TileBitDrawers.this.isUnlimited() || TileBitDrawers.this.isVending()) {
                if (convRate == null || protoStack[slot] == null || convRate[slot] == 0)
                    return Integer.MAX_VALUE;
                return Integer.MAX_VALUE / convRate[slot];
            }

            if (convRate == null || protoStack[0] == null || convRate[0] == 0)
                return itemPrototype.getItem().getItemStackLimit(itemPrototype) * getBaseStackCapacity();

            if (BaseDrawerData.areItemsEqual(protoStack[slot], itemPrototype))
                return getMaxCapacity(slot);

            return 0;
        }

        @Override
        public int getRemainingCapacity (int slot) {
            if (TileBitDrawers.this.isVending())
                return Integer.MAX_VALUE;

            return getMaxCapacity(slot) - getStoredItemCount(slot);
        }

        @Override
        public int getStoredItemStackSize (int slot) {
            if (protoStack[slot] == null || convRate == null || convRate[slot] == 0)
                return 0;

            return protoStack[slot].getItem().getItemStackLimit(protoStack[slot]);
        }

        @Override
        public int getItemCapacityForInventoryStack (int slot) {
            if (isVoid())
                return Integer.MAX_VALUE;
            else
                return getMaxCapacity(slot);
        }

        @Override
        public int getConversionRate (int slot) {
            if (protoStack[slot] == null || convRate == null || convRate[slot] == 0)
                return 0;

            return convRate[0] / convRate[slot];
        }

        @Override
        public int getStoredItemRemainder (int slot) {
            return TileBitDrawers.this.getStoredItemRemainder(slot);
        }

        @Override
        public boolean isSmallestUnit (int slot) {
            if (protoStack[slot] == null || convRate == null || convRate[slot] == 0)
                return false;

            return convRate[slot] == 1;
        }

        @Override
        public boolean isVoidSlot (int slot) {
            return isVoid();
        }

        @Override
        public boolean isShroudedSlot (int slot) {
            return isShrouded();
        }

        @Override
        public boolean setIsSlotShrouded (int slot, boolean state) {
            setIsShrouded(state);
            return true;
        }

        @Override
        public boolean isLocked (int slot, LockAttribute attr) {
            return TileBitDrawers.this.isLocked(attr);
        }

        @Override
        public void writeToNBT (int slot, NBTTagCompound tag) {
            ItemStack protoStack = getStoredItemPrototype(slot);
            if (protoStack != null && protoStack.getItem() != null) {
                tag.setShort("Item", (short) Item.getIdFromItem(protoStack.getItem()));
                tag.setShort("Meta", (short) protoStack.getItemDamage());
                tag.setInteger("Count", 0); // TODO: Remove when ready to break 1.1.7 compat

                if (protoStack.getTagCompound() != null)
                    tag.setTag("Tags", protoStack.getTagCompound());
            }
        }

        @Override
        public void readFromNBT (int slot, NBTTagCompound tag) {
            if (tag.hasKey("Item")) {
                Item item = Item.getItemById(tag.getShort("Item"));
                if (item != null) {
                    ItemStack stack = new ItemStack(item);
                    stack.setItemDamage(tag.getShort("Meta"));
                    if (tag.hasKey("Tags"))
                        stack.setTagCompound(tag.getCompoundTag("Tags"));

                    protoStack[slot] = stack;
                }
            }
        }

        private void clear () {
            for (int i = 0; i < getDrawerCount(); i++) {
                protoStack[i] = null;
                convRate[i] = 0;
            }

            refresh();
            TileBitDrawers.this.markDirty();
        }

        public void refresh () {
            for (int i = 0; i < getDrawerCount(); i++) {
                IDrawer drawer = getDrawer(i);
                if (drawer instanceof BitDrawerData)
                    ((BitDrawerData) drawer).refresh();
            }
        }

        private int getStackCapacity (int slot) {
            if (convRate == null || convRate[slot] == 0)
                return 0;

            int slotStacks = getBaseStackCapacity();

            int stackLimit = convRate[0] * slotStacks;
            return stackLimit / convRate[slot];
        }

        private int getBaseStackCapacity () {
            return TileBitDrawers.this.getEffectiveStorageMultiplier() * TileBitDrawers.this.getDrawerCapacity();
        }

        public void markAmountDirty () {
            if (getWorld().isRemote)
                return;

            IMessage message = new CountUpdateMessage(getPos(), 0, pooledCount);
            NetworkRegistry.TargetPoint targetPoint = new NetworkRegistry.TargetPoint(getWorld().provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 500);

            BitDrawers.network.sendToAllAround(message, targetPoint);
        }

        public void markDirty (int slot) {
            if (getWorld().isRemote)
                return;

            IBlockState state = worldObj.getBlockState(getPos());
            worldObj.notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    private static class InventoryLookup extends InventoryCrafting
    {
        private ItemStack[] stackList;

        public InventoryLookup (int width, int height) {
            super(null, width, height);
            stackList = new ItemStack[width * height];
        }

        @Override
        public int getSizeInventory ()
        {
            return this.stackList.length;
        }

        @Override
        public ItemStack getStackInSlot (int slot)
        {
            return slot >= this.getSizeInventory() ? null : this.stackList[slot];
        }

        @Override
        public ItemStack removeStackFromSlot (int slot) {
            return null;
        }

        @Override
        public ItemStack decrStackSize (int slot, int count) {
            return null;
        }

        @Override
        public void setInventorySlotContents (int slot, ItemStack stack) {
            stackList[slot] = stack;
        }
    }
}
