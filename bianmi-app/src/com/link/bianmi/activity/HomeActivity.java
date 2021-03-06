package com.link.bianmi.activity;

import java.util.ArrayList;

import lib.widget.imageviewex.ImageViewEx;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.link.bianmi.R;
import com.link.bianmi.SysConfig;
import com.link.bianmi.UserConfig;
import com.link.bianmi.adapter.ViewPagerAdapter;
import com.link.bianmi.asynctask.listener.OnTaskOverListener;
import com.link.bianmi.entity.Reminder;
import com.link.bianmi.entity.Secret;
import com.link.bianmi.entity.manager.ConfigManager;
import com.link.bianmi.entity.manager.ReminderManager;
import com.link.bianmi.fragment.FriendFragment;
import com.link.bianmi.fragment.HotFragment;
import com.link.bianmi.fragment.NearbyFragment;
import com.link.bianmi.utils.UmengSocialClient;
import com.link.bianmi.widget.AudioCircleButton;
import com.link.bianmi.widget.BlurView;
import com.link.bianmi.widget.ScaleImageView;
import com.link.bianmi.widget.ScaleImageView.ImageViewListener;
import com.link.bianmi.widget.SuperToast;
import com.link.bianmi.widget.ViewPagerTabBar;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;

public class HomeActivity extends BaseFragmentActivity {
	public ViewPager mViewPager;
	private ViewPagerTabBar mViewPagerTab;
	private ListPopupWindow mMenuWindow;
	private MenuAdapter mMenuAdapter;
	private ArrayList<Fragment> mFragments;
	private Reminder mReminder;
	private final int REQUEST_CODE_REMINDER = 1111;// 查看提醒

	// 放大图片
	private ViewStub mScaleViewStub;
	private View mScaleRootView;
	private ScaleImageView mScaleImageView;
	private BlurView mScaleBlurView;
	private ImageViewEx mScaleViewEx;
	private TextView mScaleContentText;
	private TextView mScaleDetalsText;
	private ObjectAnimator mScalseFadeInAnim, mScaleFadeOutAnim;
	private boolean mScaleIsClose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 检查更新
		UmengUpdateAgent.setUpdateAutoPopup(true);
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateUIStyle(UpdateStatus.STYLE_NOTIFICATION);
		UmengUpdateAgent.update(this);

		// 初始化Push
		XGPushConfig.enableDebug(this, true);
		Context context = getApplicationContext();
		XGPushManager.registerPush(context);
		Intent service = new Intent(context, XGPushService.class);
		context.startService(service);

		// 初始化ActionBar
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(true);
		setContentView(R.layout.activity_home);

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPagerTab = (ViewPagerTabBar) findViewById(R.id.viewpagertab);
		mFragments = new ArrayList<Fragment>();

