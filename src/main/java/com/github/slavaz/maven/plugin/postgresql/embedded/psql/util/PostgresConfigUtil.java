package com.github.slavaz.maven.plugin.postgresql.embedded.psql.util;

import com.github.slavaz.maven.plugin.postgresql.embedded.psql.IPgInstanceProcessData;
import com.github.slavaz.maven.plugin.postgresql.embedded.psql.PgVersion;
import de.flapdoodle.embed.process.distribution.IVersion;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.IOException;
import java.util.Collection;

import static de.flapdoodle.embed.process.runtime.Network.getLocalHost;

public class PostgresConfigUtil {

    public static PostgresConfig getPostgresConfig(final IPgInstanceProcessData pgInstanceProcessData) throws
            IOException {

        final AbstractPostgresConfig.Storage storage = getStorage(pgInstanceProcessData);
        final AbstractPostgresConfig.Credentials creds = getCredentials(pgInstanceProcessData);
        final IVersion version = getVersion(pgInstanceProcessData);
        final PostgresConfig config = getConfig(pgInstanceProcessData, storage, creds, version);

        config.getAdditionalInitDbParams().addAll(getCharsetParameters(pgInstanceProcessData));

        return config;
    }

    private static Collection<String> getCharsetParameters(IPgInstanceProcessData pgInstanceProcessData) {
        return new CharsetParameterList(pgInstanceProcessData).get();
    }

    private static PostgresConfig getConfig(IPgInstanceProcessData pgInstanceProcessData, AbstractPostgresConfig
            .Storage storage, AbstractPostgresConfig.Credentials creds, IVersion version) throws IOException {

        return new PostgresConfig(version, getNet(pgInstanceProcessData), storage, new AbstractPostgresConfig
                .Timeout(), creds);
    }

    private static AbstractPostgresConfig.Credentials getCredentials(IPgInstanceProcessData pgInstanceProcessData) {

        return new AbstractPostgresConfig.Credentials(
                pgInstanceProcessData.getUserName(), pgInstanceProcessData.getPassword());
    }

    private static AbstractPostgresConfig.Storage getStorage(IPgInstanceProcessData pgInstanceProcessData) throws
            IOException {

        return new AbstractPostgresConfig.Storage(
                pgInstanceProcessData.getDbName(), pgInstanceProcessData.getPgDatabaseDir());
    }

    private static IVersion getVersion(IPgInstanceProcessData pgInstanceProcessData) {

        return PgVersion.get(pgInstanceProcessData.getPgServerVersion());
    }

    private static AbstractPostgresConfig.Net getNet(IPgInstanceProcessData pgInstanceProcessData) throws IOException {

        String host = "".equals(pgInstanceProcessData.getPgHost()) ? getLocalHost().getHostAddress() :
                pgInstanceProcessData.getPgHost();
        return new AbstractPostgresConfig.Net(host, pgInstanceProcessData.getPgPort());
    }
}
