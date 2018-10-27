package dao;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.util.*;

import java.util.List;

import com.fazecast.jSerialComm.*;
import com.google.gson.*;
import com.sun.crypto.provider.AESParameters;

import javax.swing.*;
import javax.swing.event.ListDataListener;
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
                .ParseCanBusLog("/filters.asc");

        List<CanbusMessage> messagesToFilter = new ArrayList<CanbusMessage>(canBusMessagesToFilter);
        Set<CanbusMessage> hs = new HashSet<>();
        hs.addAll(messagesToFilter);
        messagesToFilter.clear();
        messagesToFilter.addAll(hs);
        
        /** Setting up SerialcomService **/
        SerialCommunicationService scs = null;
        
        scs  = new SerialCommunicationService(Integer.parseInt(config.getBaudRate()));
         
        SerialPort serials[] = scs.getComports();
       // SerialPort serials[] = new SerialPort[5];
        /** things happening in the gui here **/
        CanbusDataView canBusView = new CanbusDataView();
        new LiveCommunicationObserver(scs, canBusView.getView());
        
        SerialPortView scsView = new SerialPortView(scs);
        scsView.updatePorts(serials);
        
        
        
        
        // TODO: do this
       // new ScsPortObserver(scs, scsView.getView());
        

        

        ArrayList<CanbusMessage> emptyList = new ArrayList<>();
