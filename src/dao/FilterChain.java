package dao;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FilterChain {
    private List<Filter> filters = new ArrayList<>();
    private List<CanbusMessage> filteredMessages = new ArrayList<>();
    private JTextArea target;

    public void addFilter(Filter filter){
        filters.add(filter);
    }


    //TODO: lets through filter
    public void execute(CanbusMessage request){
        for (Filter filter : filters) {
            CanbusMessage canbusMessage =filter.execute(request);
            if (canbusMessage != null){
                target.append(canbusMessage.toListString()+"\n");
            }
        }

    }

    public void setTarget(JTextArea target){
        this.target = target;
    }
}