package daphee;


import net.minecraft.block.Block;
import net.minecraftforge.client.model.AdvancedModelLoader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import daphee.dapheePeripherals.block.BlockAdvancedCommandBlock;
import daphee.dapheePeripherals.helpers.ConfigFile;
import daphee.dapheePeripherals.lib.Reference;
import daphee.dapheePeripherals.network.BasePacket;
import daphee.dapheePeripherals.network.PacketHandler;
import daphee.dapheePeripherals.shared.CommonProxy;

@Mod(modid=Reference.MOD_ID,name=Reference.MOD_NAME,version=Reference.MOD_VERSION,dependencies="after:ComputerCraft;")
@NetworkMod(clientSideRequired=true,serverSideRequired=false,channels={BasePacket.CHANNEL},packetHandler=PacketHandler.class)
public class DapheePeripherals {
    @Mod.Instance(Reference.MOD_ID)
    public static DapheePeripherals instance;
    
    //Blocks
    public static Block advancedCommandBlock;
    
    @SidedProxy(clientSide="daphee.dapheePeripherals.client.ClientProxy",serverSide="daphee.dapheePeripherals.server.ServerProxy")
    public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        ConfigFile.loadConfiguration(event.getSuggestedConfigurationFile());
        
        advancedCommandBlock = new BlockAdvancedCommandBlock(ConfigFile.advancedCommandBlockId)
            .setBlockUnbreakable().setResistance(6000000.0F)
            .setUnlocalizedName("advancedCommandBlock")
            .setTextureName("dapheePeripherals:advanced_command_block");
        proxy.preInit();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event){
        LanguageRegistry.addName(advancedCommandBlock,"Advanced Command Block");
        GameRegistry.registerBlock(advancedCommandBlock,"advancedCommandBlock");
        proxy.init();
    }
    
}
