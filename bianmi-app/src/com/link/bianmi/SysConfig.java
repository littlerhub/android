package com.link.bianmi;

import java.io.File;
import java.util.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * 
 * @Description 系统配置
 * @author pangfq
 * @date 2014年7月10日 上午10:04:30
 */
public class SysConfig {
	private static SysConfig mInstance;
	private SharedPreferences mPref;

	public static SysConfig getInstance() {
		if (mInstance == null) {
			mInstance = new SysConfig(MyApplication.getInstance());
		}
		return mInstance;
	}

	private Properties mProperties;

	private SysConfig(Context context) {
		mPref = PreferenceManager.getDefaultSharedPreferences(context);

		mProperties = new Properties();

		mProperties.setProperty("bianmi.debug", String.valueOf(false));// 是否为debug模式

		// ---------------debug
		mProperties.setProperty("bianmi.url.base.debug",
				"http://infinigag-us.aws.af.cm");
		mProperties.setProperty("bianmi.dbname.debug", "bianmi_d");
		mProperties.setProperty("bianmi.url.signup.debug",
				"http://bianmi.oschina.mopaas.com/index.php/Index/Login/reg");
		mProperties.setProperty("bianmi.url.signin.debug",
				"http://bianmi.oschina.mopaas.com/index.php/Index/Login/index");
		mProperties
				.setProperty("bianmi.url.signout.debug",
						"http://bianmi.oschina.mopaas.com/index.php/Index/Login/signout");
		mProperties.setProperty("bianmi.url.secret.publish.debug",
				"http://bianmi.oschina.mopaas.com/publish.php");
		mProperties.setProperty("bianmi.url.secret.list.hot.debug",
				"http://bianmi.oschina.mopaas.com/secrets_hot.php");
		mProperties.setProperty("bianmi.url.secret.list.friend.debug",
				"http://bianmi.oschina.mopaas.com/secrets_friend.php");
		mProperties.setProperty("bianmi.url.secret.list.nearby.debug",
				"http://bianmi.oschina.mopaas.com/secrets_nearby.php");
		mProperties.setProperty("bianmi.url.secret.like.debug",
				"http://bianmi.oschina.mopaas.com/like.php");
		mProperties.setProperty("bianmi.url.comment.like.debug",
				"http://bianmi.oschina.mopaas.com/comment_like.php");
		mProperties.setProperty("bianmi.url.config.debug",
				"http://bianmi.oschina.mopaas.com/config.php");
		mProperties.setProperty("bianmi.url.comment.list.debug",
				"http://bianmi.oschina.mopaas.com/comments.php");
		mProperties.setProperty("bianmi.url.comment.publish.debug",
				"http://bianmi.oschina.mopaas.com/publish_comment.php");
		mProperties.setProperty("bianmi.url.contacts.upload.debug",
				"http://bianmi.oschina.mopaas.com/upload_contacts.php");
		mProperties.setProperty("bianmi.url.reminder.debug",
				"http://bianmi.oschina.mopaas.com/reminder.php");
		mProperties.setProperty("bianmi.url.reminder.system.debug",
				"http://bianmi.oschina.mopaas.com/reminders_system.php");
		mProperties.setProperty("bianmi.url.reminder.person.debug",
				"http://bianmi.oschina.mopaas.com/reminders_person.php");
		mProperties.setProperty("bianmi.url.user.clear.debug",
				"http://bianmi.oschina.mopaas.com/clear_privacy.php");
		mProperties.setProperty("bianmi.url.secret.details.debug",
				"http://bianmi.oschina.mopaas.com/secret_details.php");
		// ---------------release
		mProperties.setProperty("bianmi.dbname.release", "bianmi_v1");
		mProperties.setProperty("bianmi.url.base.release",
				"http://infinigag-us.aws.af.cm"); // Base URL
		mProperties.setProperty("bianmi.url.signup.release",
				"http://bianmi.oschina.mopaas.com/signup.php"); // 注册
		mProperties.setProperty("bianmi.url.signin.release",
				"http://bianmi.oschina.mopaas.com/signin.php"); // 登录
		mProperties.setProperty("bianmi.url.signout.release",
				"http://bianmi.oschina.mopaas.com/signout.php"); // 登出
		mProperties.setProperty("bianmi.qiniu.uptoken",
				"http://bianmi.oschina.mopaas.com/token.php?type=uptoken");// 上传token
		mProperties.setProperty("bianmi.url.secret.publish.release",
				"http://bianmi.oschina.mopaas.com/publish.php");
		mProperties.setProperty("bianmi.url.secret.list.hot.release",
				"http://bianmi.oschina.mopaas.com/secrets_hot.php");
		mProperties.setProperty("bianmi.url.secret.list.friend.release",
				"http://bianmi.oschina.mopaas.com/secrets_friend.php");
		mProperties.setProperty("bianmi.url.secret.list.nearby.release",
				"http://bianmi.oschina.mopaas.com/secrets_nearby.php");
		mProperties.setProperty("bianmi.url.secret.like.release",
				"http://bianmi.oschina.mopaas.com/like.php");
		mProperties.setProperty("bianmi.url.comment.like.release",
				"http://bianmi.oschina.mopaas.com/comment_like.php");
		mProperties.setProperty("bianmi.url.config.release",
				"http://bianmi.oschina.mopaas.com/config.php");
		mProperties.setProperty("bianmi.url.comment.list.release",
				"http://bianmi.oschina.mopaas.com/comments.php");
		mProperties.setProperty("bianmi.url.comment.publish.release",
				"http://bianmi.oschina.mopaas.com/publish_comment.php");
		mProperties.setProperty("bianmi.url.contacts.upload.release",
				"http://bianmi.oschina.mopaas.com/upload_contacts.php");
		mProperties.setProperty("bianmi.url.reminder.release",
				"http://bianmi.oschina.mopaas.com/reminder.php");
		mProperties.setProperty("bianmi.url.reminder.system.release",
				"http://bianmi.oschina.mopaas.com/reminders_system.php");
		mProperties.setProperty("bianmi.url.reminder.person.release",
				"http://bianmi.oschina.mopaas.com/reminders_person.php");
		mProperties.setProperty("bianmi.url.user.clear.release",
				"http://bianmi.oschina.mopaas.com/clear_privacy.php");
		mProperties.setProperty("bianmi.url.secret.details.release",
				"http://bianmi.oschina.mopaas.com/secret_details.php");
		// 七牛
		mProperties.setProperty("qiniu.bucketname.attach", "bianmi"); // 七牛
																		// Bucket
		mProperties.setProperty("qiniu.bucketdomain.attach",
				"bianmi.qiniudn.com"); // 七牛 BucketDomain.Image

	}

