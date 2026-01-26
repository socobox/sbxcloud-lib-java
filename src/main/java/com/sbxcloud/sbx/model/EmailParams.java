package com.sbxcloud.sbx.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Parameters for sending emails.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailParams(
        String from,
        List<String> to,
        List<String> cc,
        List<String> bcc,
        String subject,
        Map<String, Object> data,
        @JsonProperty("template_key") String templateKey,
        String template
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String from;
        private List<String> to;
        private List<String> cc;
        private List<String> bcc;
        private String subject;
        private Map<String, Object> data;
        private String templateKey;
        private String template;

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(List<String> to) {
            this.to = to;
            return this;
        }

        public Builder to(String... to) {
            this.to = List.of(to);
            return this;
        }

        public Builder cc(List<String> cc) {
            this.cc = cc;
            return this;
        }

        public Builder bcc(List<String> bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public Builder templateKey(String templateKey) {
            this.templateKey = templateKey;
            return this;
        }

        public Builder template(String template) {
            this.template = template;
            return this;
        }

        public EmailParams build() {
            return new EmailParams(from, to, cc, bcc, subject, data, templateKey, template);
        }
    }
}
