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
 * Copyright (c) 2017-2020 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.network.game;

import fr.quatrevieux.araknemu.common.session.SessionLog;
import fr.quatrevieux.araknemu.core.event.Dispatcher;
import fr.quatrevieux.araknemu.core.network.session.AbstractDelegatedSession;
import fr.quatrevieux.araknemu.core.network.session.Session;
import fr.quatrevieux.araknemu.game.account.GameAccount;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;
import fr.quatrevieux.araknemu.game.fight.spectator.Spectator;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.AccountSession;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import java.util.Optional;

/**
 * Session wrapper for game server
 */
public final class GameSession extends AbstractDelegatedSession implements AccountSession<GameAccount>, Dispatcher {
    private @Nullable GameAccount account;
    private @Nullable GamePlayer player;
    private @Nullable ExplorationPlayer exploration;
    private @Nullable PlayerFighter fighter;
    private @Nullable Spectator spectator;
    private @MonotonicNonNull SessionLog log;

    public GameSession(Session session) {
        super(session);
    }

    @Override
    @EnsuresNonNull({"account()", "this.account"})
    @SuppressWarnings("contracts.postcondition")
    public void attach(GameAccount account) {
        this.account = account;
    }

    @Override
    @Pure
    public @Nullable GameAccount account() {
        return account;
    }

    @Override
    @Pure
    @EnsuresNonNullIf(expression = {"account()", "this.account"}, result = true)
    @SuppressWarnings({"contracts.postcondition", "contracts.conditional.postcondition.true.override", "contracts.conditional.postcondition"})
    public boolean isLogged() {
        return account != null;
    }

    @Override
    public @Nullable GameAccount detach() {
        return account = null;
    }

    /**
     * Set the logged player
     *
     * @throws IllegalStateException When a player is already set
     */
    public void setPlayer(@Nullable GamePlayer player) {
        if (this.player != null && player != null) {
            throw new IllegalStateException("A player is already loaded");
        }

        this.player = player;
    }

    /**
     * Get the logged player
     *
     * @return The player instance, or null is not in game
     */
    public @Nullable GamePlayer player() {
        return player;
    }

    /**
     * Get the exploration player
     *
     * @return The player instance, or null if not on exploration
     */
    public @Nullable ExplorationPlayer exploration() {
        return exploration;
    }

    /**
     * Set the exploration player
     */
    public void setExploration(@Nullable ExplorationPlayer exploration) {
        this.exploration = exploration;
    }

    /**
     * Get the fighter
     *
     * @return The fighter or null is not fighting
     */
    public @Nullable PlayerFighter fighter() {
        return fighter;
    }

    /**
     * Set the fighter
     */
    public void setFighter(@Nullable PlayerFighter fighter) {
        this.fighter = fighter;
    }

    /**
     * Get the current active spectator session
     */
    public @Nullable Spectator spectator() {
        return spectator;
    }

    /**
     * Define the spectator session
     */
    public void setSpectator(@Nullable Spectator spectator) {
        this.spectator = spectator;
    }

    /**
     * Get the session log
     */
    public Optional<SessionLog> log() {
        return Optional.ofNullable(log);
    }

    /**
     * Define the current session log related to the current session
     */
    public void setLog(SessionLog log) {
        this.log = log;
    }

    @Override
    public void dispatch(Object event) {
        if (player != null) {
            player.dispatcher().dispatch(event);
        }

        if (exploration != null) {
            exploration.dispatcher().dispatch(event);
        }

        if (fighter != null) {
            fighter.dispatcher().dispatch(event);
        }

        if (spectator != null) {
            spectator.dispatcher().dispatch(event);
        }
    }

    @Override
    public String toString() {
        String str = "ip=" + channel().address().getAddress().getHostAddress();

        if (account != null) {
            str += "; account=" + account.id();
        }

        if (player != null) {
            str += "; player=" + player.id() + "; position=" + player.position();
        }

        if (exploration != null) {
            str += "; state=exploring";
        }

        if (fighter != null) {
            str += "; state=fighting";
        }

        if (spectator != null) {
            str += "; state=spectator";
        }

        return str;
    }
}
