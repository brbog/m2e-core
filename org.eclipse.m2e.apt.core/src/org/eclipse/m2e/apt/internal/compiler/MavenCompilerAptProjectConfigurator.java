/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 *     Karl M. Davis (Knowledge Computing Corp.) - initial implementation
 ************************************************************************************/

package org.eclipse.m2e.apt.internal.compiler;

import org.eclipse.core.runtime.Assert;

import org.eclipse.m2e.apt.internal.AbstractAptProjectConfigurator;
import org.eclipse.m2e.apt.internal.AptConfiguratorDelegate;
import org.eclipse.m2e.apt.internal.NoOpDelegate;
import org.eclipse.m2e.apt.preferences.AnnotationProcessingMode;


public class MavenCompilerAptProjectConfigurator extends AbstractAptProjectConfigurator {

  @Override
  protected AptConfiguratorDelegate getDelegate(AnnotationProcessingMode mode) {
    Assert.isNotNull(mode, "AnnotationProcessingMode can not be null");
    switch(mode) {
      case jdt_apt:
        return new MavenCompilerJdtAptDelegate(markerManager);
      case maven_execution:
        return new MavenCompilerExecutionDelegate(markerManager);
      case disabled:
      default:
    }
    return new NoOpDelegate();
  }

}
