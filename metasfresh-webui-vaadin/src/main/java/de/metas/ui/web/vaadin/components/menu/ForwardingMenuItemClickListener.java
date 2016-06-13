package de.metas.ui.web.vaadin.components.menu;

import de.metas.ui.web.window.shared.menu.MenuItem;

/*
 * #%L
 * metasfresh-webui
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

class ForwardingMenuItemClickListener implements MenuItemClickListener
{
	private MenuItemClickListener delegate = MenuItemClickListener.NULL;

	public void setDelegate(final MenuItemClickListener delegate)
	{
		this.delegate = delegate == null ? MenuItemClickListener.NULL : delegate;
	}
	
	public MenuItemClickListener getDelegate()
	{
		return delegate;
	}

	@Override
	public void onMenuItemClicked(final MenuItem menuItem)
	{
		delegate.onMenuItemClicked(menuItem);
	}
}