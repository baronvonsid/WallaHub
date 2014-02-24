package walla.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface AccountDataHelper {

	public long CreateAccount(Account newAccount) throws WallaException;
	public Account GetAccount(long userId) throws WallaException;
	public boolean CheckProfileNameIsUnique(String profileName) throws WallaException;
	public UserApp GetUserApp(long userId, long userAppId) throws WallaException;
	

}
