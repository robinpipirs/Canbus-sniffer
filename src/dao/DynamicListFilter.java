package dao;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by robinpipirs on 17/03/16.
 */
public class DynamicListFilter implements Filter {
    private List<CanbusMessage> filterList = new ArrayList<>();
    public void addMessageToFilter(CanbusMessage canbusMessage){
        this.filterList.add(canbusMessage);
    }
    public List<CanbusMessage> getListOfFilteredMessages(){
        return this.filterList;
    }
    @Override
    public CanbusMessage execute(CanbusMessage request) {
        if (filterList.contains(request)){
           // System.out.println("Dynamic entry filter found filtering");
            return null;
        }
        else return request;
    }
}
