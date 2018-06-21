package dao;

import javax.swing.*;

/**
 * Created by robinpipirs on 16/03/16.
 */
public class LiveCommunicationObserver extends CommunicationObserver {

    public LiveCommunicationObserver(SerialCommunicationService serialCommunicationService, JTextArea jta) {
        this.serialCommunicationService = serialCommunicationService;
        this.jta = jta;
        this.serialCommunicationService.attach(this);
    }

    @Override
    public void update() {
        jta.append(serialCommunicationService.getMessage().toListString()+"\n");
    }
}
