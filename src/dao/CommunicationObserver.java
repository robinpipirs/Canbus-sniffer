package dao;


import javax.swing.*;

/**
 * Created by robinpipirs on 16/03/16.
 */
public abstract class CommunicationObserver {
    protected SerialCommunicationService serialCommunicationService;
    protected JTextArea jta;
    protected DefaultListModel model;
    public abstract void update();
}
