package de.viadee.sonarIssueScoring.service.desirability;

import javax.annotation.Nullable;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Redacted;

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
    @Redacted
    public abstract String password();

    public static ServerInfo anonymous(String url) {return ServerInfo.of(url, null, null);}
}
