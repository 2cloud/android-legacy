package com.secondbit.twocloud.util;

import java.util.List;

import org.apache.http.NameValuePair;

import android.net.Uri;

public class RequestHelper {

	public static final String QUERY_PREPEND = "?";
	public static final String QUERY_JOIN = "&";
	public static final String QUERY_ASSIGN = "=";
	
	public static final String REST_GET = "GET";
	public static final String REST_POST = "POST";
	public static final String REST_DELETE = "DELETE";
	public static final String REST_PUT = "PUT";
	
	public static final String INTENT_EXTRA_HOST = "host";
	public static final String INTENT_EXTRA_QUERY = "query";
	
	public static String buildQueryString(List<NameValuePair> params) {
		if(params == null || params.size() == 0) {
			return "";
		}
		String query = QUERY_PREPEND;
		int size = params.size();
		for(int index=0; index < size; index++) {
			NameValuePair param = params.get(index);
			query += Uri.encode(param.getName()) + QUERY_ASSIGN + Uri.encode(param.getValue()) + QUERY_JOIN;
		}
		if(query.endsWith(QUERY_JOIN)) {
			query = query.substring(0, query.length() - 1);
		}
		return query;
	}
	
	public static String sanitiseHost(String host) throws NullPointerException {
		if(host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}
		return host;
	}
}