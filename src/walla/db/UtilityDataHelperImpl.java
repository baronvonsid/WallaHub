package walla.db;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.*;

import org.springframework.http.HttpStatus;

@Repository
public class UtilityDataHelperImpl implements UtilityDataHelper{

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(UtilityDataHelperImpl.class);
	
	public List<Platform> GetPlatformObjects() throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT [PlatformId],[Name],[ThumbHeight],[ThumbWidth],[ViewHeight],[ViewWidth],[ListNumber],[Supported] FROM [Platform]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<Platform> platformList = new ArrayList<Platform>();
			
			while (resultset.next())
			{
				Platform platform = new Platform();
				platform.setPlatformId(resultset.getInt(1));
				platform.setListNumber(resultset.getInt(7));
				platformList.add(platform);
			}
			resultset.close();
			
			if (platformList.size() < 1)
			{
				String error = "GetPlatformObjects didn't return any records";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetPlatformObjects", error, 0);
			}
			
			return platformList;
			
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetPlatformObjects", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetPlatformObjects", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	public long GetNewId(String idType) throws WallaException
	{
		Connection conn = null;
		CallableStatement idSproc = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			
			String sprocSql = "EXEC [dbo].[GetId] ?, ?";
			
		    idSproc = conn.prepareCall(sprocSql);
		    idSproc.setString(1, idType);
		    idSproc.registerOutParameter(2, Types.INTEGER);
		    idSproc.execute();
		    
		    long newTagId = idSproc.getLong(2);
		    if (newTagId > 0)
		    {
		    	return newTagId;
		    }
		    else
		    {
		    	String error = "GETID sproc didn't return a positive number";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetNewId", error, 0);
		    }
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetNewId", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetNewId", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (idSproc != null) try { idSproc.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}

	public UtilityDataHelperImpl() {
		meLogger.debug("UtilityDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
