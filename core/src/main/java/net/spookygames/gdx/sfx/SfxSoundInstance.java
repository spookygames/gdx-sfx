/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Spooky Games
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
package net.spookygames.gdx.sfx;

import com.badlogic.gdx.audio.Sound;

public class SfxSoundInstance {

	/** Interface definition for a callback to be invoked when playback of a sound has completed. */
	public interface OnCompletionListener {
		/** Called when the end of a sound is reached during playback.
		 * 
		 * @param sound the SoundInstance that reached the end */
		void onCompletion (SfxSoundInstance sound);
	}

	public final Sound sound;
	public final long id;
	public final float duration;
	private float elapsed;
	private float volume;
	private float panning;

	private boolean looping = false;
	private boolean running = true;
	private OnCompletionListener completionListener = null;

	public SfxSoundInstance(SfxSound container, float volume) {
		this(container, volume, 0f);
	}
	
	public SfxSoundInstance(SfxSound container, float volume, float panning) {
		super();
		this.sound = container.sound;
		this.volume = volume;
		this.panning = panning;
		this.id = sound.play(this.volume, 1f, this.panning);
		this.elapsed = 0f;
		this.duration = container.duration;
	}

	public boolean update(float deltaTime) {
		if(running) {
			elapsed += deltaTime;
		}
		if(elapsed >= duration) {

			elapsed -= duration;

			if(looping) {
				return false;
			} else {
				running = false;
				if(completionListener != null) {
					completionListener.onCompletion(this);
				}
				return true;
			}
		}

		return false;
	}

	public boolean isLooping() {
		return looping;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
		sound.setLooping(id, looping);
	}

	public float getVolume() {
		return this.volume;
	}

	public void setVolume(float volume) {
		if(volume > 1f)
			volume = 1f;
		if(volume < 0f)
			volume = 0f;
		
		this.volume = volume;
		sound.setVolume(id, volume);
	}

	public float getPanning() {
		return this.panning;
	}

	public void setPanning(float panning) {
		if(panning > 1f)
			panning = 1f;
		if(panning < -1f)
			panning = -1f;
		
		this.panning = panning;
		sound.setPan(id, panning, this.volume);
	}

	public OnCompletionListener getCompletionListener() {
		return completionListener;
	}

	public void setCompletionListener(OnCompletionListener completionListener) {
		this.completionListener = completionListener;
	}

	public void stop() {
		sound.stop(id);
		running = false;
		looping = false;
		elapsed = Float.MAX_VALUE;
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
