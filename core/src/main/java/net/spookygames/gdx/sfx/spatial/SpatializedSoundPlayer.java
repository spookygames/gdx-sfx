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

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.LongMap.Entry;
import com.badlogic.gdx.utils.Pool;

import net.spookygames.gdx.sfx.SfxSound;

public class SpatializedSoundPlayer<T> {
	
	private final Pool<SpatializedSound<T>> pool = new Pool<SpatializedSound<T>>() {
		@Override
		protected SpatializedSound<T> newObject() {
			return new SpatializedSound<T>();
		};
	};

	private final LongMap<SpatializedSound<T>> sounds = new LongMap<SpatializedSound<T>>();
	
	private Spatializer<T> spatializer;

	private float volume = 1f;

	public Spatializer<T> getSpatializer() {
		return spatializer;
	}

	public void setSpatializer(Spatializer<T> spatializer) {
		this.spatializer = spatializer;
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public long play(T position, SfxSound sound) {
		return play(position, sound, 1f, false);
	}

	public long play(T position, SfxSound sound, float pitch, boolean looping) {

		SpatializedSound<T> instance = pool.obtain();
		
		float duration = sound.getDuration();
		
		Spatializer<T> spatializer = this.spatializer;
		
		long id = instance.initialize(sound, duration, position, 0f, pitch, 0f);

		if (id == -1) {
			pool.free(instance);
			Gdx.app.error("gdx-sfx", "Couldn't play sound " + sound);
		} else {
			instance.setLooping(looping);
			spatializer.spatialize(id, position, sound, this.volume);

			sounds.put(id, instance);
		}
		
		return id;
	}

	public void update(float delta) {
		Spatializer<T> spatializer = this.spatializer;
		
		Iterator<Entry<SpatializedSound<T>>> iterator = sounds.iterator();
		while (iterator.hasNext()) {
			Entry<SpatializedSound<T>> entry = iterator.next();
			SpatializedSound<T> instance = entry.value;

			if (instance.update(delta)) {
				iterator.remove();
				pool.free(instance);
			} else {
				spatializer.spatialize(entry.key, instance.position, instance.sound, this.volume);
			}
		}
	}

	public void stop() {
		Pool<SpatializedSound<T>> pool = this.pool;
		LongMap<SpatializedSound<T>> sounds = this.sounds;
		for (SpatializedSound<T> object : sounds.values())
			pool.free(object);
		sounds.clear();
	}

	public void stop(long id) {
		SpatializedSound<T> sound = sounds.remove(id);

		if (sound != null) {
			sound.stop();
			pool.free(sound);
		}
	}
}
