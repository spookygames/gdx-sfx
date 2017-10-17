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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class SomeSoundSpatializer2 implements Spatializer<Vector2> {

	private float horizontalRange;
	private float verticalRange;
	private final Vector3 center = new Vector3();
	
	public float getHorizontalRange() {
		return horizontalRange;
	}

	public void setHorizontalRange(float horizontalRange) {
		if (this.horizontalRange == horizontalRange)
			return;
		this.horizontalRange = horizontalRange;
	}

	public float getVerticalRange() {
		return verticalRange;
	}

	public void setVerticalRange(float verticalRange) {
		if (this.verticalRange == verticalRange)
			return;
		this.verticalRange = verticalRange;
	}

	public Vector3 getCenter() {
		return center;
	}

	public void setCenter(float x, float y, float z) {
		if (this.center.epsilonEquals(x, y, z, 0.01f))
			return;
		this.center.set(x, y, z);
	}

	public void setCenter(Vector3 center) {
		if (this.center.epsilonEquals(center, 0.01f))
			return;
		this.center.set(center);
	}

	@Override
	public void spatialize(SpatializedSound<Vector2> instance, float nominalVolume) {
		Vector2 position = instance.getPosition();
		float x = position.x;
		float y = position.y;

		float centerX = center.x;
		float centerY = center.y;

		// Horizontal attenuation
		float range2 = horizontalRange * horizontalRange;
		float dst2 = Vector2.dst2(x, y, centerX, centerY);

		float hRatio = 1f - MathUtils.clamp(dst2 / range2, 0f, 1f);

		// Vertical attenuation
		float centerZ = center.z;

		float vRatio = 1f - MathUtils.clamp(centerZ / verticalRange, 0f, 1f);
		vRatio = vRatio * vRatio * vRatio;
		
		// Panning
		float panning = (x - centerX) / horizontalRange;

		// Result
		float volume = nominalVolume * hRatio * vRatio;
		instance.setPan(MathUtils.clamp(panning, -1f, 1f), MathUtils.clamp(volume, 0f, 1f));
	}

}
