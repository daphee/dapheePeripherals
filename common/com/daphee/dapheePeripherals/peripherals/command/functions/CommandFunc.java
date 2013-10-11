package com.daphee.dapheePeripherals.peripherals.command.functions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;

import com.daphee.dapheePeripherals.peripherals.Arg;
import com.daphee.dapheePeripherals.peripherals.FunctionAdapter;
import com.daphee.dapheePeripherals.peripherals.LuaMethod;
import com.daphee.dapheePeripherals.peripherals.LuaType;
import com.daphee.dapheePeripherals.peripherals.MethodDeclaration;
import com.daphee.dapheePeripherals.peripherals.command.CommandBlockPeripheral;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class CommandFunc extends FunctionAdapter {
    private TileEntityCommandBlock commandTile;
    
    private LinkedList<Task> queue = new LinkedList<Task>();
    private LinkedList<ThreadedTask> threaded_queue = new LinkedList<ThreadedTask>();
    
    private List<IComputerAccess> attached_computers = new LinkedList<IComputerAccess>();
    private Map<IComputerAccess,LinkedList<ComputerTask>> computer_tasks = new HashMap<IComputerAccess,LinkedList<ComputerTask>>();
    
    public CommandFunc(CommandBlockPeripheral peripheral,TileEntityCommandBlock commandTile) {
        super(peripheral);
        this.commandTile = commandTile;
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @LuaMethod(args={@Arg(name="command",type=LuaType.STRING)})
    public Object[] setCommand(String command)throws Exception{
        this.queue.add(new Task(new Object[]{command}) {
            @Override
            public void run(){
                CommandFunc.this.commandTile.setCommand((String)this.args[0]);
                CommandFunc.this.commandTile.worldObj.markBlockForUpdate(CommandFunc.this.commandTile.xCoord, 
                        CommandFunc.this.commandTile.yCoord, 
                        CommandFunc.this.commandTile.zCoord);
            }
        });
        return null;
    }
    @LuaMethod
    public Object[] getCommand() throws Exception{
        return new Object[]{this.commandTile.getCommand()};
    }
    @LuaMethod
    public Object[] runCommand() throws Exception{
        this.queue.add(new Task(){
            @Override
            public void run(){
              CommandFunc.this.commandTile.executeCommandOnPowered(CommandFunc.this.commandTile.worldObj);
            }
          });
        return null;
    }
    
    @LuaMethod(args={@Arg(type=LuaType.STRING,name="command")})
    public Object[] run(IComputerAccess computer,String torun) throws Exception{
        String full_command = torun.trim();
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
        this.attached_computers.add(computer);
        this.computer_tasks.put(computer,new LinkedList<ComputerTask>());
    }

    @Override
    public void detach(IComputerAccess computer) {
        this.attached_computers.remove(computer);
        this.computer_tasks.remove(computer);
    }

    @Override
    public Object[] processArgs(MethodDeclaration method,
            IComputerAccess computer, ILuaContext context, String methodName,
            Object[] args) {
        if(method.getName()=="run"){
            Object[] _args = new Object[args.length+1];
            _args[0] = computer;
            System.arraycopy(args, 0, _args, 1, args.length);
            return _args;
        }
        return args;
    }

}
