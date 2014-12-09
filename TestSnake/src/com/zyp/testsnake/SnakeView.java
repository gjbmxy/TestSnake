package com.zyp.testsnake;

import java.util.ArrayList;
import java.util.Random;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class SnakeView extends TileView
{
	/**
	 * Create a simple handler that we can use to cause animation to happen. We
	 * set ourselves as a target and we can use the sleep() function to cause an
	 * update/invalidate to occur at a later date. 用Handler机制实现定时刷新。
	 * 为什么使用Handler呢？大家可以参考 android 的线程模型（注意UI线程不是线程安全的～） 具体使用方法网上的资源很多，在此不赘述～
	 */
	private RefreshHandler mRedrawHandler = new RefreshHandler();

	/**
	 * mLastMove: tracks the absolute time when the snake last moved, and is
	 * used to determine if a move should be made based on mMoveDelay.
	 * 记录上次移动的确切时间。 同mMoveDelay一起处理与用户的异步操作的协同问题。
	 */
	private long mLastMove;

	/**
	 * mScore: used to track the number of apples captured mMoveDelay: number of
	 * milliseconds between snake movements. This will decrease as apples are
	 * captured.
	 */
	private long mScore = 0;// 记录获得的分数。
	private long mMoveDelay = 600;// 每移动一步的延时。初始时设置为600ms，以后每吃一个果子，打个9折,造成的结果是速度越来越快。

	/**
	 * mStatusText: text shows to the user in some run states 用来显示游戏状态的TextView
	 */
	private TextView mStatusText;

	/**
	 * Current mode of application: READY to run, RUNNING, or you have already
	 * lost. static final ints are used instead of an enum for performance
	 * reasons. 游戏的四种状态。初始时为 预备开始的状态。
	 */
	int mMode = READY;
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;

	private static final String TAG = "SnakeView";

	/**
	 * Everyone needs a little randomness in their life
	 * 随机数生成器。用来产生随机的苹果。在addRandomApple()中使用。
	 */
	private static final Random RNG = new Random();

	/**
	 * Current direction the snake is headed. 蛇体运动的方向标识。
	 */
	int mDirection = NORTH;
	int mNextDirection = NORTH;
	static final int NORTH = 1;
	static final int SOUTH = 2;
	static final int EAST = 3;
	static final int WEST = 4;
	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 * 游戏中仅有的三种砖块对应的数值。
	 */
	static final int RED_STAR = 1;
	static final int YELLOW_STAR = 2;
	static final int GREEN_STAR = 3;
	/**
	 * mSnakeTrail: a list of Coordinates that make up the snake's body
	 * mAppleList: the secret location of the juicy apples the snake craves.
	 * 两个链表，分别用来存储 蛇体 和 果子的坐标。 每次蛇体的运动，蛇体的增长，产生新的苹果，被吃掉苹果，都会在这里记录。
	 */
	private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();

	public SnakeView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initSnakeView();// 构造函数中，别忘了，初始化游戏～
	}

	public SnakeView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initSnakeView();// 构造函数中，别忘了，初始化游戏～
	}

	class RefreshHandler extends Handler
	{

		@Override
		public void handleMessage(Message msg)
		{
			SnakeView.this.update();
			SnakeView.this.invalidate();
		}

		public void sleep(long delayMillis)
		{
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}

	};

	public class Coordinate
	{
		public int x;
		public int y;

		public Coordinate(int newX, int newY)
		{
			x = newX;
			y = newY;
		}

		public boolean equals(Coordinate other)
		{
			if (x == other.x && y == other.y)
			{
				return true;
			}
			return false;
		}

		@Override
		public String toString()
		{
			return "Coordinate:[" + x + "," + y + "]";
		}

	}

	private void initSnakeView()
	{
		setFocusable(true);
		Resources r = this.getContext().getResources();

		resetTiles(4);
		loadTile(RED_STAR, r.getDrawable(R.drawable.redstar));
		loadTile(YELLOW_STAR, r.getDrawable(R.drawable.yellowstar));
		loadTile(GREEN_STAR, r.getDrawable(R.drawable.greenstar));

	}

	/**
	 * Selects a random location within the garden that is not currently covered
	 * by the snake. Currently _could_ go into an infinite loop if the snake
	 * currently fills the garden, but we'll leave discovery of this prize to a
	 * truly excellent snake-player. 在地图上随机的增加果子。注意苹果的位置不可以是蛇体所在噢～这里有个小bug，没有检测
	 * 产生的果子位置 可能与 另一个果子位置重合。 新产生的果子的坐标会增加到mApplist的数组上。
	 */
	private void addRandomApple()
	{
		Coordinate newCoord = null;
		boolean found = false;
		while (!found)
		{
			// donnot let the apple on the edge;
			int newX = 1 + RNG.nextInt(mXTileCount - 2);
			int newY = 1 + RNG.nextInt(mYTileCount - 2);
			newCoord = new Coordinate(newX, newY);
			// make sure it's not under the snake
			boolean collison = false;
			int snakelength = mSnakeTrail.size();
			for (int index = 0; index < snakelength; index++)
			{
				if (mSnakeTrail.get(index).equals(newCoord))
				{
					collison = true;
				}
			}
			found = !collison;
		}
		if (newCoord == null)
		{
			Log.e(TAG, "SOMEHOW ended up with a null newcoord");
		}
		mAppleList.add(newCoord);//缺少这句导致无法产生apple
	}

	void initNewGame()// 清空保存蛇体和果子的数据结构
	{
		mSnakeTrail.clear();
		mAppleList.clear();
		// 设定初始状态蛇体的位置
		mSnakeTrail.add(new Coordinate(7, 7));
		mSnakeTrail.add(new Coordinate(6, 7));
		mSnakeTrail.add(new Coordinate(5, 7));
		mSnakeTrail.add(new Coordinate(4, 7));
		mSnakeTrail.add(new Coordinate(3, 7));
		mSnakeTrail.add(new Coordinate(2, 7));
		mNextDirection = NORTH;
		addRandomApple();
		addRandomApple();

		mMoveDelay = 500;
		mScore = 0;
	}

	/**
	 * Given a ArrayList of coordinates, we need to flatten them into an array
	 * of ints before we can stuff them into a map for flattening and storage.
	 * 
	 * @param cvec
	 *            : a ArrayList of Coordinate objects
	 * @return : a simple array containing the x/y values of the coordinates as
	 *         [x1,y1,x2,y2,x3,y3...】 在游戏暂停时，需要通过Bundle方式保存数据。见saveState()。
	 *         Bundle支持简单的数组。 所以需要将我们的部分数据结构，如蛇体和苹果位置的数组，转换成简单的序列化的int数组。
	 * */
	private int[] coordArrayListToArray(ArrayList<Coordinate> cvec)
	{
		int count = cvec.size();// 获取长度
		int[] rawArray = new int[count * 2];// 建立2倍长度的数组
		for (int index = 0; index < count; index++)
		{
			Coordinate c = cvec.get(index);// 将coordinate中的数据赋值到数组
			rawArray[2 * index] = c.x;
			rawArray[2 * index + 1] = c.y;// x,y 01 23 45 67
		}
		return rawArray;
	}

	/**
	 * Given a flattened array of ordinate pairs, we reconstitute them into a
	 * ArrayList of Coordinate objects
	 * 是coordArrayListToArray（）的逆过程，用来读取保存在Bundle中的数据。
	 * 
	 * @param rawArray
	 *            : [x1,y1,x2,y2,...]
	 * @return a ArrayList of Coordinates
	 */
	private ArrayList<Coordinate> coorArrayToArrayList(int[] rawArray)
	{
		ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();
		int coorCount = rawArray.length;
		for (int index = 0; index < coorCount; index += 2)
		{
			Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
			coordArrayList.add(c);
		}
		return coordArrayList;
	}

	// ===================================================================
	/**
	 * Save game state so that the user does not lose anything if the game
	 * process is killed while we are in the background.
	 * 在意外情况下，暂时性保存游戏数据，在下次打开游戏时，可以继续游戏。如来电话了。
	 * 
	 * @return a Bundle with this view's state
	 */
	public Bundle saveState()
	{
		Bundle map = new Bundle();// 用于Android的Activity之间传递数据的类

		map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
		map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));
		map.putInt("mDirection", Integer.valueOf(mDirection));
		map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
		map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
		map.putLong("mScore", Long.valueOf(mScore));

		return map;
	}

	/**
	 * Restore game state if our process is being relaunched
	 * 回复游戏数据。是saveState()的逆过程
	 * 
	 * @param icicle
	 *            a Bundle containing the game state
	 */
	public void restoreState(Bundle icicle)
	{
		setMode(PAUSE);

		mAppleList = coorArrayToArrayList(icicle.getIntArray("mAppleList"));
		mDirection = icicle.getInt("mDirection");
		mNextDirection = icicle.getInt("mNextDirection");
		mMoveDelay = icicle.getLong("mMoveDelay");
		mScore = icicle.getLong("mScore");
		mSnakeTrail = coorArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
	}

	/**
	 * Sets the TextView that will be used to give information (such as "Game
	 * Over" to the user. 起初不明白这个方法有什么作用。删除了以后才发现错误。Snake类会调用到它，来绑定到相应的textview.
	 */
	public void setTextView(TextView newView)
	{
		mStatusText = newView;
	}

	/**
	 * Updates the current mode of the application (RUNNING or PAUSED or the
	 * like) as well as sets the visibility of textview for notification
	 * 
	 * @param newMode
	 */
	public void setMode(int newMode)
	{
		int oldMode = mMode;
		mMode = newMode;

		if (newMode == RUNNING & oldMode != RUNNING)
		{
			mStatusText.setVisibility(View.INVISIBLE);
			update();
			return;

		}
		Resources res = getContext().getResources();
		// CharSequence类型,这是一个接口，代表的是一个有序字符集合
		// 对于一个抽象类或者是接口类，不能使用new来进行赋值，
		// 但是可以通过以下的方式来进行实例的创建：
		CharSequence str = "";

		if (newMode == PAUSE)
		{
			str = res.getText(R.string.mode_pause);
		}
		if (newMode == READY)
		{
			str = res.getText(R.string.mode_ready);
		}
		if (newMode == LOSE)
		{
			str = res.getString(R.string.mode_lose_prefix) +"吃了apple："+ mScore
					+ res.getString(R.string.mode_lose_suffix);
		}

		mStatusText.setText(str);
		mStatusText.setVisibility(View.VISIBLE);
	}

	/*
	 * handles key events in the game. Update the direction our snake is
	 * traveling based on the DPAD. Ignore events that would cause the snake to
	 * immediately turn back on itself. 按键的监听。 现在大多数的android手机都没有按键了。
	 * 笔者就是在自己的模拟机上才能正常的使用这款小游戏的 - -#
	 * 
	 * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
	 */

	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the snake's
	 * location. 刷新游戏状态。每次游戏画面的更新、游戏数据的更新，都是依靠这个update()来完成的。
	 */
	public void update()
	{
		if (mMode == RUNNING)
		{
			long now = System.currentTimeMillis();

			if (now - mLastMove > mMoveDelay)
			{
				clearTiles();
				updateWalls();
				updateSnake();
				updateApples();
				mLastMove = now;
			}
			mRedrawHandler.sleep(mMoveDelay);
		}
	}

	/**
	 * Draws some walls. 用setTile绘制墙壁
	 */
	private void updateWalls()
	{
		for (int x = 0; x < mXTileCount; x++)
		{
			setTile(GREEN_STAR, x, 0);
			setTile(GREEN_STAR, x, mYTileCount - 1);
		}
		for (int y = 1; y < mYTileCount - 1; y++)
		{
			setTile(GREEN_STAR, 0, y);
			setTile(GREEN_STAR, mXTileCount - 1, y);
		}
	}

	/**
	 * Draws some apples. 绘制果子
	 */
	private void updateApples()
	{
		//遍历c，就和for(int i = 0;i  < a.length; i++){} 是一个意思。
		for (Coordinate c : mAppleList)
		{
			setTile(YELLOW_STAR, c.x, c.y);
		}
	}

	/**
	 * Figure out which way the snake is going, see if he's run into anything
	 * (the walls, himself, or an apple). If he's not going to die, we then add
	 * to the front and subtract from the rear in order to simulate motion. If
	 * we want to grow him, we don't subtract from the rear.
	 * 
	 */
	public void updateSnake()
	{
		boolean growSnake = false;
		Coordinate head = mSnakeTrail.get(0);
		Coordinate newHead = new Coordinate(1, 1);

		mDirection = mNextDirection;

		switch (mDirection)
		{
		case EAST:
		{
			newHead = new Coordinate(head.x + 1, head.y);
			break;
		}
		case WEST:
		{
			newHead = new Coordinate(head.x - 1, head.y);
			break;
		}
		case NORTH:
		{
			newHead = new Coordinate(head.x, head.y - 1);
			break;
		}
		case SOUTH:
		{
			newHead = new Coordinate(head.x, head.y + 1);
			break;
		}
		}
		// Collision detection
		// For now we have a 1-square wall around the entire arena
		// 撞墙检测
		if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > mXTileCount - 2)
				|| (newHead.y > mYTileCount - 2))
		{
			setMode(LOSE);
			return;
		}
		// Look for collisions with itself
		// 撞自己检测
		int snakelength = mSnakeTrail.size();
		for (int snakeindex = 0; snakeindex < snakelength; snakeindex++)
		{
			Coordinate c = mSnakeTrail.get(snakeindex);
			if (c.equals(newHead))
			{
				setMode(LOSE);
				return;
			}
		}

		// Look for apples
		// 吃果子检测
		int applecount = mAppleList.size();
		for (int appleindex = 0; appleindex < applecount; appleindex++)
		{
			Coordinate c = mAppleList.get(appleindex);
			if (c.equals(newHead))
			{
				mAppleList.remove(c);
				addRandomApple();

				mScore++;
				mMoveDelay *= 0.8;

				growSnake = true;
			}
		}
		// push a new head onto the ArrayList and pull off the tail
		// 前进
		mSnakeTrail.add(0, newHead);
		// except if we want the snake to grow
		if (!growSnake)
		{
			mSnakeTrail.remove(mSnakeTrail.size() - 1);
		}
		// 绘制新的蛇体
		int index = 0;
		for (Coordinate c : mSnakeTrail)
		{
			if (index == 0)
			{
				setTile(YELLOW_STAR, c.x, c.y);
			} else
			{
				setTile(RED_STAR, c.x, c.y);
			}
			index++;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg)
	{
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
		{
			if (mMode == READY | mMode == LOSE)
			{
				/*
				 * At the beginning of the game, or the end of a previous one,
				 * we should start a new game.
				 */
				initNewGame();
				setMode(RUNNING);
				update();
				return (true);
			}
			if (mMode == PAUSE)
			{
				setMode(RUNNING);
				update();
				return (true);
			}
			if (mDirection != SOUTH)
			{
				mNextDirection = NORTH;
			}
			return (true);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
		{
			if (mDirection != NORTH)
			{
				mNextDirection = SOUTH;
			}
			return (true);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
		{
			if (mDirection != EAST)
			{
				mNextDirection = WEST;
			}
			return (true);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
		{
			if (mDirection != WEST)
			{
				mNextDirection = EAST;
			}
			return (true);
		}
		return super.onKeyDown(keyCode, msg);
	}

}