	public String getRootPath() {
		String bianmiPath = getSDPath();
		if (TextUtils.isEmpty(bianmiPath)) {
			bianmiPath = MyApplication.getInstance().getFilesDir().getPath()
					+ File.separator + "BianMi";
		} else {
			bianmiPath += File.separator + "BianMi";
		}

		return bianmiPath;
	}

	public String getSecretPath() {
		String secretPath = getRootPath() + File.separator + "secret";
		File dir = new File(secretPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		return secretPath;
	}

	public String getSDPath() {
		String path = "";
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			path = Environment.getExternalStorageDirectory().toString();// 获取跟目录
		}
		return path;
	}

	public String getPathTemp() {
		String tempPath = getRootPath() + File.separator + "temp";
		File dir = new File(tempPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		return tempPath;
	}

	/** 是否为Debug模式 **/
	public boolean isDebug() {
		return Boolean.valueOf(mProperties.getProperty("bianmi.debug"));
	}

	/** 获取数据库名称 **/
	public String getDName() {
		if (isDebug())
			return mProperties.getProperty("bianmi.dbname.debug");
		else
			return mProperties.getProperty("bianmi.dbname.release");
	}

	/** 获取BaseUrl **/
	public String getBaseUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.base.debug");
		} else {
			return mProperties.getProperty("bianmi.url.base.release");
		}
	}

	/** 获取注册URL **/
	public String getSignUpUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.signup.debug");
		} else {
			return mProperties.getProperty("bianmi.url.signup.release");
		}
	}

	/** 获取登录URL **/
	public String getSignInUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.signin.debug");
		} else {
			return mProperties.getProperty("bianmi.url.signin.release");
		}
	}

	/** 获取登出URL **/
	public String getSignOutUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.signout.debug");
		} else {
			return mProperties.getProperty("bianmi.url.signout.release");
		}
	}

	/** 获取七牛 bucketname.attach **/
	public String getQiniuBucketNameAttach() {
		return mProperties.getProperty("qiniu.bucketname.attach");
	}

	public String getQiniuBucketDomainAttach() {
		return mProperties.getProperty("qiniu.bucketdomain.attach");
	}

	/**
	 * 获取七牛上传资源的token
	 */
	public String getQiniuUptoken() {
		return mProperties.getProperty("bianmi.qiniu.uptoken");
	}

	/**
	 * 发表秘密的url
	 */
	public String getPublishSecretUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.secret.publish.debug");
		} else {
			return mProperties.getProperty("bianmi.url.secret.publish.release");
		}
	}

	/**
	 * 获取热门秘密列表
	 */
	public String getHotSecretsUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.secret.list.hot.debug");
		} else {
			return mProperties
					.getProperty("bianmi.url.secret.list.hot.release");
		}
	}

	/**
	 * 获取朋友秘密列表
	 */
	public String getFriendSecretsUrl() {
		if (isDebug()) {
			return mProperties
					.getProperty("bianmi.url.secret.list.friend.debug");
		} else {
			return mProperties
					.getProperty("bianmi.url.secret.list.friend.release");
		}
	}

	/**
	 * 获取附近秘密列表
	 */
	public String getNearbySecretsUrl() {
		if (isDebug()) {
			return mProperties
					.getProperty("bianmi.url.secret.list.nearby.debug");
		} else {
			return mProperties
					.getProperty("bianmi.url.secret.list.nearby.release");
		}
	}

	/**
	 * 获取提醒的url
	 */
	public String getReminderUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.reminder.debug");
		} else {
			return mProperties.getProperty("bianmi.url.reminder.release");
		}
	}

	/**
	 * 获取系统提醒的url
	 */
	public String getReminderSystemUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.reminder.system.debug");
		} else {
			return mProperties
					.getProperty("bianmi.url.reminder.system.release");
		}
	}

	/**
	 * 获取我的提醒的url
	 */
	public String getReminderPersonUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.reminder.person.debug");
		} else {
			return mProperties
					.getProperty("bianmi.url.reminder.person.release");
		}
	}

	/**
	 * 秘密点赞的url
	 */
	public String getLikeUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.secret.like.debug");
		} else {
			return mProperties.getProperty("bianmi.url.secret.like.release");
		}
	}

	/**
	 * 评论点赞的url
	 */
	public String getCommentLikeUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.comment.like.debug");
		} else {
			return mProperties.getProperty("bianmi.url.comment.like.release");
		}
	}

	/**
	 * 秘密详情的url
	 */
	public String getSecretDetails() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.secret.details.debug");
		} else {
			return mProperties.getProperty("bianmi.url.secret.details.release");
		}
	}

	/**
	 * 获取服务端下发配置的url
	 */
	public String getConfigUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.config.debug");
		} else {
			return mProperties.getProperty("bianmi.url.config.release");
		}
	}

	/**
	 * 获取评论列表
	 */
	public String getCommentsUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.comment.list.debug");
		} else {
			return mProperties.getProperty("bianmi.url.comment.list.release");
		}
	}

	/**
	 * 发表评论的url
	 */
	public String getPublishCommentUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.comment.publish.debug");
		} else {
			return mProperties
					.getProperty("bianmi.url.comment.publish.release");
		}
	}

	/**
	 * 获取上传联系人的url
	 */
	public String getUploadContactsUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.contacts.upload.debug");
		} else {
			return mProperties
					.getProperty("bianmi.url.contacts.upload.release");
		}
	}

	/**
	 * 清除痕迹URL
	 */
	public String getClearPrivacyUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.user.clear.debug");
		} else {
			return mProperties.getProperty("bianmi.url.user.clear.release");
		}
	}

	public static class Constant {
		public static final String INTENT_BUNDLE_KEY_ISGUEST = "is_guest";
	}

	/** 是否展示广告 **/
	public boolean showAd() {
		return mPref.getBoolean("bianmi.config.showad", false);
	}

	/** 设置是否展示 **/
	public void setShowAd(boolean showAd) {
		SharedPreferences.Editor editor = mPref.edit();
		editor.putBoolean("bianmi.config.showad", showAd);
		editor.commit();
	}

	/** 短信验证是否可用 **/
	public boolean smsAccess() {
		return mPref.getBoolean("bianmi.config.sms", true);
	}

	/** 设置短信验证是否可用 **/
	public void setSmsAccess(boolean showAd) {
		SharedPreferences.Editor editor = mPref.edit();
		editor.putBoolean("bianmi.config.sms", showAd);
		editor.commit();
	}
}
