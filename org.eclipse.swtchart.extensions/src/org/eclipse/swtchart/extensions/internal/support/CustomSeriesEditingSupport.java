/*******************************************************************************
 * Copyright (c) 2023 Lablicate GmbH.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.swtchart.extensions.internal.support;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swtchart.extensions.core.CustomSeriesLabelProvider;
import org.eclipse.swtchart.extensions.core.CustomSeriesListUI;
import org.eclipse.swtchart.extensions.model.ICustomSeries;

public class CustomSeriesEditingSupport extends EditingSupport {

	private CustomSeriesListUI customSeriesListUI;
	private String title = "";

	public CustomSeriesEditingSupport(CustomSeriesListUI seriesListUI, String title) {

		super(seriesListUI);
		this.customSeriesListUI = seriesListUI;
		this.title = title;
	}

	@Override
	protected boolean canEdit(Object element) {

		boolean canEdit;
		switch(title) {
			case CustomSeriesLabelProvider.DRAW:
				canEdit = true;
				break;
			default:
				canEdit = false;
				break;
		}
		return canEdit;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {

		CellEditor cellEditor;
		switch(title) {
			case CustomSeriesLabelProvider.DRAW:
				cellEditor = new CheckboxCellEditor(customSeriesListUI.getTable());
				break;
			default:
				cellEditor = new TextCellEditor(customSeriesListUI.getTable());
				break;
		}
		return cellEditor;
	}

	@Override
	protected Object getValue(Object element) {

		Object object = null;
		if(element instanceof ICustomSeries) {
			ICustomSeries customSeries = (ICustomSeries)element;
			/*
			 * Series Settings
			 */
			switch(title) {
				case CustomSeriesLabelProvider.DRAW:
					object = customSeries.isDraw();
					break;
				default:
					object = null;
					break;
			}
		}
		return object;
	}

	@Override
	protected void setValue(Object element, Object object) {

		if(element instanceof ICustomSeries) {
			ICustomSeries customSeries = (ICustomSeries)element;
			/*
			 * Series Settings
			 */
			switch(title) {
				case CustomSeriesLabelProvider.DRAW:
					customSeries.setDraw(Boolean.valueOf(object.toString()));
					break;
			}
			//
			refresh();
		}
	}

	private void refresh() {

		customSeriesListUI.refresh();
	}
}