package com.fabianrethy.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class Invader extends SpaceShip {
	boolean isVisible;
	Random generator = new Random();

	public Invader(Context context, int row, int column, int screenX, int screenY) {
		rect = new RectF();

		length = screenX / 20;
		height = screenY / 20;

		isVisible = true;

		float padding = screenX / 25;

		x = column * (length + padding);
		y = row * (length + padding / 4);

		bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
		bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);
		bitmap1 = Bitmap.createScaledBitmap(bitmap1, (int)length, (int)height, false);
		bitmap2 = Bitmap.createScaledBitmap(bitmap2, (int)length, (int)height, false);

		shipSpeed = 40;
		shipMoving = RIGHT;
	}

	public void setInvisible() {
		isVisible = false;
	}

	public boolean getVisibility() {
		return isVisible;
	}
}
