package environment;

import java.util.HashMap;

import lexer.Token;

/**
 * A simple representation of an executional environment.
 * 
 * @author Zach Kissel
 */
public class Environment
{
    private final HashMap<String, Object> env;

    /**
     * Sets up the initial environment.
     */
    public Environment()
    {
        env = new HashMap<>();
    }

    /**
     * Returns the environment value associated with a token.
     *
     * @param tok the token to look up the value of.
     * @return the value of {@code tok} in the environment, or null if not present.
     */
    public Object lookup(Token tok)
    {
        return env.get(tok.getValue());
    }

    /**
     * Update the environment such that token {@code tok} has the given value {@code val}.
     *
     * @param tok the token to update.
     * @param val the value to associate with the token.
     */
    public void updateEnvironment(Token tok, Object val)
    {
        String key = tok.getValue();
        if (env.containsKey(key)) {
            env.replace(key, val);
        } else {
            env.put(key, val);
        }
    }

    /**
     * Update the environment by variable name.
     *
     * @param name the variable name.
     * @param value the value to associate with the name.
     */
    public void updateEnvironment(String name, Object value)
    {
        if (env.containsKey(name)) {
            env.replace(name, value);
        } else {
            env.put(name, value);
        }
    }

    /**
     * Makes a copy of the current environment.
     *
     * @return a copy of the environment.
     */
    public Environment copy()
    {
        Environment newEnv = new Environment();
        newEnv.env.putAll(this.env);
        return newEnv;
    }
}
