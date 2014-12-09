package com.zyp.testsnake;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Snake extends Activity implements OnClickListener
{
	private SnakeView mSnakeView;

	// 声明变量之前需引入头文件import android.widget.Button;ImageButton;
	private Button play;
	private ImageButton left;
	private ImageButton right;
	private ImageButton up;
	private ImageButton down;

	private final static int PLAY = 1;
	private final static int LEFT = 2;
	private final static int RIGHT = 3;
	private final static int UP = 4;
	private final static int DOWN = 5;

	private static String ICICLE_KEY = "snake-view";

	protected static final int GUINOTIFIER = 0x1234;

	private Handler handler;

	private UpdateStatus updateStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// requestWindowFeature设置title、progress、fullscream等
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 载入布局文件--main.xml
		setContentView(R.layout.snake_layout);
		// 将R文件中的view对象的id赋值给new出来的对象
		mSnakeView = (SnakeView) findViewById(R.id.snake);
		// used to give information to user like Game Over
		mSnakeView.setTextView((TextView) findViewById(R.id.text));

		// 将布局文件中的view对象的id赋值给play变量,并给play变量赋数值
		play = (Button) findViewById(R.id.play);
		play.setId(PLAY);
		play.setOnClickListener(this);// this这种情况是implement了setonclicklistener
		play.setBackgroundColor(Color.argb(0, 0, 255, 0));// green

		left = (ImageButton) findViewById(R.id.left);
		left.setId(LEFT);// setId()其中I为大写，否则不提示
		left.setOnClickListener(this);
		left.setBackgroundColor(Color.argb(1, 1, 255, 1));
		left.setVisibility(View.GONE);// 参数来自View类

		right = (ImageButton) findViewById(R.id.right);
		right.setId(RIGHT);
		right.setOnClickListener(this);
		right.setBackgroundColor(Color.argb(1, 1, 255, 1));
		right.setVisibility(View.GONE);

		up = (ImageButton) findViewById(R.id.up);
		up.setId(UP);
		up.setOnClickListener(this);
		up.setBackgroundColor(Color.argb(1, 1, 255, 1));
		up.setVisibility(View.GONE);

		down = (ImageButton) findViewById(R.id.down);
		down.setId(DOWN);
		down.setOnClickListener(this);
		down.setBackgroundColor(Color.argb(1, 1, 255, 1));
		down.setVisibility(View.GONE);

		// 判断游戏从什么状态开始
		if (savedInstanceState == null)
		{
			mSnakeView.setMode(mSnakeView.READY);
		} else
		{
			Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
			if (map != null)
			{
				mSnakeView.restoreState(map);
			} else
			{
				mSnakeView.setMode(SnakeView.PAUSE);
			}

		}

		// handler是android中为了处理异步线程更新UI的问题而出现的一个工具。
		// 在android异步线程是不能够更新UI的，只能在主线程中更新UI。
		// 匿名内部类
		handler = new Handler()
		{
			public void handleMessage(Message msg)//函数方法名写错了导致多线程没有更新
			{
				switch (msg.what)
				{
				case Snake.GUINOTIFIER:
					play.setVisibility(View.VISIBLE);
					left.setVisibility(View.GONE);
					right.setVisibility(View.GONE);
					up.setVisibility(View.GONE);
					down.setVisibility(View.GONE);
					break;

				}
				super.handleMessage(msg);
			}
		};

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mSnakeView.setMode(SnakeView.PAUSE);
		play.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
		case PLAY:
			play.setVisibility(View.GONE);
			left.setVisibility(View.VISIBLE);
			right.setVisibility(View.VISIBLE);
			up.setVisibility(View.VISIBLE);
			down.setVisibility(View.VISIBLE);
			if (mSnakeView.mMode == mSnakeView.READY
					| mSnakeView.mMode == mSnakeView.LOSE)
			{
				
				mSnakeView.initNewGame();
				mSnakeView.setMode(mSnakeView.RUNNING);
				mSnakeView.update();
				
				updateStatus = new UpdateStatus();
				updateStatus.start();
				break;
			}
			if (mSnakeView.mMode == mSnakeView.PAUSE)
			{
				//play.setVisibility(View.VISIBLE);
				mSnakeView.setMode(mSnakeView.RUNNING);
				mSnakeView.update();

				break;
			}
			if (mSnakeView.mDirection != mSnakeView.NORTH)
			{
				mSnakeView.mNextDirection = mSnakeView.NORTH;
				break;
			}
			break;
			
		case LEFT:
			if (mSnakeView.mDirection != mSnakeView.EAST)
			{
				mSnakeView.mNextDirection = mSnakeView.WEST;
			}
			break;
		case RIGHT:
			if (mSnakeView.mDirection != mSnakeView.WEST)
			{
				mSnakeView.mNextDirection = mSnakeView.EAST;
			}
			break;
		case UP:
			if (mSnakeView.mDirection != mSnakeView.SOUTH)
			{
				mSnakeView.mNextDirection = mSnakeView.NORTH;
			}
			break;
		case DOWN:
			if (mSnakeView.mDirection != mSnakeView.NORTH)
			{
				mSnakeView.mNextDirection = mSnakeView.SOUTH;
			}
			break;		
		default:
			break;
		}

	}

	
	class UpdateStatus extends Thread
	{

		@Override
		public void run()
		{

			super.run();

			while (true)
			{
				if (mSnakeView.mMode == mSnakeView.LOSE)
				{
					Message m = new Message();
					m.what = Snake.GUINOTIFIER;
					Snake.this.handler.sendMessage(m);

					break;
				}
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

	}

}
