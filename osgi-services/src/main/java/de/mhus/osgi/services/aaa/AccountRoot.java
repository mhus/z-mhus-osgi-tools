/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.services.aaa;

import java.util.Date;
import java.util.UUID;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.NotSupportedException;

public class AccountRoot implements Account {

    private MProperties attributes = new MProperties();

    @Override
    public String getName() {
        return AccessApi.ROOT_NAME;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean validatePassword(String password) {
        return false;
    }

    @Override
    public String toString() {
        return AccessApi.ROOT_NAME;
    }

    @Override
    public boolean isSynthetic() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return AccessApi.ROOT_NAME;
    }

    @Override
    public boolean hasGroup(String group) {
        return true;
    }

    @Override
    public IReadProperties getAttributes() {
        return attributes;
    }

    @Override
    public void putAttributes(IReadProperties properties) throws NotSupportedException {
        attributes.putReadProperties(properties);
    }

    @Override
    public String[] getGroups() throws NotSupportedException {
        throw new NotSupportedException();
    }

    @Override
    public boolean reloadAccount() {
        return false;
    }

    @Override
    public Date getCreationDate() {
        return null;
    }

    @Override
    public Date getModifyDate() {
        return null;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
