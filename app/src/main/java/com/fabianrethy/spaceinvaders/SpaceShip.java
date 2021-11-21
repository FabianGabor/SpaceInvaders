package com.fabianrethy.spaceinvaders;

import android.graphics.Bitmap;
import android.graphics.RectF;

public abstract class SpaceShip {
	RectF rect;
	Bitmap bitmap1;
	Bitmap bitmap2;

	float length;
	float height;
	float x;
	float y;
	float shipSpeed;

	public final int LEFT = 1;
	public final int RIGHT = 2;
	int shipMoving;
	final int STOPPED = 0;

	public RectF getRect() {
		return rect;
	}

	public Bitmap getBitmap1() {
		return bitmap1;
	}

	public Bitmap getBitmap2() {
		return bitmap2;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getLength() {
		return length;
	}

	public void update(long fps) {
		if (shipMoving == LEFT) {
			x = x - shipSpeed / fps;
		}

		if (shipMoving == RIGHT) {
			x = x + shipSpeed / fps;
		}

		rect.top = y;
		rect.bottom = y + height;
		rect.left = x;
		rect.right = x + length;
	}
}
