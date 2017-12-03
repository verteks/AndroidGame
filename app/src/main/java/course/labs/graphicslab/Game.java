package course.labs.graphicslab;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Game extends Activity {

	// Эти переменные нужны для тестирования, не изменять
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static final String TAG = "Lab-Graphics";

	// Главный view
	private RelativeLayout mFrame;

	// Bitmap изображения пузыря
	private Bitmap mBitmap;

	// размеры экрана
	private int mDisplayWidth, mDisplayHeight;

	// Звуковые переменные

	// AudioManager
	private AudioManager mAudioManager;
	// SoundPool
	private SoundPool mSoundPool;
	// ID звука лопания пузыря
	private int mSoundID;
	// Громкость аудио
	private float mStreamVolume;

	// Детектор жестов
	private GestureDetector mGestureDetector;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Установка пользовательского интерфейса
		mFrame = (RelativeLayout) findViewById(R.id.frame);

		// Загружаем базовое изображение для пузыря
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);

	}

	@Override
	protected void onResume() {
		super.onResume();
		setupGestureDetector();
		Log.d("Sound Load:","sound loaded successfully");

	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {

			// Получаем размер экрана для того чтобы View знал, где границы отображения
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();

			Player player = new Player(mFrame.getContext(),mDisplayWidth/2, (float) (0.95)*mDisplayHeight);
			player.setGoTO(mDisplayWidth/2);
			mFrame.addView(player);
			player.start();



}
	}

	// Устанавливаем GestureDetector
	private void setupGestureDetector() {

		mGestureDetector = new GestureDetector(this,

				new GestureDetector.SimpleOnGestureListener() {

					// Если на BubleView происходит жест швыряния, тогда изменяем его направление и скорость (velocity)
					public boolean onDown(MotionEvent event) {
						Player player = (Player) mFrame.getChildAt(0);
						if(player.getPosition()>event.getRawX()){

							Log.d("Player","Go right");
						}else{
							Log.d("Player","Go left");
						}
						player.setGoTO(event.getRawX());

						return true;
					}
				});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// DONE - Делегируем нажатие детектору жестов gestureDetector
		mGestureDetector.onTouchEvent(event);
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


	public class Player extends View {

		private static final int BITMAP_SIZE = 64;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private ScheduledFuture<?> mFireFuture;
		private int mScaledBitmapWidth;
		private int mDx = 25;
		private int fireRate = 500;
		private Bitmap mScaledBitmap;

		// местоположение, скорость и направление пузыря
		private float mXPos, mYPos, mDy, mWidth,goTO, mRadiusSquared;
		private long mRotate, mDRotate;

		private Timer timer;
		private TimerTask mTimerTask;

		Player(Context context, float x, float y) {
			super(context);

			mScaledBitmapWidth=BITMAP_SIZE;
			mWidth = mScaledBitmapWidth / 2;
			mScaledBitmap = Bitmap.createScaledBitmap(mBitmap,mScaledBitmapWidth,mScaledBitmapWidth,true);
			mXPos = x - mWidth;
			mYPos = y - mWidth;



			mPainter.setAntiAlias(true);
			mTimerTask = new MyTimerTask();
			timer = new Timer();

		}
		private void firefire(){
			timer.schedule(mTimerTask,1000);
		}

		private void setPosition(float x) {
			mXPos = x - mWidth;
		}
		private float getPosition() {
			return mXPos + mWidth;
		}
		private void setGoTO(float x){
			goTO=x-mWidth;
		}




		// Начинаем перемещать BubbleView & обновлять экран
		private void start() {

			// Создаем WorkerThread
			ScheduledExecutorService executor = Executors
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
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

			ExecutorService service = Executors.newCachedThreadPool();

			mFireFuture = executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {

					createBall();
					Log.d("Player","Ball fire");

				}
			}, 0, 1000, TimeUnit.MILLISECONDS);
		}
		private void createBall(){
			mFrame.addView(new Ball(mFrame.getContext(),mXPos,mYPos));
		}


		private void stop(final boolean wasPopped) {

			if (null != mMoverFuture && !mMoverFuture.isDone()) {
				mMoverFuture.cancel(true);
			}

			// Данный код будет выполнен в UI потоке
			mFrame.post(new Runnable() {
				@Override
				public void run() {

					mFrame.removeView(Player.this);


					// играем звук лопания
					if (wasPopped) {
						mSoundPool.play(mSoundID, 0.5f, 0.5f, 0, 0, 1.0f);



					}
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

			if ((mXPos>goTO) & (mXPos>0)) {
				mXPos -= mDx;
			}
			if ((mXPos<goTO) && (mXPos+mScaledBitmapWidth<mDisplayWidth)) {
				mXPos += mDx;
			}


			return true;
		}

	}



	public class Ball extends View {

		private static final int SIZE = 32;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private int mDy = 40;
		private Bitmap mScaledBitmap;

		// местоположение, скорость и направление пузыря
		private float mXPos, mYPos, mWidth;

		Ball(Context context, float x, float y) {
			super(context);

			// Радиус Bitmap
			mWidth = SIZE / 2;
			// Центрируем положение игрока относительно точки касания пальца пользователя
			mXPos = x - mWidth;
			mYPos = y - mWidth;

			mPainter.setAntiAlias(true);
			this.start();
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
					mFrame.removeView(Ball.this);
				}
			});
		}


	}



	// Не изменяйте следующий код

	@Override
	public void onBackPressed() {
		openOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_still_mode:
				speedMode = STILL;
				return true;
			case R.id.menu_single_speed:
				speedMode = SINGLE;
				return true;
			case R.id.menu_random_mode:
				speedMode = RANDOM;
				return true;
			case R.id.quit:
				exitRequested();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void exitRequested() {
		super.onBackPressed();
	}


	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					mFrame.addView(new Ball(mFrame.getContext(),mDisplayWidth/2,mDisplayHeight/2));

					Log.d("Player","Ball fire");
				}});
		}
	}
}



