/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.jst.web.ui.action.server;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.common.model.ServiceDialog;
import org.jboss.tools.common.model.options.PreferenceModelUtilities;
import org.jboss.tools.jst.web.messages.xpl.WebUIMessages;
import org.jboss.tools.jst.web.server.*;

public class RefreshServerActionDelegate extends AbstractServerActionDelegate {

	protected void doRun() {
		IServer server = ServerManager.getInstance().getSelectedServer();
		if(server == null) return;
		ServiceDialog d = PreferenceModelUtilities.getPreferenceModel().getService();
		try {
			server.restart(getLaunchMode(server), new NullProgressMonitor());
		} catch (Exception e) {
			d.showDialog(WebUIMessages.ERROR, e.getMessage(), new String[]{WebUIMessages.CLOSE}, null, ServiceDialog.ERROR);
		}
	}

	protected String getLaunchMode(IServer server) {
		String mode = server.getMode();
		if(mode == null) mode = ILaunchManager.RUN_MODE;
		return mode;
	}

	protected boolean isActionEnabled() {
		IServer selected = ServerManager.getInstance().getSelectedServer();
		return (selected != null && selected.getServerState() == IServer.STATE_STARTED);
	}	
			
	protected String computeToolTip() {
		IServer selected = ServerManager.getInstance().getSelectedServer();
		String name = selected == null ? "" : selected.getName(); //$NON-NLS-1$
		return WebUIMessages.REFRESH + name;
	}	

}
