/*******************************************************************************
 * Copyright (c) 2020, 2023 SWTChart project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Himanshu Balasamanta: Orignal API and implementation
 * Philip Wenig - resource handling
 *******************************************************************************/
package org.eclipse.swtchart.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ICircularSeries;
import org.eclipse.swtchart.ILegend;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.Resources;
import org.eclipse.swtchart.internal.ChartLayoutData;
import org.eclipse.swtchart.internal.Legend;
import org.eclipse.swtchart.internal.Title;
import org.eclipse.swtchart.internal.Util;
import org.eclipse.swtchart.internal.series.Series;

/**
 * A legend for chart.
 */
public class CircularLegend extends Composite implements ILegend, PaintListener {

	/** the plot chart */
	private Composite composite;
	private ISeries<?>[] series;
	private Title title;
	/** the state indicating the legend visibility */
	private boolean visible;
	/** the position of legend */
	private int position;
	/** the margin */
	private static final int MARGIN = 5;
	/** the width of area to draw symbol */
	private static final int SYMBOL_WIDTH = 20;
	/** the line width */
	// private static final int LINE_WIDTH = 2; // it's not used yet.
	/** the default foreground */
	private static final Color DEFAULT_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	/** the default background */
	private static final Color DEFAULT_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	/** the default font */
	private Font defaultFont;
	/** the default font size */
	private static final int DEFAULT_FONT_SIZE = Resources.SMALL_FONT_SIZE;
	/** the default position */
	private static final int DEFAULT_POSITION = SWT.RIGHT;
	/** the map between series id and cell bounds */
	private Map<String, Rectangle> cellBounds;

	/**
	 * Constructor.
	 *
	 * @param chart
	 *            the chart
	 * @param style
	 *            the style
	 */
	public CircularLegend(Composite composite, int style) {

		super(composite, style | SWT.DOUBLE_BUFFERED);
		this.composite = composite;
		visible = true;
		position = DEFAULT_POSITION;
		cellBounds = new HashMap<String, Rectangle>();
		defaultFont = Resources.getFont("Tahoma", DEFAULT_FONT_SIZE, SWT.NORMAL); //$NON-NLS-1$
		setFont(defaultFont);
		setForeground(DEFAULT_FOREGROUND);
		setBackground(DEFAULT_BACKGROUND);
		addPaintListener(this);
	}

	@Override
	public void setVisible(boolean visible) {

		if(this.visible == visible) {
			return;
		}
		this.visible = visible;
	}

	@Override
	public boolean isVisible() {

		return visible;
	}

	@Override
	public void setFont(Font font) {

		if(font == null) {
			super.setFont(defaultFont);
		} else {
			super.setFont(font);
		}
	}

	@Override
	public void setForeground(Color color) {

		if(color == null) {
			super.setForeground(DEFAULT_FOREGROUND);
		} else {
			super.setForeground(color);
		}
	}

	@Override
	public void setBackground(Color color) {

		if(color == null) {
			super.setBackground(DEFAULT_BACKGROUND);
		} else {
			super.setBackground(color);
		}
	}

	@Override
	public int getPosition() {

		return position;
	}

	@Override
	public void setPosition(int value) {

		if(value == SWT.LEFT || value == SWT.RIGHT || value == SWT.TOP || value == SWT.BOTTOM) {
			position = value;
		} else {
			position = DEFAULT_POSITION;
		}
	}

	@Override
	public Rectangle getBounds(String seriesId) {

		if(seriesId == null) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		return cellBounds.get(seriesId.trim());
	}

