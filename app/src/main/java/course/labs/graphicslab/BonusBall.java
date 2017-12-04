package course.labs.graphicslab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by vshir on 04.12.2017.
 */

public class BonusBall extends View {

    private static final String TAG = "BonusBall";
    private static final int SIZE = 70;
    private static final int REFRESH_RATE = 40;
    private final Paint mPainter = new Paint();
    private ScheduledFuture<?> mMoverFuture;
    private int mDy = 36;
    private Bitmap mScaledBitmap;

    private RelativeLayout mFrame;
    private Random rnd ;
    // местоположение, скорость и направление пузыря
    private float mXPos, mYPos, mWidth;

    BonusBall(Context context, float x, float y,RelativeLayout mFrame) {
        super(context);

        this.mFrame=mFrame;
        // Радиус Bitmap
        mWidth = SIZE / 2;
        // Центрируем положение игрока относительно точки касания пальца пользователя
        mXPos = x ;
        mYPos = y ;

//        rnd = new Random();
//        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        mPainter.setColor(Color.GREEN);

        this.start();
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

                for (int i =0;i<mFrame.getChildCount();i++){
                    if (mFrame.getChildAt(i) instanceof Player){
                        Player player = (Player) mFrame.getChildAt(i);
                        if (player.intersect(mXPos,mYPos,mWidth)){
                            stop();
                        }
                    }

                }

                if (mYPos<mFrame.getHeight()) {
                    mYPos+=mDy;
                }else {
                    stop();
                }




                postInvalidate();


            }
        }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
    }

    private void stop() {

        if (null != mMoverFuture && !mMoverFuture.isDone()) {
            mMoverFuture.cancel(true);
        }
        // Данный код будет выполнен в UI потоке
        mFrame.post(new Runnable() {
            @Override
            public void run() {
                mFrame.removeView(BonusBall.this);
            }
        });
    }
//    private void boom()
//    {
//        List<Ball> list = Ball.getAll();
//        for (Ball a:list) {
//        if (a.intersect(mXPos,mYPos,mWidth)){
//            stop();
//            delete();
//        }
//
//        }
//    }

}
