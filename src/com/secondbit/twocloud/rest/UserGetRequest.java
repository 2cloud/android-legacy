package com.secondbit.twocloud.rest;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;

public class UserGetRequest extends HttpGet {

	public UserGetRequest() {
		// TODO Auto-generated constructor stub
	}

	public UserGetRequest(URI uri) {
		super(uri);
		// TODO Auto-generated constructor stub
	}

	public UserGetRequest(String uri) {
		super(uri);
		// TODO Auto-generated constructor stub
	}

}
