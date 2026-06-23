package fr.oxidayzz.world.commands;

import fr.oxidayzz.world.WorldManager;
import fr.oxidayzz.world.WorldService;
import fr.oxidayzz.world.generator.BiomeGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorldCommandsTest {

    @Mock WorldManager plugin;
    @Mock WorldService worldService;
    @Mock Player player;
    @Mock Command command;

    private WorldCommands commands;

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getWorldService()).thenReturn(worldService);
        commands = new WorldCommands(plugin);
    }

    // --- onCommand tests ---

    @Test
    void onCommandReturnsTrueForNonPlayer() {
        org.bukkit.command.CommandSender consoleSender = mock(org.bukkit.command.CommandSender.class);
        assertTrue(commands.onCommand(consoleSender, command, "rw", new String[]{"list"}));
    }

    @Test
    void onCommandReturnsTrueForNoArgs() {
        assertTrue(commands.onCommand(player, command, "rw", new String[]{}));
    }

    @Test
    void onCommandConfirmDelegatesToService() {
        commands.onCommand(player, command, "rw", new String[]{"confirm"});
        verify(worldService).confirm(player);
    }

    @Test
    void onCommandCancelDelegatesToService() {
        commands.onCommand(player, command, "rw", new String[]{"cancel"});
        verify(worldService).cancel(player);
    }

    @Test
    void onCommandDeleteDelegatesToService() {
        commands.onCommand(player, command, "rw", new String[]{"delete", "myworld"});
        verify(worldService).askDeleteConfirmation(player, "myworld");
    }

    @Test
    void onCommandTpDelegatesToService() {
        commands.onCommand(player, command, "rw", new String[]{"tp", "target"});
        verify(worldService).teleportPlayer(player, "target");
    }

    @Test
    void onCommandCreateWithValidArgs() {
        commands.onCommand(player, command, "rw", new String[]{
                "create", "testworld", "ocean", "100", "100", "100", "100", "100", "100"
        });
        verify(worldService).askConfirmation(
                eq(player), eq("testworld"), eq(false), eq("ocean"),
                eq(100), eq(100), eq(100), eq(100), eq(100), eq(100), eq(false));
    }

    @Test
    void onCommandCreateWithFlatFlag() {
        commands.onCommand(player, command, "rw", new String[]{
                "create", "flatworld", "flat", "50", "50", "50", "50", "50", "50"
        });
        verify(worldService).askConfirmation(
                eq(player), eq("flatworld"), eq(true), eq("flat"),
                eq(50), eq(50), eq(50), eq(50), eq(50), eq(50), eq(false));
    }

    @Test
    void onCommandCreateWithNoStruct() {
        commands.onCommand(player, command, "rw", new String[]{
                "create", "w", "ocean", "10", "20", "30", "40", "50", "60", "nostruct"
        });
        verify(worldService).askConfirmation(
                eq(player), eq("w"), eq(false), eq("ocean"),
                eq(10), eq(20), eq(30), eq(40), eq(50), eq(60), eq(true));
    }

    @Test
    void onCommandCreateWithInsufficientArgsSendsNothing() {
        commands.onCommand(player, command, "rw", new String[]{"create", "testworld"});
        verifyNoInteractions(worldService);
    }

    @Test
    void onCommandScanWithDefaultRadius() {
        commands.onCommand(player, command, "rw", new String[]{"scan"});
        verify(worldService).scanOres(player, 1);
    }

    @Test
    void onCommandScanWithCustomRadius() {
        commands.onCommand(player, command, "rw", new String[]{"scan", "5"});
        verify(worldService).scanOres(player, 5);
    }

    @Test
    void onCommandPregenDelegatesToService() {
        commands.onCommand(player, command, "rw", new String[]{"pregen", "myworld", "10"});
        verify(worldService).pregenWorld(player, "myworld", 10);
    }

    @Test
    void onCommandPregenWithInsufficientArgsSendsMessage() {
        commands.onCommand(player, command, "rw", new String[]{"pregen"});
        verify(player).sendMessage(contains("/rw pregen"));
    }

    @Test
    void onCommandUnknownSubCommandDoesNothing() {
        commands.onCommand(player, command, "rw", new String[]{"unknown"});
        verifyNoInteractions(worldService);
    }

    @Test
    void onCommandCreateWithBadNumberSendsUsage() {
        commands.onCommand(player, command, "rw", new String[]{
                "create", "w", "ocean", "abc", "20", "30", "40", "50", "60"
        });
        verify(player).sendMessage(contains("Usage"));
    }

    // --- onTabComplete tests ---

    @Test
    void tabCompleteFirstArgReturnsSubcommands() {
        List<String> result = commands.onTabComplete(player, command, "rw", new String[]{""});
        assertTrue(result.contains("create"));
        assertTrue(result.contains("delete"));
        assertTrue(result.contains("tp"));
        assertTrue(result.contains("confirm"));
        assertTrue(result.contains("cancel"));
        assertTrue(result.contains("list"));
        assertTrue(result.contains("scan"));
        assertTrue(result.contains("pregen"));
    }

    @Test
    void tabCompleteFirstArgFiltersPartialMatch() {
        List<String> result = commands.onTabComplete(player, command, "rw", new String[]{"cr"});
        assertTrue(result.contains("create"));
        assertFalse(result.contains("delete"));
    }

    @Test
    void tabCompleteThirdArgForCreateIncludesBiomeKeys() {
        List<String> result = commands.onTabComplete(player, command, "rw", new String[]{"create", "myworld", ""});
        assertTrue(result.contains("flat"));
        for (BiomeGroup bg : BiomeGroup.values()) {
            assertTrue(result.contains(bg.getKey()));
        }
    }

    @Test
    void tabCompleteSecondArgForTpIncludesWorlds() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            World mockWorld = mock(World.class);
            when(mockWorld.getName()).thenReturn("survival");
            bukkit.when(Bukkit::getWorlds).thenReturn(List.of(mockWorld));

            List<String> result = commands.onTabComplete(player, command, "rw", new String[]{"tp", ""});
            assertTrue(result.contains("survival"));
        }
    }

    @Test
    void tabCompleteSecondArgForDeleteIncludesWorlds() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            World mockWorld = mock(World.class);
            when(mockWorld.getName()).thenReturn("creative");
            bukkit.when(Bukkit::getWorlds).thenReturn(List.of(mockWorld));

            List<String> result = commands.onTabComplete(player, command, "rw", new String[]{"delete", ""});
            assertTrue(result.contains("creative"));
        }
    }

    @Test
    void tabCompleteSecondArgForPregenIncludesWorlds() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            World mockWorld = mock(World.class);
            when(mockWorld.getName()).thenReturn("world");
            bukkit.when(Bukkit::getWorlds).thenReturn(List.of(mockWorld));

            List<String> result = commands.onTabComplete(player, command, "rw", new String[]{"pregen", ""});
            assertTrue(result.contains("world"));
        }
    }

    @Test
    void tabCompleteUnrelatedPositionReturnsEmpty() {
        List<String> result = commands.onTabComplete(player, command, "rw", new String[]{"create", "myworld", "ocean", ""});
        assertTrue(result.isEmpty());
    }
}
