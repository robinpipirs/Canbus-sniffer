package dao;


import java.util.Arrays;

/**
 * Created by robinpipirs on 10/03/16.
 */
public class CanbusMessage {

    private String timestamp;
    private String id;
    private String[] data;

    public CanbusMessage(String timestamp, String id, String[] data) {
        this.timestamp =timestamp;
        this.id =id;
        this.data=data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CanbusMessage that = (CanbusMessage) o;

        if (!id.equals(that.id)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(data, that.data);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "CanbusMessage{" +
                "timestamp='" + timestamp + '\'' +
                ", id='" + id + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public String canbusHexString(){
        String dataString = "";
        for (String s:data
             ) {
            dataString=dataString+" "+s;

        }
        return "0"+id.substring(0,1)+" "+id.substring(1,3)+dataString +" AA 0A";

    }

    public String toListString() {
        String hexString = "";
        for (int i =0; i < data.length; i++){
            hexString = hexString + data[i] + " ";
        }
        return "ID: " + id + " " + "Data: " + hexString;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public String[] getData() {
        return data;
    }
}
