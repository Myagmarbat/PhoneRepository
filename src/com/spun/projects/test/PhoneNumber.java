package com.spun.projects.test;

import java.util.*;

/**
 * Phone number validation, and formatter. this class is immutable.
 **/
public class PhoneNumber {
	public static final int USA = 0;
	public static final String[] COUNTRY_CODES = { "1", "20", "212", "213", "216", "218", "220", "221", "222", "223",
			"224", "225", "226", "227", "228", "229", "230", "231", "232", "233", "234", "235", "236", "237", "238",
			"239", "240", "241", "242", "243", "244", "245", "246", "247", "248", "249", "250", "251", "252", "253",
			"254", "255", "256", "257", "258", "260", "261", "262", "263", "264", "265", "266", "267", "268", "269",
			"27", "290", "291", "297", "298", "299", "30", "31", "32", "33", "34", "350", "351", "352", "353", "354",
			"355", "356", "357", "358", "359", "36", "370", "371", "372", "373", "374", "375", "376", "377", "378",
			"380", "381", "385", "386", "387", "389", "39", "40", "41", "420", "421", "423", "43", "44", "45", "46",
			"47", "48", "49", "500", "501", "502", "503", "504", "505", "506", "507", "508", "509", "51", "52", "53",
			"5399", "54", "55", "56", "57", "58", "590", "591", "592", "593", "594", "595", "596", "597", "598", "599",
			"60", "61", "618", "62", "63", "64", "65", "66", "670", "672", "673", "674", "675", "676", "677", "678",
			"679", "680", "681", "682", "683", "684", "685", "686", "687", "688", "689", "690", "691", "692", "7",
			"808", "81", "82", "84", "850", "852", "853", "855", "856", "86", "870", "871", "872", "873", "874", "878",
			"880", "881", "8816", "8817", "88213", "88216", "886", "90", "91", "92", "93", "94", "95", "960", "961",
			"962", "963", "964", "965", "966", "967", "968", "970", "971", "972", "973", "974", "975", "976", "977",
			"98", "992", "993", "994", "995", "996", "998" };

	private static final String REASONS[] = { "Phone Number Too Long or Too Short", "US Number must be length 10",
			"Unknown Country Code" };

	/** The original value. */
	private String originalValue = null;

	// An index into the COUNTRY_CODES array
	private int countryCodeIndex = USA;
	private String strippedValue = null;

	private String invalidReason = null;

	public PhoneNumber(String originalValue) {
		this.originalValue = originalValue;
		// in this method I also set right country code index into countryCodeIndex variable
		// It's not possible to seperate this logic, because we can't get countrycode from strippedNumber
		this.strippedValue = generateStrippedValue(originalValue);
	}

	private String generateStrippedValue(String originalValue) {
		// 1.1 all letter in lower case
	    String number = originalValue.toLowerCase();
	    // 1.2 change all ext to x
		number = number.replace("ext", "x");
		char[] chs = number.toCharArray();
		StringBuilder sb = new StringBuilder();
		// 1.3 ban all occurence
		Set<Character> banned = new HashSet<Character>(Arrays.asList('(', ')', '.', ' ', '-'));
		// 1.4 ban more than one occurence
		List<Character> bannedPlus = new ArrayList<>(Arrays.asList('+', 'x'));
		int[] bannedPlusCount = new int[bannedPlus.size()];

		for(char c: chs) {
			if(banned.contains(c)) continue;
			if(bannedPlus.contains(c)){
				if(bannedPlusCount[bannedPlus.indexOf(c)] == 0){
					bannedPlusCount[bannedPlus.indexOf(c)]++;
				} else {
					continue;
				}
			}
			sb.append(c);
		}
		// 2. decide country code
		String delims = "[ .,-/(]+";
		String[] tokens = originalValue.split(delims);
		String ccode = tokens[0];
		int index = ccode.indexOf("+");
		ccode = ccode.replace("+", "");

		int len = getNoExtensionLength(sb.toString());

		// I wanted to seperate this logic, but not possible get country code from already strippedNumber
		countryCodeIndex = getCountryIndex(ccode);
		if((len == 10 && countryCodeIndex == -1) || (len == 11 && ccode.charAt(0) == '1')) countryCodeIndex = USA;

		if(sb.charAt(0) != '+') {
			len = getNoExtensionLength(sb.toString());
			if (len <= 10){
				sb.insert(0, "+1");
			}
			else if (len > 10){
				sb.insert(0, "+");
			}
		}
		return sb.toString();
	}

