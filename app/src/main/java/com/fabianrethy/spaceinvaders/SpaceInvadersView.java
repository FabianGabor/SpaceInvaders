package com.fabianrethy.spaceinvaders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpaceInvadersView extends SurfaceView implements Runnable {
	private final Context context;
	private Thread gameThread = null;
	private final SurfaceHolder surfaceHolder;
	private volatile boolean playing;
	private boolean paused = true;
	private final Paint paint;
	private long fps;
	private static int screenX;
	private static int screenY;
	private PlayerShip playerShip;
	private Bullet bullet;
	private final Bullet[] invadersBullets = new Bullet[200];
	private int nextBullet;
	private final Invader[] invaders = new Invader[60];
	private int numInvaders = 0;
	private final DefenceBrick[] bricks = new DefenceBrick[400];
	private int numBricks;
	private int score = 0;
	private int lives = 3;

	private long invaderFaceInterval = 1000;
	private boolean invaderFace;
	private long lastInvaderFaceTime = System.currentTimeMillis();

	public SpaceInvadersView(Context context, int screenX, int screenY) {
		super(context);
		this.context = context;
		surfaceHolder = getHolder();
		paint = new Paint();
		SpaceInvadersView.screenX = screenX;
		SpaceInvadersView.screenY = screenY;
		prepareLevel();
	}

	private void prepareLevel() {
		playerShip = new PlayerShip(context, screenX, screenY);
		bullet = new Bullet(screenY);

		for (int i = 0; i < invadersBullets.length; i++) {
			invadersBullets[i] = new Bullet(screenY);
		}

		numInvaders = 0;
		for (int column = 0; column < 6; column++) {
			for (int row = 0; row < 5; row++) {
				invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
				numInvaders++;
			}
		}

		numBricks = 0;
		for (int shelterNumber = 0; shelterNumber < 4; shelterNumber++) {
			for (int column = 0; column < 10; column++) {
				for (int row = 0; row < 5; row++) {
					bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
					numBricks++;
				}
			}
		}

		invaderFaceInterval = 1000;
	}

	public static int getScreenX() {
		return screenX;
	}

	@Override
	public void run() {
		while (playing) {
			long startFrameTime = System.currentTimeMillis();

			if (!paused) {
				update();
			}
			draw();

			long timeThisFrame = System.currentTimeMillis() - startFrameTime;
			if (timeThisFrame >= 1) {
				fps = 1000 / timeThisFrame;
			}

			if (!paused) {
				if ((startFrameTime - lastInvaderFaceTime) > invaderFaceInterval) {
					lastInvaderFaceTime = System.currentTimeMillis();
					invaderFace = !invaderFace;
				}
			}
		}
	}

	private void update() {
		boolean bumped = false;
		boolean lost = false;
		playerShip.update(fps);

		if (bullet.getStatus()) {
			bullet.update(fps);
		}

		for (Bullet invadersBullet : invadersBullets) {
			if (invadersBullet.getStatus()) {
				invadersBullet.update(fps);
			}
		}

		for (int i = 0; i < numInvaders; i++) {
			if (invaders[i].getVisibility()) {
				invaders[i].update(fps);

				if (invaders[i].takeAim(playerShip.getX(), playerShip.getLength())) {
					if (invadersBullets[nextBullet].shoot(invaders[i].getX() + invaders[i].getLength() / 2, invaders[i].getY(), bullet.DOWN)) {
						nextBullet++;
						int maxInvaderBullets = 10;
						if (nextBullet == maxInvaderBullets) {
							nextBullet = 0;
						}
					}
				}

				if (invaders[i].getX() > screenX - invaders[i].getLength() || invaders[i].getX() < 0) {
					bumped = true;
				}
			}
		}

		if (bumped) {
			for (int i = 0; i < numInvaders; i++) {
				invaders[i].dropDownAndReverse();
				if (invaders[i].getY() > screenY - screenY / 10.0) {
					lost = true;
				}
			}
			invaderFaceInterval = invaderFaceInterval - 80;
		}

		if (lost) {
			prepareLevel();
		}

		if (bullet.getImpactPointY() < 0) {
			bullet.setInactive();
		}

		for (Bullet value : invadersBullets) {
			if (value.getImpactPointY() > screenY) {
				value.setInactive();
			}
		}

		if (bullet.getStatus()) {
			for (int i = 0; i < numInvaders; i++) {
				if (invaders[i].getVisibility()) {
					if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
						invaders[i].setInvisible();
						bullet.setInactive();
						score = score + 10;

						if (score == numInvaders * 10) {
							paused = true;
							score = 0;
							lives = 3;
							prepareLevel();
						}
					}
				}
			}
		}

		for (Bullet invadersBullet : invadersBullets) {
			checkBulletCollisionWithBrick(invadersBullet);
		}
		checkBulletCollisionWithBrick(bullet);

		for (Bullet invadersBullet : invadersBullets) {
			checkInvaderBulletCollisionWithPlayer(invadersBullet);
		}
	}

	private void checkInvaderBulletCollisionWithPlayer(Bullet invadersBullet) {
		if (invadersBullet.getStatus()) {
			if (RectF.intersects(playerShip.getRect(), invadersBullet.getRect())) {
				invadersBullet.setInactive();
				lives--;

				if (lives == 0) {
					paused = true;
					lives = 3;
					score = 0;
					prepareLevel();
				}
			}
		}
	}

	private void checkBulletCollisionWithBrick(Bullet bullet) {
		if (bullet.getStatus()) {
			for (int i = 0; i < numBricks; i++) {
				if (bricks[i].getVisibility()) {
					if (RectF.intersects(bullet.getRect(), bricks[i].getRect())) {
						bullet.setInactive();
						bricks[i].setInvisible();
					}
				}
			}
		}
	}

	private void draw() {
		if (surfaceHolder.getSurface().isValid()) {
			Canvas canvas = surfaceHolder.lockCanvas();
			canvas.drawColor(Color.argb(255, 0, 0, 0));
			paint.setColor(Color.argb(255, 255, 255, 255));
			canvas.drawBitmap(playerShip.getBitmap1(), playerShip.getX(), screenY - 50, paint);

			for (int i = 0; i < numInvaders; i++) {
				if (invaders[i].getVisibility()) {
					if (invaderFace) {
						canvas.drawBitmap(invaders[i].getBitmap1(), invaders[i].getX(), invaders[i].getY(), paint);
					} else {
						canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
					}
				}
			}

			for (int i = 0; i < numBricks; i++) {
				if (bricks[i].getVisibility()) {
					canvas.drawRect(bricks[i].getRect(), paint);
				}
			}

			if (bullet.getStatus()) {
				canvas.drawRect(bullet.getRect(), paint);
			}

			for (Bullet invadersBullet : invadersBullets) {
				if (invadersBullet.getStatus()) {
					canvas.drawRect(invadersBullet.getRect(), paint);
				}
			}

			paint.setColor(Color.argb(255, 249, 129, 0));
			paint.setTextSize(40);
			canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);

			surfaceHolder.unlockCanvasAndPost(canvas);
		}
	}

	public void pause() {
		playing = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			Log.e("Error:", "joining thread");
		}

	}

	public void resume() {
		playing = true;
		gameThread = new Thread(this);
		gameThread.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				paused = false;
				if (motionEvent.getY() > screenY - screenY / 8.0) {
					if (motionEvent.getX() > screenX / 2.0) {
						playerShip.setMovementState(playerShip.RIGHT);
					} else {
						playerShip.setMovementState(playerShip.LEFT);
					}
				}

				if (motionEvent.getY() < screenY - screenY / 8.0) {
					bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, bullet.UP);
				}
				break;

			case MotionEvent.ACTION_UP:
				if (motionEvent.getY() > screenY - screenY / 10.0) {
					playerShip.setMovementState(playerShip.STOPPED);
				}
				break;
		}
		return true;
	}
}
