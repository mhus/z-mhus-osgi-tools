package de.mhus.osgi.vaadin_groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.lang.reflect.Method;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

import de.mhus.lib.core.logging.Log;

public class ConsoleView extends VerticalLayout implements View {

	private TextArea output;
	private TextArea inputLine;
	private CheckBox cbCleanLine;
	private CheckBox cbCleanOutput;
	private CheckBox cbBindingsUpdate;
	private StringBuffer text;
	private Log log = Log.getLog(GroovyConsoleUI.class);
	private Binding binding;
	private GroovyShell shell;
	private Table bindings;
	private BindingsDataSource bindingsDataSource;
	private TextArea bindingInfo;

	@Override
	public void enter(ViewChangeEvent event) {
		
		
		
	}
	
	public void initUi() {
		
		
		setMargin(false);
		setSizeFull();
//        mainLayout.setWidth("100%");
		
		HorizontalLayout topLine = new HorizontalLayout();
		addComponent(topLine);
		setExpandRatio(topLine, 0);
		
		
		HorizontalSplitPanel mainLR = new HorizontalSplitPanel();
		//mainLR.setMargin(false);
		mainLR.setSplitPosition(400, Sizeable.UNITS_PIXELS);
        mainLR.setSizeFull();
		addComponent(mainLR);
		setExpandRatio(mainLR, 1);
		
		
		VerticalSplitPanel vert = new VerticalSplitPanel();
		// vert.setMargin(false);
		mainLR.addComponent(vert);
		
        vert.setWidth("100%");
        vert.setSplitPosition(400, Sizeable.UNITS_PIXELS);
        
        output = new TextArea();
        output.setHeight("100%");
        output.setWidth("100%");
        
        vert.addComponent(output);
        
        inputLine = new TextArea();
        inputLine.setHeight("100%");
        inputLine.setWidth("100%");
//		inputLine.setValue("return ");

        HorizontalLayout buttonBar = new HorizontalLayout();
        
        Button button = new Button("Execute (Alt+Enter)");
        button.addListener(new Button.ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doExecute();
			}
		});
//        TextArea inputArea = new TextArea();
        
        
        inputLine.addShortcutListener(new ShortcutListener("ALT+ENTER",KeyCode.ENTER, new int[] {ShortcutAction.ModifierKey.ALT}) {
			
			@Override
			public void handleAction(Object sender, Object target) {
				doExecute();
			}
		});
                
        buttonBar.addComponent(button);
        
        button = new Button("Delete Binding");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doDeleteBinding();
			}
		});
        buttonBar.addComponent(button);

        button = new Button("Refresh Binding");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doRefreshBinding();
			}
		});
        buttonBar.addComponent(button);
        
        button = new Button("Load Script");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doLoadScript();
			}
		});
        buttonBar.addComponent(button);
        
        button = new Button("Save Script");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doSaveScript();
			}
		});
        buttonBar.addComponent(button);

        button = new Button("Destroy Application");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				getUI().close();
			}
		});
        buttonBar.addComponent(button);

//        output.setValue("");
//        output.setReadOnly(true);
        
        addComponent(buttonBar);
		setExpandRatio(buttonBar, 0);
		
        HorizontalLayout configBar = new HorizontalLayout();
        
        cbCleanLine = new CheckBox("Clean Line");
        cbCleanLine.setValue(false);
        configBar.addComponent(cbCleanLine);
        
        cbCleanOutput = new CheckBox("Clean Output");
        cbCleanOutput.setValue(true);
        configBar.addComponent(cbCleanOutput);

        cbBindingsUpdate = new CheckBox("Bindings Update");
        cbBindingsUpdate.setValue(true);
        configBar.addComponent(cbBindingsUpdate);
        
        addComponent(configBar);
		setExpandRatio(configBar, 0);
        
        vert.addComponent(inputLine);


