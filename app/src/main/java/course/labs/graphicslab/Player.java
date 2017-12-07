package course.labs.graphicslab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by vshir on 04.12.2017.
 */
public class Player extends View {

    private static final String TAG = "Player";
    private static final int BITMAP_SIZE = 128;
    private static final int REFRESH_RATE = 40;
    private final Paint mPainter = new Paint();
    private ScheduledFuture<?> mMoverFuture;
    private ScheduledFuture<?> mFireFuture;
    private int mScaledBitmapWidth;
    private int mDx = 30;
    private int fireRate = 500;
    private Bitmap mScaledBitmap;
    // Bitmap изображения пузыря
    private Bitmap mBitmap;
    private int boost1,boost2,boost3 =0;
    private  boolean boost1T,boost2T,boost3T = false;

    private RelativeLayout mFrame;

    private ScheduledExecutorService executor1;

    // границы игрового пространствва
    private float leftBorder, rightBorder;

    // местоположение, скорость и направление пузыря
    private float mXPos, mYPos, mDy, mWidth,goTO, mRadiusSquared;
    private long mRotate, mDRotate;
    private int lul = 5;
    private Random rnd;

    Player(Context context, float x, float y, RelativeLayout mFrame,Bitmap mBitmap, float leftBorder, float rightBorder) {
        super(context);

        this.mFrame=mFrame;
        this.mBitmap=mBitmap;
        this.leftBorder=leftBorder;
        this.rightBorder=rightBorder;
        mScaledBitmapWidth=BITMAP_SIZE;
        mWidth = mScaledBitmapWidth / 2;
        mScaledBitmap = Bitmap.createScaledBitmap(mBitmap,mScaledBitmapWidth,mScaledBitmapWidth,true);
        mXPos = x - mWidth;
        mYPos = y - mWidth;



        rnd = new Random();
        mPainter.setAntiAlias(true);
        start();
        fire();
    }

    private void setPosition(float x) {
        mXPos = x - mWidth;
    }
    private float getPosition() {
        return mXPos + mWidth;
    }
    public void setGoTO(float x){
        goTO=x-mWidth;
    }




    // Начинаем перемещать BubbleView & обновлять экран
    private void start() {

        // Создаем WorkerThread
        final ScheduledExecutorService executor = Executors
                .newScheduledThreadPool(1);

        // Запускаем run() в Worker Thread каждые REFRESH_RATE милисекунд
        // Сохраняем ссылку на данный процесс в mMoverFuture
        mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                if (Player.this.mXPos != Player.this.goTO) {
                    moveToGoTO();
                }
                Player.this.postInvalidate();



            }
        }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);

    }
    // стреляем
    private void fire() {
        //final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor1 = Executors.newScheduledThreadPool(1);

        final ExecutorService service = Executors.newCachedThreadPool();
        lul = 5;

        mFireFuture = executor1.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                if (boost1>0){boost1--;boost1T=true;}
                if (boost2>0){boost2--;boost2T=true;}
                if (boost3>0){boost3--;boost3T=true;}


                createBall(boost1T,boost2T,boost3T);

                boost1T=boost2T=boost3T=false;


            }
        }, 0, fireRate, TimeUnit.MILLISECONDS);
    }
    private void createBall(final boolean boost1T, final boolean boost2T, final boolean boost3T){
        mFrame.post(new Runnable() {
            @Override
            public void run() {
                mFrame.addView(new Ball(mFrame.getContext(), mXPos+mWidth, mYPos,mFrame,boost1T,boost2T,boost3T));
            }
        });

    }

   public void kill(){
       mMoverFuture.cancel(true);
       mFireFuture.cancel(true);
       executor1.shutdownNow();
       mFrame.post(new Runnable() {
           @Override
           public void run() {
               mFrame.removeView(Player.this);
           }
       });


    }

    // Рисуем Пузырь в его текущем положении
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();

        canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);

        canvas.restore();
    }

    // Возвращает true если BubbleView все еще на экране после хода
    // operation
    private synchronized boolean moveToGoTO() {


        if ((mXPos>goTO) & (mXPos>leftBorder)) {
            mXPos -= mDx;
        }
        if ((mXPos<goTO) && (mXPos+mScaledBitmapWidth<rightBorder)) {
            mXPos += mDx;
        }


        return true;
    }



    public boolean intersect(float x, float y, float radius, boolean bonus){
        double a = Math.abs(x-mXPos);
        double b = Math.abs(y-mYPos);
        a=  Math.pow(a,2);
        b=  Math.pow(b,2);
        double c = Math.pow(a+b,0.5);
        double cc =radius+mWidth;
        if (c<cc){
            if (bonus){
           int  random = rnd.nextInt(3)+1;
            if (random==1){
                boost1+=10;
                setTextBonus("ylalal");
            }
            if (random==2){
                setTextBonus("ylalal");
                boost2+=10;
            }
            if (random==3){
                setTextBonus("ylalal");
                boost3+=10;
            }
            }
            return true;
        }else{
            return false;}
    }

    public void pause()  {
        mMoverFuture.cancel(true);
        mFireFuture.cancel(true);
        executor1.shutdownNow();
    };
    public void resume()  {
        start();
        fire();
    };

    private void setTextBonus(String text){
        final TextView textView = Game.textViewBonus;
        textView.setText(text);
        textView.setCursorVisible(true);
        mFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setCursorVisible(false);
            }
        },2000);

    }
}