package dao;



import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robinpipirs on 16/03/16.
 */
public class SerialCommunicationService implements Runnable {

    private static InputStream in;
    private static OutputStream sos;
    private String lastPortName = null;
    private static SerialPort comPort;
    private int baudRate;
    private String canbusString = "";
    private String[] sendMessage = {"01", "20", "80", "35", "42", "9D", "20", "59", "60", "00", "AA"};
    private boolean sendMessageNow = false;
    boolean running;
    private List<CommunicationObserver> observers = new ArrayList<>();
    private CanbusMessage message;
    private CanbusMessage canbusMessage;
    private CanbusLogFileParser canbusLogFileParser = new CanbusLogFileParser();

    SerialCommunicationService(int baudRate)
    {
    		this.baudRate = baudRate;
    }
    
    public void Disconnect() {
    		closePort();
    }

    public OutputStream getOutPutStream(){
        return sos;
    }

    public void setWriteMessage(String[] sendMessage){
        this.sendMessage = sendMessage;

        for (int i = 0; i < sendMessage.length; i++){
            System.out.print(sendMessage[i]+" ");
        }
        System.out.println("");
        sendMessageNow = true;
    }

    public CanbusMessage getMessage() {
        return message;
    }

    public void setMessage(CanbusMessage message) {
        this.message = message;
        notifyAllObservers();
     }

    public void attach(CommunicationObserver observer){
        observers.add(observer);
    }

    public void notifyAllObservers(){
        for (CommunicationObserver observer : observers) {
            observer.update();
        }
    }
    
    public void start(String portName) {
		
		try {
			// TODO: fix this§
			comPort = null;
			comPort = createPort(portName);
			boolean isOpesn = comPort.isOpen();
			
			if(comPort == null) {
				System.out.println("error finding port");
				return;
			}
			
			boolean isOpen = comPort.openPort();
		    System.out.println("port open?.."+ isOpen);
			
			//in = comPort.getInputStream();
			
			comPort.addDataListener(new SerialPortDataListener() {
				
				@Override
				public void serialEvent(SerialPortEvent event) {
					// TODO Auto-generated method stub
					int avail = event.getSerialPort().bytesAvailable();
			        if (avail == 0) {
			            return;
			        }
			        String strRead;
			        try {
			        		byte[] newData = new byte[comPort.bytesAvailable()];
			            int numRead = comPort.readBytes(newData, newData.length);
			           // System.out.println("Read " + numRead + " bytes.");

			            	for (byte b : newData) {
			            		char c = (char) b;
						
			            		if (c != '\n')
			    	            {
			    	                canbusString = canbusString + String.valueOf(c);
			    	            }
			    	            else {
			    	                //System.out.println("debug: "+canbusString);
			    	                canbusMessage = canbusLogFileParser.ParseCanBusStringFromInputSerial(canbusString);
			    	                //System.out.println("parsed message: " +canbusMessage);
			    	                if ((canbusMessage != null) && !canbusMessage.getId().equals("TO")){
			    	                    setMessage(canbusMessage);
			    	                }
			    	                //System.out.println("can: "+canbusString);
			    	                canbusString = "";
			    	            }
			            		
			            	}
			            
			            }
			        catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    
				}
				
				@Override
				public int getListeningEvents() {
					// TODO Auto-generated method stub
					return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
				}
			});
			
			
			
			//sos = comPort.getOutputStream();
			
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		running = true;
	
    }
    
//    public void serialEvent(SerialPortEvent event) {
//        int avail = event.getSerialPort().bytesAvailable();
//        if (avail == 0) {
//            return;
//        }
//       // char c = (char)in.read();
//        try {
//        	
//        		byte[] newData = new byte[comPort.bytesAvailable()];
//            int numRead = comPort.readBytes(newData, newData.length);
//            System.out.println("Read " + numRead + " bytes.");
//        	
//        	
////            char c = (char)in.read();
////
////            if (c != '\n')
////            {
////                canbusString = canbusString + String.valueOf(c);
////            }
////            else {
////                //System.out.println("debug: "+canbusString);
////                canbusMessage = canbusLogFileParser.ParseCanBusStringFromInputSerial(canbusString);
////                //System.out.println("parsed message: " +canbusMessage);
////                if ((canbusMessage != null) && !canbusMessage.getId().equals("TO")){
////                    setMessage(canbusMessage);
////                }
////                //System.out.println("can: "+canbusString);
////                canbusString = "";
////            }
////            //Raw: 01 20 80 35 42 9D 20 59 60 00 (AA 0A) = 11 bytes
////            Thread.sleep(1);
////            
//            
//            
//            
//            }
//        		catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
    
    public SerialPort createPort(String portName) {

        SerialPort port = null;
        for (SerialPort p : SerialPort.getCommPorts()) {
        		String systemPortName = p.getSystemPortName();
            if (portName.equals(systemPortName)) {
            		port = p;
                port.setComPortParameters(115200, 8, 1, SerialPort.NO_PARITY);
                break;
            }
        }
        return port;
    }
    
    public void closePort() {
        if (comPort != null) {
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            comPort.removeDataListener();
            System.out.println("Going to close the port...");
            boolean result = comPort.closePort();
            System.out.println("Port closed? .."+ result);
        }
    }
     
    
    public void stop() {
    		running = false;
    		try {
    		
			in.close();
			//sos.close();
			in = null;
			sos = null;
			closePort();
    			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
   

    @Override
    public void run() {

        try
        {
            while(true){
            	Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
            comPort.closePort();
        }   
    }

	public SerialPort[] getComports() {
		
		// TODO Auto-generated method stub
		return comPort.getCommPorts();
	}

}
//sos.write(new byte[]{
//        (byte)(Integer.parseInt("01",16) & 0xff),
//        (byte)(Integer.parseInt("20",16) & 0xff),
//        (byte)(Integer.parseInt("80",16) & 0xff),
//        (byte)(Integer.parseInt("35",16) & 0xff),
//        (byte)(Integer.parseInt("42",16) & 0xff),
//        (byte)(Integer.parseInt("9D",16) & 0xff),
//        (byte)(Integer.parseInt("20",16) & 0xff),
//        (byte)(Integer.parseInt("59",16) & 0xff),
//        (byte)(Integer.parseInt("60",16) & 0xff),
//        (byte)(Integer.parseInt("00",16) & 0xff),
//        (byte)(Integer.parseInt("AA",16) & 0xff),
//        (byte)(Integer.parseInt("0A",16) & 0xff)
//        });