package daphee.dapheePeripherals.helpers;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class ConfigFile {
    
    public static String[] socket_whitelist = new String[]{};
    public static String[] socket_blacklist = new String[]{};
    
    //Block and Item IDs
    public static int advancedCommandBlockId = 650;
    
    public static void loadConfiguration(File suggestedFile){
        Configuration conf = new Configuration(suggestedFile);
        
        Property prop = conf.get("socket","whitelist",socket_whitelist);
        prop.comment = "Whitelist for all peripherals with the socket module.";
        socket_whitelist = prop.getStringList();
        
        prop = conf.get("socket","blacklist",socket_blacklist);
        prop.comment = "Blacklist for all peripherals with the socket module.";
        socket_blacklist = prop.getStringList();
        
        prop = conf.get("block", "advancedCommandBlockId", advancedCommandBlockId);
        prop.comment = "The id of the Advanced Command Block";
        advancedCommandBlockId = prop.getInt();
    }
}
