package dao;

public class Target {
    public void execute(CanbusMessage request){
        System.out.println("Executing request: " + request);
    }
}