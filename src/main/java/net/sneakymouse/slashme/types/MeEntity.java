package net.sneakymouse.slashme.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MeEntity {

    private static final String RESET_PREFIX = "<reset>";

    private final LivingEntity entity;
    private final List<String> messages;
    private TextDisplay display = null;

    public MeEntity(LivingEntity entity, String message) {
        this.entity = entity;
        messages = new ArrayList<>(Collections.singletonList(withResetPrefix(message)));
    }

    public void spawn() {
        Location location = this.entity.getLocation().clone();
        location.setY(location.getY() + this.entity.getEyeHeight() * 0.85);

        this.display = this.entity.getWorld().spawn(location, TextDisplay.class);
        this.display.text(this.makeMessage());

        this.display.setBillboard(Display.Billboard.CENTER);
        this.display.setLineWidth(150);
        this.display.setShadowed(true);
        this.display.setBrightness(new Display.Brightness(15, 15));

        double entityScale = 1.0;
        AttributeInstance scaleAttribute = this.entity.getAttribute(Attribute.SCALE);
        if (scaleAttribute != null) {
            entityScale = scaleAttribute.getValue();
        }

        this.display.setTransformation(new Transformation(
                new Vector3f(0F, -0.6F * (float) entityScale, 0.5F * (float) entityScale),
                new AxisAngle4f(), new Vector3f((float) entityScale), new AxisAngle4f()));

        this.display.addScoreboardTag("MeEntity");

        this.entity.addPassenger(this.display);
    }

    public void remove() {
        if (this.display != null && this.display.isValid()) {
            this.display.remove();
        }
    }

    public int addMessage(String message) {
        int messageId = this.messages.size();

        this.messages.add(withResetPrefix(message));
        this.display.text(this.makeMessage());

        return messageId;
    }

    public boolean removeMessage(int id) {
        for (int i = id - 1; i > -1; i--) {
            if (this.messages.get(i) != null) {
                this.messages.set(i, this.messages.get(i) + "\n" + this.messages.get(id));
                break;
            }
        }

        this.messages.set(id, null);

        Component message = this.makeMessage();

        if (message != null) {
            this.display.text(message);
            return false;
        } else {
            return true;
        }
    }

    private Component makeMessage() {
        String message = null;

        for (int i = 0; i < this.messages.size(); i++) {
            if (this.messages.get(i) != null) {
                if (message == null) {
                    message = this.messages.get(i);
                } else {
                    message += "\n" + this.messages.get(i);
                }
            }
        }

        if (message == null)
            return null;

        return MiniMessage.miniMessage().deserialize(message);
    }

    private static String withResetPrefix(String message) {
        return RESET_PREFIX + message;
    }

}
