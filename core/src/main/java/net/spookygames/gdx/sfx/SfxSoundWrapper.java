/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2019 Spooky Games
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

public class SfxSoundWrapper implements SfxSound {

	private final Sound wrapped;

	private final String title;
	private final float duration; // In seconds

	public SfxSoundWrapper(Sound wrappedSound, String title, float duration) {
		this.wrapped = wrappedSound;
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
	public long play() {
		return wrapped.play();
	}

	@Override
	public long play(float volume) {
		return wrapped.play(volume);
	}

	@Override
	public long play(float volume, float pitch, float pan) {
		return wrapped.play(volume, pitch, pan);
	}

	@Override
	public long loop() {
		return wrapped.loop();
	}

	@Override
	public long loop(float volume) {
		return wrapped.loop(volume);
	}

	@Override
	public long loop(float volume, float pitch, float pan) {
		return wrapped.loop(volume, pitch, pan);
	}

	@Override
	public void stop() {
		wrapped.stop();
	}

	@Override
	public void pause() {
		wrapped.pause();
	}

	@Override
	public void resume() {
		wrapped.resume();
	}

	@Override
	public void dispose() {
		wrapped.dispose();
	}

	@Override
	public void stop(long soundId) {
		wrapped.stop(soundId);
	}

	@Override
	public void pause(long soundId) {
		wrapped.pause(soundId);
	}

	@Override
	public void resume(long soundId) {
		wrapped.resume(soundId);
	}

	@Override
	public void setLooping(long soundId, boolean looping) {
		wrapped.setLooping(soundId, looping);
	}

	@Override
	public void setPitch(long soundId, float pitch) {
		wrapped.setPitch(soundId, pitch);
	}

	@Override
	public void setVolume(long soundId, float volume) {
		wrapped.setVolume(soundId, volume);
	}

	/** Just so you know: Panning only works with Mono samples! Now you know. */
	@Override
	public void setPan(long soundId, float pan, float volume) {
		wrapped.setPan(soundId, pan, volume);
	}

	@Override
	public String toString() {
		return title + " [" + duration + "] (SfxSoundWrapper)";
	}

}
