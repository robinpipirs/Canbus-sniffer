package dao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robinpipirs on 17/03/16.
 */
public class ListFilter implements Filter {

    private List<CanbusMessage> filterList = new ArrayList<>();

    public ListFilter(List<CanbusMessage> filterList) {
        this.filterList = filterList;
    }
    public void addToList(CanbusMessage msg){
        filterList.add(msg);
        System.out.println(filterList.contains(msg));
    }
    public void removeFromList(CanbusMessage msg){
        System.out.println(filterList.contains(msg));
        filterList.remove(msg);
       System.out.println(filterList.contains(msg));

    }
    public List<CanbusMessage> getList(){
        return filterList;
    }
    @Override
    public CanbusMessage execute(CanbusMessage request) {
//        CanbusMessage canCan = new CanbusMessage("88120","58D",new String[]{"64","10","00"});
//        System.out.println("CONTAINS THIS MESSAGE: "+filterList.contains(canCan));
//        System.out.println("C: "+filterList.contains(canCan) +" "+canCan.toListString() + " "+canCan.getTimestamp());
//        System.out.println("C: "+filterList.contains(request) +" "+request.toListString() + " "+request.getTimestamp());
//        if (filterList.contains(request)){
//            return null;
//        }
         if(!request.getId().equals("6A6")) 
        {
        		return null;
        }
        else return request;
    }
}
