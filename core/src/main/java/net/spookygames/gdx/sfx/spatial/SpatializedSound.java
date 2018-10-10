/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 Spooky Games
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
package net.spookygames.gdx.sfx.spatial;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Pool.Poolable;

public class SpatializedSound<T> implements Poolable {

	private Sound sound;
	private long id;
	private float duration;
	private T position;
	private float volume;
	private float pitch;
	private float pan;
	private float fadeTime;

	private float elapsed;

	private boolean running = false;
	private boolean looping = false;

	private float fadeProgress = -1;
	private boolean fadeIn;
	private float realVolume;

	private boolean stop = false;

	@Override
	public void reset() {
		if (sound != null)
			sound.stop(id);
		sound = null;

		id = -1L;

		duration = -1f;

		position = null;

		fadeTime = 0;
		fadeProgress = -1;
		volume = 1f;
		pitch = 1f;
		pan = 0f;

		elapsed = Float.MAX_VALUE;

		stop = false;
		running = false;
		looping = false;
	}

	public long initialize(Sound sound, float duration, T position, float volume, float pitch, float panning, float fadeTime, boolean fadeIn) {
		this.sound = sound;
		this.duration = duration;

		this.position = position;

		this.elapsed = 0f;

		running = true;
		this.fadeTime = fadeTime;
		this.volume = volume;
		this.id = sound.play(fadeIn ? 0 : volume, this.pitch = pitch, this.pan = panning);
		if (fadeIn) {
			fadeIn();
		}

		return this.id;
	}

	public long getId() {
		return id;
	}

	public Sound getSound() {
		return sound;
	}

	public float getDuration() {
		return this.duration;
	}

	public T getPosition() {
		return position;
	}

	public void setPosition(T position) {
		this.position = position;
	}

	public float getPitch() {
		return this.pitch;
	}

	public void setPitch(float pitch) {
		if (this.pitch != pitch) {
			this.pitch = pitch;
			sound.setPitch(id, pitch);
		}
	}

	public float getVolume() {
		return this.volume;
	}

	public void setVolume(float volume) {
		// setup correct target volume we got from spatialize
		if (elapsed == 0 && fadeProgress > -1) {
			realVolume = volume;
		} else if (this.volume != volume) {
			this.volume = volume;
			sound.setVolume(id, volume);
		}
	}

	public float getPan() {
		return this.pan;
	}

	public void setPan(float pan, float volume) {
		// setup correct target volume we got from spatialize
		if (elapsed == 0 && fadeProgress > -1) {
			realVolume = volume;
			this.pan = pan;
			sound.setPan(id, pan, 0);
		} else if (this.pan != pan || this.volume != volume) {
			this.pan = pan;
			this.volume = volume;
			sound.setPan(id, pan, volume);
		}
	}

	public boolean isLooping() {
		return looping;
	}

	public void setLooping(boolean looping) {
		if (this.looping != looping) {
			this.looping = looping;
			sound.setLooping(id, looping);
		}
	}

	public boolean isFading() {
		return fadeProgress > -1;
	}

	public boolean update(float deltaTime) {
		if (sound == null) {
			return true;
		}

		if (running) {
			elapsed += deltaTime;
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
						sound.pause(id);
						running = false;
					}
				}
			}
		}

		if (elapsed >= duration) {
			elapsed -= duration;

			if (looping) {
				return false;
			} else {
				running = false;
				return true;
			}
		}

		return false;
	}

	public void stop() {
		if (fadeTime > 0) {
			stop = true;
			fadeOut();
		} else {
			reset();
		}
	}

	public void resume() {
		sound.resume(id);
		running = true;
		if (fadeTime > 0) {
			fadeIn();
		}
	}

	public void pause() {
		if (fadeTime > 0) {
			fadeOut();
		} else {
			sound.pause(id);
			running = false;
		}
	}

	public void fadeIn() {
		realVolume = volume;
		setVolume(0);
		fadeIn = true;
		fadeProgress = 0;
	}

	public void fadeOut() {
		realVolume = volume;
		fadeIn = false;
		fadeProgress = 0;
	}
}
