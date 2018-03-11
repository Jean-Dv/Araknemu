package fr.quatrevieux.araknemu.data.living.repository.implementation.sql;

import fr.quatrevieux.araknemu.core.dbal.repository.EntityNotFoundException;
import fr.quatrevieux.araknemu.data.living.entity.player.Player;
import fr.quatrevieux.araknemu.data.living.entity.player.PlayerSpell;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSpellRepositoryTest extends GameBaseCase {
    private PlayerSpellRepository repository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.use(PlayerSpell.class);

        repository = new PlayerSpellRepository(
            app.database().get("game")
        );
    }

    @Test
    void getNotFound() {
        assertThrows(EntityNotFoundException.class, () -> repository.get(new PlayerSpell(123, 0, false, 0, 'e')));
    }

    @Test
    void addAndGet() {
        PlayerSpell spell = repository.add(
            new PlayerSpell(1, 3, true, 5, 'd')
        );

        spell = repository.get(spell);

        assertEquals(1, spell.playerId());
        assertEquals(3, spell.spellId());
        assertTrue(spell.classSpell());
        assertEquals('d', spell.position());
        assertEquals(5, spell.level());
    }

    @Test
    void has() {
        PlayerSpell spell = repository.add(
            new PlayerSpell(1, 3, true, 5, 'd')
        );

        assertTrue(repository.has(spell));
        assertFalse(repository.has(new PlayerSpell(0, 0, false, 0, 'a')));
    }

    @Test
    void addForReplace() {
        PlayerSpell spell = repository.add(
            new PlayerSpell(1, 3, true, 1, 'd')
        );

        spell.setLevel(5);
        spell.setPosition('c');

        repository.add(spell);

        spell = repository.get(spell);

        assertEquals(1, spell.playerId());
        assertEquals(3, spell.spellId());
        assertTrue(spell.classSpell());
        assertEquals(5, spell.level());
        assertEquals('c', spell.position());
    }

    @Test
    void delete() {
        PlayerSpell spell = repository.add(
            new PlayerSpell(1, 3, true, 1, 'd')
        );

        repository.delete(spell);

        assertFalse(repository.has(spell));
    }

    @Test
    void deleteNotFound() {
        assertThrows(EntityNotFoundException.class, () -> repository.delete(
            new PlayerSpell(1, 3, true, 1, 'd')
        ));
    }

    @Test
    void byPlayer() {
        repository.add(new PlayerSpell(1, 3, true, 1, 'd'));
        repository.add(new PlayerSpell(1, 9, true, 1, '_'));
        repository.add(new PlayerSpell(8, 9, true, 1, '_'));


        Collection<PlayerSpell> items = repository.byPlayer(new Player(1));

        assertCount(2, items);
    }
}
