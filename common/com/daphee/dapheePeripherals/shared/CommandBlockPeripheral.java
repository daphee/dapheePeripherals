package com.daphee.dapheePeripherals.shared;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import com.daphee.DapheePeripherals;

import cpw.mods.fml.common.modloader.ModLoaderHelper;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import dan200.computer.api.ComputerCraftAPI;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IHostedPeripheral;
import dan200.computer.api.ILuaContext;

public class CommandBlockPeripheral implements IHostedPeripheral{
    private TileEntityCommandBlock commandTile;
    
    //Task queues
    private LinkedList<Task> queue = new LinkedList<Task>();
    private LinkedList<ThreadedTask> threaded_queue = new LinkedList<ThreadedTask>();
    
    private List<IComputerAccess> attached_computers = new LinkedList<IComputerAccess>();
    private Map<IComputerAccess,LinkedList<ComputerTask>> computer_tasks = new HashMap<IComputerAccess,LinkedList<ComputerTask>>();
    
    private Map<Integer,Object> sockets = new HashMap<Integer,Object>();
    private Map<Integer,Integer> socket_computer = new HashMap<Integer,Integer>();
    private Map<Integer,LinkedList<ThreadedTask>> socket_tasks = new HashMap<Integer,LinkedList<ThreadedTask>>();
    private int socket_counter = 0;
    private int server_counter = 1;
    
