package com.polydes.blockdefs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.polydes.common.io.XML;

import stencyl.core.lib.Game;
import stencyl.sw.SW;
import stencyl.sw.editors.game.advanced.ExtensionInstance;
import stencyl.sw.editors.snippet.designer.Definition;
import stencyl.sw.editors.snippet.designer.Definitions.DefinitionMap;
import stencyl.sw.ext.BaseExtension;
import stencyl.sw.ext.OptionsPanel;
import stencyl.sw.util.FileHelper;
import stencyl.sw.util.Locations;

public class BlockDefsExtension extends BaseExtension
{
	private static final Logger log = Logger.getLogger(BlockDefsExtension.class);
	
	//loaded but not yet applied enhancements
	private final Map<String, Map<String, DefinitionAdditions>> loadedDefinitionAdditions = new HashMap<>();
	
	/*
	 * Happens when StencylWorks launches. 
	 * 
	 * Avoid doing anything time-intensive in here, or it will
	 * slow down launch.
	 */
	@Override
	public void onStartup()
	{
		super.onStartup();
		
		isInMenu = false;
		isInGameCenter = false;
		
		SW.get().getEngineExtensionManager().getExtensionBlocks().addListener(event -> {
			
			if(Game.noGameOpened())
			{
				loadedDefinitionAdditions.clear();
				return;
			}
			
			for(ExtensionInstance inst : Game.getGame().getExtensions().values())
			{
				String extensionID = inst.getExtensionID();
				if(inst.isEnabled())
				{
					if(!loadedDefinitionAdditions.containsKey(extensionID) && SW.get().getEngineExtensionManager().getExtensionBlocks().containsKey(extensionID))
					{
						loadedDefinitionAdditions.put(extensionID, new HashMap<>());
						File extensionRoot = new File(Locations.getGameExtensionLocation(extensionID));
						File enhancements = new File(extensionRoot, "blocks-enhancements.xml");
						if(enhancements.exists())
						{
							try
							{
								Element e = FileHelper.readXMLFromFile(enhancements).getDocumentElement();
								loadEnhancements(extensionID, e);
								
								DefinitionMap loadedDefs = SW.get().getEngineExtensionManager().getExtensionBlocks().get(extensionID);
								if(!loadedDefs.isEmpty())
									applyEnhancements(extensionID, loadedDefs);
								loadedDefs.addListener(blocksChangedEvent -> {
									applyEnhancements(extensionID, loadedDefs);
								});
							}
							catch(IOException e)
							{
								log.error(e.getMessage(), e);
							}
						}
					}
				}
				else
				{
					loadedDefinitionAdditions.remove(extensionID);
				}
			}
			
			
		});
	}
	
	private void loadEnhancements(String extensionID, Element enhancements)
	{
		for(Element e : XML.children(enhancements))
		{
			if(e.getTagName().equals("block"))
			{
				String blockTag = e.getAttribute("tag");
				
				Map<Integer, String> frMap = new HashMap<>();
				
				Element fields = XML.child(e, "fields");
				if(fields != null)
				{
					for(Element field : XML.children(fields))
					{
						int order = Integer.parseInt(field.getAttribute("order"));
						frMap.put(order, field.getTagName());
					}
				}
				
				loadedDefinitionAdditions.get(extensionID).put(blockTag, new DefinitionAdditions(frMap));
			}
		}
	}
	
	private void applyEnhancements(String extensionID, DefinitionMap loadedDefs)
	{
		for(Definition def : loadedDefs.values())
		{
			DefinitionAdditions additions = loadedDefinitionAdditions.get(extensionID).remove(def.tag);
			if(additions != null)
			{
				def.codeGenerator = new EnhancedCodeMap(def, additions);
			}
		}
	}

	/*
	 * Happens when the extension is told to display.
	 * 
	 * May happen multiple times during the course of the app. 
	 * 
	 * A good way to handle this is to make your extension a singleton.
	 */
	@Override
	public void onActivate()
	{
	}
	
	/*
	 * Happens when StencylWorks closes.
	 *  
	 * Usually used to save things out.
	 */
	@Override
	public void onDestroy()
	{
	}
	
	/*
	 * Happens when a game is saved.
	 */
	@Override
	public void onGameSave(Game game)
	{
	}
	
	/*
	 * Happens when a game is opened.
	 */
	@Override
	public void onGameOpened(Game game)
	{
	}

	/*
	 * Happens when a game is closed.
	 */
	@Override
	public void onGameClosed(Game game)
	{
	}
	
	/*
	 * Happens when the user requests the Options dialog for your extension.
	 * 
	 * You need to provide the form. We wrap it in a dialog.
	 */
	@Override
	public OptionsPanel onOptions()
	{
		return new OptionsPanel()
		{
			JTextField text;
			JCheckBox check;
			JComboBox<?> dropdown;
			
			/*
			 * Construct the form.
			 * 
			 * We provide a simple way to construct forms without
			 * knowing Swing (Java's GUI library).
			 */
			@Override
			public void init()
			{
				startForm();
				addHeader("Options");
				text = addTextfield("Name:");
				check = addCheckbox("Do you like chocolate?");
				dropdown = addDropdown("Where are you from?", new String[] {"Americas", "Europe", "Asia", "Other"});
				endForm();
				
				//Set the form's values
				text.setText("" + properties.get("name"));
				check.setSelected(Boolean.parseBoolean("" + properties.get("choc")));
				dropdown.setSelectedItem(properties.get("loc"));
			}
			
			/*
			 * Use this to save the form data out.
			 * All you need to do is place the properties into preferences.
			 */
			@Override
			public void onPressedOK()
			{
				properties.put("name", text.getText());
				properties.put("choc", check.isSelected());
				properties.put("loc", dropdown.getSelectedItem());
			}

			@Override
			public void onPressedCancel()
			{
			}

			@Override
			public void onShown()
			{
			}
		};
	}
	
	/*
	 * Happens when the extension is first installed.
	 */
	@Override
	public void onInstall()
	{
	}
	
	/*
	 * Happens when the extension is uninstalled.
	 * 
	 * Clean up files.
	 */
	@Override
	public void onUninstall()
	{
	}
}