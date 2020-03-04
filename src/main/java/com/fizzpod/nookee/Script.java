package com.fizzpod.nookee;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerErrorException;

public class Script {

    private final String script;
    private final ScriptEngine engine;
    private static final Logger LOGGER = LoggerFactory.getLogger(Script.class);

    public Script(String script, ScriptEngine engine) {
        this.script = script;
        this.engine = engine;
    }

    public String getScript() {
        return script;
    }

    public Object run(String message) {
        engine.put("request", message);
        try {
            return engine.eval(script);
        } catch (ScriptException e) {
            LOGGER.error("Error running scipt", e);
            throw new ServerErrorException("Could not fulfil request.", e);
        }
    }

}