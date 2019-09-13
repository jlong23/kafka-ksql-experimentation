package com.nationwide.nf.ips.streaming.util;

import org.w3c.dom.Document;

public class XMLBusinessKeyExtractor2 {

	public static final String KEY_SEPERATOR = ",";

	// These make the Message Unique
	public static final String XPATH_MESSAGE_ID = "//messageHeader/messageID/text()";
	public static final String XPATH_TRANSACTION_NAME = "//messageHeader/TransactionName/text()";

	//
	// Index Values for Message Selection
	//
	// The OIPA Activity GUID ( 738C8B64-4970-489B-8DB5-5FCC3121DE13 )
	public static final String XPATH_ACTIVITY_GUID = "//AsActivity/Field/FieldName[text()='ActivityGUID']/parent::Field//following-sibling::FieldValue/text()";

	// The Policy Number
	public static final String XPATH_POLICYNUMBER = "//AsPolicy/Field/FieldName[text()='PolicyNumber']/parent::Field//following-sibling::FieldValue/text()";

	// The Activity Effective Date ( 2019-01-25 02:20:13.819 )
	public static final String XPATH_EFFECTIVE_DATE = "//AsActivity/Field/FieldName[text()='EffectiveDate']/parent::Field//following-sibling::FieldValue/text()";

	// The Activity Process Date ( 2019-01-25 02:20:13.819 )
	public static final String XPATH_PROCESS_DATE = "//AsActivity/Field/FieldName[text()='ProcessDate']/parent::Field//following-sibling::FieldValue/text()";

	// Find the Numeric Product Identifier ( 11 )
	public static final String XPATH_PRODUCT_ID = "//AsPlan/Field/FieldName[text()='ProductIdentifier']/parent::Field//following-sibling::FieldValue/text()";

	// Find Mailing Address
	public static final String XPATH_MAILING_STATECODE = "//AsAddress[Field[FieldName[text()='AddressGUID'] and FieldValue[text() = //AsRole[Field[FieldValue[text()='Owner']] and Field[FieldName/text() = 'CorrespondenceContactFlag' and FieldValue/text() = 'CHECKED']]/Field[FieldName/text()='AddressGUID']/FieldValue/text()]]]/Field[FieldName[text()='StateCode']]/FieldValue/text()";

	private XMLUtility xmlUtil;

	public XMLBusinessKeyExtractor2(XMLUtility xmlUtil) {
		this.xmlUtil = xmlUtil;
	}

	public String getKeyFromXml(Document doc) {
		StringBuilder resultkey = new StringBuilder();

		resultkey.append(xmlUtil.getFirstDocumentElementTextByXPath(doc, XPATH_TRANSACTION_NAME));
		resultkey.append(KEY_SEPERATOR);

		resultkey.append(xmlUtil.getFirstDocumentElementTextByXPath(doc, XPATH_PROCESS_DATE));
		resultkey.append(KEY_SEPERATOR);

		resultkey.append(xmlUtil.getFirstDocumentElementTextByXPath(doc, XPATH_ACTIVITY_GUID));
		resultkey.append(KEY_SEPERATOR);

		resultkey.append(xmlUtil.getFirstDocumentElementTextByXPath(doc, XPATH_POLICYNUMBER));
		resultkey.append(KEY_SEPERATOR);

		resultkey.append(xmlUtil.getFirstDocumentElementTextByXPath(doc, XPATH_PRODUCT_ID));
		resultkey.append(KEY_SEPERATOR);

		resultkey.append(xmlUtil.getFirstDocumentElementTextByXPath(doc, XPATH_MAILING_STATECODE));

		return resultkey.toString();
	}
}
