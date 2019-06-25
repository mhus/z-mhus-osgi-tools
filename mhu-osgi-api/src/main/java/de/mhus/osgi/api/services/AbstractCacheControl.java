/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.api.services;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;

public abstract class AbstractCacheControl extends MLog implements CacheControlIfc {

	protected boolean enabled = true;
	protected boolean supportDisable = true;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!supportDisable) return;
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return MSystem.toString(this,getSize(),isEnabled() + (supportDisable ? "" : "!") );
	}
	
	@Override
	public String getName() {
		return getClass().getCanonicalName();
	}
	
}
