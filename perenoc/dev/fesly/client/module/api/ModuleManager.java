package dev.fesly.client.module.api;

import com.google.gson.JsonObject;
import dev.fesly.Nova;
import dev.fesly.client.module.impl.combat.*;
import dev.fesly.client.module.impl.misc.*;
import dev.fesly.client.module.impl.movement.*;
import dev.fesly.client.module.impl.render.*;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.KeyboardInputEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class ModuleManager extends ArrayList<Module> {

    public void init() {

        add(new Aura());

        add(new Blink());
        add(new AutoAuth());
        add(new HighJump());
        add(new AutoSprint());
        add(new JumpCircles());
        add(new ESP());
        add(new Test());
        //add(new Trails());

        add(new Pigga());
        add(new AntiBot());
        add(new Interface());
        add(new LivingTags());
        //add(new PostProcessing());
        add(new ClickGUI());
        add(new OtherParticles());
        add(new CustomFog());
        add(new SwingAnimations());
        add(new Tracers());
        add(new Velocity());
        add(new NoSlowDown());
        add(new BetterTab());
        add(new GAppleTimer());
        add(new NoPush());
        add(new NoJumpDelay());
        add(new AutoTpAccept());
        add(new Flight());
        add(new MCP());
        add(new BackTracks());
        add(new Spider());
        add(new ElytraFlight());
        add(new FreeCam());
        add(new ScreenWalk());
        add(new FullBright());
        add(new DragonFly());
        add(new NightMode());
        add(new TNTTime());
        add(new Strafe());
        add(new Timer());
        add(new WaterSpeed());
        add(new KTLeave());
        add(new ElytraSwap());
        add(new XrayBypass());
        add(new AutoTotem());
        add(new ItemSwapFix());

        if (Nova.getInstance().getUser().rank().toLowerCase().contains("admin") ||
                Nova.getInstance().getUser().rank().toLowerCase().contains("developer")) {
            add(new AutoIntaveOsk());
        }


        // Automatic initializations
        this.stream().filter(module -> module.getModuleInfo().autoEnabled()).forEach(module -> module.setEnabled(true));

        // Has to be a listener to handle the key presses
        Nova.getInstance().getEventBus().register(this);
    }

    public List<Module> getAll() {
        return new ArrayList<>(this);
    }

    public <T extends Module> T get(final String name) {
        return this.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .map(module -> (T) module)
                .findAny().orElse(null);
    }

    public <T extends Module> T get(final Class<T> clazz) {
        return this.stream()
                .filter(module -> module.getClass() == clazz)
                .map(module -> (T) module)
                .findAny().orElse(null);
    }

    public List<Module> get(final Category category) {
        return this.stream()
                .filter(module -> module.getModuleInfo().category() == category)
                .collect(Collectors.toList());
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<KeyboardInputEvent> onKey = event -> {

        if (event.getScreen() != null) return;

        this.stream()
                .filter(module -> module.getKeyCode() == event.getKeyCode())
                .forEach(Module::toggle);
    };

    @Override
    public boolean add(final Module module) {
        final boolean result = super.add(module);
        this.updateArraylistCache();
        return result;
    }

    @Override
    public void add(final int i, final Module module) {
        super.add(i, module);
        this.updateArraylistCache();
    }

    @Override
    public Module remove(final int i) {
        final Module result = super.remove(i);
        this.updateArraylistCache();
        return result;
    }

    @Override
    public boolean remove(final Object o) {
        final boolean result = super.remove(o);
        this.updateArraylistCache();
        return result;
    }

    public void load(JsonObject json) {
        for (Module module : getAll()) {
            try {
                if (!json.has(module.getName().toLowerCase())) continue;
                module.load(json.getAsJsonObject(module.getName().toLowerCase()));
            } catch (Exception ignored) {
            }
        }
    }

    public JsonObject save() {
        JsonObject json = new JsonObject();
        for (Module module : getAll()) {
            try {
                json.add(module.getName().toLowerCase(), module.save());
            } catch (Exception ignored) {
            }
        }
        return json;
    }

    public JsonObject save(Category category) {
        JsonObject json = new JsonObject();
        for (Module module : get(category)) {
            try {
                json.add(module.getName().toLowerCase(), module.save());
            } catch (Exception ignored) {
            }
        }
        return json;
    }


    private void updateArraylistCache() {
        final Interface interfaceModule = this.get(Interface.class);
        if (interfaceModule == null) return;

        interfaceModule.createArrayList();
    }
}