package org.visallo.pente.gpw;

import org.vertexium.Element;
import org.vertexium.Property;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorkData;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorker;

import java.io.InputStream;

public class PenteGameImportWorker extends GraphPropertyWorker {
    public PenteGameImportWorker() {
        System.err.println("*** PenteGameImportWorker");
    }

    @Override
    public boolean isHandled(Element element, Property property) {
        return false;
    }

    @Override
    public void execute(InputStream inputStream, GraphPropertyWorkData graphPropertyWorkData) throws Exception {

    }
}
