package com.secondbit.twocloud.test.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.secondbit.twocloud.util.RequestHelper;

public class RequestHelperTest extends TestCase {
	
	public TestResult run() {
		return new TestResult();
	}
	
	protected void setUp() {
		
	}
	
	protected void tearDown() {
		
	}
	
	public void testConstants() {
		assertNotNull(RequestHelper.REST_DELETE);
		assertNotNull(RequestHelper.REST_GET);
		assertNotNull(RequestHelper.REST_POST);
		assertNotNull(RequestHelper.REST_PUT);
		
		assertNotNull(RequestHelper.QUERY_ASSIGN);
		assertNotNull(RequestHelper.QUERY_JOIN);
		assertNotNull(RequestHelper.QUERY_PREPEND);

		assertNotNull(RequestHelper.INTENT_EXTRA_HOST);
		assertNotNull(RequestHelper.INTENT_EXTRA_QUERY);
	}
	
	public void testSingleParamBuildQueryString() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name1", "value1"));
		String queryString = RequestHelper.buildQueryString(params);
		assertEquals("?name1=value1", queryString);
	}
	
	public void testDoubleParamBuildQueryString() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name1", "value1"));
		params.add(new BasicNameValuePair("name2", "value2"));
		String queryString = RequestHelper.buildQueryString(params);
		assertEquals("?name1=value1&name2=value2", queryString);
	}
	
	public void testEscapeCharactersBuildQueryString() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ampersandValue", "special&character"));
		params.add(new BasicNameValuePair("special&character", "ampersandName"));
		params.add(new BasicNameValuePair("equalsValue", "special=character"));
		params.add(new BasicNameValuePair("special=character", "equalsName"));
		params.add(new BasicNameValuePair("spaceValue", "special character"));
		params.add(new BasicNameValuePair("special character", "spaceName"));
		params.add(new BasicNameValuePair("percentValue", "special%character"));
		params.add(new BasicNameValuePair("special%character", "percentName"));
		params.add(new BasicNameValuePair("hashValue", "special#character"));
		params.add(new BasicNameValuePair("special#character", "hashName"));
		params.add(new BasicNameValuePair("questionValue", "special?character"));
		params.add(new BasicNameValuePair("special?character", "questionName"));
		String queryString = RequestHelper.buildQueryString(params);
		String escapedQueryString = "?ampersandValue=special%26character&special%26character=ampersandName&equalsValue=special%3Dcharacter&special%3Dcharacter=equalsName&spaceValue=special%20character&special%20character=spaceName&percentValue=special%25character&special%25character=percentName&hashValue=special%23character&special%23character=hashName&questionValue=special%3Fcharacter&special%3Fcharacter=questionName";
		assertEquals(queryString, escapedQueryString);
	}
}
