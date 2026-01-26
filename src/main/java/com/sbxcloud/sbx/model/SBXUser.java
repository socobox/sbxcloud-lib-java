package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * SBX user information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SBXUser(
        Integer id,
        String name,
        String code,
        String email,
        String login,
        String role,
        String domain,
        @JsonProperty("domain_id") Integer domainId,
        @JsonProperty("membership_role") String membershipRole,
        @JsonProperty("member_of") List<Membership> memberOf,
        @JsonProperty("home_folder_key") String homeFolderKey
) {
    /**
     * User membership in a domain.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Membership(
            @JsonProperty("domain_id") Integer domainId,
            String domain,
            String role
    ) {
    }
}
