package de.mhus.osgi.commands.impl;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.osgi.framework.BundleContext;

import de.mhus.lib.karaf.TemplateUtils;
import de.mhus.osgi.commands.db.TraceDataSource;

@Command(scope = "jdbc", name = "createdbtrace", description = "Create DB Trace")
public class CmdCreateDbTrace implements Action {

    @Option(name = "-o", aliases = { "--online" }, description = "Create the datasource online and not a blueprint", required = false, multiValued = false)
    boolean online;

	@Argument(index=0, name="source", required=true, description="Source Datasource", multiValued=false)
    String source;

	@Argument(index=1, name="target", required=true, description="New Target Datasource", multiValued=false)
    String target;

	private BundleContext context;

	private DataSourceUtil util;

	public Object execute(CommandSession session) throws Exception {

		if (online) {
	        
	        TraceDataSource dataSource = new TraceDataSource();
	        dataSource.setSource(source);
	        dataSource.setContext(context);
	        
	        util.registerDataSource(dataSource, target);
	        
		} else {
			
	        File karafBase = new File(System.getProperty("karaf.base"));
	        File deployFolder = new File(karafBase, "deploy");
	        File outFile = new File(deployFolder, "datasource-trace_" + target + ".xml");

	        HashMap<String, String> properties = new HashMap<String, String>();
	        properties.put("name", target);
	        properties.put("source", source);
	        String templateFile = "datasource-trace.xml";
            InputStream is = this.getClass().getResourceAsStream(templateFile);
            if (is == null) {
                throw new IllegalArgumentException("Template resource " + templateFile + " doesn't exist");
            }
            TemplateUtils.createFromTemplate(outFile, is, properties);
			
		}
				
		return null;
	}

	public void setContext(BundleContext context) {
        this.context = context;
        this.util = new DataSourceUtil(context);
    }

}
