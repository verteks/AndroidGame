package course.labs.graphicslab;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class Game extends Activity {

	// Эти переменные нужны для тестирования, не изменять
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static final String TAG = "Game";

	// Главный view
	private RelativeLayout mFrame;

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

	// Детектор жестов
	private GestureDetector mGestureDetector;
	private Player player;
	private Boss boss;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Установка пользовательского интерфейса
		mFrame = (RelativeLayout) findViewById(R.id.frame);

		// Загружаем базовое изображение для пузыря
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);



	}

	private void createNewGame(){
		if (player!=null){player.kill();}
		if (boss!=null){boss.kill();}
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
		super.onPause();
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
				createNewGame();
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




}



