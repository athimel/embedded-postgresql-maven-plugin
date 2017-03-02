package com.github.slavaz.maven.plugin.postgresql.embedded.psql;

import de.flapdoodle.embed.process.distribution.IVersion;
import org.apache.commons.lang3.StringUtils;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Locale;

import static de.flapdoodle.embed.process.runtime.Network.getLocalHost;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Created by slavaz on 13/02/17.
 */
public class PgInstanceManager {

    private IPgInstanceProcessData pgInstanceProcessData = PgInstanceProcessData.getInstance();

    public void start() throws IOException {

        final PostgresStarter<PostgresExecutable, PostgresProcess> postgresStarter =
                PostgresStarter.getDefaultInstance();

        final PostgresConfig postgresConfig = getPostgresConfig();

        PostgresExecutable postgresExecutable = postgresStarter.prepare(postgresConfig);

        pgInstanceProcessData.setProcess(postgresExecutable.start());
    }

    public void stop() {
        final PostgresProcess process = pgInstanceProcessData.getProcess();

        process.stop();
    }

    private PostgresConfig getPostgresConfig() throws IOException {

        final AbstractPostgresConfig.Storage storage = new AbstractPostgresConfig.Storage(
                pgInstanceProcessData.getDbName(), pgInstanceProcessData.getPgDatabaseDir());

        final AbstractPostgresConfig.Credentials creds = new AbstractPostgresConfig.Credentials(
                pgInstanceProcessData.getUserName(), pgInstanceProcessData.getPassword());

        final IVersion version = getVersion();

        final PostgresConfig config =
                new PostgresConfig(version, getNet(), storage, new AbstractPostgresConfig.Timeout(), creds);

        config.getAdditionalInitDbParams()
                .addAll(new CharsetParametersList(pgInstanceProcessData).get());

        return config;
    }

    private AbstractPostgresConfig.Net getNet() throws IOException {
        return new AbstractPostgresConfig.Net(getLocalHost().getHostAddress(), pgInstanceProcessData.getPgPort());
    }

    private IVersion getVersion() {
        return PgVersion.get(pgInstanceProcessData.getPgServerVersion());
    }

    static class CharsetParametersList {
        private final static String NO_PARAMETERS = "no";

        private final String charsetName;
        private final String localeName;

        CharsetParametersList(final IPgInstanceProcessData pgInstanceProcess) {
            charsetName = calculateCharset(pgInstanceProcess);
            localeName = calculateLocale(pgInstanceProcess);
        }

        Collection<String> get() {
            if (NO_PARAMETERS.equals(localeName) || NO_PARAMETERS.equals(charsetName)) {
                return emptyList();
            }

            final String lc = localeName + "." + charsetName;
            return asList("-E", charsetName, "--locale=" + lc, "--lc-collate=" + lc, "--lc-ctype=" + lc);
        }

        private String calculateCharset(final IPgInstanceProcessData iPgInstanceProcessData) {
            if (StringUtils.isEmpty(iPgInstanceProcessData.getPgCharset())) {
                return Charset.defaultCharset()
                        .name();
            }
            return iPgInstanceProcessData.getPgCharset();
        }

        private String calculateLocale(final IPgInstanceProcessData iPgInstanceProcessData) {
            if (StringUtils.isEmpty(iPgInstanceProcessData.getPgLocale())) {
                return Locale.getDefault()
                        .toString();
            }
            return iPgInstanceProcessData.getPgLocale();
        }

    }
}
