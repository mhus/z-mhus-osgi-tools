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
package de.mhus.karaf.commands.mhus;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.console.ANSIConsole;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.Console.COLOR;
import de.mhus.lib.core.io.TailInputStream;
import de.mhus.lib.core.logging.LevelMapper;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.logging.TrailLevelMapper;
import de.mhus.lib.core.system.IApi;
import de.mhus.lib.logging.level.GeneralMapper;
import de.mhus.lib.logging.level.ThreadBasedMapper;
import de.mhus.lib.logging.level.ThreadMapperConfig;
import de.mhus.lib.mutable.KarafMApiImpl;

@Command(scope = "mhus", name = "log", description = "Manipulate Log behavior.")
@Service
public class CmdLog extends MLog implements Action {

    @Reference
    private Session session;

	@Argument(index=0, name="cmd", required=true, description="Command:\n"
			+ " clear - reset all loggers,\n"
			+ " add <path> - add a trace log,\n"
			+ " full - enable full trace logging,\n"
			+ " dirty - enable dirty logging,\n"
			+ " level - set log level (console logger),\n"
			+ " reloadconfig,\n"
			+ " settrail [<config>] - enable trail logging for this thread,\n"
			+ " istrail - output the traillog config,\n"
			+ " releasetrail - unset the current trail log config\n"
			+ " resetalltrail - unset all trail log configs\n"
			+ " general - enable general logging\n"
			+ " off - log mapping off\n"
			+ " trace,debug,info,warn,error,fatal <msg>\nconsole [console=ansi] [file=data/log/karaf.log] [color=true]\n"
			+ " maxmsgsize [new size] - show or set maximum message size, disable with 0\n"
			+ " stacktracetrace [true|false]", multiValued=false)
    String cmd;

