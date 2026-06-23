package fr.oxidayzz.world;

import fr.oxidayzz.world.WorldService.ActionType;
import fr.oxidayzz.world.WorldService.PendingAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldServiceTest {

    // --- PendingAction record tests ---

    @Test
    void pendingActionCreateRecordStoresValues() {
        PendingAction action = new PendingAction(
                "testWorld", true, "ocean,desert",
                100, 200, 150, 50, 75, 125,
                true, ActionType.CREATE);

        assertEquals("testWorld", action.name());
        assertTrue(action.isFlat());
        assertEquals("ocean,desert", action.biomeList());
        assertEquals(100, action.d());
        assertEquals(200, action.g());
        assertEquals(150, action.i());
        assertEquals(50, action.l());
        assertEquals(75, action.e());
        assertEquals(125, action.r());
        assertTrue(action.noStructures());
        assertEquals(ActionType.CREATE, action.type());
    }

    @Test
    void pendingActionDeleteRecordStoresValues() {
        PendingAction action = new PendingAction(
                "deleteMe", false, null,
                0, 0, 0, 0, 0, 0,
                false, ActionType.DELETE);

        assertEquals("deleteMe", action.name());
        assertFalse(action.isFlat());
        assertNull(action.biomeList());
        assertEquals(0, action.d());
        assertEquals(ActionType.DELETE, action.type());
    }

    @Test
    void pendingActionEquality() {
        PendingAction a1 = new PendingAction("w", false, null, 1, 2, 3, 4, 5, 6, false, ActionType.CREATE);
        PendingAction a2 = new PendingAction("w", false, null, 1, 2, 3, 4, 5, 6, false, ActionType.CREATE);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void pendingActionInequality() {
        PendingAction a1 = new PendingAction("w1", false, null, 0, 0, 0, 0, 0, 0, false, ActionType.CREATE);
        PendingAction a2 = new PendingAction("w2", false, null, 0, 0, 0, 0, 0, 0, false, ActionType.CREATE);
        assertNotEquals(a1, a2);
    }

    @Test
    void pendingActionToStringContainsValues() {
        PendingAction action = new PendingAction("myWorld", true, "ocean", 10, 20, 30, 40, 50, 60, true, ActionType.CREATE);
        String str = action.toString();
        assertTrue(str.contains("myWorld"));
        assertTrue(str.contains("CREATE"));
    }

    // --- ActionType enum tests ---

    @Test
    void actionTypeCreateExists() {
        assertEquals(ActionType.CREATE, ActionType.valueOf("CREATE"));
    }

    @Test
    void actionTypeDeleteExists() {
        assertEquals(ActionType.DELETE, ActionType.valueOf("DELETE"));
    }

    @Test
    void actionTypeValuesLength() {
        assertEquals(2, ActionType.values().length);
    }

    @Test
    void pendingActionWithNullBiomeList() {
        PendingAction action = new PendingAction("w", false, null, 0, 0, 0, 0, 0, 0, false, ActionType.DELETE);
        assertNull(action.biomeList());
    }

    @Test
    void pendingActionWithEmptyBiomeList() {
        PendingAction action = new PendingAction("w", false, "", 0, 0, 0, 0, 0, 0, false, ActionType.CREATE);
        assertEquals("", action.biomeList());
    }

    @Test
    void pendingActionWithNegativeBoosts() {
        PendingAction action = new PendingAction("w", false, null, -10, -20, -30, -40, -50, -60, false, ActionType.CREATE);
        assertEquals(-10, action.d());
        assertEquals(-20, action.g());
        assertEquals(-30, action.i());
    }

    @Test
    void pendingActionWithMaxIntBoosts() {
        PendingAction action = new PendingAction(
                "w", false, null,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                false, ActionType.CREATE);
        assertEquals(Integer.MAX_VALUE, action.d());
    }
}
