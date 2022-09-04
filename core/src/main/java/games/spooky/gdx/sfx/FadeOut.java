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

import com.badlogic.gdx.math.MathUtils;

public class FadeOut extends SfxMusicEffect {

	private float baseVolume;
	private float beginning;
	
	@Override
	public void setMusic(SfxMusic music) {
		super.setMusic(music);
		if (music != null)
			beginning = music.getDuration() - this.getDuration();
	}

	@Override
	protected void begin() {
		super.begin();
		baseVolume = -1f;
	}

	@Override
	protected boolean apply(float position) {
		if (position < beginning) {
			return false;
		} else {
			float ratio = (position - beginning) / getDuration();
			if (ratio <= 1.0f) {
				float target = this.baseVolume;
				if (target < 0f)
					this.baseVolume = target = music.getVolume();
				music.setVolume(MathUtils.clamp(getInterpolation().apply(target, 0f, ratio), 0f, 1f));
				return false;
			} else {
				return true;
			}
		}

	}

	@Override
	public void stop(float position) {
		beginning = position;
	}

	@Override
	protected void end() {
		super.end();
		music.setVolume(0f);
	}

	@Override
	public void reset() {
		super.reset();
		beginning = 0f;
	}
	
	@Override
	public void restart() {
		super.restart();
	}

}
