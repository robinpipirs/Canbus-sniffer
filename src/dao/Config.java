package dao;

public class Config {
	private String ComportName;
	private String BaudRate;

	public String getBaudRate() {
		return BaudRate;
	}

	public String getComportName() {
		return ComportName;
	}
	
    // Getters and setters are not required for this example.
    // GSON sets the fields directly using reflection.

    @Override
    public String toString() {
        return ComportName + " - " + BaudRate;
    }
    
    
}