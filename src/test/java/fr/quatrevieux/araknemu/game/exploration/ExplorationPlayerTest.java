package fr.quatrevieux.araknemu.game.exploration;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.core.event.Listener;
import fr.quatrevieux.araknemu.game.exploration.event.MapChanged;
import fr.quatrevieux.araknemu.game.exploration.event.MapLeaved;
import fr.quatrevieux.araknemu.game.exploration.event.MapJoined;
import fr.quatrevieux.araknemu.game.exploration.event.OrientationChanged;
import fr.quatrevieux.araknemu.game.exploration.interaction.action.BlockingAction;
import fr.quatrevieux.araknemu.game.exploration.interaction.event.PlayerMoveFinished;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMap;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.sprite.PlayerSprite;
import fr.quatrevieux.araknemu.game.player.characteristic.PlayerLife;
import fr.quatrevieux.araknemu.game.player.inventory.PlayerInventory;
import fr.quatrevieux.araknemu.game.world.creature.Operation;
import fr.quatrevieux.araknemu.game.world.map.Direction;
import fr.quatrevieux.araknemu.network.game.out.game.AddSprites;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ExplorationPlayerTest extends GameBaseCase {
    private ExplorationPlayer player;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps();
        player = new ExplorationPlayer(gamePlayer());
        session.setExploration(player);
    }

    @Test
    void sprite() throws SQLException, ContainerException {
        assertEquals(
            new PlayerSprite(new ExplorationPlayer(gamePlayer())).toString(),
            player.sprite().toString()
        );
    }

    @Test
    void join() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);

        AtomicReference<ExplorationMap> ref = new AtomicReference<>();
        Listener<MapJoined> listener = new Listener<MapJoined>() {
            @Override
            public void on(MapJoined event) {
                ref.set(event.map());
            }

            @Override
            public Class<MapJoined> event() {
                return MapJoined.class;
            }
        };

        player.dispatcher().add(listener);
        player.join(map);

        assertSame(map, player.map());
        assertSame(map, ref.get());
    }

    @Test
    void move() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);
        player.join(map);

        AtomicReference<PlayerMoveFinished> ref = new AtomicReference<>();
        map.dispatcher().add(PlayerMoveFinished.class, ref::set);

        player.move(map.get(123), Direction.EAST);

        assertEquals(123, player.cell());
        assertEquals(123, player.position().cell());
        assertEquals(player.orientation(), Direction.EAST);

        assertEquals(123, ref.get().cell().id());
        assertSame(player, ref.get().player());
    }

    @Test
    void leave() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);
        player.join(map);

        AtomicReference<ExplorationMap> ref = new AtomicReference<>();
        Listener<MapLeaved> listener = new Listener<MapLeaved>() {
            @Override
            public void on(MapLeaved event) {
                ref.set(event.map());
            }

            @Override
            public Class<MapLeaved> event() {
                return MapLeaved.class;
            }
        };

        player.dispatcher().add(listener);

        player.leave();

        assertFalse(map.creatures().contains(player));
        assertSame(map, ref.get());
    }

    @Test
    void changeCell() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);
        player.join(map);

        player.changeCell(741);

        assertEquals(741, player.position().cell());

        requestStack.assertLast(
            new AddSprites(
                Collections.singleton(player.sprite())
            )
        );
    }

    @Test
    void changeMap() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);

        AtomicReference<MapJoined> ref1 = new AtomicReference<>();
        AtomicReference<MapChanged> ref2 = new AtomicReference<>();

        player.dispatcher().add(new Listener<MapJoined>() {
            @Override
            public void on(MapJoined event) {
                ref1.set(event);
            }

            @Override
            public Class<MapJoined> event() {
                return MapJoined.class;
            }
        });
        player.dispatcher().add(MapChanged.class, ref2::set);

        try {
            player.changeMap(map, 85);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        assertSame(map, player.map());
        assertSame(map, ref1.get().map());
        assertSame(map, ref2.get().map());
        assertEquals(new Position(10300, 85), player.position());
    }

    @Test
    void inventory() {
        assertInstanceOf(PlayerInventory.class, player.inventory());
    }

    @Test
    void life() {
        assertInstanceOf(PlayerLife.class, player.properties().life());
    }

    @Test
    void interactions() {
        assertFalse(player.interactions().busy());
    }

    @Test
    void player() throws SQLException, ContainerException {
        assertSame(gamePlayer(), player.player());
    }

    @Test
    void register() {
        player.register(session);
        assertSame(player, session.exploration());
    }

    @Test
    void unregister() {
        session.setExploration(player);

        player.unregister(session);

        assertNull(session.exploration());
    }

    @Test
    void unregisterShouldLeaveMap() throws ContainerException {
        session.setExploration(player);

        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);
        player.join(map);

        player.unregister(session);

        assertNull(session.exploration());
        assertFalse(player.map().creatures().contains(player));
    }

    @Test
    void unregisterShouldStopInteractions() {
        session.setExploration(player);
        player.interactions().push(Mockito.mock(BlockingAction.class));

        player.unregister(session);

        assertNull(session.exploration());
        assertFalse(player.interactions().busy());
    }

    @Test
    void restrictions() {
        assertTrue(player.restrictions().canChallenge());
        assertFalse(player.restrictions().canAttack());
        assertFalse(player.restrictions().isTomb());
        assertFalse(player.restrictions().isSlow());
    }

    @Test
    void orientation() throws ContainerException {
        assertEquals(Direction.SOUTH_EAST, player.orientation());

        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);
        player.join(map);

        AtomicReference<OrientationChanged> ref = new AtomicReference<>();
        map.dispatcher().add(OrientationChanged.class, ref::set);

        player.setOrientation(Direction.WEST);
        assertEquals(Direction.WEST, player.orientation());

        assertSame(player, ref.get().player());
        assertSame(Direction.WEST, ref.get().orientation());
    }

    @Test
    void apply() {
        Operation operation = Mockito.mock(Operation.class);

        player.apply(operation);

        Mockito.verify(operation).onExplorationPlayer(player);
    }
}
