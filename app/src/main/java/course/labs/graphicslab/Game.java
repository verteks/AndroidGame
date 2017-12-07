package course.labs.graphicslab;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Game extends Activity {

	// Эти переменные нужны для тестирования, не изменять
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static int incrementa=0;

	private static final String TAG = "Game";
	public static Bitmap mBitmapFireBall;
	public static Bitmap mBitmapSnowFlake;
	public static Bitmap mBitmapDrop;

	// Главный view
	private RelativeLayout mFrame;
	public static TextView textViewBonus;

	// Bitmap изображения пузыря
	private Bitmap mBitmap;

	// размеры экрана
	private int mDisplayWidth, mDisplayHeight;

	// границы игрового пространствва
	private float leftBordder, rightBorder;
	// AudioManager
	private AudioManager mAudioManager;
	// SoundPool
	private SoundPool mSoundPool;
	// ID звука лопания пузыря
	private int mSoundID;
	// Громкость аудио
	private float mStreamVolume;

	private boolean gameCreated = false;
	private boolean wasPaussed = false;

	// Детектор жестов
	private GestureDetector mGestureDetector;
	private Player player;
	private Boss boss;

	public static Bitmap getmBitmapFireBall() {
		return mBitmapFireBall;
	}

	public static Bitmap getmBitmapSnowFlake() {
		return mBitmapSnowFlake;
	}

	public static Bitmap getmBitmapDrop() {
		return mBitmapDrop;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Установка пользовательского интерфейса
		mFrame = (RelativeLayout) findViewById(R.id.frame);
		textViewBonus =(TextView) findViewById(R.id.textViewBonus);


		// Загружаем базовое изображение для пузыря
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.persik1);
		mBitmapFireBall = BitmapFactory.decodeResource(getResources(), R.drawable.fireball64);
		mBitmapSnowFlake = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake);
		mBitmapDrop = BitmapFactory.decodeResource(getResources(), R.drawable.drop);
	}

	private void createNewGame(){
		if (player!=null){player.kill();
			Log.d(TAG,"player dead");}
		if (boss!=null){boss.kill();
		Log.d(TAG,"boss dead");}
		mFrame.removeAllViews();
		player = new Player(mFrame.getContext(),mDisplayWidth/2, (float) (0.95)*mDisplayHeight,mFrame,mBitmap,leftBordder,rightBorder);
		player.setGoTO(mDisplayWidth/2);
		mFrame.addView(player);
		boss = new Boss(mFrame.getContext(),mFrame,leftBordder,rightBorder);
		mFrame.addView(boss);
		gameCreated = true;
	}


	@Override
	protected void onResume() {
		Log.d(TAG,"was resumed");

		if ((gameCreated)&&(wasPaussed)) {
			for (int i = 0; i < mFrame.getChildCount(); i++) {
				if (mFrame.getChildAt(i) instanceof Ball) {
					((Ball) mFrame.getChildAt(i)).resume();
				}
				if (mFrame.getChildAt(i) instanceof BossBall) {
					((BossBall) mFrame.getChildAt(i)).resume();
				}
				if (mFrame.getChildAt(i) instanceof BonusBall) {
					((BonusBall) mFrame.getChildAt(i)).resume();
				}
				if (mFrame.getChildAt(i) instanceof Player) {
					((Player) mFrame.getChildAt(i)).resume();
				}
				if (mFrame.getChildAt(i) instanceof Boss) {
					((Boss) mFrame.getChildAt(i)).resume();
				}

			}
		}
		wasPaussed=false;
		super.onResume();
		setupGestureDetector();
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {

			// Получаем размер экрана для того чтобы View знал, где границы отображения
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();

			leftBordder = (float) (0.05*mDisplayWidth);
			rightBorder= mDisplayWidth-(float) (0.05*mDisplayWidth);
			if (!gameCreated){
				createNewGame();

			}else {
				onResume();
			}

}
	}

	// Устанавливаем GestureDetector
	private void setupGestureDetector() {

		mGestureDetector = new GestureDetector(this,

				new GestureDetector.SimpleOnGestureListener() {

					// Если на BubleView происходит жест швыряния, тогда изменяем его направление и скорость (velocity)
					public boolean onDown(MotionEvent event) {
						Player player = (Player) mFrame.getChildAt(0);
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
		Log.d(TAG,"was paused");
		wasPaussed=true;

		for (int i =0;i<mFrame.getChildCount();i++){
			if (mFrame.getChildAt(i) instanceof Ball){((Ball) mFrame.getChildAt(i)).pause();}
			if (mFrame.getChildAt(i) instanceof BossBall){((BossBall) mFrame.getChildAt(i)).pause();}
			if (mFrame.getChildAt(i) instanceof BonusBall){((BonusBall) mFrame.getChildAt(i)).pause();}
			if (mFrame.getChildAt(i) instanceof Player){((Player) mFrame.getChildAt(i)).pause();}
			if (mFrame.getChildAt(i) instanceof Boss){((Boss) mFrame.getChildAt(i)).pause();}

		}
		super.onPause();
	}








	// Не изменяйте следующий код

	@Override
	public void onBackPressed() {
		onPause();
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
				gameCreated=false;
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

	public static void incrementa(boolean add){
		if (add){
		incrementa++;}
		else{incrementa--;}
		Log.d("incrementa",""+incrementa);

	}




}



