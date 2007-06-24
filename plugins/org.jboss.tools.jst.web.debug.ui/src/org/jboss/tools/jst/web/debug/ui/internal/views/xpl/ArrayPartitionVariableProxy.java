/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc.
 *     Red Hat, Inc. 
 *******************************************************************************/
package org.jboss.tools.jst.web.debug.ui.internal.views.xpl;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author Jeremy
 */
public class ArrayPartitionVariableProxy extends ArrayVariableProxy {
	/**
	 * @param origin
	 */
	ArrayPartitionVariableProxy(StackFrameWrapper frameWrapper, IVariable origin) {
		super(frameWrapper, origin);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		try {
			IValue value = null;
			value = getOriginValue();
			fValue = (value == null ?  null : ValueProxyFactory.createValueProxy(fStackFrameWrapper, value, ArrayPartitionValueProxy.class));
		} catch (Exception e) {
		}
		fHasValueChanged = false;
		return fValue;
	}

}

class ArrayPartitionValueProxy extends ArrayValueProxy {
	ArrayPartitionValueProxy(StackFrameWrapper frameWrapper, IValue origin) {
		super(frameWrapper, origin);
	}
}
