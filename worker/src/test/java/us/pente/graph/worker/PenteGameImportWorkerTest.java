package us.pente.graph.worker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.vertexium.*;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.visallo.core.util.StreamUtil.stream;
import static us.pente.graph.worker.PenteGameImportWorker.ONTOLOGY_BASE_IRI;

public class PenteGameImportWorkerTest {
    private Graph graph;
    private Authorizations authorizations;
    private VisibilityTranslator visibilityTranslator;
    private User user;
    private Vertex archiveVertex;
    private Visibility visibility;

    @Before
    public void before() throws Exception {
        graph = InMemoryGraph.create();
        authorizations = new InMemoryAuthorizations();
        visibilityTranslator = new DirectVisibilityTranslator();

        visibility = visibilityTranslator.getDefaultVisibility();
        archiveVertex = graph.addVertex(visibility, authorizations);
        InputStream archiveIn = getClass().getResource("/games.zip").openStream();
        StreamingPropertyValue value = new StreamingPropertyValue(archiveIn, byte[].class);
        VisalloProperties.RAW.setProperty(archiveVertex, value, visibility, authorizations);
        VisalloProperties.MIME_TYPE.addPropertyValue(archiveVertex, "", "application/zip", visibility, authorizations);

        user = Mockito.mock(User.class);
        Mockito.when(user.getUserId()).thenReturn("user_id");
    }

    @Test
    public void isHandledReturnsTrueForRawPropertyWithZipMimeType() throws Exception {
        PenteGameImportWorker worker = createWorker();

        boolean handled = worker.isHandled(archiveVertex, VisalloProperties.RAW.getProperty(archiveVertex));

        assertThat(handled, is(true));
    }

    @Test
    public void isHandledReturnsFalseForRawPropertyWithOtherMimeType() throws Exception {
        PenteGameImportWorker worker = createWorker();

        VisalloProperties.MIME_TYPE.removeProperty(archiveVertex, "", authorizations);
        VisalloProperties.MIME_TYPE.addPropertyValue(
                archiveVertex, "", "application/octet-stream", visibility, authorizations);

        boolean handled = worker.isHandled(archiveVertex, VisalloProperties.RAW.getProperty(archiveVertex));

        assertThat(handled, is(false));
    }

    @Test
    public void isHandledReturnsFalseForNullProperty() throws Exception {
        PenteGameImportWorker worker = createWorker();

        assertThat(worker.isHandled(archiveVertex, null), is(false));
    }

    @Test
    public void executeShouldCreateGraphElementsFromGameArchiveVertex() throws Exception {
        PenteGameImportWorker worker = createWorker();
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

        Vertex game1 = gameVertices.get(0);
        assertThat(game1.getPropertyValue(ONTOLOGY_BASE_IRI + "winner"), is("superman"));
        assertThat(game1.getPropertyValue(ONTOLOGY_BASE_IRI + "loser"), is("batman"));

        Set<EdgeVertexPair> wins = stream(
                game1.getEdgeVertexPairs(Direction.IN, ONTOLOGY_BASE_IRI + "wonGame", authorizations))
                .collect(Collectors.toSet());

        Set<EdgeVertexPair> losses = stream(
                game1.getEdgeVertexPairs(Direction.IN, ONTOLOGY_BASE_IRI + "lostGame", authorizations))
                .collect(Collectors.toSet());

        assertThat(wins.size(), is(1));
        assertThat(losses.size(), is(1));

        EdgeVertexPair win = wins.stream().findAny().orElse(null);
        assertThat(win.getVertex().getPropertyValue(ONTOLOGY_BASE_IRI + "playerName"), is ("superman"));

        EdgeVertexPair loss = losses.stream().findAny().orElse(null);
        assertThat(loss.getVertex().getPropertyValue(ONTOLOGY_BASE_IRI + "playerName"), is ("batman"));
    }

    private PenteGameImportWorker createWorker() throws Exception {
        GraphPropertyWorkerPrepareData prepareData = new GraphPropertyWorkerPrepareData(
                null, null, user, authorizations, null);
        PenteGameImportWorker worker = new PenteGameImportWorker(visibilityTranslator);
        worker.setGraph(graph);
        worker.prepare(prepareData);
        return worker;
    }
}
