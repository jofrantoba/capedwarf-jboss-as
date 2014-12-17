/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.capedwarf.deployment;

import java.lang.reflect.Method;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.modules.Module;

/**
 * Boot war modules.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfBootProcessor extends CapedwarfWebDeploymentProcessor {
    @Override
    protected void doDeploy(DeploymentUnit unit) throws DeploymentUnitProcessingException {
        ApplicationConfiguration configuration = unit.getAttachment(CapedwarfAttachments.APPLICATION_CONFIGURATION);
        AppEngineWebXml appEngineWebXml = configuration.getAppEngineWebXml();
        if (appEngineWebXml.isWarmupRequests()) {
            final Module module = unit.getAttachment(Attachments.MODULE);
            final ClassLoader cl = module.getClassLoader();
            final ClassLoader previous = SecurityActions.setTCCL(cl);
            try {
                Class<?> siClass = cl.loadClass("org.jboss.capedwarf.tasks.ServletInvoker");
                Method invoke = siClass.getMethod("invoke", String.class, String.class);
                invoke.invoke(null, module.getIdentifier().toString(), "/_ah/warmup");
            } catch (Exception e) {
                throw new DeploymentUnitProcessingException(e);
            } finally {
                SecurityActions.setTCCL(previous);
            }
        }
    }
}
