package com.momo.dvzchatbubbles.types;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChatBubble {

    private final Player player;
    private final List<String> messages;
    private TextDisplay display = null;

    public ChatBubble(Player player, String message) {
        this.player = player;
        messages = new ArrayList<>(Collections.singletonList(message));
    }

    public void spawn() {
        Location location = this.player.getLocation().clone();
        location.setY(location.getY() + this.player.getEyeHeight() * 0.85);

        this.display = this.player.getWorld().spawn(location, TextDisplay.class);
        this.display.setText(this.makeMessage());

        this.display.setBillboard(Display.Billboard.CENTER);
        this.display.setLineWidth(150);
        this.display.setSeeThrough(false);
        this.display.setDefaultBackground(false);
        this.display.setShadowed(true);
        this.display.setBrightness(new Display.Brightness(15, 15));

        this.display.setInterpolationDuration(0);
        this.display.setInterpolationDelay(-1);
        this.display.setTransformation(new Transformation(new Vector3f(0F,-0.6F,0.5F), new AxisAngle4f(), new Vector3f(1), new AxisAngle4f()));

        this.player.addPassenger(this.display);
    }

    public void remove() {
        if (this.display != null && this.display.isValid()) {
            this.display.remove();
        }
    }

    public int addMessage(String message) {
        int messageId = this.messages.size();

        this.messages.add(message);
        this.display.setText(this.makeMessage());

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

        String message = this.makeMessage();

        if (message != null) {
            this.display.setText(this.makeMessage());
            return false;
        } else {
            return true;
        }
    }

    private String makeMessage() {
        String message = null;

        for (int i = 0; i < this.messages.size(); i ++) {
            if (this.messages.get(i) != null) {
                if (message == null) {
                    message = this.messages.get(i);
                } else {
                    message += "\n" + this.messages.get(i);
                }
            }
        }

        return message;
    }

}
