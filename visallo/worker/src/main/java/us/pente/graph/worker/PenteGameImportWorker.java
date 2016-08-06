package us.pente.graph.worker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import org.vertexium.*;
import org.vertexium.mutation.ElementMutation;
import org.vertexium.property.StreamingPropertyValue;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorkData;
import org.visallo.core.ingest.graphProperty.GraphPropertyWorker;
import org.visallo.core.model.Description;
import org.visallo.core.model.Name;
import org.visallo.core.model.properties.VisalloProperties;
import org.visallo.core.model.properties.types.*;
import org.visallo.core.security.VisibilityTranslator;
import org.visallo.web.clientapi.model.ClientApiPublishItem;
import org.visallo.web.clientapi.model.ClientApiVertexPublishItem;
import org.visallo.web.clientapi.model.VisibilityJson;
import us.pente.graph.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static org.visallo.core.model.properties.VisalloProperties.*;

@Name("Pente Game Import")
@Description("Imports Pente.org game files (.zip)")
public class PenteGameImportWorker extends GraphPropertyWorker {

    @VisibleForTesting
    static final String ONTOLOGY_BASE_IRI = "http://pente.us/pente#";

    private final Visibility visibility;

    @Inject
    public PenteGameImportWorker(VisibilityTranslator visibilityTranslator) {
        this.visibility = visibilityTranslator.getDefaultVisibility();
    }

    @Override
    public boolean isHandled(Element element, Property property) {
        return property != null &&
                element instanceof Vertex &&
                !CONCEPT_TYPE.hasProperty(element) &&
                RAW.getPropertyName().equals(property.getName()) &&
                "application/zip".equals(MIME_TYPE.getOnlyPropertyValue(element));
    }