    public CommandBlockPeripheral(TileEntityCommandBlock commandTile){
        this.commandTile = commandTile;
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public String getType() {
        return "command";
    }

    @Override
    public String[] getMethodNames() {
        return new String[]{"setCommand","getCommand","runCommand","run",
                "socket","close",
                "read","readLine",
                "write","writeLine",
                "closeAll",
                "bind","accept",
                "registerCommand"};
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context,
            int method, Object[] arguments) throws Exception {
        ServerSocket sock;
        switch(method){
        case 0://setCommand
            if(arguments.length < 1 || !(arguments[0] instanceof String))
                throw new Exception("setCommand(String command)");
            this.queue.add(new Task(arguments) {
                @Override
                public void run(){
                    CommandBlockPeripheral.this.commandTile.setCommand((String)this.args[0]);
                    CommandBlockPeripheral.this.commandTile.worldObj.markBlockForUpdate(CommandBlockPeripheral.this.commandTile.xCoord, 
                            CommandBlockPeripheral.this.commandTile.yCoord, 
                            CommandBlockPeripheral.this.commandTile.zCoord);
                }
            });
            return null;
        case 1: //getCommand
            return new Object[]{this.commandTile.getCommand()};
        case 2: //runCommand
            this.queue.add(new Task(){
              @Override
              public void run(){
                CommandBlockPeripheral.this.commandTile.executeCommandOnPowered(CommandBlockPeripheral.this.commandTile.worldObj);
              }
            });
            return null;
        case 3: //run
            if(arguments.length < 1 || !(arguments[0] instanceof String))
                throw new Exception("First argument needs to be a String");
            
            String full_command = ((String)arguments[0]).trim();
            String[] arr = full_command.split(" ");
            List<String> list = new LinkedList<String>(Arrays.asList(arr));
            String command = list.remove(0);
            String args[] = list.toArray(new String[list.size()]);
            
            ServerCommandManager manager = (ServerCommandManager)MinecraftServer.getServer().getCommandManager();
            if(!manager.getCommands().containsKey(command))
                throw new Exception("There is no such command '"+command+"'");
            
            ICommand c = (ICommand)manager.getCommands().get(command);
            CommandSender s = new CommandSender("computer #"+computer.getID());
            c.processCommand(s,args);
            
            return new Object[]{s.received};
        case 4: //socket
            if(arguments.length < 2 || !(arguments[0] instanceof String) || !(arguments[1] instanceof Double))
                throw new Exception("socket(address,port)");
            
            Socket socket = new Socket((String)arguments[0],((Double)arguments[1]).intValue());
            Integer id = this.socket_counter+=2;
            this.sockets.put(id, socket);
            this.socket_computer.put(id,computer.getID());
            this.socket_tasks.put(id, new LinkedList<ThreadedTask>());
            
            return new Object[]{id};
        case 5: //close
           if(arguments.length < 1 || !(arguments[0] instanceof Double))
              throw new Exception("close(handle)");
           id = ((Double)arguments[0]).intValue();
           if(!this.sockets.containsKey(id))
               throw new Exception("This socket doesn't exist");
                
           removeSocket(id);
           return null;
        case 6: //read
            if(arguments.length < 1 || !(arguments[0] instanceof Double))
                throw new Exception("read(handle)");
            id = ((Double)arguments[0]).intValue();
            if(!this.sockets.containsKey(id))
                throw new Exception("This socket doesn't exist");
            if(id%2!=0)
                throw new Exception("You need a normal for this method");
            
            int count = 1;
            if(arguments.length > 1 && arguments[1] instanceof Double)
                count = ((Double)arguments[1]).intValue();
            
            this.socket_tasks.get(id).add(new ComputerSocketTask(computer,id,new Object[]{count}){
                @Override
                public void run(){
                    try {
                        int count = (Integer)this.args[1];
                        BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                        char[] bytes = new char[count];
                        int read = reader.read(bytes, 0, count);
                        computer.queueEvent("read_"+this.socketId, new Object[]{read,new String(bytes)});
                    } catch(IOException e){
                        
                    }
                    super.run();
                }
            });
            return null;
        case 7: //readLine
            if(arguments.length < 1 || !(arguments[0] instanceof Double))
                throw new Exception("readLine(handle)");
            
            id = ((Double)arguments[0]).intValue();
            if(!this.sockets.containsKey(id))
                throw new Exception("This socket doesn't exist");
            if(id%2!=0)
                throw new Exception("You need a normal for this method");
            
            
            this.socket_tasks.get(id).add(new ComputerSocketTask(computer,id,new Object[]{}){
                @Override
                public void run(){
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                        String line = reader.readLine();
                        computer.queueEvent("read_line_"+this.socketId, new Object[]{line}); 
                    } catch(IOException e){
                        
                    }
                    super.run();
                }
            });
            return null;
        case 8: //write
            if(arguments.length < 2 || !(arguments[0] instanceof Double))
                throw new Exception("write(handle)");
            
            id = ((Double)arguments[0]).intValue();
            if(!this.sockets.containsKey(id))
                throw new Exception("This socket doesn't exist");
            if(id%2!=0)
                throw new Exception("You need a normal for this method");
            
            socket = (Socket)this.sockets.get(id);
            
            DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
            for(int i=1;i<arguments.length;i++){
                if(arguments[i] instanceof String){
                    stream.writeBytes((String)arguments[i]);
                }
                else if(arguments[i] instanceof Double){
                    stream.writeDouble((Double)arguments[i]);
                }
                else{
                    stream.writeBoolean((Boolean)arguments[i]);
                }
            }
            
            return new Object[]{stream.size()};
        case 9://writeLine
            if(arguments.length < 2 || !(arguments[0] instanceof Double))
                throw new Exception("writeLine(handle)");
            
            id = ((Double)arguments[0]).intValue();
            if(!this.sockets.containsKey(id))
                throw new Exception("This socket doesn't exist");
            if(id%2!=0)
                throw new Exception("You need a normal for this method");
            
            socket = (Socket)this.sockets.get(id);
            
            stream = new DataOutputStream(socket.getOutputStream());
            for(int i=1;i<arguments.length;i++){
                if(arguments[i] instanceof String){
                    stream.writeBytes((String)arguments[i]);
                }
                else if(arguments[i] instanceof Double){
                    stream.writeDouble((Double)arguments[i]);
                }
                else{
                    stream.writeBoolean((Boolean)arguments[i]);
                }
            }
            
            stream.writeBytes("\n");
            return new Object[]{stream.size()};
        case 10://closeAll
            for(int key:this.sockets.keySet()){
                removeSocket(key);
            }
            return null;
        case 11://bind
            if(arguments.length < 1 || !(arguments[0] instanceof Double))
                throw new Exception("bind(port)");
            
            sock = new ServerSocket(((Double)arguments[0]).intValue());
            
            id = this.server_counter+=2;
            
            this.sockets.put(id, sock);
            this.socket_computer.put(id, computer.getID());
            this.socket_tasks.put(id, new LinkedList<ThreadedTask>());
            
            return new Object[]{id};
        case 12://accept
            if(arguments.length < 1 || !(arguments[0] instanceof Double))
                throw new Exception("readLine(handle)");
            
            id = ((Double)arguments[0]).intValue();
            if(!this.sockets.containsKey(id))
                throw new Exception("This socket doesn't exist");
            if(id%2!=1)
                throw new Exception("You need a ServerSocket for this method");
            
            this.socket_tasks.get(id).add(new ComputerServerSocketTask(computer,id,new Object[]{}){
                @Override
                public void run(){
                    try {
                        Socket sock = this.socket.accept();
                        
                        int id = CommandBlockPeripheral.this.socket_counter+=2;
                        
                        CommandBlockPeripheral.this.sockets.put(id, sock);
                        CommandBlockPeripheral.this.socket_computer.put(id, computer.getID());
                        CommandBlockPeripheral.this.socket_tasks.put(id, new LinkedList<ThreadedTask>());
                        
                        computer.queueEvent("accept_"+this.socketId, new Object[]{id});
                    } catch(IOException e){
                        
                    }
                    super.run();
                }
            });
            return null;
        case 13://registerCommand
            
        }
        return null;
    }

    @Override
    public boolean canAttachToSide(int side) {
        return true;
    }

