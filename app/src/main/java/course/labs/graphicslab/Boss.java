package course.labs.graphicslab;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Boss extends View {

    private static final String TAG = "Boss";
    private static final int BITMAP_SIZE = 64;
    private static final int LIVE_RATE = 10000;
    private int attackRatee;
    private int attackLiveRate;
    private int attackWaves;
    private int attackEnemy;
    private int enemyPerWave;
    private ScheduledFuture<?> mMoverFuture;
    private ScheduledFuture<?> mAttackExecuter;

    private RelativeLayout mFrame;

    // границы игрового пространствва
    private float leftBorder, rightBorder;


    private ScheduledExecutorService attackerExecutor;
    Boss(Context context, RelativeLayout mFrame, float leftBorder, float rightBorder) {
        super(context);

        this.mFrame = mFrame;
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;

        live();
    }

    private void live() {
        attackWaves=0;
        // Создаем WorkerThread
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
            //mAttackExecuter.cancel(true);
            attackRatee=9000;
            attackEnemy = (int) (9+0.2*9*attackWaves);
            enemyPerWave = attackEnemy/(attackRatee/1000);
                if (enemyPerWave >4) {
                    enemyPerWave = 4;
                    attackLiveRate = (int) 9000/(attackEnemy/enemyPerWave);

                }else{
                    attackLiveRate=1000;
                }
            attackWaves++;
            attack();
                Log.d(TAG,"Ball "+attackLiveRate );
                Log.d(TAG,"Ball ");
                //Log.d(TAG,"bonusball "+BonusBall. );

            }
        }, 1000, LIVE_RATE, TimeUnit.MILLISECONDS);

    }
    private void attack() {
         //final int enemyPerWave = 1;
        final Random rnd = new Random();
        // Создаем WorkerThread
        attackerExecutor = Executors.newScheduledThreadPool(1);
        mAttackExecuter = attackerExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (int i =0; i<enemyPerWave;i++){
                    int x = (int) (rnd.nextInt((int) (rightBorder-leftBorder))+leftBorder);

                    createBossBall(x,0);
                }
                attackRatee-=attackLiveRate;
                if (attackRatee < 100){
                    mAttackExecuter.cancel(true);
                    attackerExecutor.shutdownNow();

                }
            }
        }, 0,attackLiveRate , TimeUnit.MILLISECONDS);

    }
    public void kill(){
        mMoverFuture.cancel(true);
        mAttackExecuter.cancel(true);
        mFrame.post(new Runnable() {
            @Override
            public void run() {
                mFrame.removeView(Boss.this);
            }
        });


    }
    private void createBossBall(final float x, final float y){
        mFrame.post(new Runnable() {
            @Override
            public void run() {
                mFrame.addView(new BossBall(mFrame.getContext(), x, y,mFrame));
            }
        });

    }
    public void pause()  {
        attackerExecutor.shutdownNow();
        mMoverFuture.cancel(true);
    };
    public void resume()  {
        live();
    };
}
