package dao;

import javax.swing.*;

public class FilterManager {
    FilterChain filterChain;

    public FilterManager(DefaultListModel target){
        filterChain = new FilterChain();
        filterChain.setTarget(target);
    }
    public void setFilter(Filter filter){
        filterChain.addFilter(filter);
    }

    public void filterRequest(CanbusMessage request){
        filterChain.execute(request);
    }
    
    public void setCounter(JTextField cntr) 
    {
    		filterChain.addCounter(cntr);
    }
}