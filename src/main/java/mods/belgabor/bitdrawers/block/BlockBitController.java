package mods.belgabor.bitdrawers.block;

import com.jaquadro.minecraft.storagedrawers.block.BlockController;
import mods.belgabor.bitdrawers.block.tile.TileBitController;
import net.minecraft.world.World;

/**
 * Created by Belgabor on 24.07.2016.
 */
public class BlockBitController extends BlockController {
    public BlockBitController(String name) {
        super(name);
    }
    
    @Override
    public TileBitController createNewTileEntity (World world, int meta) {
        return new TileBitController();
    }
}
