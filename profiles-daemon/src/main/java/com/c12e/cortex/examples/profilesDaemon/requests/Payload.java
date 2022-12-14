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

package com.c12e.cortex.examples.profilesDaemon.requests;

import java.util.Map;

public class Payload {
    private Map<String, String> payload;

    public Map<String, String> getPayload() {
        if(payload == null){
            return Map.of();
        } else {
            return payload;
        }
    }

    public void setPayload(Map<String, String> payload) {
        this.payload = payload;
    }
}

