package org.visallo.pente.gpw;

import org.vertexium.Authorizations;
import org.vertexium.Element;
import org.vertexium.Property;
import org.vertexium.Vertex;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorkData;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorker;
import org.visallo.core.ingest.graphProperty.PostMimeTypeWorker;
import org.visallo.core.model.Description;
import org.visallo.core.model.Name;

import java.io.InputStream;

@Name("Pente Game Import")
@Description("Imports Pente.org game files (.zip)")
public class PenteGameImportWorker extends PostMimeTypeWorker {

    @Override
    protected void execute(String mimeType, GraphPropertyWorkData data, Authorizations authorizations)
            throws Exception {
        if ("application/zip".equals(mimeType)) {
            Vertex file = (Vertex) data.getElement();
        }
    }
}
