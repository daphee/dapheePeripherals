package com.daphee.dapheePeripherals.peripherals.webinterface.server;

import static spark.Spark.*;
import spark.*;

class WebInterfaceServerImpl extends Thread {
    public WebInterfaceServerImpl(int port){
        setPort(port);
    }
    
    public void run(){
        //add routes here
        
        
        
        synchronized(this){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class WebInterfaceServer{
    private WebInterfaceServerImpl server;
    
    public WebInterfaceServer(int port){
        server = new WebInterfaceServerImpl(port);
    }
    
    public void start(){
        server.start();
    }
    
    public void stop(){
        
    }
}
