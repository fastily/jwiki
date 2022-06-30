package io.github.fastily.jwiki.core;

import java.io.IOException;
import java.net.CookieManager;
import java.net.Proxy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Functions which perform {@code GET} and {@code POST} requests to the MediaWiki api and returns Response objects in a suitable format.
 * 
 * @author Fastily
 *
 */
class ApiClient
{
	/**
	 * MediaType for {@code application/octet-stream}.
	 */
	private static final MediaType octetstream = MediaType.parse("application/octet-stream");

	/**
	 * HTTP client used for all requests.
	 */
	protected final OkHttpClient client;

	/**
	 * The Wiki object tied to this ApiClient.
	 */
	private Wiki wiki;

	/**
	 * Constructor, create a new ApiClient for a Wiki instance.
	 * 
	 * @param wiki The Wiki object this ApiClient is associated with.
	 * @param proxy The proxy to use. Optional param - set null to disable.
	 * @param cookieManager The CookieManager to use. Optional param - set null to use a default CookieManager.
	 */
	protected ApiClient(Wiki wiki, Proxy proxy, CookieManager cookieManager)
	{
		this.wiki = wiki;

		OkHttpClient.Builder builder = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(cookieManager == null ? new CookieManager() : cookieManager)).readTimeout(2, TimeUnit.MINUTES);
		if (proxy != null)
			builder.proxy(proxy);

		client = builder.build();
	}

	/**
	 * Create a basic Request template which serves as the basis for any Request objects.
	 * 
	 * @param params Any URL parameters (not URL-encoded).
	 * @return A new Request.Builder with default values needed to hit MediaWiki API endpoints.
	 */
	private Request.Builder startReq(HashMap<String, String> params)
	{
		HttpUrl.Builder hb = wiki.conf.baseURL.newBuilder();
		params.forEach(hb::addQueryParameter);

		return new Request.Builder().url(hb.build()).header("User-Agent", wiki.conf.userAgent);
	}

	/**
	 * Basic {@code GET} to the MediaWiki api.
	 * 
	 * @param params Any URL parameters (not URL-encoded).
	 * @return A Response object with the result of this Request.
	 * @throws IOException Network error
	 */
	protected Response basicGET(HashMap<String, String> params) throws IOException
	{
		return client.newCall(startReq(params).get().build()).execute();
	}

	/**
	 * Basic form-data {@code POST} to the MediaWiki api.
	 * 
	 * @param params Any URL parameters (not URL-encoded).
	 * @param form The Key-Value form parameters to {@code POST}.
	 * @return A Response object with the result of this Request.
	 * @throws IOException Network error
	 */
	protected Response basicPOST(HashMap<String, String> params, HashMap<String, String> form) throws IOException
	{
		FormBody.Builder fb = new FormBody.Builder();
		form.forEach(fb::add);

		return client.newCall(startReq(params).post(fb.build()).build()).execute();
	}

	/**
	 * Performs a multi-part file {@code POST}.
	 * 
	 * @param params Any URL parameters (not URL-encoded).
	 * @param form The Key-Value form parameters to {@code POST}.
	 * @param fn The system name of the file to {@code POST}
	 * @param chunk The raw byte data associated with this file which will be sent in this {@code POST}.
	 * @return A Response with the results of this {@code POST}.
	 * @throws IOException Network error
	 */
	protected Response multiPartFilePOST(HashMap<String, String> params, HashMap<String, String> form, String fn, byte[] chunk) throws IOException
	{
		MultipartBody.Builder mpb = new MultipartBody.Builder().setType(MultipartBody.FORM);
		form.forEach(mpb::addFormDataPart);

		mpb.addFormDataPart("chunk", fn, RequestBody.create(chunk, octetstream));

		Request r = startReq(params).post(mpb.build()).build();
		return client.newCall(r).execute();
	}
}