package dao;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FilterChain {
    private List<Filter> filters = new ArrayList<>();
    private List<CanbusMessage> filteredMessages = new ArrayList<>();
    private DefaultListModel target;

    public void addFilter(Filter filter){
        filters.add(filter);
    }


    //TODO: lets through filter
    public void execute(CanbusMessage request){
        for (Filter filter : filters) {
        		
        	
//        	if(request.getId().equals("5BF")){
//				System.out.println("sss");
//			}
        	
        	
            CanbusMessage canbusMessage =filter.execute(request);
            if (canbusMessage != null){
                target.addElement(canbusMessage.toListString());
                
                
            }
        }

    }

    public void setTarget(DefaultListModel target2){
        this.target = target2;
    }
}