/******************************************************************************* 
 * Copyright (c) 2013-2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.jst.web.ui.palette;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.jst.web.kb.taglib.IHTMLLibraryVersion;
import org.jboss.tools.jst.web.ui.JSTWebUIImages;
import org.jboss.tools.jst.web.ui.palette.internal.html.impl.PaletteDrawerImpl;

class MobileDrawerFigure extends CustomDrawerFigure {
	private Control control;
	private static JQueryMobileVersionPopUp popup;

	private PaletteDrawerImpl category;
        
	public MobileDrawerFigure(PaletteDrawerImpl category, Control control) {
		super(control);

		this.category = category;
		this.control = control;

		Figure collapseToggle = (Figure)getChildren().get(0);
		Figure title = (Figure)collapseToggle.getChildren().get(0);
		Figure pinFigure = (Figure)title.getChildren().get(0);
		Figure drawerFigure = (Figure)title.getChildren().get(1);

		if(category.getVersions().length > 0) {
			VersionFigure label = new VersionFigure(category.getVersion().toString());
			GridLayout layout = new GridLayout(4, false);
			title.setLayoutManager(layout);
               
			layout.setConstraint(drawerFigure, new GridData(GridData.FILL_HORIZONTAL));
				title.add(drawerFigure);
			title.add(label);
			title.add(pinFigure);
		}
	}

	@Override
	protected void handleExpandStateChanged() {
		super.handleExpandStateChanged();
		if(isCalledByButtonModel()) {
			category.getPaletteGroup().getPaletteModel().onCategoryExpandChange(category.getLabel(), isExpanded());
		}
	}

	private boolean isCalledByButtonModel() {
		boolean buttonModel = false;
		for (StackTraceElement s: new Throwable().getStackTrace()) {
			if(ButtonModel.class.getName().endsWith(s.getClassName())) {
				buttonModel = true;
			} else if(MobileDrawerEditPart.class.getName().equals(s.getClassName())) {
				return false;
			}
		}
		return buttonModel;
		
	}
	
	private Label label = new Label("", JSTWebUIImages.getImage(JSTWebUIImages.getInstance().createImageDescriptor(JSTWebUIImages.DROP_DOWN_LIST_IMAGE)));

	public class VersionFigure extends Clickable{
		private Color backColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		private Color foreColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);

		public VersionFigure(String text){
			super(label);
			label.setText(text);
			label.setTextPlacement(Label.WEST);
			setRolloverEnabled(true);
			setBorder(new MarginBorder(2));
			addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event) {
					popup = new JQueryMobileVersionPopUp(control, VersionFigure.this);
					popup.show(category.getVersions());
				}
			});
		}

		public void setVersion(IHTMLLibraryVersion newVersion) {
			((Label)getChildren().get(0)).setText(newVersion.toString());
			category.getPaletteGroup().getPaletteModel().getPaletteContents().setPreferredVersion(category.getLabel(), newVersion);
			category.loadVersion(newVersion);
		}
                
		public String getVersion() {
			return ((Label)getChildren().get(0)).getText();
		}

		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);

			ButtonModel model = getModel();
			if (isRolloverEnabled() && model.isMouseOver()) {
				graphics.setBackgroundColor(backColor);
				graphics.fillRoundRectangle(getClientArea().getCopy().getExpanded(1, 1), 7, 7);

				graphics.setForegroundColor(foreColor);
				graphics.drawRoundRectangle(getClientArea().getCopy().getExpanded(1, 1), 7, 7);
			}
		}
	}
}