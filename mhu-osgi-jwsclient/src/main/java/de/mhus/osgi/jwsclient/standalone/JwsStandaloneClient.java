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
package de.mhus.osgi.jwsclient.standalone;

import java.io.IOException;

import de.mhus.osgi.jwsclient.Client;
import de.mhus.osgi.jwsclient.Target;
import de.mhus.osgi.jwsclient.TargetFactory;

public class JwsStandaloneClient extends Client {

	private static JwsStandaloneClient instance;

	private JwsStandaloneClient() {}
	
	public static synchronized JwsStandaloneClient instance() {
		if (instance == null)
			instance = new JwsStandaloneClient();
		return instance;
	}
	
	@Override
	public Target createTarget(String url) throws IOException {
		return super.createTarget(url);
	}
	
	@Override
	public void registerFactory(String name, TargetFactory factory) {
		super.registerFactory(name, factory);
	}


	
}
