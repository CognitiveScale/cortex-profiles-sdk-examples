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

import io.prometheus.client.Gauge;

public class Metrics {
    static final Gauge KPIGauge = Gauge.build()
            .name("kpi").help("Profile KPI").labelNames("KPI").register();

    static void setKPI(Long number) {
        KPIGauge.labels("KPI").set(number);
    }
}
