/*
 * This file is part of Araknemu.
 *
 * Araknemu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Araknemu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Araknemu.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2017-2019 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.fight.turn.action;

import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.FightBaseCase;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.turn.FightTurn;
import fr.quatrevieux.araknemu.game.fight.turn.Turn;
import fr.quatrevieux.araknemu.game.fight.turn.action.event.FightActionStarted;
import fr.quatrevieux.araknemu.game.fight.turn.action.event.FightActionTerminated;
import fr.quatrevieux.araknemu.game.fight.turn.order.AlternateTeamFighterOrder;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionHandlerTest extends FightBaseCase {
    private Fight fight;
    private ActionHandler actionHandler;
    private FightTurn turn;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        fight = createFight();
        fight.start(new AlternateTeamFighterOrder());
        turn = new FightTurn(getFighter(0), fight, Duration.ZERO);
        actionHandler = new ActionHandler(turn, fight);
    }

    @Override
    @AfterEach
    public void tearDown() throws fr.quatrevieux.araknemu.core.di.ContainerException {
        fight.cancel(true);

        super.tearDown();
    }

    @Test
    void startInvalid() {
        assertFalse(actionHandler.start(new FightAction() {
            @Override
            public boolean validate(Turn turn) {
                return false;
            }

            @Override
            public ActionResult start() {
                return null;
            }

            @Override
            public Fighter performer() {
                return null;
            }

            @Override
            public ActionType type() {
                return null;
            }

            @Override
            public Duration duration() {
                return null;
            }
        }));
    }

    @Test
    void startSuccessWillDispatchEvent() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ZERO);

        Mockito.when(result.success()).thenReturn(true);

        AtomicReference<FightActionStarted> ref = new AtomicReference<>();
        fight.dispatcher().add(FightActionStarted.class, ref::set);

        assertTrue(actionHandler.start(action));
        assertSame(action, ref.get().action());
        assertSame(result, ref.get().result());
    }

    @Test
    void startFailedWillDispatchEventAndCallFailed() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);

        Mockito.when(result.success()).thenReturn(false);

        AtomicReference<FightActionStarted> ref = new AtomicReference<>();
        fight.dispatcher().add(FightActionStarted.class, ref::set);

        assertTrue(actionHandler.start(action));
        assertSame(action, ref.get().action());
        assertSame(result, ref.get().result());

        Mockito.verify(action, Mockito.never()).duration();
        Mockito.verify(result).apply(turn);
    }

    @RepeatedIfExceptionsTest
    void startSuccessTerminateActionWhenDurationIsReached() throws InterruptedException {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(100));

        Mockito.when(result.success()).thenReturn(true);

        assertTrue(actionHandler.start(action));

        Mockito.verify(result, Mockito.never()).apply(turn);

        Thread.sleep(150);
        Mockito.verify(result, Mockito.times(1)).apply(turn);
    }

    @Test
    void startWithPendingAction() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(1000));

        Mockito.when(result.success()).thenReturn(true);

        assertTrue(actionHandler.start(action));

        FightAction other = Mockito.mock(FightAction.class);

        assertFalse(actionHandler.start(other));
        Mockito.verify(other, Mockito.never()).start();
    }

    @Test
    void terminateWithoutPendingAction() {
        AtomicReference<FightActionTerminated> ref = new AtomicReference<>();
        fight.dispatcher().add(FightActionTerminated.class, ref::set);

        actionHandler.terminate();

        assertNull(ref.get());
    }

    @Test
    void terminateSuccessWillDispatchEvent() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(1000));

        Mockito.when(result.success()).thenReturn(true);

        actionHandler.start(action);

        AtomicReference<FightActionTerminated> ref = new AtomicReference<>();
        fight.dispatcher().add(FightActionTerminated.class, ref::set);

        actionHandler.terminate();

        Mockito.verify(result).apply(turn);
        assertSame(action, ref.get().action());
    }

    @RepeatedIfExceptionsTest
    void terminateSuccessWillCancelTimer() throws InterruptedException {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(200));

        Mockito.when(result.success()).thenReturn(true);

        actionHandler.start(action);

        actionHandler.terminate();

        Thread.sleep(250);
        Mockito.verify(result, Mockito.times(1)).apply(turn);
    }

    @Test
    void terminateWithTerminationListener() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(1000));

        Mockito.when(result.success()).thenReturn(true);

        actionHandler.start(action);

        AtomicBoolean b = new AtomicBoolean();
        actionHandler.terminated(() -> b.set(true));
        assertFalse(b.get());

        actionHandler.terminate();

        assertTrue(b.get());
    }

    @Test
    void terminateWithException() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(1000));
        Mockito.doThrow(new RuntimeException()).when(result).apply(turn);

        Mockito.when(result.success()).thenReturn(true);

        actionHandler.start(action);

        AtomicBoolean b = new AtomicBoolean();
        actionHandler.terminated(() -> b.set(true));

        assertThrows(Exception.class, () -> actionHandler.terminate());

        assertTrue(b.get());
    }

    @Test
    void terminateWithTerminationListenerWillRemoveOldListeners() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(1000));

        Mockito.when(result.success()).thenReturn(true);

        actionHandler.start(action);

        AtomicInteger i = new AtomicInteger();
        actionHandler.terminated(() -> i.incrementAndGet());
        assertEquals(0, i.get());

        actionHandler.terminate();

        actionHandler.start(action);
        actionHandler.terminate();

        assertEquals(1, i.get());
    }

    @Test
    void terminatedWithoutPendingAction() {
        AtomicBoolean b = new AtomicBoolean();
        actionHandler.terminated(() -> b.set(true));
        assertTrue(b.get());
    }

    @Test
    void terminateOnStoppedFightShouldBeIgnored() {
        FightAction action = Mockito.mock(FightAction.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate(turn)).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofMillis(1000));

        Mockito.when(result.success()).thenReturn(true);

        actionHandler.start(action);
        fight.stop();

        AtomicReference<FightActionTerminated> ref = new AtomicReference<>();
        fight.dispatcher().add(FightActionTerminated.class, ref::set);

        actionHandler.terminate();

        Mockito.verify(result, Mockito.never()).apply(turn);
        assertNull(ref.get());
    }
}
