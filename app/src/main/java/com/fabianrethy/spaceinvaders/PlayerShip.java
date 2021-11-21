package com.fabianrethy.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class PlayerShip extends SpaceShip {

	public PlayerShip(Context context, int screenX, int screenY) {
		rect = new RectF();

		length = screenX / 10;
		height = screenY / 10;
		x = screenX / 2;
		y = screenY - 20;
		bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
		bitmap1 = Bitmap.createScaledBitmap(bitmap1, (int) (length), (int) (height), false);
		shipSpeed = 350;
		shipMoving = STOPPED;
	}

	public void setMovementState(int state) {
		shipMoving = state;
	}
}

