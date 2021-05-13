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
 * Copyright (c) 2017-2021 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.admin.executor.argument;

import fr.quatrevieux.araknemu.common.account.Permission;
import fr.quatrevieux.araknemu.game.admin.AdminPerformer;
import fr.quatrevieux.araknemu.game.admin.Command;
import fr.quatrevieux.araknemu.game.admin.CommandParser;
import fr.quatrevieux.araknemu.game.admin.exception.AdminException;
import fr.quatrevieux.araknemu.game.admin.exception.CommandException;
import fr.quatrevieux.araknemu.game.admin.formatter.HelpFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StringListHydratorTest {
    private StringListHydrator hydrator;

    @BeforeEach
    void setUp() {
        hydrator = new StringListHydrator();
    }

    @Test
    void notAList() {
        Command<Object> command = new Command<Object>() {
            @Override
            public String name() {
                return "foo";
            }

            @Override
            public String description() {
                return null;
            }

            @Override
            public HelpFormatter help() {
                return null;
            }

            @Override
            public void execute(AdminPerformer performer, Object arguments) throws AdminException {

            }

            @Override
            public Set<Permission> permissions() {
                return null;
            }
        };

        assertFalse(hydrator.supports(command, null));
        assertFalse(hydrator.supports(command, Arrays.asList("foo", "bar")));
        assertFalse(hydrator.supports(command, new Object()));
    }

    @Test
    void notAListOfString() {
        Command<List<Integer>> command = new Command<List<Integer>>() {
            @Override
            public String name() {
                return "foo";
            }

            @Override
            public String description() {
                return null;
            }

            @Override
            public HelpFormatter help() {
                return null;
            }

            @Override
            public void execute(AdminPerformer performer, List<Integer> arguments) throws AdminException {

            }

            @Override
            public Set<Permission> permissions() {
                return null;
            }
        };

        assertFalse(hydrator.supports(command, null));
        assertFalse(hydrator.supports(command, Arrays.asList(1, 2)));
    }

    @Test
    void implicitListOfString() {
        Command<List<String>> command = new Command<List<String>>() {
            @Override
            public String name() {
                return "foo";
            }

            @Override
            public String description() {
                return null;
            }

            @Override
            public HelpFormatter help() {
                return null;
            }

            @Override
            public void execute(AdminPerformer performer, List<String> arguments) throws AdminException {

            }

            @Override
            public Set<Permission> permissions() {
                return null;
            }
        };

        assertTrue(hydrator.supports(command, null));
        assertTrue(hydrator.supports(command, Arrays.asList("foo", "bar")));

        assertIterableEquals(Arrays.asList("bar", "baz"), hydrator.hydrate(command, null, new CommandParser.Arguments("", "", "", Arrays.asList("foo", "bar", "baz"), null)));

        List<String> param = new ArrayList<>();
        assertSame(param, hydrator.hydrate(command, param, new CommandParser.Arguments("", "", "", Arrays.asList("foo", "bar", "baz"), null)));
        assertIterableEquals(Arrays.asList("bar", "baz"), param);
    }

    @Test
    void explicitListOfString() {
        Command<List<String>> command = new Command<List<String>>() {
            @Override
            public String name() {
                return "foo";
            }

            @Override
            public String description() {
                return null;
            }

            @Override
            public HelpFormatter help() {
                return null;
            }

            @Override
            public void execute(AdminPerformer performer, List<String> arguments) throws AdminException {

            }

            @Override
            public Set<Permission> permissions() {
                return null;
            }

            @Override
            public List<String> createArguments() {
                return new ArrayList<>();
            }
        };

        assertTrue(hydrator.supports(command, null));
        assertTrue(hydrator.supports(command, Arrays.asList("foo", "bar")));

        assertIterableEquals(Arrays.asList("bar", "baz"), hydrator.hydrate(command, null, new CommandParser.Arguments("", "", "", Arrays.asList("foo", "bar", "baz"), null)));

        List<String> param = new ArrayList<>();
        assertSame(param, hydrator.hydrate(command, param, new CommandParser.Arguments("", "", "", Arrays.asList("foo", "bar", "baz"), null)));
        assertIterableEquals(Arrays.asList("bar", "baz"), param);
    }
}
