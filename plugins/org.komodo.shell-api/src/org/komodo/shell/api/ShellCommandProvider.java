/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.shell.api;

import java.util.Map;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Repository;


/**
 * Provides comands - used by the shell command factory when creating the map of
 * available shell commands.
 * 
 * This class adapted from classes at https://github.com/Governance/s-ramp/blob/master/s-ramp-shell-api
 * - altered map
 * 
 * @author eric.wittmann@redhat.com
 */
public interface ShellCommandProvider {

	/**
	 * Called to get the collection of commands contributed by the provider.
	 * @return the map of commands
	 */
	public Map<String, Class<? extends ShellCommand>> provideCommands();

	/**
	 * @param uow the transaction
	 * @param kObj the KomodoObject
	 * @return resolved object
	 * @throws KException the exception
	 */
	public < T extends KomodoObject > T resolve ( final Repository.UnitOfWork uow, final KomodoObject kObj ) throws KException;
}
