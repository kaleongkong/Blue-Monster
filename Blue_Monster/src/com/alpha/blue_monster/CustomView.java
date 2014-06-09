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
import android.graphics.Paint.Style;
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
	private int initialEatCounter = 100;
	private int eatCounter = initialEatCounter;
	private boolean pat = false;
	private boolean patRelease = false;
	private boolean foodButtonPressed;
	private int foodCount;
	private int initialPatCounter = 150;
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
		barLabelx = 10;
		barLabely = padding;
		barWidth = 5;
		barMaxLength = 200;
		foodCount = 0;
		foodButtonPressed = false;
		startTimer = false;
		paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(2);
		txtPaint = new Paint();
		txtPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		txtPaint.setStrokeWidth(1);
		txtPaint.setColor(Color.BLACK);
		txtPaint.setTextSize(40);
		barDropRate = 300;
		
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
			barMaxLength = (int)(screenWidth*0.45);
			Color c = new Color();
			canvas.drawColor(c.rgb(0xB2, 0xff, 0xff));
			

			
			if(pat){
				if(!backwards){
					canvas.drawBitmap(blueMonsterShy, x, canvas.getHeight()-img.getHeight(), null);
				}else{
					canvas.drawBitmap(fblueMonsterShy, x, canvas.getHeight()-img.getHeight(), null);
				}
			}else if(patRelease){
				if(patCounter>0){
					patCounter--;
					if(Math.floor((double)(patCounter/25.0))%2 ==0){
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
					if(!backwards){
						if(Math.floor((double)(eatCounter/25.0))%2 ==0){
							canvas.drawBitmap(blueMonster, x, canvas.getHeight()-blueMonster.getHeight(), null);
						} else{
							canvas.drawBitmap(blueMonsterEat, x, canvas.getHeight()-blueMonsterEat.getHeight(), null);
						}
					}else{
						if(Math.floor((double)(eatCounter/25.0))%2 ==0){
							canvas.drawBitmap(fblueMonster, x, canvas.getHeight()-fblueMonster.getHeight(), null);
						} else{
							canvas.drawBitmap(fblueMonsterEat, x, canvas.getHeight()-fblueMonsterEat.getHeight(), null);
						}
					}
				}else{
					eatCounter = initialEatCounter;
					int healpt = 30;
					if(((Float)monster.get("hp")).floatValue() < ((Float)monster.get("maxhp")).floatValue()-healpt){
						float currentvalue = ((Float)monster.get("hp")).floatValue();
						monster.set("hp", new Float(currentvalue+healpt));
					}else{
						monster.set("hp", ((Float)monster.get("maxhp")).floatValue());
					}
					eating = false;
				}
			}else{
				if(x<canvas.getWidth()-blueMonster.getWidth()&& !backwards){
					x=x+2;
				}else{
					backwards = true;
				}
				if(x>0&& backwards){
					img = fblueMonster;
					x=x-2;
				}else{
					backwards = false;
				}
				canvas.drawBitmap(img, x, canvas.getHeight()-img.getHeight(), null);
			}
			
			//Log.v("CustomView: ", "foodCount: "+new Integer(foodCount).toString());
			//Log.v("CustomView: ", "eatCounter: "+new Integer(eatCounter).toString());
			//Log.v("CustomView: ", "List Length: "+new Integer(foodCoordinates.size()).toString());
			Point curPoint;
			int foodCurX = 0;
			int foodCurY = 0;
			for(int i = 0; i< foodCoordinates.size(); i++){
				curPoint = foodCoordinates.get(i);
				foodCurX = curPoint.x;
				foodCurY = curPoint.y;
				canvas.drawBitmap(porkImg, foodCurX, foodCurY, null);
			}
			if(foodCount>0 && eatCounter == initialEatCounter){
				for(int i = 0; i< foodCount; i++){
					curPoint = foodCoordinates.get(i);
					foodCurX = curPoint.x;
					foodCurY = curPoint.y;
					float temp =0;
					if(!backwards){
						temp = Math.abs(x - foodCurX + blueMonster.getWidth());
					}else{
						temp = Math.abs(foodCurX-x);
					}
					if( temp< 50){
						eating = true;
						foodCoordinates.remove(i);
						foodCount--;
						i--;
					}
					//Log.v("customview:", new Float(Math.abs(x - foodCurX)).toString());
				}
			}
			
			drawBar(canvas, "Health Point: ", "hp", monster, barLabelx, barLabely, 30, 150, false, true);
			drawBar(canvas, "Happiness: ", "happiness", monster, barLabelx, barLabely+30, 30, 210, false, true);
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
	
	private void drawBar (Canvas canvas, String str, String barType, Monster monster, float barLabelx, float barLabely, float timeElapsedTxtX, float timeElapsedTxtY, boolean timeElapsedShow, boolean fractionShow){
		
		Paint text = new Paint();
		text.setColor(Color.BLACK);
		text.setStyle(Style.FILL_AND_STROKE);
		text.setTextSize(30);
		
		float barLength = 0;
		barLength = (((Float)monster.get(barType)).floatValue() / ((Float)monster.get("max"+barType)).floatValue())*barMaxLength;
		float barRight = barLabelx+imgHashTable.get(barType).getWidth()+smallPadding+barLength;
		float barLeft = barLabelx+imgHashTable.get(barType).getWidth()+smallPadding;
		float barTop = barLabely+imgHashTable.get(barType).getHeight()/2;
		float barBottom = barLabely+imgHashTable.get(barType).getHeight()/2 +barWidth;
		
		if (barType =="hp"){
			canvas.drawText("HP : ", barLabelx, barBottom, text);
		} else if(barType == "happiness"){
			canvas.drawText("HAPPINESS : ", barLabelx, barBottom, text);
		}
		
		canvas.drawRect(barLeft, barTop-10, barRight, barBottom, paint);
		
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
		int val1 = 0;
		int val2 = 0;
		if(fractionShow)
			val1 = ((Float)monster.get(barType)).intValue();
			val2 = ((Float)monster.get("max"+barType)).intValue();
			canvas.drawText(str+ new Integer(val1).toString()+" / " + new Integer(val2).toString(), timeElapsedTxtX, timeElapsedTxtY+30, txtPaint);
			//canvas.drawText(str+ ((Float)monster.get(barType)).intValue()+" / " + ((Float)monster.get("max"+barType)).toString(), timeElapsedTxtX, timeElapsedTxtY+30, txtPaint);
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
