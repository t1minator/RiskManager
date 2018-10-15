package com.broyles.riskmanager;

import java.util.ArrayList;
import java.util.List;

public class Config {
	
	private static final Config instance = new Config();
	private Config() {};
	public static Config getInstance() {
		if (accounts == null) {
			accounts = new ArrayList<AccountConstants>(); 
			
			AccountConstants a1 = new AccountConstants();
			a1.setURL("https://api-fxpractice.oanda.com");
			a1.setTOKEN("298a7580728ba6089ae899950911bec9-ae5861556d83f8ef7f4a6252e95601ec");
			AccountConstants a2 = new AccountConstants();
			a2.setURL("https://api-fxtrade.oanda.com");
			a2.setTOKEN("cd9fd91c7be925dc3ba6a2b96c938f07-a0208998c370f24817c00a0200ba7cc7");
			
			accounts.add(a2);
			accounts.add(a1);			
			
		}
		
		return instance;
	}
	public List<AccountConstants> getAccounts() {
		return accounts;
	}
	
	private static List<AccountConstants> accounts = null;
   
    
}
// prod
// cd9fd91c7be925dc3ba6a2b96c938f07-a0208998c370f24817c00a0200ba7cc7
//	https://api-fxtrade.oanda.com
