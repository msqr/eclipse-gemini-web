/*******************************************************************************
 * Copyright (c) 2012, 2014 SAP AG
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *   http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.  
 *
 * Contributors:
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package test;

import javax.servlet.annotation.WebServlet;

/**
 * Servlet covers bug - https://issues.apache.org/bugzilla/show_bug.cgi?id=53356 - Mapping a servlet to the
 * application's context root results in IAE
 */
@WebServlet("")
public class Bug53356Servlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

}
