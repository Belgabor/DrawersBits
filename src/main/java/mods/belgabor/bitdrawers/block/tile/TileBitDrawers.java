package mods.belgabor.bitdrawers.block.tile;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.inventory.ContainerDrawersComp;
import com.jaquadro.minecraft.storagedrawers.network.CountUpdateMessage;
import com.jaquadro.minecraft.storagedrawers.storage.BaseDrawerData;
import com.jaquadro.minecraft.storagedrawers.storage.ICentralInventory;
import mod.chiselsandbits.api.*;
import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.core.BDLogger;
import mods.belgabor.bitdrawers.core.BitHelper;
import mods.belgabor.bitdrawers.storage.BitDrawerData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

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
        for (int i = 0; i < protoStack.length; i++)
            protoStack[i] = ItemStack.EMPTY;
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
        return new BitDrawerData(getCentralInventory(), slot);
    }
    
    @Override
    public Container createContainer (InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerDrawersComp(playerInventory, this);
    }

    @Override
    public String getGuiID () {
        return StorageDrawers.MOD_ID + ":compDrawers";
    }


    @Override
    public boolean isDrawerEnabled (int slot) {
        if (slot > 0 && convRate[slot] == 0)
            return false;

        return super.isDrawerEnabled(slot);
    }
    
    @Override
    public int interactPutItemsIntoSlot (int slot, EntityPlayer player) {
        if (BitDrawers.config.debugTrace)
            BDLogger.info("TileBitDrawers:interactPutItemsIntoSlot %d", slot);
        ItemStack stack = player.inventory.getCurrentItem();
        if (!stack.isEmpty()) {
            if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                return interactPutBagIntoSlot(slot, stack);
            } else if (slot == 2){
                ItemType type = BitDrawers.cnb_api.getItemType(stack);
                if (type == ItemType.POSITIVE_DESIGN || type == ItemType.NEGATIVE_DESIGN || type == ItemType.MIRROR_DESIGN) {
                    return interactSetCustomSlot(stack);
                }
            }
        }
        return super.interactPutItemsIntoSlot(slot, player);
    }
    
    public int interactPutBagIntoSlot(int slot, ItemStack stack) {
        if (BitDrawers.config.debugTrace)
            BDLogger.info("TileBitDrawers:interactPutBagIntoSlot %d %s", slot, stack==null?"null":stack.getDisplayName());
        int added = 0;
        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler instanceof IBitBag) {
            slot = 1;
            if (BitDrawers.config.debugTrace)
                BDLogger.info("TileBitDrawers:interactPutBagIntoSlot Bit Bag detected");
        }
        for(int i = 0; i < handler.getSlots(); i++) {
            while (true) {
                ItemStack extract = handler.extractItem(i, 64, true);
                if (extract.isEmpty())
                    break;
                int extracted = extract.getCount();
                int inserted = putItemsIntoSlot(slot, extract, extracted);
                if (inserted > 0) {
                    added += inserted;
                    ItemStack test = handler.extractItem(i, inserted, false);
                    if (test.getCount() < inserted)
                        BDLogger.error("Could not extract simulated amount from bag. Something went very wrong.");
                }
                if (inserted < extracted)
                    break;
            }
        }
        return added;
    }
    
    public int interactSetCustomSlot(@Nonnull ItemStack stack) {
        ItemStack bit = getDrawer(1).getStoredItemPrototype();
        if (bit.isEmpty())
            return 0;

        IBitBrush brush;
        try {
            brush = BitDrawers.cnb_api.createBrush(bit);
        } catch (APIExceptions.InvalidBitItem e) {
            return 0;
        }
        ItemStack item = BitHelper.getMonochrome(stack, brush);
        if (item.isEmpty())
            populateSlot(2, ItemStack.EMPTY, 0);
        else
            populateSlot(2, item, item.getCount());
        
        return 1;
    }
    
    @Override
    public int putItemsIntoSlot (int slot, ItemStack stack, int count) {
        if (BitDrawers.config.debugTrace)
            BDLogger.info("TileBitDrawers:putItemsIntoSlot %d %s %d", slot, stack==null?"null":stack.getDisplayName(), count);
        int added = 0;
        if (!stack.isEmpty()) {
            if (BitDrawers.cnb_api.getItemType(stack) == ItemType.CHISLED_BLOCK) {
                return putChiseledBlockIntoDrawer(stack, count);
            } else if (convRate != null && convRate[0] == 0) {
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
        }

        return added + super.putItemsIntoSlot(slot, stack, count);
    }
    
    public int putChiseledBlockIntoDrawer (ItemStack stack, int count) {
        if (BitDrawers.config.debugTrace)
            BDLogger.info("TileBitDrawers:putChiseledBlockIntoDrawer %s %d", stack==null?"null":stack.getDisplayName(), count);
        count = Math.min(count, stack.getCount());
        IDrawer drawer = getDrawer(1);
        IBitAccess access = BitDrawers.cnb_api.createBitItem(stack);
        if (convRate == null || convRate[0] == 0 || access == null)
            return 0;
        BitHelper.BitCounter counter = new BitHelper.BitCounter();
        access.visitBits(counter);
        IBitBrush stored = null;
        try {
            stored = BitDrawers.cnb_api.createBrush(drawer.getStoredItemPrototype());
        } catch (APIExceptions.InvalidBitItem invalidBitItem) {
            BDLogger.error("Failed to create bit brush for stored bit");
            BDLogger.error(invalidBitItem);
            return 0;
        }
        if (counter.counts.size() != 1 || !counter.counts.containsKey(stored.getStateID()) || counter.counts.get(stored.getStateID()) == 0) {
            if (BitDrawers.config.debugTrace)
                BDLogger.info("TileBitDrawers:putChiseledBlockIntoDrawer Not Matched %d", counter.counts.size());
            return 0;
        }
        
        int bitSize = counter.counts.get(stored.getStateID());
        int canStore = isVoid()?count:drawer.getRemainingCapacity() / bitSize;
        int toStore = Math.min(canStore, count);
        int toStoreBits = toStore * bitSize;
        ItemStack store = drawer.getStoredItemPrototype().copy();
        store.setCount(toStoreBits);
        int storedBits = super.putItemsIntoSlot(1, store, toStoreBits);
        if (storedBits != toStoreBits) {
            BDLogger.error("Couldn't store bits when inserting chiseled block. This is not supposed to happen at this point.");
            toStore = storedBits / bitSize;
        }
        stack.shrink(toStore);
        return toStore;
    }

    @Override
    public void readFromPortableNBT (NBTTagCompound tag) {
        pooledCount = 0;

        for (int i = 0; i < getDrawerCount(); i++) {
            protoStack[i] = ItemStack.EMPTY;
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

        if (getWorld() != null && !getWorld().isRemote) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public NBTTagCompound writeToPortableNBT (NBTTagCompound tag) {
        super.writeToPortableNBT(tag);

        tag.setInteger("Count", pooledCount);

        if (convRate[0] > 0)
            tag.setInteger("Conv0", convRate[0]);
        if (convRate[1] > 0)
            tag.setInteger("Conv1", convRate[1]);
        if (convRate[2] > 0)
            tag.setInteger("Conv2", convRate[2]);
        
        return tag;
    }

    @Override
    public void clientUpdateCount (int slot, int count) {
        if (count != pooledCount) {
            pooledCount = count;
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public String getName () {
        return "bitDrawers.container.bitDrawers";
    }

    private void populateSlots (ItemStack stack) {
        if (BitDrawers.cnb_api.getItemType(stack) == ItemType.CHISLED_BIT) {
            ItemStack fullStack = BitHelper.getBlock(stack);
            if (!fullStack.isEmpty()) {
                populateSlot(0, fullStack, 4096);
                populateSlot(1, stack, 1);
            }
        } else {
            ItemStack bitStack = BitHelper.getBit(stack);
            if (!bitStack.isEmpty()) {
                populateSlot(0, stack, 4096);
                populateSlot(1, bitStack, 1);
            }
        }

    }
    
    private void populateSlot (int slot, ItemStack stack, int conversion) {
        convRate[slot] = conversion;
        protoStack[slot] = stack==null?null:stack.copy();
        //centralInventory.setStoredItem(slot, stack, 0);
        //getDrawer(slot).setStoredItem(stack, 0);
        if (getWorld() != null && !getWorld().isRemote) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }
    
    private class BitCentralInventory implements ICentralInventory
    {
        @Override
        public ItemStack getStoredItemPrototype (int slot) {
            return protoStack[slot];
        }

        @Override
        public int getDefaultMaxCapacity (int slot) {
            if (!isDrawerEnabled(slot))
                return 0;

            if (TileBitDrawers.this.isUnlimited() || TileBitDrawers.this.isVending())
                return Integer.MAX_VALUE;

            return 64 * getBaseStackCapacity();
        }
        
        @Override
        public IDrawer setStoredItem (int slot, ItemStack itemPrototype, int amount) {
            if (BitDrawers.config.debugTrace)
                BDLogger.info("setStoredItem %d %s %d", slot, itemPrototype==null?"null":itemPrototype.getDisplayName(), amount);
            if ((!itemPrototype.isEmpty()) && convRate != null && convRate[0] == 0) {
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

                if (getWorld() != null && !getWorld().isRemote) {
                    IBlockState state = getWorld().getBlockState(getPos());
                    getWorld().notifyBlockUpdate(getPos(), state, state, 3);
                }
            }
            else if (itemPrototype.isEmpty()) {
                setStoredItemCount(slot, 0);
            }
            return getDrawer(slot);
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
            if (BitDrawers.config.debugTrace)
                BDLogger.info("BitCentralInventory:setStoredItemCount %d %d", slot, amount);
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
                if (pooledCount != 0 || TileBitDrawers.this.isItemLocked(LockAttribute.LOCK_POPULATED))
                    markAmountDirty();
                else {
                    clear();
                    if (getWorld() != null && !getWorld().isRemote) {
                        IBlockState state = getWorld().getBlockState(getPos());
                        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
                    }
                }
            }
        }

        @Override
        public int getMaxCapacity (int slot) {
            if (protoStack[slot].isEmpty() || convRate == null || convRate[slot] == 0)
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
            if (itemPrototype.isEmpty() || itemPrototype.getItem() == null)
                return 0;

            if (TileBitDrawers.this.isUnlimited() || TileBitDrawers.this.isVending()) {
                if (convRate == null || protoStack[slot].isEmpty() || convRate[slot] == 0)
                    return Integer.MAX_VALUE;
                return Integer.MAX_VALUE / convRate[slot];
            }

            if (convRate == null || protoStack[0].isEmpty() || convRate[0] == 0)
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
            if (protoStack[slot].isEmpty() || convRate == null || convRate[slot] == 0)
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
            if (protoStack[slot].isEmpty() || convRate == null || convRate[slot] == 0)
                return 0;

            return convRate[0] / convRate[slot];
        }

        @Override
        public int getStoredItemRemainder (int slot) {
            return TileBitDrawers.this.getStoredItemRemainder(slot);
        }

        @Override
        public boolean isSmallestUnit (int slot) {
            if (protoStack[slot].isEmpty() || convRate == null || convRate[slot] == 0)
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
        public boolean isSlotShowingQuantity(int i) {
            return TileBitDrawers.this.isShowingQuantity();
        }
        
        @Override
        public boolean setIsSlotShowingQuantity(int slot, boolean state) {
            return TileBitDrawers.this.setIsShowingQuantity(state);
        }

        @Override
        public boolean isLocked (int slot, LockAttribute attr) {
            return TileBitDrawers.this.isItemLocked(attr);
        }

        @Override
        public void writeToNBT (int slot, NBTTagCompound tag) {
            ItemStack protoStack = getStoredItemPrototype(slot);
            if ((!protoStack.isEmpty()) && protoStack.getItem() != null) {
                tag.setShort("Item", (short) Item.getIdFromItem(protoStack.getItem()));
                tag.setShort("Meta", (short) protoStack.getItemDamage());

                if (protoStack.getTagCompound() != null)
                    tag.setTag("Tags", protoStack.getTagCompound());
            }
        }

        @Override
        public void readFromNBT (int slot, NBTTagCompound tag) {
            if (tag.hasKey("Item")) {
                Item item = Item.getItemById(tag.getShort("Item"));
                if (item != null) {
                    ItemStack stack = new ItemStack(item, 1);
                    stack.setItemDamage(tag.getShort("Meta"));
                    if (tag.hasKey("Tags"))
                        stack.setTagCompound(tag.getCompoundTag("Tags"));

                    protoStack[slot] = stack;
                }
            }
        }

        private void clear () {
            for (int i = 0; i < getDrawerCount(); i++) {
                protoStack[i] = ItemStack.EMPTY;
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

            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
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
