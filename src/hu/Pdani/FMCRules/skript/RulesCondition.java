package hu.Pdani.FMCRules.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import hu.Pdani.FMCRules.FMCRulesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Skript condition to check if the user accepted the rules or not
 * @since 1.0
 */
public class RulesCondition extends Condition {
    static {
        Skript.registerCondition(RulesCondition.class, "%player% accept[ed] rules");
    }

    private Expression<Player> player;

    @Override
    public String toString(Event event, boolean debug) {
        return player.toString(event, debug) + " accept[ed] rules";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        Player p = player.getSingle(event);
        return FMCRulesPlugin.getUserStatus(p);
    }
}
