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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.errors.MException;

public class AccountFromFile extends MLog implements AccountSource, ModifyAccountApi {

    private String path = "aaa/account/";

    @Override
    public Account findAccount(String account) {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists() || !file.isFile()) {
            log().w("file not found", file);
            return null;
        }

        try {
            return new AccountFile(file, account);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log().e(account, e);
            return null;
        }
    }

    /*
    <user>
      <password plain="12345" />
      <information name="Admin" />
      <attributes>
        <attribute name="privatekey" value=""/>
      </attributes>
      <groups>
        <group name="ADMINISTRATOR"/>
      </groups>
    </user>
    	 */
    @Override
    public void createAccount(String account, String password, IReadProperties properties)
            throws MException {

        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (file.exists()) throw new MException("Account already exists", account);

        try {
            Document doc = MXml.createDocument();
            Element rootE = doc.createElement("user");
            doc.appendChild(rootE);
            rootE.setAttribute("created", MDate.toIsoDateTime(System.currentTimeMillis()));
            // Element passE = doc.createElement("password");
            // rootE.appendChild(passE);
            rootE.setAttribute("password", MPassword.encodePasswordMD5(password));
            Element infoE = doc.createElement("information");
            rootE.appendChild(infoE);
            infoE.setAttribute("name", properties.getString(AccessApi.DISPLAY_NAME, account));
            Element attrE = doc.createElement("attributes");
            rootE.appendChild(attrE);

            for (Entry<String, Object> entry : properties.entrySet()) {
                Element aE = doc.createElement("attribute");
                attrE.appendChild(aE);
                aE.setAttribute("name", entry.getKey());
                aE.setAttribute("value", String.valueOf(entry.getValue()));
            }

            Element groupE = doc.createElement("groups");
            rootE.appendChild(groupE);

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public void deleteAccount(String account) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);

        file.delete();
    }

    @Override
    public void changePasswordInternal(String account, String newPassword) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));
            rootE.setAttribute("password", newPassword);

            Element passE = MXml.getElementByPath(rootE, "password");
            if (passE != null) rootE.removeChild(passE); // password node overrides root password

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public void changePassword(String account, String newPassword) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);
        newPassword = MPassword.encodePasswordMD5(newPassword);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));
            rootE.setAttribute("password", newPassword);

            Element passE = MXml.getElementByPath(rootE, "password");
            if (passE != null) rootE.removeChild(passE); // password node overrides root password

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public void changeAccount(String account, IReadProperties properties) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));

            Element infoE = MXml.getElementByPath(rootE, "information");
            rootE.appendChild(infoE);
            infoE.setAttribute("name", properties.getString(AccessApi.DISPLAY_NAME, account));

            Element attrE = MXml.getElementByPath(rootE, "attributes");
            // remove all
            while (attrE.hasChildNodes()) attrE.removeChild(attrE.getFirstChild());
            // set new
            for (Entry<String, Object> entry : properties.entrySet()) {
                Element aE = doc.createElement("attribute");
                attrE.appendChild(aE);
                aE.setAttribute("name", entry.getKey());
                aE.setAttribute("value", String.valueOf(entry.getValue()));
            }

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public void appendGroups(String account, String... group) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));
            Element groupsE = MXml.getElementByPath(rootE, "groups");

            HashSet<String> groups = new HashSet<>();
            while (groupsE.hasChildNodes()) {
                Node childE = groupsE.getFirstChild();
                if (childE instanceof Element) {
                    String name = ((Element) childE).getAttribute("name");
                    groups.add(name);
                }
                groupsE.removeChild(childE);
            }

            for (String name : group) groups.add(name);

            for (String name : groups) {
                Element groupE = doc.createElement("group");
                groupsE.appendChild(groupE);
                groupE.setAttribute("name", name);
            }

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public void removeGroups(String account, String... group) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));
            Element groupsE = MXml.getElementByPath(rootE, "groups");

            HashSet<String> groups = new HashSet<>();
            while (groupsE.hasChildNodes()) {
                Node childE = groupsE.getFirstChild();
                if (childE instanceof Element) {
                    String name = ((Element) childE).getAttribute("name");
                    groups.add(name);
                }
                groupsE.removeChild(childE);
            }

            for (String name : group) groups.remove(name);

            for (String name : groups) {
                Element groupE = doc.createElement("group");
                groupsE.appendChild(groupE);
                groupE.setAttribute("name", name);
            }

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public Collection<String> getGroups(String account) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));
            Element groupsE = MXml.getElementByPath(rootE, "groups");

            HashSet<String> groups = new HashSet<>();
            while (groupsE.hasChildNodes()) {
                Node childE = groupsE.getFirstChild();
                if (childE instanceof Element) {
                    String name = ((Element) childE).getAttribute("name");
                    groups.add(name);
                }
                groupsE.removeChild(childE);
            }
            return groups;
        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public ModifyAccountApi getModifyApi() {
        return this;
    }

    @Override
    public Collection<String> getAccountList(String filter) {
        File dir = MApi.getFile(MApi.SCOPE.DATA, path);
        LinkedList<String> out = new LinkedList<>();
        for (String name : dir.list()) {
            if (name.endsWith(".xml")) {
                name = name.substring(0, name.length() - 4);
                if (filter == null || MString.compareFsLikePattern(name, filter)) out.add(name);
            }
        }
        return out;
    }

    @Override
    public void activateAccount(String account, boolean active) throws MException {
        File file = MApi.getFile(MApi.SCOPE.DATA, path + MFile.normalize(account.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Account not found", account);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("active", String.valueOf(active));

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }
}
