package org.visallo.pente.gpw;

import org.vertexium.Element;
import org.vertexium.Property;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorkData;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorker;
import org.visallo.core.model.Description;
import org.visallo.core.model.Name;

import java.io.InputStream;

@Name("Pente Game Import")
@Description("Imports Pente.org game files (.zip)")
public class PenteGameImportWorker extends GraphPropertyWorker {

    @Override
    public boolean isHandled(Element element, Property property) {
        return false;
    }

    @Override
    public void execute(InputStream inputStream, GraphPropertyWorkData graphPropertyWorkData) throws Exception {

    }
}
