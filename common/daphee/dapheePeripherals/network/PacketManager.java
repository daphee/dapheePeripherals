package daphee.dapheePeripherals.network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.network.Player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PacketManager {
    public static Map<Integer,Class<?extends BasePacket>> packet_types = new HashMap<Integer,Class<?extends BasePacket>>();

    public static void handlePacket(Packet250CustomPayload packet,Player player) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            int id = inputStream.readByte();
            if(!packet_types.containsKey(id))
                return;
            BasePacket p = packet_types.get(id).newInstance();
            p.read(inputStream,packet.length);
            p.handle((EntityPlayer)player);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(InstantiationException e){
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public class PacketException extends Exception {
        public PacketException() {
        }

        public PacketException(String message, Throwable cause) {
                super(message, cause);
        }

        public PacketException(String message) {
                super(message);
        }

        public PacketException(Throwable cause) {
                super(cause);
        }
    }
    
    
}
