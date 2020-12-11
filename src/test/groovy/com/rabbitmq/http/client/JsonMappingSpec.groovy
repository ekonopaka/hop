/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rabbitmq.http.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.http.client.domain.QueueInfo
import com.rabbitmq.http.client.domain.UserInfo
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths


class JsonMappingSpec extends Specification {

  static ObjectMapper[] mappers() {
    [Client.createDefaultObjectMapper(), ReactorNettyClient.createDefaultObjectMapper()]
  }

  @Unroll
  def "JSON document for queue with NaN message count should return -1 for message count"() {
    when: "JSON document for queue has no ready messages count field"
    def q = mapper.readValue(JSON_QUEUE_NO_READY_MESSAGES, QueueInfo.class)

    then: "the field value should be -1 in the Java object"
    q.messagesReady == -1

    where:
    mapper << mappers()
  }

  @Unroll
  def "JSON document for queue with defined message count should return appropriate value for message count"() {
    when: "JSON document for queue has a ready messages count field with a value"
    def q = Client.createDefaultObjectMapper().readValue(JSON_QUEUE_SOME_READY_MESSAGES, QueueInfo.class)

    then: "the field value of the Java object should be the same as in the JSON document"
    q.messagesReady == 1000

    where:
    mapper << mappers()
  }

  @Unroll
  def "fields for classic HA queue should be mapped correctly"() {
    when: "JSON document for classic HA queue has details on nodes"
    def q = Client.createDefaultObjectMapper().readValue(JSON_CLASSIC_HA_QUEUE, QueueInfo.class)

    then: "the Java object should be filled accordingly"
    q.type == "classic"
    q.recoverableMirrors == ["rabbit-3@host3", "rabbit-2@host2"]
    q.mirrorNodes == ["rabbit-3@host3", "rabbit-2@host2"]
    q.synchronisedMirrorNodes == ["rabbit-3@host3", "rabbit-2@host2"]

    where:
    mapper << mappers()
  }

  @Unroll
  def "fields for quorum queue should be mapped correctly"() {
    when: "JSON document for quorum queue has details on nodes"
    def q = Client.createDefaultObjectMapper().readValue(JSON_QUORUM_QUEUE, QueueInfo.class)

    then: "the Java object should be filled accordingly"
    q.type == "quorum"
    q.leaderNode == "rabbit-1@host1"
    q.memberNodes == ["rabbit-3@host3", "rabbit-2@host2", "rabbit-1@host1"]

    where:
    mapper << mappers()
  }

  @Unroll
  def "User object with a tag array (RabbitMQ 3.9.0+)"() {
    when: "User object with a tag array"
    def s = readJSONDocumentResource("json/user_with_tags_as_an_array.json")
    def u = mapper.readValue(s, UserInfo.class)

    then: "the field value should be an array in the Java object"
    u.tags == ["administrator"]

    where:
    mapper << mappers()
  }

  @Unroll
  def "User object with a comman-separated tag list (versions prior to 3.9.0)"() {
    when: "User object with a comma-separate tag list"
    def s = readJSONDocumentResource("json/user_with_tags_as_a_comma_separated_list.json")
    def u = mapper.readValue(s, UserInfo.class)

    then: "the field value should be an array in the Java object"
    u.tags == ["administrator", "impersonator"]

    where:
    mapper << mappers()
  }

  def String readJSONDocumentResource(String name) {
    def resource = UserInfo.getClassLoader().getResource(name)
    return new String(Files.readAllBytes(Paths.get(resource.toURI())))
  }

