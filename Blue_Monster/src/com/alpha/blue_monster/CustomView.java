package com.alpha.blue_monster;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CustomView extends SurfaceView implements Runnable{
	SurfaceHolder ourHolder;
	Thread ourThread =null;
	private boolean isRunning = false;
	private boolean backwards = false;
	Bitmap blueMonster;
	Bitmap fblueMonster;
	Bitmap blueMonsterShy;
	Bitmap fblueMonsterShy;
	Bitmap blueMonsterSmile;
	Bitmap blueMonsterLaugh;
	Bitmap blueMonsterEat;
	Bitmap fblueMonsterEat;
	Bitmap porkImg;
	Bitmap [] foodImgs;
	
	private LinkedList<Point> foodCoordinates;
	private static final int MAX_FOOD_NUM  =10;
	
	private float smallPadding = 10;
	private float padding = 20;
	private float x;
	private float barLabelx;
	private float barLabely;
	private float barMaxLength;
	private float barWidth;
	
	private float screenHeight;
	private float screenWidth;
	
	private long initialTime;
	private long elapsedTime;
	private boolean startTimer;
	private Paint paint;
	private Paint txtPaint;
	private float barDropRate;
	private Monster monster;
	private ImgHashtable imgHashTable = new ImgHashtable();
	
	private boolean eating = false;
	private int initialEatCounter = 20;
	private int eatCounter = initialEatCounter;
	private boolean pat = false;
	private boolean patRelease = false;
	private boolean foodButtonPressed;
	private int foodCount;
	private int initialPatCounter = 30;
	private int patCounter = initialPatCounter;
	
	public CustomView(Context context, Monster monster){
		super(context);
		this.monster = monster;
		blueMonster = BitmapFactory.decodeResource(getResources(),R.drawable.blue_monster);
		blueMonsterShy = BitmapFactory.decodeResource(getResources(),R.drawable.blue_monster_shy);
		blueMonsterSmile = BitmapFactory.decodeResource(getResources(), R.drawable.blue_monster_smile);
		blueMonsterLaugh = BitmapFactory.decodeResource(getResources(), R.drawable.blue_monster_laugh);
		blueMonsterEat = BitmapFactory.decodeResource(getResources(), R.drawable.blue_monster_eat);
		porkImg = BitmapFactory.decodeResource(getResources(), R.drawable.pork);
		foodImgs = new Bitmap[MAX_FOOD_NUM];
		Matrix mirrorMatrix = new Matrix();
		mirrorMatrix.preScale(-1.0f, 1.0f);
		fblueMonster = Bitmap.createBitmap(blueMonster, 0, 0, blueMonster.getWidth(), blueMonster.getHeight(), mirrorMatrix, false);
		fblueMonsterShy = Bitmap.createBitmap(blueMonsterShy, 0, 0, blueMonsterShy.getWidth(), blueMonsterShy.getHeight(), mirrorMatrix, false);
		fblueMonsterEat = Bitmap.createBitmap(blueMonsterEat, 0, 0, blueMonsterEat.getWidth(), blueMonsterEat.getHeight(), mirrorMatrix, false);
		ourHolder= getHolder();
		x =0;
		barLabelx = 0;
		barLabely = padding;
		barWidth = 5;
		barMaxLength = 200;
		foodCount = 0;
		foodButtonPressed = false;
		startTimer = false;
		paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(2);
		txtPaint = new Paint();
		txtPaint.setStyle(Paint.Style.STROKE);
		txtPaint.setStrokeWidth(1);
		txtPaint.setColor(Color.BLACK);
		txtPaint.setTextSize(30);
		barDropRate = 10;
		
		foodCoordinates= new LinkedList<Point>();
		
		imgHashTable.put("hp", BitmapFactory.decodeResource(getResources(),R.drawable.hp));
		imgHashTable.put("happiness", BitmapFactory.decodeResource(getResources(),R.drawable.happiness));
	}
	
	public void pause(){
		isRunning = false;
		while(true){
			try {
				ourThread.join();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
		ourThread = null;
	}
	
	public void resume(){
		isRunning = true;
		ourThread = new Thread(this);
		ourThread.start();
	}
	
	public void run(){
		while(isRunning){ //0.1 second per loop
			if(!ourHolder.getSurface().isValid()){
				continue;
			}
			Bitmap img = blueMonster;
			Canvas canvas = ourHolder.lockCanvas();
			screenHeight = canvas.getHeight();
			screenWidth = canvas.getWidth();
			canvas.drawColor(Color.WHITE);
			

			
			if(pat){
				if(!backwards){
					canvas.drawBitmap(blueMonsterShy, x, canvas.getHeight()-img.getHeight(), null);
				}else{
					canvas.drawBitmap(fblueMonsterShy, x, canvas.getHeight()-img.getHeight(), null);
				}
			}else if(patRelease){
				if(patCounter>0){
					patCounter--;
					if(Math.floor((double)(patCounter/5.0))%2 ==0){
						canvas.drawBitmap(blueMonsterLaugh, x, canvas.getHeight()-blueMonsterLaugh.getHeight(), null);
					} else{
						canvas.drawBitmap(blueMonsterSmile, x, canvas.getHeight()-blueMonsterSmile.getHeight(), null);
					}
				}else{
					patCounter = initialPatCounter;
					patRelease = false;
				}
			}else if(eating){
				if(eatCounter>0){
					eatCounter--;
					if(Math.floor((double)(eatCounter/5.0))%2 ==0){
						canvas.drawBitmap(blueMonster, x, canvas.getHeight()-blueMonster.getHeight(), null);
					} else{
						canvas.drawBitmap(blueMonsterEat, x, canvas.getHeight()-blueMonsterEat.getHeight(), null);
					}
				}else{
					eatCounter = initialEatCounter;
					int healpt = 30;
					if(((Float)monster.get("hp")).floatValue() < ((Float)monster.get("maxhp")).floatValue()-healpt){
						float currentvalue = ((Float)monster.get("hp")).floatValue();
						monster.set("hp", new Float(currentvalue+healpt));
					}
					eating = false;
				}
				foodCount--;
			}else{
				if(x<canvas.getWidth()-blueMonster.getWidth()&& !backwards){
					x=x+10;
				}else{
					backwards = true;
				}
				if(x>0&& backwards){
					img = fblueMonster;
					x=x-10;
				}else{
					backwards = false;
				}
				canvas.drawBitmap(img, x, canvas.getHeight()-img.getHeight(), null);
			}
			
			Log.v("CustomView: ", new Integer(foodCount).toString());
			if(foodCount>0){
				for(int i = 0; i< foodCount; i++){
					Point curPoint = foodCoordinates.get(i);
					int foodCurX = curPoint.x;
					int foodCurY = curPoint.y;
					canvas.drawBitmap(porkImg, foodCurX, foodCurY, null);
					float temp =0;
					if(!backwards){
						temp = Math.abs(x - foodCurX);
					}else{
						temp = Math.abs(foodCurX-x);
					}
					if( temp< 1){
						eating = true;
						foodCoordinates.remove(i);
					}
				}
			}
			
			drawBar(canvas, "hp", monster, barLabelx, barLabely, 120, 150, false, true);
			drawBar(canvas, "happiness", monster, barLabelx, barLabely+30, 120, 210, false, true);
			if(foodButtonPressed){
				if(foodCount< MAX_FOOD_NUM){
					foodCoordinates.add(new Point((int)(Math.random()*screenWidth), (int)screenHeight-porkImg.getHeight()));
					foodCount++;
				}
				foodButtonPressed=false;
			}
			
			
			ourHolder.unlockCanvasAndPost(canvas);
		}
	}
	
	private void drawBar (Canvas canvas, String barType, Monster monster, float barLabelx, float barLabely, float timeElapsedTxtX, float timeElapsedTxtY, boolean timeElapsedShow, boolean fractionShow){
		
		canvas.drawBitmap(imgHashTable.get(barType), barLabelx, barLabely, null);		
		float barLength = 0;
		barLength = (((Float)monster.get(barType)).floatValue() / ((Float)monster.get("max"+barType)).floatValue())*barMaxLength;
		float barRight = barLabelx+imgHashTable.get(barType).getWidth()+smallPadding+barLength;
		float barLeft = barLabelx+imgHashTable.get(barType).getWidth()+smallPadding;
		float barTop = barLabely+imgHashTable.get(barType).getHeight()/2;
		float barBottom = barLabely+imgHashTable.get(barType).getHeight()/2 +barWidth;
		
		canvas.drawRect(barLeft, barTop, barRight, barBottom, paint);
		
		if(((Float)monster.get(barType)).floatValue()>0){
			if(startTimer == false){
				startTimer = true;
				initialTime = System.nanoTime();
			}else if (startTimer == true){
				elapsedTime = System.nanoTime()-initialTime;
			}
			if (elapsedTime%barDropRate ==0){
				float currentvalue = ((Float)monster.get(barType)).floatValue();
				monster.set(barType, new Float(currentvalue-1));
			}
		}
		if(timeElapsedShow)
			canvas.drawText(new Long(elapsedTime/1000000000).toString()+" sec", timeElapsedTxtX, timeElapsedTxtY, txtPaint);
		if(fractionShow)
			canvas.drawText("Health Point: "+ ((Float)monster.get(barType)).toString()+" / " + ((Float)monster.get("max"+barType)).toString(), timeElapsedTxtX, timeElapsedTxtY+30, txtPaint);
	}
	
	public Monster getMonster(){
		return this.monster;
	}
	public void setMonster(Monster monster){
		this.monster = monster;
	}
	
	public float[] monsterCoordinate(){
		float[] cor = new float[4];
		cor[0] = this.x; //left boundary x
		cor[1] = this.x + this.blueMonster.getWidth(); //right bourdary x
		cor[2] = screenHeight- this.blueMonster.getHeight(); //top
		cor[3] = screenHeight; // bottom
		return cor;
	}
	
	public void setpat(boolean pat){
		this.pat = pat;
	}
	public boolean getpat(){
		return pat;
	}
	public void setbackwards(boolean backwards){
		this.backwards = backwards;
	}
	public void setpatRelease(boolean patRelease){
		this.patRelease = patRelease;
	}
	public void setfoodButtonPressed(boolean foodButtonPressed){
		this.foodButtonPressed = foodButtonPressed;
	}
}
