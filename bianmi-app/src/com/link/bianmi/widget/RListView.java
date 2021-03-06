package com.link.bianmi.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.link.bianmi.R;
import com.link.bianmi.utils.NetworkUtil;
import com.link.bianmi.utils.Tools;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * 下拉刷新，上拉加载更多的ListView
 * 
 */
public class RListView extends ListView implements OnScrollListener {

	/** 上下文 **/
	private Context mContext;
	/** 进度条最大进度 **/
	private static final int MAX_PROGRESS = 360;
	/** 手势下拉距离比 **/
	private final static int RATIO = 2;

	/** 页眉 **/
	private View mHeadView;
	/** 页脚 提示 **/
	private TextView mHeadTips;
	/** 进度条有效距离(页眉离开顶部-View高度) **/
	/** 页眉高度 **/
	private int mViewHeight;
	/** 按下时的y坐标 为下滑页眉准备 **/
	private int mHeadStartY;
	/** 用于保证startY的值在一个完整的touch事件中只被记录一次 **/
	private boolean mIsHeadRecord = false;

	/** 页眉进度条增长速率 **/
	private static float HEADRATE = 1.8f;

	private ProgressWheel mHeadProgressWheel;

	/** 页脚视图 **/
	private View mFootView;
	/** 页脚内容视图 **/
	private View mFootInfoView;
	private ProgressWheel mFootProgressWheel;
	/** 页脚 提示 **/
	private TextView mFootTips;

	/** 按下时的y坐标 为上滑脚准备 **/
	private int mFootStartY;
	/** 用于保证startY的值在一个完整的touch事件中只被记录一次 **/
	private boolean mIsFootRecord = false;

	/** 页角进度条增长速率 **/
	private static float FOOTRATE = 1.8f;
	/** Head动画 **/
	private ValueAnimator mHeadAnimator;
	/** Foot动画 **/
	private ValueAnimator mFootAnimator;

	/** 当前视图能看到的第一个项的索引 **/
	private int mFirstItemIndex = -1;
	/** 列表数据是否已经显示到最后一条数据 **/
	private boolean mIsButtom = false;

	/** 触发监听 **/
	private OnListener mOnListener;

	/** 允许最大Head拉动距离 **/
	private int mHeadMaxOverscrollDistance;
	/** 允许最大Foot拉动距离 **/
	private int mFootMaxOverscrollDistance;

	/** 最大允许拖动距离 dp **/
	private final static int MaxOverscrollDistance = 360;

	/** 滑动方向 **/
	private TouchDirectionState mTouchDirection = TouchDirectionState.None;

	/** 所有滑动方向 **/
	public enum TouchDirectionState {
		None,
		/** 上滑 **/
		Up,
		/** 下滑 **/
		Down
	}

	/** 当前Footer显示状态 **/
	private int mFootVisiable = View.VISIBLE;
	/** 当前Header显示状态 **/
	@SuppressWarnings("unused")
	private int mHeadVisiable = View.VISIBLE;

	/** Foot拖动时的触发状态 **/
	private boolean mFootTouchActivate = false;

	/** Head拖动时的触发状态 **/
	private boolean mHeadTouchActivate = false;

	/** 滑动方向判断 灵敏度距离缓存距离 **/
	float mTouchDirectionSensitivity = 0;

	/** Touch时坐标 **/
	float mTouchEventY = -1;

	/** 是否正在播放head动画 **/
	boolean mIsHeadAnimator = false;
	/** 当前是否触发head状态 **/
	boolean mIsActivingHead = false;

	/** 是否正在播放foot动画 **/
	boolean mIsFootAnimator = false;
	/** 当前是否触发foot状态 **/
	boolean mIsActivingFoot = false;

	// ------------------------------构造器------------------------------

	public RListView(Context context) {
		super(context);
		mContext = context;
		initClewListView();
	}

