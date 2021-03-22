/*******************************************************************************
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.m2e.tests.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;


/**
 * A special wagon for testing that allows to record the requests made to a repository. Use
 * {@link #setRequestFilterPattern(String, boolean)} to configure what to record and to optionally clear previous
 * records. The repository URL to use with this wagon looks like {@code filex://localhost/<path-relative-to-project>}.
 */
public class FilexWagon extends FileWagon {

  private static List<String> requests = new ArrayList<>();

  private static String requestFilterPattern;

  private static String requestFailPattern;

  public static List<String> getRequests() {
    return requests;
  }

  public static void setRequestFilterPattern(String regex, boolean clear) {
    requestFilterPattern = regex;
    if(clear) {
      requests.clear();
    }
  }

  public static void setRequestFailPattern(String regex) {
    requestFailPattern = regex;
  }

  public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider)
      throws ConnectionException, AuthenticationException {
    String basedir = repository.getBasedir();
    if(basedir != null && basedir.startsWith("/")) {
      repository.setBasedir(basedir.substring(1));
    }

    super.connect(repository, authenticationInfo, proxyInfoProvider);
  }

  public void fillInputData(InputData inputData) throws TransferFailedException, ResourceDoesNotExistException {
    record("GET", inputData.getResource());
    fail(inputData.getResource());

    super.fillInputData(inputData);
  }

  public void fillOutputData(OutputData outputData) throws TransferFailedException {
    record("PUT", outputData.getResource());
    fail(outputData.getResource());

    super.fillOutputData(outputData);
  }

  private static void record(String op, Resource resource) {
    String name = resource.getName();
    if(requestFilterPattern == null || name.matches(requestFilterPattern)) {
      requests.add(op + " " + name);
    }
  }

  private static void fail(Resource resource) throws TransferFailedException {
    String name = resource.getName();
    if(requestFailPattern != null && name.matches(requestFailPattern)) {
      throw new TransferFailedException("Test failure");
    }
  }

  public static void reset() {
    requestFailPattern = null;
    requestFilterPattern = null;
    requests.clear();
  }
}
