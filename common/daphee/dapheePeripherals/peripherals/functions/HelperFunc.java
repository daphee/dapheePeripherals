package daphee.dapheePeripherals.peripherals.functions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.SharedDrawable;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.Resource;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import daphee.dapheePeripherals.helpers.IconPacket;
import daphee.dapheePeripherals.helpers.IconRegister;
import daphee.dapheePeripherals.helpers.IconRequest;
import daphee.dapheePeripherals.helpers.Task;
import daphee.dapheePeripherals.peripherals.Arg;
import daphee.dapheePeripherals.peripherals.FunctionAdapter;
import daphee.dapheePeripherals.peripherals.LuaMethod;
import daphee.dapheePeripherals.peripherals.LuaType;
import daphee.dapheePeripherals.peripherals.MethodDeclaration;

public class HelperFunc extends FunctionAdapter {
    
    public HelperFunc() {
        super();
    }
   
    
    @LuaMethod(args={@Arg(name="player",type=LuaType.STRING),@Arg(name="id",type=LuaType.NUMBER)})
    public Object[] getIcon(IComputerAccess computer,String asPlayer,Double id) throws IOException{
        //For now just call the same on client and server, therefore use standard texturepack 
        try {
            /*TextureMap blockTextureMap = (TextureMap)Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture);
            TextureMap itemTextureMap = (TextureMap)Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationItemsTexture);
            //Is Blocktexture
            TextureAtlasSprite texture = blockTextureMap.getTextureExtry(asPlayer);
            if(texture==null)
                texture = itemTextureMap.getTextureExtry(asPlayer);
            if(texture==null){
                System.out.println("Cant find Texture");
            }
            else {
                System.out.println("Foudn texture "+texture.getFrameCount());
                BufferedImage img = new BufferedImage(texture.getIconWidth(), texture.getIconHeight(), BufferedImage.TYPE_INT_RGB);
                img.setRGB(0, 0, texture.getIconWidth(), texture.getIconHeight(), texture.getFrameTextureData(0), 0, texture.getIconWidth());
                File f = new File(Minecraft.getMinecraft().mcDataDir,id.intValue()+".png");
                ImageIO.write(img, "png", f);
                System.out.println("saved to "+f.getPath());
            }*/
            
            Drawable sd = new SharedDrawable(Display.getDrawable());
            sd.makeCurrent();
        RenderItem rb = new RenderItem();
        ByteBuffer buffer = BufferUtils.createByteBuffer(640 * 640 * 4);
        File file = new File(Minecraft.getMinecraft().mcDataDir,"test.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(640, 640, BufferedImage.TYPE_INT_RGB);
        rb.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), new ItemStack(Block.anvil), 0, 0);
        GL11.glReadPixels(0, 0, 640, 640, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer ); 
        for(int x = 0; x < 640; x++){
            for(int y = 0; y < 640; y++)
            {
                int i = (x + (640 * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, 640 - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
        ImageIO.write(image, "PNG", file);
        /*Icon icon = Item.itemsList[id.intValue()].getIconFromDamage(0);
        String blockLocation = "textures/blocks";
        String itemLocation = "texture/items";
        ResourceLocation resourcelocation = new ResourceLocation(icon.getIconName());
        ResourceLocation resourceBlock = new ResourceLocation(resourcelocation.getResourceDomain(), String.format("%s/%s%s", new Object[] {blockLocation, resourcelocation.getResourcePath(), ".png"}));
        ResourceLocation resourceItem = new ResourceLocation(resourcelocation.getResourceDomain(), String.format("%s/%s%s", new Object[] {itemLocation, resourcelocation.getResourcePath(), ".png"}));
        Resource res = null;
        try {
            res = Minecraft.getMinecraft().getResourceManager().getResource(resourceBlock);
        } catch(FileNotFoundException e){
        }
        try {
            res = Minecraft.getMinecraft().getResourceManager().getResource(resourceItem);
        } catch(FileNotFoundException e){
        }
        
        if(res!=null){
            System.out.println("Found resource");
            File f = new File(Minecraft.getMinecraft().mcDataDir,id.intValue()+".png");
            InputStream in = res.getInputStream();
            FileOutputStream out = new FileOutputStream(f);
            int i;
            System.out.println("Writing");
            while(true){
                i = in.read();
                if(i==-1)
                    break;
                System.out.println("reading"+i);
                out.write(i);
            }
            System.out.println("Saved as "+f.getPath());
        }
        */
        return null;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void update() {
      
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {

    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {

    }

    @Override
    public boolean canAttachToSide(int side) {
        return true;
    }

    @Override
    public void attach(IComputerAccess computer) {
    }

    @Override
    public void detach(IComputerAccess computer) {
    }

    @Override
    public Object[] processArgs(MethodDeclaration method,
            IComputerAccess computer, ILuaContext context, String methodName,
            Object[] args) {
            Object[] _args = new Object[args.length+1];
            _args[0] = computer;
            System.arraycopy(args, 0, _args, 1, args.length);
            return _args;
    }

}
    