    @Override
    public void execute(InputStream inputStream, GraphPropertyWorkData workData) throws Exception {
        Authorizations authorizations = getAuthorizations();
        Vertex archiveVertex = (Vertex) workData.getElement();
        StreamingPropertyValue raw = VisalloProperties.RAW.getPropertyValue(archiveVertex);
        Path zipPath = copyToTempFile(raw);
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            CONCEPT_TYPE.setProperty(
                    archiveVertex, ONTOLOGY_BASE_IRI + "gameArchive", propertyMetadata(), visibility, authorizations);
            getGraph().flush();
            publishVertex(archiveVertex, workData);
            Stream<Game> games = GameArchive.parse(zipFile);
            games.forEach(game -> createGameVertex(game, authorizations));
        } finally {
            Files.delete(zipPath);
        }
    }

    private void createGameVertex(Game game, Authorizations authorizations) {
        VertexBuilder gameBuilder = getGraph().prepareVertex("GAME_" + game.id, visibility);
        setPropertiesOnElement(gameBuilder, game, ImmutableSet.of("id", "date", "time", "moves"));
        VisalloProperties.CONCEPT_TYPE.setProperty(gameBuilder, ONTOLOGY_BASE_IRI + "game", visibility);
        setDateTimePropertyOnElement(gameBuilder, game);
        addMovesToGameVertex(gameBuilder, game);
        setElementMetadata(gameBuilder);
        Vertex gameVertex = gameBuilder.save(authorizations);
        Vertex player1Vertex = findOrCreatePlayerVertex(
                game.player1Name, game.player1Rating, game.player1Type, game.id, authorizations);
        Vertex player2Vertex = findOrCreatePlayerVertex(
                game.player2Name, game.player2Rating, game.player2Type, game.id, authorizations);
        createPlayerToGameEdge(gameVertex, player1Vertex, game.isWinner(game.player1Name), authorizations);
        createPlayerToGameEdge(gameVertex, player2Vertex, game.isWinner(game.player2Name), authorizations);
        getGraph().flush();
    }

    private Vertex findOrCreatePlayerVertex(
            String name, int rating, PlayerType type, String gameId, Authorizations authorizations) {
        Vertex playerVertex = getGraph().getVertex(name, authorizations);
        if (playerVertex == null) {
            VertexBuilder playerBuilder = getGraph().prepareVertex("PLAYER_" + name, visibility);
            VisalloProperties.CONCEPT_TYPE.setProperty(playerBuilder, ONTOLOGY_BASE_IRI + "player", visibility);
            Mappable playerProperties = new Mappable() {
                @Override
                public Map<String, Object> toMap() {
                    return ImmutableMap.of("playerName", name, "playerType", type.name());
                }
            };
            setPropertiesOnElement(playerBuilder, playerProperties, ImmutableSet.of("ratings"));
            setElementMetadata(playerBuilder);
            playerVertex = playerBuilder.save(authorizations);
        }
        playerVertex.addPropertyValue(
                gameId, ONTOLOGY_BASE_IRI + "ratings", rating, propertyMetadata(), visibility, authorizations);
        return playerVertex;
    }

    private void addMovesToGameVertex(VertexBuilder gameBuilder, Game game) {
        for (Move move : game.moves) {
            String moveKey = Integer.toString(move.number);
            String moveValue = String.format("%s %s", move.player1, move.player2);
            gameBuilder.addPropertyValue(
                    moveKey, ONTOLOGY_BASE_IRI + "moves", moveValue, propertyMetadata(), visibility);
        }
    }

    private void createPlayerToGameEdge(
            Vertex gameVertex, Vertex playerVertex, boolean isWinner, Authorizations authorizations) {
        String edgeLabel = isWinner ? "wonGame" : "lostGame";
        getGraph().addEdge(playerVertex, gameVertex, ONTOLOGY_BASE_IRI + edgeLabel, visibility, authorizations);
    }

    @SuppressWarnings("unchecked")
    private void setPropertiesOnElement(ElementMutation element, Mappable mappable, Set<String> excludeProperties) {
        Map<String, Object> properties = mappable.toMap();
        properties.keySet().stream().filter(name -> !excludeProperties.contains(name)).forEach(name -> {
            Object value = properties.get(name);
            SingleValueVisalloProperty propertyType = propertyWrapper(name, value);
            propertyType.setProperty(element, value, propertyMetadata(), visibility);
        });
    }

    private void setDateTimePropertyOnElement(ElementMutation element, Mappable mappable) {
        Map<String, Object> map = mappable.toMap();
        LocalDate date = LocalDate.parse((String) map.get("date"));
        LocalTime time = LocalTime.parse((String) map.get("time"));
        LocalDateTime dateTime = time.atDate(date);
        Date legacyDateTime = Date.from(dateTime.toInstant(ZoneOffset.from(ZonedDateTime.now())));
        String propertyName = ONTOLOGY_BASE_IRI + "dateTime";
        new DateSingleValueVisalloProperty(propertyName)
                .setProperty(element, legacyDateTime, propertyMetadata(), visibility);
    }

    private SingleValueVisalloProperty propertyWrapper(String name, Object value) {
        String propertyName = ONTOLOGY_BASE_IRI + name;
        if (value instanceof String) {
            return new StringSingleValueVisalloProperty(propertyName);
        } else if (value instanceof Boolean) {
            return new BooleanSingleValueVisalloProperty(propertyName);
        } else if (value instanceof Integer) {
            return new IntegerSingleValueVisalloProperty(propertyName);
        } else if (value instanceof Date) {
            return new DateSingleValueVisalloProperty(propertyName);
        } else if (value instanceof LocalDate) {
            return new LocalDateSingleValueVisalloProperty(propertyName);
        } else if (value instanceof Number) {
            return new IntegerSingleValueVisalloProperty(propertyName);
        }
        else {
            throw new IllegalArgumentException("no property conversion available for object class " + value.getClass());
        }
    }

    private void setElementMetadata(ElementMutation element) {
        VisalloProperties.VISIBILITY_JSON.setProperty(element, new VisibilityJson(), visibility);
        VisalloProperties.MODIFIED_BY.setProperty(element, getUser().getUserId(), visibility);
        VisalloProperties.MODIFIED_DATE.setProperty(element, new Date(), visibility);
    }

    private Metadata propertyMetadata() {
        return new PropertyMetadata(getUser(), new VisibilityJson(), visibility).createMetadata();
    }

    private void publishVertex(Vertex vertex, GraphPropertyWorkData data) throws InterruptedException {
        ClientApiVertexPublishItem vertexPublishItem = new ClientApiVertexPublishItem();
        vertexPublishItem.setVertexId(vertex.getId());
        vertexPublishItem.setAction(ClientApiPublishItem.Action.ADD_OR_UPDATE);
        // Sleeping seems to work around an org.elasticsearch.index.engine.VersionConflictEngineException thrown by ES
        // on a separate thread when the vertex is published.
        Thread.sleep(1000);
        getWorkspaceRepository().publish(
                new ClientApiPublishItem[]{vertexPublishItem}, data.getWorkspaceId(), getAuthorizations());
    }

    private Path copyToTempFile(StreamingPropertyValue spv) throws IOException {
        Path tempPath = Files.createTempFile(getClass().getName(), ".zip");
        try (InputStream inputStream = spv.getInputStream()) {
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempPath;
    }
}
