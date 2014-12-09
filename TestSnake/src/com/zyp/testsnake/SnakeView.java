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
	 * update/invalidate to occur at a later date. ��Handler����ʵ�ֶ�ʱˢ�¡�
	 * Ϊʲôʹ��Handler�أ���ҿ��Բο� android ���߳�ģ�ͣ�ע��UI�̲߳����̰߳�ȫ�ġ��� ����ʹ�÷������ϵ���Դ�ܶ࣬�ڴ˲�׸����
	 */
	private RefreshHandler mRedrawHandler = new RefreshHandler();

	/**
	 * mLastMove: tracks the absolute time when the snake last moved, and is
	 * used to determine if a move should be made based on mMoveDelay.
	 * ��¼�ϴ��ƶ���ȷ��ʱ�䡣 ͬmMoveDelayһ�������û����첽������Эͬ���⡣
	 */
	private long mLastMove;

	/**
	 * mScore: used to track the number of apples captured mMoveDelay: number of
	 * milliseconds between snake movements. This will decrease as apples are
	 * captured.
	 */
	private long mScore = 0;// ��¼��õķ�����
	private long mMoveDelay = 600;// ÿ�ƶ�һ������ʱ����ʼʱ����Ϊ600ms���Ժ�ÿ��һ�����ӣ����9��,��ɵĽ�����ٶ�Խ��Խ�졣

	/**
	 * mStatusText: text shows to the user in some run states ������ʾ��Ϸ״̬��TextView
	 */
	private TextView mStatusText;

	/**
	 * Current mode of application: READY to run, RUNNING, or you have already
	 * lost. static final ints are used instead of an enum for performance
	 * reasons. ��Ϸ������״̬����ʼʱΪ Ԥ����ʼ��״̬��
	 */
	int mMode = READY;
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;

	private static final String TAG = "SnakeView";

	/**
	 * Everyone needs a little randomness in their life
	 * ��������������������������ƻ������addRandomApple()��ʹ�á�
	 */
	private static final Random RNG = new Random();

	/**
	 * Current direction the snake is headed. �����˶��ķ����ʶ��
	 */
	int mDirection = NORTH;
	int mNextDirection = NORTH;
	static final int NORTH = 1;
	static final int SOUTH = 2;
	static final int EAST = 3;
	static final int WEST = 4;
	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 * ��Ϸ�н��е�����ש���Ӧ����ֵ��
	 */
	static final int RED_STAR = 1;
	static final int YELLOW_STAR = 2;
	static final int GREEN_STAR = 3;
	/**
	 * mSnakeTrail: a list of Coordinates that make up the snake's body
	 * mAppleList: the secret location of the juicy apples the snake craves.
	 * ���������ֱ������洢 ���� �� ���ӵ����ꡣ ÿ��������˶�������������������µ�ƻ�������Ե�ƻ���������������¼��
	 */
	private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();

	public SnakeView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initSnakeView();// ���캯���У������ˣ���ʼ����Ϸ��
	}

	public SnakeView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initSnakeView();// ���캯���У������ˣ���ʼ����Ϸ��
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
	 * truly excellent snake-player. �ڵ�ͼ����������ӹ��ӡ�ע��ƻ����λ�ò����������������ޡ������и�Сbug��û�м��
	 * �����Ĺ���λ�� ������ ��һ������λ���غϡ� �²����Ĺ��ӵ���������ӵ�mApplist�������ϡ�
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
		mAppleList.add(newCoord);//ȱ����䵼���޷�����apple
	}

	void initNewGame()// ��ձ�������͹��ӵ����ݽṹ
	{
		mSnakeTrail.clear();
		mAppleList.clear();
		// �趨��ʼ״̬�����λ��
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
	 *         [x1,y1,x2,y2,x3,y3...�� ����Ϸ��ͣʱ����Ҫͨ��Bundle��ʽ�������ݡ���saveState()��
	 *         Bundle֧�ּ򵥵����顣 ������Ҫ�����ǵĲ������ݽṹ���������ƻ��λ�õ����飬ת���ɼ򵥵����л���int���顣
	 * */
	private int[] coordArrayListToArray(ArrayList<Coordinate> cvec)
	{
		int count = cvec.size();// ��ȡ����
		int[] rawArray = new int[count * 2];// ����2�����ȵ�����
		for (int index = 0; index < count; index++)
		{
			Coordinate c = cvec.get(index);// ��coordinate�е����ݸ�ֵ������
			rawArray[2 * index] = c.x;
			rawArray[2 * index + 1] = c.y;// x,y 01 23 45 67
		}
		return rawArray;
	}

	/**
	 * Given a flattened array of ordinate pairs, we reconstitute them into a
	 * ArrayList of Coordinate objects
	 * ��coordArrayListToArray����������̣�������ȡ������Bundle�е����ݡ�
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
	 * ����������£���ʱ�Ա�����Ϸ���ݣ����´δ���Ϸʱ�����Լ�����Ϸ�������绰�ˡ�
	 * 
	 * @return a Bundle with this view's state
	 */
	public Bundle saveState()
	{
		Bundle map = new Bundle();// ����Android��Activity֮�䴫�����ݵ���

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
	 * �ظ���Ϸ���ݡ���saveState()�������
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
	 * Over" to the user. ������������������ʲô���á�ɾ�����Ժ�ŷ��ִ���Snake�����õ��������󶨵���Ӧ��textview.
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
		// CharSequence����,����һ���ӿڣ��������һ�������ַ�����
		// ����һ������������ǽӿ��࣬����ʹ��new�����и�ֵ��
		// ���ǿ���ͨ�����µķ�ʽ������ʵ���Ĵ�����
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
			str = res.getString(R.string.mode_lose_prefix) +"����apple��"+ mScore
					+ res.getString(R.string.mode_lose_suffix);
		}

		mStatusText.setText(str);
		mStatusText.setVisibility(View.VISIBLE);
	}

	/*
	 * handles key events in the game. Update the direction our snake is
	 * traveling based on the DPAD. Ignore events that would cause the snake to
	 * immediately turn back on itself. �����ļ����� ���ڴ������android�ֻ���û�а����ˡ�
	 * ���߾������Լ���ģ����ϲ���������ʹ�����С��Ϸ�� - -#
	 * 
	 * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
	 */

	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the snake's
	 * location. ˢ����Ϸ״̬��ÿ����Ϸ����ĸ��¡���Ϸ���ݵĸ��£������������update()����ɵġ�
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
	 * Draws some walls. ��setTile����ǽ��
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
	 * Draws some apples. ���ƹ���
	 */
	private void updateApples()
	{
		//����c���ͺ�for(int i = 0;i  < a.length; i++){} ��һ����˼��
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
		// ײǽ���
		if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > mXTileCount - 2)
				|| (newHead.y > mYTileCount - 2))
		{
			setMode(LOSE);
			return;
		}
		// Look for collisions with itself
		// ײ�Լ����
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
		// �Թ��Ӽ��
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
		// ǰ��
		mSnakeTrail.add(0, newHead);
		// except if we want the snake to grow
		if (!growSnake)
		{
			mSnakeTrail.remove(mSnakeTrail.size() - 1);
		}
		// �����µ�����
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
