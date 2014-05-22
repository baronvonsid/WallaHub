package walla.datatypes.java;

public class Presentation {

	private int presentationId;
	private String name;
	private String desc;
	private String jspName;
	private String cssExtension;
	private int maxSections;
	
	public Presentation() {
		// TODO Auto-generated constructor stub
	}
	
	public void setPresentationId(int presentationId)
	{
		this.presentationId = presentationId;
	}
	
	public int getPresentationId()
	{
		return this.presentationId;
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
	
	public void setJspName(String jspName)
	{
		this.jspName = jspName;
	}
	
	public String getJspName()
	{
		return this.jspName;
	}
	
	public void setCssExtension(String cssExtension)
	{
		this.cssExtension = cssExtension;
	}
	
	public String getCssExtension()
	{
		return this.cssExtension;
	}
	
	public void setMaxSections(int maxSections)
	{
		this.maxSections = maxSections;
	}
	
	public int getMaxSections()
	{
		return this.maxSections;
	}
}
