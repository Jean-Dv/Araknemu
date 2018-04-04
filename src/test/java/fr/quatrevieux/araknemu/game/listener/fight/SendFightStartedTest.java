package fr.quatrevieux.araknemu.game.listener.fight;

import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.fight.FightService;
import fr.quatrevieux.araknemu.game.fight.builder.ChallengeBuilder;
import fr.quatrevieux.araknemu.game.fight.event.FightStarted;
import fr.quatrevieux.araknemu.network.game.out.fight.BeginFight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SendFightStartedTest extends GameBaseCase {
    private SendFightStarted listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps();

        listener = new SendFightStarted(
            container.get(FightService.class).handler(ChallengeBuilder.class).start(
                builder -> {
                    try {
                        builder
                            .fighter(gamePlayer(true))
                            .fighter(makeOtherPlayer())
                            .map(container.get(ExplorationMapService.class).load(10340))
                        ;
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail(e);
                    }
                }
            )
        );
    }

    @Test
    void onFightStarted() {
        listener.on(new FightStarted());

        requestStack.assertLast(new BeginFight());
    }
}