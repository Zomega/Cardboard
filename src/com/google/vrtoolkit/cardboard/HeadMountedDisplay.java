package com.google.vrtoolkit.cardboard;

import android.view.Display;

public class HeadMountedDisplay {
	private ScreenParams mScreen;
	private CardboardDeviceParams mCardboard;

	public HeadMountedDisplay(Display display) {
		this.mScreen = new ScreenParams(display);
		this.mCardboard = new CardboardDeviceParams();
	}

	public HeadMountedDisplay(HeadMountedDisplay hmd) {
		this.mScreen = new ScreenParams(hmd.mScreen);
		this.mCardboard = new CardboardDeviceParams(hmd.mCardboard);
	}

	public void setScreen(ScreenParams screen) {
		this.mScreen = new ScreenParams(screen);
	}

	public ScreenParams getScreen() {
		return this.mScreen;
	}

	public void setCardboard(CardboardDeviceParams cardboard) {
		this.mCardboard = new CardboardDeviceParams(cardboard);
	}

	public CardboardDeviceParams getCardboard() {
		return this.mCardboard;
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof HeadMountedDisplay)) {
			return false;
		}
		HeadMountedDisplay o = (HeadMountedDisplay) other;

		return (this.mScreen.equals(o.mScreen))
				&& (this.mCardboard.equals(o.mCardboard));
	}
}
