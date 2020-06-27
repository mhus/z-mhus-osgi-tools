package de.mhus.karaf.commands.impl;

import java.util.Collection;

import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.apache.felix.hc.api.ResultLog.Entry;
import org.apache.felix.hc.api.execution.HealthCheckExecutionOptions;
import org.apache.felix.hc.api.execution.HealthCheckExecutionResult;
import org.apache.felix.hc.api.execution.HealthCheckExecutor;
import org.apache.felix.hc.api.execution.HealthCheckSelector;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "felix", name = "healthcheck", description = "Healthcheck")
@Service
public class CmdHealthCheck extends AbstractCmd {

    @Option(
            name = "-or",
            aliases = {"--combine-or"},
            description = "Combine tags with or",
            required = false,
            multiValued = false)
    private boolean checkCombineTagsWithOr = false;
    @Option(
            name = "-f",
            aliases = {"--force"},
            description = "Force execution",
            required = false,
            multiValued = false)
    private boolean checkForceInstantExecution = false;
    
    @Option(
            name = "-t",
            aliases = {"--timeout"},
            description = "Override global timeout",
            required = false,
            multiValued = false)
    private String checkOverrideGlobalTimeoutStr = null;
    
    @Argument(
            index = 0,
            name = "tags",
            required = false,
            description = "Filter Tags",
            multiValued = false)
    private String checkTags = "*";

    @Override
    public Object execute2() throws Exception {
        HealthCheckExecutor healthCheckExecutor = M.l(HealthCheckExecutor.class);
        if (healthCheckExecutor == null) System.out.println("HealthCheckExecutor not found");
        
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Name", "LogLevel","Status","Message","Exception");
        if (healthCheckExecutor != null) {
            // https://github.com/apache/felix/tree/archived/healthcheck/webconsoleplugin

            HealthCheckExecutionOptions options = new HealthCheckExecutionOptions();
            options.setCombineTagsWithOr(checkCombineTagsWithOr);
            options.setForceInstantExecution(checkForceInstantExecution);
            if (MString.isSet(checkOverrideGlobalTimeoutStr))
                try {
                    options.setOverrideGlobalTimeout(Integer.valueOf(checkOverrideGlobalTimeoutStr));
                } catch (NumberFormatException nfe) {
                    // override not set in UI
                }
            HealthCheckSelector selector = MString.isSet(checkTags) ? HealthCheckSelector.tags(checkTags.split(",")) : HealthCheckSelector.empty();
            Collection<HealthCheckExecutionResult> results = healthCheckExecutor.execute(selector, options);
            for (HealthCheckExecutionResult result : results) {
                try {
                    String name = result.getHealthCheckMetadata().getName();
                    Result status = result.getHealthCheckResult();
                    for (Entry entry : status) {
                        out.addRowValues(name, entry.getLogLevel(), entry.getStatus(), entry.getMessage(),entry.getException());
                    }
                } catch (Throwable t) {
                    log().e(t);
                }
            }
        } else {
            // direct ... legacy without HealthCheckExecutor service
            for (HealthCheck check : MOsgi.getServices(HealthCheck.class, null)) {
                try {
                    String name = check.toString();
                    int pos = name.indexOf('@');
                    if (pos > 0) name = name.substring(0,pos);
                    Result status = check.execute();
                    for (Entry entry : status) {
                        out.addRowValues(name, entry.getLogLevel(), entry.getStatus(), entry.getMessage(),entry.getException());
                    }
                } catch (Throwable t) {
                    log().e(t);
                }
            }
        }
        out.print();
        
        return null;
    }

}
