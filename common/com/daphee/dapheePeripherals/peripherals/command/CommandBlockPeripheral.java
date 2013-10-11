package com.daphee.dapheePeripherals.peripherals.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import com.daphee.DapheePeripherals;
import com.daphee.dapheePeripherals.peripherals.FunctionManager;
import com.daphee.dapheePeripherals.peripherals.command.functions.CommandFunc;
import com.daphee.dapheePeripherals.peripherals.command.functions.SocketFunc;
import com.daphee.dapheePeripherals.peripherals.webinterface.functions.Tunnel;

import cpw.mods.fml.common.modloader.ModLoaderHelper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import dan200.computer.api.ComputerCraftAPI;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IHostedPeripheral;
import dan200.computer.api.ILuaContext;

public class CommandBlockPeripheral implements IHostedPeripheral{
    private TileEntityCommandBlock commandTile;
  
    private FunctionManager manager;
    private String[] methodNames;
    
    public CommandBlockPeripheral(TileEntityCommandBlock commandTile){
        this.commandTile = commandTile;
        
        manager = new FunctionManager();
        manager.addAdapter(new SocketFunc(this));
        manager.addAdapter(new CommandFunc(this,commandTile));
        manager.addAdapter(new Tunnel(this));
        
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
        //computer.mount("daphee", ComputerCraftAPI.createResourceMount(DapheePeripherals.class, "dapheePeripherals", "lua"));
    }

    @Override
    public void detach(IComputerAccess computer) {
        manager.detach(computer);
    }

    @Override
    public void update() {
        manager.update();
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        manager.readFromNBT(nbttagcompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        manager.writeToNBT(nbttagcompound);
    }
    
    
}
