/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.jst.web.ui.palette.internal.html.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.palette.PaletteRoot;
import org.jboss.tools.jst.web.kb.taglib.IHTMLLibraryVersion;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.PagePaletteContents;
import org.jboss.tools.jst.web.ui.palette.html.jquery.wizard.JQueryConstants;
import org.jboss.tools.jst.web.ui.palette.internal.html.IPaletteGroup;
import org.jboss.tools.jst.web.ui.palette.model.IPaletteModel;
import org.jboss.tools.jst.web.ui.palette.model.PaletteModel;
/**
 * html palette model implementation
 * 
 * @see IPaletteModel
 * 
 * @author Daniel Azarov
 *
 */
public class PaletteModelImpl implements IPaletteModel{
	private static String POINT_ID = "org.jboss.tools.jst.web.ui.PaletteGroup"; //$NON-NLS-1$
	
	private HashMap<String, IPaletteGroup> paletteGroupMap = null;
	private ArrayList<IPaletteGroup> sortedPaletteGroups = null;
	
	private PaletteRoot paletteRoot = null;
	
	private PagePaletteContents paletteContents;
	
	public PaletteModelImpl(){
	}
	
	public PaletteRoot getPaletteRoot(){
		if(paletteRoot == null){
			load();
		}
		
		return paletteRoot;
	}
	
	@Override
	public void load() {
		sortedPaletteGroups = loadPaletteGroups();
		paletteGroupMap = new HashMap<String, IPaletteGroup>();
		
		String expandedCategory = getPreferredExpandedCategory();
		
		paletteRoot = new PaletteRootImpl(this);
		for(IPaletteGroup paletteGroup : sortedPaletteGroups){
			paletteGroupMap.put(paletteGroup.getName(), paletteGroup);
			paletteGroup.setPaletteModel(this);
			IHTMLLibraryVersion version = getSelectedVersion(paletteGroup);
			paletteGroup.setSelectedVersion(version);
			PaletteDrawerImpl drawer = new PaletteDrawerImpl(paletteGroup);
			if(expandedCategory != null && expandedCategory.equals(paletteGroup.getName())){
				drawer.setInitialState(PaletteDrawerImpl.INITIAL_STATE_OPEN);
			}else{
				drawer.setInitialState(PaletteDrawerImpl.INITIAL_STATE_CLOSED);
			}
			paletteRoot.add(drawer);
		}
	}
	
	public static ArrayList<IPaletteGroup> loadPaletteGroups() {
		TreeMap<String,IPaletteGroup>groupsByOrderId = new TreeMap<String,IPaletteGroup>();
		
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(POINT_ID);
		for (IConfigurationElement element : point.getConfigurationElements()) {
			IPaletteGroup paletteGroup = createPaletteGroupInstance(element);
			if(paletteGroup != null && paletteGroup.isEnabled()){
				
				String orderId = element.getAttribute("orderId");
				if(orderId == null){
					orderId = paletteGroup.getName();
				}
				groupsByOrderId.put(orderId, paletteGroup);

			}
		}
		return new ArrayList<IPaletteGroup>(groupsByOrderId.values());
	}
	
	public String[] getPaletteGroups(){
		if(sortedPaletteGroups == null){
			load();
		}
		ArrayList<String> groupNames = new ArrayList<String>();
		for(IPaletteGroup group : sortedPaletteGroups){
			groupNames.add(group.getName());
		}
		return groupNames.toArray(new String[]{});
	}
	
	public IPaletteGroup getPaletteGroup(String name){
		if(paletteGroupMap == null){
			load();
		}
		return paletteGroupMap.get(name);
	}
	
	private IHTMLLibraryVersion getSelectedVersion(IPaletteGroup paletteGroup) {
		IHTMLLibraryVersion version = null;
		if(paletteContents != null){
			version = paletteContents.getVersion(paletteGroup.getName());
		}
		if(version == null){
			version = paletteGroup.getLastVersionGroup().getVersion();
		}
		return version;
	}
	
	private static IPaletteGroup createPaletteGroupInstance(IConfigurationElement element) {
		try {
			return (IPaletteGroup)element.createExecutableExtension("class"); //$NON-NLS-1$
		} catch(CoreException e) {
			WebUiPlugin.getDefault().logError(e);
			return null;
		}
	}

	@Override
	public String getType() {
		return IPaletteModel.TYPE_HTML5;
	}
	
	@Override
	public void setPaletteContents(PagePaletteContents paletteContents) {
		this.paletteContents = paletteContents;
	}
	
	@Override
	public PagePaletteContents getPaletteContents() {
		return paletteContents;
	}
	
	public void onCategoryExpandChange(String name, boolean state) {
		if(paletteContents != null) {
			IFile f = paletteContents.getFile();
			if(state) {
				try {
					f.setPersistentProperty(PaletteModel.HTML5_EXPANDED_CATEGORY_NAME, name);
				} catch (CoreException e) {
					WebUiPlugin.getDefault().logError(e);
				}
				WebUiPlugin.getDefault().getPreferenceStore().setValue(PaletteModel.HTML5_EXPANDED_CATEGORY, name);
			}
		}
	}
	
	@Override
	public String getPreferredExpandedCategory() {
		if(paletteContents != null) {
			IFile f = paletteContents.getFile();
			try {
				String s = f.getPersistentProperty(PaletteModel.HTML5_EXPANDED_CATEGORY_NAME);
				if(s == null || s.length() == 0) {
					s = WebUiPlugin.getDefault().getPreferenceStore().getString(PaletteModel.HTML5_EXPANDED_CATEGORY);
					if(s == null || s.length() == 0) {
						s = JQueryConstants.JQM_CATEGORY;
					}
					f.setPersistentProperty(PaletteModel.HTML5_EXPANDED_CATEGORY_NAME, s);
				} else {
					WebUiPlugin.getDefault().getPreferenceStore().setValue(PaletteModel.HTML5_EXPANDED_CATEGORY, s);
				}
				return s; 
			} catch (CoreException e) {
				WebUiPlugin.getDefault().logError(e);
			}
		}
		return null;
	}

	public void reloadCategory(){
		for(Object child : paletteRoot.getChildren()){
			if(child instanceof PaletteDrawerImpl){
				PaletteDrawerImpl drawer = (PaletteDrawerImpl)child;
				IHTMLLibraryVersion newVersion = getSelectedVersion(drawer.getPaletteGroup());
				if(!drawer.getVersion().equals(newVersion)){
					drawer.loadVersion(newVersion);
				}
			}
		}
	}
}
