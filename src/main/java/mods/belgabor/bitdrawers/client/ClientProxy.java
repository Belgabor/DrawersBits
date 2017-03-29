package mods.belgabor.bitdrawers.client;

import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.core.CommonProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Belgabor on 18.07.2016.
 */

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void initClient() {
        BitDrawers.blocks.initClient();
    }
}
