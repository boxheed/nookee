package com.fizzpod.nookee;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

@RestController
@RequestMapping("/api")
public class HookController {

	private final Logger LOGGER = LoggerFactory.getLogger(HookController.class);

    @Value("${hooks.root}")
    private final String hookRoot = "./hooks";

    private final ScriptResolver scriptResolver;

    public HookController() {
        this.scriptResolver = new ScriptResolver(new File(hookRoot));
    }

    @RequestMapping(path = "/**", method = RequestMethod.GET)
    public ResponseEntity<?> doGet(final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.debug("GET {}", path);
        final Object response = invokeHook(path, null, RequestMethod.GET);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.POST)
    public ResponseEntity<?> doPost(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message, RequestMethod.POST);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.PUT)
    public ResponseEntity<?> doPut(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message, RequestMethod.PUT);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.PATCH)
    public ResponseEntity<?> doPatch(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message, RequestMethod.PATCH);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.DELETE)
    public ResponseEntity<?> doDelete(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message, RequestMethod.DELETE);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    private Object invokeHook(final String path, final String message, final RequestMethod verb) {
        Object result = null;
        Script script = this.scriptResolver.getScript(path, verb);
        if(script != null) {
            result = script.run(message);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return result;
    }
	
}
