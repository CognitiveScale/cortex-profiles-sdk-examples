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

/**
 * Example CLI application that uses a secondary configuration, the `app-config.json`, to define a number of
 * catalog entities to be managed during execution
 */
@CommandLine.Command(name = "catalog-management", description = "Managing Catalog with Side-loaded Config", mixinStandardHelpOptions = true)
public class ManageCatalog extends RailedCommand {

    /**
     * Ingest a data source
     * @param cortexSession - the Cortex session
     * @param project - the data source project name
     * @param dataSourceName - the data source name
     */
    protected void buildDataSource(CortexSession cortexSession, String project, String dataSourceName) {
        IngestDataSourceJob ingestMemberBase = cortexSession.job().ingestDataSource(project, dataSourceName, cortexSession.getContext());
        ingestMemberBase.performFeatureCatalogCalculations = () -> false;
        ingestMemberBase.run();

    }

    /**
     * Handles user defined process after catalog management occurs, currently builds all defined profiles in app config.
     * May ingest data sources associated with the profiles if skipDataSource flag is not set.
     * @param project - the project name
     * @param cortexSession - the Cortex session
     * @param config - the loaded app config
     */
    @Override
    public void runApp(String project, CortexSession cortexSession, DocumentContext config) {
        List<Map> profiles = config.read(APP_PATH + ".profiles");
        Boolean skipDataSource = config.read("$.process.skipDataSource");
            // Iterate over profiles in config
        for (Map profile : profiles) {
            // Build primary data source
            String profileSchemaName = (String) profile.get("name");
            if(skipDataSource) {
                //Build profile directly from connection, not supported for streaming connections and requires
                //the profile schema to be built off a single data source. Profile schemas with multiple data sources
                //cannot be built directly from a connection.
                System.out.println("Building profile: " + profileSchemaName);
                BuildProfileJob buildProfileJob = cortexSession.job().buildProfile(project, profileSchemaName, cortexSession.getContext());
                buildProfileJob.performFeatureCatalogCalculations = () -> false;
                buildProfileJob.getDataset = (p, n) -> IngestDataSourceJob.DEFAULT_DATASOURCE_FORMATTER
                        .apply(cortexSession.read().connection(p, cortexSession.catalog().getDataSource(p, n).getConnection().getName()).load());
                buildProfileJob.run();
            } else {
                ProfileSchema profileSchema = cortexSession.catalog().getProfileSchema(project, profileSchemaName);

                //build primary datasource
                System.out.println("Ingesting Primary DataSource: " + profileSchema.getPrimarySource().getName());
                buildDataSource(cortexSession, project, profileSchema.getPrimarySource().getName());

                //build all joined datasources
                profileSchema.getJoins().forEach(join -> {
                    System.out.println("Ingesting Joined DataSource: " + join.getName());
                    buildDataSource(cortexSession, project, join.getName());
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

