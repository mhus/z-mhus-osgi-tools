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
package de.mhus.osgi.services.aaa.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.security.AaaUtil;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AuthorizationSource;
import de.mhus.lib.core.security.ModifyAuthorizationApi;
import de.mhus.lib.errors.MException;

public class AuthFromFile extends MLog implements AuthorizationSource, ModifyAuthorizationApi {

    @Override
    public Boolean hasResourceAccess(Account account, String aclName) {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/groupmapping/"
                                + MFile.normalize(aclName.trim()).toLowerCase()
                                + ".txt");
        if (!file.exists()) {
            log().w("file not found", file);
            return null;
        }
        try {
            List<String> acl = MFile.readLines(file, true);
            return AaaUtil.hasAccess(account, acl);
        } catch (IOException e) {
            log().w("read error", file);
            return null;
        }
    }

    @Override
    public String getResourceAccessAcl(Account account, String aclName) {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/groupmapping/"
                                + MFile.normalize(aclName.trim()).toLowerCase()
                                + ".txt");
        if (!file.exists()) {
            log().w("file not found", file);
            return null;
        }
        String acl = MFile.readFile(file);
        return acl;
    }

    @Override
    public void createAuthorization(String aclName, String acl) throws MException {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/groupmapping/"
                                + MFile.normalize(aclName.trim()).toLowerCase()
                                + ".txt");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        MFile.writeFile(file, acl);
    }

    @Override
    public void deleteAuthorization(String aclName) throws MException {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/groupmapping/"
                                + MFile.normalize(aclName.trim()).toLowerCase()
                                + ".txt");
        if (!file.exists()) throw new MException("authorization not found", aclName);

        file.delete();
    }

    @Override
    public ModifyAuthorizationApi getModifyApi() {
        return this;
    }

    @Override
    public String getAuthorizationAcl(String aclName) throws MException {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/groupmapping/"
                                + MFile.normalize(aclName.trim()).toLowerCase()
                                + ".txt");
        if (!file.exists()) throw new MException("authorization not found", aclName);
        return MFile.readFile(file);
    }
}
