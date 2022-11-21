/*
 * Copyright 2022 Cognitive Scale, Inc. All Rights Reserved.
 *
 *  See LICENSE.txt for details.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.c12e.cortex.examples.aggregate;

import com.c12e.cortex.phoenix.*;
import com.c12e.cortex.phoenix.spec.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.*;

public class ProfileSchemaDeserializer extends StdDeserializer<ProfileSchema> {

    public ProfileSchemaDeserializer() {
        this(null);
    }

    public ProfileSchemaDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ProfileSchema deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        List<AttributeSpec> attributes = Arrays.asList(ctxt.readTreeAsValue(node.get("customAttributes"), CustomAttributeSpec[].class));
        attributes.addAll(Arrays.asList(ctxt.readTreeAsValue(node.get("bucketAttributes"), BucketAttributeSpec[].class)));

        return new ProfileSchema(
                node.get("project").asText(),
                node.get("name").asText(),
                node.has("title") ? node.get("title").asText(null) : null,
                node.has("description") ? node.get("description").asText(null) : null,
                ctxt.readTreeAsValue(node.get("names"), ProfileNames.class),
                ctxt.readTreeAsValue(node.get("primarySource"), DataSourceSelection.class),
                Arrays.asList(ctxt.readTreeAsValue(node.get("joins"), JoinSourceSelection[].class)),
                node.has("userId") ? node.get("userId").asText(null) : null,
                attributes,
                Arrays.asList(ctxt.readTreeAsValue(node.get("attributeTags"), AttributeTag[].class))
        );
    }
}