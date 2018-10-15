package com.broyles.riskmanager;

import com.oanda.v20.account.AccountID;
import com.oanda.v20.primitives.InstrumentName;

public class AccountConstants {

	    private String URL = "https://api-fxpractice.oanda.com";
	    private String TOKEN = "298a7580728ba6089ae899950911bec9-ae5861556d83f8ef7f4a6252e95601ec";
	    private AccountID ACCOUNTID = new AccountID("101-001-7063324-001");
	    private InstrumentName INSTRUMENT  = new InstrumentName("EUR_USD");
	    
		public String getURL() {
			return URL;
		}
		public void setURL(String uRL) {
			URL = uRL;
		}
		public String getTOKEN() {
			return TOKEN;
		}
		public void setTOKEN(String tOKEN) {
			TOKEN = tOKEN;
		}
		public AccountID getACCOUNTID() {
			return ACCOUNTID;
		}
		public void setACCOUNTID(AccountID aCCOUNTID) {
			ACCOUNTID = aCCOUNTID;
		}
		public InstrumentName getINSTRUMENT() {
			return INSTRUMENT;
		}
		public void setINSTRUMENT(InstrumentName iNSTRUMENT) {
			INSTRUMENT = iNSTRUMENT;
		}
}
