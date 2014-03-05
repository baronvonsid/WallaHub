package walla.db;

import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.java.App;
import walla.datatypes.java.Platform;
import walla.utils.WallaException;

public interface UtilityDataHelper {

	public List<Platform> GetPlatformList() throws WallaException;
	public List<App> GetAppList() throws WallaException;
	public long GetNewId(String idType) throws WallaException;
	public String GetString(String sql) throws WallaException;
	public int GetInt(String sql) throws WallaException;
	
	public void setDataSource(DataSource dataSource);
	
	
}
