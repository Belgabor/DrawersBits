package mods.belgabor.bitdrawers;

/**
 * Created by Belgabor on 02.06.2016.
 */
import com.jaquadro.minecraft.storagedrawers.network.BlockClickMessage;
import com.jaquadro.minecraft.storagedrawers.network.CountUpdateMessage;
import mod.chiselsandbits.api.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;
import mods.belgabor.bitdrawers.config.ConfigManager;
import mods.belgabor.bitdrawers.core.BDLogger;
import mods.belgabor.bitdrawers.core.BlockRegistry;
import mods.belgabor.bitdrawers.core.CommonProxy;
import mods.belgabor.bitdrawers.core.RecipeRegistry;
import mods.belgabor.bitdrawers.event.PlayerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = BitDrawers.MODID, version = BitDrawers.VERSION, name = BitDrawers.MODNAME, dependencies = "required-after:chiselsandbits@[11.6,];required-after:StorageDrawers@1.10.2-3.2.2;required-after:Chameleon")
@ChiselsAndBitsAddon
public class BitDrawers implements IChiselsAndBitsAddon
{
    public static final String MODNAME = "Drawers & Bits";
    public static final String MODID = "bitdrawers";
    public static final String VERSION = "0.21";
    
    @SidedProxy(
            clientSide = "mods.belgabor.bitdrawers.client.ClientProxy",
            serverSide = "mods.belgabor.bitdrawers.core.CommonProxy"
    )
    public static CommonProxy proxy;
    public static ConfigManager config;
    
    public static IChiselAndBitsAPI cnb_api;

    public static SimpleNetworkWrapper network;
    
    public static BlockRegistry blocks = new BlockRegistry();
    public static RecipeRegistry recipes = new RecipeRegistry();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        BDLogger.logger = event.getModLog();
        config = new ConfigManager(event.getSuggestedConfigurationFile());
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        blocks.init();
        proxy.initClient();
        network.registerMessage(BlockClickMessage.Handler.class, BlockClickMessage.class, 0, Side.SERVER);
        
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            network.registerMessage(CountUpdateMessage.Handler.class, CountUpdateMessage.class, 1, Side.CLIENT);
        }
        else {
            network.registerMessage(CountUpdateMessage.HandlerStub.class, CountUpdateMessage.class, 1, Side.CLIENT);
        }
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        recipes.init();
    }
    
    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI api) {
        cnb_api = api;
    }
}
