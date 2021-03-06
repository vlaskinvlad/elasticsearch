/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.ingest;

import org.elasticsearch.Version;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.equalTo;

public class SimulatePipelineRequestTests extends ESTestCase {

    public void testSerialization() throws IOException {
        SimulatePipelineRequest request = new SimulatePipelineRequest(new BytesArray(""), XContentType.JSON);
        // Sometimes we set an id
        if (randomBoolean()) {
            request.setId(randomAsciiOfLengthBetween(1, 10));
        }

        // Sometimes we explicitly set a boolean (with whatever value)
        if (randomBoolean()) {
            request.setVerbose(randomBoolean());
        }

        BytesStreamOutput out = new BytesStreamOutput();
        request.writeTo(out);
        StreamInput streamInput = out.bytes().streamInput();
        SimulatePipelineRequest otherRequest = new SimulatePipelineRequest();
        otherRequest.readFrom(streamInput);

        assertThat(otherRequest.getId(), equalTo(request.getId()));
        assertThat(otherRequest.isVerbose(), equalTo(request.isVerbose()));
    }

    public void testSerializationWithXContent() throws IOException {
        SimulatePipelineRequest request =
            new SimulatePipelineRequest(new BytesArray("{}".getBytes(StandardCharsets.UTF_8)), XContentType.JSON);
        assertEquals(XContentType.JSON, request.getXContentType());

        BytesStreamOutput output = new BytesStreamOutput();
        request.writeTo(output);
        StreamInput in = StreamInput.wrap(output.bytes().toBytesRef().bytes);

        SimulatePipelineRequest serialized = new SimulatePipelineRequest();
        serialized.readFrom(in);
        assertEquals(XContentType.JSON, serialized.getXContentType());
        assertEquals("{}", serialized.getSource().utf8ToString());
    }

    public void testSerializationWithXContentBwc() throws IOException {
        final byte[] data = Base64.getDecoder().decode("AAAAAnt9AAA=");
        final Version version = randomFrom(Version.V_5_0_0, Version.V_5_0_1, Version.V_5_0_2,
            Version.V_5_0_3_UNRELEASED, Version.V_5_1_1_UNRELEASED, Version.V_5_1_2_UNRELEASED, Version.V_5_2_0_UNRELEASED);
        try (StreamInput in = StreamInput.wrap(data)) {
            in.setVersion(version);
            SimulatePipelineRequest request = new SimulatePipelineRequest();
            request.readFrom(in);
            assertEquals(XContentType.JSON, request.getXContentType());
            assertEquals("{}", request.getSource().utf8ToString());

            try (BytesStreamOutput out = new BytesStreamOutput()) {
                out.setVersion(version);
                request.writeTo(out);
                assertArrayEquals(data, out.bytes().toBytesRef().bytes);
            }
        }
    }
}