//        engine.put("vaadin", topLine);

        
        bindings = new Table("Bindings");
        bindings.setWidth("100%");
        bindings.setHeight("100%");
        
        bindingsDataSource = new BindingsDataSource();
        bindings.setContainerDataSource( bindingsDataSource );
        bindings.setColumnReorderingAllowed(true);
        bindings.setColumnCollapsingAllowed(true);
        bindings.setMultiSelect(false);
        bindings.setSelectable(true);
        bindings.setImmediate(true);
        bindings.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				doShowBindingInfos();
			}
        	
        });
        
        bindings.setColumnHeaders(new String[] { "Name", "Type", "Value" });

        VerticalSplitPanel rightSplit = new VerticalSplitPanel();
        rightSplit.addComponent(bindings);
        rightSplit.setSizeFull();
        rightSplit.setSplitPosition(150, Sizeable.UNITS_PIXELS);
        
        bindingInfo = new TextArea();
        bindingInfo.setWidth("100%");
        bindingInfo.setHeight("100%");
        
        rightSplit.addComponent(bindingInfo);
        
        mainLR.addComponent(rightSplit);
        
		CompilerConfiguration config = new CompilerConfiguration();
		//config.setDisabledGlobalASTTransformations(new HashSet<String>(Arrays.asList(new String[] { "org.codehaus.groovy.transform.BaseScriptASTTransformation" })) );
		config.setScriptBaseClass(Printer.class.getCanonicalName());
        binding = new Binding();
        binding.setVariable("vaadin", topLine);
		binding.setVariable("context", ((GroovyConsoleUI)getUI()).getContext() );

        shell = new GroovyShell(GroovyShell.class.getClassLoader(), binding, config);
        
        doRefreshBinding();
	}

	protected void doShowBindingInfos() {
		bindingInfo.setValue("");
		try {
			Item item = bindings.getItem(bindings.getValue());
			String name = (String)item.getItemProperty("Name").getValue();
			Object obj = binding.getVariable(name);
			StringBuffer out = new StringBuffer();
			for (Method m : obj.getClass().getMethods()) {
					out.append(m.getReturnType().getSimpleName()).append(" ");
					out.append(m.getName()).append("(");
					int nr = 0;
					for (Class<?> pt : m.getParameterTypes()) {
						if (nr != 0) out.append(", ");
						out.append(pt.getSimpleName()).append(" nr" + nr);
						nr++;
					}
					out.append(")\n");
			}
			bindingInfo.setValue(out.toString());
		} catch (Throwable t) {
			bindingInfo.setValue(t.toString());
			log.error("",t);
		}
	}

	protected void doSaveScript() {
		
	}

	protected void doLoadScript() {
		// TODO Auto-generated method stub
		
	}

	protected void doRefreshBinding() {
		bindingsDataSource.removeAllItems();
		bindingsDataSource.update();
	}

	protected void doDeleteBinding() {
		try {
			Item item = bindings.getItem(bindings.getValue());
			String name = (String)item.getItemProperty("Name").getValue();
//			Object obj = engine.get(name);
			binding.setVariable(name, null);
			bindingsDataSource.removeItem(name);
			bindingInfo.setValue("");
		} catch (Throwable t) {
			bindingInfo.setValue(t.toString());
			log.error("",t);
		}
	}
	
	protected void doExecute() {
		if (((Boolean)cbCleanOutput.getValue()).booleanValue()) output.setValue("");
		String cmd = (String)inputLine.getValue();
		text = new StringBuffer();
		text.append( (String)output.getValue() );
		text.append( "> " + cmd + "\n");
		try {
			binding.setVariable("text", text);
			Object ret = shell.evaluate(cmd);
			text.append( "< " + ret + "\n" );
			if (ret != null) getUI().showNotification(ret.toString());
		} catch (Throwable e) {
			log.error("",e);
			getUI().showNotification(e.toString(),Notification.TYPE_ERROR_MESSAGE );
			text.append(e.toString() + "\n");
		}
		output.setValue( text.toString() );
		text = null;
		if (((Boolean)cbCleanLine.getValue()).booleanValue()) inputLine.setValue("");
		
		if (((Boolean)cbBindingsUpdate.getValue()).booleanValue()) doRefreshBinding();
		
	}

	private class BindingsDataSource extends IndexedContainer {
		
		private static final long serialVersionUID = 1L;

		BindingsDataSource() {
			
			addContainerProperty("Name", String.class,null);
			addContainerProperty("Type", String.class,null);
			addContainerProperty("Value", String.class,null);
			
			update();
		}
		
		void update() {
			// removeAllItems(); // TODO merge
			if (binding == null) return;
			for ( Object key : binding.getVariables().keySet() ) {
				
				Item item = addItem(key);
				item.getItemProperty("Name").setValue(key);
				item.getItemProperty("Type").setValue(binding.getVariable(key.toString()).getClass().getCanonicalName());
				item.getItemProperty("Value").setValue(shorten(binding.getVariable(key.toString())));
				
			}
			
		}

		private String shorten(Object value) {
			if (value == null) return "[null]";
			String ret = value.toString();
			if (ret.length() > 100 ) return ret.substring(0,100) + " ...";
			return ret;
		}
		
	}


	public static abstract class Printer extends Script {

		public void print(Object obj) {
			((StringBuffer)getBinding().getVariable("text")).append(obj);
		}
		public void println(Object obj) {
			((StringBuffer)getBinding().getVariable("text")).append(obj).append("\n");
		}
	}

}
