/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Spooky Games
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
package games.spooky.gdx.sfx;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;

public class SfxMusicWrapper implements SfxMusic {

	private final Music wrapped;

	private final String title;
	private final float duration; // In seconds
	
	private final Array<SfxMusicEffect> effects = new Array<SfxMusicEffect>(0);

	private float position = 0f;

	private boolean paused = false;
	private boolean stopPending = false;

	// Copy of some stuff as we have no direct access to them
	private float pan = 0f;
	private OnCompletionListener listener = null;

	public SfxMusicWrapper(Music wrappedMusic, String title, float duration) {
		this.wrapped = wrappedMusic;
		this.title = title;
		this.duration = duration;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public float getDuration() {
		return duration;
	}

	@Override
	public void play() {
		wrapped.play();

		if (paused) {
			paused = false;
		} else {
			position = 0f;
			Array<SfxMusicEffect> effects = this.effects;
			for (int i = 0, n = effects.size ; i < n ; i++) {
				SfxMusicEffect effect = effects.get(i);
				effect.restart();
			}
		}
		stopPending = false;
	}

	@Override
	public void pause() {
		paused = true;
		wrapped.pause();
	}

	@Override
	public void stop() {
		if (hasEffects()) {
			stopPending = true;
			Array<SfxMusicEffect> effects = this.effects;
			for (int i = 0, n = effects.size ; i < n ; i++) {
				SfxMusicEffect effect = effects.get(i);
				effect.stop(position);
			}
		} else {
			doStop();
		}
	}
	
	private void doStop() {
		wrapped.stop();
		if (listener != null)
			listener.onCompletion(this);
	}

	@Override
	public boolean isPlaying() {
		return wrapped.isPlaying();
	}

	@Override
	public void setLooping(boolean isLooping) {
		wrapped.setLooping(isLooping);
	}

	@Override
	public boolean isLooping() {
		return wrapped.isLooping();
	}

	@Override
	public void setVolume(float volume) {
		wrapped.setVolume(volume);
	}

	@Override
	public float getVolume() {
		return wrapped.getVolume();
	}

	@Override
	public float getPan() {
		return this.pan;
	}

	/** Just so you know: Panning only works with Mono samples! Now you know. */
	@Override
	public void setPan(float pan, float volume) {
		this.pan = pan;
		wrapped.setPan(pan, volume);
	}
	
	@Override
	public float getPosition() {
		return position;
	}

	@Override
	public void setPosition(float position) {
		wrapped.setPosition(position);
		this.position = position;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		wrapped.setOnCompletionListener(listener);
		this.listener = listener;
	}
	
	public Array<SfxMusicEffect> getEffects() {
		return effects;
	}
	
	public boolean hasEffects () {
		return effects.size > 0;
	}
	
	public void addEffect(SfxMusicEffect effect) {
		if(this.effects.contains(effect, true))
			return;
		effect.setMusic(this);
		this.effects.add(effect);
	}
	
	public void removeEffect(SfxMusicEffect effect) {
		if (effects.removeValue(effect, true)) effect.setMusic(null);
	}
	
	public void clearEffects() {
		for (int i = effects.size - 1; i >= 0; i--)
			effects.get(i).setMusic(null);
		effects.clear();
	}

	@Override
	public boolean update(float deltaTime) {
		if (isPlaying()) {
			float position = this.position + deltaTime;
			this.position = position;
			
			// Apply effects
			boolean effectsOver = true;
			Array<SfxMusicEffect> effects = this.effects;
			int size = effects.size;
			
			if (size > 0) {
				for (int i = 0 ; i < size ; i++) {
					SfxMusicEffect effect = effects.get(i);
					effectsOver = effect.update(position) && effectsOver;
				}
			}
			
			if (stopPending && effectsOver) {
				doStop();
				position = 0f;
				return true;
			} else if (isLooping()) {
				float duration = getDuration();
				if (position > duration) {
					position -= duration;
					for (int i = 0 ; i < size ; i++) {
						SfxMusicEffect effect = effects.get(i);
						effect.restart();
					}
				}
			}
			return false;
		} else {
			this.position = 0f;
			return true;
		}
	}

	@Override
	public void dispose() {
		clearEffects();
		wrapped.dispose();
	}

	@Override
	public String toString() {
		return title + " [" + getPosition() + "/" + duration + "] (SfxMusicWrapper)";
	}

}
