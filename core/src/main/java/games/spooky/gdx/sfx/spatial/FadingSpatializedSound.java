/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2021 Spooky Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package games.spooky.gdx.sfx.spatial;

import com.badlogic.gdx.audio.Sound;

public class FadingSpatializedSound<T> extends SpatializedSound<T> {
	private float fadeTime;
	private float fadeProgress = -1;
	private boolean fadeIn;
	private float realVolume;

	private boolean stop = false;

	@Override
	public void reset() {
		super.reset();
		fadeTime = 0;
		fadeProgress = -1;
		stop = false;
		fadeIn = false;
		realVolume = 0;
	}

	public long initialize(Sound sound, float duration, T position, float volume, float pitch, float panning, float intrinsicVolume, float fadeTime, boolean fadeIn) {
		long id = super.initialize(sound, duration, position, volume, pitch, panning, intrinsicVolume);

		this.fadeTime = fadeTime;

		if (fadeIn) {
			fadeIn();
		}

		return id;
	}

	public boolean isFading() {
		return fadeProgress > -1;
	}

	@Override
    public void setVolume(float volume) {
		// setup correct target volume we got from spatialize
		if (elapsed == 0 && fadeProgress > -1) {
			realVolume = volume;
		} else if (getVolume() != volume) {
			super.setVolume(volume);
		}
	}

    @Override
	public void setPan(float pan, float volume) {
		// setup correct target volume we got from spatialize
		if (elapsed == 0 && fadeProgress > -1) {
			realVolume = volume;
			super.setPan(pan, 0);
		} else {
			super.setPan(pan, volume);
		}
	}

	@Override
	public boolean update(float deltaTime) {
		if (getSound() == null) {
			return true;
		}

		if (fadeTime > 0 && fadeProgress > -1) {
			float progress = fadeProgress / fadeTime;
			if (!fadeIn)
				progress = 1 - progress;
			setVolume(progress * realVolume);

			fadeProgress += deltaTime;

			if (fadeProgress >= fadeTime) {
				fadeProgress = -1;
				setVolume(fadeIn ? realVolume : 0);

				if (!fadeIn) {
					if (stop) {
						reset();
					} else {
						super.pause();
					}
				}
			}
		}

		return super.update(deltaTime);
	}

	public void stop() {
		if (fadeTime > 0) {
			stop = true;
			fadeOut();
		} else {
			super.stop();
		}
	}

	public void resume() {
		super.resume();
		if (fadeTime > 0) {
			fadeIn();
		}
	}

	public void pause() {
		if (fadeTime > 0) {
			fadeOut();
		} else {
			super.pause();
		}
	}

	public void fadeIn() {
		realVolume = getVolume();
		setVolume(0);
		fadeIn = true;
		fadeProgress = 0;
	}

	public void fadeOut() {
		realVolume = getVolume();
		fadeIn = false;
		fadeProgress = 0;
	}
}
