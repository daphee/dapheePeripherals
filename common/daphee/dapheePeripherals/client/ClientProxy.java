package daphee.dapheePeripherals.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetServerHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

import daphee.dapheePeripherals.shared.CommonProxy;

public class ClientProxy extends CommonProxy {
    public static boolean saved = false;
    @Override
    public void init() {
        super.init();
        //MinecraftForge.EVENT_BUS.register(this);
    }
    //Ignore this stuff
    /*
    @ForgeSubscribe
    public void onRenderGameOverlay(RenderGameOverlayEvent e){
        if(saved)
          return;
        System.out.println("Rendering awesome stuff");
        boolean FBOEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
        if(!FBOEnabled){
            System.out.println("Framebuffer objects not enabled.");
            saved = true;
            return;
        }
        saved = true;
        
        int width = 512;
        int height = 512;
        
        //Generate framebuffer object
        int framebuffer =  glGenFramebuffersEXT();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
        
        //Generate Texture
        int color_tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, color_tex);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0,GL_RGB, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT,GL_COLOR_ATTACHMENT0_EXT,GL_TEXTURE_2D, color_tex, 0);
        
        //Generate and Setup a 24bit depth buffer
        int depth_rb = glGenRenderbuffersEXT();
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depth_rb);
        glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
        //Attach depth buffer
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT,GL_RENDERBUFFER_EXT, depth_rb);
        
        //status
        int fb = glCheckFramebufferStatusEXT( GL_FRAMEBUFFER_EXT ); 
        switch ( fb ) {
            case GL_FRAMEBUFFER_COMPLETE_EXT:
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception" );
            default:
                throw new RuntimeException( "Unexpected reply from glCheckFramebufferStatusEXT: " + fb );
        }
        //End status
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
        glViewport(0,0,width,height);
 
        RenderItem rb = new RenderItem();
        rb.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), new ItemStack(Block.anvil), 0, 0);
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        glBindTexture(GL_TEXTURE_2D, color_tex);
        glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,buffer); 
        //glReadBuffer(GL_COLOR_ATTACHMENT0_EXT);
        //glReadPixels(0,0,width,height,GL_RGBA,GL_UNSIGNED_BYTE,buffer);
        //Save awesome stuff to picture
        File file = new File(Minecraft.getMinecraft().mcDataDir,"test.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int c = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++)
            {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                c++;
            }
        }
        System.out.println("Read "+c+" pixels");
        try {
            ImageIO.write(image, format, file);
            System.out.println("Saved to "+file.getPath());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        
        glDeleteTextures(color_tex);
        glDeleteRenderbuffersEXT(depth_rb);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT,0);
        glDeleteFramebuffersEXT(framebuffer);
        //NULL means reserve texture memory, but texels are undefined
        //Generate the buffers
        /*int width = 32;
        int height = 32;
        //Generate framebuffer object
        int framebuffer =  glGenFramebuffersEXT();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
        
        //Create a color buffer
        int color_rb = glGenRenderbuffersEXT();
        //Bind the color buffer
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, color_rb);
        //Setup the color buffer
        glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_RGBA8, width, height);
        //Attach buffer
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_RENDERBUFFER_EXT, color_rb);
        
        //Generate and Setup a 24bit depth buffer
        int depth_rb = glGenRenderbuffersEXT();
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depth_rb);
        glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
        //Attach depth buffer
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT,GL_RENDERBUFFER_EXT, depth_rb);
        
        //Status check
        int fb = glCheckFramebufferStatusEXT( GL_FRAMEBUFFER_EXT ); 
        switch ( fb ) {
            case GL_FRAMEBUFFER_COMPLETE_EXT:
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception" );
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                throw new RuntimeException( "FrameBuffer: " + framebuffer
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception" );
            default:
                throw new RuntimeException( "Unexpected reply from glCheckFramebufferStatusEXT: " + fb );
        }
        //End status
 
        //Set to do awesome Stuff on this framebuffer
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT,framebuffer);
        //Do awesome stuff here
        
        RenderItem rb = new RenderItem();
        rb.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), new ItemStack(Block.anvil), 0, 0);
        
        //End
        //Save awesome Stuff to buffer
        //ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        //glReadBuffer(GL_COLOR_ATTACHMENT0_EXT);
        //glReadPixels(0,0,width,height,GL_RGB,GL_UNSIGNED_BYTE,buffer);
        //Save awesome stuff to picture
        /*File file = new File(Minecraft.getMinecraft().mcDataDir,"test.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int c = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++)
            {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                c++;
            }
        }
        System.out.println("Read "+c+" pixels");
        /*try {
            ImageIO.write(image, format, file);
            System.out.println("Saved to "+file.getPath());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
      
        //Return to onscreen rendering / Cleanup
        glDeleteRenderbuffersEXT(color_rb);
        glDeleteRenderbuffersEXT(depth_rb);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT,0);
        glDeleteFramebuffersEXT(framebuffer);*|
 
    }*/
}
