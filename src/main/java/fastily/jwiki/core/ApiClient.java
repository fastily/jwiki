package fastily.jwiki.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fastily.jwiki.util.FL;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Functions which perform {@code GET} and {@code POST} requests to the MediaWiki api and returns Response objects in a
 * suitable format.
 * 
 * @author Fastily
 *
 */
public final class ApiClient
{
	/**
	 * MediaType for {@code application/octet-stream}.
	 */
	private static final MediaType octetstream = MediaType.parse("application/octet-stream");

	/**
	 * HTTP client used for all requests.
	 */
	public final OkHttpClient client;

	/**
	 * The Wiki object tied to this ApiClient.
	 */
	private Wiki wiki;

	/**
	 * Constructor, create a new ApiClient for a Wiki instance.
	 * 
	 * @param wiki The Wiki object this ApiClient is associated with.
	 */
	protected ApiClient(Wiki wiki)
	{
		this.wiki = wiki;
		client = new OkHttpClient.Builder().cookieJar(new JwikiCookieJar()).readTimeout(1, TimeUnit.MINUTES).build();
	}

	/**
	 * Constructor, derives an ApiClient from a source Wiki. Useful for {@code centralauth} login/credential sharing.
	 * 
	 * @param from The source Wiki to create the new Wiki with
	 * @param to The new Wiki to apply {@code from}'s ApiClient settings on.
	 */
	protected ApiClient(Wiki from, Wiki to)
	{
		wiki = to;
		client = from.apiclient.client;

		JwikiCookieJar cl = (JwikiCookieJar) client.cookieJar();
		
		HashMap<String, String> l = new HashMap<>();
		cl.cj.get(from.conf.domain).forEach((k,v) -> {
			if(k.contains("centralauth"))
				l.put(k, v);
		});
		
		cl.cj.put(wiki.conf.domain, l);
	}

	/**
	 * Create a basic Request template which serves as the basis for any Request objects.
	 * 
	 * @param params Any URL parameters (not URL-encoded).
	 * @return A new Request.Builder with default values needed to hit MediaWiki API endpoints.
	 */
	private Request.Builder startReq(HashMap<String, String> params)
	{
		HttpUrl.Builder hb = new HttpUrl.Builder().scheme(wiki.conf.comms).host(wiki.conf.domain).addPathSegments(wiki.conf.scptPath);
		params.forEach(hb::addQueryParameter);
		
		return new Request.Builder().addHeader("User-Agent", wiki.conf.userAgent).url(hb.build());
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
		return client.newCall(startReq(params).build()).execute();
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
	 * @param params Any URL parameters (not URL-encoded).
	 * @param form The Key-Value form parameters to {@code POST}.
	 * @param fn The system name of the file to {@code POST}
	 * @param chunk The raw byte data associated with this file which will be sent in this {@code POST}.
	 * @return A Response with the results of this {@code POST}.
	 * @throws IOException Network error
	 */
	protected Response multiPartFilePOST(HashMap<String, String> params, HashMap<String, String> form, String fn, byte[] chunk)
			throws IOException
	{
		MultipartBody.Builder mpb = new MultipartBody.Builder().setType(MultipartBody.FORM);
		form.entrySet().stream().forEach(e -> mpb.addFormDataPart(e.getKey(), e.getValue()));

		mpb.addFormDataPart("chunk", fn, RequestBody.create(octetstream, chunk));

		Request r = startReq(params).post(mpb.build()).build();
//		System.out.println(JT.bodyToString(r));
		return client.newCall(r).execute();
	}

	/**
	 * Basic CookieJar policy for use with jwiki.
	 * 
	 * @author Fastily
	 *
	 */
	private static class JwikiCookieJar implements CookieJar
	{
		/**
		 * Internal HashMap tracking cookies. Legend - [ domain : [ key : value ] ].
		 */
		private HashMap<String, HashMap<String, String>> cj = new HashMap<>();

		/**
		 * Constructor, create a new JwikiCookieJar
		 */
		private JwikiCookieJar()
		{

		}

		/**
		 * Called when receiving a Response from the Api.
		 */
		@Override
		public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
		{
			String host = url.host();
			if (!cj.containsKey(host))
				cj.put(host, new HashMap<>());

			HashMap<String, String> m = cj.get(host);
			for (Cookie c : cookies)
				m.put(c.name(), c.value());
		}

		/**
		 * Called when creating a new Request to the Api.
		 */
		@Override
		public List<Cookie> loadForRequest(HttpUrl url)
		{
			String host = url.host();
			return !cj.containsKey(host) ? new ArrayList<>()
					: FL.toAL(cj.get(host).entrySet().stream()
							.map(e -> new Cookie.Builder().name(e.getKey()).value(e.getValue()).domain(host).build()));
		}
	}
}