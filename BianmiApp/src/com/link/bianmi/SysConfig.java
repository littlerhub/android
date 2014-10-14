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
			mInstance = new SysConfig(BianmiApplication.getInstance());
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
				"http://121.40.88.136/bianmi/index.php/Index/Login/reg");
		mProperties.setProperty("bianmi.url.signin.debug",
				"http://121.40.88.136/bianmi/index.php/Index/Login/index");
		mProperties.setProperty("bianmi.url.signout.debug",
				"http://121.40.88.136/bianmi/index.php/Index/Login/signout");
		mProperties.setProperty("bianmi.url.secret.publish.debug",
				"http://192.168.1.101/bianmi/add.php");
		mProperties.setProperty("bianmi.url.secret.list.debug",
				"http://192.168.1.101/bianmi/secrets.php");
		mProperties.setProperty("bianmi.url.secret.like.debug",
				"http://192.168.1.101/bianmi/like.php");
		mProperties.setProperty("bianmi.url.comment.like.debug",
				"http://192.168.1.101/bianmi/comment_like.php");
		mProperties.setProperty("bianmi.url.config.debug",
				"http://192.168.1.101/bianmi/config.php");
		mProperties.setProperty("bianmi.url.comment.list.debug",
				"http://192.168.1.101/bianmi/comments.php");
		mProperties.setProperty("bianmi.url.comment.publish.debug",
				"http://192.168.1.101/bianmi/add_comment.php");

		// ---------------release
		mProperties.setProperty("bianmi.dbname.release", "bianmi_v1");
		mProperties.setProperty("bianmi.url.base.release",
				"http://infinigag-us.aws.af.cm"); // Base URL
		mProperties.setProperty("bianmi.url.signup.release",
				"http://192.168.1.101/bianmi/signup.php"); // 注册
		mProperties.setProperty("bianmi.url.signin.release",
				"http://192.168.1.101/bianmi/signin.php"); // 登录
		mProperties.setProperty("bianmi.url.signout.release",
				"http://192.168.1.101/bianmi/signout.php"); // 登出
		mProperties.setProperty("bianmi.qiniu.uptoken",
				"http://192.168.1.101/bianmi/token.php?type=uptoken");// 上传token
		mProperties.setProperty("bianmi.url.secret.publish.release",
				"http://192.168.1.101/bianmi/add.php");
		mProperties.setProperty("bianmi.url.secret.list.release",
				"http://192.168.1.101/bianmi/secrets.php");
		mProperties.setProperty("bianmi.url.secret.like.release",
				"http://192.168.1.101/bianmi/like.php");
		mProperties.setProperty("bianmi.url.comment.like.release",
				"http://192.168.1.101/bianmi/comment_like.php");
		mProperties.setProperty("bianmi.url.config.release",
				"http://192.168.1.101/bianmi/config.php");
		mProperties.setProperty("bianmi.url.comment.list.release",
				"http://192.168.1.101/bianmi/comments.php");
		mProperties.setProperty("bianmi.url.comment.publish.release",
				"http://192.168.1.101/bianmi/add_comment.php");
		// 七牛
		mProperties.setProperty("qiniu.bucketname.attach", "bianmi"); // 七牛
																		// Bucket
		mProperties.setProperty("qiniu.bucketdomain.attach",
				"bianmi.qiniudn.com"); // 七牛 BucketDomain.Image

	}

	public String getRootPath() {
		String bianmiPath = getSDPath();
		if (TextUtils.isEmpty(bianmiPath)) {
			bianmiPath = BianmiApplication.getInstance().getFilesDir()
					.getPath()
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

	/** 获取Url:热门 **/
	public String getHotUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.base.debug") + "/hot/";
		} else {
			return mProperties.getProperty("bianmi.url.base.release") + "/hot/";
		}
	}

	/** 获取Url:朋友 **/
	public String getFriendUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.base.debug") + "/fresh/";
		} else {
			return mProperties.getProperty("bianmi.url.base.release")
					+ "/fresh/";
		}
	}

	/** 获取Url:附近 **/
	public String getNearbyUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.base.debug")
					+ "/trending/";
		} else {
			return mProperties.getProperty("bianmi.url.base.release")
					+ "/trending/";
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
	 * 获取秘密列表
	 */
	public String getSecretsUrl() {
		if (isDebug()) {
			return mProperties.getProperty("bianmi.url.secret.list.debug");
		} else {
			return mProperties.getProperty("bianmi.url.secret.list.release");
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
}
