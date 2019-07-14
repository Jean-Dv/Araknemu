package fr.quatrevieux.araknemu.game.player;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.exploration.Restrictions;
import fr.quatrevieux.araknemu.game.item.ItemService;
import fr.quatrevieux.araknemu.game.exploration.sprite.PlayerSprite;
import fr.quatrevieux.araknemu.game.world.creature.Sprite;
import fr.quatrevieux.araknemu.game.item.inventory.exception.InventoryException;
import fr.quatrevieux.araknemu.game.world.map.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSpriteTest extends GameBaseCase {
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet
            .pushItemTemplates()
            .pushItemSets()
        ;
    }

    @Test
    void data() throws SQLException, ContainerException {
        GamePlayer player = gamePlayer();

        PlayerSprite sprite = new PlayerSprite(new ExplorationPlayer(player));

        assertEquals(1, sprite.id());
        assertEquals(200, sprite.cell());
        assertEquals("Bob", sprite.name());
        assertEquals(Direction.SOUTH_EAST, sprite.orientation());
        assertEquals(Sprite.Type.PLAYER, sprite.type());
    }

    @Test
    void generateString() throws SQLException, ContainerException {
        assertEquals(
            "200;1;0;1;Bob;1;10^100x100;0;;7b;1c8;315;,,,,;;;;;;8;",
            new PlayerSprite(new ExplorationPlayer(gamePlayer())).toString()
        );
    }

    @Test
    void withRestrictions() throws SQLException, ContainerException {
        ExplorationPlayer exploration = explorationPlayer();
        exploration.player().restrictions().set(
            fr.quatrevieux.araknemu.game.player.Restrictions.Restriction.DENY_CHALLENGE,
            fr.quatrevieux.araknemu.game.player.Restrictions.Restriction.DENY_ASSAULT
        );

        assertEquals(
            "279;1;0;1;Bob;1;10^100x100;0;;7b;1c8;315;,,,,;;;;;;b;",
            new PlayerSprite(exploration).toString()
        );
    }

    @Test
    void withAccessories() throws SQLException, ContainerException, InventoryException {
        gamePlayer().inventory().add(container.get(ItemService.class).create(2411), 1, 6);
        gamePlayer().inventory().add(container.get(ItemService.class).create(2414), 1, 7);
        gamePlayer().inventory().add(container.get(ItemService.class).create(2416), 1, 1);

        assertEquals(
            "200;1;0;1;Bob;1;10^100x100;0;;7b;1c8;315;970,96b,96e,,;;;;;;8;",
            new PlayerSprite(new ExplorationPlayer(gamePlayer())).toString()
        );
    }

    @Test
    void withOrientation() throws SQLException, ContainerException {
        ExplorationPlayer exploration = new ExplorationPlayer(gamePlayer());

        exploration.setOrientation(Direction.WEST);

        assertEquals(
            "200;4;0;1;Bob;1;10^100x100;0;;7b;1c8;315;,,,,;;;;;;8;",
            new PlayerSprite(exploration).toString()
        );
    }
}
