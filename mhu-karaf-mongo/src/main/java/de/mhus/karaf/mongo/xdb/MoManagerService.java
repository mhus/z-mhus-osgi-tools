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
package de.mhus.karaf.mongo.xdb;

import de.mhus.lib.errors.MException;
import de.mhus.lib.mongo.MoManager;

public interface MoManagerService {

	void doOpen() throws MException;
	
	void doInitialize();

	void doClose();

	String getServiceName();

	MoManager getManager();

	String getMongoDataSourceName();
	
	boolean isConnected();
	
}