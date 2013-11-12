package daphee.dapheePeripherals.peripherals.functions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.nbt.NBTTagCompound;


import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import daphee.dapheePeripherals.peripherals.Arg;
import daphee.dapheePeripherals.peripherals.FunctionAdapter;
import daphee.dapheePeripherals.peripherals.LuaMethod;
import daphee.dapheePeripherals.peripherals.LuaType;
import daphee.dapheePeripherals.peripherals.MethodDeclaration;

public class SocketFunc extends FunctionAdapter {
    private Map<Integer,Object> sockets = new HashMap<Integer,Object>();
    private Map<Integer,Integer> socket_computer = new HashMap<Integer,Integer>();
    private Map<Integer,LinkedList<ThreadedTask>> socket_tasks = new HashMap<Integer,LinkedList<ThreadedTask>>();
    private final AtomicInteger socket_counter = new AtomicInteger();
    private final AtomicInteger server_counter = new AtomicInteger();
    
    public SocketFunc() {
        super();
    }
    
    /*
     * Socket Functions
     */
    @LuaMethod(name="socket",args={@Arg(name="host",type=LuaType.STRING),@Arg(name="port",type=LuaType.NUMBER)})
    public Object[] createSocket(IComputerAccess computer, String host, Double port) throws IOException {
        Socket socket = new Socket(host,port.intValue());
        Integer id = this.socket_counter.incrementAndGet()*2;
        this.sockets.put(id, socket);
        this.socket_computer.put(id,computer.getID());
        this.socket_tasks.put(id, new LinkedList<ThreadedTask>());
        
        return new Object[]{id};
    }
    
    @LuaMethod(name="close",args={@Arg(name="id",type=LuaType.NUMBER)})
    public Object[] closeSocket(IComputerAccess computer, Double id) throws Exception {
         if(!this.sockets.containsKey(id.intValue()))
             throw new Exception("This socket doesn't exist");
         
         removeSocket(id.intValue());
         return null;
    }
    
    @LuaMethod(name="read",args={@Arg(name="id",type=LuaType.NUMBER),@Arg(name="toRead",type=LuaType.NUMBER)})
    public Object[] read(IComputerAccess computer,Double id, Double toRead) throws Exception {
        if(!this.sockets.containsKey(id.intValue()))
            throw new Exception("This socket doesn't exist");
        if(id.intValue()%2!=0)
            throw new Exception("You need a normal socket for this method");
        
        this.socket_tasks.get(id.intValue()).add(new ComputerSocketTask(computer,id.intValue(),new Object[]{toRead}){
            @Override
            public void run(){
                try {
                    int count = (Integer)this.args[1];
                    BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    char[] bytes = new char[count];
                    int num_read = reader.read(bytes, 0, count);
                    if(num_read==-1)
                        removeSocket(this.socketId);
                    computer.queueEvent("read_"+this.socketId, new Object[]{num_read,new String(bytes)});
                } catch(IOException e){
                    
                }
                super.run();
            }
        });
        return null;
    }
    