  static final String JSON_QUEUE_NO_READY_MESSAGES =
          "   {\n" +
          "      \"arguments\":{\n" +
          "         \n" +
          "      },\n" +
          "      \"auto_delete\":false,\n" +
          "      \"backing_queue_status\":{\n" +
          "         \"avg_ack_egress_rate\":0.0,\n" +
          "         \"avg_ack_ingress_rate\":0.0,\n" +
          "         \"avg_egress_rate\":0.0,\n" +
          "         \"avg_ingress_rate\":0.0,\n" +
          "         \"delta\":[\n" +
          "            \"delta\",\n" +
          "            \"undefined\",\n" +
          "            0,\n" +
          "            0,\n" +
          "            \"undefined\"\n" +
          "         ],\n" +
          "         \"len\":0,\n" +
          "         \"mode\":\"default\",\n" +
          "         \"next_seq_id\":0,\n" +
          "         \"q1\":0,\n" +
          "         \"q2\":0,\n" +
          "         \"q3\":0,\n" +
          "         \"q4\":0,\n" +
          "         \"target_ram_count\":\"infinity\"\n" +
          "      },\n" +
          "      \"consumer_utilisation\":null,\n" +
          "      \"consumers\":0,\n" +
          "      \"durable\":true,\n" +
          "      \"effective_policy_definition\":{\n" +
          "         \n" +
          "      },\n" +
          "      \"exclusive\":false,\n" +
          "      \"exclusive_consumer_tag\":null,\n" +
          "      \"garbage_collection\":{\n" +
          "         \"fullsweep_after\":65535,\n" +
          "         \"max_heap_size\":0,\n" +
          "         \"min_bin_vheap_size\":46422,\n" +
          "         \"min_heap_size\":233,\n" +
          "         \"minor_gcs\":2\n" +
          "      },\n" +
          "      \"head_message_timestamp\":null,\n" +
          "      \"idle_since\":\"2020-10-08 7:35:55\",\n" +
          "      \"memory\":18260,\n" +
          "      \"message_bytes\":0,\n" +
          "      \"message_bytes_paged_out\":0,\n" +
          "      \"message_bytes_persistent\":0,\n" +
          "      \"message_bytes_ram\":0,\n" +
          "      \"message_bytes_ready\":0,\n" +
          "      \"message_bytes_unacknowledged\":0,\n" +
          "      \"messages\":0,\n" +
          "      \"messages_details\":{\n" +
          "         \"rate\":0.0\n" +
          "      },\n" +
          "      \"messages_paged_out\":0,\n" +
          "      \"messages_persistent\":0,\n" +
          "      \"messages_ram\":0,\n" +
          "      \"messages_ready_details\":{\n" +
          "         \"rate\":0.0\n" +
          "      },\n" +
          "      \"messages_ready_ram\":0,\n" +
          "      \"messages_unacknowledged\":0,\n" +
          "      \"messages_unacknowledged_details\":{\n" +
          "         \"rate\":0.0\n" +
          "      },\n" +
          "      \"messages_unacknowledged_ram\":0,\n" +
          "      \"name\":\"queue1\",\n" +
          "      \"operator_policy\":null,\n" +
          "      \"policy\":null,\n" +
          "      \"recoverable_slaves\":null,\n" +
          "      \"reductions\":4474,\n" +
          "      \"reductions_details\":{\n" +
          "         \"rate\":0.0\n" +
          "      },\n" +
          "      \"single_active_consumer_tag\":null,\n" +
          "      \"state\":\"running\",\n" +
          "      \"type\":\"classic\",\n" +
          "      \"vhost\":\"vh1\"\n" +
          "   }\n"

