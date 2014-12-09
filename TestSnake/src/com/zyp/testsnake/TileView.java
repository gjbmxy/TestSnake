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

	protected static int mTileSize;// 每个tile的边长的像素数量
	// bitmap图，将方块加载到这个数组，resettiles，loadtile
	private Bitmap[] mTileArray;
	// settile cleartile操作 图形显示 ==画布
	private int[][] mTileGrid;
	// canvas绘图 paint画笔实现
	private final Paint mPaint = new Paint();

	public TileView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TileView);

		mTileSize = a.getInt(R.styleable.TileView_tileSize, 30);//原值为12为改变蛇胖度改为30

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


	// 用tilecount重置字典
	public void resetTiles(int tilecount)
	{
		mTileArray = new Bitmap[tilecount];
	}

	// 屏幕尺寸改变时，调整tile计数指标

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		mXTileCount = (int) Math.floor(w / mTileSize);
		mYTileCount = (int) Math.floor(h / mTileSize);
		mXOffset = ((w - (mTileSize * mXTileCount)) / 2);
		mYOffset = ((h - (mTileSize * mYTileCount)) / 2);//误将h'写为w导致画布绘制不完整
		// mTileGrid==画布
		mTileGrid = new int[mXTileCount][mYTileCount];
		clearTiles();
	}

	// 加载具体的砖块图片 到 砖块字典。 即将对应的砖块的图片 对应的加载到 mTileArray数组中
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


	// 清空图形，更新画面，调用了绘图的settile
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
	// 将画布绘制到手机界面上

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
