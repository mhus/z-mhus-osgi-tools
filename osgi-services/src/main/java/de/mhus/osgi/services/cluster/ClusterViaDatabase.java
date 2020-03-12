package de.mhus.osgi.services.cluster;

import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.base.service.ClusterApi;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.config.XmlConfigFile;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.lib.sql.DataSourceProvider;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbPool;
import de.mhus.lib.sql.DbResult;
import de.mhus.lib.sql.DbStatement;
import de.mhus.lib.sql.DefaultDbPool;
import de.mhus.lib.sql.Dialect;
import de.mhus.osgi.api.services.MOsgi;

public class ClusterViaDatabase extends MLog implements ClusterApi {

    private static final CfgInt CFG_INIT_RETRY_SEC =
            new CfgInt(ClusterViaDatabase.class, "initRetrySec", 30);

    private String dsName;
    private DataSource ds;
    private DbPool pool;
    private String prefix;
    private String table;
    private String key;
    private SoftHashMap<String, Lock> cache = new SoftHashMap<>();

    private boolean startInit;

    public ClusterViaDatabase(String dsName, String prefix) {
        this.dsName = dsName;
        this.prefix = prefix;
        this.table = prefix + "_lock_";
        this.key = "id_";
    }

    @Override
    public Lock getLock(String name) {
        init();
        synchronized (cache) {
            return cache.getOrCreate(name, (k) -> new DbLock(k));
        }
    }

    @Override
    public boolean isReady() {
        if (!startInit)
            init();
        if (ds == null) return false;
        return true; // TODO check data source status !!!!
    }

    private synchronized void init() {
        startInit = true;
        if (ds != null) return; // TODO timeout
        while (true) {
            try {
                ds = MOsgi.getDataSource(dsName);
                if (ds == null) {
                    log().w("Datasource not found",dsName);
                } else {
                    // init pool object
                    DataSourceProvider dsProvider = new DataSourceProvider();
                    String driver = ds.getConnection().getMetaData().getDriverName();
                    Dialect dialect = Dialect.findDialect(driver);
                    dsProvider.setDataSource(ds);
                    dsProvider.setDialect(dialect);
                    pool = new DefaultDbPool(dsProvider);
                    // init tables
                    URL url = MSystem.locateResource(this, "SqlDbStorage.xml");
                    DbConnection con = pool.getConnection();
                    XmlConfigFile data = new XmlConfigFile(url.openStream());
                    data.setString("prefix", prefix);
                    pool.getDialect().createStructure(data, con, null, false);
                    con.close();
                    return;
                }
            } catch (Exception e) {
                log().e(e);
            }
            log().i("Retry init of DB in " + CFG_INIT_RETRY_SEC.value() + " sec");
            MThread.sleep(CFG_INIT_RETRY_SEC.value() * 1000);
        }
    }

    private Con tryLock(String value) {
        log().t("Try Lock", value);
        DbConnection con = null;
        try {
            con = pool.getConnection();
            DbStatement sth = con.createStatement("SELECT "+key+" FROM " + table + " WHERE " + key + "=$key$ FOR UPDATE NOWAIT");
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("key", value);
            try {
                DbResult res = sth.executeQuery(attributes);
                if (res.next()) {
                    res.close();
                    log().t("=== Lock1", value);
                    return new Con(con,sth);
                }
                res.close();
            } catch (SQLException e) {
                if (!e.getMessage().contains("timeout"))
                    throw e;
                sth.close();
                con.close();
                log().t("--- No0", value, e);
                return null;
            }
            DbStatement sthSet = con.createStatement("INSERT INTO " + table + "(" + key+") VALUES ($key$)");
            boolean done = false;
            try {
                sthSet.execute(attributes);
                done = true;
            } catch (SQLIntegrityConstraintViolationException e) {
            }
            if (!done) {
                con.close();
                log().t("--- No1", value);
                return null;
            }
            sthSet.close();
            con.commit();
            
            try {
                DbResult res = sth.executeQuery(attributes);
                if (res.next()) {
                    res.close();
                    log().t("=== Lock2", value);
                    return new Con(con,sth);
                }
                res.close();
            } catch (SQLException e) {
                if (!e.getMessage().contains("timeout"))
                    throw e;
                sth.close();
                con.commit();
                con.close();
                log().t("--- No2", value, e);
                return null;
            }
            sth.close();
            con.commit();
            con.close();
            log().t("--- No3", value);
            return null;
        } catch (Exception e) {
            log().d(e);
            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception e1) {
                    log().e(e1);
                }
                con.close();
            }
            log().t("--- No3", value);
            return null;
        }

    }
    
    private class Con {
        DbConnection con;
        DbStatement sth;
        
        public Con(DbConnection con, DbStatement sth) {
            this.con = con;
            this.sth = sth;
        }

        public void close() {
            if (sth == null) return;
            sth.close();
            try {
                con.commit();
            } catch (Exception e) {
                log().e(e);
            }
            con.close();
            sth = null;
            con = null;
        }
    }
    
    private class DbLock implements Lock {

        private Con con;
        private String name;
        private long lockStart;
        private int lockCnt;
        private String lockOwner;
        private String lockStacktrace;

        public DbLock(String name) {
            this.name = name;
        }

        @Override
        public Lock lock() {
            while (true) {
                Con con = tryLock(name);
                if (con != null) {
                    this.con = con;
                    lockStart = System.currentTimeMillis();
                    lockCnt++;
                    lockOwner = MSystem.findCalling(3) + " " + Thread.currentThread().getId();
                    lockStacktrace = MCast.toString("", Thread.currentThread().getStackTrace());
                    return this;
                }
                MThread.sleep(200);
            }
        }

        @Override
        public boolean lock(long timeout) {
            long start = System.currentTimeMillis();
            while (true) {
                Con con = tryLock(name);
                if (con != null) {
                    this.con = con;
                    lockStart = System.currentTimeMillis();
                    lockCnt++;
                    lockOwner = MSystem.findCalling(3) + " " + Thread.currentThread().getId();
                    lockStacktrace = MCast.toString("", Thread.currentThread().getStackTrace());
                    return true;
                }
                if (MPeriod.isTimeOut(start, timeout))
                    return false;
                MThread.sleep(200);
            }
        }

        @Override
        public synchronized boolean unlock() {
            if (con == null) return true;
            lockOwner = null;
            lockStacktrace = null;
            lockStart = 0;
            con.close();
            return true;
        }

        @Override
        public void unlockHard() {
            unlock();
        }

        @Override
        public boolean isLocked() {
            return con != null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOwner() {
            return lockOwner;
        }

        @Override
        public long getLockTime() {
            return lockStart;
        }

        @Override
        public boolean refresh() {
            return true;
        }

        @Override
        public long getCnt() {
            return lockCnt;
        }

        @Override
        public String getStartStackTrace() {
            return lockStacktrace;
        }
        
    }
}
