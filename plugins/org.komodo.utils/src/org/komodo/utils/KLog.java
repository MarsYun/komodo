/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.logging.KLogger;

/**
 *
 */
public class KLog implements KLogger {

    private class BasicLogger implements KLogger, StringConstants {

        private final Logger logger;

        private String logPath = System.getProperty("user.home") + //$NON-NLS-1$
                                                        File.separator + DOT_KOMODO + File.separator + KOMODO + DOT + LOG;

        private FileHandler logPathHandler;

        public BasicLogger() {
            this.logger = Logger.getLogger(KLogger.class.getName());

            try {
                initHandler();
            } catch (Exception ex) {
                // If this goes wrong then something really has gone wrong!!
                throw new RuntimeException(ex);
            }
        }

        private void initHandler() throws IOException {
            logPathHandler = new FileHandler(logPath);
            this.logger.addHandler(logPathHandler);
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public String getLogPath() {
            return logPath;
        }

        @Override
        public void setLogPath(String logPath) throws Exception {
            this.logPath = logPath;
            this.logger.removeHandler(logPathHandler);
            initHandler();
        }

        @Override
        public void info(String message, Object... args) {
            logger.log(Level.INFO, message, args);
        }

        @Override
        public void info(String message, Throwable throwable, Object... args) {
            logger.log(Level.INFO, message, throwable);
        }

        @Override
        public void warn(String message, Object... args) {
            logger.log(Level.WARNING, message, args);
        }

        @Override
        public void warn(String message, Throwable throwable, Object... args) {
            logger.log(Level.WARNING, message, throwable);
        }

        @Override
        public void error(String message, Object... args) {
            logger.log(Level.SEVERE, message, args);
        }

        @Override
        public void error(String message, Throwable throwable, Object... args) {
            logger.log(Level.SEVERE, message, throwable);
        }

        @Override
        public void debug(String message, Object... args) {
            logger.log(Level.FINE, message, args);
        }

        @Override
        public void debug(String message, Throwable throwable, Object... args) {
            logger.log(Level.FINE, message, throwable);
        }

        @Override
        public void trace(String message, Object... args) {
            logger.log(Level.FINEST, message, args);
        }

        @Override
        public void trace(String message, Throwable throwable, Object... args) {
            logger.log(Level.FINE, message, throwable);
        }
    }

    private static KLog instance;

    /**
     * @return singleton instance of this logger
     */
    public static KLog getLogger() {
        if (instance == null)
            instance = new KLog();

        return instance;
    }

    private final KLogger kLogger;

    /**
     * 
     */
    private KLog() {
        ServiceLoader<KLogger> loader = ServiceLoader.load(KLogger.class);
        Iterator<KLogger> loaderIter = loader.iterator();
        if (loaderIter.hasNext()) {
            this.kLogger = loaderIter.next();
        } else {
            // Fallback to basic java.util.logger implementation
            this.kLogger = new BasicLogger();
        }
    }

    @Override
    public void dispose() {
        kLogger.dispose();
    }

    @Override
    public String getLogPath() throws Exception {
        return kLogger.getLogPath();
    }

    @Override
    public void setLogPath(String logPath) throws Exception {
        kLogger.setLogPath(logPath);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#info(java.lang.String, java.lang.Object[])
     */
    @Override
    public void info(String message, Object... args) {
        kLogger.info(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#info(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public void info(String message, Throwable throwable, Object... args) {
        kLogger.info(message, throwable, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#warn(java.lang.String, java.lang.Object[])
     */
    @Override
    public void warn(String message, Object... args) {
        kLogger.warn(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#warn(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public void warn(String message, Throwable throwable, Object... args) {
        kLogger.warn(message, throwable, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#error(java.lang.String, java.lang.Object[])
     */
    @Override
    public void error(String message, Object... args) {
        kLogger.error(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#error(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public void error(String message, Throwable throwable, Object... args) {
        kLogger.error(message, throwable, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#debug(java.lang.String, java.lang.Object[])
     */
    @Override
    public void debug(String message, Object... args) {
        kLogger.debug(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#debug(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public void debug(String message, Throwable throwable, Object... args) {
        kLogger.debug(message, throwable, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#trace(java.lang.String, java.lang.Object[])
     */
    @Override
    public void trace(String message, Object... args) {
        kLogger.trace(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#trace(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public void trace(String message, Throwable throwable, Object... args) {
        kLogger.trace(message, throwable, args);
    }

}