    @Override
    public void attach(IComputerAccess computer) {
        this.attached_computers.add(computer);
        this.computer_tasks.put(computer,new LinkedList<ComputerTask>());
        
        computer.mount("command", ComputerCraftAPI.createResourceMount(DapheePeripherals.class, "dapheePeripherals", "lua"));
    }

    @Override
    public void detach(IComputerAccess computer) {
        this.attached_computers.remove(computer);
        this.computer_tasks.remove(computer);
        
        for(int key:this.socket_computer.keySet()){
            if(this.socket_computer.get(key)==computer.getID()){
                try {
                    removeSocket(key);
                } catch (IOException e) {}
            }
        }
    }

    @Override
    public void update() {
        while (!this.queue.isEmpty()){
            Task task = this.queue.removeFirst();
            task.run();
        }
        while(!this.threaded_queue.isEmpty()){
            ThreadedTask task = this.threaded_queue.removeFirst();
            task.start();
        }
        for(LinkedList<ComputerTask> tasks:this.computer_tasks.values()){
            if(tasks.size()>0){
                if(!tasks.get(0).isAlive()&&!tasks.get(0).finished){
                    tasks.get(0).start();
                }
                if(!tasks.get(0).isAlive()&&tasks.get(0).finished){
                    tasks.remove(0);
                }
            }
        }
        for(int socketId:this.socket_tasks.keySet()){
            List<ThreadedTask> tasks = this.socket_tasks.get(socketId);
            if(tasks.size()>0){
                if(!tasks.get(0).isAlive()&&!tasks.get(0).finished)
                    tasks.get(0).start();
                if(!tasks.get(0).isAlive()&&tasks.get(0).finished)
                    tasks.remove(0);
            }
        }
    }
    
    public void removeSocket(int id) throws IOException{
        System.out.println("Removing socket:"+id);
        if(!this.sockets.containsKey(id))
            return;
        
        if(this.socket_computer.containsKey(id)){
            this.socket_computer.remove(id);
        }
        
        if(this.socket_tasks.containsKey(id)){
            this.socket_tasks.remove(id);
        }
        
        if(id%2==0){
            Socket sock = (Socket)this.sockets.remove(id);
            sock.close();
        }
        else {
            ServerSocket sock = (ServerSocket)this.sockets.remove(id);
            sock.close();
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        
    }
    
    private class Task {
        public Object[] args;
        public Task(Object[] args){
            this.args = args;
        }
        
        public Task(){   
        }
        
        public void run(){
        }
    }
    
    private class ThreadedTask extends Thread {
        private Object[] args;
        public boolean finished = false;
        public ThreadedTask(){   
        }
        public ThreadedTask(Object[] args){
            this.args = args;
        }
        
        @Override
        public void run(){
            finished = true;
        }
    }
    
    private class ComputerTask extends ThreadedTask {
        public IComputerAccess computer;
        public Object[] args;
        public ComputerTask(IComputerAccess computer,Object[] args){
            this.args = args;
        }
        
        @Override
        public void run(){
            super.run();
        }
    }
    
    private class ComputerSocketTask extends ThreadedTask {
        public IComputerAccess computer;
        public int socketId;
        public Socket socket;
        public Object[] args;
        
        public ComputerSocketTask(IComputerAccess computer,int socketId,Object[] args){
            this.computer = computer;
            this.socketId = socketId;
            this.args = args;
            this.socket = (Socket)CommandBlockPeripheral.this.sockets.get(socketId);
        }
        
        @Override
        public void run(){
            super.run();
        }
    }
    
    private class ComputerServerSocketTask extends ThreadedTask {
        public IComputerAccess computer;
        public int socketId;
        public ServerSocket socket;
        public Object[] args;
        
        public ComputerServerSocketTask(IComputerAccess computer,int socketId,Object[] args){
            this.computer = computer;
            this.socketId = socketId;
            this.args = args;
            this.socket = (ServerSocket)CommandBlockPeripheral.this.sockets.get(socketId);
        }
        
        @Override
        public void run(){
            super.run();
        }
    }
    
    private class CommandSender implements ICommandSender{
        public String name;
        public String received;
        
        public CommandSender(String name){
            this.name = name;
            this.received = new String();
        }
        
        @Override
        public String getCommandSenderName() {
            return this.name;
        }

        @Override
        public void sendChatToPlayer(ChatMessageComponent chatmessagecomponent) {
            this.received += chatmessagecomponent.toString();
        }

        @Override
        public boolean canCommandSenderUseCommand(int i, String s) {
            return true;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return null;
        }

        @Override
        public World func_130014_f_() {
            return null;
        }
        
    }
    
    @ForgeSubscribe
    public void onChatMessage(ServerChatEvent evt){
        for(IComputerAccess computer:this.attached_computers){
            computer.queueEvent("chat_event", new Object[]{evt.username,evt.message});
        }
    }
}
