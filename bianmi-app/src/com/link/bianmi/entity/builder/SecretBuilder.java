package com.link.bianmi.entity.builder;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.link.bianmi.entity.ListResult;
import com.link.bianmi.entity.Secret;

public class SecretBuilder implements BaseEntityBuilder<Secret>,
		BaseEntitysBuilder<Secret> {

	private static SecretBuilder mInstance = null;

	private SecretBuilder() {

	}

	public static SecretBuilder getInstance() {
		if (mInstance == null) {
			mInstance = new SecretBuilder();
		}

		return mInstance;
	}

	@Override
	public Secret buildEntity(JSONObject jsonObj) {
		Secret secret = null;
		try {
			if (jsonObj != null && jsonObj.has("secret")) {
				JSONObject secretJson = jsonObj.getJSONObject("secret");
				secret = new Secret();
				secret.resourceId = secretJson.getString("id");
				secret.content = secretJson.getString("content");
				secret.from = secretJson.getString("from");
				secret.imageUrl = secretJson.getString("image_url");
				secret.audioUrl = secretJson.getString("audio_url");
				secret.audioLength = secretJson.getInt("audio_length");
				secret.likes = secretJson.getInt("likes");
				secret.comments = secretJson.getInt("comments");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return secret;
	}

	@Override
	public ListResult<Secret> buildEntitys(JSONObject jsonObj) {
		ListResult<Secret> listResult = new ListResult<Secret>();
		listResult.list = new ArrayList<Secret>();
		try {
			if (jsonObj != null && jsonObj.has("list")) {
				JSONObject listJson = jsonObj.getJSONObject("list");
				if (listJson != null && listJson.has("has_more")) {
					listResult.hasMore = listJson.getBoolean("has_more");
				}

				if (listJson != null && listJson.has("secrets")) {
					JSONArray jsonArr = listJson.getJSONArray("secrets");
					for (int i = 0; i < jsonArr.length(); i++) {
						JSONObject jsonArrObj = jsonArr.getJSONObject(i);

						Secret secret = new Secret();
						secret.resourceId = jsonArrObj.getString("id");
						secret.content = jsonArrObj.getString("content");
						secret.from = jsonArrObj.getString("from");
						secret.imageUrl = jsonArrObj.getString("image_url");
						secret.audioUrl = jsonArrObj.getString("audio_url");
						secret.audioLength = jsonArrObj.getInt("audio_length");
						secret.likes = jsonArrObj.getInt("likes");
						secret.comments = jsonArrObj.getInt("comments");
						listResult.list.add(secret);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return listResult;
	}
}
