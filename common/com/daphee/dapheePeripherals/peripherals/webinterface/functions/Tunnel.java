package com.daphee.dapheePeripherals.peripherals.webinterface.functions;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.nbt.NBTTagCompound;

import com.daphee.dapheePeripherals.peripherals.FunctionAdapter;
import com.daphee.dapheePeripherals.peripherals.LuaType;
import com.daphee.dapheePeripherals.peripherals.Arg;
import com.daphee.dapheePeripherals.peripherals.LuaMethod;
import com.daphee.dapheePeripherals.peripherals.MethodDeclaration;
import com.daphee.dapheePeripherals.peripherals.command.CommandBlockPeripheral;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class Tunnel extends FunctionAdapter {
    private HashMap<Integer,Request> requests = new HashMap<Integer,Request>();
    private final AtomicInteger request_counter = new AtomicInteger();
    
    public Tunnel(CommandBlockPeripheral peripheral) {
        super(peripheral);
    }
    
    @LuaMethod(args={@Arg(name="id",type=LuaType.NUMBER),@Arg(name="response",type=LuaType.STRING)})
    public Object[] response(Double Did,String response) throws Exception{
        int id = Did.intValue();
        if(!requests.containsKey(id))
           throw new Exception("This request doesn't exist");
        
        synchronized (requests.get(id)) {
            requests.get(id).setResponse(response);
            requests.get(id).notifyAll();
        }
        return null;
    }
    
    public String request(IComputerAccess computer,Request request){
        int id = this.request_counter.incrementAndGet();
        requests.put(id, request);
        computer.queueEvent("request", new Object[]{id,request.getPacket()});
        synchronized(request){
            try {
                request.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
        if(request.hasResponse())
            return request.getResponse();
        return null;
    }

    @Override
    public void update() {

    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canAttachToSide(int side) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void attach(IComputerAccess computer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void detach(IComputerAccess computer) {

    }

    @Override
    public Object[] processArgs(MethodDeclaration method,
            IComputerAccess computer, ILuaContext context, String methodName,
            Object[] args) {
        return args;
    }

}
