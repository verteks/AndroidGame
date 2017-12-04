package course.labs.graphicslab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by vshir on 04.12.2017.
 */

public class Ball extends View {

    private static final String TAG = "Ball";
    private static final int SIZE = 32;
    private static final int REFRESH_RATE = 40;
    private final Paint mPainter = new Paint();
    private ScheduledFuture<?> mMoverFuture;
    private int mDy = 20;
    private int mDx = 0;
    private int mDwidth = 0;
    private Bitmap mScaledBitmap;
    private boolean terminatorMod = false;

    private RelativeLayout mFrame;
 private Random rnd ;
    // местоположение, скорость и направление пузыря
    private float mXPos, mYPos, mWidth;

    Ball(Context context, float x, float y, RelativeLayout mFrame, boolean boost1T, boolean boost2T, boolean boost3T) {
        super(context);

        this.mFrame=mFrame;
        // Радиус Bitmap
        mWidth = SIZE / 2;
        // Центрируем положение игрока относительно точки касания пальца пользователя
        mXPos = x ;
        mYPos = y ;

//        rnd = new Random();
//        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        mPainter.setColor(Color.RED);
        if (boost1T){
            mDwidth = 1;
        }
        if (boost2T) {
            terminatorMod=true;
        }
        if (boost3T ) {
            createRBall(boost1T,boost2T);
        }


        this.start();
    }

    private void createRBall(final boolean boost1T, final boolean boost2T){
        mFrame.post(new Runnable() {
            @Override
            public void run() {
                Ball ball1 = new Ball(mFrame.getContext(), mXPos+mWidth, mYPos,mFrame,boost1T,boost2T,false);
                ball1.setDx(10);
                Ball ball2 = new Ball(mFrame.getContext(), mXPos+mWidth, mYPos,mFrame,boost1T,boost2T,false);
                ball2.setDx(-10);
                mFrame.addView(ball1);
                mFrame.addView(ball2);

            }
        });

    }
    private void setDx(int dx){
        this.mDx=dx;
    }
    public static List<Ball> getAll(){
        return getAll();
    }
    private void delete(){
        this.destroyDrawingCache();
    }
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawCircle(mXPos,mYPos,mWidth,mPainter);

        canvas.restore();
    }

    private void start() {

        // Создаем WorkerThread
        ScheduledExecutorService executor = Executors
                .newScheduledThreadPool(1);

        // Запускаем run() в Worker Thread каждые REFRESH_RATE милисекунд
        // Сохраняем ссылку на данный процесс в mMoverFuture
        mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                if (mYPos>0) {
                    mYPos-=mDy;
                    mWidth+=mDwidth;
                    doDx();
                }else {
                    stop();
                }

                postInvalidate();

            }
        }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
    }
private void doDx(){
    if (mXPos-mWidth<0){mDx*=-1;}
    if (mXPos+mWidth>mFrame.getWidth()){mDx*=-1;}
    mXPos+=mDx;
}
    private void stop() {

        if (null != mMoverFuture && !mMoverFuture.isDone()) {
            mMoverFuture.cancel(true);
        }
        // Данный код будет выполнен в UI потоке
        mFrame.post(new Runnable() {
            @Override
            public void run() {
                mFrame.removeView(Ball.this);
                delete();
            }
        });
    }
    public boolean intersect(float x,float y, float radius){
        float a = Math.abs(x-mXPos);
        float b = Math.abs(y-mYPos);
        a= (float) Math.pow(a,2);
        b= (float) Math.pow(b,2);
        float c = (float) Math.pow(a+b,0.5);
        if (c<radius+mWidth){
            if (terminatorMod){return true;}else{
            stop();
            return true;
        }}else{
                return false;}
    }


}
