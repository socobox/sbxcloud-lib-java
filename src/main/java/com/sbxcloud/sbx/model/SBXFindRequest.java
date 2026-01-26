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
        List<LogicalGroup> where,
        Integer page,
        Integer size,
        @JsonProperty("fetch") List<String> fetchModels,
        @JsonProperty("rfetch") List<String> fetchReferencingModels,
        @JsonProperty("autowire") List<String> autowire,
        List<String> keys
) {
    public static Builder builder(String model) {
        return new Builder(model);
    }

    public static class Builder {
        private final String rowModel;
        private List<LogicalGroup> where;
        private Integer page;
        private Integer size;
        private List<String> fetchModels;
        private List<String> fetchReferencingModels;
        private List<String> autowire;
        private List<String> keys;

        private Builder(String rowModel) {
            this.rowModel = rowModel;
        }

        public Builder where(List<LogicalGroup> where) {
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

        public Builder keys(List<String> keys) {
            this.keys = keys;
            return this;
        }

        public SBXFindRequest build() {
            return new SBXFindRequest(rowModel, where, page, size, fetchModels, fetchReferencingModels, autowire, keys);
        }
    }
}
