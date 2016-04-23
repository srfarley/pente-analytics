package us.pente.graph.worker;

import org.junit.Test;
import org.mockito.Mockito;
import org.vertexium.Authorizations;
import org.vertexium.Graph;
import org.vertexium.Vertex;
import org.vertexium.Visibility;
import org.vertexium.inmemory.InMemoryAuthorizations;
import org.vertexium.inmemory.InMemoryGraph;
import org.vertexium.property.StreamingPropertyValue;
import org.vertexium.query.Query;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorkData;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import org.visallo.core.model.properties.VisalloProperties;
import org.visallo.core.model.workQueue.Priority;
import org.visallo.core.security.DirectVisibilityTranslator;
import org.visallo.core.security.VisibilityTranslator;
import org.visallo.core.user.User;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.visallo.core.util.StreamUtil.stream;
import static us.pente.graph.worker.PenteGameImportWorker.ONTOLOGY_BASE_IRI;

public class PenteGameImportWorkerTest {
    @Test
    public void executeShouldCreateGraphElementsFromGameArchiveVertex() throws Exception {
        Authorizations authorizations = new InMemoryAuthorizations();
        VisibilityTranslator visibilityTranslator = new DirectVisibilityTranslator();
        Visibility visibility = visibilityTranslator.getDefaultVisibility();
        Graph graph = InMemoryGraph.create();
        Vertex archiveVertex = graph.addVertex(visibility, authorizations);
        InputStream archiveIn = getClass().getResource("/games.zip").openStream();
        StreamingPropertyValue value = new StreamingPropertyValue(archiveIn, byte[].class);
        VisalloProperties.RAW.setProperty(archiveVertex, value, visibility, authorizations);

        User user = Mockito.mock(User.class);
        Mockito.when(user.getUserId()).thenReturn("USER");
        GraphPropertyWorkerPrepareData prepareData = new GraphPropertyWorkerPrepareData(
                null, null, user, authorizations, null);
        PenteGameImportWorker worker = new PenteGameImportWorker(visibilityTranslator);
        worker.setGraph(graph);
        worker.prepare(prepareData);
        GraphPropertyWorkData workData = new GraphPropertyWorkData(
                visibilityTranslator, archiveVertex, null, null, "", Priority.NORMAL);

        worker.execute(null, workData);

        Query gameQuery = graph.query(authorizations)
                .has(VisalloProperties.CONCEPT_TYPE.getPropertyName(), ONTOLOGY_BASE_IRI + "game");
        List<Vertex> gameVertices = stream(gameQuery.vertices()).collect(Collectors.toList());
        assertThat(gameVertices.size(), is(3));

        Query playerQuery = graph.query(authorizations)
                .has(VisalloProperties.CONCEPT_TYPE.getPropertyName(), ONTOLOGY_BASE_IRI + "player");
        List<Vertex> playerVertices = stream(playerQuery.vertices()).collect(Collectors.toList());
        assertThat(playerVertices.size(), is(6));

        // TODO: verify edges
    }
}
