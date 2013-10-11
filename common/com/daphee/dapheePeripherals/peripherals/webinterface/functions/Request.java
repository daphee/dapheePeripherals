package com.daphee.dapheePeripherals.peripherals.webinterface.functions;


public class Request {
    private String packet;
    private String response;
    public Request(String packet){
        this.packet = packet;
    }
    
    public String getPacket(){
        return this.packet;
    }
    
    public void setResponse(String resp){
        this.response = resp;
    }
    
    public String getResponse() {
        return this.response;
    }
    
    public boolean hasResponse(){
        return this.response == null;
    }
}
