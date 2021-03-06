package com.link.bianmi.adapter;

import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.link.bianmi.R;
import com.link.bianmi.asynctask.listener.OnTaskOverListener;
import com.link.bianmi.entity.Comment;
import com.link.bianmi.entity.Secret;
import com.link.bianmi.entity.manager.CommentManager;
import com.link.bianmi.utils.ContextUtil;
import com.link.bianmi.utils.TimeUtil;
import com.link.bianmi.utils.ViewHolder;
import com.link.bianmi.widget.AudioCircleButton;
import com.link.bianmi.widget.SuperToast;

public class SecretDetailsAdapter extends BaseAdapter {

	private final int TYPE_SECRET = 0;// 秘密正文
	private final int TYPE_COMMENT = 1;// 评论列表

	private Context mContext;

	private Secret mSecret;
	private List<Comment> mCommentsList;

	private FinalBitmap mFBitmap;

	public SecretDetailsAdapter(Context context, Secret secret) {
		mContext = context;
		mSecret = secret;
		mFBitmap = FinalBitmap.create(context);
		mFBitmap.configLoadingImage(R.drawable.ic_launcher);
	}

	@Override
	public int getCount() {
		return 1 + getCommentsCount();
	}

	@Override
	public Object getItem(int position) {
		if (position == 0) {
			return mSecret;
		} else {
			return mCommentsList != null ? mCommentsList.get(position - 1)
					: null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return TYPE_SECRET;
		} else {
			return TYPE_COMMENT;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		if (convertView == null) {
			if (type == TYPE_SECRET) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.details_listview_item_secret, null);
			} else if (type == TYPE_COMMENT) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.details_listview_item_comment, null);
			} else {
				throw new RuntimeException("view type error");
			}
		}

		if (type == TYPE_SECRET) {
			bindSecretView(convertView, (Secret) getItem(position));
		} else if (type == TYPE_COMMENT) {
			bindCommentView(convertView, (Comment) getItem(position), position);
		}

		return convertView;
	}

	/** 评论列表 **/
	private void bindCommentView(View convertView, final Comment comment,
			final int position) {
		if (comment == null)
			return;
		ImageView avatarImage = ViewHolder.get(convertView,
				R.id.avatar_imageview);
		TextView contentText = ViewHolder.get(convertView,
				R.id.content_textview);
		// 楼层、时间
		TextView floorTimeLikesText = ViewHolder.get(convertView,
				R.id.floor_time_likes_textview);
		AudioCircleButton audioBtn = ViewHolder.get(convertView,
				R.id.avatar_audiocirclebutton);
		final ImageView likedImage = ViewHolder.get(convertView,
				R.id.liked_imageview);
		TextView nocommentsText = ViewHolder.get(convertView,
				R.id.nocomments_textview);
		// 暂无评论
		if (comment.createdTime == 0) {
			nocommentsText.setVisibility(View.VISIBLE);
			return;
		} else {
			nocommentsText.setVisibility(View.GONE);
		}
		// 头像
		mFBitmap.display(avatarImage, comment.avatarUrl);
		// 内容
		contentText.setText(comment.content);

		if (comment.likes <= 0) {
			floorTimeLikesText.setText(String.format(
					mContext.getString(R.string.floor_time),
					position,
					TimeUtil.formatTimeAgo(mContext, System.currentTimeMillis()
							- comment.createdTime)));
			// 楼层、时间、赞数
		} else {
			floorTimeLikesText.setText(String.format(
					mContext.getString(R.string.floor_time_likes),
					position,
					TimeUtil.formatTimeAgo(mContext, System.currentTimeMillis()
							- comment.createdTime), comment.likes));
		}

		// 语音
		audioBtn.init(comment.audioUrl, comment.audioLength);
		// 点赞
		likeOrDislike(likedImage, comment.isLiked);

		likedImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final String resourceId = comment.resourceId;
				CommentManager.Task.likeOrDislike(resourceId, !comment.isLiked,
						new OnTaskOverListener<Boolean>() {
							@Override
							public void onSuccess(Boolean t) {
								comment.isLiked = t;
								mCommentsList.set(position - 1, comment);
								likeOrDislike(likedImage, t);
							}

							@Override
							public void onFailure(int code, String msg) {
								SuperToast.makeText(mContext, msg,
										SuperToast.LENGTH_SHORT).show();
							}
						});
			}
		});
	}

	/** 秘密正文 **/
	private void bindSecretView(View convertView, Secret secret) {
		if (secret == null)
			return;
		// 内容
		TextView contentText = ViewHolder.get(convertView,
				R.id.content_textview);
		contentText.setText(secret.content);
		// 语音
		AudioCircleButton audioButton = ViewHolder.get(convertView,
				R.id.audio_button);
		audioButton.init(secret.audioUrl, secret.audioLength);
		// 点赞数
		TextView likedText = ViewHolder.get(convertView, R.id.liked_textview);
		likedText.setText(String.valueOf(secret.likes));
		// 评论数
		TextView commentText = ViewHolder.get(convertView,
				R.id.wherefrom_textview);
		commentText.setText(String.valueOf(secret.comments));
		// 图片
		ImageView pictureImage = ViewHolder.get(convertView,
				R.id.picture_imageview);
		LayoutParams params = pictureImage.getLayoutParams();
		params.width = (int) ContextUtil.getScreenWidth();
		params.height = (int) (ContextUtil.getScreenWidth() / 720 * 500);
		mFBitmap.display(pictureImage, secret.imageUrl);
		// 来自哪里
		TextView whereText = ViewHolder.get(convertView,
				R.id.wherefrom_textview);
		whereText.setText(secret.from);
	}

	private int getCommentsCount() {
		if (mCommentsList == null)
			return 0;
		return mCommentsList.size();
	}

	private void likeOrDislike(ImageView imageView, boolean isliked) {
		if (isliked) {
			imageView.setImageResource(R.drawable.ab_ic_liked);
		} else {
			imageView.setImageResource(R.drawable.ab_ic_like);
		}
	}

	public void refresh(List<Comment> commentsList, Secret secret) {
		this.mCommentsList = commentsList;
		this.mSecret = secret;
		this.notifyDataSetChanged();
	}
}