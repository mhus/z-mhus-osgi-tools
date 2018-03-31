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
package de.mhus.osgi.crypt.api;

import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;

public interface BlockSign {

	/**
	 * Check Signature.
	 * @param key 
	 * 
	 * @param text
	 * @param sign 
	 * @return true if valid
	 */
	boolean validate(PemPub key, String text, String sign);

	/**
	 * Create a signature. Return the result as string block
	 * @param key 
	 * 
	 * @param text
	 * @return signed string
	 */
	String sign(PemPriv key, String text);

	/**
	 * Returns the identifier of the sign method
	 * 
	 * @return the name
	 */
	String getName();

}
