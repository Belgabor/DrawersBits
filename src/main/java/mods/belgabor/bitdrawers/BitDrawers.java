package mods.belgabor.bitdrawers;

/**
 * Created by Belgabor on 02.06.2016.
 */
import com.jaquadro.minecraft.storagedrawers.network.CountUpdateMessage;
import mod.chiselsandbits.api.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;
import mods.belgabor.bitdrawers.core.BlockRegistry;
import mods.belgabor.bitdrawers.core.CommonProxy;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = BitDrawers.MODID, version = BitDrawers.VERSION, dependencies = "required-after:chiselsandbits;required-after:StorageDrawers;required-after:Chameleon")
@ChiselsAndBitsAddon
public class BitDrawers implements IChiselsAndBitsAddon
{
    public static final String MODID = "bitdrawers";
    public static final String VERSION = "0.1";
    
    @SidedProxy(
            clientSide = "mods.belgabor.bitdrawers.client.ClientProxy",
            serverSide = "mods.belgabor.bitdrawers.core.CommonProxy"
    )
    public static CommonProxy proxy;
    
    public static IChiselAndBitsAPI cnb_api;

    public static SimpleNetworkWrapper network;
    
    public static BlockRegistry blocks = new BlockRegistry();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        blocks.init();
        proxy.initClient();
        
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            network.registerMessage(CountUpdateMessage.Handler.class, CountUpdateMessage.class, 1, Side.CLIENT);
        }
        else {
            network.registerMessage(CountUpdateMessage.HandlerStub.class, CountUpdateMessage.class, 1, Side.CLIENT);
        }
    }
    
    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI api) {
        cnb_api = api;
    }
}
