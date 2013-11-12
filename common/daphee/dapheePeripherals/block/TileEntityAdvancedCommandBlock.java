package daphee.dapheePeripherals.block;

import dan200.computer.api.ComputerCraftAPI;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import daphee.DapheePeripherals;
import daphee.dapheePeripherals.peripherals.FunctionManager;
import daphee.dapheePeripherals.peripherals.functions.CommandFunc;
import daphee.dapheePeripherals.peripherals.functions.HelperFunc;
import daphee.dapheePeripherals.peripherals.functions.SocketFunc;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityCommandBlock;

public class TileEntityAdvancedCommandBlock extends TileEntityCommandBlock implements IPeripheral{
    private FunctionManager manager;
    private String[] methodNames;
    
    public TileEntityAdvancedCommandBlock() {
        super();
        manager = new FunctionManager();
        manager.addAdapter(new SocketFunc());
        manager.addAdapter(new CommandFunc(this));
        manager.addAdapter(new HelperFunc());
        methodNames = manager.getMethodNames();
    }
    
    @Override
    public String getType() {
        return "command";
    }

    @Override
    public String[] getMethodNames() {
        return this.methodNames;
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context,
            int method, Object[] arguments) throws Exception {
        return manager.callMethod(computer,context,this.methodNames[method],arguments);
    }

    @Override
    public boolean canAttachToSide(int side) {
        return manager.canAttachToSide(side);
    }

    @Override
    public void attach(IComputerAccess computer) {
        manager.attach(computer);
        computer.mount("daphee", ComputerCraftAPI.createResourceMount(DapheePeripherals.class, "dapheeperipherals", "lua"));
    }

    @Override
    public void detach(IComputerAccess computer) {
        manager.detach(computer);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        manager.readFromNBT(nbttagcompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        manager.writeToNBT(nbttagcompound);
    }
    
    @Override
    public void updateEntity() {
        super.updateEntity();
        manager.update();
    }
    
    
}
