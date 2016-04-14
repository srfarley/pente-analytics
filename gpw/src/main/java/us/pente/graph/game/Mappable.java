package us.pente.graph.game;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public interface Mappable {
    Gson GSON = Converters.registerAll(new GsonBuilder()).create();

    default Map<String, Object> toMap() {
        return GSON.fromJson(GSON.toJson(this), new TypeToken<Map<String, Object>>(){}.getType());
    }
}
