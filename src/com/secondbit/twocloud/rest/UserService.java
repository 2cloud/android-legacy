package com.secondbit.twocloud.rest;

import android.app.IntentService;
import android.content.Intent;

import com.secondbit.twocloud.util.RequestHelper;

public class UserService extends IntentService {
	
	private static final String REST_BASE = "/users/";
	
	public UserService() {
		super("UserService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		String host = intent.getStringExtra(RequestHelper.INTENT_EXTRA_HOST);
		String query = intent.getStringExtra(RequestHelper.INTENT_EXTRA_QUERY);
		if(RequestHelper.REST_GET.equals(action)) {
			UserGetRequest request = getRequest(host, query);
		}
	}
	
	private UserGetRequest getRequest(String host, String query) {
		String urlString = _buildGetRequestUrl(host, query);
		UserGetRequest request = new UserGetRequest(urlString);
		return request;
	}
	
	private String _buildGetRequestUrl(String host, String query) {
		host = RequestHelper.sanitiseHost(host);
		return host + REST_BASE + query;
	}
}
