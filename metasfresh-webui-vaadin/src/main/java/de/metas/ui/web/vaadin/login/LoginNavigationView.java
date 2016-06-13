package de.metas.ui.web.vaadin.login;

import org.compiere.util.KeyNamePair;
import org.compiere.util.ValueNamePair;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

import de.metas.ui.web.vaadin.VaadinClientApplication;
import de.metas.ui.web.vaadin.components.navigator.MFView;
import de.metas.ui.web.vaadin.event.UIEventBus;
import de.metas.ui.web.vaadin.login.event.UserLoggedInEvent;
import de.metas.ui.web.vaadin.session.UserSession;
import de.metas.ui.web.window.shared.login.LoginAuthRequest;
import de.metas.ui.web.window.shared.login.LoginCompleteRequest;

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

@SuppressWarnings("serial")
public class LoginNavigationView extends CustomComponent implements MFView
{
	public LoginNavigationView()
	{
		super();
		setSizeFull();
	}

	@Override
	public void enter(final ViewChangeEvent event)
	{
		if (VaadinClientApplication.isTesting())
		{
			setCompositionRoot(new Label("Automatically logging in...."));
			
			final RestProxyLoginModel loginModel = new RestProxyLoginModel();
			loginModel.authenticate(LoginAuthRequest.of("SuperUser", "System"));
			loginModel.setLanguage(ValueNamePair.of("de_DE", "German"));
			loginModel.loginComplete(
					LoginCompleteRequest.of(
							new KeyNamePair(1000000, "Admin") // role
							, new KeyNamePair(1000000, "?") // client
							, new KeyNamePair(1000000, "?") // org
							, (KeyNamePair)null // warehouse
			));

			UserSession.getCurrent().setLoggedIn(true);
			UIEventBus.post(new UserLoggedInEvent());

			return;
		}

		setCompositionRoot(new LoginPresenter().getComponent());
	}

	@Override
	public void exit(final ViewChangeEvent event)
	{
		// nothing
	}
}
