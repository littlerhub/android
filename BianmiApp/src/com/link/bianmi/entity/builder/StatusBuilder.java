package com.link.bianmi.entity.builder;

import org.json.JSONException;
import org.json.JSONObject;

import com.link.bianmi.entity.ResultStatus;

public class StatusBuilder implements BaseEntityBuilder<ResultStatus> {
	
	private static StatusBuilder mInstance = null;

	private StatusBuilder() {

	}

	public static StatusBuilder getInstance() {
		if (mInstance == null) {
			mInstance = new StatusBuilder();
		}

		return mInstance;
	}

	@Override
	public ResultStatus buildEntity(JSONObject jsonObj) {
		ResultStatus status = null;
		try {
			if (jsonObj != null && jsonObj.has("status")) {
				JSONObject statusJson = jsonObj.getJSONObject("status");
				status = new ResultStatus();
				status.code = statusJson.getInt("code");
				status.msg = statusJson.getString("msg");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return status;
	}
}