	/**
	 * Sorts the given series array. For instance, if there are two stack series
	 * in horizontal orientation, the top of stack series should appear at top
	 * of legend.
	 * <p>
	 * If there are multiple x axes, the given series array will be sorted with
	 * x axis first. And then, the series in each x axis will be sorted with
	 * {@link Legend#sort(List, boolean, boolean)}.
	 *
	 * @param seriesArray
	 *            the series array
	 * @return the sorted series array
	 */
	private ISeries<?>[] sort(ISeries<?>[] seriesArray) {

		// create a map between axis id and series list
		Map<Integer, List<ISeries<?>>> map = new HashMap<Integer, List<ISeries<?>>>();
		for(ISeries<?> series : seriesArray) {
			int axisId = series.getXAxisId();
			List<ISeries<?>> list = map.get(axisId);
			if(list == null) {
				list = new ArrayList<ISeries<?>>();
			}
			list.add(series);
			map.put(axisId, list);
		}
		// sort an each series list
		List<ISeries<?>> sortedArray = new ArrayList<ISeries<?>>();
		for(Entry<Integer, List<ISeries<?>>> entry : map.entrySet()) {
			sortedArray.addAll(sort(entry.getValue(), false, true));
		}
		return sortedArray.toArray(new ISeries[sortedArray.size()]);
	}

	/**
	 * Sorts the given series list which belongs to a certain x axis.
	 * <ul>
	 * <li>The stacked series will be gathered, and the order of stack series
	 * will be reversed.</li>
	 * <li>In the case of vertical orientation, the order of whole series will
	 * be reversed.</li>
	 * </ul>
	 *
	 * @param seriesList
	 *            the series list which belongs to a certain x axis
	 * @param isCategoryEnabled
	 *            true if category is enabled
	 * @param isVertical
	 *            true in the case of vertical orientation
	 * @return the sorted series array
	 */
	private static List<ISeries<?>> sort(List<ISeries<?>> seriesList, boolean isCategoryEnabled, boolean isVertical) {

		List<ISeries<?>> sortedArray = new ArrayList<ISeries<?>>();
		// gather the stacked series reversing the order of stack series
		int insertIndex = -1;
		for(int i = 0; i < seriesList.size(); i++) {
			if(isCategoryEnabled && ((Series<?>)seriesList.get(i)).isValidStackSeries()) {
				if(insertIndex == -1) {
					insertIndex = i;
				} else {
					sortedArray.add(insertIndex, seriesList.get(i));
					continue;
				}
			}
			sortedArray.add(seriesList.get(i));
		}
		// reverse the order of whole series in the case of vertical orientation
		if(isVertical) {
			Collections.reverse(sortedArray);
		}
		return sortedArray;
	}

	/**
	 * Update the layout data.
	 */
	public void updateLayoutData() {

		if(!visible) {
			setLayoutData(new ChartLayoutData(0, 0));
			return;
		}
		int width = 0;
		int height = 0;
		ISeries<?>[] seriesArray = sort(series);
		Rectangle r = composite.getClientArea();
		Rectangle titleBounds = (title).getBounds();
		int titleHeight = titleBounds.y + titleBounds.height;
		int cellHeight = Util.getExtentInGC(getFont(), null).y;
		if(position == SWT.RIGHT || position == SWT.LEFT) {
			int columns = 1;
			int yPosition = MARGIN;
			int maxCellWidth = 0;
			for(ISeries<?> series : seriesArray) {
				if(!series.isVisibleInLegend()) {
					continue;
				}
				if(series instanceof ICircularSeries) {
					if(((ICircularSeries<?>)series).getLabels() != null) {
						String[] labels = ((ICircularSeries<?>)series).getLabels();
						for(int i = 0; i != labels.length; i++) {
							int textWidth = Util.getExtentInGC(getFont(), labels[i]).x;
							int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
							maxCellWidth = Math.max(maxCellWidth, cellWidth);
							if(yPosition + cellHeight < r.height - titleHeight - MARGIN || yPosition == MARGIN) {
								yPosition += cellHeight + MARGIN;
							} else {
								columns++;
								yPosition = cellHeight + MARGIN * 2;
							}
							cellBounds.put(labels[i], new Rectangle(maxCellWidth * (columns - 1), yPosition - cellHeight - MARGIN, cellWidth, cellHeight));
							height = Math.max(yPosition, height);
						}
						width = maxCellWidth * columns;
						continue;
					}
				}
			}
			width = maxCellWidth * columns;
		} else if(position == SWT.TOP || position == SWT.BOTTOM) {
			int rows = 1;
			int xPosition = 0;
			for(ISeries<?> series : seriesArray) {
				if(!series.isVisibleInLegend()) {
					continue;
				}
				if(series instanceof ICircularSeries) {
					if(((ICircularSeries<?>)series).getLabels() != null) {
						String[] labels = ((ICircularSeries<?>)series).getLabels();
						for(int i = 0; i != labels.length; i++) {
							int textWidth = Util.getExtentInGC(getFont(), labels[i]).x;
							int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
							if(xPosition + cellWidth < r.width || xPosition == 0) {
								xPosition += cellWidth;
							} else {
								rows++;
								xPosition = cellWidth;
							}
							cellBounds.put(labels[i], new Rectangle(xPosition - cellWidth, (cellHeight + MARGIN) * (rows - 1) + MARGIN, cellWidth, cellHeight));
							width = Math.max(xPosition, width);
						}
						height = (cellHeight + MARGIN) * rows + MARGIN;
						continue;
					}
				}
			}
			height = (cellHeight + MARGIN) * rows + MARGIN;
		}
		setLayoutData(new ChartLayoutData(width, height));
	}

