# Drawers &amp; Bits
Drawers &amp; Bits is an addon for the Minecraft mods [Chisels &amp; Bits](http://mods.curse.com/mc-mods/minecraft/231095-chisels-bits) by AlgorithmX2 and [Storage Drawers](http://mods.curse.com/mc-mods/minecraft/223852-storage-drawers) by jaquadro. The mod adds special drawers and support for bits.

This mod highly relies on the version of storage drawers, so it can break with an update. I will try to fix that as soon as I notice.

## Bit Drawer
Currently the mod adds one block, the bit drawer. It basically works similar to a compacting drawer by automatically converting between bits and the full block. It can only store blocks that can be broken down into bits. Otherwise it acts as any drawer, aka it can be part of a drawer controller network, accepts upgrades and can be locked etc.

A small warning, since a block has 4096 bits the drawer gets close to how much can be reasonably stored internally in a number. With default settings you are fine, but if you upgrade the storage capacity of the bit drawer or upgrades in the config, you will rapidly hit the limit (at 8191 stacks à 64 blocks).

### Special Interactions
For now all of these only work when manually interacting with the bit drawer. Sneak/non-sneak left-click behaviour will be inverted if the respective option is set for Storage Drawers.

**Bags:** _Right-clicking_ with a bag will deposit everything in the bag matching the clicked slot into the drawer. This works with the bit bag but can also work with other mod's bags if the support the correct Forge capability. **Bit bags** do not need to target the bit slot, right-clicking to deposit bits works on the whole front. _Left-clicking_ with a **bit bag** on the front will fill a slot in the bag with bits from the drawer (fill up a partial one or a new one if there is no partially filled slot), sneaking while doing so fills the bag with as many bits as are available/will fit. 
 
**Designs:** _Left-clicking_ the _bit slot_ with a design gives you the block described by the design, sneaking gives you a stack. The bit composition is ignored, it will be made entirely from the bit in the drawer. _Right-clicking_ the _custom slot_ (bottom right) with a design will set the chiseled block you get from the custom slot. Use an empty design to clear.
 
**Chiseled blocks:** _Left-clicking_ the _bit slot_ works like left-clicking with a design. _Right-clicking_ the drawer front with a chiseled block will insert the stack into the drawer if it's made entirely from the stored bit (and there is enough space). This only works if the drawer contents are set.


