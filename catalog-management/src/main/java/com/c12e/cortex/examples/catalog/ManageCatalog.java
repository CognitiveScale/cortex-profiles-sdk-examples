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

package com.c12e.cortex.examples.catalog;

import com.c12e.cortex.phoenix.ProfileSchema;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.module.job.BuildProfileJob;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;
import com.jayway.jsonpath.DocumentContext;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "catalog-management", description = "Managing Catalog with Side-loaded Config", mixinStandardHelpOptions = true)
public class ManageCatalog extends RailedCommand {

    @Override
    public void runApp(String project, CortexSession cortexSession, DocumentContext config) {
        List<Map> profiles = config.read(APP_PATH + ".profiles");
        Boolean skipDataSource = config.read("$.process.skipDataSource");
            // Iterate over profiles in config
        for (Map profile : profiles) {
            // Build primary data source
            String profileSchemaName = (String) profile.get("name");

            if(skipDataSource) {
                //build profile directly from connection, not supported for streaming connections
                System.out.println("Building profile: " + profileSchemaName);
                BuildProfileJob buildProfileJob = cortexSession.job().buildProfile(project, profileSchemaName, cortexSession.getContext());
                buildProfileJob.performFeatureCatalogCalculations = () -> false;
                buildProfileJob.getDataset = (p, n) -> IngestDataSourceJob.DEFAULT_DATASOURCE_FORMATTER
                        .apply(cortexSession.read().connection(p, n).load()
                        );
                buildProfileJob.run();
            } else {
                ProfileSchema profileSchema = cortexSession.catalog().getProfileSchema(project, profileSchemaName);

                //build primary datasource
                System.out.println("Ingesting Primary DataSource: " + profileSchema.getPrimarySource().getName());
                IngestDataSourceJob ingestMemberBase = cortexSession.job().ingestDataSource(project, profileSchema.getPrimarySource().getName(), cortexSession.getContext());
                ingestMemberBase.performFeatureCatalogCalculations = () -> false;
                ingestMemberBase.run();

                //build all joined datasources
                profileSchema.getJoins().forEach(join -> {
                    System.out.println("Ingesting Joined DataSource: " + join.getName());
                    IngestDataSourceJob ingestJoin = cortexSession.job().ingestDataSource(project, join.getName(), cortexSession.getContext());
                    ingestJoin.performFeatureCatalogCalculations = () -> false;
                    ingestJoin.run();
                });

                // Build profile
                System.out.println("Building profile: " + profileSchemaName);
                BuildProfileJob buildProfileJob = cortexSession.job().buildProfile(project, profileSchemaName, cortexSession.getContext());
                buildProfileJob.performFeatureCatalogCalculations = () -> false;
                buildProfileJob.run();
            }
        }
    }
}

