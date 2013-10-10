package com.daphee;

import com.daphee.dapheePeripherals.shared.CommonProxy;
import com.daphee.lib.Reference;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid=Reference.MOD_ID,name=Reference.MOD_NAME,version=Reference.MOD_VERSION,dependencies="after:ComputerCraft")
@NetworkMod(clientSideRequired=true,serverSideRequired=false)
public class DapheePeripherals {
    @Mod.Instance(Reference.MOD_ID)
    public static DapheePeripherals instance;
    
    @SidedProxy(clientSide="com.daphee.dapheePeripherals.client.ClientProxy",serverSide="com.daphee.dapheePeripherals.server.ServerProxy")
    public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        proxy.preInit();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event){
        proxy.init();
    }
    
}
