package daphee.dapheePeripherals.block;

import daphee.dapheePeripherals.peripherals.FunctionManager;
import daphee.dapheePeripherals.peripherals.functions.CommandFunc;
import daphee.dapheePeripherals.peripherals.functions.HelperFunc;
import daphee.dapheePeripherals.peripherals.functions.SocketFunc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAdvancedCommandBlock extends BlockCommandBlock {
    
    public BlockAdvancedCommandBlock(int id) {
        super(id);
    }
    
    @Override
    public TileEntity createNewTileEntity(World par1World) {
        return new TileEntityAdvancedCommandBlock();
    }
}
