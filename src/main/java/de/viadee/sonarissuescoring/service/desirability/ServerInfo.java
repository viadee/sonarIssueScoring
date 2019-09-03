package de.viadee.sonarissuescoring.service.desirability;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * Location / authentication information for external servers.
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonDeserialize
public final class ServerInfo {
    private final String url;
    private final String user;
    private final String password;

    private ServerInfo(String url, @Nullable String user, @Nullable String password) {
        this.url = Objects.requireNonNull(url, "url");
        this.user = user;
        this.password = password;
    }

    @JsonCreator public static ServerInfo of(String url, @Nullable String user, @Nullable String password) {
        return new ServerInfo(url, user, password);
    }

    public static ServerInfo anonymous(String url) {return of(url, null, null);}

    public String url() {
        return url;
    }

    @Nullable public String user() {
        return user;
    }

    @Nullable public String password() {
        return password;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ServerInfo that = (ServerInfo) o;
        return url.equals(that.url) && Objects.equals(user, that.user) && Objects.equals(password, that.password);
    }

    @Override public int hashCode() {
        return Objects.hash(url, user, password);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("ServerInfo").omitNullValues().add("url", url).add("user", user).toString();
    }
}