	/**
	 * Gets the legend label.
	 * 
	 * @param series
	 *            the series
	 * @return the legend label
	 */
	private static String getLegendLabel(ISeries<?> series) {

		String description = series.getDescription();
		if(description == null) {
			return series.getId();
		}
		return description;
	}

	/**
	 * Draws the symbol of series.
	 *
	 * @param gc
	 *            the graphics context
	 * @param series
	 *            the series
	 * @param r
	 *            the rectangle to draw the symbol of series
	 */
	protected void drawSymbol(GC gc, Series<?> series, Rectangle r) {

		return;
	}

	@Override
	public void paintControl(PaintEvent e) {

		if(!visible) {
			return;
		}
		GC gc = e.gc;
		gc.setFont(getFont());
		ISeries<?>[] seriesArray = series;
		if(seriesArray.length == 0) {
			return;
		}
		// draw frame
		gc.fillRectangle(0, 0, getSize().x - 1, getSize().y - 1);
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setLineWidth(1);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		gc.drawRectangle(0, 0, getSize().x - 1, getSize().y - 1);
		// draw content
		for(int i = 0; i < seriesArray.length; i++) {
			if(!seriesArray[i].isVisibleInLegend()) {
				continue;
			}
			//
			if(seriesArray[i] instanceof ICircularSeries) {
				ICircularSeries<?> pieSeries = (ICircularSeries<?>)seriesArray[i];
				String[] labels = pieSeries.getLabels();
				Color[] color = pieSeries.getColors();
				for(int j = 0; j != labels.length; j++) {
					Rectangle r = cellBounds.get(labels[j]);
					if(r != null) {
						String labelPie = labels[j];
						Color colorPie = color[j];
						if(labelPie != null && colorPie != null) {
							drawPieSymbol(gc, labelPie, colorPie, new Rectangle(r.x + MARGIN, r.y + MARGIN, SYMBOL_WIDTH, r.height - MARGIN * 2));
							gc.setBackground(getBackground());
							gc.setForeground(getForeground());
							gc.drawText(labelPie, r.x + SYMBOL_WIDTH + MARGIN * 2, r.y, true);
						}
					}
				}
			} else {
				// draw plot line, symbol etc
				String id = seriesArray[i].getId();
				Rectangle r = cellBounds.get(id);
				drawSymbol(gc, (Series<?>)seriesArray[i], new Rectangle(r.x + MARGIN, r.y + MARGIN, SYMBOL_WIDTH, r.height - MARGIN * 2));
				// draw label
				String label = getLegendLabel(seriesArray[i]);
				gc.setBackground(getBackground());
				gc.setForeground(getForeground());
				gc.drawText(label, r.x + SYMBOL_WIDTH + MARGIN * 2, r.y, true);
			}
		}
	}

	private void drawPieSymbol(GC gc, String string, Color color, Rectangle r) {

		gc.setBackground(color);
		int size = SYMBOL_WIDTH / 2;
		int x = r.x + size / 2;
		int y = (int)(r.y - size / 2d + r.height / 2d);
		gc.fillArc(x, y, size, size, 0, 360);
	}

	public void setChart(Chart chart) {

		this.series = chart.getSeriesSet().getSeries();
		this.title = (Title)chart.getTitle();
	}
}
