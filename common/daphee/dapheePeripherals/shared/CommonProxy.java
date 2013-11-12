package daphee.dapheePeripherals.shared;

import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityCommandBlock;


import dan200.computer.api.ComputerCraftAPI;
import daphee.dapheePeripherals.helpers.IconPacket;
import daphee.dapheePeripherals.lib.Reference;
import daphee.dapheePeripherals.network.PacketManager;

public class CommonProxy {
    public void preInit(){
        
    }
    
    public void init(){
        System.out.println("Loading daphee's Peripherals v"+Reference.MOD_VERSION);
        registerPacketTypes();
    }
    
   public void registerPacketTypes(){
       PacketManager.packet_types.put(0, IconPacket.class);
   }
    
}
