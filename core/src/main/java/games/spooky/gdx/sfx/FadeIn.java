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

import com.badlogic.gdx.math.MathUtils;

public class FadeIn extends SfxMusicEffect {

	private float baseVolume;

	@Override
	protected void begin() {
		super.begin();
		baseVolume = music.getVolume();
	}

	@Override
	protected boolean apply(float position) {
		float ratio = position / getDuration();
		if (ratio <= 1.0f) {
			music.setVolume(MathUtils.clamp(getInterpolation().apply(0f, baseVolume, ratio), 0f, 1f));
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void end() {
		super.end();
		music.setVolume(baseVolume);
	}

	@Override
	public void reset() {
		super.reset();
		baseVolume = 0f;
	}

}
