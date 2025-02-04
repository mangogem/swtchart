/*******************************************************************************
 * Copyright (c) 2017, 2023 Lablicate GmbH.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.swtchart.extensions.barcharts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.IBarSeries.BarWidthStyle;
import org.eclipse.swtchart.extensions.core.AbstractSeriesSettings;
import org.eclipse.swtchart.extensions.core.ISeriesSettings;

public class BarSeriesSettings extends AbstractSeriesSettings implements IBarSeriesSettings {

	private Color barColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private int barPadding = 20;
	private int barWidth = 1;
	private BarWidthStyle barWidthStyle = BarWidthStyle.FIXED;
	private boolean barOverlay = false;
	private boolean enableStack = false;
	private IBarSeriesSettings seriesSettingsHighlight = null;

	@Override
	public Color getBarColor() {

		return barColor;
	}

	@Override
	public void setBarColor(Color barColor) {

		this.barColor = barColor;
	}

	@Override
	public int getBarPadding() {

		return barPadding;
	}

	@Override
	public void setBarPadding(int barPadding) {

		this.barPadding = barPadding;
	}

	@Override
	public int getBarWidth() {

		return barWidth;
	}

	@Override
	public void setBarWidth(int barWidth) {

		this.barWidth = barWidth;
	}

	@Override
	public BarWidthStyle getBarWidthStyle() {

		return barWidthStyle;
	}

	@Override
	public void setBarWidthStyle(BarWidthStyle barWidthStyle) {

		this.barWidthStyle = barWidthStyle;
	}

	@Override
	public boolean isBarOverlay() {

		return barOverlay;
	}

	@Override
	public void setBarOverlay(boolean barOverlay) {

		this.barOverlay = barOverlay;
	}

	@Override
	public boolean isEnableStack() {

		return enableStack;
	}

	@Override
	public void setEnableStack(boolean enableStack) {

		this.enableStack = enableStack;
	}

	@Override
	public ISeriesSettings getSeriesSettingsHighlight() {

		if(seriesSettingsHighlight == null) {
			try {
				seriesSettingsHighlight = (IBarSeriesSettings)this.clone();
				seriesSettingsHighlight.setHighlight(true);
			} catch(CloneNotSupportedException e) {
				seriesSettingsHighlight = new BarSeriesSettings();
			}
		}
		return seriesSettingsHighlight;
	}

	@Override
	public ISeriesSettings makeDeepCopy() {

		IBarSeriesSettings barSeriesSettings = new BarSeriesSettings();
		transfer(barSeriesSettings);
		return barSeriesSettings;
	}

	@Override
	public boolean transfer(ISeriesSettings seriesSettingsSink) {

		boolean success = false;
		if(seriesSettingsSink instanceof IBarSeriesSettings) {
			IBarSeriesSettings source = this;
			IBarSeriesSettings sink = (IBarSeriesSettings)seriesSettingsSink;
			sink.setDescription(source.getDescription());
			sink.setVisible(source.isVisible());
			sink.setVisibleInLegend(source.isVisibleInLegend());
			sink.setBarColor(source.getBarColor());
			sink.setBarPadding(source.getBarPadding());
			sink.setBarWidth(source.getBarWidth());
			sink.setBarOverlay(source.isBarOverlay());
			sink.setEnableStack(source.isEnableStack());
			sink.setHighlight(source.isHighlight());
			success = true;
		}
		//
		return success;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {

		IBarSeriesSettings barSeriesSettings = new BarSeriesSettings();
		transfer(barSeriesSettings);
		return barSeriesSettings;
	}
}