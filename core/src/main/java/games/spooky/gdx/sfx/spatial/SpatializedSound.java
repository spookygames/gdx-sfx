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
import com.badlogic.gdx.utils.Pool.Poolable;

public class SpatializedSound<T> implements Poolable {

	private Sound sound;
	private long id;
	private float duration;
	private T position;

	/** 
	 * Intrinsic volume of this sound, set at init, and multiples all subsequent volumes.
	 * This allows us to dynamically change the volume of a sound at runtime without
	 * having to modify the source file directly.
	 **/
	private float intrinsicVolume;

	/** current (realtime) volume of this sound, modified when spatializing */
	private float volume;
	
	private float pitch;
	private float pan;

	protected float elapsed;

	private boolean running = false;
	private boolean looping = false;

	@Override
	public void reset() {
		if (sound != null)
			sound.stop(id);
		sound = null;

		id = -1L;

		duration = -1f;

		position = null;

		volume = 1f;
		intrinsicVolume = 1f;
		pitch = 1f;
		pan = 0f;

		elapsed = Float.MAX_VALUE;

		running = false;
		looping = false;
	}

	public long initialize(Sound sound, float duration, T position, float volume, float pitch, float panning, float intrinsicVolume) {
		this.sound = sound;
		this.duration = duration;
		this.intrinsicVolume = intrinsicVolume;
		this.volume = volume;
		this.pitch = pitch;
		this.pan = panning;

		this.position = position;

		this.elapsed = 0f;

		running = true;
		
		float effectiveVolume = this.volume * this.intrinsicVolume;		
		return this.id = sound.play(effectiveVolume, this.pitch, this.pan);
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

	/** Get the current (realtime) volume of this sound. */
	public float getVolume() {
		return this.volume;
	}

    /**
     * Set the realtime volume of this sound.
     * @param volume multiplied by {@link #intrinsicVolume} to get an effective realtime volume
     */
	public void setVolume(float volume) {
	    float effectiveVolume = volume * this.intrinsicVolume;
		if (this.volume != effectiveVolume) {
			this.volume = effectiveVolume;
			sound.setVolume(id, effectiveVolume);
		}
	}
	
	/** Get the intrinsic volume of this sound, which is multiplied against all subsequent volume changes */ 
    public float getIntrinsicVolume() {
        return this.intrinsicVolume;
    }

	public float getPan() {
		return this.pan;
	}

	/**
	 * Set the realtime pan and volume of this sound.
	 * @param volume multiplied by {@link #intrinsicVolume} to get an effective realtime volume
	 */
	public void setPan(float pan, float volume) {
	    float effectiveVolume = volume * this.intrinsicVolume;
		if (this.pan != pan || this.volume != effectiveVolume) {
			this.pan = pan;
			this.volume = effectiveVolume;
			sound.setPan(id, pan, effectiveVolume);
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

	public boolean update(float deltaTime) {
		if (running) {
			elapsed += deltaTime;
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
		reset();
	}

	public void resume() {
		sound.resume(id);
		running = true;
	}

	public void pause() {
		sound.pause(id);
		running = false;
	}
}
