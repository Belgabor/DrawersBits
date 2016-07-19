package mods.belgabor.bitdrawers.client;

import mods.belgabor.bitdrawers.BitDrawers;
import mods.belgabor.bitdrawers.core.CommonProxy;

/**
 * Created by Belgabor on 18.07.2016.
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void initClient() {
        BitDrawers.blocks.initClient();
    }
}
