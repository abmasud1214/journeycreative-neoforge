package mod.journeycreative;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@EventBusSubscriber(modid = JourneyCreative.MODID)
public class ResearchConfig {
    public static Map<Identifier, Integer> RESEARCH_AMOUNT_REQUIREMENTS = new HashMap<>();
    public static float DEFAULT_AMOUNT_ADJUSTMENT = 1.0f;
    public static Map<Identifier, List<Identifier>> RESEARCH_PREREQUISITES = new HashMap<>();
    public static Set<Identifier> RESEARCH_PROHIBITED = new HashSet<>();
    public static Set<Identifier> RESEARCH_BLOCKED = new HashSet<>();

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(JourneyCreative.MODID, "research_loader"), new ResearchLoader());
    }

    private static class ResearchLoader extends SimplePreparableReloadListener<ResearchData> {
        @Override
        protected ResearchData prepare(ResourceManager manager, ProfilerFiller profiler) {
            ResearchData data = new ResearchData();

            for (var entry : manager.listResources("research", path -> path.getPath().endsWith(".json")).entrySet()) {
                Identifier id = entry.getKey();
                try (Reader reader = new InputStreamReader(entry.getValue().open())) {
                    if (id.getPath().endsWith("research_amount.json")) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        data.amounts.putAll(parseResearchAmounts(json));
                        data.default_amount = parseDefaultAmount(json);
                    } else if (id.getPath().endsWith("research_prerequisite.json")) {
                        data.prereqs.putAll(parseResearchPrerequisites(reader));
                    } else if (id.getPath().endsWith("research_prohibited.json")) {
                        data.prohibited.addAll(parseResearchProhibited(reader));
                    } else if (id.getPath().endsWith("research_certificate_blocked.json")) {
                        data.blocked.addAll(parseResearchBlocked(reader));
                    }
                } catch (Exception e) {
                    // Consider using a proper logger here
                    System.err.println(e.toString());
                    System.err.println("Failed to load research resource: " + id);
                }
            }
            return data;
        }

        @Override
        protected void apply(ResearchData data, ResourceManager manager, ProfilerFiller profiler) {
            // 3. Update the static fields safely on the main thread
            RESEARCH_AMOUNT_REQUIREMENTS = Collections.unmodifiableMap(data.amounts);
            RESEARCH_PREREQUISITES = Collections.unmodifiableMap(data.prereqs);
            RESEARCH_PROHIBITED = Collections.unmodifiableSet(data.prohibited);
            RESEARCH_BLOCKED = Collections.unmodifiableSet(data.blocked);
            DEFAULT_AMOUNT_ADJUSTMENT = data.default_amount;
        }
    }

    private static class ResearchData {
        final Map<Identifier, Integer> amounts = new HashMap<>();
        float default_amount = 1.0f;
        final Map<Identifier, List<Identifier>> prereqs = new HashMap<>();
        final Set<Identifier> prohibited = new HashSet<>();
        final Set<Identifier> blocked = new HashSet<>();
    }

    public static Map<Identifier, Integer> parseResearchAmounts(JsonObject root) {
        Map<Identifier, Integer> map = new HashMap<>();
        JsonObject requirements = root.getAsJsonObject("requirements");
        for (Map.Entry<String, JsonElement> requirement : requirements.entrySet()) {
            Identifier itemId = Identifier.parse(requirement.getKey());
            int amount = requirement.getValue().getAsInt();
            map.put(itemId, amount);
        }
        return map;
    }

    public static float parseDefaultAmount(JsonObject root) {
        return root.getAsJsonPrimitive("default").getAsFloat();
    }

    public static Map<Identifier, List<Identifier>> parseResearchPrerequisites(Reader reader) {
        Map<Identifier, List<Identifier>> map = new HashMap<>();
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonObject requirements = root.getAsJsonObject("prerequisites");

        for (Map.Entry<String, JsonElement> requirement : requirements.entrySet()) {
            Identifier itemId = Identifier.parse(requirement.getKey());
            List<Identifier> itemPrereqs = new ArrayList<>();
            for (JsonElement element : requirement.getValue().getAsJsonArray()) {
                itemPrereqs.add(Identifier.parse(element.getAsString()));
            }

            map.put(itemId, itemPrereqs);
        }
        return map;
    }

    public static Set<Identifier> parseResearchProhibited(Reader reader) {
        Set<Identifier> set = new HashSet<>();
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        for (JsonElement element : root.getAsJsonArray("unresearchable")) {
            set.add(Identifier.parse(element.getAsString()));
        }
        return set;
    }

    public static Set<Identifier> parseResearchBlocked(Reader reader) {
        Set<Identifier> set = new HashSet<>();
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        for (JsonElement element : root.getAsJsonArray("blocked")) {
            set.add(Identifier.parse(element.getAsString()));
        }
        return set;
    }
}
