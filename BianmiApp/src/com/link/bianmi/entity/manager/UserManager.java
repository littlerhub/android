package com.link.bianmi.entity.manager;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.link.bianmi.SysConfig;
import com.link.bianmi.UserConfig;
import com.link.bianmi.asynctask.TaskParams;
import com.link.bianmi.asynctask.TaskResult;
import com.link.bianmi.asynctask.TaskResult.TaskStatus;
import com.link.bianmi.asynctask.listener.ITaskListener;
import com.link.bianmi.asynctask.listener.OnInsertTaskListener;
import com.link.bianmi.asynctask.listener.OnSelectTaskListener;
import com.link.bianmi.asynctask.listener.OnUpdateTaskListener;
import com.link.bianmi.entity.Result;
import com.link.bianmi.entity.User;
import com.link.bianmi.entity.builder.ResultBuilder;
import com.link.bianmi.entity.builder.UserBuilder;
import com.link.bianmi.http.HttpClient;
import com.link.bianmi.http.Response;
import com.link.bianmi.http.ResponseException;

public class UserManager {

	enum TaskType {
		TYPE_SIGNUP, // 注册
		TYPE_SIGNIN, // 登录
		TYPE_SIGNOUT, // 登出
	}

	public static class API {

		/** 登录 **/
		public static void signIn(String phonenum, String passmd5,
				OnSelectTaskListener<User> listener) {
			TaskParams taskParams = new TaskParams();
			ArrayList<NameValuePair> requestParams = null;
			requestParams = new ArrayList<NameValuePair>();
			NameValuePair phonenumParam = new BasicNameValuePair("phonenum",
					phonenum);
			NameValuePair passmd5Param = new BasicNameValuePair("passmd5",
					passmd5);
			requestParams.add(phonenumParam);
			requestParams.add(passmd5Param);
			taskParams.put("request", requestParams);
			UserAsyncTask userTask = new UserAsyncTask(TaskType.TYPE_SIGNIN,
					listener);
			userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					taskParams);
		}

