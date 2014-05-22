package walla.datatypes.java;

public class Style {

	private int styleId;
	private String name;
	private String desc;
	private String cssFolder;
	
	public Style() {
		// TODO Auto-generated constructor stub
	}
	
	public void setStyleId(int styleId)
	{
		this.styleId = styleId;
	}
	
	public int getStyleId()
	{
		return this.styleId;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setDesc(String desc)
	{
		this.desc = desc;
	}
	
	public String getDesc()
	{
		return this.desc;
	}
	
	public void setCssFolder(String cssFolder)
	{
		this.cssFolder = cssFolder;
	}
	
	public String getCssFolder()
	{
		return this.cssFolder;
	}
}
