package com.daphee.dapheePeripherals.peripherals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class FunctionManager {
    private Map<FunctionAdapter,ArrayList<MethodDeclaration>> adapters = new HashMap<FunctionAdapter,ArrayList<MethodDeclaration>>();
    private Map<String,MethodDeclaration> mapping = new HashMap<String,MethodDeclaration>();
    
    public void map(){
        mapping.clear();
        for(ArrayList<MethodDeclaration> methods: adapters.values()){
            for(MethodDeclaration method:methods){
                if(mapping.containsKey(method.getName())){
                    String name1 = mapping.get(method.getName()).getMethod().getClass().getName();
                    String name2 = method.getMethod().getClass().getName();
                    System.out.println("[ERROR] There are two functions '"+method.getName()+"' ("+name1+","+name2+")");
                    continue;
                }
                mapping.put(method.getName(),method);
            }
        }
    }
    
    public void addAdapter(FunctionAdapter adapter){
        ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
        for(Method method:adapter.getClass().getMethods()){
            LuaMethod ann = method.getAnnotation(LuaMethod.class);
            if(ann!=null){
                methods.add(new MethodDeclaration(ann, method, adapter));
            }
        }
        
        adapters.put(adapter, methods);
        map();
    }
    
    public String[] getMethodNames(){
        ArrayList<String> names = new ArrayList<String>();
        for(ArrayList<MethodDeclaration> methods: adapters.values()){
            for(MethodDeclaration method:methods){
                names.add(method.getName());
            }
        }
        return names.toArray(new String[names.size()]);
    }
    /*
     * Warning: Shitty algorithm
     */
    public Object[] callMethod(IComputerAccess computer,ILuaContext context, String methodName, Object[] arguments) throws Exception{
        MethodDeclaration method = mapping.get(methodName);
        
        LinkedList<Object> argList = new LinkedList<Object>(Arrays.asList(arguments));
        LinkedList<Object> converted = new LinkedList<Object>();
        LinkedList<Arg> luaArgs = new LinkedList<Arg>(Arrays.asList(method.getAnnotation().args()));
        
        while(luaArgs.size()>0){
            Arg arg = luaArgs.remove();
            if(argList.size()==0)
                throw new Exception("Too less arguments");
            Object obj = argList.remove();
            System.out.println(arg.type().getJavaType().getName()+":"+obj.getClass().getName());
            if(!arg.type().getJavaType().isAssignableFrom(obj.getClass()))
                throw new Exception("Argument '"+arg.name()+"' has to be of type "+arg.type().getName());
            converted.add(obj);
            System.out.println("Added"+obj.toString());
        }
        
        Object[] required = converted.toArray();
        Object[] extra = argList.toArray();
        
        System.out.println("R:"+required.length+" E:"+extra.length);
        
        required = method.getAdapter().processArgs(method, computer, context, methodName, required);
        
        System.out.println("R:"+required.length+" E:"+extra.length);
        boolean needs_extra = method.getMethod().getParameterTypes()[method.getMethod().getParameterTypes().length-1] == Object[].class;
        Object[] f;
        if(required.length < method.getMethod().getParameterTypes().length&&needs_extra){
            System.out.println("Adding extra");
            f = new Object[required.length+1];
            System.arraycopy(required, 0, f, 0, required.length);
            f[required.length] = extra;
        }
        else {
            f = new Object[required.length];
            System.arraycopy(required, 0, f, 0, required.length);
        }
        
        System.out.println("F:"+f.length+" M:"+method.getMethod().getParameterTypes().length);
        
        if(f.length < method.getMethod().getParameterTypes().length)
            throw new Exception("Sorry, I have still too less arguments");
        try {
        Object[] r = (Object[]) method.getMethod().invoke(method.getAdapter(), f);
        return r;
        } catch(InvocationTargetException e){
            throw new Exception(e.getCause());
        }
       
    }
    
    public void update(){
        for(FunctionAdapter adapter:adapters.keySet()){
            adapter.update();
        }
    }

    public void readFromNBT(NBTTagCompound nbttagcompound){
        for(FunctionAdapter adapter:adapters.keySet()){
            adapter.readFromNBT(nbttagcompound);
        }
    }
    
    public void writeToNBT(NBTTagCompound nbttagcompound){
        for(FunctionAdapter adapter:adapters.keySet()){
            adapter.writeToNBT(nbttagcompound);
        }
    }
    
    public boolean canAttachToSide(int side){
        boolean attach = true;
        for(FunctionAdapter adapter:adapters.keySet()){
            attach&=adapter.canAttachToSide(side);
        }
        return attach;
    }
    
    public void attach(IComputerAccess computer){
        for(FunctionAdapter adapter:adapters.keySet()){
            adapter.attach(computer);
        }
    }

    public void detach(IComputerAccess computer){
        for(FunctionAdapter adapter:adapters.keySet()){
            adapter.detach(computer);
        }
    }
}
