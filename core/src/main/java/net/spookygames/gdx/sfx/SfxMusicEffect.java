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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

@SuppressWarnings("rawtypes")
abstract public class SfxMusicEffect implements Poolable {

	protected SfxMusic music;

	private Pool pool;

	private float duration;
	private Interpolation interpolation = Interpolation.linear;
	
	private boolean began;
	private boolean complete;

	public SfxMusic getMusic() {
		return music;
	}

	@SuppressWarnings("unchecked")
	public void setMusic(SfxMusic music) {
		this.music = music;
		if (music == null) {
			if (pool != null) {
				pool.free(this);
				pool = null;
			}
		}
	}

	public Pool getPool() {
		return pool;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
	}

	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public Interpolation getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation == null ? Interpolation.linear : interpolation;
	}

	@Override
	public void reset() {
		music = null;
		pool = null;
		interpolation = Interpolation.linear;
		restart();
	}

	public void restart() {
		began = false;
		complete = false;
	}

	public boolean update(float position) {
		if (complete)
			return true;

		// Ensure this action can't be returned to the pool while executing.
		Pool pool = getPool();
		setPool(null);

		try {
			if (!began) {
				begin();
				began = true;
			}
			complete = apply(position);
			if (complete)
				end();
			return complete;
		} finally {
			setPool(pool);
		}
	}

	protected void begin() {
	}

	protected void end() {
	}

	public void stop(float position) {
		// End before the end
	}

	abstract protected boolean apply(float position);

}
