package walla.db;

import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.java.Platform;
import walla.utils.WallaException;

public interface UtilityDataHelper {

	public List<Platform> GetPlatformObjects() throws WallaException;
	public long GetNewId(String idType) throws WallaException;
	
	public void setDataSource(DataSource dataSource);
	
	
}
