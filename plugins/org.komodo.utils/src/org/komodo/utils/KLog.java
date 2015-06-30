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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.constants.SystemConstants;
import org.komodo.spi.logging.KLogger;

/**
 *
 */
public class KLog implements KLogger {

    private class BasicLogger implements KLogger, StringConstants {

        private final Logger logger;

        private String logPath;

        private FileHandler logPathHandler;

        private Level level = Level.ALL;

        public BasicLogger() {
            this.logger = Logger.getLogger(KLogger.class.getName());

            try {
                initLogger();
            } catch (Exception ex) {
                // If this goes wrong then something really has gone wrong!!
                throw new RuntimeException(ex);
            }
        }

        private void initLogger() throws IOException {
            // construct default log file path if necessary
            if ( StringUtils.isBlank( this.logPath ) ) {
                this.logPath = System.getProperty( SystemConstants.ENGINE_DATA_DIR );
                this.logPath += File.separator + SystemConstants.LOG_FILE_NAME;
            }

            // make sure log file exists
            final Path logFilePath = Paths.get( this.logPath );

            if ( !Files.exists( logFilePath ) ) {
                if ( !Files.exists( logFilePath.getParent() ) ) {
                    Files.createDirectories( logFilePath.getParent() );
                }

                Files.createFile( logFilePath );
            }

            dispose();

            //
            // Ensure this and all parent loggers have the
            // same level set. Also, the handlers maintain
            // their own level field so update them as well
            // since they are responsible for publishing the
            // log record.
            //
            Logger log = this.logger;
            while(log != null) {
                log.setLevel(level);
                Handler[] handlers = log.getHandlers();
                if (handlers != null) {
                    for (Handler handler : handlers) {
                        handler.setLevel(level);
                    }
                }
                log = log.getParent();
            }

            logPathHandler = new FileHandler(logPath);

            this.logger.addHandler(logPathHandler);
        }

        @Override
        public void dispose() {
            if (this.logger != null && logPathHandler != null)
                logger.removeHandler(logPathHandler);

            if (logPathHandler != null)
                logPathHandler.close();

            logPathHandler = null;
        }

        @Override
        public String getLogPath() {
            return logPath;
        }

        @Override
        public void setLogPath(String logPath) throws Exception {
            this.logPath = logPath;
            this.logger.removeHandler(logPathHandler);
            initLogger();
        }

        @Override
        public void setLevel(Level level) throws Exception {
            this.level = level;
            initLogger();
        }

        @Override
        public void info(String message, Object... args) {
            logger.log(Level.INFO, message, args);
        }

        @Override
        public void info(String message, Throwable throwable, Object... args) {
            logger.log(Level.INFO, message, throwable);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.logging.KLogger#isInfoEnabled()
         */
        @Override
        public boolean isInfoEnabled() {
            return (this.level == Level.INFO);
        }

        @Override
        public void warn(String message, Object... args) {
            logger.log(Level.WARNING, message, args);
        }

        @Override
        public void warn(String message, Throwable throwable, Object... args) {
            logger.log(Level.WARNING, message, throwable);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.logging.KLogger#isWarnEnabled()
         */
        @Override
        public boolean isWarnEnabled() {
            return (this.level == Level.WARNING);
        }

        @Override
        public void error(String message, Object... args) {
            logger.log(Level.SEVERE, message, args);
        }

        @Override
        public void error(String message, Throwable throwable, Object... args) {
            logger.log(Level.SEVERE, message, throwable);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.logging.KLogger#isErrorEnabled()
         */
        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public void debug(String message, Object... args) {
            logger.log(Level.FINE, message, args);
        }

        @Override
        public void debug(String message, Throwable throwable, Object... args) {
            logger.log(Level.FINE, message, throwable);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.logging.KLogger#isDebugEnabled()
         */
        @Override
        public boolean isDebugEnabled() {
            return (this.level == Level.FINE);
        }

        @Override
        public void trace(String message, Object... args) {
            logger.log(Level.FINEST, message, args);
        }

        @Override
        public void trace(String message, Throwable throwable, Object... args) {
            logger.log(Level.FINEST, message, throwable);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.logging.KLogger#isTraceEnabled()
         */
        @Override
        public boolean isTraceEnabled() {
            return (this.level == Level.FINEST);
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
     * @see org.komodo.spi.logging.KLogger#setLevel(java.util.logging.Level)
     */
    @Override
    public void setLevel(Level level) throws Exception {
        kLogger.setLevel(level);
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

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return this.kLogger.isInfoEnabled();
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

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {
        return this.kLogger.isWarnEnabled();
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

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {
        return this.kLogger.isErrorEnabled();
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

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return this.kLogger.isDebugEnabled();
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

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {
        return this.kLogger.isTraceEnabled();
    }

}
