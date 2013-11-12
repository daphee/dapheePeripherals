package daphee.dapheePeripherals.helpers;

public class IconRequest {
    public String response;
    public int itemId;
    public Task task;
    public IconRequest(int itemId,Task task){
        this.itemId = itemId;
        this.task = task;
    }
}

