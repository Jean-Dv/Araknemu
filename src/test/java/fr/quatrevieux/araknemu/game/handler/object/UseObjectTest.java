package fr.quatrevieux.araknemu.game.handler.object;

import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.data.world.entity.environment.npc.Npc;
import fr.quatrevieux.araknemu.data.world.entity.environment.npc.NpcTemplate;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.exploration.interaction.action.ActionType;
import fr.quatrevieux.araknemu.game.exploration.npc.GameNpc;
import fr.quatrevieux.araknemu.game.item.ItemService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.Restrictions;
import fr.quatrevieux.araknemu.game.player.inventory.InventoryEntry;
import fr.quatrevieux.araknemu.game.item.inventory.exception.ItemNotFoundException;
import fr.quatrevieux.araknemu.network.exception.CloseImmediately;
import fr.quatrevieux.araknemu.network.game.in.object.ObjectUseRequest;
import fr.quatrevieux.araknemu.network.game.out.account.Stats;
import fr.quatrevieux.araknemu.network.game.out.basic.Noop;
import fr.quatrevieux.araknemu.network.game.out.game.action.GameActionResponse;
import fr.quatrevieux.araknemu.network.game.out.info.Error;
import fr.quatrevieux.araknemu.network.game.out.info.Information;
import fr.quatrevieux.araknemu.network.game.out.object.DestroyItem;
import fr.quatrevieux.araknemu.network.game.out.object.InventoryWeight;
import fr.quatrevieux.araknemu.network.game.out.object.ItemQuantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UseObjectTest extends GameBaseCase {
    private UseObject handler;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        handler = new UseObject();

        dataSet.pushUsableItems();
        gamePlayer(true);

        explorationPlayer();
        requestStack.clear();
    }

    @Test
    void handleForSelfSuccessWithBoostStatsObject() throws Exception {
        InventoryEntry entry = explorationPlayer().inventory().add(container.get(ItemService.class).create(800));
        requestStack.clear();

        handler.handle(session, new ObjectUseRequest(entry.id(), 0, 0, false));

        requestStack.assertAll(
            new Stats(gamePlayer().properties()),
            new InventoryWeight(gamePlayer()),
            Information.characteristicBoosted(Characteristic.AGILITY, 1),
            new DestroyItem(entry),
            new InventoryWeight(gamePlayer())
        );

        assertThrows(ItemNotFoundException.class, () -> explorationPlayer().inventory().get(entry.id()));
        assertEquals(0, entry.quantity());
        assertEquals(1, explorationPlayer().properties().characteristics().base().get(Characteristic.AGILITY));
    }

    @Test
    void handleDoNothing() throws Exception {
        InventoryEntry entry = explorationPlayer().inventory().add(container.get(ItemService.class).create(468));
        requestStack.clear();

        handler.handle(session, new ObjectUseRequest(entry.id(), 0, 0, false));

        requestStack.assertAll(new Noop());
    }

    @Test
    void handleForTargetPlayer() throws Exception {
        InventoryEntry entry = explorationPlayer().inventory().add(container.get(ItemService.class).create(468));

        GamePlayer other = makeOtherPlayer();
        ExplorationPlayer otherPlayer = new ExplorationPlayer(other);
        explorationPlayer().map().add(otherPlayer);
        other.properties().life().set(10);
        requestStack.clear();

        handler.handle(session, new ObjectUseRequest(entry.id(), otherPlayer.id(), 0, true));

        requestStack.assertAll(
            new DestroyItem(entry),
            new InventoryWeight(gamePlayer())
        );
        assertEquals(20, other.properties().life().current());
        assertEquals(0, entry.quantity());
    }

    @Test
    void handleForTargetCell() throws Exception {
        InventoryEntry entry = explorationPlayer().inventory().add(container.get(ItemService.class).create(2240), 100);
        requestStack.clear();

        handler.handle(session, new ObjectUseRequest(entry.id(), 0, 150, true));

        requestStack.assertAll(
            new GameActionResponse("1", ActionType.FIREWORK, explorationPlayer().id(), "150,2900,11,8,1"),
            new ItemQuantity(entry),
            new InventoryWeight(gamePlayer())
        );
        assertEquals(99, entry.quantity());
    }

    @Test
    void handleForTargetNpc() throws Exception {
        dataSet.pushNpcs();

        InventoryEntry entry = explorationPlayer().inventory().add(container.get(ItemService.class).create(468));

        GameNpc npc = new GameNpc(
            dataSet.refresh(new Npc(457, 0, null, null)),
            dataSet.refresh(new NpcTemplate(848, 0, 0, 0, null, null, null, 0, 0))
        );

        explorationPlayer().map().add(npc);
        requestStack.clear();

        handler.handle(session, new ObjectUseRequest(entry.id(), npc.id(), 0, true));

        requestStack.assertAll(new Noop());
    }

    @Test
    void handleCantUseObject() throws Exception {
        gamePlayer().restrictions().set(Restrictions.Restriction.DENY_USE_OBJECT);
        InventoryEntry entry = explorationPlayer().inventory().add(container.get(ItemService.class).create(800));
        requestStack.clear();

        assertErrorPacket(Error.cantDoOnCurrentState(), () -> handler.handle(session, new ObjectUseRequest(entry.id(), 0, 0, false)));
    }

    @Test
    void functionalSuccess() throws Exception {
        InventoryEntry entry = explorationPlayer().inventory().add(container.get(ItemService.class).create(800));
        requestStack.clear();

        handlePacket(new ObjectUseRequest(entry.id(), 0, 0, false));

        assertEquals(1, explorationPlayer().properties().characteristics().base().get(Characteristic.AGILITY));
    }

    @Test
    void functionalErrorNotExploring() throws Exception {
        gamePlayer().stop(gamePlayer().exploration());

        InventoryEntry entry = gamePlayer().inventory().add(container.get(ItemService.class).create(800));
        requestStack.clear();

        assertThrows(CloseImmediately.class, () -> handlePacket(new ObjectUseRequest(entry.id(), 0, 0, false)));
        assertEquals(1, entry.quantity());
    }
}
