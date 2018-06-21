package dao;

import javax.swing.*;

/**
 * Created by robinpipirs on 16/03/16.
 */
public class FilterCommunicationObserver extends CommunicationObserver {

    private FilterManager filterManager;

    public FilterCommunicationObserver(SerialCommunicationService serialCommunicationService, JTextArea jta) {
        this.serialCommunicationService = serialCommunicationService;
        this.jta = jta;
        this.serialCommunicationService.attach(this);
    }

    public void setFilterManager(FilterManager filterManager){
        this.filterManager = filterManager;
    }
    @Override
    public void update() {
        //jta.append(serialCommunicationService.getMessage());
        //filtering called here
        filterManager.filterRequest(serialCommunicationService.getMessage());
    }
}
