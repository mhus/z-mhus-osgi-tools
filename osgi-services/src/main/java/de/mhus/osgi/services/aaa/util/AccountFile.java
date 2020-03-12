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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.crypt.MCrypt;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.NotSupportedException;

public class AccountFile extends MLog implements Account {

    private Document doc;
    private String account;
    private boolean valide;
    private File file;
    private long modified;
    private String name;
    private boolean active;
    private UUID uuid;

    private String passwordMd5 = null;
    private long timeout;
    private Boolean isPasswordValidated = null;
    private HashSet<String> groups = new HashSet<>();
    private MProperties attributes = new MProperties();
    private long created;

    public AccountFile(File f, String account)
            throws ParserConfigurationException, SAXException, IOException {
        this.account = account;
        valide = account != null;
        file = f;
        reloadInternal();
    }

    @Override
    public String getName() {
        return account;
    }

    @Override
    public boolean isValid() {
        return valide;
    }

    @Override
    public synchronized boolean validatePassword(String password) {
        if (isPasswordValidated == null) {
            try {
                if (MString.isSet(password))
                    isPasswordValidated = validatePasswordInternal(password);
            } catch (Throwable t) {
                log().w("validatePassword", account, t);
            }
        }
        return isPasswordValidated == null ? false : isPasswordValidated;
    }

    public boolean isChanged() {
        return !file.exists() || modified != file.lastModified();
    }

    @Override
    public String toString() {
        return account + " " + name;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return attributes.getString(MConstants.ADDR_DISPLAY_NAME, name);
    }

    public long getTimeout() {
        return timeout;
    }

    public boolean validatePasswordInternal(String password) {
        return MPassword.validatePasswordMD5(password, passwordMd5);
    }

    @Override
    public boolean hasGroup(String group) {
        return groups.contains(group);
    }

    @Override
    public IReadProperties getAttributes() {
        return attributes;
    }

    @Override
    public void putAttributes(IReadProperties properties) throws NotSupportedException {
        attributes.putReadProperties(properties);
        // save back ...
        doSave();
    }

    protected void doSave() {
        Element xmlAttr = MXml.getElementByPath(doc.getDocumentElement(), "attributes");
        if (xmlAttr == null) {
            xmlAttr = doc.createElement("attributes");
            doc.getDocumentElement().appendChild(xmlAttr);
        }
        doc.getDocumentElement().setAttribute("active", String.valueOf(active));
        for (Element elem : MXml.getLocalElementIterator(xmlAttr)) xmlAttr.removeChild(elem);
        for (Entry<String, Object> item : attributes.entrySet()) {
            Element attr = doc.createElement("attribute");
            attr.setAttribute("name", item.getKey());
            attr.setAttribute("value", String.valueOf(item.getValue()));
            xmlAttr.appendChild(attr);
        }
        try {
            MXml.saveXml(doc, file);
        } catch (Exception e) {
            log().w(file, e.toString());
            throw new NotSupportedException(e);
        }
    }

    @Override
    public String[] getGroups() throws NotSupportedException {
        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public boolean reloadAccount() {
        try {
            reloadInternal();
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log().d(name, e);
        }
        return false;
    }

    public void reloadInternal() throws ParserConfigurationException, SAXException, IOException {
        FileInputStream is = new FileInputStream(file);
        doc = MXml.loadXml(is);
        Element userE = doc.getDocumentElement();
        is.close();

        BasicFileAttributes fileAttr =
                Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        created = fileAttr.creationTime().toMillis();
        modified = file.lastModified();

        name = MXml.getElementByPath(doc.getDocumentElement(), "information").getAttribute("name");

        timeout = MCast.tolong(userE.getAttribute("timeout"), 0);

        {
            Element xmlGroups = MXml.getElementByPath(userE, "groups");
            groups.clear();
            for (Element xmlAce : MXml.getLocalElementIterator(xmlGroups, "group")) {
                groups.add(xmlAce.getAttribute("name").trim().toLowerCase());
            }
        }
        {
            Element xmlAttr = MXml.getElementByPath(userE, "attributes");
            attributes.clear();
            for (Element xmlAce : MXml.getLocalElementIterator(xmlAttr, "attribute")) {
                attributes.setString(xmlAce.getAttribute("name"), xmlAce.getAttribute("value"));
            }
        }

        Element pwE = MXml.getElementByPath(userE, "password");
        if (pwE != null)
            passwordMd5 = MCrypt.md5WithSalt(MPassword.decode(pwE.getAttribute("plain")));
        else passwordMd5 = userE.getAttribute("password");

        attributes.put("ro.created", userE.getAttribute("created"));

        active = MCast.toboolean(userE.getAttribute("active"), true);

        // as last - may need to save if not exists, this means all data must be read
        String uuidStr = userE.getAttribute("uuid");
        if (!MValidator.isUUID(uuidStr)) {
            uuid = UUID.randomUUID();
            userE.setAttribute("uuid", uuid.toString());
            doSave();
        }
    }

    public String getMd5Password() {
        return passwordMd5;
    }

    @Override
    public Date getCreationDate() {
        return new Date(created);
    }

    @Override
    public Date getModifyDate() {
        return new Date(modified);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
