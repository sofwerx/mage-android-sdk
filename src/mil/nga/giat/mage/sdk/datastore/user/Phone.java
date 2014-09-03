package mil.nga.giat.mage.sdk.datastore.user;

import java.io.Serializable;

public class Phone implements Serializable {
	private String number;
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
}
