/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autocomplete;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;

import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class OobaboogaMessageCompleter extends MessageCompleter
{
	public OobaboogaMessageCompleter(ModelSettings modelSettings)
	{
		super(modelSettings);
	}
	
	@Override
	protected JsonObject buildParams(String prompt)
	{
		JsonObject params = new JsonObject();
		params.addProperty("prompt", prompt);
		params.addProperty("max_length", modelSettings.maxTokens.getValueI());
		params.addProperty("temperature", modelSettings.temperature.getValue());
		params.addProperty("top_p", modelSettings.topP.getValue());
		return params;
	}
	
	@Override
	protected WsonObject requestCompletion(JsonObject parameters)
		throws IOException, JsonException
	{
		// set up the API request
		URL url = new URL("http://127.0.0.1:5000/api/v1/generate");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		
		// set the request body
		conn.setDoOutput(true);
		try(OutputStream os = conn.getOutputStream())
		{
			os.write(JsonUtils.GSON.toJson(parameters).getBytes());
			os.flush();
		}
		
		// parse the response
		return JsonUtils.parseConnectionToObject(conn);
	}
	
	@Override
	protected String extractCompletion(WsonObject response) throws JsonException
	{
		// extract completion from response
		String completion =
			response.getArray("results").getObject(0).getString("text");
		
		// remove the extra character at the start
		if(!completion.isEmpty())
			completion = completion.substring(1);
		
		// remove the next message
		if(completion.contains("\n<"))
			completion = completion.substring(0, completion.indexOf("\n<"));
		
		// remove newlines
		completion = completion.replace("\n", " ");
		
		return completion;
	}
}
