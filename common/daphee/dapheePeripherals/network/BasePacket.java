package daphee.dapheePeripherals.network;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;

import cpw.mods.fml.common.network.PacketDispatcher;

public abstract class BasePacket {
    public static final String CHANNEL = "daphee-Channel";
    public abstract void write(DataOutputStream stream) throws IOException;
    public abstract void read(DataInputStream stream, int length) throws IOException;
    public abstract void handle(EntityPlayer player);
    
    public final Packet makePacket() throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(new DataOutputStream(out));
        return PacketDispatcher.getPacket(CHANNEL, out.toByteArray());
    }
    
}
