package com.daphee.dapheePeripherals.shared;

import net.minecraft.tileentity.TileEntityCommandBlock;

import com.daphee.lib.Reference;

import dan200.computer.api.ComputerCraftAPI;

public class CommonProxy {
    public void preInit(){
        
    }
    
    public void init(){
        System.out.println("Loading daphee's Peripherals v"+Reference.MOD_VERSION);
        registerTileEntities();
    }
    
   public void registerTileEntities(){
       System.out.println("[DEBUG] registering External Peripheral");
       ComputerCraftAPI.registerExternalPeripheral(TileEntityCommandBlock.class, new CommandBlockPeripheralHandler());
   }
}