    @LuaMethod(name="readLine",args={@Arg(name="id",type=LuaType.NUMBER)})
    public Object[] readLine(IComputerAccess computer, Double id) throws Exception {
        System.out.println("readLine");
        if(!this.sockets.containsKey(id.intValue()))
            throw new Exception("This socket doesn't exist");
        if(id.intValue()%2!=0)
            throw new Exception("You need a normal socket for this method");
        
        
        this.socket_tasks.get(id.intValue()).add(new ComputerSocketTask(computer,id.intValue(),new Object[]{}){
            @Override
            public void run(){
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    String line = reader.readLine();
                    if(line==null)
                        removeSocket(this.socketId);
                    computer.queueEvent("read_line_"+this.socketId, new Object[]{line}); 
                } catch(IOException e){
                    computer.queueEvent("read_line_"+this.socketId, new Object[]{null,e.toString()});
                }
                super.run();
            }
        });
        return null;
    }
    
    @LuaMethod(name="write",args={@Arg(name="id",type=LuaType.NUMBER)})
    public Object[] write(IComputerAccess computer, Double id, Object[] arguments) throws Exception{
        System.out.println("write");
        if(!this.sockets.containsKey(id.intValue())){
            throw new Exception("This socket doesn't exist");
        }
        if(id.intValue()%2!=0){
            throw new Exception("You need a normal for this method");
          }
        
        Socket socket = (Socket)this.sockets.get(id.intValue());
        DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
        for(int i=0;i<arguments.length;i++){
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
    }
    
    @LuaMethod(name="writeLine",args={@Arg(name="id",type=LuaType.NUMBER)})
    public Object[] writeLine(IComputerAccess computer, Double id, Object[] arguments) throws Exception{
        Object _arguments[] = new Object[arguments.length+1];
        
        System.arraycopy(arguments, 0, _arguments, 0, arguments.length);
        _arguments[arguments.length] = "\n";
        
        return write(computer,id,_arguments);
    }
    
    @LuaMethod
    public Object[] closeAll(IComputerAccess access) throws IOException{
        for(int id:this.sockets.keySet()){
            removeSocket(id);
        }
        return null;
    }
    
    @LuaMethod(name="bind",args={@Arg(name="port",type=LuaType.NUMBER)})
    public Object[] createServerSocket(IComputerAccess computer,Double port) throws Exception{
        //Check if the computer has an open socket 
        for(int i=0;i<server_counter.get();i++){
            int _id = i*2+1; //ServerSocket are at odd ids.
            if(sockets.containsKey(_id) && sockets.get(_id) instanceof ServerSocket
                    && socket_computer.containsKey(_id) && socket_computer.get(_id)==computer.getID()){
                ServerSocket s = (ServerSocket)sockets.get(_id);
                //We found a open server socket on the calling computer with same port. better return this.
                if(s.getLocalPort()==port.intValue()){
                    return new Object[]{_id};
                }
            }
        }
        ServerSocket sock = new ServerSocket(port.intValue());
        int id = this.server_counter.incrementAndGet()*2+1;
        this.sockets.put(id, sock);
        this.socket_computer.put(id, computer.getID());
        this.socket_tasks.put(id, new LinkedList<ThreadedTask>());
        
        return new Object[]{id};
    }
    
    @LuaMethod(name="accept",args={@Arg(name="id",type=LuaType.NUMBER)})
    public Object[] accept(IComputerAccess computer,Double id) throws Exception{
        if(!this.sockets.containsKey(id.intValue()))
            throw new Exception("This socket doesn't exist");
        if(id.intValue()%2!=1)
            throw new Exception("You need a ServerSocket for this method");
        
        this.socket_tasks.get(id.intValue()).add(new ComputerServerSocketTask(computer,id.intValue(),new Object[]{}){
            @Override
            public void run(){
                try {
                    Socket sock = this.socket.accept();
                    
                    int id = SocketFunc.this.socket_counter.incrementAndGet()*2;
                    
                    SocketFunc.this.sockets.put(id, sock);
                    SocketFunc.this.socket_computer.put(id, computer.getID());
                    SocketFunc.this.socket_tasks.put(id, new LinkedList<ThreadedTask>());
                    
                    computer.queueEvent("accept_"+this.socketId, new Object[]{id});
                } catch(IOException e){
                    computer.queueEvent("accept_"+this.socketId, new Object[]{null});
                }
                super.run();
            }
        });
        return null;
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
    public void update() {
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
        for(int sock:this.sockets.keySet()){
            if(this.socket_computer.containsKey(sock)&&this.socket_computer.get(sock)==computer.getID()){
                try{
                    removeSocket(sock);
                }catch(IOException e){};
            }
                
        }
    }
    
    public class ThreadedTask extends Thread {
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
    
    private class ComputerSocketTask extends ThreadedTask {
        public IComputerAccess computer;
        public int socketId;
        public Socket socket;
        public Object[] args;
        
        public ComputerSocketTask(IComputerAccess computer,int socketId,Object[] args){
            this.computer = computer;
            this.socketId = socketId;
            this.args = args;
            this.socket = (Socket)SocketFunc.this.sockets.get(socketId);
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
            this.socket = (ServerSocket)SocketFunc.this.sockets.get(socketId);
        }
        
        @Override
        public void run(){
            super.run();
        }
    }
    
    @Override
    public Object[] processArgs(MethodDeclaration method,IComputerAccess computer,
            ILuaContext context, String methodName, Object[] args) {
        Object[] _args = new Object[args.length+1];
        _args[0] = computer;
        System.arraycopy(args, 0, _args, 1, args.length);
        return _args;
    }

    
   
}
