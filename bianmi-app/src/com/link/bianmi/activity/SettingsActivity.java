package com.link.bianmi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.link.bianmi.BianmiApplication;
import com.link.bianmi.R;
import com.link.bianmi.UserConfig;
import com.link.bianmi.activity.base.BaseFragmentActivity;
import com.link.bianmi.asynctask.listener.OnTaskOverListener;
import com.link.bianmi.entity.manager.UserManager;
import com.link.bianmi.unit.ninelock.NineLockActivity;
import com.link.bianmi.unit.ninelock.NineLockSettingsActivity;
import com.link.bianmi.utility.Tools;
import com.link.bianmi.widget.SuperToast;
import com.link.bianmi.widget.SwitchButton;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class SettingsActivity extends BaseFragmentActivity {

	// 设置密码开关
	private SwitchButton mPassSwitchBtn = null;
	private TextView mVersionText = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(getString(R.string.settings));
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_settings);

		// 设置密码
		findViewById(R.id.password_group).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						launchActivityForResult(NineLockSettingsActivity.class,
								REQUEST_CODE_SETPASS);
					}
				});
		// 设置密码开关
		mPassSwitchBtn = (SwitchButton) findViewById(R.id.settings_item_password_switchbutton);
		changeSwitchButtonState();
		mPassSwitchBtn
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// 开启手势锁屏
						if (isChecked
								&& !UserConfig.getInstance().getLockPassKey()
										.isEmpty()) {
							UserConfig.getInstance().setLockPassStartStatus(
									true);
							return;
						}
						// 关闭手势锁屏
						if (!isChecked
								&& !UserConfig.getInstance().getLockPassKey()
										.isEmpty()) {
							Bundle bundle = new Bundle();
							bundle.putBoolean("close_lock", true);
							launchActivityForResult(NineLockActivity.class,
									bundle, REQUEST_CODE_CLOSELOCK);
						}
					}
				});
		// 常见问题
		findViewById(R.id.faq_group).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchActivity(WebActivity.class);
			}
		});
		// 检查更新
		findViewById(R.id.update_group).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						checkUpdate();
					}
				});
		// 当前版本名称
		mVersionText = (TextView) findViewById(R.id.version_textview);
		mVersionText.setText(String.format(getString(R.string.current_version),
				Tools.getVersionName(SettingsActivity.this)));

		// 退出登录
		findViewById(R.id.settings_item_exit_group).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mLoadingItem.setVisible(true);
						UserManager.Task
								.signOut(new OnTaskOverListener<Object>() {
									@Override
									public void onFailure(int code, String msg) {
										SuperToast.makeText(
												SettingsActivity.this,
												"退出失败" + msg,
												SuperToast.LENGTH_SHORT).show();
										mLoadingItem.setVisible(false);
									}

									@Override
									public void onSuccess(Object t) {
										mLoadingItem.setVisible(false);
										BianmiApplication.getInstance()
												.signOut();
										launchActivity(WelcomeActivity.class);
										ActivitysManager.removeAllActivity();
									}
								});
					}
				});
	}

	private final int REQUEST_CODE_SETPASS = 1111;// 设置手势密码
	private final int REQUEST_CODE_CLOSELOCK = 2222;// 关闭手势密码

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == REQUEST_CODE_SETPASS || requestCode == REQUEST_CODE_CLOSELOCK)
				&& resultCode == Activity.RESULT_OK)
			changeSwitchButtonState();
		else if (requestCode == REQUEST_CODE_CLOSELOCK
				&& resultCode != Activity.RESULT_OK) {
			mPassSwitchBtn.setChecked(true);
		}
	}

	private MenuItem mLoadingItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.loading, menu);
		mLoadingItem = menu.findItem(R.id.action_loading);
		mLoadingItem.setVisible(false);
		return true;
	}

	// -------------------------------自定义方法---------------------------
	private void changeSwitchButtonState() {
		if (UserConfig.getInstance().getLockPassKey().isEmpty()) {
			mPassSwitchBtn.setVisibility(View.GONE);
			return;
		}
		mPassSwitchBtn.setVisibility(View.VISIBLE);
		mPassSwitchBtn.setChecked(UserConfig.getInstance()
				.getLockPassStartStatus());
	}

	/**
	 * 检查是否有更新
	 * 
	 * @return
	 */
	private void checkUpdate() {
		mLoadingItem.setVisible(true);
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateUIStyle(UpdateStatus.STYLE_DIALOG);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				mLoadingItem.setVisible(false);
				switch (updateStatus) {
				case UpdateStatus.Yes: // has update
					UmengUpdateAgent.showUpdateDialog(SettingsActivity.this,
							updateInfo);
					break;
				case UpdateStatus.No: // has no update
					SuperToast.makeText(SettingsActivity.this, "没有更新",
							SuperToast.LENGTH_SHORT).show();
					break;
				case UpdateStatus.NoneWifi: // none wifi
					SuperToast.makeText(SettingsActivity.this,
							"没有wifi连接， 只在wifi下更新", SuperToast.LENGTH_SHORT)
							.show();
					break;
				case UpdateStatus.Timeout: // time out
					SuperToast.makeText(SettingsActivity.this, "超时",
							SuperToast.LENGTH_SHORT).show();
					break;
				}
			}
		});
		UmengUpdateAgent.update(this);
	}
}