package com.zyp.testsnake;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class TileView extends View
{
	protected static int mXTileCount;
	protected static int mYTileCount;

	protected static int mXOffset;
	protected static int mYOffset;

	protected static int mTileSize;// ÿ��tile�ı߳�����������
	// bitmapͼ����������ص�������飬resettiles��loadtile
	private Bitmap[] mTileArray;
	// settile cleartile���� ͼ����ʾ ==����
	private int[][] mTileGrid;
	// canvas��ͼ paint����ʵ��
	private final Paint mPaint = new Paint();

	public TileView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TileView);

		mTileSize = a.getInt(R.styleable.TileView_tileSize, 30);//ԭֵΪ12Ϊ�ı����ֶȸ�Ϊ30

		a.recycle();
	}

	public TileView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TileView);
		mTileSize = a.getInt(R.styleable.TileView_tileSize, 30);
		a.recycle();
	}


	// ��tilecount�����ֵ�
	public void resetTiles(int tilecount)
	{
		mTileArray = new Bitmap[tilecount];
	}

	// ��Ļ�ߴ�ı�ʱ������tile����ָ��

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		mXTileCount = (int) Math.floor(w / mTileSize);
		mYTileCount = (int) Math.floor(h / mTileSize);
		mXOffset = ((w - (mTileSize * mXTileCount)) / 2);
		mYOffset = ((h - (mTileSize * mYTileCount)) / 2);//��h'дΪw���»������Ʋ�����
		// mTileGrid==����
		mTileGrid = new int[mXTileCount][mYTileCount];
		clearTiles();
	}

	// ���ؾ����ש��ͼƬ �� ש���ֵ䡣 ������Ӧ��ש���ͼƬ ��Ӧ�ļ��ص� mTileArray������
	public void loadTile(int key, Drawable tile)
	{
		Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		tile.setBounds(0, 0, mTileSize, mTileSize);
		tile.draw(canvas);
		mTileArray[key] = bitmap;
	}

	//


	// ���ͼ�Σ����»��棬�����˻�ͼ��settile
	public void clearTiles()
	{
		for (int x = 0; x < mXTileCount; x++)
		{
			for (int y = 0; y < mYTileCount; y++)
			{
				setTile(0, x, y);
			}
		}
	}
	public void setTile(int tileindex, int x, int y)
	{
		mTileGrid[x][y] = tileindex;
	}
	// ���������Ƶ��ֻ�������

	@Override
	protected void onDraw(Canvas canvas)
	{
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		for(int x=0;x<mXTileCount;x+=1)
		{
			for(int y=0;y<mYTileCount;y+=1)
			{
				if(mTileGrid[x][y]>0)
				{
					canvas.drawBitmap(mTileArray[mTileGrid[x][y]],
							mXOffset+x*mTileSize,
							mYOffset+y*mTileSize,
							mPaint);
				}
			}
		}
	}

}