		/** 登出 **/
		public static void signOut(OnUpdateTaskListener listener) {

			TaskParams taskParams = new TaskParams();
			ArrayList<NameValuePair> requestParams = null;
			requestParams = new ArrayList<NameValuePair>();
			NameValuePair useridParam = new BasicNameValuePair("userid",
					UserConfig.getInstance().getUserId());
			requestParams.add(useridParam);
			taskParams.put("request", requestParams);
			UserAsyncTask userTask = new UserAsyncTask(TaskType.TYPE_SIGNOUT,
					listener);
			userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					taskParams);

		}

		/** 注册 **/
		public static void signUp(String phonenum, String passmd5,
				OnInsertTaskListener listener) {
			TaskParams taskParams = new TaskParams();
			ArrayList<NameValuePair> requestParams = new ArrayList<NameValuePair>();
			NameValuePair phonenumParam = new BasicNameValuePair("phonenum",
					phonenum);
			NameValuePair passmd5Param = new BasicNameValuePair("passmd5",
					passmd5);
			requestParams.add(phonenumParam);
			requestParams.add(passmd5Param);
			taskParams.put("request", requestParams);
			UserAsyncTask userTask = new UserAsyncTask(TaskType.TYPE_SIGNUP,
					listener);
			userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					taskParams);
		}
	}

	public static class DB {

	}

	static class UserAsyncTask extends
			AsyncTask<TaskParams, Void, TaskResult<?>> {

		TaskType taskType;
		ITaskListener listener;

		public UserAsyncTask(TaskType taskType, ITaskListener listener) {
			this.taskType = taskType;
			this.listener = listener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected TaskResult<?> doInBackground(TaskParams... params) {
			Result result = new Result();
			TaskResult<?> taskResult = new TaskResult<Result>(
					TaskStatus.FAILED, result);
			// 登录
			if (taskType == TaskType.TYPE_SIGNIN) {
				ArrayList<NameValuePair> requestParam = (ArrayList<NameValuePair>) params[0]
						.get("request");
				Response response = HttpClient.doPost(requestParam, SysConfig
						.getInstance().getSignInUrl());
				try {
					// 解析Result
					JSONObject jsonObj = response.asJSONObject();
					result = ResultBuilder.getInstance().buildEntity(jsonObj);
					// 返回数据成功
					if (result != null && result.code == Result.RESULT_CODE_OK) {
						User user = UserBuilder.getInstance().buildEntity(
								jsonObj);
						taskResult = new TaskResult<User>(TaskStatus.OK, user);
					} else {
						taskResult = new TaskResult<Result>(TaskStatus.FAILED,
								result);
					}

				} catch (ResponseException e) {
					e.printStackTrace();
				}
				// 登出
			} else if (taskType == TaskType.TYPE_SIGNOUT) {

				ArrayList<NameValuePair> requestParam = (ArrayList<NameValuePair>) params[0]
						.get("request");
				Response response = HttpClient.doPost(requestParam, SysConfig
						.getInstance().getSignOutUrl());
				try {
					// 解析Result
					JSONObject jsonObj = response.asJSONObject();
					result = ResultBuilder.getInstance().buildEntity(jsonObj);
					// 返回数据成功
					if (result != null && result.code == Result.RESULT_CODE_OK) {
						taskResult = new TaskResult<Result>(TaskStatus.OK);
					} else {
						taskResult = new TaskResult<Result>(TaskStatus.FAILED,
								result);
					}

				} catch (ResponseException e) {
					e.printStackTrace();
				}

				// 注册
			} else if (taskType == TaskType.TYPE_SIGNUP) {
				ArrayList<NameValuePair> requestParam = (ArrayList<NameValuePair>) params[0]
						.get("request");
				Response response = HttpClient.doPost(requestParam, SysConfig
						.getInstance().getSignUpUrl());
				try {
					// 解析Result
					JSONObject jsonObj = response.asJSONObject();
					result = ResultBuilder.getInstance().buildEntity(jsonObj);
					// 返回数据成功
					if (result != null && result.code == Result.RESULT_CODE_OK) {
						taskResult = new TaskResult<Result>(TaskStatus.OK);
					} else {
						taskResult = new TaskResult<Result>(TaskStatus.FAILED,
								result);
					}
				} catch (ResponseException e) {
					e.printStackTrace();
				}
			}

			return taskResult;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(TaskResult<?> taskResult) {
			super.onPostExecute(taskResult);

			// 登录
			if (taskType == TaskType.TYPE_SIGNIN) {
				if (taskResult.getStatus() == TaskStatus.OK) {
					((OnSelectTaskListener<User>) listener)
							.onSuccess((User) taskResult.getEntity());
				} else if (taskResult.getStatus() == TaskStatus.FAILED) {
					Result result = (Result) taskResult.getEntity();
					((OnSelectTaskListener<User>) listener).onFailure(
							result.code, result.msg);
				}
				// 登出
			} else if (taskType == TaskType.TYPE_SIGNOUT) {
				if (taskResult.getStatus() == TaskStatus.OK) {
					((OnUpdateTaskListener) listener).onSuccess();
				} else if (taskResult.getStatus() == TaskStatus.FAILED) {
					Result result = (Result) taskResult.getEntity();
					((OnUpdateTaskListener) listener).onFailure(result.code,
							result.msg);
				}
				// 注册
			} else if (taskType == TaskType.TYPE_SIGNUP) {
				if (taskResult.getStatus() == TaskStatus.OK) {
					((OnInsertTaskListener) listener).onSuccess();
				} else if (taskResult.getStatus() == TaskStatus.FAILED) {
					Result result = (Result) taskResult.getEntity();
					((OnInsertTaskListener) listener).onFailure(result.code,
							result.msg);
				}
			}
		}
	}

}