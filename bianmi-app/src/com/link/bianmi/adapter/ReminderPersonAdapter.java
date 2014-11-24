package com.link.bianmi.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.link.bianmi.R;
import com.link.bianmi.entity.Reminder;
import com.link.bianmi.imageloader.ImageLoader;
import com.link.bianmi.utility.ViewHolder;

/**
 * "我的提醒"列表 适配器
 */
public class ReminderPersonAdapter extends BaseAdapter {
	private List<Reminder.Person> mDataList;

	private Context mContext;

	public ReminderPersonAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList == null ? null : mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.reminder_person_listview_item, null);

		ImageView imageView = ViewHolder.get(convertView, R.id.imageview);
		TextView titleTextView = ViewHolder.get(convertView,
				R.id.title_textview);
		TextView contentTextView = ViewHolder.get(convertView,
				R.id.content_textview);
		TextView likesTextView = ViewHolder.get(convertView,
				R.id.likes_textview);
		TextView commentsTextView = ViewHolder.get(convertView,
				R.id.comments_textview);
		if (mDataList == null || mDataList.size() <= 0)
			return null;
		Reminder.Person personReminder = mDataList.get(position);
		if (personReminder != null) {
			ImageLoader.displayImage(imageView, personReminder.imageUrl,
					R.drawable.ic_launcher, true);
			titleTextView.setText(personReminder.title);
			contentTextView.setText(personReminder.content);
			likesTextView.setText(String.valueOf(personReminder.likes));
			commentsTextView.setText(String.valueOf(personReminder.comments));
		}
		return convertView;
	}

	public void refresh(List<Reminder.Person> dataList) {
		mDataList = dataList;
		this.notifyDataSetChanged();
	}
}