//        ListFilter listFilter = new ListFilter(messagesToFilter);
        ListFilter listFilter = new ListFilter(emptyList);
        

        FilterListModel fm = new FilterListModel(listFilter);
        
        FilterManager filterManager = new FilterManager(canBusView.getListModel());
        filterManager.setFilter(listFilter);
        
        new FilterCommunicationObserver(scs,canBusView.getFilterView(), canBusView.getListModel()).setFilterManager(filterManager);
        final Thread thread = new Thread(scs);
        thread.start();

        
        AddSelectionModel adsm = new AddSelectionModel(canBusView.getList() ,canBusView.getListModel(), fm.getListModel(), listFilter);
        AddCounterModel adcm = new AddCounterModel(); 
        filterManager.setCounter(adcm.getCounter());
        
        /** Gui is built here**/

        JFrame mainJFrame;
        mainJFrame = new JFrame();
        mainJFrame.setLayout(new BorderLayout());
        mainJFrame.setTitle("Pipirs Solutions canbus sniffer");
        mainJFrame.setBackground(Color.BLUE);

        mainJFrame.add(scsView,BorderLayout.NORTH);
        mainJFrame.add(canBusView,BorderLayout.WEST);
        JPanel messageAndFilterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 0.3;
        c.weightx = 0.3;
  //      c.insets = new Insets(10,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
     //   messageAndFilterPanel.add(new SendMessageModel(scs),BorderLayout.NORTH);
        messageAndFilterPanel.add(adsm, c);
        c.fill = GridBagConstraints.HORIZONTAL;  
        c.gridx = 0;
        c.gridy = 1;
        
        messageAndFilterPanel.add(adcm, c);
        
      //  c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 0;
        c.gridy =2;
        
        messageAndFilterPanel.add(fm,c);
        mainJFrame.add(messageAndFilterPanel, BorderLayout.EAST);
        mainJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainJFrame.pack();
        mainJFrame.setVisible(true);

        /** connecting ui components to the services**/
    }
    
    public static class AddCounterModel extends JPanel {
    	
    		private JTextField cntr = new JTextField(10);
    	
	    	public JTextField getCounter() {
				return cntr;
		}
	    	
    		public AddCounterModel() {
	    		JPanel panel = new JPanel(new BorderLayout());
	    
	    		BorderLayout bl = new BorderLayout();
	    		panel.add(cntr, BorderLayout.CENTER );
	    		
	    		cntr.setSize(5, 15);
	        cntr.setSize(5, 15);
	           
            add(panel);
    		
    		}
     
    }
    
    public static class AddSelectionModel extends JPanel {
    	
    		private JButton copyBtn = new JButton(">>");
    		
		private DefaultListModel<CanbusMessage>  m1;
    		private DefaultListModel<CanbusMessage>  m2;
    		private JList l1;
    		private ListFilter lf;
    		
    		public AddSelectionModel(JList jList, DefaultListModel  messageModel, DefaultListModel filterModel, ListFilter listFilter) {
    			
    			m1 = messageModel;
    			m2 = filterModel;
    			l1 = jList;
    			lf = listFilter;
    			
    			
    			JPanel panel = new JPanel(new BorderLayout());
    			
    				//copyBtn.setBounds(x, y, width, height);
    			

                copyBtn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    
                    	int[] indexes = jList.getSelectedIndices();
                    	for (int index : indexes) {
							filterModel.addElement(messageModel.getElementAt(index));

							String[] canStringArray = messageModel.getElementAt(index).toString().split(" ");			
							String tmpId = canStringArray[1];
									
							ArrayList<String> tmpData = new ArrayList<String>();
							
							for (int i = 3; i < canStringArray.length; i++) {
								 tmpData.add(canStringArray[i]);
							
							}
							
							String[] dataArray = new String[tmpData.size()];
							dataArray = tmpData.toArray(dataArray);
							
							CanbusMessage cm = new CanbusMessage("1234", tmpId, dataArray);
							
							listFilter.addToList(cm);
							
						}
                    }
                });
                
                copyBtn.setSize(15, 15);
                
                BorderLayout bl = new BorderLayout();
                panel.add(copyBtn, BorderLayout.WEST);
                
                
                BorderLayout layout = new BorderLayout();
                layout.setVgap(5);
                layout.setHgap(5);
                setLayout(layout);
          
                add(panel);
    		}
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
        
        JList listFiltered;
        DefaultListModel model;

        public JTextArea getView(){
            return jta;
        }
        public JTextArea getFilterView(){
          return jtaFiltered;
        }
        
        public JList getList(){
            return listFiltered;
        }
        
        public DefaultListModel getListModel() {
    		return model;
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

            model = new DefaultListModel();
            listFiltered = new JList(model);

            JScrollPane paneFiltered = new JScrollPane(listFiltered,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            paneFiltered.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
				
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					// TODO Auto-generated method stub
					e.getAdjustable().setValue(e.getAdjustable().getMaximum()); 
				}
			});
            
            paneFiltered.setPreferredSize(new Dimension(400, 400));
            
            JPanel jView = new JPanel(new BorderLayout());
            jView.add(sp, BorderLayout.WEST);
            jView.add(new JPanel(), BorderLayout.CENTER);
            //jView.add(spFiltered, BorderLayout.EAST);
            jView.add(paneFiltered, BorderLayout.EAST);

            add(jView);
        }
    }
    
    public static class SerialPortView extends JPanel {
    	
    		private SerialCommunicationService scs;
        private JComboBox<String> comboBox;
        private JButton connBtn = new JButton("Connect!");
        private JButton discBtn = new JButton("Disconnect!");
        private DefaultListModel<String> model;
        
        public JComboBox<String> getComportView(){
            return comboBox;
        }

        public void updatePorts(SerialPort[] serials){
        	
        		ArrayList<String> options = new ArrayList<String>();
        
    			//comboBox.removeAll();
        		for (SerialPort serial : serials) { 
        			if(serial != null) {

            			options.add(serial.getSystemPortName()); 
        			}
        			}
      		
        		String[] stockArr = new String[options.size()];
        		stockArr = options.toArray(stockArr);
        		
        		
        		JComboBox<String> tempModel = new JComboBox<>(stockArr);
        		ComboBoxModel<String> tm = tempModel.getModel();
        		comboBox.setModel(tm);
        		
        }
        
        public SerialPortView(SerialCommunicationService serialCommunicationService) {
            
        		scs = serialCommunicationService;
	        	comboBox = new JComboBox<String>(new String[] {""}) ;
	        	comboBox.setBounds(315, 47, 150, 25);
        		
            JPanel jView = new JPanel(new BorderLayout());
            jView.add(comboBox, BorderLayout.WEST);
            connBtn.setPreferredSize(new Dimension(80,25));
            connBtn.setPreferredSize(new Dimension(80,25));
            
            connBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	//comboBox.getSelectedItem().toString()
                		scs.start(comboBox.getSelectedItem().toString());
                }
            });
            
            discBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                   scs.Disconnect();
                }
            });
            
            jView.add(connBtn, BorderLayout.CENTER);
            jView.add(discBtn, BorderLayout.EAST);
            add(jView);
        }
    }


    public static class FilterListModel extends JPanel {

        JList list;
        DefaultListModel model;
        CanbusLogFileParser canbusLogFileParser = new CanbusLogFileParser();
        private List<CanbusMessage> canList;
        private ListFilter listFilter;

        public DefaultListModel<CanbusMessage> getListModel() {
        		return model;
        }
        
        public FilterListModel(ListFilter listFilter) {
        	
            this.listFilter = listFilter;
            setLayout(new BorderLayout());
            model = new DefaultListModel<CanbusMessage>();
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
                     //   System.out.println(canString);
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


