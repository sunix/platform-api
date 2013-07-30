/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.vfs.client.marshal;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;

import org.exoplatform.gwtframework.commons.exception.UnmarshallerException;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;
import org.exoplatform.ide.vfs.client.model.ProjectModel;

/** @version $Id:$ */
public class ProjectUnmarshaller implements Unmarshallable<ProjectModel> {

    private final ProjectModel item;

    public ProjectUnmarshaller(ProjectModel item) {

        this.item = item;

    }

    /** @see org.exoplatform.gwtframework.commons.rest.Unmarshallable#unmarshal(com.google.gwt.http.client.Response) */
    @Override
    public void unmarshal(Response response) throws UnmarshallerException {
        doUnmarshal(response.getText());
    }

    protected void doUnmarshal(String text) throws UnmarshallerException {
        try {
            item.init(JSONParser.parseLenient(text).isObject());
        } catch (Exception exc) {
            String message = "Can't parse item " + text;
            throw new UnmarshallerException(message);
        }
    }

    @Override
    public ProjectModel getPayload() {
        return this.item;
    }

}
