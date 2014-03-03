package walla.datatypes.java;

public class Platform {

	private int platformId;
	private String operatingSystem;
	private String machineType;
	private String shortName;
	private boolean supported;
	
	public Platform() {
		// TODO Auto-generated constructor stub
	}

	public void setPlatformId(int platformId)
	{
		this.platformId = platformId;
	}
	
	public int getPlatformId()
	{
		return this.platformId;
	}
	
	public void setOperatingSystem(String operatingSystem)
	{
		this.operatingSystem = operatingSystem;
	}
	
	public String getOperatingSystem()
	{
		return this.operatingSystem;
	}
	
	public void setMachineType(String machineType)
	{
		this.machineType = machineType;
	}
	
	public String getMachineType()
	{
		return this.machineType;
	}
	
	public void setShortName(String shortName)
	{
		this.shortName = shortName;
	}
	
	public String getShortName()
	{
		return this.shortName;
	}
	
	public void setSupported(boolean supported)
	{
		this.supported = supported;
	}
	
	public boolean getSupported()
	{
		return this.supported;
	}
}
