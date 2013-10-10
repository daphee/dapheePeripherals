package com.daphee.dapheePeripherals.shared;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import dan200.computer.api.IHostedPeripheral;
import dan200.computer.api.IPeripheralHandler;

public class CommandBlockPeripheralHandler implements IPeripheralHandler{

    @Override
    public IHostedPeripheral getPeripheral(TileEntity tile) {
        return new CommandBlockPeripheral((TileEntityCommandBlock)tile);
    }

}
