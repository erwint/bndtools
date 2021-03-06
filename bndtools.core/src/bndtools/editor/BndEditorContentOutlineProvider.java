/*******************************************************************************
 * Copyright (c) 2010 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package bndtools.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import aQute.lib.osgi.Constants;
import bndtools.editor.model.BndEditModel;
import bndtools.editor.model.ServiceComponent;
import bndtools.launch.LaunchConstants;
import bndtools.model.clauses.ExportedPackage;
import bndtools.model.clauses.ImportPattern;

public class BndEditorContentOutlineProvider implements ITreeContentProvider, PropertyChangeListener {

    static final String PRIVATE_PKGS = "__private_pkgs";
    static final String EXPORTS = "__exports";
    static final String IMPORT_PATTERNS = "__import_patterns";

	BndEditModel model;
	private final TreeViewer viewer;

	public BndEditorContentOutlineProvider(TreeViewer viewer) {
		this.viewer = viewer;
	}
	public Object[] getElements(Object inputElement) {
		Object[] result;
		if(model.isProjectFile()) {
			result = new String[] { PRIVATE_PKGS, EXPORTS, IMPORT_PATTERNS, BndEditor.BUILD_PAGE, BndEditor.PROJECT_RUN_PAGE, BndEditor.COMPONENTS_PAGE, BndEditor.SOURCE_PAGE };
		} else if(model.getBndResource().getName().endsWith(LaunchConstants.EXT_BNDRUN)) {
		    result = new String[] { BndEditor.PROJECT_RUN_PAGE, BndEditor.SOURCE_PAGE };
		} else {
			result = new String[] { PRIVATE_PKGS, EXPORTS, IMPORT_PATTERNS, BndEditor.COMPONENTS_PAGE, BndEditor.SOURCE_PAGE };
		}
		return result;
	}
	public void dispose() {
		if(model != null)
			model.removePropertyChangeListener(this);
	}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(model != null)
			model.removePropertyChangeListener(this);

		model = (BndEditModel) newInput;
		if(model != null)
			model.addPropertyChangeListener(this);
	}

    public Object[] getChildren(Object parentElement) {
        Object[] result = new Object[0];

        if (parentElement instanceof String) {
            if (BndEditor.COMPONENTS_PAGE.equals(parentElement)) {
                Collection<ServiceComponent> components = model.getServiceComponents();
                if (components != null)
                    result = components.toArray(new ServiceComponent[components.size()]);
            } else if (EXPORTS.equals(parentElement)) {
                List<ExportedPackage> exports = model.getExportedPackages();
                if (exports != null)
                    result = exports.toArray(new Object[exports.size()]);
            } else if (PRIVATE_PKGS.equals(parentElement)) {
                List<String> packages = model.getPrivatePackages();
                if (packages != null) {
                    List<PrivatePkg> wrapped = new ArrayList<PrivatePkg>(packages.size());
                    for (String pkg : packages) {
                        wrapped.add(new PrivatePkg(pkg));
                    }
                    result = wrapped.toArray(new Object[wrapped.size()]);
                }
            } else if (IMPORT_PATTERNS.equals(parentElement)) {
                List<ImportPattern> imports = model.getImportPatterns();
                if (imports != null)
                    result = imports.toArray(new Object[imports.size()]);
            }
        }
        return result;
    }

	public Object getParent(Object element) {
		return null;
	}

    public boolean hasChildren(Object element) {
        if (element instanceof String) {
            if (BndEditor.COMPONENTS_PAGE.equals(element)) {
                Collection<ServiceComponent> components = model.getServiceComponents();
                return components != null && !components.isEmpty();
            }
            if (EXPORTS.equals(element)) {
                List<ExportedPackage> exports = model.getExportedPackages();
                return exports != null && !exports.isEmpty();
            }
            if (PRIVATE_PKGS.equals(element)) {
                List<String> packages = model.getPrivatePackages();
                return packages != null && !packages.isEmpty();
            }
            if (IMPORT_PATTERNS.equals(element)) {
                List<ImportPattern> imports = model.getImportPatterns();
                return imports != null && !imports.isEmpty();
            }
        }
        return false;
    }
	public void propertyChange(PropertyChangeEvent evt) {
		if(Constants.SERVICE_COMPONENT.equals(evt.getPropertyName())) {
			viewer.refresh(BndEditor.COMPONENTS_PAGE);
			viewer.expandToLevel(BndEditor.COMPONENTS_PAGE, 1);
		} else if(Constants.EXPORT_PACKAGE.equals(evt.getPropertyName())) {
			viewer.refresh(EXPORTS);
			viewer.expandToLevel(EXPORTS, 1);
		} else if(Constants.PRIVATE_PACKAGE.equals(evt.getPropertyName())) {
		    viewer.refresh(PRIVATE_PKGS);
		    viewer.expandToLevel(PRIVATE_PKGS, 1);
		} else if(Constants.IMPORT_PACKAGE.equals(evt.getPropertyName())) {
			viewer.refresh(IMPORT_PATTERNS);
			viewer.expandToLevel(IMPORT_PATTERNS, 1);
		}
	}
}

class PrivatePkg {
    final String pkg;

    PrivatePkg(String pkg) {
        this.pkg = pkg;
    }
}