	private int getCountryIndex(String ccode){
		for(int i = 0; i < COUNTRY_CODES.length; i++){
			if(ccode != null && COUNTRY_CODES[i].equals(ccode)){
				return i;
			}
		}
		this.invalidReason = REASONS[2];
		return -1;
	}
	/* get len till extension */
	private int getNoExtensionLength(String strippedNumber){
		int len = strippedNumber.length();
		if(strippedNumber.indexOf("x") != -1) len = strippedNumber.indexOf("x");
		return len;
	}

	private static int getStaticExtensionLength(String strippedNumber){
		int len = strippedNumber.length();
		if(strippedNumber.indexOf("x") != -1) len = strippedNumber.indexOf("x");
		return len;
	}

	/**************************************************************************/
	/* Reduce the string to just numbers */
	private static String stripPhoneNumber(String number) {
		StringBuilder sb = new StringBuilder();
		for(char c: number.toCharArray()){
			if(Character.isDigit(c))
				sb.append(c);
		}
		return sb.toString();
	}

	/**************************************************************************/
	/* It's not possible to getCountryCodeIndex from strippedNumber
	   Because we don't know how to where was the delimiiter in strippedNumber, we only know it using originalValue and
	   strippedNumber. So this method doesn't make sense.
	 */
	private static int getCountryCodeIndex(String strippedNumber) {
		return -1;
	}

	/**************************************************************************/
	private static String validate(int countryCodeIndex, String stripedNumber) {
		return (countryCodeIndex == USA) ? validateNorthAmerican(countryCodeIndex, stripedNumber)
				: validateInternational(countryCodeIndex, stripedNumber);
	}

	/**************************************************************************/
	/*
	 * International Phone number must be between 9-15 chars
	 */
	private static String validateInternational(int countryCodeIndex, String strippedNumber) {
		if(extractPhoneBody(countryCodeIndex, strippedNumber) == null) return REASONS[2];
		return extractPhoneBody(countryCodeIndex, strippedNumber).length() >= 9 &&  
				extractPhoneBody(countryCodeIndex, strippedNumber).length() <= 15 ? null : REASONS[0];
	}

	/**************************************************************************/
	private static String validateNorthAmerican(int countryCodeIndex, String strippedNumber) {
		if(extractPhoneBody(countryCodeIndex, strippedNumber) == null) return REASONS[2];
		return extractPhoneBody(countryCodeIndex, strippedNumber).length() == 12 ? null : REASONS[1];
	}

	/**************************************************************************/
	private static String extractPhoneBody(int countryCodeIndex, String strippedNumber) {
		return countryCodeIndex == USA ? getValueAsNorthAmerican(countryCodeIndex, strippedNumber) :
				getValueAsInternational(countryCodeIndex, strippedNumber);
	}

	/**************************************************************************/
	private static String getValueAsNorthAmerican(int countryCodeIndex, String stripped) {
		StringBuilder sb = new StringBuilder(stripped);
		if(sb.indexOf("+1") == 0) sb.delete(0, 2);
		if(getStaticExtensionLength(sb.toString()) != 10 ) return null;
		sb.insert(0, "(");
		sb.insert(4, ")");
		sb.insert(8, "-");
		return sb.toString();
	}

	/**************************************************************************/
	private static String getValueAsInternational(int countryCodeIndex, String stripped) {
		if(countryCodeIndex == -1) return null;
		StringBuilder sb = new StringBuilder(stripped);

		if(countryCodeIndex == USA){
			int idx = 2;
			sb.insert(idx, ".");
			sb.insert(idx + 4, ".");
			sb.insert(idx + 8, ".");
		} else {
			String cc = COUNTRY_CODES[countryCodeIndex];
			sb.insert(cc.length() + 1, ".");
			sb.insert(cc.length() + 5, ".");
			sb.insert(cc.length() + 9, ".");
		}
		return sb.toString();
	}

	/**************************************************************************/
	public String getValueAsNorthAmerican() {
		return getValueAsNorthAmerican(this.countryCodeIndex, this.strippedValue);
	}

	/**************************************************************************/
	public String getValueAsInternational() {
		return getValueAsInternational(this.countryCodeIndex, this.strippedValue);
	}

	/**************************************************************************/
	public boolean isValid() {
		this.invalidReason = validate(this.countryCodeIndex, this.strippedValue);
		return (this.invalidReason == null);
	}

	/**************************************************************************/
	public String getInvalidReason() {
		return this.invalidReason;
	}

	/**************************************************************************/

	public boolean isNorthAmericanNumber() {
		return this.countryCodeIndex == USA;
	}

	/**************************************************************************/

	public String getOriginalText() {
		return originalValue;
	}

	/***********************************************************************/
	public String getStrippedNumber() {
		return this.strippedValue;
	}
	/**************************************************************************/
	/**************************************************************************/
}