/*
 * vagrant-junit5-extensions
 * Copyright (C) 2018 - coffeemilk.app
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.coffeemilk.test.junit5.extension.vagrant;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class VagrantJUnit5Extension implements BeforeAllCallback, AfterAllCallback {

	private VagrantOperations vagrantOperations;

	public VagrantJUnit5Extension() {
		this(new VagrantOperationsCommandLine());
	}

	public VagrantJUnit5Extension(VagrantOperations vagrantOperations) {
		this.vagrantOperations = vagrantOperations;
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		vagrantOperations.shutdown();
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		if (vagrantOperations.isUp()) {
			vagrantOperations.reload(true);
		} else {
			vagrantOperations.waitForStartup();
		}
	}
}
