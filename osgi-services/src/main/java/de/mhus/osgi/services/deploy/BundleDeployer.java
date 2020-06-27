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
package de.mhus.osgi.services.deploy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.MOsgi;

public class BundleDeployer extends MLog {

    public enum SENSIVITY {
        CHECK,
        WRITE,
        UPDATE,
        OVERWRITE,
        CLEANUP,
        RESET
    }

    public static final String DEPLOY_PROPERTIES = "_deploy.properties";

    private Bundle bundle;
    private File target;
    private SENSIVITY sensivity;
    private String bundlePath;
    private File bundleDirectory;

    public static File deploy(Bundle bundle, String bundlePath, SENSIVITY sensivity)
            throws MException {
        File target = new File(MOsgi.getTmpFolder(), "bundle");
        BundleDeployer deployer = new BundleDeployer(bundle, bundlePath, target, sensivity);
        deployer.doDeploy();
        return deployer.getBundleDirectory();
    }

    public static void delete(Bundle bundle, String bundlePath) throws MException {
        File target = new File(MOsgi.getTmpFolder(), "bundle");
        new BundleDeployer(bundle, bundlePath, target, SENSIVITY.CLEANUP).doDelete();
    }

    public BundleDeployer() {}

    public BundleDeployer(Bundle bundle, String bundlePath, File target, SENSIVITY sensivity)
            throws MException {
        setTarget(target);
        setBundlePath(bundlePath);
        setBundle(bundle);
        setSensivity(sensivity);
    }

    public void doDeploy() {
        Enumeration<String> list = bundle.getEntryPaths("/");
        if (list == null) return;
        log().d("Export Bundle", bundle.getSymbolicName());
        while (list.hasMoreElements()) {
            String path = list.nextElement();
            log().t(path);
            if (path.equals(bundlePath)) {
                doExport(bundle, bundle.getEntry(path));
                break;
            }
        }
    }

    private void doExport(Bundle bundle, URL entry) {
        log().d("Export", target, bundle.getSymbolicName());

        String path = entry.getPath();

        MProperties config = null;
        String configPath = path + DEPLOY_PROPERTIES;
        URL configEntry = bundle.getEntry(configPath);
        if (configEntry != null) {
            try {
                InputStream is = configEntry.openStream();
                config = MProperties.load(is);
                is.close();
            } catch (Throwable t) {
            }
        }
        if (config == null) config = new MProperties();

        bundleDirectory =
                new File(
                        target,
                        config.getString("name", bundle.getSymbolicName()) + "/" + bundlePath);

        if (sensivity.ordinal() <= SENSIVITY.CHECK.ordinal()
                || bundleDirectory.exists() && sensivity.ordinal() <= SENSIVITY.WRITE.ordinal())
            return;
        if (sensivity.ordinal() > SENSIVITY.OVERWRITE.ordinal()) doCleanup(bundleDirectory);

        Enumeration<String> list = bundle.getEntryPaths(path);
        while (list.hasMoreElements()) {
            String path2 = list.nextElement();
            if (!path2.endsWith("/" + DEPLOY_PROPERTIES))
                doExport(
                        bundle,
                        path2,
                        path.length() - 1,
                        bundleDirectory,
                        config,
                        sensivity.ordinal() <= SENSIVITY.UPDATE.ordinal());
        }
    }

    private void doExport(
            Bundle bundle,
            String path,
            int prefixLength,
            File root,
            MProperties config,
            boolean update) {
        log().t("File", path);
        if (path.endsWith("/")) {
            Enumeration<String> list = bundle.getEntryPaths(path);
            while (list.hasMoreElements()) {
                String path2 = list.nextElement();
                doExport(bundle, path2, prefixLength, root, config, update);
            }
        } else {

            FileDeployer deployer = findFileDeployer(MFile.getFileExtension(path));
            URL entry = bundle.getEntry(path);
            if (deployer != null) {
                deployer.doDeploy(root, path.substring(prefixLength), entry, config);
            } else {
                File f = new File(root, path.substring(prefixLength));
                //				if (f.exists() && update && ) return; // Update is not working, have no modify
                // date of the origin

                if (f.exists() && f.isDirectory())
                    MFile.deleteDir(f); // delete directory before write the file

                f.getParentFile().mkdirs();
                try {
                    InputStream is = entry.openStream();
                    FileOutputStream os = new FileOutputStream(f);
                    MFile.copyFile(is, os);
                    is.close();
                    os.close();
                } catch (Throwable t) {
                    log().w(path, f, t);
                }
            }
        }
    }

    private void doCleanup(File root) {
        MFile.deleteDir(root);
    }

    public static FileDeployer findFileDeployer(String suffix) {
        if (suffix == null) return null;
        suffix = suffix.toLowerCase();
        FileDeployer deployer = null;
        try {
            deployer = MOsgi.getService(FileDeployer.class, "(extension=" + suffix + ")");
        } catch (NotFoundException e) {
        }
        return deployer;
    }

    public void doDelete() {
        Enumeration<String> list = bundle.getEntryPaths("/CHERRY");
        if (list == null) return;
        log().d("Delete Bundle: " + bundle.getSymbolicName());
        while (list.hasMoreElements()) {
            String path = list.nextElement();
            doDelete(bundle.getEntry(path), target);
        }
    }

    private void doDelete(URL entry, File target) {
        log().d("Delete", target, bundle.getSymbolicName());

        String path = entry.getPath();

        MProperties config = null;
        String configPath = path + DEPLOY_PROPERTIES;
        URL configEntry = bundle.getEntry(configPath);
        if (configEntry != null) {
            try {
                InputStream is = configEntry.openStream();
                config = MProperties.load(is);
                is.close();
            } catch (Throwable t) {
            }
        }
        if (config == null) config = new MProperties();

        bundleDirectory =
                new File(
                        target,
                        config.getString("name", bundle.getSymbolicName()) + "/" + bundlePath);

        MFile.deleteDir(bundleDirectory);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) throws MException {
        if (!target.exists()) target.mkdirs();
        if (!target.isDirectory()) throw new MException("target must be a directory", target);
        this.target = target;
    }

    public SENSIVITY getSensivity() {
        return sensivity;
    }

    public void setSensivity(SENSIVITY sensivity) {
        this.sensivity = sensivity;
    }

    public String getBundlePath() {
        return bundlePath;
    }

    public void setBundlePath(String bundlePath) {
        this.bundlePath = MFile.normalizePath(bundlePath);
        if (this.bundlePath.startsWith("/")) this.bundlePath = this.bundlePath.substring(1);
        if (!this.bundlePath.endsWith("/")) this.bundlePath = this.bundlePath + "/";
    }

    public File getBundleDirectory() {
        return bundleDirectory;
    }
}
