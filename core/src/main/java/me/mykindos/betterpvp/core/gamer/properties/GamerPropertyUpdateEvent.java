package me.mykindos.betterpvp.core.gamer.properties;

import lombok.Getter;
import me.mykindos.betterpvp.core.gamer.Gamer;
import me.mykindos.betterpvp.core.properties.PropertyUpdateEvent;

@Getter
public class GamerPropertyUpdateEvent extends PropertyUpdateEvent {

    private final Gamer gamer;

    public GamerPropertyUpdateEvent(Gamer gamer, String property, Object value) {
        super(property, value);
        this.gamer = gamer;
    }
}
