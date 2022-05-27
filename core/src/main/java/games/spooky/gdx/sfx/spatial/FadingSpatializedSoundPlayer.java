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

import java.util.Iterator;

import com.badlogic.gdx.Gdx;

import games.spooky.gdx.sfx.SfxSound;

public class FadingSpatializedSoundPlayer<T> extends SpatializedSoundPlayer<T> {
	private float fadeTime = 0f;

	public void setFadeTime(float fadeTime) {
		this.fadeTime = fadeTime;
	}

	public float getFadeTime() {
		return fadeTime;
	}

	@Override
	protected SpatializedSound<T> newObject() {
		return new FadingSpatializedSound<T>();
	}

	public long play(T position, SfxSound sound, float pitch, boolean looping, boolean fadeIn) {
	    return play(position, sound, 1f, pitch, looping, fadeIn);
	}
	
	public long play(T position, SfxSound sound, float intrinsicVolume, float pitch, boolean looping, boolean fadeIn) {
		FadingSpatializedSound<T> instance = (FadingSpatializedSound<T>) pool.obtain();

		float duration = sound.getDuration();

		Spatializer<T> spatializer = this.spatializer;

		long id = instance.initialize(sound, duration, position, 0f, pitch, 0f,	intrinsicVolume, fadeTime, fadeIn);

		if (id == -1) {
			pool.free(instance);
			Gdx.app.error("gdx-sfx", "Couldn't play sound " + sound);
		} else {
			instance.setLooping(looping);
			spatializer.spatialize(instance, this.volume);

			sounds.put(id, instance);
		}

		return id;
	}

	@Override
	public void update(float delta) {
		Spatializer<T> spatializer = this.spatializer;

		Iterator<SpatializedSound<T>> iterator = sounds.values();
		while (iterator.hasNext()) {
			SpatializedSound<T> instance = iterator.next();

			if (instance.update(delta)) {
				iterator.remove();
				pool.free(instance);
			} else if (!((FadingSpatializedSound<T>) instance).isFading()) {
				spatializer.spatialize(instance, this.volume);
			}
		}
	}
	
	@Override
	public void stop(long id) {
		SpatializedSound<T> sound = sounds.get(id);

		if (sound != null) {
			sound.stop();
		}
	}
}
