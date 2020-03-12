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
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.lib.core.security.ModifyTrustApi;
import de.mhus.lib.core.security.Trust;
import de.mhus.lib.core.security.TrustSource;
import de.mhus.lib.errors.MException;

public class TrustFromFile extends MLog implements TrustSource, ModifyTrustApi {

    @Override
    public Trust findTrust(String trust) {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/trust/" + MFile.normalize(trust.trim()).toLowerCase() + ".xml");
        if (!file.exists() || !file.isFile()) return null;

        try {
            return new TrustFile(file, trust);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log().w(trust, e);
            return null;
        }
    }
    /*
    <trust>
      <password plain="123" />
      <information name="Default" />
      <attributes>
        <attribute name="privatekey" value=""/>
      </attributes>
    </trust>
     */
    @Override
    public void createTrust(String trust, String password, IReadProperties properties)
            throws MException {

        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/trust/" + MFile.normalize(trust.trim()).toLowerCase() + ".xml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (file.exists()) throw new MException("Trust already exists", trust);

        try {
            Document doc = MXml.createDocument();
            Element rootE = doc.createElement("user");
            doc.appendChild(rootE);
            rootE.setAttribute("created", MDate.toIsoDateTime(System.currentTimeMillis()));
            Element passE = doc.createElement("password");
            rootE.appendChild(passE);
            passE.setAttribute("plain", MPassword.encode(password));
            Element infoE = doc.createElement("information");
            rootE.appendChild(infoE);
            infoE.setAttribute("name", properties.getString(AccessApi.DISPLAY_NAME, trust));
            Element attrE = doc.createElement("attributes");
            rootE.appendChild(attrE);

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
    public void deleteTrust(String trust) throws MException {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/trust/" + MFile.normalize(trust.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Trust not found", trust);

        file.delete();
    }

    @Override
    public void changePassword(String trust, String newPassword) throws MException {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/trust/" + MFile.normalize(trust.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Trust not found", trust);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));
            Element passE = MXml.getElementByPath(rootE, "password");
            passE.setAttribute("plain", MPassword.encode(newPassword));

            FileWriter os = new FileWriter(file);
            MXml.saveXml(rootE, os, true);
            os.close();

        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public void changeTrust(String trust, IReadProperties properties) throws MException {
        File file =
                MApi.getFile(MApi.SCOPE.DATA, 
                        "aaa/trust/" + MFile.normalize(trust.trim()).toLowerCase() + ".xml");
        if (!file.exists()) throw new MException("Trust not found", trust);

        try {
            Document doc = MXml.loadXml(file);

            Element rootE = doc.getDocumentElement();
            rootE.setAttribute("modified", MDate.toIsoDateTime(System.currentTimeMillis()));

            Element infoE = MXml.getElementByPath(rootE, "information");
            rootE.appendChild(infoE);
            infoE.setAttribute("name", properties.getString(AccessApi.DISPLAY_NAME, trust));

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
    public Trust getTrust(String trust) {
        return findTrust(trust);
    }

    @Override
    public ModifyTrustApi getModifyApi() {
        return this;
    }
}
