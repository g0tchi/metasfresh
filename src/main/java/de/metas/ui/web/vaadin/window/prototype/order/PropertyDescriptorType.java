package de.metas.ui.web.vaadin.window.prototype.order;

/*
 * #%L
 * de.metas.ui.web.vaadin
 * %%
 * Copyright (C) 2016 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public enum PropertyDescriptorType
{
	Group,
	Tabular,

	/**
	 * A single value, such as a document number
	 */
	Value,

	/**
	 * One logical value that consists of a number of individual fields, like a bpartner's company name, together with its address and a contact's name
	 */
	ComposedValue,
	;
}
