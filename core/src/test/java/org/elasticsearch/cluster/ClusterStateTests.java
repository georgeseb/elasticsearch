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
package org.elasticsearch.cluster;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.transport.DummyTransportAddress;
import org.elasticsearch.test.ESTestCase;

import static org.hamcrest.Matchers.equalTo;

public class ClusterStateTests extends ESTestCase {

    public void testSupersedes() {
        final DiscoveryNode node1 = new DiscoveryNode("node1", DummyTransportAddress.INSTANCE, Version.CURRENT);
        final DiscoveryNode node2 = new DiscoveryNode("node2", DummyTransportAddress.INSTANCE, Version.CURRENT);
        final DiscoveryNodes nodes = DiscoveryNodes.builder().put(node1).put(node2).build();
        ClusterState noMaster1 = ClusterState.builder(ClusterName.DEFAULT).version(randomInt(5)).nodes(nodes).build();
        ClusterState noMaster2 = ClusterState.builder(ClusterName.DEFAULT).version(randomInt(5)).nodes(nodes).build();
        ClusterState withMaster1a = ClusterState.builder(ClusterName.DEFAULT).version(randomInt(5)).nodes(DiscoveryNodes.builder(nodes).masterNodeId(node1.id())).build();
        ClusterState withMaster1b = ClusterState.builder(ClusterName.DEFAULT).version(randomInt(5)).nodes(DiscoveryNodes.builder(nodes).masterNodeId(node1.id())).build();
        ClusterState withMaster2 = ClusterState.builder(ClusterName.DEFAULT).version(randomInt(5)).nodes(DiscoveryNodes.builder(nodes).masterNodeId(node2.id())).build();

        // states with no master should never supersede anything
        assertFalse(noMaster1.supersedes(noMaster2));
        assertFalse(noMaster1.supersedes(withMaster1a));

        // states should never supersede states from another master
        assertFalse(withMaster1a.supersedes(withMaster2));
        assertFalse(withMaster1a.supersedes(noMaster1));

        // state from the same master compare by version
        assertThat(withMaster1a.supersedes(withMaster1b), equalTo(withMaster1a.version() > withMaster1b.version()));

    }
}
