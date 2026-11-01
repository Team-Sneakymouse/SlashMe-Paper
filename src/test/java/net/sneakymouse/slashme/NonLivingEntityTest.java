package net.sneakymouse.slashme;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.sneakymouse.slashme.commands.MeHandler;
import net.sneakymouse.slashme.commands.SlashMeCommands;
import net.sneakymouse.slashme.types.MeEntity;

class NonLivingEntityTest {
    static Stream<Class<? extends Entity>> nonLivingTypes() {
        return Stream.of(ItemDisplay.class, BlockDisplay.class, TextDisplay.class,
                Item.class, Minecart.class, Boat.class, Interaction.class, Entity.class);
    }

    @ParameterizedTest
    @MethodSource("nonLivingTypes")
    void nonLivingEntitiesCanSpawnStackAndRemoveBubbles(Class<? extends Entity> type) throws Exception {
        Entity target = mock(type);
        World world = mock(World.class);
        TextDisplay display = mock(TextDisplay.class);
        Location origin = new Location(world, 1, 2, 3);
        when(target.getLocation()).thenReturn(origin);
        when(target.getWorld()).thenReturn(world);
        double height = type == Minecart.class ? 1.5 : 0.0;
        when(target.getHeight()).thenReturn(height);
        when(world.spawn(any(Location.class), eq(TextDisplay.class))).thenReturn(display);
        when(target.addPassenger(display)).thenReturn(true);
        when(display.isValid()).thenReturn(true);

        MeEntity bubble = new MeEntity(target, "First");
        bubble.spawn();
        verify(target).addPassenger(display);
        ArgumentCaptor<Location> location = ArgumentCaptor.forClass(Location.class);
        verify(world).spawn(location.capture(), eq(TextDisplay.class));
        assertEquals(2 + height * 0.85, location.getValue().getY(), 1e-9);
        ArgumentCaptor<org.bukkit.util.Transformation> transformation = ArgumentCaptor.forClass(org.bukkit.util.Transformation.class);
        verify(display).setTransformation(transformation.capture());
        assertEquals(new org.joml.Vector3f(1), transformation.getValue().getScale());
        assertEquals(2, origin.getY(), "Do not mutate the target's location");
        assertEquals(1, bubble.addMessage("Second"));
        verify(display).text(Component.text("First\nSecond"));
        assertFalse(bubble.removeMessage(0));
        assertTrue(bubble.removeMessage(1));
        bubble.remove();
        verify(display).remove();
    }

    @ParameterizedTest
    @ValueSource(strings = {"you", "youu"})
    @SuppressWarnings("unchecked")
    void commandsAcceptNonLivingTargets(String name) throws Exception {
        try (var arguments = mockStatic(ArgumentTypes.class); var handler = mockStatic(MeHandler.class)) {
            arguments.when(ArgumentTypes::entity).thenReturn(mock(ArgumentType.class));
            CommandContext<CommandSourceStack> ctx = mock(CommandContext.class);
            CommandSourceStack source = mock(CommandSourceStack.class);
            Player executor = mock(Player.class);
            Entity target = mock(ItemDisplay.class);
            EntitySelectorArgumentResolver resolver = mock(EntitySelectorArgumentResolver.class);
            when(ctx.getSource()).thenReturn(source);
            when(source.getExecutor()).thenReturn(executor);
            when(ctx.getArgument("target", EntitySelectorArgumentResolver.class)).thenReturn(resolver);
            when(ctx.getArgument("message", String.class)).thenReturn("Glows");
            when(resolver.resolve(source)).thenReturn(List.of(target));
            Method execute = SlashMeCommands.class.getDeclaredMethod("executeYou", CommandContext.class, String.class, int.class);
            execute.setAccessible(true);
            int duration = name.equals("you") ? 120 : 300;
            assertEquals(1, execute.invoke(null, ctx, name, duration));
            handler.verify(() -> MeHandler.execute(executor, target, "Glows", duration, name));
        }
    }

    @Test
    void removingNonLivingTargetCleansUpBubble() {
        SlashMe plugin = mock(SlashMe.class, CALLS_REAL_METHODS);
        plugin.entityChatBubbles = new HashMap<>();
        Entity target = mock(ItemDisplay.class);
        UUID id = UUID.randomUUID();
        when(target.getUniqueId()).thenReturn(id);
        MeEntity bubble = mock(MeEntity.class);
        plugin.entityChatBubbles.put(id, bubble);
        EntityRemoveEvent event = mock(EntityRemoveEvent.class);
        when(event.getEntity()).thenReturn(target);
        plugin.onEntityRemove(event);
        assertFalse(plugin.entityChatBubbles.containsKey(id));
        verify(bubble).remove();
    }
}
