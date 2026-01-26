package com.sbxcloud.sbx.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module for SBX entity serialization.
 * <p>
 * Currently registers any custom serializers/deserializers needed for SBX types.
 *
 * <p><b>Note:</b> For records implementing SbxEntity, you still need @JsonProperty
 * annotations for the key and meta fields:
 *
 * <pre>{@code
 * @SbxModel("contact")
 * public record Contact(
 *     @JsonProperty("_KEY") String key,
 *     @JsonProperty("_META") SBXMeta meta,
 *     String name
 * ) implements SbxEntity {}
 * }</pre>
 *
 * The repository pattern eliminates verbosity at the usage site - you only define
 * the entity once, then use clean typed operations everywhere else.
 */
public class SbxModule extends SimpleModule {

    public SbxModule() {
        super("SbxModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        // Reserved for future custom serializers/deserializers
    }
}
