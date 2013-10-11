package com.daphee.dapheePeripherals.peripherals;

import com.daphee.dapheePeripherals.peripherals.command.CommandBlockPeripheral;

import net.minecraft.nbt.NBTTagCompound;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public abstract class FunctionAdapter{
    private CommandBlockPeripheral peripheral;
    public FunctionAdapter(CommandBlockPeripheral peripheral){
        this.peripheral = peripheral;
    }
    
    public abstract void update();

    public abstract void readFromNBT(NBTTagCompound nbttagcompound);
    
    public abstract void writeToNBT(NBTTagCompound nbttagcompound);
    
    public abstract boolean canAttachToSide(int side);
    
    public abstract void attach(IComputerAccess computer);

    public abstract void detach(IComputerAccess computer);
    
    public abstract Object[] processArgs(MethodDeclaration method, IComputerAccess computer,ILuaContext context, String methodName, Object[] args);

}
