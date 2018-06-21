package dao;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import java.util.List;

import com.fazecast.jSerialComm.*;
import com.google.gson.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

/**
 * Created by robinpipirs on 10/03/16.
 */
public class Sniffer {


    public static void main(String[] args) throws IOException {
        Collection<CanbusMessage> canBusMessagesToFilter;

        Config config = null;
        
        /** Fetching config **/
        try{
        	
        		Reader reader = new InputStreamReader(Sniffer.class.getResourceAsStream("/config.json"), "UTF-8");
        	
        	    Gson gson = new GsonBuilder().create();
            config = gson.fromJson(reader, Config.class);
            //System.out.println(config);
        }catch (Exception e) {
			// TODO: handle exception
        	System.out.println(e);
		}
        

        CanbusLogFileParser canbusLogfileParser = new CanbusLogFileParser();
        canBusMessagesToFilter = canbusLogfileParser
                .ParseCanBusLog("bin/filters.asc");

        List<CanbusMessage> messagesToFilter = new ArrayList<CanbusMessage>(canBusMessagesToFilter);

        /** Setting up SerialcomService **/
        SerialCommunicationService scs = new SerialCommunicationService(config.getComportName(),Integer.parseInt(config.getBaudRate()));
       
        /** things happening in the gui here **/
        CanbusDataView canBusView = new CanbusDataView();
        new LiveCommunicationObserver(scs, canBusView.getView());

        FilterManager filterManager = new FilterManager(canBusView.getFilterView());

        ListFilter listFilter = new ListFilter(messagesToFilter);
        filterManager.setFilter(listFilter);

        new FilterCommunicationObserver(scs,canBusView.getFilterView()).setFilterManager(filterManager);
        final Thread thread = new Thread(scs);
        thread.start();

        /** Gui is built here**/

        JFrame mainJFrame;
        mainJFrame = new JFrame();
        mainJFrame.setLayout(new BorderLayout());
        mainJFrame.setTitle("PipirsSolutions canbus sniffer");
        mainJFrame.setBackground(Color.BLUE);

        mainJFrame.add(canBusView,BorderLayout.WEST);
        JPanel messageAndFilterPanel = new JPanel(new BorderLayout());
        messageAndFilterPanel.add(new SendMessageModel(scs),BorderLayout.NORTH);
        messageAndFilterPanel.add(new FilterListModel(listFilter),BorderLayout.SOUTH);
        mainJFrame.add(messageAndFilterPanel, BorderLayout.EAST);
        mainJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainJFrame.pack();
        mainJFrame.setVisible(true);

        /** connecting ui components to the services**/


    }

    public static class SendMessageModel extends JPanel {

        private JButton sendBtn = new JButton("Send!");
        private JTextField id = new JTextField(10);
        private JTextField data = new JTextField(10);
        private SerialCommunicationService scs;

        public SendMessageModel(SerialCommunicationService scs){
            this.scs=scs;
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(sendBtn,BorderLayout.EAST);
            panel.add(id,BorderLayout.WEST);
            panel.add(data,BorderLayout.CENTER);
            add(panel);
            sendBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String canId = id.getText();
                    String canData = data.getText();
                    String id;
                    String id2;
                    if ((canId.length() > 2) || (canId.length() < 3) && canData.length() > 2){
                        if (canId.length()>2){
                            id = "0"+canId.substring(0,1);
                            id2= canId.substring(1,3);
                        }
                        else{
                            id = "00";
                            id2 = canId;
                        }
                        String delims = "[ ]";
                        String[] tokens = canData.split(delims);
                        String[] message = new String[tokens.length+3];

                        message[0] = id;
                        message[1] = id2;
                        message[message.length-1] = "AA";

                        for (int i = 0; i < tokens.length;i++){
                            message[i+2] = tokens[i];
                        }

                        byte[] writeBytes = new byte[message.length];

                        for(int i = 0; i < writeBytes.length; i++){
                            writeBytes[i] = (byte)(Integer.parseInt(message[i],16) & 0xff);
                        }
                        try {
                            scs.getOutPutStream().write(writeBytes);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    }
                }
            });
        }

    }

    public static class CanbusDataView extends JPanel {

        private JTextArea jta;
        private JTextArea jtaFiltered;

        public JTextArea getView(){
            return jta;
        }
        public JTextArea getFilterView(){
          return jtaFiltered;
        }

        public CanbusDataView() {

            jta = new JTextArea(40, 40);
            DefaultCaret caret = (DefaultCaret) jta.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            jta.append("Live canBus signal will appear here.\n");
            JScrollPane sp = new JScrollPane(jta,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            JScrollBar bar = sp.getVerticalScrollBar();
            bar.setPreferredSize(new Dimension(10, 0));

            jtaFiltered = new JTextArea(40, 40);
            DefaultCaret caret2 = (DefaultCaret) jtaFiltered.getCaret();
            caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            jtaFiltered.append("Filtered canBusSignal will appear here.\n");
            JScrollPane spFiltered = new JScrollPane(jtaFiltered,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            JScrollBar bar2 = spFiltered.getVerticalScrollBar();
            bar2.setPreferredSize(new Dimension(10, 0));

            JPanel jView = new JPanel(new BorderLayout());
            jView.add(sp, BorderLayout.WEST);
            jView.add(new JPanel(), BorderLayout.CENTER);
            jView.add(spFiltered, BorderLayout.EAST);

            add(jView);

        }
    }


    public static class FilterListModel extends JPanel {

        JList list;
        DefaultListModel model;
        CanbusLogFileParser canbusLogFileParser = new CanbusLogFileParser();
        private List<CanbusMessage> canList;
        private ListFilter listFilter;

        public FilterListModel(ListFilter listFilter) {
            this.listFilter = listFilter;
            setLayout(new BorderLayout());
            model = new DefaultListModel();
            list = new JList(model);
            JScrollPane pane = new JScrollPane(list);
            JButton addButton = new JButton("Add");
            JButton removeButton = new JButton("Remove");
            JTextField id = new JTextField(10);
            JTextField data = new JTextField(10);
            JPanel canbusPanel = new JPanel(new BorderLayout());
            canbusPanel.add(id,BorderLayout.WEST);
            canbusPanel.add(data,BorderLayout.EAST);

            for (CanbusMessage canMsg: this.listFilter.getList()
                 ) {
                model.addElement(canMsg.toListString());
            }

            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String canId = id.getText();
                    String canData = data.getText();
                    if ((canId.length() > 2) || (canId.length() < 3) && canData.length() > 2){
                        String canString = "T: 0 ID: " + canId + ", Data: " + canData;
                        System.out.println(canString);
                        CanbusMessage canbusMessage = canbusLogFileParser.ParseCanBusString(canString);
                        if (canbusMessage != null){
                            model.addElement(canbusMessage.toListString());
                            listFilter.addToList(canbusMessage);
                        }
                    }
                }
            });
            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.getSize() > 0) {
                        String canString = model.getElementAt(list.getSelectedIndex()).toString();
                        canString = "T: 2000 " + canString;
                        CanbusMessage deleteMsg = canbusLogFileParser.ParseCanBusString(canString);

                        model.removeElementAt(list.getSelectedIndex());
                        listFilter.removeFromList(deleteMsg);
                    }
                }
            });

            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(pane, BorderLayout.NORTH);
            jPanel.add(canbusPanel,BorderLayout.WEST);
            jPanel.add(addButton, BorderLayout.CENTER);
            jPanel.add(removeButton, BorderLayout.EAST);
            add(jPanel,BorderLayout.SOUTH);
        }
    }
}


