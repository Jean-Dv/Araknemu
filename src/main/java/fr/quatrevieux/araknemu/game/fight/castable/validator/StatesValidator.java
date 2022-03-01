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

package fr.quatrevieux.araknemu.game.fight.castable.validator;

import fr.quatrevieux.araknemu.game.fight.castable.Castable;
import fr.quatrevieux.araknemu.game.fight.fighter.States;
import fr.quatrevieux.araknemu.game.fight.map.FightCell;
import fr.quatrevieux.araknemu.game.fight.turn.Turn;
import fr.quatrevieux.araknemu.network.game.out.info.Error;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Validate fighter states
 */
public final class StatesValidator implements CastConstraintValidator {
    @Override
    public boolean check(Turn turn, Castable castable, FightCell target) {
        final States states = turn.fighter().states();

        return
            states.hasAll(castable.constraints().requiredStates())
            && !states.hasOne(castable.constraints().forbiddenStates())
        ;
    }

    @Override
    public @Nullable Error validate(Turn turn, Castable castable, FightCell target) {
        return check(turn, castable, target) ? null : Error.cantCastBadState();
    }
}
