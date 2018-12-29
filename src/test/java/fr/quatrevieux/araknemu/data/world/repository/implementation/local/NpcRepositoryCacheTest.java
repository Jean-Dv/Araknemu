package fr.quatrevieux.araknemu.data.world.repository.implementation.local;

import fr.quatrevieux.araknemu.core.dbal.repository.EntityNotFoundException;
import fr.quatrevieux.araknemu.data.world.entity.environment.npc.Npc;
import fr.quatrevieux.araknemu.data.world.repository.environment.npc.NpcRepository;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class NpcRepositoryCacheTest extends GameBaseCase {
    private NpcRepositoryCache repository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushNpcs();

        repository = new NpcRepositoryCache(
            container.get(NpcRepository.class)
        );
    }

    @Test
    void getNotFound() {
        assertThrows(EntityNotFoundException.class, () -> repository.get(-5));
    }

    @Test
    void getSame() {
        assertSame(
            repository.get(457),
            repository.get(457)
        );
    }

    @Test
    void getUsingEntity() {
        assertSame(
            repository.get(new Npc(457, 0, null, null)),
            repository.get(457)
        );
    }

    @Test
    void hasNotLoaded() {
        assertTrue(repository.has(new Npc(457, 0, null, null)));
        assertFalse(repository.has(new Npc(-1, 0, null, null)));
    }

    @Test
    void hasCached() {
        repository.get(457);
        assertTrue(repository.has(new Npc(457, 0, null, null)));
    }

    @Test
    void all() {
        Collection<Npc> npcs = repository.all();

        assertCount(3, npcs);

        for (Npc npc : npcs) {
            assertSame(npc, repository.get(npc));
        }
    }

    @Test
    void byMapIdNotLoaded() {
        assertEquals(Collections.emptyList(), repository.byMapId(-5));

        assertEquals(Arrays.asList(repository.get(457), repository.get(458)), repository.byMapId(10302));
        assertEquals(Arrays.asList(repository.get(472)), repository.byMapId(10340));
    }

    @Test
    void byMapIdLoaded() {
        repository.all();

        assertEquals(Collections.emptyList(), repository.byMapId(-5));

        assertEquals(Arrays.asList(repository.get(457), repository.get(458)), repository.byMapId(10302));
        assertEquals(Arrays.asList(repository.get(472)), repository.byMapId(10340));
    }
}
