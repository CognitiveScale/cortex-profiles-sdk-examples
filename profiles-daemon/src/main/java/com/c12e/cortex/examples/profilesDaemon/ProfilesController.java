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

import com.c12e.cortex.examples.profilesDaemon.requests.Payload;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="api/v1/profiles", method = { RequestMethod.POST, RequestMethod.GET })
public class ProfilesController {

    private final ProfilesService profilesService;
    private final LettuceConnectionFactory redisConnectionFactory;
    private final RedisClient redisClient;

    @Autowired
    public ProfilesController(ProfilesService profilesService, LettuceConnectionFactory redisConnectionFactory, RedisClient redisClient) { this.profilesService = profilesService;
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisClient = redisClient;
    }

    @GetMapping(path="{profileSchema}/{profileId}")
    public Map<String, Map<String, String>> getProfile(@PathVariable String profileSchema, @PathVariable String profileId) {
        /**
         * Fetches profile from a particular profileSchema and profileId
         */
        Map<String, String> response = profilesService.getProfileById(redisClient, profileSchema, profileId);
        return Map.of("payload", response);
    }

    @GetMapping(path="{profileSchema}")
    public Map<String, List> getProfileIds(@PathVariable String profileSchema) {
        /**
         * Fetches all profile Ids for a particular profileSchema
         */
        List<String> response = profilesService.getProfileIds(redisClient, profileSchema);
        return Map.of("payload", response);
    }

    @PostMapping(path="profileById")
    public Map<String, Map<String, String>> profileById(@RequestBody Payload payload) {
        /**
         * Fetches profile from a particular profileSchema and profileId
         */
        Map<String, String> response = profilesService.getProfileById(redisClient, payload.getPayload().get("profileSchema"), payload.getPayload().get("profileId"));
        return Map.of("payload", response);
    }

    @PostMapping(path="listProfileIds")
    public Map<String, List> listProfileIds(@RequestBody Payload payload) {
        /**
         * Fetches all profile Ids for a particular profileSchema
         */
        List<String> response = profilesService.getProfileIds(redisClient, payload.getPayload().get("profileSchema"));
        return Map.of("payload", response);
    }
}
