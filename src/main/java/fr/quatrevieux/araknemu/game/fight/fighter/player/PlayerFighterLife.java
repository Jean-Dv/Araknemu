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

package fr.quatrevieux.araknemu.game.fight.fighter.player;

import fr.quatrevieux.araknemu.game.fight.fighter.BaseFighterLife;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.FighterLife;
import fr.quatrevieux.araknemu.game.world.creature.Life;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Handle life points for {@link PlayerFighter}
 *
 * The life points will be saved when fight started
 */
public final class PlayerFighterLife implements FighterLife {
    private final Life baseLife;
    private final Fighter fighter;

    private @MonotonicNonNull BaseFighterLife delegate;

    public PlayerFighterLife(Life baseLife, Fighter fighter) {
        this.baseLife = baseLife;
        this.fighter = fighter;
    }

    @Override
    public @NonNegative int current() {
        return delegate != null ? delegate.current() : baseLife.current();
    }

    @Override
    public @NonNegative int max() {
        return delegate != null ? delegate.max() : baseLife.max();
    }

    @Override
    public boolean dead() {
        return delegate != null && delegate.dead();
    }

    @Override
    public int alter(Fighter caster, int value) {
        if (delegate == null) {
            throw new IllegalStateException("PlayerFighterLife must be initialized");
        }

        return delegate.alter(caster, value);
    }

    @Override
    public void alterMax(Fighter caster, int value) {
        if (delegate == null) {
            throw new IllegalStateException("PlayerFighterLife must be initialized");
        }

        delegate.alterMax(caster, value);
    }

    @Override
    public void kill(Fighter caster) {
        if (delegate == null) {
            throw new IllegalStateException("PlayerFighterLife must be initialized");
        }

        delegate.kill(caster);
    }

    @Override
    public void resuscitate(Fighter caster, @Positive int value) {
        if (delegate == null) {
            throw new IllegalStateException("PlayerFighterLife must be initialized");
        }

        delegate.resuscitate(caster, value);
    }

    /**
     * Initialise the fighter life when fight is started
     */
    public void init() {
        if (delegate != null) {
            throw new IllegalStateException("Player fighter life is already initialised");
        }

        delegate = new BaseFighterLife(fighter, baseLife.current(), baseLife.max());
    }
}