		mFragments.add(new HotFragment());
		mFragments.add(new FriendFragment());
		mFragments.add(new NearbyFragment());
		String fragmentTitles[] = new String[] {
				this.getResources().getString(R.string.hot),
				this.getResources().getString(R.string.friend),
				this.getResources().getString(R.string.nearby) };
		mViewPager.setOffscreenPageLimit(mFragments.size());
		mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(),
				mFragments, fragmentTitles));

		mViewPagerTab.setViewPager(mViewPager);
		mScaleViewStub = (ViewStub) findViewById(R.id.image_viewstub);

		executeGetSysConfigTask();
		executeGetReminderTask();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mMenuAdapter != null) {
			mMenuAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		AudioCircleButton.stopPlay();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_REMINDER) {
			mReminder = null;
			mReminderItem.setIcon(R.drawable.ab_ic_reminder);
		}
	}

	private MenuItem mMoreItem;
	private MenuItem mLoadingItem;
	private MenuItem mReminderItem;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.findItem(R.id.action_add).setVisible(true);
		menu.findItem(R.id.action_reminder).setVisible(true);
		if (mLoadingItem.isVisible()) {
			mMoreItem.setVisible(false);
		} else {
			mMoreItem.setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	private boolean mFragmentsLoaded = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		mMoreItem = menu.findItem(R.id.action_more);
		mLoadingItem = menu.findItem(R.id.action_loading);
		mReminderItem = menu.findItem(R.id.action_reminder);
		if (mReminder == null) {
			mReminder = new Reminder();
		}
		mReminderItem
				.setIcon(mReminder.hasReminder ? R.drawable.ab_ic_reminder_has
						: R.drawable.ab_ic_reminder);
		mLoadingItem.setVisible(false);
		mMoreItem.setVisible(true);
		if (!mFragmentsLoaded) {
			for (int i = 0; i < mFragments.size(); i++) {
				mFragments.get(i).onCreateOptionsMenu(menu, inflater);
			}
			mFragmentsLoaded = true;
		}

		return true;// 返回true，否则不显示Menu
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_add) {
			if (UserConfig.getInstance().getIsGuest()) {
				showGuestTipDialog(getString(R.string.guest_action_publis_msg));
			} else {
				launchActivity(PublishActivity.class);
			}
		} else if (item.getItemId() == R.id.action_reminder) {
			if (UserConfig.getInstance().getIsGuest()) {
				showGuestTipDialog(getString(R.string.guest_action_reminder_msg));
			} else {
				launchActivityForResult(ReminderActivity.class,
						REQUEST_CODE_REMINDER);
			}
		} else if (item.getItemId() == R.id.action_more) {
			launchActivityForResult(SettingsActivity.class, 6666);
			// showMoreOptionMenu(findViewById(R.id.action_more));
		}
		return true;
	}

	long mLastBackPressedTime = 0;

	@Override
	public void onBackPressed() {
		// 如果正在加载，则取消加载
		if (mLoadingItem.isVisible()) {
			finishLoaded(true);
			return;
		}

		if (isScaleViewShow()) {
			dismissScaleView();
		} else {
			long cur_time = System.currentTimeMillis();

			if ((cur_time - mLastBackPressedTime) < 1000) {
				super.onBackPressed();
			} else {
				mLastBackPressedTime = cur_time;
				SuperToast.makeText(HomeActivity.this,
						R.string.press_back_again_to_exit,
						SuperToast.LENGTH_SHORT).show();
			}
		}
	}

	// ------------------------------Private------------------------------

	/**
	 * 更多菜单
	 */
	private void showMoreOptionMenu(View view) {
		if (mMenuWindow != null) {
			mMenuWindow.dismiss();
			mMenuWindow = null;
		}
		mMenuWindow = new ListPopupWindow(this);
		if (mMenuAdapter == null) {
			mMenuAdapter = new MenuAdapter();
		}
		mMenuWindow.setModal(true);
		mMenuWindow.setContentWidth(getResources().getDimensionPixelSize(
				R.dimen.popupmenu_dialog_width));
		mMenuWindow.setAdapter(mMenuAdapter);
		mMenuWindow.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					UmengSocialClient.showShareDialog(HomeActivity.this);
					break;
				case 1:
					launchActivityForResult(SettingsActivity.class, 6666);
					break;
				default:
					break;
				}
				if (mMenuWindow != null) {
					mMenuWindow.dismiss();
					mMenuWindow = null;
				}
			}

		});
		mMenuWindow.setAnchorView(view);
		mMenuWindow.show();
		mMenuWindow.getListView().setDividerHeight(1);
	}

	private class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.popup_menu_item, null);
			TextView name = (TextView) convertView.findViewById(R.id.tv_name);

			if (position == 0) {
				name.setText(R.string.invite_friends);
			} else if (position == 1) {
				name.setText(R.string.settings);
			}
			return convertView;
		}
	}

	private void inflateImageViewStub() {

		mScaleRootView = mScaleViewStub.inflate();
		mScaleRootView.setVisibility(View.GONE);
		mScaleContentText = (TextView) mScaleRootView
				.findViewById(R.id.content_textview);
		mScaleDetalsText = (TextView) mScaleRootView
				.findViewById(R.id.details_textview);
		((View) mScaleContentText.getParent()).setAlpha(0);
		mScaleViewEx = (ImageViewEx) mScaleRootView
				.findViewById(R.id.fragment_image_imageViewex);
		mScaleViewEx.setFillDirection(ImageViewEx.FillDirection.HORIZONTAL);
		mScaleBlurView = (BlurView) mScaleRootView
				.findViewById(R.id.fragment_image_blurview);

		mScaleImageView = (ScaleImageView) mScaleRootView
				.findViewById(R.id.fragment_image_scaleimageview);
		mScaleImageView.setBlurView(mScaleBlurView);

		mScaleIsClose = false;
		mScalseFadeInAnim = ObjectAnimator.ofFloat(
				((View) mScaleContentText.getParent()), "alpha", 0f, 1f);
		mScalseFadeInAnim.setDuration(ScaleImageView.anim_duration / 2);
		mScalseFadeInAnim.setStartDelay(ScaleImageView.anim_duration / 2);
		mScalseFadeInAnim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {

			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		mScaleFadeOutAnim = ObjectAnimator.ofFloat(
				((View) mScaleContentText.getParent()), "alpha", 1f, 0f);
		mScaleFadeOutAnim.setDuration(ScaleImageView.anim_duration / 2);
		mScaleImageView.setImageViewListener(new ImageViewListener() {
			@Override
			public void onSingleTap() {
				mScaleIsClose = true;
				mScaleImageView.startCloseScaleAnimation();
				mScaleFadeOutAnim.start();
			}

			@Override
			public void onScaleEnd() {
				if (mScaleIsClose) {
					mScaleImageView.setImageDrawable(null);
					mScaleRootView.setVisibility(View.GONE);
					showScaleView(null, false, null);
					supportInvalidateOptionsMenu();
					mScaleIsClose = false;
				} else {
					mScaleImageView.setTopCrop(false);
					mScaleImageView.initAttacher();
				}
			}
		});

	}

	private boolean isScaleViewShow() {
		if (mScaleRootView != null)
			return mScaleRootView.getVisibility() == View.VISIBLE;
		return false;

	}

	private void dismissScaleView() {
		if (!mScaleIsClose) {
			mScaleIsClose = true;
			mScaleImageView.startCloseScaleAnimation();
		}
	}

	private void startScaleAnimation(ImageView smallImageView,
			final Secret secret) {
		mScaleContentText.setText(secret.content);
		mScaleDetalsText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (secret != null) {
					launchActivity(DetailsActivity.class, "secret", secret);
				}
			}
		});
		mScaleRootView.setVisibility(View.VISIBLE);
		mScalseFadeInAnim.start();
		mScaleBlurView.drawBlurOnce();
		mScaleImageView.startScaleAnimation(smallImageView);
		supportInvalidateOptionsMenu();
	}

	// ------------------------------Public------------------------------
	/**
	 * 游客提醒对话框
	 */
	public void showGuestTipDialog(String msg) {
		if (msg == null || msg.isEmpty())
			return;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder
				.setMessage(msg)
				.setPositiveButton(getString(R.string.guest_go_signup),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (SysConfig.getInstance().smsAccess()) {
									launchActivity(SignUpBySmsActivity.class);
								} else {
									launchActivity(SignUpActivity.class);
								}
							}
						}).create();
		dialog.show();
	}

	public View getViewPagerTab() {
		return mViewPagerTab;
	}

	/**
	 * 结束加载
	 * 
	 * @param isStopAtOnce
	 *            true 立即结束
	 */
	public void finishLoaded(boolean isStopAtOnce) {

		if (mLoadingItem == null || mMoreItem == null)
			return;

		if (isStopAtOnce) {
			mLoadingItem.getActionView().clearAnimation();
			mLoadingItem.setVisible(false);
			mMoreItem.setVisible(true);
			return;
		}
		AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
		anim.setDuration(1000);
		anim.setFillAfter(true);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				finishLoaded(true);
			}
		});
		mLoadingItem.getActionView().setAnimation(anim);
	}

	/**
	 * 开始加载
	 */
	public void startLoading() {
		if (mLoadingItem == null || mMoreItem == null)
			return;
		mMoreItem.setVisible(false);
		mLoadingItem.setVisible(true);

	}

	/**
	 * 显示图片缩放控件
	 * 
	 */
	public void showScaleView(ImageView smallImageView, boolean show,
			Secret item) {
		if (show) {
			if (mScaleRootView == null)
				inflateImageViewStub();
			startScaleAnimation(smallImageView, item);
		}
	}

	// ------------------------------Task------------------------------
	/**
	 * 初始化系统配置
	 */
	private void executeGetSysConfigTask() {
		ConfigManager.Task.getConfig();
	}

	/**
	 * 获取提醒的Task
	 */
	private void executeGetReminderTask() {
		ReminderManager.Task.getReminder(new OnTaskOverListener<Reminder>() {

			@Override
			public void onSuccess(Reminder t) {
				mReminder = t;
				if (t != null && mReminderItem != null) {
					mReminderItem
							.setIcon(mReminder.hasReminder ? R.drawable.ab_ic_reminder_has
									: R.drawable.ab_ic_reminder);
				}
			}

			@Override
			public void onFailure(int code, String msg) {
			}
		});
	}
}