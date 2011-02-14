/*******************************************************************************
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.m2e.core.ui.internal.actions;

import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.*;


import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.core.IMavenConstants;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.index.IndexedArtifactFile;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.ui.internal.Messages;
import org.eclipse.m2e.core.ui.internal.dialogs.MavenRepositorySearchDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class AddDependencyAction extends MavenActionSupport implements IWorkbenchWindowActionDelegate {
  private static final Logger log = LoggerFactory.getLogger(AddDependencyAction.class);

    public static final String ID = "org.eclipse.m2e.addDependencyAction"; //$NON-NLS-1$

    public void run(IAction action) {
      IFile file = getPomFileFromPomEditorOrViewSelection();

      if(file == null) {
        return;
      }

      MavenPlugin plugin = MavenPlugin.getDefault();
      MavenProject mp = null;
      IProject prj = file.getProject();
      if (prj != null && IMavenConstants.POM_FILE_NAME.equals(file.getProjectRelativePath().toString())) {
          IMavenProjectFacade facade = MavenPlugin.getDefault().getMavenProjectManager().getProject(prj);
          if (facade != null) {
            mp = facade.getMavenProject();
          }
      }
      
      MavenRepositorySearchDialog dialog = MavenRepositorySearchDialog.createSearchDependencyDialog(getShell(), Messages.AddDependencyAction_searchDialog_title, mp, prj, false);
      if(dialog.open() == Window.OK) {
        IndexedArtifactFile indexedArtifactFile = (IndexedArtifactFile) dialog.getFirstResult();
        if(indexedArtifactFile != null) {
          try {
            MavenModelManager modelManager = plugin.getMavenModelManager();
            final Dependency dependency = indexedArtifactFile.getDependency();
            String selectedScope = dialog.getSelectedScope();
            dependency.setScope(selectedScope);
            
            if (indexedArtifactFile.version == null) {
              dependency.setVersion(null);
            }
            performOnDOMDocument(new OperationTuple(file, new Operation() {
              public void process(Document document) {
                Element depsEl = getChild(document.getDocumentElement(), "dependencies");//$NON-NLS-1$
                Element dep = findChild(depsEl, "dependency", //$NON-NLS-1$
                    childEquals("groupId", dependency.getGroupId()), //$NON-NLS-1$
                    childEquals("artifactId", dependency.getArtifactId()));//$NON-NLS-1$
                if (dep == null) {
                  dep = createDependency(depsEl, dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
                } else {
                  //only set version if already exists
                  if (dependency.getVersion() != null) {
                    setText(getChild(dep, "version"), dependency.getVersion());//$NON-NLS-1$
                  }
                }
                if (dependency.getType() != null //
                    && !"jar".equals(dependency.getType()) // //$NON-NLS-1$
                    && !"null".equals(dependency.getType())) { // guard against MNGECLIPSE-622 //$NON-NLS-1$
                  
                  setText(getChild(dep, "type"), dependency.getType());
                }
                
                if (dependency.getClassifier() != null) {
                  setText(getChild(dep, "classifier"), dependency.getClassifier());//$NON-NLS-1$
                }
                
                if(dependency.getScope() != null && !"compile".equals(dependency.getScope())) { //$NON-NLS-1$
                  setText(getChild(dep, "scope"), dependency.getScope());//$NON-NLS-1$
                }
                
              }
            }));
          } catch(Exception ex) {
            String msg = NLS.bind(Messages.AddDependencyAction_error_msg, file);
            log.error(msg, ex);
            MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.AddDependencyAction_error_title, msg);
          }
        }
      }
    }
    
    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }
  }
