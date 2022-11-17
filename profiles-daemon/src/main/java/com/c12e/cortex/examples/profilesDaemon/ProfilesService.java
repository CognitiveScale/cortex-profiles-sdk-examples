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

package com.c12e.cortex.examples.profilesDaemon;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProfilesService {

    public Map<String, String> getProfileById(RedisClient redisClient, String profileSchema, String profileId) {
        StatefulRedisConnection<String, String> connection
                = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        Map data = syncCommands.hgetall(profileSchema+":"+profileId);
        connection.close();

        return data;
    }

    public List<String> getProfileIds(RedisClient redisClient, String profileSchema) {
        StatefulRedisConnection<String, String> connection
                = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        List<String> data = syncCommands.keys(profileSchema+"*");
        connection.close();

        return data;
    }
}
