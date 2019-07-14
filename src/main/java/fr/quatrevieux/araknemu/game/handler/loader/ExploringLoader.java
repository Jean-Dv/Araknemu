package fr.quatrevieux.araknemu.game.handler.loader;

import fr.quatrevieux.araknemu.core.di.Container;
import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.game.exploration.exchange.ExchangeFactory;
import fr.quatrevieux.araknemu.game.fight.FightService;
import fr.quatrevieux.araknemu.game.handler.EnsureExploring;
import fr.quatrevieux.araknemu.game.handler.dialog.PerformResponseAction;
import fr.quatrevieux.araknemu.game.handler.dialog.StartDialog;
import fr.quatrevieux.araknemu.game.handler.dialog.StopDialog;
import fr.quatrevieux.araknemu.game.handler.emote.ChangeOrientation;
import fr.quatrevieux.araknemu.game.handler.exchange.AcceptExchange;
import fr.quatrevieux.araknemu.game.handler.exchange.AskExchange;
import fr.quatrevieux.araknemu.game.handler.exchange.LeaveExchange;
import fr.quatrevieux.araknemu.game.handler.exchange.StartExchange;
import fr.quatrevieux.araknemu.game.handler.exchange.movement.SetExchangeItems;
import fr.quatrevieux.araknemu.game.handler.exchange.movement.SetExchangeKamas;
import fr.quatrevieux.araknemu.game.handler.fight.ListFights;
import fr.quatrevieux.araknemu.game.handler.fight.ShowFightDetails;
import fr.quatrevieux.araknemu.game.handler.game.CancelGameAction;
import fr.quatrevieux.araknemu.game.handler.game.LoadExtraInfo;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.in.PacketHandler;

/**
 * Loader for exploration packets
 */
final public class ExploringLoader extends AbstractLoader {
    public ExploringLoader() {
        super(EnsureExploring::new);
    }

    @Override
    public PacketHandler<GameSession, ?>[] handlers(Container container) throws ContainerException {
        return new PacketHandler[] {
            new LoadExtraInfo(container.get(FightService.class)),
            new CancelGameAction(),
            new ListFights(container.get(FightService.class)),
            new ShowFightDetails(container.get(FightService.class)),
            new ChangeOrientation(),
            new StartDialog(),
            new StopDialog(),
            new PerformResponseAction(),
            new AskExchange(container.get(ExchangeFactory.class)),
            new LeaveExchange(),
            new StartExchange(),
            new SetExchangeKamas(),
            new SetExchangeItems(),
            new AcceptExchange(),
        };
    }
}
