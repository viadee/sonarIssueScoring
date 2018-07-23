package de.viadee.sonarIssueScoring.service.desirability;

import javax.annotation.Nullable;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;

/**
 * Location / authentication information for external servers.
 */
@SuppressWarnings("ClassReferencesSubclass")
@Immutable
@JsonDeserialize
@ImmutableStyle
public abstract class BaseServerInfo {
    public abstract String url();

    @Nullable
    public abstract String user();

    @Nullable
    public abstract String password();

    public static ServerInfo anonymous(String url) {return ServerInfo.of(url, null, null);}

    @Override
    public String toString() { // Hide passwords
        return "ServerInfo[url=" + url() + ", user=" + user() + ", password" + (password() == null ? "==" : "!=") + "null]";
    }
}