	@Argument(index=1, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-m", aliases="--max", description="Maximum log-block size gap before skip",required=false)
	protected int maxDelta = -1; // Max output per block!

	// private Appender appender;

	@Override
	public Object execute() throws Exception {

		IApi s = MApi.get();
		if (! (s instanceof KarafMApiImpl)) {
			System.out.println("Karaf MApi not set");
			return null;
		}
		KarafMApiImpl api = (KarafMApiImpl)s;
		
		switch (cmd) {
		case "stacktracetrace": {
			Log.setStacktraceTrace(MCast.toboolean(parameters[0], false));
			System.out.println("OK");
		} break;
		case "clear": {
			api.clearTrace();
			api.setFullTrace(false);
			MApi.updateLoggers();
			System.out.println("OK");
		} break;
		case "full": {
			api.setFullTrace(MCast.toboolean(parameters.length >= 1 ? parameters[0] : "1", false));
			MApi.updateLoggers();
			System.out.println("OK");
		} break;
		case "dirty": {
			MApi.setDirtyTrace(MCast.toboolean(parameters.length >= 1 ? parameters[0] : "1", false));
			System.out.println("OK");
		} break;
		case "add": {
			for (String p : parameters)
				api.setTrace(p);
			MApi.updateLoggers();
			System.out.println("OK");
		} break;
		case "list": {
			System.out.println("Default Level  : " + api.getLogFactory().getDefaultLevel());
			System.out.println("Trace          : " + api.isFullTrace());
			System.out.println("LogFoctory     : " + api.getLogFactory().getClass().getSimpleName());
			System.out.println("DirtyTrace     : " + MApi.isDirtyTrace());
			LevelMapper lm = api.getLogFactory().getLevelMapper();
			if (lm != null) {
			System.out.println("LevelMapper    : " + lm.getClass().getSimpleName());
			if (lm instanceof TrailLevelMapper)
			System.out.println("   Configurtion: " + ((TrailLevelMapper)lm).doSerializeTrail() );
			}
			if (api.getLogFactory().getParameterMapper() != null)
			System.out.println("ParameterMapper: " + api.getLogFactory().getParameterMapper().getClass().getSimpleName());
			
			for (String name : api.getTraceNames())
				System.out.println(name);
		} break;
		case "reloadconfig": { //TODO need single command class
			api.getCfgManager().reConfigure();
			MApi.updateLoggers();
			System.out.println("OK");
		} break;
		case "level": {
			api.getLogFactory().setDefaultLevel(Log.LEVEL.valueOf(parameters[0].toUpperCase()));
			MApi.updateLoggers();
			System.out.println("OK");
		} break;
		case "settrail": {
			LevelMapper mapper = api.getLogFactory().getLevelMapper();
			if (MLogUtil.isTrailLevelMapper()) {
				MLogUtil.setTrailConfig(MLogUtil.TRAIL_SOURCE_SHELL,parameters == null || parameters.length < 1 ? "" : parameters[0]);
				System.out.println("Trail Config: " + MLogUtil.getTrailConfig() );
			} else {
				System.out.println("Wrong Mapper " + mapper);
			}
		} break;
		case "istrail": {
			LevelMapper mapper = api.getLogFactory().getLevelMapper();
			if (MLogUtil.isTrailLevelMapper()) {
				System.out.println("LevelMapper: " + MLogUtil.getTrailConfig());
			} else {
				System.out.println("Wrong Mapper " + mapper);
			}
		} break;
		case "releasetrail": {
			LevelMapper mapper = api.getLogFactory().getLevelMapper();
			if (MLogUtil.isTrailLevelMapper()) {
				MLogUtil.releaseTrailConfig();
				System.out.println("OK");
			} else {
				System.out.println("Wrong Mapper " + mapper);
			}
		} break;
		case "resetalltrail": {
			LevelMapper mapper = api.getLogFactory().getLevelMapper();
			if (MLogUtil.isTrailLevelMapper()) {
				MLogUtil.resetAllTrailConfigs();
				System.out.println("OK");
			} else {
				System.out.println("Wrong Mapper " + mapper);
			}
		} break;
		case "maxmsgsize": {
			if (parameters != null && parameters.length > 0)
				api.getLogFactory().setMaxMessageSize(MCast.toint(parameters[0], 0));
			else
				System.out.println("Max Message Size: "  + api.getLogFactory().getMaxMessageSize() );
		} break;
		case "general": {
			ThreadMapperConfig config = new ThreadMapperConfig();
			config.doConfigure(MLogUtil.TRAIL_SOURCE_SHELL, parameters == null || parameters.length < 1 ? "" : parameters[0]);
			GeneralMapper mapper = new GeneralMapper();
			mapper.setConfig(config);
			api.getLogFactory().setLevelMapper(mapper);
			System.out.println("Sel Global Mapper: OK " + api.getLogFactory().getLevelMapper() + " " + config.getTrailId());
			log().d("Set general mapper");
		} break;
		case "trail": {
			api.getLogFactory().setLevelMapper(new ThreadBasedMapper());
			System.out.println("Set Trail Mapper OK " + api.getLogFactory().getLevelMapper() );
			log().d("Set trail mapper");
		} break;
		case "off": {
			api.getLogFactory().setLevelMapper(null);
			System.out.println("Remove Mapper OK " + api.getLogFactory().getLevelMapper() );
			log().d("Mapper off");
		} break;
		case "trace": {
			log().t((Object[])parameters);
		} break;
		case "debug": {
			log().d((Object[])parameters);
		} break;
		case "info": {
			log().i((Object[])parameters);
		} break;
		case "warn": {
			log().w((Object[])parameters);
		} break;
		case "error": {
			log().e((Object[])parameters);
		} break;
		case "fatal": {
			log().f((Object[])parameters);
		} break;
		case "console": {
			final MProperties pe = new MProperties(parameters);
			MThread a = (MThread) session.get("__log_tail");
			if (a != null) {
				// a.throwException(new EOFException());
				a.interupt();
				session.put("__log_tail", null);
			} else {
				PrintStream sConsole = session.getConsole();
				if (sConsole == null || session.getTerminal() == null) return null;
				@SuppressWarnings("resource")
				final Console os = (Console) (pe.getString("console","ansi").equals("ansi") ?
						new ANSIConsole(System.in, sConsole) :
						new de.mhus.lib.core.console.SimpleConsole(System.in, sConsole));
				
				final Session finalSession = session;
				
				final File file = new File( pe.getString("file","data/log/karaf.log") );
				final boolean color = pe.getBoolean("color", true);
				MThread appender = new MThread(new Runnable() {
					
					@Override
					public void run() {
						try {
							os.println("Log Listen");
							os.flush();
							TailInputStream tail = new TailInputStream(file);
							StringBuilder buf = new StringBuilder();
							boolean niceMode = false;
							Field runningField = finalSession.getClass().getDeclaredField("running");
							if (!runningField.canAccess(finalSession)) runningField.setAccessible(true);
							try {
								while (true) {
									if (maxDelta > 0 && tail.available() > maxDelta  ) {
										MThread.sleep(200);
										os.cleanup();
										os.println("--- Skip Log ---");
										os.flush();
										tail.clean();
									}
									int i = tail.read();
									if(Thread.currentThread().isInterrupted()) break;
									boolean running = (boolean)runningField.get(finalSession);
									if (!running) break;
									if (i >=0 ) {
										char c = (char)i; 
										os.print(c);
										if (c == '\n') {
											if (niceMode)
												os.cleanup();
											niceMode = false;
											buf.setLength(0);
										} else {
											if (!niceMode && buf.length() < 40) {
												buf.append(c);
												if (color && c == '|') {
													String m = buf.toString();
													if (m.endsWith("| INFO  |")) {
														os.setColor(COLOR.GREEN , COLOR.UNKNOWN);
														niceMode = true;
													} else
													if (m.endsWith("| ERROR |")) {
														os.setBold(true);
														os.setColor(COLOR.RED , COLOR.UNKNOWN);
														niceMode = true;
													} else
													if (m.endsWith("| DEBUG |")) {
														os.setColor(COLOR.YELLOW , COLOR.UNKNOWN);
														niceMode = true;
													} else
													if (m.endsWith("| WARN  |")) {
														os.setColor(COLOR.RED , COLOR.UNKNOWN);
														niceMode = true;
													} else
													if (m.endsWith("| FATAL |")) {
														os.setBlink(true);
														os.setColor(COLOR.RED , COLOR.UNKNOWN);
														niceMode = true;
													}
												}
											}
										}
										os.flush();
									}
								}
							} catch (Throwable t) {
								log().d(t);
							}
							log().i("Session Log Closed");
							os.println("Log Closed");
							tail.close();
						} catch (Throwable t) {
							log().d(t);
						}
					}
				});
				appender.start();
				
				session.put("__log_tail", appender);
			}
			
//			if (appender != null) {
//				Logger.getRootLogger().removeAppender(appender );
//				appender = null;
//				System.out.println("Off");
//			} else {
//				appender = new Appender() {
//					
//					private String name;
//					private Layout layout;
//					private ErrorHandler eh;
//					private LinkedList<Filter> filters = new LinkedList<>();
//	
//					@Override
//					public void setName(String name) {
//						this.name = name;
//					}
//					
//					@Override
//					public void setLayout(Layout layout) {
//						this.layout = layout;
//					}
//					
//					@Override
//					public void setErrorHandler(ErrorHandler errorHandler) {
//						this.eh = errorHandler;
//					}
//					
//					@Override
//					public boolean requiresLayout() {
//						return false;
//					}
//					
//					@Override
//					public String getName() {
//						return name;
//					}
//					
//					@Override
//					public Layout getLayout() {
//						return layout;
//					}
//					
//					@Override
//					public Filter getFilter() {
//						return filters.getLast();
//					}
//					
//					@Override
//					public ErrorHandler getErrorHandler() {
//						return eh;
//					}
//					
//					@Override
//					public void doAppend(LoggingEvent event) {
//						Level level = event.getLevel();
//						switch (level.toInt()) {
//						case Level.INFO_INT:
//							os.setColor(COLOR.GREEN , COLOR.UNKNOWN);
//							break;
//						case Level.DEBUG_INT:
//							os.setColor(COLOR.YELLOW , COLOR.UNKNOWN);
//							break;
//						case Level.WARN_INT:
//							os.setColor(COLOR.RED , COLOR.UNKNOWN);
//							break;
//						case Level.ERROR_INT:
//							os.setBold(true);
//							os.setColor(COLOR.RED , COLOR.UNKNOWN);
//							break;
//						case Level.FATAL_INT:
//							os.setBlink(true);
//							os.setColor(COLOR.RED , COLOR.UNKNOWN);
//							break;
//						case Level.TRACE_INT:
//							os.setColor(COLOR.WHITE , COLOR.UNKNOWN);
//							break;
//						default:
//							break;
//						}
//						os.println(level.toString() + ": " + event.getLoggerName() + " " + event.getMessage() );
//						os.cleanup();
//					}
//					
//					@Override
//					public void close() {
//						
//					}
//					
//					@Override
//					public void clearFilters() {
//						filters.clear();
//					}
//					
//					@Override
//					public void addFilter(Filter newFilter) {
//						filters.add(newFilter);
//					}
//				};
//				Logger.getRootLogger().addAppender(appender );
//				System.out.println("On");
//			}
			
		} break;
		default:
			System.out.println("Unknown cmd");
		}
		
		
		return null;
	}

}