	public RListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initClewListView();

	}

	public RListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initClewListView();
	}

	// ------------------------------Override------------------------------

	/***
	 * touch 事件监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mIsHeadAnimator || mIsFootAnimator) {
			return super.onTouchEvent(ev);
		}

		try {
			switch (ev.getAction()) {
			// 按下
			case MotionEvent.ACTION_DOWN:
				doActionDown(ev);
				break;
			// 移动
			case MotionEvent.ACTION_MOVE:
				doActionMove(ev);
				break;
			// 抬起
			case MotionEvent.ACTION_UP:
				doActionUp(ev);
				break;
			default:
				break;
			}
			return super.onTouchEvent(ev);

		} catch (Exception ex) {
		}

		return false;
	}

	/***
	 * ListView 滑动监听
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	private int lastFirstVisibleItem;
	private int lastTop;
	private int scrollPosition;
	private int lastHeight;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		View firstChild = view.getChildAt(0);
		if (firstChild == null) {
			return;
		}
		int top = firstChild.getTop();
		int height = firstChild.getHeight();
		int delta;
		int skipped = 0;
		if (lastFirstVisibleItem == firstVisibleItem) {
			delta = lastTop - top;
		} else if (firstVisibleItem > lastFirstVisibleItem) {
			skipped = firstVisibleItem - lastFirstVisibleItem - 1;
			delta = skipped * height + lastHeight + lastTop - top;
		} else {
			skipped = lastFirstVisibleItem - firstVisibleItem - 1;
			delta = skipped * -height + lastTop - (height + top);
		}
		boolean exact = skipped == 0;
		scrollPosition += -delta;
		if (mOnListener != null) {
			mOnListener.onScroll(-delta, scrollPosition, exact);
		}
		lastFirstVisibleItem = firstVisibleItem;
		lastTop = top;
		lastHeight = firstChild.getHeight();

		if (totalItemCount <= 0)
			mFirstItemIndex = 0;

		// 是否已经显示到底部
		if (firstVisibleItem + visibleItemCount >= totalItemCount) {
			mIsButtom = true;

		} else {
			mIsButtom = false;
		}

		mFirstItemIndex = firstVisibleItem;

	}

	// ------------------------------Private------------------------------

	// 初始化
	private void initClewListView() {
		// 初始化HeadView
		mHeadView = LayoutInflater.from(mContext).inflate(
				R.layout.rlistview_head, null);
		mHeadProgressWheel = (ProgressWheel) mHeadView
				.findViewById(R.id.head_progresswheel);
		mHeadView.setBackgroundResource(R.color.rlistview_head_bg);
		mHeadTips = (TextView) mHeadView.findViewById(R.id.head_tips_textview);
		mHeadProgressWheel.setProgress(0);
		measureView(mHeadView); // 测量尺寸
		mViewHeight = mHeadView.getMeasuredHeight();
		addHeaderView(mHeadView, null, false);// 加入View
		mHeadView.setPadding(0, -1 * mViewHeight, 0, 0);// 设置到顶部不显示位置.

		// 初始化FootView
		mFootView = LayoutInflater.from(mContext).inflate(
				R.layout.rlistview_foot, null);
		mFootInfoView = mFootView.findViewById(R.id.info_view);

		mFootProgressWheel = (ProgressWheel) mFootView
				.findViewById(R.id.foot_progresswheel);
		mFootTips = (TextView) mFootView.findViewById(R.id.foot_tips_textview);
		mFootProgressWheel.setProgress(0);
		measureView(mFootView);
		addFooterView(mFootView, null, false);
		setOnScrollListener(this);// ListView滚动监听

		mFootVisiable = mFootView.getVisibility();
		mHeadVisiable = mHeadView.getVisibility();

		mTouchDirectionSensitivity = Tools.dip2px(mContext, 20); // 默认20DP

		mHeadMaxOverscrollDistance = Tools.dip2px(mContext,
				MaxOverscrollDistance);
		mFootMaxOverscrollDistance = mHeadMaxOverscrollDistance;
	}

	/***
	 * 测量 headView的宽和高.
	 * 
	 * @param child
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	/***
	 * 按下操作 获取按下时的y坐标
	 * 
	 * @param event
	 */
	private void doActionDown(MotionEvent ev) {

		mTouchEventY = ev.getY();
		mIsHeadRecord = false;// 此时的touch事件完毕，要关闭。
		mIsFootRecord = false;
		mTouchDirection = TouchDirectionState.None;
	}

	/***
	 * 拖拽移动操作
	 * 
	 * @param event
	 */
	private void doActionMove(MotionEvent ev) {
		// touch 方向判断
		float eventY = ev.getY();
		if (mOnListener != null
				&& eventY - mTouchEventY > mTouchDirectionSensitivity
				&& mTouchDirection != TouchDirectionState.Down) { // 下滑
			mTouchDirection = TouchDirectionState.Down;
		} else if (mOnListener != null
				&& eventY - mTouchEventY < -mTouchDirectionSensitivity
				&& mTouchDirection != TouchDirectionState.Up) { // 上滑
			mTouchDirection = TouchDirectionState.Up;
		}

		int moveY = ((int) ev.getY());
		// 检测Head
		if (!mIsActivingHead && mFirstItemIndex == 0) {
			// 检测是否是一次touch事件.
			if (mIsHeadRecord == false) {
				mHeadStartY = (int) moveY;
				mIsHeadRecord = true;
			}
			// 向下拉headview移动距离为y移动的一半.
			int offsetHead = (int) ((moveY - mHeadStartY) / RATIO);
			int abs_offsetHead = Math.abs(offsetHead);
			if (abs_offsetHead < mHeadMaxOverscrollDistance) { // 还没有到设定的最大位置
				int intervalTop = abs_offsetHead - mViewHeight; // 整个headView离开顶部距离
				if (offsetHead > 0) { // header下滑
					int top = 0;
					int buttom = 0;
					if (intervalTop <= 0) {
						top = intervalTop;
					} else {
						top = intervalTop / 2;
						buttom = top;
					}
					mHeadView.setPadding(0, top, 0, buttom);
					int progress = 0;
					if (intervalTop > 0) {
						progress = (int) (HEADRATE * intervalTop);
						progress = Math.min(progress, MAX_PROGRESS);
					}

					// 检测是否改变触发状态
					if (progress >= MAX_PROGRESS && !mHeadTouchActivate) {
						mHeadTouchActivate = true;
					} else if (progress < MAX_PROGRESS && mHeadTouchActivate) {
						mHeadTouchActivate = false;
					}

					mHeadProgressWheel.setProgress(progress);
					this.setSelection(0);
				} else {
					mHeadProgressWheel.setProgress(0);
					mHeadView.setPadding(0, -mViewHeight, 0, 0);
				}
			}
		}

		if (!mIsActivingHead && mHeadView.getPaddingTop() != -mViewHeight) // 如果非激活head状态，并且Header已经显示，返回
			return;

		// 检测Foot
		if (mIsButtom) {
			// header已经恢复到位，并且列表已经现实到底部时 footer上滑动
			// if (mIsFootRecord == false && mIsButtom) {
			if (mIsFootRecord == false) {
				mFootStartY = (int) moveY - mViewHeight;
				mIsFootRecord = true;
			}

			int offsetFoot = (int) ((moveY - mFootStartY) / RATIO);
			int abs_offsetFoot = Math.abs(offsetFoot);

			if (abs_offsetFoot < mFootMaxOverscrollDistance) { // 还没有到设定的最大位置
				if (offsetFoot <= 0) {
					int top = abs_offsetFoot / 2;
					if (mFootVisiable != View.VISIBLE) { // 如果当前没有显示，需要扣除本身高度
						top -= mViewHeight / 2;
					}
					int buttom = top;
					mFootInfoView.setPadding(0, top, 0, buttom);
					int progress = 0;
					if (abs_offsetFoot > 0) {
						progress = (int) (FOOTRATE * abs_offsetFoot);
						progress = Math.min(progress, MAX_PROGRESS);
					}

					// 检测是否改变触发状态
					if (progress >= MAX_PROGRESS && !mFootTouchActivate) {
						mFootTouchActivate = true;
					} else if (progress < MAX_PROGRESS && mFootTouchActivate) {
						mFootTouchActivate = false;
					}

					mFootProgressWheel.setProgress(progress);
					if (this.getAdapter() != null
							&& this.getAdapter().getCount() > 0)
						this.setSelection(this.getAdapter().getCount() - 1);
				}
			}

		}
	}

	/***
	 * 手势抬起操作
	 * 
	 * @param event
	 */
	private void doActionUp(MotionEvent event) {
		if (!mIsActivingHead && mHeadView.getPaddingTop() != -mViewHeight) { // Head没有恢复到位
			bounceHead();
		} else if (mFootInfoView.getPaddingTop() != 0) { // Foot 没有恢复到位
			bounceFoot();
		}

		NetworkUtil.isNetworkAvailable(getContext());
	}

	/** 反弹Header **/
	private void bounceHead() {

		if (mHeadProgressWheel.progress >= MAX_PROGRESS) {
			mIsActivingHead = true;
		} else {
			mIsActivingHead = false;
		}

		final int f_position = mHeadView.getPaddingTop()
				+ mHeadView.getPaddingBottom();
		final int f_step = 400;

		int viewHeight = mViewHeight;
		if (mIsActivingHead) // 激活状态,需要悬停
			viewHeight = 0;

		final float f_div = (float) (f_position + viewHeight) / f_step; // 回弹空间间隔;

		final float f_pgsFinalSetp = f_position / f_div; // 进度条为0时的setp(当position=0时进度条为:
															// f_position-
															// curStep*f_div=0）
		final int f_pgsLast = mHeadProgressWheel.progress; // 进度条当前位置

		mHeadAnimator = ValueAnimator.ofInt(0, f_step).setDuration(f_step);
		mHeadAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {

				int curStep = (Integer) animation.getAnimatedValue();
				int position = (int) (f_position - curStep * f_div);
				int top = 0;
				int buttom = 0;
				if (position <= 0) {
					top = position;
				} else {
					top = position / 2;
					buttom = top;
				}
				// 进度条定位
				if (!mIsActivingHead && mHeadProgressWheel.progress > 0) {
					int prg = (int) (Math.max(0, f_pgsFinalSetp - curStep)
							* f_pgsLast / f_pgsFinalSetp);
					mHeadProgressWheel.setProgress(prg);
				}
				if (!mIsActivingHead && mHeadProgressWheel.progress > 0) {
					int prg = (int) (Math.max(0, f_pgsFinalSetp - curStep)
							* f_pgsLast / f_pgsFinalSetp);
					mHeadProgressWheel.setProgress(prg);
				}
				mHeadView.setPadding(0, top, 0, buttom);
			}
		});
		mHeadAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mIsHeadAnimator = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mHeadAnimator = null;
				mIsHeadAnimator = false;
				mIsHeadRecord = false;
				mHeadTouchActivate = false;
				if (mIsActivingHead) {
					mHeadView.setPadding(0, 0, 0, 0);
					startHeadAnim();
				} else {
					mHeadView.setPadding(0, -mViewHeight, 0, 0);
				}
				if (mIsActivingHead && mOnListener != null)
					mOnListener.onHeadLoading();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mHeadAnimator = null;
				mIsHeadAnimator = false;
			}
		});
		mHeadAnimator.start();
	}

	/** 反弹Footer **/
	private void bounceFoot() {

		if (mFootProgressWheel.progress >= MAX_PROGRESS) {
			mIsActivingFoot = true;
		} else {
			mIsActivingFoot = false;
		}

		final int f_position = mFootInfoView.getPaddingTop()
				+ mFootInfoView.getPaddingBottom();
		final int f_step = 400;

		final float f_div = (float) f_position / f_step;// 回弹空间间隔

		final int f_pgsLast = mFootProgressWheel.progress;// 进度条当前位置

		mFootAnimator = ValueAnimator.ofInt(0, f_step).setDuration(f_step);
		mFootAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {

				int curStep = (Integer) animation.getAnimatedValue();
				int position = (int) (f_position - curStep * f_div);
				int top = Math.max(0, position / 2);
				int buttom = top;

				// 进度条定位
				if (!mIsActivingFoot && mFootProgressWheel.progress > 0) {
					int prg = Math.max(0, f_step - curStep) * f_pgsLast
							/ f_step;
					mFootProgressWheel.setProgress(prg);
				}

				// 进度条定位
				if (!mIsActivingFoot && mFootProgressWheel.progress > 0) {
					int prg = Math.max(0, f_step - curStep) * f_pgsLast
							/ f_step;
					mFootProgressWheel.setProgress(prg);
				}
				// 设置位置
				mFootInfoView.setPadding(0, top, 0, buttom);
			}
		});
		mFootAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mIsFootAnimator = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mHeadAnimator = null;
				mIsFootAnimator = false;
				mIsFootRecord = false;
				mFootTouchActivate = false;
				mFootInfoView.setPadding(0, 0, 0, 0);
				if (mIsActivingFoot) {
					startFootAnim();
				}
				if (mIsActivingFoot && mOnListener != null)
					mOnListener.onFootLoading();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mFootAnimator = null;
				mIsFootAnimator = false;
			}
		});
		mFootAnimator.start();
	}

	private void startFootAnim() {
		startAnim(mFootProgressWheel);
	}

	private void stopFootAnim() {
		stopAnim();
	}

	private void stopHeadAnim() {
		stopAnim();
	}

	private void startHeadAnim() {
		startAnim(mHeadProgressWheel);
	}

	private ObjectAnimator mObjectAnimator = null;

	private void startAnim(View view) {

		mObjectAnimator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
		mObjectAnimator.setDuration(1000);
		mObjectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
		mObjectAnimator.start();

	}

	private void stopAnim() {
		if (mObjectAnimator != null) {
			mObjectAnimator.end();
		}
	}

	// ------------------------------Public------------------------------
	/** 停止Foot的activing状态 **/
	public void stopFootLoading() {
		if (!mIsActivingFoot)
			return;
		if (mFootAnimator != null) {
			mFootAnimator.cancel();
			mFootAnimator = null;
		}
		mIsActivingFoot = false;
		mFootProgressWheel.setProgress(0);
		mFootInfoView.setPadding(0, 0, 0, 0);
		mFootProgressWheel.setVisibility(View.VISIBLE);

		stopFootAnim();

		mOnListener.onFootLoaded();
	}

	/** 停止head的加载状态 **/
	public void stopHeadLoading() {
		if (!mIsActivingHead)
			return;
		if (mHeadAnimator != null) {
			mHeadAnimator.cancel();
			mHeadAnimator = null;
		}
		mIsActivingHead = false;

		final int f_position = mHeadView.getPaddingTop() + mViewHeight
				+ mHeadView.getPaddingBottom();
		if (f_position > 0) {
			final int f_step = 200;
			final float f_div = (float) (f_position) / f_step; // 回弹空间间隔;
			ValueAnimator animator = ValueAnimator.ofInt(0, f_step)
					.setDuration(f_step);
			animator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {

					int curStep = (Integer) animation.getAnimatedValue();

					int top = (int) (-curStep * f_div);

					mHeadView.setPadding(0, top, 0, 0);
				}
			});
			animator.addListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mHeadProgressWheel.setVisibility(View.VISIBLE);
					mHeadView.setPadding(0, -mViewHeight, 0, 0);

				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			animator.start();
		}

		stopHeadAnim();

		mOnListener.onHeadLoaded();
	}

	/** 是否允许滑动Header **/
	public void setEnableHeader(boolean enable) {
		setHeadVisiable(enable);
		if (enable)
			mHeadMaxOverscrollDistance = Tools.dip2px(mContext,
					MaxOverscrollDistance);
		else
			mHeadMaxOverscrollDistance = 0;
	}

	/** 是否允许滑动Footer **/
	public void setEnableFooter(boolean enable) {
		setFootVisiable(enable);
		if (enable)
			mFootMaxOverscrollDistance = Tools.dip2px(mContext,
					MaxOverscrollDistance);
		else
			mFootMaxOverscrollDistance = 0;
	}

	/** 是否显示Head **/
	public void setHeadVisiable(boolean visable) {
		if (visable) {
			mHeadView.setVisibility(View.VISIBLE);
			mHeadVisiable = View.VISIBLE;
		} else {
			mHeadView.setVisibility(View.INVISIBLE);
			mHeadVisiable = View.INVISIBLE;
		}
	}

	/** 是否显示Foot **/
	public void setFootVisiable(boolean visable) {
		if (visable) {
			mFootInfoView.setPadding(0, 0, 0, 0);
			mFootView.setVisibility(View.VISIBLE);
			mFootVisiable = View.VISIBLE;
		} else {
			mFootVisiable = View.INVISIBLE;
			mFootView.setVisibility(View.INVISIBLE);
			mFootInfoView.setPadding(0, 0, 0, -mViewHeight);

		}
	}

	/** 设置Head提示内容 **/
	public void setHeadTips(String tip) {
		mHeadTips.setText(tip);
	}

	/** 设置Foot提示内容 **/
	public void setFootTips(String tip) {
		mFootTips.setText(tip);
	}

	public void setOnListener(OnListener listener) {
		this.mOnListener = listener;
	}

	public interface OnListener {
		// 正在加载
		public void onHeadLoading();

		public void onFootLoading();

		// 加载完毕
		public void onHeadLoaded();

		public void onFootLoaded();

		public void onScroll(int delta, int scrollPosition, boolean exact);

	}

}