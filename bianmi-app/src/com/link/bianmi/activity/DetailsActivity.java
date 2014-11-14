package com.link.bianmi.activity;

import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.link.bianmi.R;
import com.link.bianmi.UserConfig;
import com.link.bianmi.activity.base.BaseFragmentActivity;
import com.link.bianmi.adapter.SecretDetailsAdapter;
import com.link.bianmi.asynctask.listener.OnTaskOverListener;
import com.link.bianmi.entity.Comment;
import com.link.bianmi.entity.ListResult;
import com.link.bianmi.entity.Secret;
import com.link.bianmi.entity.manager.CommentManager;
import com.link.bianmi.utility.ShareUtil;
import com.link.bianmi.widget.InputSuit;
import com.link.bianmi.widget.RListView;
import com.link.bianmi.widget.RListView.ActivateListener;
import com.link.bianmi.widget.RListView.TouchDirectionState;
import com.link.bianmi.widget.SuperToast;

public class DetailsActivity extends BaseFragmentActivity {

	private InputSuit mInputSuit;
	private RListView mRListView;
	private SecretDetailsAdapter mAdapter;
	private List<Comment> mCommentsList;
	private String mSecretId;
	private ListPopupWindow mMenuWindow;
	private MenuAdapter mMenuAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ActionBar
		getActionBar().setTitle(
				getResources().getString(R.string.details_action_title));
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_details);

		final Secret secret = (Secret) getIntent().getSerializableExtra(
				"secret");

		if (secret == null)
			finish();

		mSecretId = secret.resourceId;
		// 正文内容、评论列表
		mRListView = (RListView) findViewById(R.id.rlistview);
		mRListView.setActivateListener(new ActivateListener() {
			@Override
			public void onTouchDirection(TouchDirectionState state) {
			}

			@Override
			public void onScrollUpDownChanged(int delta, int scrollPosition,
					boolean exact) {
			}

			@Override
			public void onMovedIndex(int index) {
			}

			@Override
			public void onHeadTouchActivate(boolean activate) {
			}

			@Override
			public void onHeadStop() {
			}

			@Override
			public void onHeadActivate() {
				fetchNew();
			}

			@Override
			public void onFootTouchActivate(boolean activate) {
			}

			@Override
			public void onFootStop() {
			}

			@Override
			public void onFootActivate() {
				// 菊花至少转0.8秒
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						loadMore();
						mRListView.stopFootActiving();
					}
				}, 800);
			}
		});
		mAdapter = new SecretDetailsAdapter(this, secret);
		mRListView.setAdapter(mAdapter);
		// 输入套件
		mInputSuit = (InputSuit) findViewById(R.id.input_suit);
		mInputSuit.init(this, null, mInputListener);

		fetchNew();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mMenuAdapter != null) {
			mMenuAdapter.notifyDataSetChanged();
		}
	}

	private MenuItem mLoadingItem;
	private MenuItem mMoreItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.details, menu);
		mMoreItem = menu.findItem(R.id.action_more);
		mLoadingItem = menu.findItem(R.id.action_loading);
		mLoadingItem.setVisible(true);
		mMoreItem.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else if (item.getItemId() == R.id.action_like) {
			item.setIcon(R.drawable.ic_action_liked);
		} else if (item.getItemId() == R.id.action_more) {
			showMoreOptionMenu(findViewById(R.id.action_more));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mInputSuit.onActivityResult(requestCode, resultCode, data);
	}

	// -----------------------------自定义---------------------------

	/**
	 * 拉取最新
	 */
	private void fetchNew() {
		executeGetCommentsTask("");
	}

	/**
	 * 加载更多
	 */
	private void loadMore() {
		if (mCommentsList != null && mCommentsList.size() > 0) {
			executeGetCommentsTask(mCommentsList.get(mCommentsList.size() - 1).resourceId);
		}
	}

	private void refreshRListView(List<Comment> comments, boolean hasMore,
			long beginTime) {
		if (comments != null && comments.size() > 0) {
			if (mCommentsList == null) {
				mCommentsList = comments;
			} else {
				mCommentsList.addAll(comments);
			}
			mAdapter.refresh(mCommentsList);
		}
		mRListView.setFootVisiable(hasMore);
		mRListView.setEnableFooter(hasMore);
		long endTime = System.currentTimeMillis();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mRListView.stopHeadActiving();
				mLoadingItem.setVisible(false);
				mMoreItem.setVisible(true);
			}
		}, endTime - beginTime > 1500 ? 0 : 1500 - (endTime - beginTime));
	}

	private InputSuit.Listener mInputListener = new InputSuit.Listener() {
		@Override
		public void onSubmit(String photoPath, String recordPath,
				int recordLen, String message, String userName, String UserId) {
			mLoadingItem.setVisible(true);
			mMoreItem.setVisible(false);
			mInputSuit.startUpload();
		}

		@Override
		public void onUploadAttach(boolean result, String photoUrl,
				String recordUrl, int recordLength) {

			if (!result) {
				SuperToast.makeText(DetailsActivity.this, "发表失败！",
						SuperToast.LENGTH_SHORT).show();
				return;
			}

			SuperToast.makeText(DetailsActivity.this, "上传七牛成功！",
					SuperToast.LENGTH_SHORT).show();

			Comment comment = new Comment();
			comment.secretid = mSecretId;
			comment.userid = UserConfig.getInstance().getUserId();
			comment.content = mInputSuit.getMessage();
			comment.audioUrl = recordUrl;
			comment.audioLength = recordLength;
			comment.createdTime = System.currentTimeMillis();

			CommentManager.Task.publishComment(comment,
					new OnTaskOverListener<Comment>() {

						@Override
						public void onSuccess(Comment t) {
							SuperToast.makeText(DetailsActivity.this, "发表成功!",
									SuperToast.LENGTH_SHORT).show();
							mLoadingItem.setVisible(false);
							mMoreItem.setVisible(true);
							mInputSuit.reset();
							fetchNew();
						}

						@Override
						public void onFailure(int code, String msg) {
							SuperToast.makeText(DetailsActivity.this, "发表失败!",
									SuperToast.LENGTH_SHORT).show();
							mLoadingItem.setVisible(false);
							mMoreItem.setVisible(true);
						}
					});

		};
	};

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
					ShareUtil.showShare(DetailsActivity.this);
					break;
				case 1:
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

	// --------------------------内部类--------------------------
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
			int iconResId = R.drawable.ic_action_add;

			if (position == 0) {
				name.setText(R.string.share);
				iconResId = R.drawable.ic_action_add;
			} else if (position == 1) {
				name.setText(R.string.follow);
				iconResId = R.drawable.ic_action_add;
			}
			Drawable drawable = getResources().getDrawable(iconResId);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			name.setCompoundDrawables(drawable, null, null, null);
			return convertView;
		}
	}

	// --------------------------Task-------------------------------
	/**
	 * 执行获取评论列表任务
	 * 
	 * @param commentid
	 * @param lastid
	 */
	private void executeGetCommentsTask(final String lastid) {
		final long beginTime = System.currentTimeMillis();
		if (mSecretId == null || mSecretId.isEmpty())
			return;
		CommentManager.Task.getComments(mSecretId, lastid,
				new OnTaskOverListener<ListResult<Comment>>() {
					@Override
					public void onSuccess(ListResult<Comment> t) {
						if (t == null)
							return;
						if (lastid.isEmpty() && mCommentsList != null) {
							mCommentsList.clear();
						}
						refreshRListView(t.list, t.hasMore, beginTime);
					}

					@Override
					public void onFailure(int code, String msg) {
						SuperToast.makeText(DetailsActivity.this, msg,
								SuperToast.LENGTH_SHORT).show();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mRListView.stopHeadActiving();
								mLoadingItem.setVisible(false);
								mMoreItem.setVisible(true);
							}
						}, 1000);
					}
				});
	}

}