  static final String JSON_QUEUE_SOME_READY_MESSAGES =
          "   {\n" +
                  "      \"arguments\":{\n" +
                  "         \n" +
                  "      },\n" +
                  "      \"auto_delete\":false,\n" +
                  "      \"backing_queue_status\":{\n" +
                  "         \"avg_ack_egress_rate\":0.0,\n" +
                  "         \"avg_ack_ingress_rate\":0.0,\n" +
                  "         \"avg_egress_rate\":0.0,\n" +
                  "         \"avg_ingress_rate\":0.0,\n" +
                  "         \"delta\":[\n" +
                  "            \"delta\",\n" +
                  "            \"undefined\",\n" +
                  "            0,\n" +
                  "            0,\n" +
                  "            \"undefined\"\n" +
                  "         ],\n" +
                  "         \"len\":0,\n" +
                  "         \"mode\":\"default\",\n" +
                  "         \"next_seq_id\":0,\n" +
                  "         \"q1\":0,\n" +
                  "         \"q2\":0,\n" +
                  "         \"q3\":0,\n" +
                  "         \"q4\":0,\n" +
                  "         \"target_ram_count\":\"infinity\"\n" +
                  "      },\n" +
                  "      \"consumer_utilisation\":null,\n" +
                  "      \"consumers\":0,\n" +
                  "      \"durable\":true,\n" +
                  "      \"effective_policy_definition\":{\n" +
                  "         \n" +
                  "      },\n" +
                  "      \"exclusive\":false,\n" +
                  "      \"exclusive_consumer_tag\":null,\n" +
                  "      \"garbage_collection\":{\n" +
                  "         \"fullsweep_after\":65535,\n" +
                  "         \"max_heap_size\":0,\n" +
                  "         \"min_bin_vheap_size\":46422,\n" +
                  "         \"min_heap_size\":233,\n" +
                  "         \"minor_gcs\":2\n" +
                  "      },\n" +
                  "      \"head_message_timestamp\":null,\n" +
                  "      \"idle_since\":\"2020-10-08 7:35:55\",\n" +
                  "      \"memory\":18260,\n" +
                  "      \"message_bytes\":0,\n" +
                  "      \"message_bytes_paged_out\":0,\n" +
                  "      \"message_bytes_persistent\":0,\n" +
                  "      \"message_bytes_ram\":0,\n" +
                  "      \"message_bytes_ready\":0,\n" +
                  "      \"message_bytes_unacknowledged\":0,\n" +
                  "      \"messages\":0,\n" +
                  "      \"messages_details\":{\n" +
                  "         \"rate\":0.0\n" +
                  "      },\n" +
                  "      \"messages_paged_out\":0,\n" +
                  "      \"messages_persistent\":0,\n" +
                  "      \"messages_ram\":0,\n" +
                  "      \"messages_ready\":1000,\n" +
                  "      \"messages_ready_details\":{\n" +
                  "         \"rate\":0.0\n" +
                  "      },\n" +
                  "      \"messages_ready_ram\":0,\n" +
                  "      \"messages_unacknowledged\":0,\n" +
                  "      \"messages_unacknowledged_details\":{\n" +
                  "         \"rate\":0.0\n" +
                  "      },\n" +
                  "      \"messages_unacknowledged_ram\":0,\n" +
                  "      \"name\":\"queue1\",\n" +
                  "      \"operator_policy\":null,\n" +
                  "      \"policy\":null,\n" +
                  "      \"recoverable_slaves\":null,\n" +
                  "      \"reductions\":4474,\n" +
                  "      \"reductions_details\":{\n" +
                  "         \"rate\":0.0\n" +
                  "      },\n" +
                  "      \"single_active_consumer_tag\":null,\n" +
                  "      \"state\":\"running\",\n" +
                  "      \"type\":\"classic\",\n" +
                  "      \"vhost\":\"vh1\"\n" +
                  "   }\n"

  static final String JSON_CLASSIC_HA_QUEUE = "{\n" +
          "  \"name\": \"ha-classic\",\n" +
          "  \"node\": \"rabbit-1@host1\",\n" +
          "  \"policy\": \"ha\",\n" +
          "  \"recoverable_slaves\": [\n" +
          "    \"rabbit-3@host3\",\n" +
          "    \"rabbit-2@host2\"\n" +
          "  ],\n" +
          "  \"slave_nodes\": [\n" +
          "    \"rabbit-3@host3\",\n" +
          "    \"rabbit-2@host2\"\n" +
          "  ],\n" +
          "  \"state\": \"running\",\n" +
          "  \"synchronised_slave_nodes\": [\n" +
          "    \"rabbit-3@host3\",\n" +
          "    \"rabbit-2@host2\"\n" +
          "  ],\n" +
          "  \"type\": \"classic\",\n" +
          "  \"vhost\": \"/\"\n" +
          "}"

  static final String JSON_QUORUM_QUEUE = "{\n" +
          "  \"leader\": \"rabbit-1@host1\",\n" +
          "  \"members\": [\n" +
          "    \"rabbit-3@host3\",\n" +
          "    \"rabbit-2@host2\",\n" +
          "    \"rabbit-1@host1\"\n" +
          "  ],\n" +
          "  \"name\": \"quorum-queue\",\n" +
          "  \"node\": \"rabbit-1@host1\",\n" +
          "  \"online\": [\n" +
          "    \"rabbit-3@host3\",\n" +
          "    \"rabbit-2@host2\",\n" +
          "    \"rabbit-1@host1\"\n" +
          "  ],\n" +
          "  \"open_files\": {\n" +
          "    \"rabbit-1@host1\": 0,\n" +
          "    \"rabbit-2@host2\": 0,\n" +
          "    \"rabbit-3@host3\": 0\n" +
          "  },\n" +
          "  \"type\": \"quorum\",\n" +
          "  \"vhost\": \"/\"\n" +
          "}"
}
