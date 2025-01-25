/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2025 Spooky Games
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

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

public class SfxPitchShiftingSoundWrapper extends SfxSoundWrapper {

	private final float minPitch;
	private final float maxPitch;

	public SfxPitchShiftingSoundWrapper(Sound wrappedSound, String title, float duration, float pitchRange) {
		super(wrappedSound, title, duration);
		float half = pitchRange / 2f;
		this.minPitch = 1f - half;
		this.maxPitch = 1f + half;
	}

	public SfxPitchShiftingSoundWrapper(Sound wrappedSound, String title, float duration, float minPitch, float maxPitch) {
		super(wrappedSound, title, duration);
		this.minPitch = minPitch;
		this.maxPitch = maxPitch;
	}

	@Override
	public long play() {
		return super.play(1f, randomPitchShift(), 0f);
	}

	@Override
	public long play(float volume) {
		return super.play(volume, randomPitchShift(), 0f);
	}

	@Override
	public long play(float volume, float pitch, float pan) {
		return super.play(volume, pitch * randomPitchShift(), pan);
	}

	@Override
	public long loop() {
		return super.loop(1f, randomPitchShift(), 0f);
	}

	@Override
	public long loop(float volume) {
		return super.loop(volume, randomPitchShift(), 0f);
	}

	@Override
	public long loop(float volume, float pitch, float pan) {
		return super.loop(volume, pitch, pan);
	}

	@Override
	public void setPitch(long soundId, float pitch) {
		super.setPitch(soundId, pitch * randomPitchShift());
	}

	private float randomPitchShift() {
		return MathUtils.random(minPitch, maxPitch);
	}

}
