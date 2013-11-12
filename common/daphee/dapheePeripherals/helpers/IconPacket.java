package daphee.dapheePeripherals.helpers;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.text.html.parser.Entity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;


import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import daphee.dapheePeripherals.network.BasePacket;

public class IconPacket extends BasePacket {
    public static enum methods {REQUEST,RESPONSE};
    public int method;
    public int requestNum;
    public int requestContent;
    public byte[] responseContent;
    
    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.write(method);
        stream.write(requestNum);
        
        if(method==methods.REQUEST.ordinal()){
            stream.write(this.requestContent);
        }
        else {
            stream.write(this.responseContent);
        }
    }

    @Override
    public void read(DataInputStream stream, int length) throws IOException {
        method = stream.readInt();
        requestNum = stream.readInt();
        
        if(method==methods.REQUEST.ordinal()){
            requestContent = stream.readInt();
        }
        else {
            responseContent = new byte[length-12];
            stream.readFully(responseContent);
        }
    }

    @Override
    public void handle(EntityPlayer player) {
        System.out.println("Got Packet");
        if(player.worldObj.isRemote&&this.method==methods.RESPONSE.ordinal()){
            System.out.println("Got Response from "+player.username);
            if(IconRegister.requests.containsKey(requestNum)){
                IconRequest req = IconRegister.requests.get(requestNum);
                synchronized(req){
                    req.response = new String(responseContent);
                }
            }
        }
        else if(!player.worldObj.isRemote&&this.method==methods.REQUEST.ordinal()) {
            System.out.println("Got Requst for Item with ID "+this.requestContent);
            System.out.println("Getting "+Item.itemsList[requestContent].getIconFromDamage(0).getIconName());
            ResourceLocation loc = new ResourceLocation(Item.itemsList[requestContent].getIconFromDamage(0).getIconName());
            IconPacket pack = new IconPacket();
            pack.requestNum = this.requestNum;
            pack.method = methods.RESPONSE.ordinal();
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
                
                char c;
                while((c=(char)in.read())!=-1){
                    b.write(c);
                }
                responseContent = b.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                pack.responseContent = new String("[error]").getBytes();
            }
            try {
                System.out.println("Sending packet");
                PacketDispatcher.sendPacketToServer(pack.makePacket());
                System.out.println("Success");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
