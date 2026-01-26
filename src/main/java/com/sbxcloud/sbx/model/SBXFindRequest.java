package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for find operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SBXFindRequest(
        @JsonProperty("row_model") String rowModel,
        String domain,
        WhereClause where,
        Integer page,
        Integer size,
        @JsonProperty("fetch") List<String> fetchModels,
        @JsonProperty("rfetch") List<String> fetchReferencingModels,
        @JsonProperty("autowire") List<String> autowire
) {
    public static Builder builder(String model) {
        return new Builder(model);
    }

    /**
     * Returns a copy of this request with the given domain.
     */
    public SBXFindRequest withDomain(int domain) {
        return new SBXFindRequest(rowModel, String.valueOf(domain), where, page, size, fetchModels, fetchReferencingModels, autowire);
    }

    public static class Builder {
        private final String rowModel;
        private String domain;
        private WhereClause where;
        private Integer page;
        private Integer size;
        private List<String> fetchModels;
        private List<String> fetchReferencingModels;
        private List<String> autowire;

        private Builder(String rowModel) {
            this.rowModel = rowModel;
        }

        public Builder domain(int domain) {
            this.domain = String.valueOf(domain);
            return this;
        }

        public Builder where(WhereClause where) {
            this.where = where;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder fetchModels(List<String> fetchModels) {
            this.fetchModels = fetchModels;
            return this;
        }

        public Builder fetchReferencingModels(List<String> fetchReferencingModels) {
            this.fetchReferencingModels = fetchReferencingModels;
            return this;
        }

        public Builder autowire(List<String> autowire) {
            this.autowire = autowire;
            return this;
        }

        public SBXFindRequest build() {
            return new SBXFindRequest(rowModel, domain, where, page, size, fetchModels, fetchReferencingModels, autowire);
        }
    }
}
