package mil.nga.giat.mage.sdk.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateUtility {

	public static final DateFormat getISO8601() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		df.setTimeZone(TimeZone.getTimeZone("Zulu"));
		return df;
	}
}
