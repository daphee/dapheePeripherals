package daphee.dapheePeripherals.peripherals;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public abstract class FunctionAdapter{
    public FunctionAdapter(){
    }
    public abstract void update();

    public abstract void readFromNBT(NBTTagCompound nbttagcompound);
    
    public abstract void writeToNBT(NBTTagCompound nbttagcompound);
    
    public abstract boolean canAttachToSide(int side);
    
    public abstract void attach(IComputerAccess computer);

    public abstract void detach(IComputerAccess computer);
    
    public abstract Object[] processArgs(MethodDeclaration method, IComputerAccess computer,ILuaContext context, String methodName, Object[] args);

}
